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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.spine.core.EventContext;
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
import io.spine.examples.chatspn.message.event.MessageContentUpdated;
import io.spine.examples.chatspn.message.event.MessageMarkedAsDeleted;
import io.spine.examples.chatspn.message.event.MessagePosted;
import io.spine.examples.chatspn.server.ProjectionReader;
import io.spine.server.projection.ProjectionRepository;
import io.spine.server.route.EventRouting;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static io.spine.client.Filters.eq;

/**
 * The repository for managing {@link ChatCardProjection} instances.
 */
public final class ChatCardRepository
        extends ProjectionRepository<ChatCardId, ChatCardProjection, ChatCard> {

    @OverridingMethodsMustInvokeSuper
    @Override
    protected void setupEventRouting(EventRouting<ChatCardId> routing) {
        super.setupEventRouting(routing);
        routing.route(PersonalChatCreated.class,
                      (event, context) -> toUsersInChat(
                              ImmutableList.of(event.getCreator(), event.getMember()),
                              event.getId()))
               .route(GroupChatCreated.class,
                      (event, context) -> {
                          var users = new ArrayList<>(event.getMemberList());
                          users.add(event.getCreator());
                          return toUsersInChat(users, event.getId());
                      })
               .route(ChatMarkedAsDeleted.class,
                      (event, context) -> toEverybodyInChat(event.getId(), context))
               .route(MessagePosted.class,
                      (event, context) -> toEverybodyInChat(event.getChat(), context))
               .route(MessageContentUpdated.class,
                      (event, context) -> toEverybodyInChat(event.getChat(), context))
               .route(MessageMarkedAsDeleted.class,
                      (event, context) -> toEverybodyInChat(event.getChat(), context))
               .route(MembersAdded.class, (event, context) -> toExistingAndNewMembers(event))
               .route(MembersRemoved.class,
                      (event, context) -> toEverybodyInChat(event.getId(), context))
               .route(UserLeftChat.class,
                      (event, context) -> toEverybodyInChat(event.getChat(), context));
    }

    /**
     * Returns IDs of chat cards of provided users in the chat.
     *
     * @param users
     *         user IDs whose chat card IDs to return
     * @param chat
     *         ID of the chat which card IDs to return
     */
    private static ImmutableSet<ChatCardId> toUsersInChat(List<ChatMember> users, ChatId chat) {
        return users
                .stream()
                .map(user -> chatCardId(chat, user.getId()))
                .collect(toImmutableSet());
    }

    /**
     * Returns IDs of chat cards of all members in the chat.
     *
     * @param chatId
     *         ID of the chat which card IDs to return
     * @param ctx
     *         event context
     */
    private ImmutableSet<ChatCardId> toEverybodyInChat(ChatId chatId, EventContext ctx) {
        var reader = new ProjectionReader<ChatCardId, ChatCard>(context().stand(),
                                                                ChatCard.class);
        var chatFilter = eq(ChatCard.Field.chatId(), chatId);
        var cards = reader.read(ctx.actorContext(), chatFilter);
        return cards
                .stream()
                .map(ChatCard::getCardId)
                .collect(toImmutableSet());
    }

    /**
     * Returns IDs of chat cards of old and new members in the chat.
     */
    private static ImmutableSet<ChatCardId> toExistingAndNewMembers(MembersAdded event) {
        var members = new ArrayList<ChatMember>();
        members.addAll(event.getNewMemberList());
        members.addAll(event.getOldMemberList());
        return members
                .stream()
                .map(member -> chatCardId(event.getId(), member.getId()))
                .collect(toImmutableSet());
    }

    /**
     * Builds {@code ChatCardId}.
     */
    private static ChatCardId chatCardId(ChatId chat, UserId user) {
        return ChatCardId
                .newBuilder()
                .setChat(chat)
                .setUser(user)
                .vBuild();
    }
}
