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

/**
 * Utility class to extract partner name.
 */
public class PartnerNameExtractor {

    /**
     * Prevents instantiation of this class.
     */
    private PartnerNameExtractor() {
    }

    /**
     * Returns partner name for the viewer.
     *
     * <p>For the first user, the partner is the second user and inversely
     *
     * @param firstUserId
     *         ID of the first user
     * @param firstUserName
     *         name of the first user
     * @param secondUserId
     *         ID of the second user
     * @param secondUserName
     *         name of the second user
     * @param viewerId
     *         ID of the viewer, must be same as {@code firstUserId} or {@code secondUserId}
     */
    public static String extractPartnerName(
            UserId firstUserId,
            String firstUserName,
            UserId secondUserId,
            String secondUserName,
            UserId viewerId
    ) {
        if (firstUserId.equals(viewerId)) {
            return secondUserName;
        } else if (secondUserId.equals(viewerId)) {
            return firstUserName;
        } else {
            throw new IllegalArgumentException(
                    "The viewer is neither the first nor the second user.");
        }
    }
}
