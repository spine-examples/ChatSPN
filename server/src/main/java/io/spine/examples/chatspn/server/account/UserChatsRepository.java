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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.spine.core.EventContext;
import io.spine.core.UserId;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.account.UserChats;
import io.spine.examples.chatspn.chat.ChatMembers;
import io.spine.examples.chatspn.chat.ChatPreview;
import io.spine.examples.chatspn.server.ProjectionReader;
import io.spine.server.projection.ProjectionRepository;
import io.spine.server.route.StateUpdateRouting;

import java.util.List;
import java.util.Set;

/**
 * The repository for managing {@link UserChatsProjection} instances.
 */
public final class UserChatsRepository
        extends ProjectionRepository<UserId, UserChatsProjection, UserChats> {

    @Override
    protected void setupStateRouting(StateUpdateRouting<UserId> routing) {
        routing.route(ChatPreview.class,
                      (state, context) -> getChatMembers(state.getId(), context));
    }

    private Set<UserId> getChatMembers(ChatId chatId, EventContext ctx) {
        ProjectionReader<ChatId, ChatMembers> reader =
                new ProjectionReader<>(context().stand(), ChatMembers.class);
        ImmutableList<ChatMembers> projections =
                reader.read(ImmutableSet.of(chatId), ctx.getImportContext());
        if (projections.isEmpty()) {
            return ImmutableSet.of();
        }
        List<UserId> members = projections.get(0)
                                          .getMemberList();
        return ImmutableSet.copyOf(members);
    }
}
