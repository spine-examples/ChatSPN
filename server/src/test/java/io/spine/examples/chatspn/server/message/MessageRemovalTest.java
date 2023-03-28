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
import io.spine.examples.chatspn.message.command.RemoveMessage;
import io.spine.examples.chatspn.message.event.MessageMarkedAsRemoved;
import io.spine.examples.chatspn.message.event.MessageRemovalFailed;
import io.spine.examples.chatspn.message.event.MessageRemoved;
import io.spine.examples.chatspn.message.rejection.RemovalRejections.MessageCannotBeMarkedAsRemoved;
import io.spine.examples.chatspn.message.rejection.RemovalRejections.MessageCannotBeRemoved;
import io.spine.examples.chatspn.server.ChatsContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.chatspn.server.given.MessageEditingTestEnv.createRandomChatIn;
import static io.spine.examples.chatspn.server.given.MessageEditingTestEnv.sendRandomMessageTo;
import static io.spine.examples.chatspn.server.given.MessageRemovalTestEnv.messageCannotBeMarkedAsRemovedFrom;
import static io.spine.examples.chatspn.server.given.MessageRemovalTestEnv.messageCannotBeRemovedFrom;
import static io.spine.examples.chatspn.server.given.MessageRemovalTestEnv.messageFrom;
import static io.spine.examples.chatspn.server.given.MessageRemovalTestEnv.messageMarkedAsRemovedFrom;
import static io.spine.examples.chatspn.server.given.MessageRemovalTestEnv.messageRemovalFailedFrom;
import static io.spine.examples.chatspn.server.given.MessageRemovalTestEnv.messageRemovedFrom;
import static io.spine.examples.chatspn.server.given.MessageRemovalTestEnv.removeMessageCommand;
import static io.spine.examples.chatspn.server.given.MessageRemovalTestEnv.removeMessageCommandWith;

@DisplayName("`MessageRemoval` should")
final class MessageRemovalTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return ChatsContext.newBuilder();
    }

    @Test
    @DisplayName("emit a `MessageRemoved` event " +
            "if the process is finished successfully and archive itself")
    void messageEditedEvent() {
        Chat chat = createRandomChatIn(context());
        Message message = sendRandomMessageTo(chat, context());
        RemoveMessage command = removeMessageCommand(message);
        context().receivesCommand(command);
        MessageRemoved expected = messageRemovedFrom(command);

        context().assertEvents()
                 .withType(MessageRemoved.class)
                 .message(0)
                 .isEqualTo(expected);
        context().assertEntity(expected.getId(), MessageRemovalProcess.class)
                 .archivedFlag()
                 .isTrue();
    }

    @Test
    @DisplayName("update a `Message` to the expected state")
    void state() {
        Chat chat = createRandomChatIn(context());
        Message message = sendRandomMessageTo(chat, context());
        RemoveMessage command = removeMessageCommand(message);
        context().receivesCommand(command);
        Message expected = messageFrom(command);

        context().assertState(expected.getId(), Message.class)
                 .comparingExpectedFieldsOnly()
                 .isEqualTo(expected);
    }

    @Test
    @DisplayName("reject with the `MessageCannotBeRemoved` " +
            "if the message remover is not the chat member")
    void messageCannotBeEditedRejection() {
        Chat chat = createRandomChatIn(context());
        Message message = sendRandomMessageTo(chat, context());
        RemoveMessage command = removeMessageCommandWith(message, GivenUserId.generated());
        context().receivesCommand(command);
        MessageCannotBeRemoved expected = messageCannotBeRemovedFrom(command);

        context().assertEvents()
                 .withType(MessageCannotBeRemoved.class)
                 .message(0)
                 .isEqualTo(expected);
    }

    @Test
    @DisplayName("emit a `MessageRemovalFailed` event " +
            "if message cannot be marked as removed and archive itself")
    void messageNotEditedEvent() {
        Chat chat = createRandomChatIn(context());
        Message message = sendRandomMessageTo(chat, context());
        RemoveMessage command = removeMessageCommandWith(message, MessageId.generate());
        context().receivesCommand(command);
        MessageRemovalFailed expected = messageRemovalFailedFrom(command);

        context().assertEvents()
                 .withType(MessageRemovalFailed.class)
                 .message(0)
                 .isEqualTo(expected);
        context().assertEntity(expected.getId(), MessageRemovalProcess.class)
                 .archivedFlag()
                 .isTrue();
    }

    @Nested
    @DisplayName("lead `MessageAggregate` to emission of the")
    class MessageAggregate {

        @Test
        @DisplayName("`MessageMarkedAsRemoved`")
        void event() {
            Chat chat = createRandomChatIn(context());
            Message message = sendRandomMessageTo(chat, context());
            RemoveMessage command = removeMessageCommand(message);
            context().receivesCommand(command);
            MessageMarkedAsRemoved expected = messageMarkedAsRemovedFrom(command);

            context().assertEvents()
                     .withType(MessageMarkedAsRemoved.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("`MessageCannotBeMarkedAsRemoved` rejection " +
                "if message with the given ID doesn't exist")
        void rejectBecauseNotExist() {
            Chat chat = createRandomChatIn(context());
            Message message = sendRandomMessageTo(chat, context());
            RemoveMessage command = removeMessageCommandWith(message, MessageId.generate());
            context().receivesCommand(command);
            MessageCannotBeMarkedAsRemoved expected =
                    messageCannotBeMarkedAsRemovedFrom(command);

            context().assertEvents()
                     .withType(MessageCannotBeMarkedAsRemoved.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("`MessageCannotBeMarkedAsRemoved` rejection " +
                "if the message is already marked as removed")
        void rejectBecauseEditorNonOwner() {
            Chat chat = createRandomChatIn(context());
            Message message = sendRandomMessageTo(chat, context());
            RemoveMessage command = removeMessageCommand(message);
            context().receivesCommand(command);
            context().receivesCommand(command);
            MessageCannotBeMarkedAsRemoved expected =
                    messageCannotBeMarkedAsRemovedFrom(command);

            context().assertEvents()
                     .withType(MessageCannotBeMarkedAsRemoved.class)
                     .message(0)
                     .isEqualTo(expected);
        }
    }
}