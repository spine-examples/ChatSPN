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

package io.spine.examples.chatspn.server.e2e.given;

import com.google.common.collect.ImmutableList;
import io.spine.base.EntityColumn;
import io.spine.client.Client;
import io.spine.core.UserId;
import io.spine.examples.chatspn.AccountCreationId;
import io.spine.examples.chatspn.ChatDeletionId;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.MessageId;
import io.spine.examples.chatspn.MessageRemovalId;
import io.spine.examples.chatspn.account.UserChats;
import io.spine.examples.chatspn.account.UserProfile;
import io.spine.examples.chatspn.account.command.CreateAccount;
import io.spine.examples.chatspn.chat.ChatPreview;
import io.spine.examples.chatspn.chat.MessagePreview;
import io.spine.examples.chatspn.chat.PersonalChatView;
import io.spine.examples.chatspn.chat.command.CreatePersonalChat;
import io.spine.examples.chatspn.chat.command.DeleteChat;
import io.spine.examples.chatspn.message.MessageView;
import io.spine.examples.chatspn.message.command.EditMessage;
import io.spine.examples.chatspn.message.command.RemoveMessage;
import io.spine.examples.chatspn.message.command.SendMessage;
import io.spine.net.EmailAddress;
import io.spine.testing.core.given.GivenUserId;

import static io.spine.client.OrderBy.Direction.ASCENDING;
import static io.spine.client.QueryFilter.eq;
import static io.spine.examples.chatspn.server.given.GivenEmailAddress.randomEmailAddress;
import static io.spine.testing.TestValues.randomString;

public final class PersonalChatTestEnv {

    private PersonalChatTestEnv() {
    }

    public static UserProfile createRandomAccount(Client client) {
        CreateAccount createAccount = CreateAccount
                .newBuilder()
                .setId(AccountCreationId.generate())
                .setUser(GivenUserId.generated())
                .setEmail(randomEmailAddress())
                .setName(randomString())
                .vBuild();
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

    public static ChatPreview createPersonalChat(UserId creator, UserId member, Client client) {
        CreatePersonalChat createPersonalChat = CreatePersonalChat
                .newBuilder()
                .setId(ChatId.generate())
                .setCreator(creator)
                .setMember(member)
                .vBuild();
        client.onBehalfOf(creator)
              .command(createPersonalChat)
              .postAndForget();
        PersonalChatView view = PersonalChatView
                .newBuilder()
                .setCreator(creator)
                .setMember(member)
                .vBuild();
        ChatPreview chatPreview = ChatPreview
                .newBuilder()
                .setId(createPersonalChat.getId())
                .setPersonalChat(view)
                .vBuild();
        return chatPreview;
    }

    public static MessageView sendMessage(ChatId chatId, UserId userId, Client client) {
        SendMessage sendMessage = SendMessage
                .newBuilder()
                .setId(MessageId.generate())
                .setChat(chatId)
                .setUser(userId)
                .setContent(randomString())
                .vBuild();
        client.onBehalfOf(userId)
              .command(sendMessage)
              .postAndForget();
        MessageView messageView = MessageView
                .newBuilder()
                .setId(sendMessage.getId())
                .setChat(chatId)
                .setUser(userId)
                .setContent(sendMessage.getContent())
                .buildPartial();
        return messageView;
    }

    public static MessageView editMessage(MessageView message, Client client) {
        EditMessage editMessage = EditMessage
                .newBuilder()
                .setId(message.getId())
                .setUser(message.getUser())
                .setChat(message.getChat())
                .setSuggestedContent(randomString())
                .vBuild();
        client.onBehalfOf(message.getUser())
              .command(editMessage)
              .postAndForget();
        MessageView messageView = message
                .toBuilder()
                .setContent(editMessage.getSuggestedContent())
                .vBuild();
        return messageView;
    }

    public static void removeMessage(MessageView message, Client client) {
        MessageRemovalId removalId = MessageRemovalId
                .newBuilder()
                .setId(message.getId())
                .vBuild();
        RemoveMessage removeMessage = RemoveMessage
                .newBuilder()
                .setId(removalId)
                .setChat(message.getChat())
                .setUser(message.getUser())
                .vBuild();
        client.onBehalfOf(message.getUser())
              .command(removeMessage)
              .postAndForget();
    }

    public static void deleteChat(ChatId chat, UserId user, Client client) {
        ChatDeletionId deletionId = ChatDeletionId
                .newBuilder()
                .setId(chat)
                .vBuild();
        DeleteChat deleteChat = DeleteChat
                .newBuilder()
                .setId(deletionId)
                .setWhoDeletes(user)
                .vBuild();
        client.onBehalfOf(user)
              .command(deleteChat)
              .postAndForget();
    }

    public static UserChats userChats(UserId userId, ChatPreview... chats) {
        UserChats userChats = UserChats
                .newBuilder()
                .setId(userId)
                .addAllChat(ImmutableList.copyOf(chats))
                .vBuild();
        return userChats;
    }

    public static ChatPreview chatPreview(ChatPreview chat, MessageView message) {
        MessagePreview messagePreview = MessagePreview
                .newBuilder()
                .setId(message.getId())
                .setUser(message.getUser())
                .setContent(message.getContent())
                .setWhenPosted(message.getWhenPosted())
                .buildPartial();
        ChatPreview chatPreview = chat
                .toBuilder()
                .setLastMessage(messagePreview)
                .vBuild();
        return chatPreview;
    }

    public static ChatPreview emptyChatPreview(ChatPreview chat) {
        ChatPreview chatPreview = chat
                .toBuilder()
                .setLastMessage(MessagePreview.getDefaultInstance())
                .vBuild();
        return chatPreview;
    }

    public static UserProfile findProfile(EmailAddress email, Client client) {
        ImmutableList<UserProfile> profiles = client
                .asGuest()
                .select(UserProfile.class)
                .where(eq(new EntityColumn("email"), email))
                .run();
        return profiles.get(0);
    }

    public static ChatPreview findChatPreview(ChatId chatId, Client client) {
        ImmutableList<ChatPreview> chatPreviews = client
                .asGuest()
                .select(ChatPreview.class)
                .byId(chatId)
                .run();
        return chatPreviews.get(0);
    }

    public static UserChats findUserChats(UserId userId, Client client) {
        ImmutableList<UserChats> userChatsList = client
                .asGuest()
                .select(UserChats.class)
                .byId(userId)
                .run();
        return userChatsList.get(0);
    }

    public static MessageView findMessageView(MessageId id, Client client) {
        ImmutableList<MessageView> userChatsList = client
                .asGuest()
                .select(MessageView.class)
                .byId(id)
                .run();
        return userChatsList.get(0);
    }

    public static ImmutableList<MessageView> readMessagesInChat(ChatId chat, Client client) {
        ImmutableList<MessageView> userChatsList = client
                .asGuest()
                .select(MessageView.class)
                .where(eq(new EntityColumn("chat"), chat))
                .orderBy(new EntityColumn("when_posted"), ASCENDING)
                .run();
        return userChatsList;
    }
}
