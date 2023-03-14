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
import io.spine.examples.chatspn.user.UserBlocklist;
import io.spine.examples.chatspn.user.command.BlockUser;
import io.spine.examples.chatspn.user.command.UnblockUser;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.chatspn.server.given.UserTestEnv.registerRandomUser;

@DisplayName("`UserBlocklistProjection` should")
class UserBlocklistProjectionTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return ChatsContext.newBuilder();
    }

    @Test
    @DisplayName("display `UserBlocklist`, as soon as `User` registered")
    void reactOnRegistration() {
        User user = registerRandomUser(context());

        UserBlocklist expected = UserBlocklist
                .newBuilder()
                .setId(user.getId())
                .vBuild();

        context().assertState(user.getId(), expected);
    }

    @Test
    @DisplayName("update `UserBlocklist`, as soon as the user blocked")
    void reactOnBlocking() {
        User blockingUser = registerRandomUser(context());
        User userToBlock = registerRandomUser(context());

        BlockUser blockingCommand = BlockUser
                .newBuilder()
                .setUserWhoBlock(blockingUser.getId())
                .setUserToBlock(userToBlock.getId())
                .vBuild();

        context().receivesCommand(blockingCommand);

        UserBlocklist expected = UserBlocklist
                .newBuilder()
                .setId(blockingUser.getId())
                .addBlockedUser(userToBlock.getId())
                .vBuild();

        context().assertState(blockingUser.getId(), expected);
    }

    @Test
    @DisplayName("update `UserBlocklist`, as soon as the user unblocked")
    void reactOnUnblocking() {
        User unblockingUser = registerRandomUser(context());
        User userToUnblock = registerRandomUser(context());

        BlockUser blockingCommand = BlockUser
                .newBuilder()
                .setUserWhoBlock(unblockingUser.getId())
                .setUserToBlock(userToUnblock.getId())
                .vBuild();

        context().receivesCommand(blockingCommand);

        UnblockUser unblockingCommand = UnblockUser
                .newBuilder()
                .setUserWhoUnblock(unblockingUser.getId())
                .setUserToUnblock(userToUnblock.getId())
                .vBuild();

        context().receivesCommand(unblockingCommand);

        UserBlocklist expected = UserBlocklist
                .newBuilder()
                .setId(unblockingUser.getId())
                .vBuild();

        context().assertState(unblockingUser.getId(), expected);
    }
}
