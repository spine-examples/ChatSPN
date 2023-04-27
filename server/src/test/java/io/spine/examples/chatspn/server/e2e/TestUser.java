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
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.spine.base.CommandMessage;
import io.spine.base.EntityColumn;
import io.spine.client.Client;
import io.spine.core.UserId;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.account.UserChats;
import io.spine.examples.chatspn.account.UserProfile;
import io.spine.examples.chatspn.account.command.CreateAccount;
import io.spine.examples.chatspn.chat.ChatPreview;
import io.spine.examples.chatspn.chat.command.CreatePersonalChat;
import io.spine.examples.chatspn.chat.command.DeleteChat;
import io.spine.examples.chatspn.message.MessageView;
import io.spine.examples.chatspn.message.command.EditMessage;
import io.spine.examples.chatspn.message.command.RemoveMessage;
import io.spine.examples.chatspn.message.command.SendMessage;
import io.spine.net.EmailAddress;

import static io.grpc.ManagedChannelBuilder.forAddress;
import static io.grpc.Status.CANCELLED;
import static io.spine.client.Client.usingChannel;
import static io.spine.client.OrderBy.Direction.ASCENDING;
import static io.spine.client.QueryFilter.eq;
import static io.spine.examples.chatspn.server.e2e.ServerRunningTest.TEST_SERVER_PORT;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.chatPreview;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.createAccount;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.createPersonalChat;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.deleteChatCommand;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.editMessageCommand;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.messageView;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.removeMessageCommand;
import static io.spine.examples.chatspn.server.e2e.given.TestUserEnv.sendMessage;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Registered user with own {@link Client}
 * and API to send commands and read projections.
 */
public class TestUser {

    private static final String ADDRESS = "localhost";
    private final ManagedChannel channel;
    private final Client client;
    private final UserProfile user;

    public TestUser() {
        channel = forAddress(ADDRESS, TEST_SERVER_PORT)
                .usePlaintext()
                .build();
        client = usingChannel(channel).build();
        user = registerUser();
    }

    public UserId userId() {
        return user.getId();
    }

    public EmailAddress email() {
        return user.getEmail();
    }

    public UserProfile profile() {
        return user;
    }

    public ChatPreview createPersonalChatWith(UserId member) {
        CreatePersonalChat command = createPersonalChat(userId(), member);
        postCommand(command);
        ChatPreview chatPreview = chatPreview(command);
        return chatPreview;
    }

    public MessageView sendMessageTo(ChatId chat) {
        SendMessage command = sendMessage(chat, userId());
        postCommand(command);
        MessageView messageView = messageView(command);
        return messageView;
    }

    public MessageView editMessage(MessageView message) {
        EditMessage command = editMessageCommand(message);
        postCommand(command);
        MessageView messageView = messageView(command);
        return messageView;
    }

    public void removeMessage(MessageView message) {
        RemoveMessage command = removeMessageCommand(message);
        postCommand(command);
    }

    public void deleteChat(ChatId chat) {
        DeleteChat command = deleteChatCommand(chat, userId());
        postCommand(command);
    }

    public UserChats readChats() {
        ImmutableList<UserChats> userChatsList = client
                .onBehalfOf(userId())
                .select(UserChats.class)
                .byId(userId())
                .run();
        return userChatsList.get(0);
    }

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

    public void closeConnection() throws InterruptedException {
        try {
            client.close();
        } catch (StatusRuntimeException e) {
            if (e.getStatus()
                 .equals(CANCELLED)) {
                fail(e);
            }
        }
        channel.shutdown();
        channel.awaitTermination(1, SECONDS);
    }

    private void postCommand(CommandMessage command) {
        client.onBehalfOf(user.getId())
              .command(command)
              .postAndForget();
    }

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
}
