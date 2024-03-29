/*
 * Copyright 2023, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.examples.chatspn.server.e2e;

import com.google.common.collect.ImmutableList;
import io.spine.base.CommandMessage;
import io.spine.base.EntityColumn;
import io.spine.base.EntityStateField;
import io.spine.client.Client;
import io.spine.client.EntityStateFilter;
import io.spine.client.EventFilter;
import io.spine.client.QueryFilter;
import io.spine.core.UserId;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.MessageId;
import io.spine.examples.chatspn.account.UserProfile;
import io.spine.examples.chatspn.chat.ChatCard;
import io.spine.examples.chatspn.message.MessageView;
import io.spine.examples.chatspn.message.event.MessageMarkedAsDeleted;
import io.spine.net.EmailAddress;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.client.OrderBy.Direction.ASCENDING;
import static io.spine.client.QueryFilter.eq;
import static io.spine.examples.chatspn.server.ExpectedOnlyAssertions.assertExpectedFields;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.chatMember;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.createAccount;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.createPersonalChat;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.deleteChatCommand;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.editMessageCommand;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.messageView;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.removeMessageCommand;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.sendMessageCommand;

/**
 * Registered user with API for testing purposes.
 */
final class TestUser {

    private final Client client;
    private final UserProfile user;
    private final List<ChatCard> chats;

    private TestUser(UserProfile user, Client client) {
        this.user = user;
        this.client = client;
        this.chats = readChats();
        observeChats();
    }

    /**
     * Registers the new user.
     */
    static TestUser registerUser(String name, String email, Client client) {
        var createAccount = createAccount(name, email);
        client.onBehalfOf(createAccount.getUser())
              .command(createAccount)
              .postAndForget();
        var userProfile = UserProfile
                .newBuilder()
                .setId(createAccount.getUser())
                .setEmail(createAccount.getEmail())
                .setName(name)
                .vBuild();
        var user = new TestUser(userProfile, client);
        return user;
    }

    /**
     * Returns user chats from the server-side.
     */
    private List<ChatCard> readChats() {
        var byCardOwnerFilter = QueryFilter.eq(
                ChatCard.Column.viewer(),
                userId()
        );
        var userChatsList = client
                .onBehalfOf(userId())
                .select(ChatCard.class)
                .where(byCardOwnerFilter)
                .byId(userId())
                .run();
        return new ArrayList<>(userChatsList);
    }

    /**
     * Subscribes to server-side changes in the user chats to update the read-side.
     */
    private void observeChats() {
        var byCardOwnerFilter = EntityStateFilter.eq(
                ChatCard.Field.viewer(),
                userId()
        );
        client.onBehalfOf(userId())
              .subscribeTo(ChatCard.class)
              .where(byCardOwnerFilter)
              .observe(updatedChatCard -> {
                  var optionalChatCard = chats
                          .stream()
                          .filter(card -> card.getCardId()
                                              .equals(updatedChatCard.getCardId()))
                          .findFirst();
                  optionalChatCard.ifPresent(chats::remove);
                  chats.add(updatedChatCard);
              })
              .post();
    }

    /**
     * Returns the user's ID.
     */
    private UserId userId() {
        return user.getId();
    }

    /**
     * Returns the user's email.
     */
    EmailAddress email() {
        return user.getEmail();
    }

    /**
     * Returns the chats in which user is a member.
     */
    List<ChatCard> chats() {
        return ImmutableList.copyOf(chats);
    }

    /**
     * Creates personal chat with provided user.
     */
    Conversation createPersonalChatWith(UserProfile memberProfile) {
        var creator = chatMember(userId(), user.getName());
        var member = chatMember(memberProfile.getId(), memberProfile.getName());
        var command = createPersonalChat(creator, member);
        postCommand(command);
        var conversation = new Conversation(command.getId());
        return conversation;
    }

