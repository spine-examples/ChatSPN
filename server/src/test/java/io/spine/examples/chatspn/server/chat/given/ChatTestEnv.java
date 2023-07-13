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
import io.spine.examples.chatspn.ChatCardId;
import io.spine.examples.chatspn.ChatDeletionId;
import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.chat.Chat;
import io.spine.examples.chatspn.chat.ChatCard;
import io.spine.examples.chatspn.chat.ChatMember;
import io.spine.examples.chatspn.chat.command.AddMembers;
import io.spine.examples.chatspn.chat.command.CreateGroupChat;
import io.spine.examples.chatspn.chat.command.CreatePersonalChat;
import io.spine.examples.chatspn.chat.command.DeleteChat;
import io.spine.examples.chatspn.chat.command.LeaveChat;
import io.spine.examples.chatspn.chat.command.RemoveMembers;
import io.spine.examples.chatspn.chat.event.ChatDeleted;
import io.spine.examples.chatspn.chat.event.GroupChatCreated;
import io.spine.examples.chatspn.chat.event.LastMemberLeftChat;
import io.spine.examples.chatspn.chat.event.MembersAdded;
import io.spine.examples.chatspn.chat.event.MembersRemoved;
import io.spine.examples.chatspn.chat.event.PersonalChatCreated;
import io.spine.examples.chatspn.chat.event.UserLeftChat;
import io.spine.examples.chatspn.chat.rejection.Rejections.MembersCannotBeAdded;
import io.spine.examples.chatspn.chat.rejection.Rejections.MembersCannotBeRemoved;
import io.spine.examples.chatspn.chat.rejection.Rejections.UserCannotLeaveChat;
import io.spine.testing.server.blackbox.BlackBoxContext;

import java.util.List;

import static io.spine.examples.chatspn.chat.Chat.ChatType.CT_GROUP;
import static io.spine.examples.chatspn.chat.Chat.ChatType.CT_PERSONAL;
import static io.spine.examples.chatspn.server.chat.given.ChatDeletionTestEnv.chatDeletionId;
import static io.spine.examples.chatspn.server.chat.given.GivenChatMember.chatMember;
import static java.util.stream.Collectors.toList;

public final class ChatTestEnv {

    /**
     * Prevents class instantiation.
     */
    private ChatTestEnv() {
    }

    public static CreatePersonalChat createPersonalChatCommand() {
        var command = CreatePersonalChat
                .newBuilder()
                .setId(ChatId.generate())
                .setCreator(chatMember("John Doe"))
                .setMember(chatMember("Emma Smith"))
                .vBuild();
        return command;
    }

    public static PersonalChatCreated personalChatCreatedFrom(CreatePersonalChat c) {
        var event = PersonalChatCreated
                .newBuilder()
                .setId(c.getId())
                .setCreator(c.getCreator())
                .setMember(c.getMember())
                .vBuild();
        return event;
    }

    public static Chat chat(CreatePersonalChat c) {
        var state = Chat
                .newBuilder()
                .setId(c.getId())
                .setType(CT_PERSONAL)
                .addMember(c.getCreator())
                .addMember(c.getMember())
                .vBuild();
        return state;
    }

    public static CreateGroupChat createGroupChatCommand() {
        var command = CreateGroupChat
                .newBuilder()
                .setId(ChatId.generate())
                .setCreator(chatMember("John Doe"))
                .addMember(chatMember("Emma Smith"))
                .addMember(chatMember("Giordano Bruno"))
                .setName("Group chat name")
                .vBuild();
        return command;
    }

    public static GroupChatCreated groupChatCreatedFrom(CreateGroupChat c) {
        var event = GroupChatCreated
                .newBuilder()
                .setId(c.getId())
                .setCreator(c.getCreator())
                .addAllMember(c.getMemberList())
                .setName(c.getName())
                .vBuild();
        return event;
    }

