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

import io.spine.examples.chatspn.chat.Chat;
import io.spine.examples.chatspn.chat.command.DeleteChat;
import io.spine.examples.chatspn.chat.event.ChatDeleted;
import io.spine.examples.chatspn.chat.event.ChatDeletionFailed;
import io.spine.examples.chatspn.chat.event.ChatMarkedAsDeleted;
import io.spine.examples.chatspn.chat.rejection.DeletionRejections.ChatCannotBeMarkedAsDeleted;
import io.spine.examples.chatspn.message.Message;
import io.spine.examples.chatspn.server.ChatsContext;
import io.spine.examples.chatspn.server.message.MessageAggregate;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.chatspn.server.chat.given.ChatDeletionTestEnv.chatCannotBeMarkedAsDeletedFrom;
import static io.spine.examples.chatspn.server.chat.given.ChatDeletionTestEnv.chatDeletedFrom;
import static io.spine.examples.chatspn.server.chat.given.ChatDeletionTestEnv.chatDeletionFailedFrom;
import static io.spine.examples.chatspn.server.chat.given.ChatDeletionTestEnv.chatMarkedAsDeletedFrom;
import static io.spine.examples.chatspn.server.chat.given.ChatDeletionTestEnv.deleteChatCommand;
import static io.spine.examples.chatspn.server.chat.given.ChatDeletionTestEnv.sendMessagesTo;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.createGroupChatIn;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.createPersonalChatIn;

@DisplayName("`ChatDeletion` should")
final class ChatDeletionTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return ChatsContext.newBuilder();
    }

    @Test
    @DisplayName("emit the `ChatDeleted` and archive itself if the chat can be deleted")
    void event() {
        Chat chat = createGroupChatIn(context());
        DeleteChat command = deleteChatCommand(chat, chat.getOwner());
        context().receivesCommand(command);
        ChatDeleted expected = chatDeletedFrom(command, chat);

        context().assertEvent(expected);
        context().assertEntity(command.getId(), ChatDeletionProcess.class)
                 .archivedFlag()
                 .isTrue();
    }

    @Test
    @DisplayName("mark messages in the chat as deleted")
    void removeMessages() {
        Chat chat = createGroupChatIn(context());
        List<Message> messages = sendMessagesTo(chat, context());
        DeleteChat command = deleteChatCommand(chat, chat.getOwner());
        context().receivesCommand(command);

        messages.forEach(message ->
                context().assertEntity(message.getId(), MessageAggregate.class)
                         .deletedFlag()
                         .isTrue());
    }

    @Test
    @DisplayName("emit the `ChatDeletionFailed` and archive itself " +
            "if the `ChatAggregate` reject with the `ChatCannotBeMarkedAsDeleted`")
    void fail() {
        Chat chat = createPersonalChatIn(context());
        DeleteChat command = deleteChatCommand(chat, GivenUserId.generated());
        context().receivesCommand(command);
        ChatDeletionFailed expected = chatDeletionFailedFrom(command);

        context().assertEvent(expected);
        context().assertEntity(command.getId(), ChatDeletionProcess.class)
                 .archivedFlag()
                 .isTrue();
    }

    @Nested
    @DisplayName("lead `ChatAggregate` to")
    class ChatAggregateBehaviour {

        @Test
        @DisplayName("update the state as expected")
        void state() {
            Chat chat = createPersonalChatIn(context());
            DeleteChat command = deleteChatCommand(chat, chat.getMember(1));
            context().receivesCommand(command);

            context().assertEntity(command.chat(), ChatAggregate.class)
                     .deletedFlag()
                     .isTrue();
        }

        @Test
        @DisplayName("emission of the `ChatMarkedAsDeleted` event")
        void event() {
            Chat chat = createGroupChatIn(context());
            DeleteChat command = deleteChatCommand(chat, chat.getOwner());
            context().receivesCommand(command);
            ChatMarkedAsDeleted expected = chatMarkedAsDeletedFrom(command, chat);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("the `ChatCannotBeMarkedAsDeleted` rejection " +
                "if chat is personal and the user who deletes is not a member")
        void rejectIfNotMemberInPersonal() {
            Chat chat = createPersonalChatIn(context());
            DeleteChat command = deleteChatCommand(chat, GivenUserId.generated());
            context().receivesCommand(command);
            ChatCannotBeMarkedAsDeleted expected =
                    chatCannotBeMarkedAsDeletedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("the `ChatCannotBeMarkedAsDeleted` rejection " +
                "if chat is group and the user who deletes is not an owner")
        void rejectIfNotOwnerInGroup() {
            Chat chat = createGroupChatIn(context());
            DeleteChat command = deleteChatCommand(chat, chat.getMember(1));
            context().receivesCommand(command);
            ChatCannotBeMarkedAsDeleted expected =
                    chatCannotBeMarkedAsDeletedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("the `ChatCannotBeMarkedAsDeleted` rejection " +
                "if chat is already deleted")
        void rejectIfAlreadyDeleted() {
            Chat chat = createGroupChatIn(context());
            DeleteChat command = deleteChatCommand(chat, chat.getOwner());
            context().receivesCommand(command);
            context().receivesCommand(command);
            ChatCannotBeMarkedAsDeleted expected =
                    chatCannotBeMarkedAsDeletedFrom(command);

            context().assertEvent(expected);
        }
    }
}
