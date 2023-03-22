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

package io.spine.examples.chatspn.server.account;

import io.spine.examples.chatspn.account.ReservedEmail;
import io.spine.examples.chatspn.account.User;
import io.spine.examples.chatspn.account.UserProfile;
import io.spine.examples.chatspn.account.command.CreateAccount;
import io.spine.examples.chatspn.account.command.ReserveEmail;
import io.spine.examples.chatspn.account.event.AccountCreated;
import io.spine.examples.chatspn.account.event.AccountNotCreated;
import io.spine.examples.chatspn.server.ChatsContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.chatspn.server.given.AccountCreationTestEnv.createRandomAccount;
import static io.spine.examples.chatspn.server.given.GivenEmailAddress.randomEmailAddress;
import static io.spine.testing.TestValues.randomString;

@DisplayName("`AccountCreation` should")
class AccountCreationTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return ChatsContext.newBuilder();
    }

    @Test
    @DisplayName("emit `AccountCreated` event if process finished successfully")
    void createdEvent() {
        User user = createRandomAccount(context());
        AccountCreated expectedEvent = AccountCreated
                .newBuilder()
                .setUser(user.getId())
                .setEmail(user.getEmail())
                .setName(user.getName())
                .vBuild();

        context().assertEvents()
                 .withType(AccountCreated.class)
                 .hasSize(1);
        context().assertEvents()
                 .withType(AccountCreated.class)
                 .message(0)
                 .isEqualTo(expectedEvent);
    }

    @Test
    @DisplayName("reserve an email")
    void reserveEmail() {
        User user = createRandomAccount(context());
        ReservedEmail reservedEmail = ReservedEmail
                .newBuilder()
                .setEmail(user.getEmail())
                .setUser(user.getId())
                .vBuild();

        context().assertState(reservedEmail.getEmail(), ReservedEmail.class)
                 .isEqualTo(reservedEmail);
    }

    @Test
    @DisplayName("emit `AccountNotCreated` event if an email has been already reserved")
    void notCreatedEvent() {
        ReserveEmail reserveEmail = ReserveEmail
                .newBuilder()
                .setEmail(randomEmailAddress())
                .setUser(GivenUserId.generated())
                .vBuild();
        context().receivesCommand(reserveEmail);
        CreateAccount createAccount = CreateAccount
                .newBuilder()
                .setUser(GivenUserId.generated())
                .setEmail(reserveEmail.getEmail())
                .setName(randomString())
                .vBuild();
        context().receivesCommand(createAccount);

        AccountNotCreated expectedEvent = AccountNotCreated
                .newBuilder()
                .setUser(createAccount.getUser())
                .setEmail(createAccount.getEmail())
                .setName(createAccount.getName())
                .vBuild();

        context().assertEvents()
                 .withType(AccountNotCreated.class)
                 .hasSize(1);
        context().assertEvents()
                 .withType(AccountNotCreated.class)
                 .message(0)
                 .isEqualTo(expectedEvent);
    }

    @Test
    @DisplayName("register a `User` with the expected state")
    void registerUser() {
        User user = createRandomAccount(context());

        context().assertState(user.getId(), User.class)
                 .isEqualTo(user);
    }

    @Test
    @DisplayName("display `UserProfile` with the expected state")
    void updateUserProfile() {
        User user = createRandomAccount(context());
        UserProfile userProfile = UserProfile
                .newBuilder()
                .setId(user.getId())
                .setEmail(user.getEmail())
                .setName(user.getName())
                .vBuild();

        context().assertState(userProfile.getId(), UserProfile.class)
                 .isEqualTo(userProfile);
    }
}