    public static Chat chat(CreateGroupChat c) {
        var state = Chat
                .newBuilder()
                .setId(c.getId())
                .setType(CT_GROUP)
                .addMember(c.getCreator())
                .addAllMember(c.getMemberList())
                .setName(c.getName())
                .setOwner(c.getCreator()
                           .getId())
                .vBuild();
        return state;
    }

    public static Chat createGroupChatIn(BlackBoxContext ctx) {
        var owner = chatMember("John Doe");
        var chat = Chat
                .newBuilder()
                .setId(ChatId.generate())
                .setName("Awesome group chat name")
                .setType(CT_GROUP)
                .setOwner(owner.getId())
                .addMember(owner)
                .addMember(chatMember("Emma Smith"))
                .vBuild();
        var command = CreateGroupChat
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
        var chat = Chat
                .newBuilder()
                .setId(ChatId.generate())
                .setType(CT_PERSONAL)
                .addMember(chatMember("Emma Smith"))
                .addMember(chatMember("John Doe"))
                .vBuild();
        var command = CreatePersonalChat
                .newBuilder()
                .setId(chat.getId())
                .setCreator(chat.getMember(0))
                .setMember(chat.getMember(1))
                .vBuild();
        ctx.receivesCommand(command);
        return chat;
    }

    public static Chat createDeletedGroupChatIn(BlackBoxContext ctx) {
        var chat = createGroupChatIn(ctx);
        var deleteChat = DeleteChat
                .newBuilder()
                .setId(chatDeletionId(chat))
                .setWhoDeletes(chat.getOwner())
                .vBuild();
        ctx.receivesCommand(deleteChat);
        return chat;
    }

    public static AddMembers addMembersCommand(Chat chat) {
        var command = AddMembers
                .newBuilder()
                .setId(chat.getId())
                .setWhoAdds(chat.getMember(0)
                                .getId())
                .addMember(chatMember("Giordano Bruno"))
                .vBuild();
        return command;
    }

    public static AddMembers addMembersCommandWith(Chat chat,
                                                   List<ChatMember> membersToAdd) {
        var command = AddMembers
                .newBuilder()
                .setId(chat.getId())
                .setWhoAdds(chat.getMember(0)
                                .getId())
                .addAllMember(membersToAdd)
                .vBuild();
        return command;
    }

    public static AddMembers addMembersCommandWith(Chat chat,
                                                   UserId whoAdds) {
        var command = AddMembers
                .newBuilder()
                .setId(chat.getId())
                .setWhoAdds(whoAdds)
                .addMember(chatMember("Giordano Bruno"))
                .vBuild();
        return command;
    }

    public static MembersAdded membersAdded(AddMembers c,
                                            Chat chat,
                                            List<ChatMember> addedMembers) {
        var event = MembersAdded
                .newBuilder()
                .setId(c.getId())
                .setChatName(chat.getName())
                .setWhoAdded(c.getWhoAdds())
                .addAllNewMember(addedMembers)
                .addAllOldMember(chat.getMemberList())
                .vBuild();
        return event;
    }

    public static MembersCannotBeAdded membersCannotBeAddedFrom(AddMembers c) {
        var rejection = MembersCannotBeAdded
                .newBuilder()
                .setId(c.getId())
                .setWhoAdds(c.getWhoAdds())
                .addAllSuggestedMember(c.getMemberList())
                .vBuild();
        return rejection;
    }

    public static Chat chatAfterAddition(Chat chat, List<ChatMember> addedMembers) {
        var state = Chat
                .newBuilder()
                .setId(chat.getId())
                .setType(chat.getType())
                .setName(chat.getName())
                .addAllMember(chat.getMemberList())
                .addAllMember(addedMembers)
                .vBuild();
        return state;
    }

    public static RemoveMembers removeMembersCommandWith(Chat chat, UserId whoRemoves) {
        var command = RemoveMembers
                .newBuilder()
                .setId(chat.getId())
                .setWhoRemoves(whoRemoves)
                .addAllMember(ImmutableList.of(chat.getMember(1)))
                .vBuild();
        return command;
    }

