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
import io.spine.examples.chatspn.account.ReservedEmail;
import io.spine.examples.chatspn.account.User;
import io.spine.examples.chatspn.account.UserProfile;
import io.spine.examples.chatspn.account.command.CreateAccount;
import io.spine.examples.chatspn.account.event.AccountCreated;
import io.spine.examples.chatspn.account.event.AccountNotCreated;
import io.spine.examples.chatspn.account.event.EmailReserved;
import io.spine.examples.chatspn.account.event.UserRegistered;
import io.spine.examples.chatspn.account.rejection.ReservedEmailRejections.EmailAlreadyReserved;
import io.spine.net.EmailAddress;
import io.spine.testing.core.given.GivenUserId;

import static io.spine.examples.chatspn.server.given.GivenEmailAddress.randomEmailAddress;
import static io.spine.testing.TestValues.randomString;

public final class AccountCreationTestEnv {

    /**
     * Prevents instantiation of this class.
     */
    private AccountCreationTestEnv() {
    }

    public static CreateAccount randomCreateAccountCommand() {
        CreateAccount command = CreateAccount
                .newBuilder()
                .setId(AccountCreationId.generate())
                .setUser(GivenUserId.generated())
                .setEmail(randomEmailAddress())
                .setName(randomString())
                .vBuild();
        return command;
    }

    public static CreateAccount createAccountCommandWith(EmailAddress email) {
        CreateAccount command = CreateAccount
                .newBuilder()
                .setId(AccountCreationId.generate())
                .setUser(GivenUserId.generated())
                .setEmail(email)
                .setName(randomString())
                .vBuild();
        return command;
    }

    public static AccountCreated accountCreatedFrom(CreateAccount c) {
        AccountCreated event = AccountCreated
                .newBuilder()
                .setId(c.getId())
                .setUser(c.getUser())
                .setEmail(c.getEmail())
                .setName(c.getName())
                .vBuild();
        return event;
    }

    public static AccountNotCreated accountNotCreatedFrom(CreateAccount c) {
        AccountNotCreated event = AccountNotCreated
                .newBuilder()
                .setId(c.getId())
                .setUser(c.getUser())
                .setEmail(c.getEmail())
                .setName(c.getName())
                .vBuild();
        return event;
    }

    public static EmailReserved emailReservedFrom(CreateAccount c) {
        EmailReserved event = EmailReserved
                .newBuilder()
                .setEmail(c.getEmail())
                .setUser(c.getUser())
                .setProcess(c.getId())
                .vBuild();
        return event;
    }

    public static EmailAlreadyReserved emailAlreadyReservedFrom(CreateAccount c) {
        EmailAlreadyReserved event = EmailAlreadyReserved
                .newBuilder()
                .setEmail(c.getEmail())
                .setUser(c.getUser())
                .setProcess(c.getId())
                .vBuild();
        return event;
    }

    public static ReservedEmail reservedEmailFrom(CreateAccount c) {
        ReservedEmail state = ReservedEmail
                .newBuilder()
                .setEmail(c.getEmail())
                .setUser(c.getUser())
                .vBuild();
        return state;
    }

    public static UserRegistered userRegisteredFrom(CreateAccount c) {
        UserRegistered state = UserRegistered
                .newBuilder()
                .setUser(c.getUser())
                .setEmail(c.getEmail())
                .setName(c.getName())
                .setProcess(c.getId())
                .vBuild();
        return state;
    }

    public static User userFrom(CreateAccount c) {
        User state = User
                .newBuilder()
                .setId(c.getUser())
                .setEmail(c.getEmail())
                .setName(c.getName())
                .vBuild();
        return state;
    }

    public static UserProfile userProfileFrom(CreateAccount c) {
        UserProfile state = UserProfile
                .newBuilder()
                .setId(c.getUser())
                .setEmail(c.getEmail())
                .setName(c.getName())
                .vBuild();
        return state;
    }
}
