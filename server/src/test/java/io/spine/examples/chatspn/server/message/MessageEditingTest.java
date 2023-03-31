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
import io.spine.examples.chatspn.chat.Chat;
import io.spine.examples.chatspn.message.Message;
import io.spine.examples.chatspn.message.command.EditMessage;
import io.spine.examples.chatspn.message.event.MessageContentUpdated;
import io.spine.examples.chatspn.message.event.MessageEdited;
import io.spine.examples.chatspn.message.event.MessageEditingFailed;
import io.spine.examples.chatspn.message.rejection.EditingRejections.MessageCannotBeEdited;
import io.spine.examples.chatspn.message.rejection.EditingRejections.MessageContentCannotBeUpdated;
import io.spine.examples.chatspn.server.ChatsContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.chatspn.server.message.given.MessageEditingTestEnv.editMessageCommand;
import static io.spine.examples.chatspn.server.message.given.MessageEditingTestEnv.editMessageCommandWith;
import static io.spine.examples.chatspn.server.message.given.MessageEditingTestEnv.messageCannotBeEditedFrom;
import static io.spine.examples.chatspn.server.message.given.MessageEditingTestEnv.messageContentCannotBeUpdatedFrom;
import static io.spine.examples.chatspn.server.message.given.MessageEditingTestEnv.messageContentUpdatedFrom;
import static io.spine.examples.chatspn.server.message.given.MessageEditingTestEnv.messageEditedFrom;
import static io.spine.examples.chatspn.server.message.given.MessageEditingTestEnv.messageEditingFailedFrom;
import static io.spine.examples.chatspn.server.message.given.MessageEditingTestEnv.messageFrom;
import static io.spine.examples.chatspn.server.message.given.MessageTestEnv.createRandomChatIn;
import static io.spine.examples.chatspn.server.message.given.MessageTestEnv.sendRandomMessageTo;

@DisplayName("`MessageEditing` should")
final class MessageEditingTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return ChatsContext.newBuilder();
    }

    @Test
    @DisplayName("emit a `MessageEdited` event " +
            "if the process is finished successfully, and archive itself")
    void messageEditedEvent() {
        Chat chat = createRandomChatIn(context());
        Message message = sendRandomMessageTo(chat, context());
        EditMessage command = editMessageCommand(message);
        context().receivesCommand(command);
        MessageEdited expected = messageEditedFrom(command);

        context().assertEvent(expected);
        context().assertEntity(expected.getId(), MessageEditingProcess.class)
                 .archivedFlag()
                 .isTrue();
    }

    @Test
    @DisplayName("reject with the `MessageCannotBeEdited` " +
            "if the message editor is not the chat member")
    void messageCannotBeEditedRejection() {
        Chat chat = createRandomChatIn(context());
        Message message = sendRandomMessageTo(chat, context());
        EditMessage command = editMessageCommandWith(message, GivenUserId.generated());
        context().receivesCommand(command);
        MessageCannotBeEdited expected = messageCannotBeEditedFrom(command);

        context().assertEvent(expected);
    }

    @Test
    @DisplayName("emit a `MessageEditingFailed` event " +
            "if message content cannot be edited, and archive itself")
    void messageNotEditedEvent() {
        Chat chat = createRandomChatIn(context());
        Message message = sendRandomMessageTo(chat, context());
        EditMessage command = editMessageCommandWith(message, MessageId.generate());
        context().receivesCommand(command);
        MessageEditingFailed expected = messageEditingFailedFrom(command);

        context().assertEvent(expected);
        context().assertEntity(expected.getId(), MessageEditingProcess.class)
                 .archivedFlag()
                 .isTrue();
    }

    @Nested
    @DisplayName("lead `MessageAggregate` to ")
    class MessageAggregateBehaviour {

        @Test
        @DisplayName("update the state as expected")
        void state() {
            Chat chat = createRandomChatIn(context());
            Message message = sendRandomMessageTo(chat, context());
            EditMessage command = editMessageCommand(message);
            context().receivesCommand(command);
            Message expected = messageFrom(command);

            context().assertState(expected.getId(), Message.class)
                     .comparingExpectedFieldsOnly()
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("emission of the `MessageContentUpdated` event")
        void event() {
            Chat chat = createRandomChatIn(context());
            Message message = sendRandomMessageTo(chat, context());
            EditMessage command = editMessageCommand(message);
            context().receivesCommand(command);
            MessageContentUpdated expected = messageContentUpdatedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("emission of the `MessageContentCannotBeUpdated` rejection " +
                "if message with the given ID doesn't exist")
        void rejectBecauseNotExist() {
            Chat chat = createRandomChatIn(context());
            Message message = sendRandomMessageTo(chat, context());
            EditMessage command = editMessageCommandWith(message, MessageId.generate());
            context().receivesCommand(command);
            MessageContentCannotBeUpdated expected =
                    messageContentCannotBeUpdatedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("emission of the `MessageContentCannotBeUpdated` rejection " +
                "if non-owner tries to edit message")
        void rejectBecauseEditorNonOwner() {
            Chat chat = createRandomChatIn(context());
            Message message = sendRandomMessageTo(chat, context());
            EditMessage command = editMessageCommandWith(message, chat.getMember(1));
            context().receivesCommand(command);
            MessageContentCannotBeUpdated expected =
                    messageContentCannotBeUpdatedFrom(command);

            context().assertEvent(expected);
        }
    }
}
