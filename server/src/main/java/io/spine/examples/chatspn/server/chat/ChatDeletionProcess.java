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

package io.spine.examples.chatspn.server.chat;

import com.google.common.collect.ImmutableSet;
import io.spine.client.Filter;
import io.spine.core.EventContext;
import io.spine.core.UserId;
import io.spine.examples.chatspn.ChatDeletionId;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.MessageId;
import io.spine.examples.chatspn.MessageRemovalOperationId;
import io.spine.examples.chatspn.chat.ChatDeletion;
import io.spine.examples.chatspn.chat.command.DeleteChat;
import io.spine.examples.chatspn.chat.command.MarkChatAsDeleted;
import io.spine.examples.chatspn.chat.event.ChatDeleted;
import io.spine.examples.chatspn.chat.event.ChatDeletionFailed;
import io.spine.examples.chatspn.chat.event.ChatMarkedAsDeleted;
import io.spine.examples.chatspn.chat.rejection.DeletionRejections.ChatCannotBeMarkedAsDeleted;
import io.spine.examples.chatspn.message.MessageView;
import io.spine.examples.chatspn.message.command.MarkMessageAsDeleted;
import io.spine.examples.chatspn.server.ProjectionReader;
import io.spine.server.command.Command;
import io.spine.server.event.React;
import io.spine.server.procman.ProcessManager;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.List;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static io.spine.client.Filters.eq;

public final class ChatDeletionProcess
        extends ProcessManager<ChatDeletionId, ChatDeletion, ChatDeletion.Builder> {

    /**
     * Reads messages per chat.
     */
    @MonotonicNonNull
    private ProjectionReader<MessageId, MessageView> projectionReader;

    @Command
    MarkChatAsDeleted handle(DeleteChat c) {
        builder().setId(c.getId());
        return MarkChatAsDeleted
                .newBuilder()
                .setId(chatId(c))
                .setWhoMarks(c.getWhoDeletes())
                .vBuild();
    }

    @Command
    Iterable<MarkMessageAsDeleted> on(ChatDeleted e, EventContext ctx) {
        Filter byChatId = eq(MessageView.Field.chat(), chatId(e));
        List<MessageView> messages = projectionReader.read(ctx.actorContext(), byChatId);
        ImmutableSet<MarkMessageAsDeleted> commands =
                messages.stream()
                        .map(message -> markMessageAsDeleted(message, e.getWhoDeleted()))
                        .collect(toImmutableSet());
        setArchived(true);
        return commands;
    }

    @React
    ChatDeleted on(ChatMarkedAsDeleted e) {
        return ChatDeleted
                .newBuilder()
                .setId(deletionId(e.getId()))
                .setWhoDeleted(e.getWhoDeleted())
                .addAllMember(e.getMemberList())
                .vBuild();
    }

    @React
    ChatDeletionFailed on(ChatCannotBeMarkedAsDeleted e) {
        return ChatDeletionFailed
                .newBuilder()
                .setId(deletionId(e.getId()))
                .setWhoDeleted(e.getWhoDeletes())
                .vBuild();
    }

    private static ChatId chatId(DeleteChat c) {
        return c.getId()
                .getId();
    }

    private static ChatId chatId(ChatDeleted c) {
        return c.getId()
                .getId();
    }

    private static ChatDeletionId deletionId(ChatId id) {
        return ChatDeletionId
                .newBuilder()
                .setId(id)
                .vBuild();
    }

    private static MarkMessageAsDeleted
    markMessageAsDeleted(MessageView message, UserId user) {
        return MarkMessageAsDeleted
                .newBuilder()
                .setId(message.getId())
                .setChat(message.getChat())
                .setUser(user)
                .setProcess(removalOperationId(message.getChat()))
                .vBuild();
    }

    private static MessageRemovalOperationId removalOperationId(ChatId id) {
        return MessageRemovalOperationId
                .newBuilder()
                .setChatDeletion(deletionId(id))
                .vBuild();
    }

    void inject(ProjectionReader<MessageId, MessageView> reader) {
        projectionReader = reader;
    }
}