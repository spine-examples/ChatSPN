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

import io.spine.core.UserId;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.chat.Chat;
import io.spine.examples.chatspn.chat.command.CreateChat;
import io.spine.examples.chatspn.chat.event.ChatCreated;
import io.spine.examples.chatspn.server.ChatsContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static io.spine.testing.TestValues.randomString;

@DisplayName("`Chat` should")
class ChatTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return ChatsContext.newBuilder();
    }

    @Test
    @DisplayName("allow creation and emit the `ChatCreated` event")
    void creation() {
        ArrayList<UserId> members = new ArrayList<>();
        members.add(GivenUserId.generated());
        members.add(GivenUserId.generated());

        CreateChat command = CreateChat
                .newBuilder()
                .setId(ChatId.generate())
                .setCreator(GivenUserId.generated())
                .addAllMember(members)
                .setName(randomString())
                .vBuild();

        context().receivesCommand(command);

        ChatCreated expectedEvent = ChatCreated
                .newBuilder()
                .setId(command.getId())
                .setCreator(command.getCreator())
                .addAllMember(command.getMemberList())
                .setName(command.getName())
                .build();
        Chat expectedState = Chat
                .newBuilder()
                .setId(command.getId())
                .addMember(command.getCreator())
                .addAllMember(command.getMemberList())
                .setName(command.getName())
                .vBuild();

        context().assertEvents()
                 .withType(ChatCreated.class)
                 .hasSize(1);
        context().assertEvent(expectedEvent);
        context().assertState(command.getId(), expectedState);
    }
}
