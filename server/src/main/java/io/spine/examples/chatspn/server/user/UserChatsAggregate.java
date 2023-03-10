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

import io.spine.core.UserId;
import io.spine.examples.chatspn.server.user.UserRoot;
import io.spine.examples.chatspn.user.event.UserRegistered;
import io.spine.examples.chatspn.userchats.UserChats;
import io.spine.examples.chatspn.userchats.event.UserChatsCreated;
import io.spine.server.aggregate.AggregatePart;
import io.spine.server.aggregate.Apply;
import io.spine.server.event.React;

/**
 * The {@code UserChatsAggregate} controls the policy of joining chats
 * and settings of chats in which the user is a member.
 */
public final class UserChatsAggregate
        extends AggregatePart<UserId, UserChats, UserChats.Builder, UserRoot> {

    /**
     * Creates a new instance of the aggregate part.
     *
     * @param root
     *         a root of the aggregate to which this part belongs
     */
    private UserChatsAggregate(UserRoot root) {
        super(root);
    }

    @React
    UserChatsCreated on(UserRegistered e) {
        return UserChatsCreated
                .newBuilder()
                .setOwner(e.getUser())
                .vBuild();
    }

    @Apply
    private void event(UserChatsCreated e) {
        builder().setOwner(e.getOwner());
    }
}
