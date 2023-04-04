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
import io.spine.core.UserId;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.chat.Chat;
import io.spine.examples.chatspn.chat.command.CreateGroupChat;
import io.spine.examples.chatspn.chat.command.CreatePersonalChat;
import io.spine.examples.chatspn.chat.command.ExcludeMembers;
import io.spine.examples.chatspn.chat.event.GroupChatCreated;
import io.spine.examples.chatspn.chat.event.MembersExcluded;
import io.spine.examples.chatspn.chat.event.PersonalChatCreated;
import io.spine.examples.chatspn.chat.rejection.MembersCannotBeExcluded;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.examples.chatspn.chat.Chat.ChatType.CT_GROUP;
import static io.spine.examples.chatspn.chat.Chat.ChatType.CT_PERSONAL;

/**
 * A chat between two or more users.
 */
public final class ChatAggregate extends Aggregate<ChatId, Chat, Chat.Builder> {

    /**
     * Handles the command to create a personal chat.
     */
    @Assign
    PersonalChatCreated handle(CreatePersonalChat c) {
        return PersonalChatCreated
                .newBuilder()
                .setId(c.getId())
                .setCreator(c.getCreator())
                .setMember(c.getMember())
                .vBuild();
    }

    @Apply
    private void event(PersonalChatCreated e) {
        builder().setId(e.getId())
                 .addMember(e.getCreator())
                 .addMember(e.getMember())
                 .setType(CT_PERSONAL);
    }

    /**
     * Handles the command to create a group chat.
     */
    @Assign
    GroupChatCreated handle(CreateGroupChat c) {
        return GroupChatCreated
                .newBuilder()
                .setId(c.getId())
                .setCreator(c.getCreator())
                .addAllMember(c.getMemberList())
                .setName(c.getName())
                .vBuild();
    }

    @Apply
    private void event(GroupChatCreated e) {
        builder().setId(e.getId())
                 .addMember(e.getCreator())
                 .addAllMember(e.getMemberList())
                 .setName(e.getName())
                 .setOwner(e.getCreator())
                 .setType(CT_GROUP);
    }

    /**
     * Handles the command to exclude members from the chat.
     * The member who sent the command cannot be excluded.
     *
     * @throws MembersCannotBeExcluded
     *         if chat isn't a group,
     *         or the user who sent the original command, is not a chat owner,
     *         or all users to exclude already aren't the chat members
     */
    @Assign
    MembersExcluded handle(ExcludeMembers c) throws MembersCannotBeExcluded {
        ImmutableList<UserId> remainingMembers = extractRemainingMembers(c);
        if (checkExclusionPossibility(c, remainingMembers)) {
            return MembersExcluded
                    .newBuilder()
                    .setId(c.getId())
                    .setWhoExcludes(c.getWhoExcludes())
                    .addAllRemainingMember(remainingMembers)
                    .vBuild();
        }
        throw MembersCannotBeExcluded
                .newBuilder()
                .setId(c.getId())
                .setWhoIncludes(c.getWhoExcludes())
                .addAllMember(c.getMemberList())
                .build();
    }

    @Apply
    private void event(MembersExcluded e) {
        builder().clearMember()
                 .addAllMember(e.getRemainingMemberList());
    }

    private boolean checkExclusionPossibility(ExcludeMembers command,
                                              List<UserId> remainingMembers) {
        boolean isGroupChat = state().getType() == CT_GROUP;
        boolean isUserWhoExcludesIsOwner = state().getOwner()
                                                  .equals(command.getWhoExcludes());
        boolean isSomeoneExcluded = remainingMembers.size() < state().getMemberCount();
        return isGroupChat && isUserWhoExcludesIsOwner && isSomeoneExcluded;
    }

    private ImmutableList<UserId> extractRemainingMembers(ExcludeMembers command) {
        List<UserId> chatMembers = state().getMemberList();
        List<UserId> membersInCommand = command.getMemberList();
        ImmutableList<UserId> remainingMembers =
                chatMembers.stream()
                           .filter(userId -> !membersInCommand.contains(userId) ||
                                   userId.equals(command.getWhoExcludes()))
                           .collect(toImmutableList());
        return remainingMembers;
    }
}
