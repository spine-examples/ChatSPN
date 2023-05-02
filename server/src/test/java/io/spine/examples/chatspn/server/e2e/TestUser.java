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
import com.google.protobuf.Message;
import io.spine.base.CommandMessage;
import io.spine.base.EntityColumn;
import io.spine.base.EntityState;
import io.spine.base.EntityStateField;
import io.spine.client.Client;
import io.spine.client.EntityStateFilter;
import io.spine.core.UserId;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.MessageId;
import io.spine.examples.chatspn.account.UserChats;
import io.spine.examples.chatspn.account.UserProfile;
import io.spine.examples.chatspn.message.MessageView;
import io.spine.net.EmailAddress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static io.spine.client.OrderBy.Direction.ASCENDING;
import static io.spine.client.QueryFilter.eq;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.chatPreview;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.createAccount;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.createPersonalChat;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.deleteChatCommand;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.editMessageCommand;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.messageView;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.sendMessage;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;

/**
 * Registered user with API for testing purposes.
 */
public final class TestUser {

    private final Client client;
    private final UserProfile user;
    private final Map<ChatId, Conversation> conversations = new HashMap<>();

    public TestUser(Client client) {
        this.client = client;
        user = registerUser();
    }

    /**
     * Returns user ID.
     */
    public UserId userId() {
        return user.getId();
    }

    /**
     * Returns user email.
     */
    public EmailAddress email() {
        return user.getEmail();
    }

    /**
     * Returns user profile.
     */
    public UserProfile profile() {
        return user;
    }

    /**
     * Creates personal chat with provided user.
     */
    public Conversation createPersonalChatWith(TestUser user) {
        var command = createPersonalChat(userId(), user.userId());
        postCommand(command);
        var chatPreview = chatPreview(command);
        var conversation = new Conversation();
        conversations.put(chatPreview.getId(), conversation);
        user.conversations.put(chatPreview.getId(), conversation);
        return conversation;
    }

    /**
     * Sends random message to the provided chat on behalf of this user.
     */
    public MessageView sendMessageTo(ChatId chat) {
        var command = sendMessage(chat, userId());
        var messageView = messageView(command);
        conversations.get(chat)
                     .send(messageView);
        postCommand(command);
        return messageView;
    }

    /**
     * Changes content of the provided message to random on behalf of this user.
     */
    public MessageView editMessage(MessageView message) {
        var command = editMessageCommand(message);
        var messageView = messageView(command);
        conversations.get(message.getChat())
                     .edit(messageView);
        postCommand(command);
        return messageView;
    }

    /**
     * Deletes the chat with provided ID on behalf of this user.
     */
    public void deleteChat(ChatId chat) {
        var command = deleteChatCommand(chat, userId());
        postCommand(command);
    }

    /**
     * Returns {@code UserChats} of this user.
     */
    public UserChats readChats() {
        var userChatsList = client
                .onBehalfOf(userId())
                .select(UserChats.class)
                .byId(userId())
                .run();
        return userChatsList.get(0);
    }

    /**
     * Returns messages from the provided chat.
     */
    public ImmutableList<MessageView> readMessagesIn(ChatId chat) {
        var chatField = MessageView.Field
                .chat()
                .getField()
                .toString();
        var whenPostedField = MessageView.Field
                .whenPosted()
                .getField()
                .toString();
        var userChatsList = client
                .onBehalfOf(userId())
                .select(MessageView.class)
                .where(eq(new EntityColumn(chatField), chat))
                .orderBy(new EntityColumn(whenPostedField), ASCENDING)
                .run();
        return userChatsList;
    }