    public static RemoveMembers removeMembersCommandWith(Chat chat,
                                                         List<ChatMember> membersToRemove) {
        var command = RemoveMembers
                .newBuilder()
                .setId(chat.getId())
                .setWhoRemoves(chat.getMember(0)
                                   .getId())
                .addAllMember(membersToRemove)
                .vBuild();
        return command;
    }

    public static MembersRemoved membersRemoved(RemoveMembers command,
                                                List<ChatMember> remainingMembers,
                                                List<ChatMember> removedMembers) {
        var event = MembersRemoved
                .newBuilder()
                .setId(command.getId())
                .setWhoRemoved(command.getWhoRemoves())
                .addAllRemainingMember(remainingMembers)
                .addAllRemovedMember(removedMembers)
                .vBuild();
        return event;
    }

    public static Chat chatAfterRemoval(Chat chat, List<ChatMember> remainingMembers) {
        var state = Chat
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
        var rejection = MembersCannotBeRemoved
                .newBuilder()
                .setId(command.getId())
                .setWhoRemoves(command.getWhoRemoves())
                .addAllMember(command.getMemberList())
                .vBuild();
        return rejection;
    }

    public static LeaveChat leaveChat(Chat chat, ChatMember user) {
        var command = LeaveChat
                .newBuilder()
                .setChat(chat.getId())
                .setUser(user)
                .vBuild();
        return command;
    }

    public static UserLeftChat userLeftChat(LeaveChat c) {
        var event = UserLeftChat
                .newBuilder()
                .setChat(c.getChat())
                .setUser(c.getUser())
                .vBuild();
        return event;
    }

    public static LastMemberLeftChat lastMemberLeftChat(LeaveChat c) {
        var event = LastMemberLeftChat
                .newBuilder()
                .setId(c.getChat())
                .setLastMember(c.getUser())
                .vBuild();
        return event;
    }

    public static ChatDeleted chatDeleted(LeaveChat c) {
        var deletionId = ChatDeletionId
                .newBuilder()
                .setId(c.getChat())
                .vBuild();
        var event = ChatDeleted
                .newBuilder()
                .setId(deletionId)
                .setWhoDeleted(c.getUser()
                                .getId())
                .vBuild();
        return event;
    }

    public static UserCannotLeaveChat userCannotLeaveChat(LeaveChat c) {
        var rejection = UserCannotLeaveChat
                .newBuilder()
                .setChat(c.getChat())
                .setUser(c.getUser())
                .vBuild();
        return rejection;
    }

    public static Chat chat(Chat chat, LeaveChat c) {
        var newMemberList = chat
                .getMemberList()
                .stream()
                .filter(member -> !member.equals(c.getUser()))
                .collect(toList());
        var state = chat
                .toBuilder()
                .clearMember()
                .addAllMember(newMemberList)
                .vBuild();
        return state;
    }

    public static ChatCardId chatCardId(ChatId chat, UserId user) {
        return ChatCardId
                .newBuilder()
                .setChat(chat)
                .setUser(user)
                .vBuild();
    }

    public static ChatCard groupChatCard(Chat chat, ChatMember viewer) {
        return groupChatCard(chat, viewer.getId());
    }

    public static ChatCard groupChatCard(Chat chat, UserId viewerId) {
        var chatCardId = chatCardId(chat.getId(), viewerId);
        var chatCard = ChatCard
                .newBuilder()
                .setCardId(chatCardId)
                .setChatId(chat.getId())
                .setViewer(viewerId)
                .addAllMember(chat.getMemberList())
                .setGroupChatName(chat.getName())
                .setType(CT_GROUP)
                .vBuild();
        return chatCard;
    }

    public static ChatCard personalChatCard(CreatePersonalChat command, ChatMember viewer) {
        var chatCardId = chatCardId(command.getId(), viewer.getId());
        var chatCard = ChatCard
                .newBuilder()
                .setCardId(chatCardId)
                .setChatId(command.getId())
                .setViewer(viewer.getId())
                .addMember(command.getCreator())
                .addMember(command.getMember())
                .setType(CT_PERSONAL)
                .vBuild();
        return chatCard;
    }
}
