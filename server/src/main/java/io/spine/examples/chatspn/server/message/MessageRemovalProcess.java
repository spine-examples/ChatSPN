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

package io.spine.examples.chatspn.server.message;

import io.spine.core.CommandContext;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.MessageRemovalId;
import io.spine.examples.chatspn.message.MessageRemoval;
import io.spine.examples.chatspn.message.command.MarkMessageAsDeleted;
import io.spine.examples.chatspn.message.command.RemoveMessage;
import io.spine.examples.chatspn.message.event.MessageMarkedAsDeleted;
import io.spine.examples.chatspn.message.event.MessageRemovalFailed;
import io.spine.examples.chatspn.message.event.MessageRemoved;
import io.spine.examples.chatspn.message.rejection.MessageCannotBeRemoved;
import io.spine.examples.chatspn.message.rejection.RemovalRejections.MessageCannotBeMarkedAsDeleted;
import io.spine.examples.chatspn.server.ProjectionReader;
import io.spine.server.command.Command;
import io.spine.server.event.React;
import io.spine.server.procman.ProcessManager;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import static io.spine.examples.chatspn.message.MessageRemovalIdentifiersConverter.messageId;
import static io.spine.examples.chatspn.message.MessageRemovalIdentifiersConverter.messageRemovalId;
import static io.spine.examples.chatspn.message.MessageRemovalIdentifiersConverter.messageRemovalOperationId;

/**
 * Coordinates the message removal.
 */
public final class MessageRemovalProcess
        extends ProcessManager<MessageRemovalId, MessageRemoval, MessageRemoval.Builder> {

    /**
     * Checker for user existence in chat as a member.
     */
    @MonotonicNonNull
    private ChatMembers chatMembers;

    /**
     * Issues a command to mark message as deleted.
     *
     * @throws MessageCannotBeRemoved
     *         if the message remover is not a chat member
     */
    @Command
    MarkMessageAsDeleted on(RemoveMessage c, CommandContext ctx) throws MessageCannotBeRemoved {
        builder().setId(c.getId());
        if (chatMembers.isMember(c.getChat(), c.getUser(), ctx)) {
            return MarkMessageAsDeleted
                    .newBuilder()
                    .setId(messageId(c.getId()))
                    .setChat(c.getChat())
                    .setUser(c.getUser())
                    .setOperation(messageRemovalOperationId(c.getId()))
                    .vBuild();
        }
        throw MessageCannotBeRemoved
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .build();
    }

    /**
     * Archives the process when the message was removed.
     */
    @React
    MessageRemoved on(MessageMarkedAsDeleted e) {
        setArchived(true);
        return MessageRemoved
                .newBuilder()
                .setId(messageRemovalId(e.getId()))
                .setChat(e.getChat())
                .setUser(e.getUser())
                .vBuild();
    }

    /**
     * Archives the process when the message removal failed.
     */
    @React
    MessageRemovalFailed on(MessageCannotBeMarkedAsDeleted e) {
        setArchived(true);
        return MessageRemovalFailed
                .newBuilder()
                .setId(messageRemovalId(e.getId()))
                .setChat(e.getChat())
                .setUser(e.getUser())
                .vBuild();
    }

    void inject(ProjectionReader<ChatId, io.spine.examples.chatspn.chat.ChatMembers> reader) {
        chatMembers = new ChatMembers(reader);
    }
}
