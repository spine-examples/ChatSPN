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
import io.spine.examples.chatspn.MessageRemovalId;
import io.spine.examples.chatspn.account.command.CreateAccount;
import io.spine.examples.chatspn.chat.command.CreatePersonalChat;
import io.spine.examples.chatspn.chat.command.DeleteChat;
import io.spine.examples.chatspn.message.MessageView;
import io.spine.examples.chatspn.message.command.EditMessage;
import io.spine.examples.chatspn.message.command.RemoveMessage;
import io.spine.examples.chatspn.message.command.SendMessage;
import io.spine.net.EmailAddress;
import io.spine.testing.core.given.GivenUserId;

public final class TestUserEnv {

    /**
     * Prevents class instantiation.
     */
    private TestUserEnv() {
    }

    public static CreateAccount createAccount(String name, String email) {
        var emailAddress = EmailAddress
                .newBuilder()
                .setValue(email)
                .vBuild();
        var command = CreateAccount
                .newBuilder()
                .setId(AccountCreationId.generate())
                .setUser(GivenUserId.generated())
                .setEmail(emailAddress)
                .setName(name)
                .vBuild();
        return command;
    }

    public static CreatePersonalChat createPersonalChat(UserId creator, UserId member) {
        var command = CreatePersonalChat
                .newBuilder()
                .setId(ChatId.generate())
                .setCreator(creator)
                .setCreatorName("John Doe")
                .setMember(member)
                .setMemberName("Emma Smith")
                .vBuild();
        return command;
    }

    public static SendMessage sendMessageCommand(ChatId chatId, UserId userId, String content) {
        var sendMessage = SendMessage
                .newBuilder()
                .setId(MessageId.generate())
                .setChat(chatId)
                .setUser(userId)
                .setContent(content)
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

    public static EditMessage editMessageCommand(MessageView message, String newContent) {
        var editMessage = EditMessage
                .newBuilder()
                .setId(message.getId())
                .setUser(message.getUser())
                .setChat(message.getChat())
                .setSuggestedContent(newContent)
                .vBuild();
        return editMessage;
    }

    public static RemoveMessage removeMessageCommand(MessageView message) {
        var messageRemoval = MessageRemovalId
                .newBuilder()
                .setId(message.getId())
                .vBuild();
        var removeMessage = RemoveMessage
                .newBuilder()
                .setId(messageRemoval)
                .setUser(message.getUser())
                .setChat(message.getChat())
                .vBuild();
        return removeMessage;
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
