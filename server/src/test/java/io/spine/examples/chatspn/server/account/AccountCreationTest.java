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
import io.spine.examples.chatspn.server.ChatsContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.chatspn.server.account.given.AccountCreationTestEnv.accountCreatedFrom;
import static io.spine.examples.chatspn.server.account.given.AccountCreationTestEnv.accountNotCreatedFrom;
import static io.spine.examples.chatspn.server.account.given.AccountCreationTestEnv.createAccountCommandWith;
import static io.spine.examples.chatspn.server.account.given.AccountCreationTestEnv.emailAlreadyReservedFrom;
import static io.spine.examples.chatspn.server.account.given.AccountCreationTestEnv.emailReservedFrom;
import static io.spine.examples.chatspn.server.account.given.AccountCreationTestEnv.randomCreateAccountCommand;
import static io.spine.examples.chatspn.server.account.given.AccountCreationTestEnv.reservedEmailFrom;
import static io.spine.examples.chatspn.server.account.given.AccountCreationTestEnv.userFrom;
import static io.spine.examples.chatspn.server.account.given.AccountCreationTestEnv.userProfileFrom;
import static io.spine.examples.chatspn.server.account.given.AccountCreationTestEnv.userRegisteredFrom;

@DisplayName("`AccountCreation` should")
final class AccountCreationTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return ChatsContext.newBuilder();
    }

    @Test
    @DisplayName("emit the `AccountCreated` event if the process is finished successfully and archive itself")
    void createdEvent() {
        var command = randomCreateAccountCommand();
        context().receivesCommand(command);
        var expected = accountCreatedFrom(command);

        context().assertEvent(expected);
        context().assertEntity(command.getId(), AccountCreationProcess.class)
                 .archivedFlag()
                 .isTrue();
    }

    @Test
    @DisplayName("reserve an email")
    void reserveEmail() {
        var command = randomCreateAccountCommand();
        context().receivesCommand(command);
        var state = reservedEmailFrom(command);

        context().assertState(state.getEmail(), ReservedEmail.class)
                 .isEqualTo(state);
    }

    @Test
    @DisplayName("lead `ReservedEmailAggregate` to emit an `EmailReserved` event")
    void emailReservedEvent() {
        var command = randomCreateAccountCommand();
        context().receivesCommand(command);
        var expected = emailReservedFrom(command);

        context().assertEvent(expected);
    }

    @Test
    @DisplayName("lead `ReservedEmailAggregate` to emit an `EmailAlreadyReserved` rejection")
    void emailAlreadyReservedRejection() {
        var firstCommand = randomCreateAccountCommand();
        context().receivesCommand(firstCommand);
        var secondCommand = createAccountCommandWith(firstCommand.getEmail());
        context().receivesCommand(secondCommand);
        var expected = emailAlreadyReservedFrom(secondCommand);

        context().assertEvent(expected);
    }

    @Test
    @DisplayName("emit the `AccountNotCreated` event if an email has been already reserved and archive itself")
    void notCreatedEvent() {
        var firstCommand = randomCreateAccountCommand();
        context().receivesCommand(firstCommand);
        var secondCommand = createAccountCommandWith(firstCommand.getEmail());
        context().receivesCommand(secondCommand);
        var expected = accountNotCreatedFrom(secondCommand);

        context().assertEvent(expected);
        context().assertEntity(secondCommand.getId(), AccountCreationProcess.class)
                 .archivedFlag()
                 .isTrue();
    }

    @Test
    @DisplayName("register a `User` with the expected state")
    void registerUser() {
        var command = randomCreateAccountCommand();
        context().receivesCommand(command);
        var user = userFrom(command);

        context().assertState(user.getId(), User.class)
                 .isEqualTo(user);
    }

    @Test
    @DisplayName("lead `UserAggregate` to emit an `UserRegistered` event")
    void userRegisteredEvent() {
        var command = randomCreateAccountCommand();
        context().receivesCommand(command);
        var expected = userRegisteredFrom(command);

        context().assertEvent(expected);
    }

    @Test
    @DisplayName("display `UserProfile` with the expected state")
    void updateUserProfile() {
        var command = randomCreateAccountCommand();
        context().receivesCommand(command);
        var userProfile = userProfileFrom(command);

        context().assertState(userProfile.getId(), UserProfile.class)
                 .isEqualTo(userProfile);
    }
}
