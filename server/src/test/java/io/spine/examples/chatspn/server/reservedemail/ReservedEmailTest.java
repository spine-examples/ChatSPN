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
import io.spine.examples.chatspn.reservedemail.command.ReserveEmail;
import io.spine.examples.chatspn.reservedemail.event.EmailReserved;
import io.spine.examples.chatspn.reservedemail.rejection.Rejections.EmailAlreadyReserved;
import io.spine.examples.chatspn.server.ChatsContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.chatspn.server.given.GivenEmailAddress.randomEmailAddress;

@DisplayName("`ReservedEmail` should")
class ReservedEmailTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return ChatsContext.newBuilder();
    }

    @Test
    @DisplayName("allow reservation and emit the `EmailReserved` event")
    void reservation() {
        ReserveEmail command = ReserveEmail
                .newBuilder()
                .setEmail(randomEmailAddress())
                .setUser(GivenUserId.generated())
                .vBuild();

        context().receivesCommand(command);

        EmailReserved expectedEvent = EmailReserved
                .newBuilder()
                .setEmail(command.getEmail())
                .setUser(command.getUser())
                .build();
        ReservedEmail expectedState = ReservedEmail
                .newBuilder()
                .setEmail(command.getEmail())
                .setUser(command.getUser())
                .build();

        context().assertEvents()
                 .withType(EmailReserved.class)
                 .hasSize(1);
        context().assertEvent(expectedEvent);
        context().assertState(command.getEmail(), ReservedEmail.class)
                 .isEqualTo(expectedState);
    }

    @Test
    @DisplayName("reject reservation when email has been already reserved")
    void rejection() {
        ReserveEmail firstCommand = ReserveEmail
                .newBuilder()
                .setEmail(randomEmailAddress())
                .setUser(GivenUserId.generated())
                .vBuild();
        ReserveEmail secondCommand = ReserveEmail
                .newBuilder()
                .setEmail(firstCommand.getEmail())
                .setUser(GivenUserId.generated())
                .vBuild();

        context().receivesCommand(firstCommand);
        context().receivesCommand(secondCommand);

        EmailAlreadyReserved expectedRejection = EmailAlreadyReserved
                .newBuilder()
                .setEmail(secondCommand.getEmail())
                .setUser(secondCommand.getUser())
                .vBuild();

        context().assertEvents()
                 .withType(EmailAlreadyReserved.class)
                 .message(0)
                 .isEqualTo(expectedRejection);
    }
}
