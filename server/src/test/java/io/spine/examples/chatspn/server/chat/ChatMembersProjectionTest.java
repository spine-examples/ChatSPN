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

import io.spine.examples.chatspn.server.ChatsContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.chatspn.server.chat.given.ChatDeletionTestEnv.deleteChatCommand;
import static io.spine.examples.chatspn.server.chat.given.ChatMembersProjectionTestEnv.chatMembers;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.addMembersCommand;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.createGroupChatCommand;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.createGroupChatIn;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.createPersonalChatCommand;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.leaveChat;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.removeMembersCommandWith;

@DisplayName("`ChatMembersProjection` should")
final class ChatMembersProjectionTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return ChatsContext.newBuilder();
    }

    @Test
    @DisplayName("display `ChatMembers`, as soon as `PersonalChatCreated` is emitted")
    void reactOnPersonalChatCreation() {
        var command = createPersonalChatCommand();
        context().receivesCommand(command);
        var expected = chatMembers(command);

        context().assertState(command.getId(), expected);
    }

    @Test
    @DisplayName("display `ChatMembers`, as soon as `GroupChatCreated` is emitted")
    void reactOnGroupChatCreation() {
        var command = createGroupChatCommand();
        context().receivesCommand(command);
        var expected = chatMembers(command);

        context().assertState(command.getId(), expected);
    }

    @Test
    @DisplayName("update `ChatMembers`, as soon as `MembersRemoved` is emitted")
    void reactOnMembersRemoved() {
        var chat = createGroupChatIn(context());
        var command = removeMembersCommandWith(chat, chat.getOwner());
        context().receivesCommand(command);
        var expected = chatMembers(chat, command);

        context().assertState(command.getId(), expected);
    }

    @Test
    @DisplayName("update `ChatMembers`, as soon as `MembersAdded` is emitted")
    void reactOnMembersAdded() {
        var chat = createGroupChatIn(context());
        var command = addMembersCommand(chat);
        context().receivesCommand(command);
        var expected = chatMembers(chat, command);

        context().assertState(command.getId(), expected);
    }

    @Test
    @DisplayName("update `ChatMembersProjection`, as soon as `ChatMarkedAsDeleted` is emitted")
    void reactOnChatDeleted() {
        var chat = createGroupChatIn(context());
        var command = deleteChatCommand(chat, chat.getOwner());
        context().receivesCommand(command);

        context().assertEntity(chat.getId(), ChatMembersProjection.class)
                 .deletedFlag()
                 .isTrue();
    }

    @Test
    @DisplayName("update `ChatMembersProjection`, as soon as `UserLeftChat` is emitted")
    void reactOnUserLeftChat() {
        var chat = createGroupChatIn(context());
        var command = leaveChat(chat, chat.getMember(0));
        context().receivesCommand(command);
        var expected = chatMembers(chat, command);

        context().assertState(chat.getId(), expected);
    }
}
