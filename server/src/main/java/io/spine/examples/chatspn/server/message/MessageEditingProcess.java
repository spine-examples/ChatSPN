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

import com.google.common.collect.ImmutableSet;
import io.spine.core.CommandContext;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.MessageId;
import io.spine.examples.chatspn.chat.ChatMembers;
import io.spine.examples.chatspn.message.MessageEditing;
import io.spine.examples.chatspn.message.command.EditMessage;
import io.spine.examples.chatspn.message.command.EditMessageContent;
import io.spine.examples.chatspn.message.event.MessageContentEdited;
import io.spine.examples.chatspn.message.event.MessageEdited;
import io.spine.examples.chatspn.message.event.MessageNotEdited;
import io.spine.examples.chatspn.message.rejection.MessageCannotBeEdited;
import io.spine.examples.chatspn.message.rejection.Rejections.MessageContentCannotBeEdited;
import io.spine.examples.chatspn.server.ProjectionReader;
import io.spine.server.command.Command;
import io.spine.server.event.React;
import io.spine.server.procman.ProcessManager;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

/**
 * Coordinates the message editing.
 */
public final class MessageEditingProcess
        extends ProcessManager<MessageId, MessageEditing, MessageEditing.Builder> {

    /**
     * Reads chat members per chat.
     */
    @MonotonicNonNull
    private ProjectionReader<ChatId, ChatMembers> projectionReader;

    /**
     * Issues a command to edit message content.
     *
     * @throws MessageCannotBeEdited
     *         if the message editor is not a chat member
     */
    @Command
    EditMessageContent on(EditMessage c, CommandContext ctx) throws MessageCannotBeEdited {
        builder().setId(c.getId());
        ChatMembers chatMembers = projectionReader
                .read(ImmutableSet.of(c.getChat()), ctx.getActorContext())
                .get(0);
        if (chatMembers.getMemberList()
                       .contains(c.getUser())) {
            return EditMessageContent
                    .newBuilder()
                    .setId(c.getId())
                    .setChat(c.getChat())
                    .setUser(c.getUser())
                    .setContent(c.getContent())
                    .vBuild();
        }
        throw MessageCannotBeEdited
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setContent(c.getContent())
                .build();
    }

    /**
     * Archives the process when the message was edited.
     */
    @React
    MessageEdited on(MessageContentEdited e) {
        setArchived(true);
        return MessageEdited
                .newBuilder()
                .setId(e.getId())
                .setChat(e.getChat())
                .setUser(e.getUser())
                .setContent(e.getContent())
                .vBuild();
    }

    /**
     * Archives the process when the message has not been edited.
     */
    @React
    MessageNotEdited on(MessageContentCannotBeEdited e) {
        setArchived(true);
        return MessageNotEdited
                .newBuilder()
                .setId(e.getId())
                .setChat(e.getChat())
                .setUser(e.getUser())
                .setContent(e.getContent())
                .vBuild();
    }

    void inject(ProjectionReader<ChatId, ChatMembers> reader) {
        projectionReader = reader;
    }
}
