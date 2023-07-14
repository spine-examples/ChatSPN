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
import io.spine.core.UserId;
import io.spine.examples.chatspn.ChatCardId;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.chat.ChatCard;
import io.spine.examples.chatspn.chat.ChatMember;
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

import java.util.ArrayList;
import java.util.List;

import static io.spine.examples.chatspn.chat.Chat.ChatType.CT_GROUP;
import static io.spine.examples.chatspn.chat.Chat.ChatType.CT_PERSONAL;

/**
 * {@link ChatCard} projection.
 */
public final class ChatCardProjection
        extends Projection<ChatCardId, ChatCard, ChatCard.Builder> {

    @Subscribe
    void on(PersonalChatCreated e) {
        builder().setViewer(viewerId())
                 .setChatId(chatId())
                 .addMember(e.getCreator())
                 .addMember(e.getMember())
                 .setType(CT_PERSONAL);
    }

    @Subscribe
    void on(GroupChatCreated e) {
        builder().setViewer(viewerId())
                 .setChatId(chatId())
                 .addMember(e.getCreator())
                 .addAllMember(e.getMemberList())
                 .setType(CT_GROUP)
                 .setGroupChatName(e.getName());
    }

    @Subscribe
    void on(MembersAdded e) {
        builder().setViewer(viewerId())
                 .setChatId(chatId())
                 .setType(CT_GROUP)
                 .setGroupChatName(e.getChatName());
        var members = new ArrayList<ChatMember>();
        members.addAll(e.getOldMemberList());
        members.addAll(e.getNewMemberList());
        members.stream()
               .filter(member -> !state()
                       .getMemberList()
                       .contains(member))
               .forEach(member -> builder().addMember(member));
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
        builder().clearMember()
                 .addAllMember(e.getRemainingMemberList());
        if (!isMember(e.getRemainingMemberList(), viewerId())) {
            setDeleted(true);
        }
    }

    @Subscribe
    void on(UserLeftChat e) {
        var userIndex = state()
                .getMemberList()
                .indexOf(e.getUser());
        builder().removeMember(userIndex);
        if (viewerId().equals(e.getUser()
                               .getId())) {
            setDeleted(true);
        }
    }

    /**
     * Extracts viewer ID from the card ID.
     */
    private UserId viewerId() {
        return builder().getCardId()
                        .getUser();
    }

    /**
     * Extracts chat ID from the card ID.
     */
    private ChatId chatId() {
        return builder().getCardId()
                        .getChat();
    }

    /**
     * Tells whether the given user is a part of the specified member list.
     */
    private static boolean isMember(List<ChatMember> members, UserId user) {
        return members
                .stream()
                .anyMatch(member -> member.getId()
                                          .equals(user));
    }
}
