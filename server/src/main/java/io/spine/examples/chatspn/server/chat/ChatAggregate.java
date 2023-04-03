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
import io.spine.examples.chatspn.chat.command.IncludeMembers;
import io.spine.examples.chatspn.chat.event.GroupChatCreated;
import io.spine.examples.chatspn.chat.event.MembersIncluded;
import io.spine.examples.chatspn.chat.event.PersonalChatCreated;
import io.spine.examples.chatspn.chat.rejection.MembersCannotBeIncluded;
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
                 .setType(CT_GROUP);
    }

    /**
     * Handles the command to include members to the chat.
     *
     * @throws MembersCannotBeIncluded
     *         if chat type isn't a {@code CT_GROUP},
     *         or the user who sent the original command, is not a chat member,
     *         or all users to include already are the chat members
     */
    @Assign
    MembersIncluded handle(IncludeMembers c) throws MembersCannotBeIncluded {
        ImmutableList<UserId> newMembers = extractNewMembers(c.getMemberList());
        if (checkInclusionPossibility(c, newMembers)) {
            return MembersIncluded
                    .newBuilder()
                    .setId(c.getId())
                    .setWhoIncludes(c.getWhoIncludes())
                    .addAllMember(newMembers)
                    .vBuild();
        }
        throw MembersCannotBeIncluded
                .newBuilder()
                .setId(c.getId())
                .setWhoIncludes(c.getWhoIncludes())
                .addAllMember(c.getMemberList())
                .build();
    }

    @Apply
    private void event(MembersIncluded e) {
        builder().setId(e.getId())
                 .addAllMember(e.getMemberList());
    }

    private boolean checkInclusionPossibility(IncludeMembers command, List<UserId> newMembers) {
        boolean isGroupChat = state().getType() == CT_GROUP;
        boolean isUserWhoIncludesIsMember = state().getMemberList()
                                                   .contains(command.getWhoIncludes());
        return isGroupChat && isUserWhoIncludesIsMember && !newMembers.isEmpty();
    }

    private ImmutableList<UserId> extractNewMembers(List<UserId> membersInCommand) {
        List<UserId> chatMembers = state().getMemberList();
        ImmutableList<UserId> newMembers =
                membersInCommand.stream()
                                .filter(userId -> !chatMembers.contains(userId))
                                .collect(toImmutableList());
        return newMembers;
    }
}
