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

package io.spine.examples.chatspn.server.chat;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.chat.ChatPreview;
import io.spine.examples.chatspn.chat.event.GroupChatCreated;
import io.spine.examples.chatspn.chat.event.PersonalChatCreated;
import io.spine.examples.chatspn.message.event.MessageContentUpdated;
import io.spine.examples.chatspn.message.event.MessageMarkedAsDeleted;
import io.spine.examples.chatspn.message.event.MessagePosted;
import io.spine.server.projection.ProjectionRepository;
import io.spine.server.route.EventRouting;

import static io.spine.server.route.EventRoute.withId;

/**
 * The repository for managing {@link ChatMembersProjection} instances.
 */
public final class ChatPreviewRepository
        extends ProjectionRepository<ChatId, ChatPreviewProjection, ChatPreview> {

    @OverridingMethodsMustInvokeSuper
    @Override
    protected void setupEventRouting(EventRouting<ChatId> routing) {
        super.setupEventRouting(routing);
        routing.route(PersonalChatCreated.class, (event, context) -> withId(event.getId()))
               .route(GroupChatCreated.class, (event, context) -> withId(event.getId()))
               .route(MessagePosted.class, (event, context) -> withId(event.getChat()))
               .route(MessageContentUpdated.class, (event, context) -> withId(event.getChat()))
               .route(MessageMarkedAsDeleted.class, (event, context) -> withId(event.getChat()));
    }
}
