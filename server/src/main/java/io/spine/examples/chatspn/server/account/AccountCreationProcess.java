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
import io.spine.examples.chatspn.account.AccountCreation;
import io.spine.examples.chatspn.account.command.CreateAccount;
import io.spine.examples.chatspn.account.command.RegisterUser;
import io.spine.examples.chatspn.account.command.ReserveEmail;
import io.spine.examples.chatspn.account.event.AccountCreated;
import io.spine.examples.chatspn.account.event.AccountNotCreated;
import io.spine.examples.chatspn.account.event.EmailReserved;
import io.spine.examples.chatspn.account.event.UserRegistered;
import io.spine.examples.chatspn.account.rejection.ReservedEmailRejections.EmailAlreadyReserved;
import io.spine.server.command.Command;
import io.spine.server.event.React;
import io.spine.server.procman.ProcessManager;

/**
 * Coordinates the account creation for user in the ChatSPN.
 */
public final class AccountCreationProcess
        extends ProcessManager<AccountCreationId, AccountCreation, AccountCreation.Builder> {

    /**
     * Issues a command to reserve email address.
     */
    @Command
    ReserveEmail on(CreateAccount c) {
        initState(c);
        return ReserveEmail
                .newBuilder()
                .setEmail(c.getEmail())
                .setUser(c.getUser())
                .setAccountCreationProcess(c.getId())
                .vBuild();
    }

    private void initState(CreateAccount c) {
        builder()
                .setId(c.getId())
                .setUser(c.getUser())
                .setName(c.getName())
                .setEmail(c.getEmail());
    }

    /**
     * Issues a command to register user after successfully email reservation.
     */
    @Command
    RegisterUser on(EmailReserved e) {
        return RegisterUser
                .newBuilder()
                .setUser(e.getUser())
                .setName(state().getName())
                .setEmail(e.getEmail())
                .setAccountCreationProcess(state().getId())
                .vBuild();
    }

    /**
     * Terminates the process if email address cannot be reserved.
     */
    @React
    AccountNotCreated on(EmailAlreadyReserved e) {
        setArchived(true);
        return AccountNotCreated
                .newBuilder()
                .setId(state().getId())
                .setUser(e.getUser())
                .setName(state().getName())
                .setEmail(e.getEmail())
                .vBuild();
    }

    /**
     * Archives the process when the account created.
     */
    @React
    AccountCreated on(UserRegistered e) {
        setArchived(true);
        return AccountCreated
                .newBuilder()
                .setId(state().getId())
                .setUser(e.getUser())
                .setName(e.getName())
                .setEmail(e.getEmail())
                .vBuild();
    }
}
