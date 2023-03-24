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
import io.spine.examples.chatspn.server.ChatsContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.chatspn.server.given.AccountCreationTestEnv.sendRandomCreateAccountCommand;
import static io.spine.testing.TestValues.randomString;

@DisplayName("`AccountCreation` should")
class AccountCreationTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return ChatsContext.newBuilder();
    }

    @Test
    @DisplayName("emit the `AccountCreated` event if the process is finished successfully and archive itself")
    void createdEvent() {
        CreateAccount command = sendRandomCreateAccountCommand(context());
        AccountCreated expectedEvent = AccountCreated
                .newBuilder()
                .setId(command.getId())
                .setUser(command.getUser())
                .setEmail(command.getEmail())
                .setName(command.getName())
                .vBuild();

        context().assertEvents()
                 .withType(AccountCreated.class)
                 .hasSize(1);
        context().assertEvents()
                 .withType(AccountCreated.class)
                 .message(0)
                 .isEqualTo(expectedEvent);
        context().assertEntity(command.getId(), AccountCreationProcess.class)
                 .archivedFlag()
                 .isTrue();
    }

    @Test
    @DisplayName("reserve an email")
    void reserveEmail() {
        CreateAccount command = sendRandomCreateAccountCommand(context());
        ReservedEmail reservedEmail = ReservedEmail
                .newBuilder()
                .setEmail(command.getEmail())
                .setUser(command.getUser())
                .vBuild();

        context().assertState(reservedEmail.getEmail(), ReservedEmail.class)
                 .isEqualTo(reservedEmail);
    }

    @Test
    @DisplayName("lead `ReservedEmailAggregate` to emit an `EmailReserved` event")
    void emailReservedEvent() {
        CreateAccount command = sendRandomCreateAccountCommand(context());
        EmailReserved expectedEvent = EmailReserved
                .newBuilder()
                .setEmail(command.getEmail())
                .setUser(command.getUser())
                .setProcess(command.getId())
                .vBuild();

        context().assertEvents()
                 .withType(EmailReserved.class)
                 .hasSize(1);
        context().assertEvents()
                 .withType(EmailReserved.class)
                 .message(0)
                 .isEqualTo(expectedEvent);
    }

    @Test
    @DisplayName("lead `ReservedEmailAggregate` to emit an `EmailAlreadyReserved` rejection")
    void emailAlreadyReservedRejection() {
        CreateAccount command = sendRandomCreateAccountCommand(context());
        CreateAccount createAccount = CreateAccount
                .newBuilder()
                .setId(AccountCreationId.generate())
                .setUser(GivenUserId.generated())
                .setEmail(command.getEmail())
                .setName(randomString())
                .vBuild();
        context().receivesCommand(createAccount);
        EmailAlreadyReserved expectedEvent = EmailAlreadyReserved
                .newBuilder()
                .setEmail(createAccount.getEmail())
                .setUser(createAccount.getUser())
                .setProcess(createAccount.getId())
                .vBuild();

        context().assertEvents()
                 .withType(EmailAlreadyReserved.class)
                 .hasSize(1);
        context().assertEvents()
                 .withType(EmailAlreadyReserved.class)
                 .message(0)
                 .isEqualTo(expectedEvent);
    }

    @Test
    @DisplayName("emit the `AccountNotCreated` event if an email has been already reserved and archive itself")
    void notCreatedEvent() {
        CreateAccount command = sendRandomCreateAccountCommand(context());
        CreateAccount createAccount = CreateAccount
                .newBuilder()
                .setId(AccountCreationId.generate())
                .setUser(GivenUserId.generated())
                .setEmail(command.getEmail())
                .setName(randomString())
                .vBuild();
        context().receivesCommand(createAccount);
        AccountNotCreated expectedEvent = AccountNotCreated
                .newBuilder()
                .setId(createAccount.getId())
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
        context().assertEntity(createAccount.getId(), AccountCreationProcess.class)
                 .archivedFlag()
                 .isTrue();
    }

    @Test
    @DisplayName("register a `User` with the expected state")
    void registerUser() {
        CreateAccount command = sendRandomCreateAccountCommand(context());
        User user = User
                .newBuilder()
                .setId(command.getUser())
                .setName(command.getName())
                .setEmail(command.getEmail())
                .vBuild();

        context().assertState(user.getId(), User.class)
                 .isEqualTo(user);
    }

    @Test
    @DisplayName("lead `UserAggregate` to emit an `UserRegistered` event")
    void userRegisteredEvent() {
        CreateAccount command = sendRandomCreateAccountCommand(context());
        UserRegistered expectedEvent = UserRegistered
                .newBuilder()
                .setUser(command.getUser())
                .setEmail(command.getEmail())
                .setName(command.getName())
                .setProcess(command.getId())
                .vBuild();

        context().assertEvents()
                 .withType(UserRegistered.class)
                 .hasSize(1);
        context().assertEvents()
                 .withType(UserRegistered.class)
                 .message(0)
                 .isEqualTo(expectedEvent);
    }

    @Test
    @DisplayName("display `UserProfile` with the expected state")
    void updateUserProfile() {
        CreateAccount command = sendRandomCreateAccountCommand(context());
        UserProfile userProfile = UserProfile
                .newBuilder()
                .setId(command.getUser())
                .setEmail(command.getEmail())
                .setName(command.getName())
                .vBuild();

        context().assertState(userProfile.getId(), UserProfile.class)
                 .isEqualTo(userProfile);
    }
}