    /**
     * Returns the user profile with the provided email address.
     */
    public UserProfile findUserBy(EmailAddress email) {
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
     * Creates an observer for the chats of this user.
     */
    public Observer<UserChats> observeChats() {
        return new Observer<>(UserChats.class, userId());
    }

    /**
     * Creates an observer for messages in the provided chat.
     */
    public Observer<MessageView> observeMessagesIn(ChatId chat) {
        var chatField = MessageView.Field
                .chat()
                .getField();
        var filter = EntityStateFilter.eq(new EntityStateField(chatField), chat);
        var observer = new Observer<>(MessageView.class, filter);
        conversations.get(chat)
                     .subscribe(observer);
        return observer;
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
     * Registers the new user.
     */
    private UserProfile registerUser() {
        var createAccount = createAccount();
        client.onBehalfOf(createAccount.getUser())
              .command(createAccount)
              .postAndForget();
        var userProfile = UserProfile
                .newBuilder()
                .setId(createAccount.getUser())
                .setEmail(createAccount.getEmail())
                .setName(createAccount.getName())
                .vBuild();
        return userProfile;
    }

    /**
     * Observer for the provided entity state.
     */
    public final class Observer<S extends EntityState> {

        private final ArrayList<CompletableFuture<S>> futureList = new ArrayList<>();
        private final AtomicInteger lastCompletedIndex = new AtomicInteger(-1);

        /**
         * Creates an observer for the entity with the provided ID.
         */
        private <I extends Message> Observer(Class<S> type, I id) {
            client.onBehalfOf(userId())
                  .subscribeTo(type)
                  .byId(id)
                  .observe(this::observationAction)
                  .post();
        }

        /**
         * Creates an observer for the entity that passed provided filters.
         */
        private Observer(Class<S> type, EntityStateFilter... filters) {
            client.onBehalfOf(userId())
                  .subscribeTo(type)
                  .where(filters)
                  .observe(this::observationAction)
                  .post();
        }

        /**
         * Updates the observer's state in response to an observed entity update.
         *
         * <p>Even if entity update is not expected the observer's state
         * will be updated.
         */
        private void observationAction(S state) {
            if (futureList.isEmpty() || futureList.get(futureList.size() - 1)
                                                  .isDone()) {
                futureList.add(new CompletableFuture<>());
            }
            var index = lastCompletedIndex.incrementAndGet();
            futureList.get(index)
                      .complete(state);
        }

        /**
         * Tells to observer that entity will be updated soon.
         */
        private void expectUpdate() {
            futureList.add(new CompletableFuture<>());
        }

        /**
         * Returns the last entity state.
         */
        public S lastState() {
            if (futureList.isEmpty()) {
                futureList.add(new CompletableFuture<>());
            }
            try {
                var future = futureList.get(futureList.size() - 1);
                return future.get(10, SECONDS);

            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw newIllegalStateException("`CompletableFuture` can't be completed", e);
            }
        }

        /**
         * Returns entity states after each update since the observer was created.
         */
        public List<S> allStates() {
            var states = futureList
                    .stream()
                    .map(future -> {
                        try {
                            return future.get(10, SECONDS);
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            throw newIllegalStateException(e,
                                                           "`CompletableFuture` can't be completed");
                        }
                    })
                    .collect(toList());
            return states;
        }
    }

    /**
     * Expected state of messages in the chat.
     *
     * <p>It must be only one for each chat and updated before the real chat.
     */
    public final class Conversation {

        private final Map<MessageId, MessageView> messages = new LinkedHashMap<>();
        private final List<Observer<MessageView>> observers = new ArrayList<>();

        /**
         * Prevents instantiation outside the parent class.
         */
        private Conversation() {
        }

        /**
         * Subscribes observer on the conversation changes.
         *
         * <p>Subscribed observers will be forced to expect updates after each
         * update of the conversation.
         */
        private void subscribe(Observer<MessageView> observer) {
            observers.add(observer);
        }

        /**
         * Update conversation with new message.
         */
        private void send(MessageView messageView) {
            messages.put(messageView.getId(), messageView);
            observers.forEach(Observer::expectUpdate);
        }

        /**
         * Update conversation with edited message.
         */
        private void edit(MessageView messageView) {
            messages.put(messageView.getId(), messageView);
            observers.forEach(Observer::expectUpdate);
        }

        /**
         * Returns the expected chat messages.
         */
        public List<MessageView> messages() {
            return ImmutableList.copyOf(messages.values());
        }
    }
}
