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

import io.spine.examples.chatspn.MessageId;
import io.spine.examples.chatspn.message.Message;
import io.spine.examples.chatspn.message.command.MarkMessageAsDeleted;
import io.spine.examples.chatspn.message.command.PostMessage;
import io.spine.examples.chatspn.message.command.UpdateMessageContent;
import io.spine.examples.chatspn.message.event.MessageContentUpdated;
import io.spine.examples.chatspn.message.event.MessageMarkedAsDeleted;
import io.spine.examples.chatspn.message.event.MessagePosted;
import io.spine.examples.chatspn.message.rejection.MessageCannotBeMarkedAsDeleted;
import io.spine.examples.chatspn.message.rejection.MessageContentCannotBeUpdated;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

import static io.spine.base.Time.currentTime;

/**
 * A single message in the chat.
 */
public final class MessageAggregate extends Aggregate<MessageId, Message, Message.Builder> {

    /**
     * Handles the command to post a message.
     */
    @Assign
    MessagePosted handle(PostMessage c) {
        return MessagePosted
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setContent(c.getContent())
                .setWhenPosted(currentTime())
                .vBuild();
    }

    @Apply
    private void event(MessagePosted e) {
        builder().setId(e.getId())
                 .setChat(e.getChat())
                 .setUser(e.getUser())
                 .setContent(e.getContent())
                 .setWhenPosted(e.getWhenPosted());
    }

    /**
     * Handles the command to update a message content.
     *
     * @throws MessageContentCannotBeUpdated
     *         if the message does not exist, or editor is not an original sender of this message
     */
    @Assign
    MessageContentUpdated handle(UpdateMessageContent c) throws MessageContentCannotBeUpdated {
        if (!state().hasId() || !state().getUser()
                                        .equals(c.getUser())) {
            throw MessageContentCannotBeUpdated
                    .newBuilder()
                    .setId(c.getId())
                    .setChat(c.getChat())
                    .setUser(c.getUser())
                    .setSuggestedContent(c.getSuggestedContent())
                    .build();
        }
        return MessageContentUpdated
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setContent(c.getSuggestedContent())
                .vBuild();
    }

    @Apply
    private void event(MessageContentUpdated e) {
        builder().setContent(e.getContent());
    }

    /**
     * Handles the command to mark a message as deleted.
     *
     * @throws MessageCannotBeMarkedAsDeleted
     *         if the message does not exist, or is already marked as deleted
     */
    @Assign
    MessageMarkedAsDeleted handle(MarkMessageAsDeleted c) throws MessageCannotBeMarkedAsDeleted {
        if (!state().hasId() || isDeleted()) {
            throw MessageCannotBeMarkedAsDeleted
                    .newBuilder()
                    .setId(c.getId())
                    .setChat(c.getChat())
                    .setUser(c.getUser())
                    .setOperation(c.getOperation())
                    .build();
        }
        return MessageMarkedAsDeleted
                .newBuilder()
                .setId(c.getId())
                .setChat(c.getChat())
                .setUser(c.getUser())
                .setOperation(c.getOperation())
                .vBuild();
    }

    @Apply
    private void event(MessageMarkedAsDeleted e) {
        setDeleted(true);
    }
}
