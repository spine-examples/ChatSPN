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
import io.spine.examples.chatspn.chat.command.DeleteChat;
import io.spine.examples.chatspn.message.Message;
import io.spine.examples.chatspn.message.command.RemoveMessage;
import io.spine.examples.chatspn.message.event.MessageMarkedAsDeleted;
import io.spine.examples.chatspn.message.event.MessageRemovalFailed;
import io.spine.examples.chatspn.message.event.MessageRemoved;
import io.spine.examples.chatspn.message.rejection.RemovalRejections.MessageCannotBeMarkedAsDeleted;
import io.spine.examples.chatspn.message.rejection.RemovalRejections.MessageCannotBeRemoved;
import io.spine.examples.chatspn.server.ChatsContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.chatspn.server.chat.given.ChatDeletionTestEnv.deleteChatCommand;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.createGroupChatIn;
import static io.spine.examples.chatspn.server.message.given.MessageRemovalTestEnv.messageCannotBeMarkedAsRemovedFrom;
import static io.spine.examples.chatspn.server.message.given.MessageRemovalTestEnv.messageCannotBeRemovedFrom;
import static io.spine.examples.chatspn.server.message.given.MessageRemovalTestEnv.messageFrom;
import static io.spine.examples.chatspn.server.message.given.MessageRemovalTestEnv.messageMarkedAsDeletedFrom;
import static io.spine.examples.chatspn.server.message.given.MessageRemovalTestEnv.messageRemovalFailedFrom;
import static io.spine.examples.chatspn.server.message.given.MessageRemovalTestEnv.messageRemovedFrom;
import static io.spine.examples.chatspn.server.message.given.MessageRemovalTestEnv.removeMessageCommand;
import static io.spine.examples.chatspn.server.message.given.MessageRemovalTestEnv.removeMessageCommandWith;
import static io.spine.examples.chatspn.server.message.given.MessageTestEnv.createRandomChatIn;
import static io.spine.examples.chatspn.server.message.given.MessageTestEnv.sendRandomMessageTo;

@DisplayName("`MessageRemoval` should")
final class MessageRemovalTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return ChatsContext.newBuilder();
    }

    @Test
    @DisplayName("emit a `MessageRemoved` event " +
            "if the process is finished successfully, and archive itself")
    void messageRemovedEvent() {
        Chat chat = createRandomChatIn(context());
        Message message = sendRandomMessageTo(chat, context());
        RemoveMessage command = removeMessageCommand(message);
        context().receivesCommand(command);
        MessageRemoved expected = messageRemovedFrom(command);

        context().assertEvent(expected);
        context().assertEntity(expected.getId(), MessageRemovalProcess.class)
                 .archivedFlag()
                 .isTrue();
    }

    @Test
    @DisplayName("reject with the `MessageCannotBeRemoved` " +
            "if the message remover is not the chat member")
    void messageCannotBeRemovedRejection() {
        Chat chat = createRandomChatIn(context());
        Message message = sendRandomMessageTo(chat, context());
        RemoveMessage command = removeMessageCommandWith(message, GivenUserId.generated());
        context().receivesCommand(command);
        MessageCannotBeRemoved expected = messageCannotBeRemovedFrom(command);

        context().assertEvent(expected);
    }

    @Test
    @DisplayName("reject with the `MessageCannotBeRemoved` " +
            "if the chat does not exist or has been deleted")
    void chatNotExist() {
        Chat chat = createGroupChatIn(context());
        Message message = sendRandomMessageTo(chat, context());
        DeleteChat deleteChat = deleteChatCommand(chat, chat.getOwner());
        context().receivesCommand(deleteChat);
        RemoveMessage command = removeMessageCommandWith(message, GivenUserId.generated());
        context().receivesCommand(command);
        MessageCannotBeRemoved expected = messageCannotBeRemovedFrom(command);

        context().assertEvent(expected);
    }

    @Test
    @DisplayName("emit a `MessageRemovalFailed` event " +
            "if message cannot be marked as deleted, and archive itself")
    void messageRemovalFailedEvent() {
        Chat chat = createRandomChatIn(context());
        Message message = sendRandomMessageTo(chat, context());
        RemoveMessage command = removeMessageCommandWith(message, MessageId.generate());
        context().receivesCommand(command);
        MessageRemovalFailed expected = messageRemovalFailedFrom(command);

        context().assertEvent(expected);
        context().assertEntity(expected.getId(), MessageRemovalProcess.class)
                 .archivedFlag()
                 .isTrue();
    }

    @Test
    @DisplayName("update a `MessageViewProjection` to the expected state")
    void updateMessageView() {
        Chat chat = createRandomChatIn(context());
        Message message = sendRandomMessageTo(chat, context());
        RemoveMessage command = removeMessageCommand(message);
        context().receivesCommand(command);

        context().assertEntity(command.message(), MessageViewProjection.class)
                 .deletedFlag()
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
            RemoveMessage command = removeMessageCommand(message);
            context().receivesCommand(command);
            Message expected = messageFrom(command);

            context().assertState(expected.getId(), Message.class)
                     .comparingExpectedFieldsOnly()
                     .isEqualTo(expected);
            context().assertEntity(expected.getId(), MessageAggregate.class)
                     .deletedFlag()
                     .isTrue();
        }

        @Test
        @DisplayName("emission of the `MessageMarkedAsDeleted` event")
        void event() {
            Chat chat = createRandomChatIn(context());
            Message message = sendRandomMessageTo(chat, context());
            RemoveMessage command = removeMessageCommand(message);
            context().receivesCommand(command);
            MessageMarkedAsDeleted expected = messageMarkedAsDeletedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("emission of the `MessageCannotBeMarkedAsDeleted` rejection " +
                "if message with the given ID doesn't exist")
        void rejectBecauseNotExist() {
            Chat chat = createRandomChatIn(context());
            Message message = sendRandomMessageTo(chat, context());
            RemoveMessage command = removeMessageCommandWith(message, MessageId.generate());
            context().receivesCommand(command);
            MessageCannotBeMarkedAsDeleted expected =
                    messageCannotBeMarkedAsRemovedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("emission of the `MessageCannotBeMarkedAsDeleted` rejection " +
                "if the message is already marked as deleted")
        void rejectBecauseAlreadyRemoved() {
            Chat chat = createRandomChatIn(context());
            Message message = sendRandomMessageTo(chat, context());
            RemoveMessage command = removeMessageCommand(message);
            context().receivesCommand(command);
            context().receivesCommand(command);
            MessageCannotBeMarkedAsDeleted expected =
                    messageCannotBeMarkedAsRemovedFrom(command);

            context().assertEvent(expected);
        }
    }
}
