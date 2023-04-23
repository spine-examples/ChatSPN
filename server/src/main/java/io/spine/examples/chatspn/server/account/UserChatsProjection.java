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

import io.spine.core.Subscribe;
import io.spine.core.UserId;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.account.UserChats;
import io.spine.examples.chatspn.account.event.UserRegistered;
import io.spine.examples.chatspn.chat.ChatPreview;
import io.spine.examples.chatspn.chat.event.ChatMarkedAsDeleted;
import io.spine.examples.chatspn.chat.event.MembersRemoved;
import io.spine.examples.chatspn.chat.event.UserLeftChat;
import io.spine.server.projection.Projection;

import java.util.Optional;

/**
 * Previews of chats in which the user is a member.
 */
public final class UserChatsProjection extends Projection<UserId, UserChats, UserChats.Builder> {

    @Subscribe
    void on(UserRegistered e) {
        builder().setId(e.getUser());
    }

    @Subscribe
    void onUpdate(ChatPreview s) {
        Optional<Integer> index = findChatIndex(s.getId());
        if (index.isPresent()) {
            builder().setChat(index.get(), s);
        } else {
            builder().addChat(s);
        }
    }

    @Subscribe
    void on(ChatMarkedAsDeleted e) {
        removeChat(e.getId());
    }

    @Subscribe
    void on(UserLeftChat e) {
        removeChat(e.getChat());
    }

    @Subscribe
    void on(MembersRemoved e) {
        removeChat(e.getId());
    }

    private void removeChat(ChatId id) {
        Optional<Integer> optionalIndex = findChatIndex(id);
        optionalIndex.ifPresent(index -> builder().removeChat(index));
    }

    /**
     * Returns the index of the chat in the list from the state.
     *
     * <p>Returns {@code Optional.empty()} if the chat doesn't exist.
     */
    private Optional<Integer> findChatIndex(ChatId chatId) {
        Optional<ChatPreview> optionalChat =
                state().getChatList()
                       .stream()
                       .filter(chat -> chat.getId()
                                           .equals(chatId))
                       .findFirst();
        if (optionalChat.isPresent()) {
            int chatIndex = state().getChatList()
                                   .indexOf(optionalChat.get());
            return Optional.of(chatIndex);
        }
        return Optional.empty();
    }
}
