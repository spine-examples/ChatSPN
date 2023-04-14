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

import com.google.errorprone.annotations.Immutable;
import io.spine.annotation.GeneratedMixin;
import io.spine.examples.chatspn.ChatDeletionId;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.message.MessageRemovalSignal;

/**
 * Common interface for signals aware of the chat deletion process.
 */
@Immutable
@GeneratedMixin
public interface ChatDeletionSignal {

    /**
     * Returns the ID of the chat
     * or default instance if the field does not exist.
     */
    default ChatId getChatId() {
        return ChatId.getDefaultInstance();
    }

    /**
     * Checks the existence of the {@code chat_id} field in the signal.
     */
    default boolean hasChatIdField() {
        return !getChatId().equals(ChatId.getDefaultInstance());
    }

    /**
     * Returns the ID of the chat deletion process
     * or default instance if the field does not exist.
     *
     * <p> Use {@link ChatDeletionSignal#chatDeletion()} instead of this method.
     */
    default ChatDeletionId getChatDeletionId() {
        return ChatDeletionId.getDefaultInstance();
    }

    /**
     * Checks the existence of the {@code chat_deletion_id} field in the signal.
     */
    default boolean hasChatDeletionIdField() {
        return !getChatDeletionId().equals(ChatDeletionId.getDefaultInstance());
    }

    /**
     * Returns the chat ID.
     */
    default ChatId chat() {
        if (hasChatIdField()) {
            return getChatId();
        }
        return getChatDeletionId().getId();
    }

    /**
     * Returns the chat deletion ID.
     */
    default ChatDeletionId chatDeletion() {
        if (hasChatIdField()) {
            return ChatDeletionId
                    .newBuilder()
                    .setId(getChatId())
                    .vBuild();
        }
        return getChatDeletionId();
    }
}
