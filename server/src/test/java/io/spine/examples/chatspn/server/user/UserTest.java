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

package io.spine.examples.chatspn.server.user;

import io.spine.examples.chatspn.server.ChatsContext;
import io.spine.examples.chatspn.user.User;
import io.spine.examples.chatspn.user.command.BlockUser;
import io.spine.examples.chatspn.user.event.UserBlocked;
import io.spine.examples.chatspn.user.event.UserRegistered;
import io.spine.examples.chatspn.user.rejection.Rejections.UserCannotBeBlocked;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.chatspn.server.given.UserTestEnv.registerRandomUser;

@DisplayName("`User` should")
class UserTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return ChatsContext.newBuilder();
    }

    @Test
    @DisplayName("allow registration and emit the `UserRegistered` event")
    void registration() {
        User user = registerRandomUser(context());

        UserRegistered expectedEvent = UserRegistered
                .newBuilder()
                .setUser(user.getId())
                .setName(user.getName())
                .build();
        User expectedState = User
                .newBuilder()
                .setId(user.getId())
                .setName(user.getName())
                .vBuild();

        context().assertEvents()
                 .withType(UserRegistered.class)
                 .hasSize(1);
        context().assertEvent(expectedEvent);
        context().assertState(user.getId(), expectedState);
    }

    @Test
    @DisplayName("allow blocking another user and emit the `UserBlocked` event")
    void blocking() {
        User blockingUser = registerRandomUser(context());
        User userToBlock = registerRandomUser(context());

        BlockUser blockingCommand = BlockUser
                .newBuilder()
                .setUserWhoBlock(blockingUser.getId())
                .setUserToBlock(userToBlock.getId())
                .vBuild();

        context().receivesCommand(blockingCommand);

        UserBlocked expectedEvent = UserBlocked
                .newBuilder()
                .setBlockingUser(blockingUser.getId())
                .setBlockedUser(userToBlock.getId())
                .build();
        User expectedState = User
                .newBuilder()
                .setId(blockingUser.getId())
                .setName(blockingUser.getName())
                .addBlockedUsers(userToBlock.getId())
                .vBuild();

        context().assertEvents()
                 .withType(UserBlocked.class)
                 .hasSize(1);
        context().assertEvent(expectedEvent);
        context().assertState(blockingCommand.getUserWhoBlock(), expectedState);
    }

    @Test
    @DisplayName("reject blocking himself")
    void rejectSelfBlocking() {
        User user = registerRandomUser(context());

        BlockUser command = BlockUser
                .newBuilder()
                .setUserWhoBlock(user.getId())
                .setUserToBlock(user.getId())
                .vBuild();

        context().receivesCommand(command);

        UserCannotBeBlocked expectedRejection = UserCannotBeBlocked
                .newBuilder()
                .setUserWhoBlock(user.getId())
                .setUserToBlock(user.getId())
                .vBuild();

        context().assertEvents()
                 .withType(UserCannotBeBlocked.class)
                 .message(0)
                 .isEqualTo(expectedRejection);
    }

    @Test
    @DisplayName("reject blocking already blocked user")
    void rejectReblocking() {
        User blockingUser = registerRandomUser(context());
        User userToBlock = registerRandomUser(context());

        BlockUser command = BlockUser
                .newBuilder()
                .setUserWhoBlock(blockingUser.getId())
                .setUserToBlock(userToBlock.getId())
                .vBuild();

        context().receivesCommand(command);
        context().receivesCommand(command);

        UserCannotBeBlocked expectedRejection = UserCannotBeBlocked
                .newBuilder()
                .setUserWhoBlock(blockingUser.getId())
                .setUserToBlock(userToBlock.getId())
                .vBuild();

        context().assertEvents()
                 .withType(UserCannotBeBlocked.class)
                 .message(0)
                 .isEqualTo(expectedRejection);
    }
}
