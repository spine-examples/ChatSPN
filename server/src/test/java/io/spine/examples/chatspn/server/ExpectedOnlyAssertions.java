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

package io.spine.examples.chatspn.server;

import com.google.protobuf.Message;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;

/**
 * Provide convince API for assertions with expected fields only.
 */
public final class ExpectedOnlyAssertions {

    private ExpectedOnlyAssertions() {
    }

    /**
     * Asserts that expected fields in provided messages are equal.
     */
    public static void assertExpectedFieldsEqual(Message current, Message expected) {
        assertThat(current).comparingExpectedFieldsOnly()
                           .isEqualTo(expected);
    }

    /**
     * Asserts that expected fields in messages from the provided collections are equal.
     */
    public static <M extends Message> void
    assertExpectedFieldsEqual(Iterable<M> current, Iterable<M> expected) {
        assertThat(current).comparingExpectedFieldsOnly()
                           .containsAtLeastElementsIn(expected);
    }
}
