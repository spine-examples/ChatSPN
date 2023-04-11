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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.spine.core.CommandContext;
import io.spine.core.UserId;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.MessageId;
import io.spine.examples.chatspn.MessageRemovalId;
import io.spine.examples.chatspn.MessageRemovalOperationId;
import io.spine.examples.chatspn.chat.ChatMembers;
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

/**
 * Coordinates the message removal.
 */
public final class MessageRemovalProcess
        extends ProcessManager<MessageRemovalId, MessageRemoval, MessageRemoval.Builder> {

    /**
     * Reads chat members per chat.
     */
    @MonotonicNonNull
    private ProjectionReader<ChatId, ChatMembers> projectionReader;

    private static MessageId messageId(RemoveMessage e) {
        return e.getId()
                .getId();
    }

    private static MessageRemovalId removalId(MessageId id) {
        return MessageRemovalId
                .newBuilder()
                .setId(id)
                .vBuild();
    }

    /**
     * Issues a command to mark message as deleted.
     *
     * @throws MessageCannotBeRemoved
     *         if the message remover is not a chat member
     */
    @Command
    MarkMessageAsDeleted on(RemoveMessage c, CommandContext ctx) throws MessageCannotBeRemoved {
        builder().setId(c.getId());
        ImmutableList<ChatMembers> projections = readProjections(c.getChat(), ctx);
        if (!projections.isEmpty() && contains(projections, c.getUser())) {
            return MarkMessageAsDeleted
                    .newBuilder()
                    .setId(messageId(c))
                    .setChat(c.getChat())
                    .setUser(c.getUser())
                    .setProcess(removalProcess(c))
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
                .setId(removalId(e.getId()))
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
                .setId(removalId(e.getId()))
                .setChat(e.getChat())
                .setUser(e.getUser())
                .vBuild();
    }

    void inject(ProjectionReader<ChatId, ChatMembers> reader) {
        projectionReader = reader;
    }

    private ImmutableList<ChatMembers> readProjections(ChatId id, CommandContext ctx) {
        return projectionReader
                .read(ImmutableSet.of(id), ctx.getActorContext());
    }

    private static boolean contains(ImmutableList<ChatMembers> projections, UserId user) {
        return projections.get(0)
                          .getMemberList()
                          .contains(user);
    }

    private static MessageRemovalOperationId removalProcess(RemoveMessage c) {
        return MessageRemovalOperationId
                .newBuilder()
                .setMessageRemoval(c.getId())
                .vBuild();
    }
}
