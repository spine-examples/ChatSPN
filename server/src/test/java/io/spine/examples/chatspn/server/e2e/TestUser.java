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
import io.spine.base.Field;
import io.spine.client.Client;
import io.spine.client.EntityStateFilter;
import io.spine.core.UserId;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.MessageId;
import io.spine.examples.chatspn.account.UserChats;
import io.spine.examples.chatspn.account.UserProfile;
import io.spine.examples.chatspn.account.command.CreateAccount;
import io.spine.examples.chatspn.chat.ChatPreview;
import io.spine.examples.chatspn.chat.command.CreatePersonalChat;
import io.spine.examples.chatspn.chat.command.DeleteChat;
import io.spine.examples.chatspn.message.MessageView;
import io.spine.examples.chatspn.message.command.EditMessage;
import io.spine.examples.chatspn.message.command.SendMessage;
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
        CreatePersonalChat command = createPersonalChat(userId(), user.userId());
        postCommand(command);
        ChatPreview chatPreview = chatPreview(command);
        Conversation conversation = new Conversation();
        conversations.put(chatPreview.getId(), conversation);
        user.conversations.put(chatPreview.getId(), conversation);
        return conversation;
    }

    /**
     * Sends random message to the provided chat on behalf of this user.
     */
    public MessageView sendMessageTo(ChatId chat) {
        SendMessage command = sendMessage(chat, userId());
        MessageView messageView = messageView(command);
        conversations.get(chat)
                     .send(messageView);
        postCommand(command);
        return messageView;
    }

    /**
     * Changes content of the provided message to random on behalf of this user.
     */
    public MessageView editMessage(MessageView message) {
        EditMessage command = editMessageCommand(message);
        MessageView messageView = messageView(command);
        conversations.get(message.getChat())
                     .edit(messageView);
        postCommand(command);
        return messageView;
    }

    /**
     * Deletes the chat with provided ID on behalf of this user.
     */
    public void deleteChat(ChatId chat) {
        DeleteChat command = deleteChatCommand(chat, userId());
        postCommand(command);
    }

    /**
     * Returns {@code UserChats} of this user.
     */
    public UserChats readChats() {
        ImmutableList<UserChats> userChatsList = client
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
        String chatField = MessageView.Field
                .chat()
                .getField()
                .toString();
        String whenPostedField = MessageView.Field
                .whenPosted()
                .getField()
                .toString();
        ImmutableList<MessageView> userChatsList = client
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
        String emailField = UserProfile.Field
                .email()
                .getField()
                .toString();
        ImmutableList<UserProfile> profiles = client
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
        Field chatField = MessageView.Field
                .chat()
                .getField();
        EntityStateFilter filter = EntityStateFilter.eq(new EntityStateField(chatField), chat);
        Observer<MessageView> observer = new Observer<>(MessageView.class, filter);
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
        CreateAccount createAccount = createAccount();
        client.onBehalfOf(createAccount.getUser())
              .command(createAccount)
              .postAndForget();
        UserProfile userProfile = UserProfile
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
         * Creates an observer on the entity with provided ID.
         */
        private <I extends Message> Observer(Class<S> type, I id) {
            client.onBehalfOf(userId())
                  .subscribeTo(type)
                  .byId(id)
                  .observe(this::observationAction)
                  .post();
        }

        /**
         * Creates an observer on the entity that passed provided filters.
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
         * @implNote Even if entity update is not expected the observer's state will be
         *         updated.
         */
        private void observationAction(S state) {
            if (futureList.isEmpty() || futureList.get(futureList.size() - 1)
                                                  .isDone()) {
                futureList.add(new CompletableFuture<>());
            }
            int index = lastCompletedIndex.incrementAndGet();
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
                CompletableFuture<S> future = futureList.get(futureList.size() - 1);
                return future.get(10, SECONDS);

            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw newIllegalStateException("`CompletableFuture` can't be completed", e);
            }
        }

        /**
         * Returns entity states after each update since the observer was created.
         */
        public List<S> allStates() {
            List<S> states = futureList
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
     */
    public final class Conversation {

        private final Map<MessageId, MessageView> messages = new LinkedHashMap<>();
        private final List<Observer<MessageView>> observers = new ArrayList<>();

        /**
         * Prevents instantiation from outside the parent class.
         */
        private Conversation() {
        }

        /**
         * Subscribes observer on the conversation changes.
         *
         * <p>Subscribed observers will be forced to expect updates after each
         * command that updates the conversation.
         */
        private void subscribe(Observer<MessageView> observer) {
            observers.add(observer);
        }

        /**
         * Update conversation by the new message sent.
         */
        private void send(MessageView messageView) {
            messages.put(messageView.getId(), messageView);
            observers.forEach(Observer::expectUpdate);
        }

        /**
         * Update conversation by the message edited.
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
