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

import com.google.common.collect.ImmutableSet;
import io.spine.core.CommandContext;
import io.spine.core.UserId;
import io.spine.examples.chatspn.ChatCardId;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.chat.ChatCard;

/**
 * Provides an API to read the {@link ChatCard} projection.
 */
public final class ChatCardReader {

    private final ProjectionReader<ChatCardId, ChatCard> reader;

    public ChatCardReader(ProjectionReader<ChatCardId, ChatCard> reader) {
        this.reader = reader;
    }

    /**
     * Tells whether the given user is a member of the specified chat.
     *
     * <p>If the chat with the provided ID does not exist, just returns {@code false}.
     *
     * @return {@code true} in case user is a member of the chat, {@code false} otherwise
     */
    public boolean isMember(ChatId id, UserId userId, CommandContext ctx) {
        var chatCardId = ChatCardId
                .newBuilder()
                .setChat(id)
                .setUser(userId)
                .vBuild();
        var projections = reader.read(ImmutableSet.of(chatCardId), ctx.getActorContext());
        return !projections.isEmpty();
    }
}
