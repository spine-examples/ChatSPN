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

import io.spine.examples.chatspn.server.account.AccountCreationRepository;
import io.spine.examples.chatspn.server.account.ReservedEmailAggregate;
import io.spine.examples.chatspn.server.account.UserAggregate;
import io.spine.examples.chatspn.server.account.UserChatsRepository;
import io.spine.examples.chatspn.server.account.UserProfileRepository;
import io.spine.examples.chatspn.server.chat.ChatAggregate;
import io.spine.examples.chatspn.server.chat.ChatDeletionRepository;
import io.spine.examples.chatspn.server.chat.ChatMembersRepository;
import io.spine.examples.chatspn.server.chat.ChatPreviewRepository;
import io.spine.examples.chatspn.server.message.MessageAggregate;
import io.spine.examples.chatspn.server.message.MessageEditingRepository;
import io.spine.examples.chatspn.server.message.MessageRemovalRepository;
import io.spine.examples.chatspn.server.message.MessageSendingRepository;
import io.spine.examples.chatspn.server.message.MessageViewRepository;
import io.spine.server.BoundedContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.server.DefaultRepository;

import static io.spine.examples.chatspn.BoundedContextName.CHATS;

/**
 * Configures Chats Bounded Context with repositories.
 */
public final class ChatsContext {

    /**
     * Prevents instantiation of this class.
     */
    private ChatsContext() {
    }

    /**
     * Creates {@code BoundedContextBuilder} for the Chats context
     * and fills it with repositories.
     */
    public static BoundedContextBuilder newBuilder() {
        return BoundedContext
                .singleTenant(CHATS)
                .add(DefaultRepository.of(UserAggregate.class))
                .add(DefaultRepository.of(ChatAggregate.class))
                .add(DefaultRepository.of(MessageAggregate.class))
                .add(new UserProfileRepository())
                .add(new ChatMembersRepository())
                .add(new MessageSendingRepository())
                .add(new MessageEditingRepository())
                .add(new MessageRemovalRepository())
                .add(DefaultRepository.of(ReservedEmailAggregate.class))
                .add(new AccountCreationRepository())
                .add(new MessageViewRepository())
                .add(new ChatDeletionRepository())
                .add(new ChatPreviewRepository())
                .add(new UserChatsRepository());
    }
}
