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

package io.spine.examples.chatspn.server.chat.given;

import com.google.common.collect.ImmutableList;
import io.spine.core.UserId;
import io.spine.examples.chatspn.ChatDeletionId;
import io.spine.examples.chatspn.MessageId;
import io.spine.examples.chatspn.chat.Chat;
import io.spine.examples.chatspn.chat.command.DeleteChat;
import io.spine.examples.chatspn.chat.event.ChatDeleted;
import io.spine.examples.chatspn.chat.event.ChatDeletionFailed;
import io.spine.examples.chatspn.chat.event.ChatMarkedAsDeleted;
import io.spine.examples.chatspn.chat.rejection.DeletionRejections.ChatCannotBeMarkedAsDeleted;
import io.spine.examples.chatspn.message.Message;
import io.spine.examples.chatspn.message.command.SendMessage;
import io.spine.testing.server.blackbox.BlackBoxContext;

import java.util.ArrayList;
import java.util.List;

public final class ChatDeletionTestEnv {

    /**
     * Prevents class instantiation.
     */
    private ChatDeletionTestEnv() {
    }

    public static List<Message> sendMessagesTo(Chat chat, BlackBoxContext context) {
        List<Message> messages = new ArrayList<>();
        for (var i = 0; i < 3; i++) {
            var message = Message
                    .newBuilder()
                    .setId(MessageId.generate())
                    .setChat(chat.getId())
                    .setContent("Hello, this is my message text")
                    .setUser(chat.getMember(0)
                                 .getId())
                    .buildPartial();
            var sendMessage = SendMessage
                    .newBuilder()
                    .setId(message.getId())
                    .setChat(message.getChat())
                    .setContent(message.getContent())
                    .setUser(message.getUser())
                    .vBuild();
            context.receivesCommand(sendMessage);
            messages.add(message);
        }
        return ImmutableList.copyOf(messages);
    }

    public static DeleteChat deleteChatCommand(Chat chat, UserId whoDeletes) {
        var command = DeleteChat
                .newBuilder()
                .setId(chatDeletionId(chat))
                .setWhoDeletes(whoDeletes)
                .vBuild();
        return command;
    }

    public static ChatDeleted chatDeletedFrom(DeleteChat c, Chat chat) {
        var event = ChatDeleted
                .newBuilder()
                .setId(c.getId())
                .setWhoDeleted(c.getWhoDeletes())
                .addAllMember(chat.getMemberList())
                .vBuild();
        return event;
    }

    public static ChatMarkedAsDeleted chatMarkedAsDeletedFrom(DeleteChat c, Chat chat) {
        var event = ChatMarkedAsDeleted
                .newBuilder()
                .setId(c.chat())
                .setWhoDeleted(c.getWhoDeletes())
                .addAllMember(chat.getMemberList())
                .vBuild();
        return event;
    }

    public static ChatCannotBeMarkedAsDeleted chatCannotBeMarkedAsDeletedFrom(DeleteChat c) {
        var rejection = ChatCannotBeMarkedAsDeleted
                .newBuilder()
                .setId(c.chat())
                .setWhoDeletes(c.getWhoDeletes())
                .vBuild();
        return rejection;
    }

    public static ChatDeletionFailed chatDeletionFailedFrom(DeleteChat c) {
        var event = ChatDeletionFailed
                .newBuilder()
                .setId(c.getId())
                .setWhoDeletes(c.getWhoDeletes())
                .vBuild();
        return event;
    }

    static ChatDeletionId chatDeletionId(Chat chat) {
        return ChatDeletionId
                .newBuilder()
                .setId(chat.getId())
                .vBuild();
    }
}
