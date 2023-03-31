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

package io.spine.examples.chatspn.server.message.given;

import io.spine.core.UserId;
import io.spine.examples.chatspn.MessageId;
import io.spine.examples.chatspn.chat.Chat;
import io.spine.examples.chatspn.message.Message;
import io.spine.examples.chatspn.message.command.SendMessage;
import io.spine.examples.chatspn.message.event.MessagePosted;
import io.spine.examples.chatspn.message.event.MessageSent;
import io.spine.examples.chatspn.message.rejection.SendingRejections.MessageCannotBeSent;

import static io.spine.testing.TestValues.randomString;

public final class MessageSendingTestEnv {

    /**
     * Prevents class instantiation.
     */
    public MessageSendingTestEnv() {
    }

    public static SendMessage randomSendMessageCommand(Chat chat) {
        SendMessage command = SendMessage
                .newBuilder()
                .setId(MessageId.generate())
                .setUser(chat.getMember(0))
                .setChat(chat.getId())
                .setContent(randomString())
                .vBuild();
        return command;
    }

    public static SendMessage sendMessageCommandWith(Chat chat, UserId userId) {
        SendMessage command = SendMessage
                .newBuilder()
                .setId(MessageId.generate())
                .setUser(userId)
                .setChat(chat.getId())
                .setContent(randomString())
                .vBuild();
        return command;
    }

    public static MessageSent messageSentFrom(SendMessage c) {
        MessageSent event = MessageSent
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setContent(c.getContent())
                .vBuild();
        return event;
    }

    public static MessagePosted messagePostedFrom(SendMessage c) {
        MessagePosted event = MessagePosted
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setContent(c.getContent())
                .buildPartial();
        return event;
    }

    public static Message messageFrom(SendMessage c) {
        Message state = Message
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setContent(c.getContent())
                .buildPartial();
        return state;
    }

    public static MessageCannotBeSent messageCannotBeSentFrom(SendMessage c) {
        MessageCannotBeSent rejection = MessageCannotBeSent
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setContent(c.getContent())
                .vBuild();
        return rejection;
    }
}
