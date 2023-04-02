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
import io.spine.examples.chatspn.message.Message;
import io.spine.examples.chatspn.message.MessageView;
import io.spine.examples.chatspn.message.command.RemoveMessage;
import io.spine.examples.chatspn.message.event.MessageMarkedAsDeleted;
import io.spine.examples.chatspn.message.event.MessageRemovalFailed;
import io.spine.examples.chatspn.message.event.MessageRemoved;
import io.spine.examples.chatspn.message.rejection.RemovalRejections.MessageCannotBeMarkedAsDeleted;
import io.spine.examples.chatspn.message.rejection.RemovalRejections.MessageCannotBeRemoved;

public final class MessageRemovalTestEnv {

    /**
     * Prevents class instantiation.
     */
    private MessageRemovalTestEnv() {
    }

    public static RemoveMessage removeMessageCommand(Message message) {
        RemoveMessage command = RemoveMessage
                .newBuilder()
                .setId(message.getId())
                .setChat(message.getChat())
                .setUser(message.getUser())
                .vBuild();
        return command;
    }

    public static RemoveMessage removeMessageCommandWith(Message message, UserId userId) {
        RemoveMessage command = RemoveMessage
                .newBuilder()
                .setId(message.getId())
                .setChat(message.getChat())
                .setUser(userId)
                .vBuild();
        return command;
    }

    public static RemoveMessage removeMessageCommandWith(Message message, MessageId messageId) {
        RemoveMessage command = RemoveMessage
                .newBuilder()
                .setId(messageId)
                .setChat(message.getChat())
                .setUser(message.getUser())
                .vBuild();
        return command;
    }

    public static MessageRemoved messageRemovedFrom(RemoveMessage c) {
        MessageRemoved event = MessageRemoved
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .vBuild();
        return event;
    }

    public static MessageRemovalFailed messageRemovalFailedFrom(RemoveMessage c) {
        MessageRemovalFailed event = MessageRemovalFailed
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .vBuild();
        return event;
    }

    public static MessageMarkedAsDeleted messageMarkedAsDeletedFrom(RemoveMessage c) {
        MessageMarkedAsDeleted event = MessageMarkedAsDeleted
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .vBuild();
        return event;
    }

    public static Message messageFrom(RemoveMessage c) {
        Message state = Message
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .buildPartial();
        return state;
    }

    public static MessageCannotBeRemoved messageCannotBeRemovedFrom(RemoveMessage c) {
        MessageCannotBeRemoved rejection = MessageCannotBeRemoved
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .vBuild();
        return rejection;
    }

    public static MessageCannotBeMarkedAsDeleted messageCannotBeMarkedAsRemovedFrom(
            RemoveMessage c) {
        MessageCannotBeMarkedAsDeleted rejection = MessageCannotBeMarkedAsDeleted
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .vBuild();
        return rejection;
    }
}
