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

import io.spine.core.UserId;
import io.spine.examples.chatspn.AccountCreationId;
import io.spine.examples.chatspn.ChatDeletionId;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.MessageId;
import io.spine.examples.chatspn.account.command.CreateAccount;
import io.spine.examples.chatspn.chat.ChatPreview;
import io.spine.examples.chatspn.chat.PersonalChatView;
import io.spine.examples.chatspn.chat.command.CreatePersonalChat;
import io.spine.examples.chatspn.chat.command.DeleteChat;
import io.spine.examples.chatspn.message.MessageView;
import io.spine.examples.chatspn.message.command.EditMessage;
import io.spine.examples.chatspn.message.command.SendMessage;
import io.spine.testing.core.given.GivenUserId;

import static io.spine.examples.chatspn.server.given.GivenEmailAddress.randomEmailAddress;
import static io.spine.testing.TestValues.randomString;

public final class TestUserEnv {

    /**
     * Prevents class instantiation.
     */
    private TestUserEnv() {
    }

    public static CreateAccount createAccount() {
        var command = CreateAccount
                .newBuilder()
                .setId(AccountCreationId.generate())
                .setUser(GivenUserId.generated())
                .setEmail(randomEmailAddress())
                .setName(randomString())
                .vBuild();
        return command;
    }

    public static CreatePersonalChat createPersonalChat(UserId creator, UserId member) {
        var command = CreatePersonalChat
                .newBuilder()
                .setId(ChatId.generate())
                .setCreator(creator)
                .setMember(member)
                .vBuild();
        return command;
    }

    public static ChatPreview chatPreview(CreatePersonalChat command) {
        var view = PersonalChatView
                .newBuilder()
                .setCreator(command.getCreator())
                .setMember(command.getMember())
                .vBuild();
        var chatPreview = ChatPreview
                .newBuilder()
                .setId(command.getId())
                .setPersonalChat(view)
                .vBuild();
        return chatPreview;
    }

    public static SendMessage sendMessage(ChatId chatId, UserId userId) {
        var sendMessage = SendMessage
                .newBuilder()
                .setId(MessageId.generate())
                .setChat(chatId)
                .setUser(userId)
                .setContent(randomString())
                .vBuild();
        return sendMessage;
    }

    public static MessageView messageView(SendMessage command) {
        var messageView = MessageView
                .newBuilder()
                .setId(command.getId())
                .setChat(command.getChat())
                .setUser(command.getUser())
                .setContent(command.getContent())
                .buildPartial();
        return messageView;
    }

    public static EditMessage editMessageCommand(MessageView message) {
        var editMessage = EditMessage
                .newBuilder()
                .setId(message.getId())
                .setUser(message.getUser())
                .setChat(message.getChat())
                .setSuggestedContent(randomString())
                .vBuild();
        return editMessage;
    }

    public static MessageView messageView(EditMessage command) {
        var messageView = MessageView
                .newBuilder()
                .setId(command.getId())
                .setChat(command.getChat())
                .setUser(command.getUser())
                .setContent(command.getSuggestedContent())
                .buildPartial();
        return messageView;
    }

    public static DeleteChat deleteChatCommand(ChatId chat, UserId user) {
        var deletionId = ChatDeletionId
                .newBuilder()
                .setId(chat)
                .vBuild();
        var deleteChat = DeleteChat
                .newBuilder()
                .setId(deletionId)
                .setWhoDeletes(user)
                .vBuild();
        return deleteChat;
    }
}
