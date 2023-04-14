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

package io.spine.examples.chatspn.message;

import com.google.errorprone.annotations.Immutable;
import io.spine.annotation.GeneratedMixin;
import io.spine.examples.chatspn.ChatDeletionId;
import io.spine.examples.chatspn.MessageId;
import io.spine.examples.chatspn.MessageRemovalId;
import io.spine.examples.chatspn.MessageRemovalOperationId;

/**
 * Common interface for signals aware of the message removal process.
 */
@Immutable
@GeneratedMixin
public interface MessageRemovalSignal {

    /**
     * Returns the ID of the message
     * or default instance if the field does not exist.
     *
     * <p> Use {@link MessageRemovalSignal#message()} instead of this method.
     */
    default MessageId getMessageId() {
        return MessageId.getDefaultInstance();
    }

    /**
     * Checks the existence of the {@code message_id} field in the signal.
     */
    default boolean hasMessageIdField() {
        return !getMessageId().equals(MessageId.getDefaultInstance());
    }

    /**
     * Returns the ID of the message removal process
     * or default instance if the field does not exist.
     *
     * <p> Use {@link MessageRemovalSignal#messageRemoval()} instead of this method.
     */
    default MessageRemovalId getMessageRemovalId() {
        return MessageRemovalId.getDefaultInstance();
    }

    /**
     * Checks the existence of the {@code message_removal_id} field in the signal.
     */
    default boolean hasMessageRemovalIdField() {
        return !getMessageRemovalId().equals(MessageRemovalId.getDefaultInstance());
    }

    /**
     * Returns the ID of the chat deletion process
     * or default instance if the field does not exist.
     *
     * <p> Use {@link MessageRemovalSignal#chatDeletion()} instead of this method.
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
     * Returns the ID of the message removal operation
     * or default instance if the field does not exist.
     *
     * <p> Use {@link MessageRemovalSignal#operation()} instead of this method.
     */
    default MessageRemovalOperationId getOperationId() {
        return MessageRemovalOperationId.getDefaultInstance();
    }

    /**
     * Checks the existence of the {@code operation_id} field in the signal.
     */
    default boolean hasOperationIdField() {
        return !getOperationId().equals(MessageRemovalOperationId.getDefaultInstance());
    }

    /**
     * Returns the message ID
     * or default instance if it is impossible to convert.
     */
    default MessageId message() {
        if (hasMessageIdField()) {
            return getMessageId();
        }
        if (hasMessageRemovalIdField()) {
            return getMessageRemovalId().getId();
        }
        if (hasOperationIdField()) {
            return getOperationId()
                    .getMessageRemoval()
                    .getId();
        }
        return MessageId.getDefaultInstance();
    }

    /**
     * Returns the message removal process ID
     * or default instance if it is impossible to convert.
     */
    default MessageRemovalId messageRemoval() {
        if (hasMessageRemovalIdField()) {
            return getMessageRemovalId();
        }
        if (hasOperationIdField()) {
            return getOperationId().getMessageRemoval();
        }
        if (hasMessageIdField()) {
            return MessageRemovalId
                    .newBuilder()
                    .setId(getMessageId())
                    .vBuild();
        }
        return MessageRemovalId.getDefaultInstance();
    }

    /**
     * Checks the binding of this signal to the message removal process.
     */
    default boolean isMessageRemovalPart() {
        if (hasChatDeletionIdField()) {
            return true;
        }
        if (hasOperationIdField()) {
            return getOperationId().hasMessageRemoval();
        }
        return false;
    }

    /**
     * Returns the chat deletion process ID
     * or default instance if it is impossible to convert.
     */
    default ChatDeletionId chatDeletion() {
        if (hasChatDeletionIdField()) {
            return getChatDeletionId();
        }
        if (hasOperationIdField()) {
            return getOperationId().getChatDeletion();
        }
        return ChatDeletionId.getDefaultInstance();
    }

    /**
     * Checks the binding of this signal to the chat deletion process.
     */
    default boolean isChatDeletionPart() {
        if (hasChatDeletionIdField()) {
            return true;
        }
        if (hasOperationIdField()) {
            return getOperationId().hasChatDeletion();
        }
        return false;
    }

    /**
     * Returns the message removal operation ID
     * or default instance if it is impossible to convert.
     */
    default MessageRemovalOperationId operation() {
        if (hasOperationIdField()) {
            return getOperationId();
        }
        if (hasMessageRemovalIdField()) {
            return MessageRemovalOperationId
                    .newBuilder()
                    .setMessageRemoval(getMessageRemovalId())
                    .vBuild();
        }
        if (hasChatDeletionIdField()) {
            return MessageRemovalOperationId
                    .newBuilder()
                    .setChatDeletion(getChatDeletionId())
                    .vBuild();
        }
        if (hasMessageIdField()) {
            MessageRemovalId messageRemoval = MessageRemovalId
                    .newBuilder()
                    .setId(getMessageId())
                    .vBuild();
            return MessageRemovalOperationId
                    .newBuilder()
                    .setMessageRemoval(messageRemoval)
                    .vBuild();
        }
        return MessageRemovalOperationId.getDefaultInstance();
    }
}
