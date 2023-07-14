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

package io.spine.examples.chatspn.server.given;

import io.spine.examples.chatspn.AccountCreationId;
import io.spine.examples.chatspn.account.UserProfile;
import io.spine.examples.chatspn.account.command.CreateAccount;
import io.spine.examples.chatspn.account.event.AccountCreated;
import io.spine.testing.core.given.GivenUserId;

import static io.spine.examples.chatspn.server.given.GivenEmailAddress.randomEmailAddress;

public final class ChatSpnServerTestEnv {

    /**
     * Prevents class instantiation.
     */
    private ChatSpnServerTestEnv() {
    }

    public static CreateAccount createAccount() {
        var command = CreateAccount
                .newBuilder()
                .setId(AccountCreationId.generate())
                .setUser(GivenUserId.generated())
                .setEmail(randomEmailAddress())
                .setName("John Doe")
                .vBuild();
        return command;
    }

    public static UserProfile userProfile(CreateAccount c) {
        var state = UserProfile
                .newBuilder()
                .setId(c.getUser())
                .setEmail(c.getEmail())
                .setName(c.getName())
                .vBuild();
        return state;
    }

    public static AccountCreated accountCreated(CreateAccount c) {
        var event = AccountCreated
                .newBuilder()
                .setId(c.getId())
                .setUser(c.getUser())
                .setEmail(c.getEmail())
                .setName(c.getName())
                .vBuild();
        return event;
    }
}
