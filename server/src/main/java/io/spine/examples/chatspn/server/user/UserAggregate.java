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

import io.spine.core.UserId;
import io.spine.examples.chatspn.user.User;
import io.spine.examples.chatspn.user.command.BlockUser;
import io.spine.examples.chatspn.user.command.RegisterUser;
import io.spine.examples.chatspn.user.event.UserBlocked;
import io.spine.examples.chatspn.user.event.UserRegistered;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

/**
 * A registered user of ChatSPN.
 */
public final class UserAggregate extends Aggregate<UserId, User, User.Builder> {

    /**
     * Handles the command to register a user.
     */
    @Assign
    UserRegistered handle(RegisterUser c) {
        return UserRegistered
                .newBuilder()
                .setUser(c.getUser())
                .setName(c.getName())
                .vBuild();
    }

    @Apply
    private void event(UserRegistered e) {
        builder().setId(e.getUser())
                 .setName(e.getName());
    }

    /**
     * Handles the command to block a user.
     */
    @Assign
    UserBlocked handle(BlockUser c) {
        return UserBlocked
                .newBuilder()
                .setBlockingUser(c.getUserWhoBlock())
                .setBlockedUser(c.getUserToBlock())
                .vBuild();
    }

    @Apply
    private void event(UserBlocked e) {
        builder().addBlockedUsers(e.getBlockedUser());
    }
}
