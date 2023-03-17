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

package io.spine.examples.chatspn.server.given;

import io.spine.core.UserId;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.MessageId;
import io.spine.examples.chatspn.chat.Chat;
import io.spine.examples.chatspn.chat.command.CreateChat;
import io.spine.examples.chatspn.message.Message;
import io.spine.examples.chatspn.message.sendingcommand.SendMessage;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.BlackBoxContext;

import static io.spine.testing.TestValues.randomString;

public class MessageSendingTestEnv {

    /**
     * Prevents instantiation of this class.
     */
    private MessageSendingTestEnv() {

    }

    public static Chat createRandomChat(BlackBoxContext context) {
        Chat chat = Chat
                .newBuilder()
                .setId(ChatId.generate())
                .addMember(GivenUserId.generated())
                .addMember(GivenUserId.generated())
                .setName(randomString())
                .vBuild();
        CreateChat command = CreateChat
                .newBuilder()
                .setId(chat.getId())
                .setCreator(chat.getMember(0))
                .addMember(chat.getMember(1))
                .setName(chat.getName())
                .vBuild();
        context.receivesCommand(command);
        return chat;
    }

    public static Message sendMessage(ChatId chat, UserId user, BlackBoxContext context) {
        Message message = Message
                .newBuilder()
                .setId(MessageId.generate())
                .setChat(chat)
                .setUser(user)
                .setContent(randomString())
                .buildPartial();
        SendMessage command = SendMessage
                .newBuilder()
                .setId(message.getId())
                .setChat(chat)
                .setUser(user)
                .setContent(message.getContent())
                .vBuild();
        context.receivesCommand(command);
        return message;
    }
}
