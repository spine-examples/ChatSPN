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

package io.spine.examples.chatspn.chat;

import io.spine.core.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.examples.chatspn.chat.PartnerNameExtractor.extractPartnerName;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("PartnerNameExtractor should ")
class PartnerNameExtractorTest {

    private final UserId firstUserId = UserId
            .newBuilder()
            .setValue("firstUserId")
            .vBuild();
    private final String firstUserName = "John Doe";
    private final UserId secondUserId = UserId
            .newBuilder()
            .setValue("secondUserId")
            .vBuild();
    private final String secondUserName = "Emma Smith";

    @DisplayName("extract second user name if the viewer is the first")
    @Test
    void firstUserViewer() {
        var result = extractPartnerName(
                firstUserId,
                firstUserName,
                secondUserId,
                secondUserName,
                firstUserId
        );
        assertThat(result).isEqualTo(secondUserName);
    }

    @DisplayName("extract first user name if the viewer is the second")
    @Test
    void secondUserViewer() {
        var result = extractPartnerName(
                firstUserId,
                firstUserName,
                secondUserId,
                secondUserName,
                secondUserId
        );
        assertThat(result).isEqualTo(firstUserName);
    }

    @DisplayName("throw `IllegalArgumentException` if the viewer is neither " +
            "the first nor the second user")
    @Test
    void wrongUserViewer() {
        var wrongViewerId = UserId
                .newBuilder()
                .setValue("wrongUserId")
                .vBuild();
        assertThrows(IllegalArgumentException.class, () -> {
            extractPartnerName(
                    firstUserId,
                    firstUserName,
                    secondUserId,
                    secondUserName,
                    wrongViewerId
            );
        });
    }
}
