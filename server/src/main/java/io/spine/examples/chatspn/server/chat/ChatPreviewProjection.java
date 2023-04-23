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

import io.spine.core.Subscribe;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.chat.ChatPreview;
import io.spine.examples.chatspn.chat.ChatPreview.GroupChatView;
import io.spine.examples.chatspn.chat.ChatPreview.MessagePreview;
import io.spine.examples.chatspn.chat.ChatPreview.PersonalChatView;
import io.spine.examples.chatspn.chat.event.ChatMarkedAsDeleted;
import io.spine.examples.chatspn.chat.event.GroupChatCreated;
import io.spine.examples.chatspn.chat.event.PersonalChatCreated;
import io.spine.examples.chatspn.message.event.MessageContentUpdated;
import io.spine.examples.chatspn.message.event.MessageMarkedAsDeleted;
import io.spine.examples.chatspn.message.event.MessagePosted;
import io.spine.server.projection.Projection;

/**
 * Single chat preview.
 */
public final class ChatPreviewProjection
        extends Projection<ChatId, ChatPreview, ChatPreview.Builder> {

    @Subscribe
    void on(PersonalChatCreated e) {
        PersonalChatView view = PersonalChatView
                .newBuilder()
                .setCreator(e.getCreator())
                .setMember(e.getMember())
                .vBuild();
        builder().setId(e.getId())
                 .setPersonalChatView(view);
    }

    @Subscribe
    void on(GroupChatCreated e) {
        GroupChatView view = GroupChatView
                .newBuilder()
                .setName(e.getName())
                .vBuild();
        builder().setId(e.getId())
                 .setGroupChatView(view);
    }

    @Subscribe
    void on(MessagePosted e) {
        MessagePreview message = MessagePreview
                .newBuilder()
                .setId(e.getId())
                .setUser(e.getUser())
                .setContent(e.getContent())
                .setWhenPosted(e.getWhenPosted())
                .vBuild();
        builder().setLastMessage(message);
    }

    @Subscribe
    void on(MessageContentUpdated e) {
        MessagePreview lastMessage = state().getLastMessage();
        if (e.getId()
             .equals(lastMessage.getId())) {
            MessagePreview message = MessagePreview
                    .newBuilder()
                    .setId(e.getId())
                    .setUser(e.getUser())
                    .setContent(e.getContent())
                    .setWhenPosted(lastMessage.getWhenPosted())
                    .vBuild();
            builder().setLastMessage(message);
        }
    }

    @Subscribe
    void on(MessageMarkedAsDeleted e) {
        MessagePreview lastMessage = state().getLastMessage();
        if (e.getId()
              .equals(lastMessage.getId())) {
            builder().setLastMessage(MessagePreview.getDefaultInstance());
        }
    }

    @Subscribe
    void on(ChatMarkedAsDeleted e) {
        setDeleted(true);
    }
}
