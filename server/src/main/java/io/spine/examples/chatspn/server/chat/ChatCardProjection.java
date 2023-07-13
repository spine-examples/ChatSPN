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
import io.spine.examples.chatspn.ChatCardId;
import io.spine.examples.chatspn.chat.ChatCard;
import io.spine.examples.chatspn.chat.ChatMembers;
import io.spine.examples.chatspn.chat.event.ChatMarkedAsDeleted;
import io.spine.examples.chatspn.chat.event.GroupChatCreated;
import io.spine.examples.chatspn.chat.event.MembersAdded;
import io.spine.examples.chatspn.chat.event.MembersRemoved;
import io.spine.examples.chatspn.chat.event.PersonalChatCreated;
import io.spine.examples.chatspn.chat.event.UserLeftChat;
import io.spine.examples.chatspn.message.MessageView;
import io.spine.examples.chatspn.message.event.MessageContentUpdated;
import io.spine.examples.chatspn.message.event.MessageMarkedAsDeleted;
import io.spine.examples.chatspn.message.event.MessagePosted;
import io.spine.server.projection.Projection;

import static io.spine.examples.chatspn.chat.Chat.ChatType.CT_GROUP;
import static io.spine.examples.chatspn.chat.Chat.ChatType.CT_PERSONAL;

/**
 * {@link ChatCard} projection.
 */
public final class ChatCardProjection
        extends Projection<ChatCardId, ChatCard, ChatCard.Builder> {

    @Subscribe
    void on(PersonalChatCreated e) {
        var viewer = builder().getCardId()
                              .getUser();
        var chat = builder().getCardId()
                            .getChat();
        var chatMembers = ChatMembers
                .newBuilder()
                .setId(chat)
                .addMember(e.getCreator())
                .addMember(e.getMember())
                .vBuild();
        var chatmates = chatMembers.chatmatesFor(viewer)
                                   .asList();
        builder().setName(chatmates.get(0)
                                   .getName())
                 .setViewer(viewer)
                 .setChatId(chat)
                 .setType(CT_PERSONAL);
    }

    @Subscribe
    void on(GroupChatCreated e) {
        var cardOwner = builder().getCardId()
                                 .getUser();
        var chat = builder().getCardId()
                            .getChat();
        builder().setName(e.getName())
                 .setViewer(cardOwner)
                 .setChatId(chat)
                 .setType(CT_GROUP);
    }

    @Subscribe
    void on(MembersAdded e) {
        var cardOwner = builder().getCardId()
                                 .getUser();
        var chat = builder().getCardId()
                            .getChat();
        builder().setName(e.getChatName())
                 .setViewer(cardOwner)
                 .setChatId(chat)
                 .setType(CT_GROUP);
    }

    @Subscribe
    void on(MessagePosted e) {
        var message = MessageView
                .newBuilder()
                .setId(e.getId())
                .setChat(e.getChat())
                .setUser(e.getUser())
                .setContent(e.getContent())
                .setWhenPosted(e.getWhenPosted())
                .vBuild();
        builder().setLastMessage(message);
    }

    @Subscribe
    void on(MessageContentUpdated e) {
        var lastMessage = state().getLastMessage();
        if (e.getId()
             .equals(lastMessage.getId())) {
            var message = MessageView
                    .newBuilder()
                    .setId(e.getId())
                    .setChat(e.getChat())
                    .setUser(e.getUser())
                    .setContent(e.getContent())
                    .setWhenPosted(lastMessage.getWhenPosted())
                    .vBuild();
            builder().setLastMessage(message);
        }
    }

    @Subscribe
    void on(MessageMarkedAsDeleted e) {
        var lastMessage = state().getLastMessage();
        if (e.getId()
             .equals(lastMessage.getId())) {
            builder().setLastMessage(MessageView.getDefaultInstance());
        }
    }

    @Subscribe
    void on(ChatMarkedAsDeleted e) {
        setDeleted(true);
    }

    @Subscribe
    void on(MembersRemoved e) {
        setDeleted(true);
    }

    @Subscribe
    void on(UserLeftChat e) {
        setDeleted(true);
    }
}
