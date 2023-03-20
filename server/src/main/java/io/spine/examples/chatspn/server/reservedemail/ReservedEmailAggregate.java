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

package io.spine.examples.chatspn.server.reservedemail;

import io.spine.examples.chatspn.reservedemail.ReservedEmail;
import io.spine.examples.chatspn.reservedemail.event.EmailReserved;
import io.spine.examples.chatspn.reservedemail.rejection.EmailAlreadyReserved;
import io.spine.net.EmailAddress;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

/**
 * Reserved email in the ChatSPN.
 */
public final class ReservedEmailAggregate
        extends Aggregate<EmailAddress, ReservedEmail, ReservedEmail.Builder> {

    /**
     * Handles the command to reserve an email.
     *
     * @throws EmailAlreadyReserved
     *         if email address has already been reserved by another user
     */
    @Assign
    EmailReserved handle(ReservedEmail c) throws EmailAlreadyReserved {
        if (state().hasUser()) {
            throw EmailAlreadyReserved
                    .newBuilder()
                    .setEmail(c.getEmail())
                    .setUser(c.getUser())
                    .build();
        }
        return EmailReserved
                .newBuilder()
                .setEmail(c.getEmail())
                .setUser(c.getUser())
                .vBuild();
    }

    @Apply
    private void event(EmailReserved e) {
        builder().setEmail(e.getEmail())
                 .setUser(e.getUser());
    }
}
