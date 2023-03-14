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

package io.spine.examples.chatspn.server.user;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.spine.core.UserId;
import io.spine.examples.chatspn.user.UserBlocklist;
import io.spine.examples.chatspn.user.event.UserBlocked;
import io.spine.examples.chatspn.user.event.UserRegistered;
import io.spine.examples.chatspn.user.event.UserUnblocked;
import io.spine.server.projection.ProjectionRepository;
import io.spine.server.route.EventRouting;

import static io.spine.server.route.EventRoute.withId;

/**
 * The repository for managing {@link UserBlocklistProjection} instances.
 */
public final class UserBlocklistRepository
        extends ProjectionRepository<UserId, UserBlocklistProjection, UserBlocklist> {

    @OverridingMethodsMustInvokeSuper
    @Override
    protected void setupEventRouting(EventRouting<UserId> routing) {
        super.setupEventRouting(routing);
        routing.route(UserRegistered.class, (event, context) -> withId(event.getUser()))
               .route(UserBlocked.class, (event, context) -> withId(event.getBlockingUser()))
               .route(UserUnblocked.class, (event, context) -> withId(event.getUnblockingUser()));
    }
}