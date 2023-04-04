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

package io.spine.examples.chatspn.server.chat.given;

import com.google.common.collect.ImmutableList;
import io.spine.core.UserId;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.chat.Chat;
import io.spine.examples.chatspn.chat.command.CreateGroupChat;
import io.spine.examples.chatspn.chat.command.CreatePersonalChat;
import io.spine.examples.chatspn.chat.command.RemoveMembers;
import io.spine.examples.chatspn.chat.event.GroupChatCreated;
import io.spine.examples.chatspn.chat.event.MembersRemoved;
import io.spine.examples.chatspn.chat.event.PersonalChatCreated;
import io.spine.examples.chatspn.chat.rejection.Rejections.MembersCannotBeRemoved;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.BlackBoxContext;

import java.util.List;

import static io.spine.examples.chatspn.chat.Chat.ChatType.CT_GROUP;
import static io.spine.examples.chatspn.chat.Chat.ChatType.CT_PERSONAL;
import static io.spine.testing.TestValues.randomString;

public final class ChatTestEnv {

    /**
     * Prevents class instantiation.
     */
    private ChatTestEnv() {
    }

    public static CreatePersonalChat createPersonalChatCommand() {
        CreatePersonalChat command = CreatePersonalChat
                .newBuilder()
                .setId(ChatId.generate())
                .setCreator(GivenUserId.generated())
                .setMember(GivenUserId.generated())
                .vBuild();
        return command;
    }

    public static PersonalChatCreated personalChatCreatedFrom(CreatePersonalChat c) {
        PersonalChatCreated event = PersonalChatCreated
                .newBuilder()
                .setId(c.getId())
                .setCreator(c.getCreator())
                .setMember(c.getMember())
                .vBuild();
        return event;
    }

    public static Chat chatFrom(CreatePersonalChat c) {
        Chat state = Chat
                .newBuilder()
                .setId(c.getId())
                .setType(CT_PERSONAL)
                .addMember(c.getCreator())
                .addMember(c.getMember())
                .vBuild();
        return state;
    }

    public static CreateGroupChat createGroupChatCommand() {
        CreateGroupChat command = CreateGroupChat
                .newBuilder()
                .setId(ChatId.generate())
                .setCreator(GivenUserId.generated())
                .addMember(GivenUserId.generated())
                .addMember(GivenUserId.generated())
                .setName(randomString())
                .vBuild();
        return command;
    }

    public static GroupChatCreated groupChatCreatedFrom(CreateGroupChat c) {
        GroupChatCreated event = GroupChatCreated
                .newBuilder()
                .setId(c.getId())
                .setCreator(c.getCreator())
                .addAllMember(c.getMemberList())
                .setName(c.getName())
                .vBuild();
        return event;
    }

    public static Chat chatFrom(CreateGroupChat c) {
        Chat state = Chat
                .newBuilder()
                .setId(c.getId())
                .setType(CT_GROUP)
                .addMember(c.getCreator())
                .addAllMember(c.getMemberList())
                .setName(c.getName())
                .setOwner(c.getCreator())
                .vBuild();
        return state;
    }

    public static Chat createGroupChatIn(BlackBoxContext ctx) {
        UserId owner = GivenUserId.generated();
        Chat chat = Chat
                .newBuilder()
                .setId(ChatId.generate())
                .setName(randomString())
                .setType(CT_GROUP)
                .setOwner(owner)
                .addMember(owner)
                .addMember(GivenUserId.generated())
                .vBuild();
        CreateGroupChat command = CreateGroupChat
                .newBuilder()
                .setId(chat.getId())
                .setName(chat.getName())
                .setCreator(chat.getMember(0))
                .addMember(chat.getMember(1))
                .vBuild();
        ctx.receivesCommand(command);
        return chat;
    }

    public static Chat createPersonalChatIn(BlackBoxContext ctx) {
        Chat chat = Chat
                .newBuilder()
                .setId(ChatId.generate())
                .setType(CT_PERSONAL)
                .addMember(GivenUserId.generated())
                .addMember(GivenUserId.generated())
                .vBuild();
        CreatePersonalChat command = CreatePersonalChat
                .newBuilder()
                .setId(chat.getId())
                .setCreator(chat.getMember(0))
                .setMember(chat.getMember(1))
                .vBuild();
        ctx.receivesCommand(command);
        return chat;
    }

    public static RemoveMembers removeMembersCommandWith(Chat chat, UserId whoRemoves) {
        RemoveMembers command = RemoveMembers
                .newBuilder()
                .setId(chat.getId())
                .setWhoRemoves(whoRemoves)
                .addAllMember(ImmutableList.of(chat.getMember(1)))
                .vBuild();
        return command;
    }

    public static RemoveMembers removeMembersCommandWith(Chat chat,
                                                         List<UserId> membersToRemove) {
        RemoveMembers command = RemoveMembers
                .newBuilder()
                .setId(chat.getId())
                .setWhoRemoves(chat.getMember(0))
                .addAllMember(membersToRemove)
                .vBuild();
        return command;
    }

    public static MembersRemoved membersRemovedFrom(RemoveMembers command,
                                                    List<UserId> remainingMembers) {
        MembersRemoved event = MembersRemoved
                .newBuilder()
                .setId(command.getId())
                .setWhoRemoved(command.getWhoRemoves())
                .addAllRemainingMember(remainingMembers)
                .vBuild();
        return event;
    }

    public static Chat chatFrom(Chat chat, List<UserId> remainingMembers) {
        Chat state = Chat
                .newBuilder()
                .setId(chat.getId())
                .setType(chat.getType())
                .addAllMember(remainingMembers)
                .setName(chat.getName())
                .setOwner(chat.getOwner())
                .vBuild();
        return state;
    }

    public static MembersCannotBeRemoved membersCannotBeRemovedFrom(RemoveMembers command) {
        MembersCannotBeRemoved rejection = MembersCannotBeRemoved
                .newBuilder()
                .setId(command.getId())
                .setWhoRemoves(command.getWhoRemoves())
                .addAllMember(command.getMemberList())
                .vBuild();
        return rejection;
    }
}
