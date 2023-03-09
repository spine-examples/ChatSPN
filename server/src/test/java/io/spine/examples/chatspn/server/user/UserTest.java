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
import io.spine.examples.chatspn.user.command.RegisterUser;
import io.spine.examples.chatspn.user.event.UserRegistered;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.testing.TestValues.randomString;

@DisplayName("`User` should")
class UserTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return ChatsContext.newBuilder();
    }

    @Nested
    @DisplayName("allow registration")
    class Registration {

        @Test
        @DisplayName("emitting the `UserRegistered` event")
        void event() {
            RegisterUser command = sendCommand();

            UserRegistered expected = UserRegistered
                    .newBuilder()
                    .setUser(command.getUser())
                    .setName(command.getName())
                    .build();

            context().assertEvents()
                     .withType(UserRegistered.class)
                     .hasSize(1);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("as entity with the `User` state")
        void entity() {
            RegisterUser command = sendCommand();

            User expected = User
                    .newBuilder()
                    .setId(command.getUser())
                    .setName(command.getName())
                    .vBuild();

            context().assertState(command.getUser(), expected);
        }

        private RegisterUser sendCommand() {
            RegisterUser command = RegisterUser
                    .newBuilder()
                    .setUser(GivenUserId.generated())
                    .setName(randomString())
                    .vBuild();
            context().receivesCommand(command);

            return command;
        }
    }
}