    /**
     * Returns the conversation of the provided chat.
     */
    Conversation openChat(ChatId chat) {
        return new Conversation(chat);
    }

    /**
     * Returns the user profile with the provided email address.
     */
    UserProfile findUserBy(EmailAddress email) {
        var emailField = UserProfile.Field
                .email()
                .getField()
                .toString();
        var profiles = client
                .onBehalfOf(userId())
                .select(UserProfile.class)
                .where(eq(new EntityColumn(emailField), email))
                .run();
        return profiles.get(0);
    }

    /**
     * Posts the provided command.
     */
    private void postCommand(CommandMessage command) {
        client.onBehalfOf(user.getId())
              .command(command)
              .postAndForget();
    }

    /**
     * The read side of the chat for the user.
     */
    final class Conversation {

        private final ChatId chat;
        private final Map<MessageId, MessageView> messages;

        /**
         * Prevents instantiation outside the parent class.
         */
        private Conversation(ChatId chat) {
            this.chat = chat;
            messages = readMessages();
            observeMessages();
        }

        /**
         * Returns chat messages from the server.
         */
        private Map<MessageId, MessageView> readMessages() {
            var whenPostedField = MessageView.Field
                    .whenPosted()
                    .getField()
                    .toString();
            var messagesList = client
                    .onBehalfOf(userId())
                    .select(MessageView.class)
                    .where(chatQueryFilter(chat))
                    .orderBy(new EntityColumn(whenPostedField), ASCENDING)
                    .run();
            Map<MessageId, MessageView> messagesMap = new LinkedHashMap<>();
            messagesList.forEach(message -> messagesMap.put(message.getId(), message));
            return messagesMap;
        }

        /**
         * Subscribes to server-side changes in the chat messages to update the read side.
         */
        private void observeMessages() {
            client.onBehalfOf(userId())
                  .subscribeTo(MessageView.class)
                  .where(chatStateFilter(chat))
                  .observe(message -> messages.put(message.getId(), message))
                  .post();
            client.onBehalfOf(userId())
                  .subscribeToEvent(MessageMarkedAsDeleted.class)
                  .where(chatEventFilter(chat))
                  .observe(event -> messages.remove(event.getId()))
                  .post();
        }

        private EntityStateFilter chatStateFilter(ChatId chat) {
            var chatField = MessageView.Field
                    .chat()
                    .getField();
            return EntityStateFilter.eq(new EntityStateField(chatField), chat);
        }

        private EventFilter chatEventFilter(ChatId chat) {
            var chatField = MessageMarkedAsDeleted.Field
                    .chat();
            return EventFilter.eq(chatField, chat);
        }

        private QueryFilter chatQueryFilter(ChatId chat) {
            var chatField = MessageView.Field
                    .chat()
                    .getField()
                    .toString();
            return eq(new EntityColumn(chatField), chat);
        }

        /**
         * Sends a new message to the chat.
         */
        void sendMessage(String content) {
            var command = sendMessageCommand(chat, userId(), content);
            var messageView = messageView(command);
            postCommand(command);
            assertExpectedFields(messages.get(command.getId()), messageView);
        }

        /**
         * Edits the message in the chat.
         */
        void editMessage(MessageView message, String newContent) {
            var command = editMessageCommand(message, newContent);
            var newMessageView = messageView(command);
            postCommand(command);
            assertExpectedFields(messages.get(message.getId()), newMessageView);
        }

        /**
         * Removes the message in the chat.
         */
        void removeMessage(MessageView message) {
            var command = removeMessageCommand(message);
            postCommand(command);
            assertThat(messages.containsKey(message.getId()))
                    .isFalse();
        }

        /**
         * Returns the chat messages.
         */
        List<MessageView> messages() {
            return ImmutableList.copyOf(messages.values());
        }

        /**
         * Deletes the chat.
         */
        void deleteChat() {
            var command = deleteChatCommand(chat, userId());
            postCommand(command);
        }
    }
}
