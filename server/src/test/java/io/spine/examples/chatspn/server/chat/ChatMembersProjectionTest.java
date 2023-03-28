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

import io.spine.examples.chatspn.chat.ChatMembers;
import io.spine.examples.chatspn.chat.command.CreateGroupChat;
import io.spine.examples.chatspn.chat.command.CreatePersonalChat;
import io.spine.examples.chatspn.server.ChatsContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.chatspn.server.given.ChatMembersProjectionTestEnv.chatMembersFrom;
import static io.spine.examples.chatspn.server.given.ChatTestEnv.createGroupChatCommand;
import static io.spine.examples.chatspn.server.given.ChatTestEnv.createPersonalChatCommand;

@DisplayName("`ChatMembersProjection` should")
class ChatMembersProjectionTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return ChatsContext.newBuilder();
    }

    @Test
    @DisplayName("display `ChatMembers`, as soon as `PersonalChatCreated` is emitted")
    void reactOnPersonalChatCreation() {
        CreatePersonalChat command = createPersonalChatCommand();
        context().receivesCommand(command);
        ChatMembers expected = chatMembersFrom(command);

        context().assertState(command.getId(), expected);
    }

    @Test
    @DisplayName("display `ChatMembers`, as soon as `GroupChatCreated` is emitted")
    void reactOnGroupChatCreation() {
        CreateGroupChat command = createGroupChatCommand();
        context().receivesCommand(command);
        ChatMembers expected = chatMembersFrom(command);

        context().assertState(command.getId(), expected);
    }
}
