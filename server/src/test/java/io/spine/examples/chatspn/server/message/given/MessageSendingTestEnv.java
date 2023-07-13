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
import io.spine.examples.chatspn.ChatCardId;
import io.spine.examples.chatspn.MessageId;
import io.spine.examples.chatspn.chat.Chat;
import io.spine.examples.chatspn.chat.ChatCard;
import io.spine.examples.chatspn.message.Message;
import io.spine.examples.chatspn.message.MessageView;
import io.spine.examples.chatspn.message.command.SendMessage;
import io.spine.examples.chatspn.message.event.MessagePosted;
import io.spine.examples.chatspn.message.event.MessageSent;
import io.spine.examples.chatspn.message.rejection.SendingRejections.MessageCannotBeSent;

import static io.spine.examples.chatspn.chat.Chat.ChatType.CT_GROUP;

public final class MessageSendingTestEnv {

    /**
     * Prevents class instantiation.
     */
    private MessageSendingTestEnv() {
    }

    public static SendMessage randomSendMessageCommand(Chat chat) {
        var command = SendMessage
                .newBuilder()
                .setId(MessageId.generate())
                .setUser(chat.getMember(0)
                             .getId())
                .setChat(chat.getId())
                .setContent("Hello, this is my message text")
                .vBuild();
        return command;
    }

    public static SendMessage sendMessageCommandWith(Chat chat, UserId userId) {
        var command = SendMessage
                .newBuilder()
                .setId(MessageId.generate())
                .setUser(userId)
                .setChat(chat.getId())
                .setContent("Hello, this is my message text")
                .vBuild();
        return command;
    }

    public static MessageSent messageSentFrom(SendMessage c) {
        var event = MessageSent
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setContent(c.getContent())
                .vBuild();
        return event;
    }

    public static MessagePosted messagePostedFrom(SendMessage c) {
        var event = MessagePosted
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setContent(c.getContent())
                .buildPartial();
        return event;
    }

    public static Message messageFrom(SendMessage c) {
        var state = Message
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setContent(c.getContent())
                .buildPartial();
        return state;
    }

    public static MessageView messageViewFrom(SendMessage c) {
        var state = MessageView
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setContent(c.getContent())
                .buildPartial();
        return state;
    }

    public static MessageCannotBeSent messageCannotBeSentFrom(SendMessage c) {
        var rejection = MessageCannotBeSent
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setContent(c.getContent())
                .vBuild();
        return rejection;
    }

    public static ChatCard chatCard(Chat chat, SendMessage command, UserId user) {
        var messageView = MessageView
                .newBuilder()
                .setId(command.getId())
                .setChat(command.getChat())
                .setUser(command.getUser())
                .setContent(command.getContent())
                .buildPartial();
        var chatCardId = ChatCardId
                .newBuilder()
                .setUser(user)
                .setChat(chat.getId())
                .vBuild();
        var state = ChatCard
                .newBuilder()
                .setCardId(chatCardId)
                .setChatId(chat.getId())
                .setViewer(user)
                .setName(chat.getName())
                .setType(CT_GROUP)
                .setLastMessage(messageView)
                .vBuild();
        return state;
    }
}
