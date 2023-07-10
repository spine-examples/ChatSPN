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
import io.spine.examples.chatspn.chat.command.AddMembers;
import io.spine.examples.chatspn.chat.command.CreateGroupChat;
import io.spine.examples.chatspn.chat.command.CreatePersonalChat;
import io.spine.examples.chatspn.chat.command.LeaveChat;
import io.spine.examples.chatspn.chat.command.MarkChatAsDeleted;
import io.spine.examples.chatspn.chat.command.RemoveMembers;
import io.spine.examples.chatspn.chat.event.ChatMarkedAsDeleted;
import io.spine.examples.chatspn.chat.event.GroupChatCreated;
import io.spine.examples.chatspn.chat.event.LastMemberLeftChat;
import io.spine.examples.chatspn.chat.event.MembersAdded;
import io.spine.examples.chatspn.chat.event.MembersRemoved;
import io.spine.examples.chatspn.chat.event.PersonalChatCreated;
import io.spine.examples.chatspn.chat.event.UserLeftChat;
import io.spine.examples.chatspn.chat.rejection.ChatCannotBeMarkedAsDeleted;
import io.spine.examples.chatspn.chat.rejection.MembersCannotBeAdded;
import io.spine.examples.chatspn.chat.rejection.MembersCannotBeRemoved;
import io.spine.examples.chatspn.chat.rejection.UserCannotLeaveChat;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;
import io.spine.server.tuple.Pair;

import java.util.List;
import java.util.Optional;

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
                .setCreatorName(c.getCreatorName())
                .setMember(c.getMember())
                .setMemberName(c.getMemberName())
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
     * Handles the command to remove members from the chat.
     *
     * <p>The member who sent the command cannot be removed.</p>
     *
     * @return {@link MembersRemoved} if at least one member was removed
     * @throws MembersCannotBeRemoved
     *         if chat is deleted,
     *         or chat isn't a group,
     *         or the user who sent the original command, was not a chat owner,
     *         or all users to remove already aren't the chat members
     */
    @Assign
    MembersRemoved handle(RemoveMembers c) throws MembersCannotBeRemoved {
        var remainingMembers = extractRemainingMembers(c);
        var membersToRemove = extractMembersToRemove(c);
        if (checkRemovalPossibility(c, remainingMembers)) {
            return MembersRemoved
                    .newBuilder()
                    .setId(c.getId())
                    .setWhoRemoved(c.getWhoRemoves())
                    .addAllRemainingMember(remainingMembers)
                    .addAllRemovedMember(membersToRemove)
                    .vBuild();
        }
        throw MembersCannotBeRemoved
                .newBuilder()
                .setId(c.getId())
                .setWhoRemoves(c.getWhoRemoves())
                .addAllMember(c.getMemberList())
                .build();
    }

    @Apply
    private void event(MembersRemoved e) {
        builder().clearMember()
                 .addAllMember(e.getRemainingMemberList());
    }

    /**
     * Checks the possibility to remove members by those criteria:
     * <ul>
     *     <li>chat isn't deleted;</li>
     *     <li>chat is a group;</li>
     *     <li>the user who sent the command was a chat owner;</li>
     *     <li>at least one user from the command can be removed.</li>
     * </ul>
     */
    private boolean checkRemovalPossibility(RemoveMembers command,
                                            List<UserId> remainingMembers) {
        if (isDeleted()) {
            return false;
        }
        var isGroupChat = state().getType() == CT_GROUP;
        var isUserWhoRemovesIsOwner = state()
                .getOwner()
                .equals(command.getWhoRemoves());
        var isSomeoneRemoved = remainingMembers.size() < state().getMemberCount();
        return isGroupChat && isUserWhoRemovesIsOwner && isSomeoneRemoved;
    }

    /**
     * Extracts the list of users who will remain after members removal.
     */
    private ImmutableList<UserId> extractRemainingMembers(RemoveMembers command) {
        var chatMembers = state().getMemberList();
        var membersInCommand = command.getMemberList();
        var remainingMembers = chatMembers
                .stream()
                .filter(userId -> !membersInCommand.contains(userId) ||
                        userId.equals(command.getWhoRemoves()))
                .collect(toImmutableList());
        return remainingMembers;
    }

    /**
     * Extracts the list of users who are members of the chat and can be removed.
     */
    private ImmutableList<UserId> extractMembersToRemove(RemoveMembers command) {
        var chatMembers = state().getMemberList();
        var membersInCommand = command.getMemberList();
        var membersToRemove = membersInCommand
                .stream()
                .filter(userId -> chatMembers.contains(userId) &&
                        !userId.equals(command.getWhoRemoves()))
                .collect(toImmutableList());
        return membersToRemove;
    }

    /**
     * Handles the command to add new members to the chat.
     *
     * @throws MembersCannotBeAdded
     *         if chat is deleted,
     *         or chat isn't a group,
     *         or the user who sent the original command, was not a chat member,
     *         or all users to add already are the chat members
     */
    @Assign
    MembersAdded handle(AddMembers c) throws MembersCannotBeAdded {
        var newMembers = extractNewMembers(c.getMemberList());
        if (checkAdditionPossibility(c, newMembers)) {
            return MembersAdded
                    .newBuilder()
                    .setId(c.getId())
                    .setChatName(state().getName())
                    .setWhoAdded(c.getWhoAdds())
                    .addAllMember(newMembers)
                    .vBuild();
        }
        throw MembersCannotBeAdded
                .newBuilder()
                .setId(c.getId())
                .setWhoAdds(c.getWhoAdds())
                .addAllSuggestedMember(c.getMemberList())
                .build();
    }

    @Apply
    private void event(MembersAdded e) {
        builder().addAllMember(e.getMemberList());
    }

    /**
     * Checks the possibility to add new members by those criteria:
     * <ul>
     *     <li>chat isn't deleted;</li>
     *     <li>chat is a group;</li>
     *     <li>the user who sent the command was a chat member;</li>
     *     <li>at least one user from the command can be added.</li>
     * </ul>
     */
    private boolean checkAdditionPossibility(AddMembers command, List<UserId> newMembers) {
        if (isDeleted()) {
            return false;
        }
        var isGroupChat = state().getType() == CT_GROUP;
        var isUserWhoAddsIsMember = state()
                .getMemberList()
                .contains(command.getWhoAdds());
        return isGroupChat && isUserWhoAddsIsMember && !newMembers.isEmpty();
    }

    /**
     * Extracts the list of users who are not members of the chat.
     */
    private ImmutableList<UserId> extractNewMembers(List<UserId> membersInCommand) {
        var chatMembers = state().getMemberList();
        var newMembers = membersInCommand
                .stream()
                .filter(userId -> !chatMembers.contains(userId))
                .collect(toImmutableList());
        return newMembers;
    }

    /**
     * Handles the command to delete the chat.
     *
     * @throws ChatCannotBeMarkedAsDeleted
     *         if the user who told to delete personal chat wasn't a chat member,
     *         or the user who told to delete group chat wasn't a chat owner or the last member,
     *         or the chat has already been deleted.
     */
    @Assign
    ChatMarkedAsDeleted handle(MarkChatAsDeleted c) throws ChatCannotBeMarkedAsDeleted {
        if (checkDeletionPossibility(c)) {
            return ChatMarkedAsDeleted
                    .newBuilder()
                    .setId(c.getId())
                    .setWhoDeleted(c.getWhoDeletes())
                    .addAllMember(state().getMemberList())
                    .vBuild();
        }
        throw ChatCannotBeMarkedAsDeleted
                .newBuilder()
                .setId(c.getId())
                .setWhoDeletes(c.getWhoDeletes())
                .build();
    }

    @Apply
    private void event(ChatMarkedAsDeleted e) {
        setDeleted(true);
    }

    /**
     * Checks the possibility to delete the chat by those criteria:
     * <ul>
     *     <li>chat isn't already deleted;</li>
     *     <li>if chat is a personal, user who send the command is a chat member;</li>
     *     <li>if chat is a group, user who send the command was a chat owner.</li>
     * </ul>
     *
     * <p>Empty chat always can be deleted.
     */
    private boolean checkDeletionPossibility(MarkChatAsDeleted c) {
        if (isDeleted()) {
            return false;
        }
        if (state().getMemberList()
                   .isEmpty()) {
            return true;
        }
        var isPersonalChat = state().getType() == CT_PERSONAL;
        var isMember = state()
                .getMemberList()
                .contains(c.getWhoDeletes());
        if (isPersonalChat && isMember) {
            return true;
        }
        var isGroupChat = state().getType() == CT_GROUP;
        var isOwner = c
                .getWhoDeletes()
                .equals(state().getOwner());
        return isGroupChat && isOwner;
    }

    /**
     * Handles the command to leave the chat.
     *
     * <p>If the last member left the chat its deletion will be requested
     *
     * @throws UserCannotLeaveChat
     *         if chat is deleted,
     *         or chat isn't a group,
     *         or user is already not a chat member.
     */
    @Assign
    Pair<UserLeftChat, Optional<LastMemberLeftChat>> handle(LeaveChat c)
            throws UserCannotLeaveChat {
        checkLeavingPossibility(c);
        var userLeftChat = userLeftChat(c);
        Optional<LastMemberLeftChat> lastMemberLeftChat = Optional.empty();
        if (state().getMemberList()
                   .size() == 1) {
            lastMemberLeftChat = Optional.of(lastMemberLeftChat(c));
        }
        return Pair.withOptional(userLeftChat, lastMemberLeftChat);
    }

    @Apply
    private void event(LastMemberLeftChat e) {
    }

    @Apply
    private void event(UserLeftChat e) {
        var userIndex = builder()
                .getMemberList()
                .indexOf(e.getUser());
        builder().removeMember(userIndex);
    }

    /**
     * Checks the possibility to leave the chat by those criteria:
     * <ul>
     *     <li>chat isn't deleted;</li>
     *     <li>chat is a group;</li>
     *     <li>the user who sent the command is a chat member.</li>
     * </ul>
     */
    private void checkLeavingPossibility(LeaveChat c) throws UserCannotLeaveChat {
        var isGroupChat = state().getType() == CT_GROUP;
        var isMember = state()
                .getMemberList()
                .contains(c.getUser());
        var canLeave = !isDeleted() && isGroupChat && isMember;
        if (!canLeave) {
            throw UserCannotLeaveChat
                    .newBuilder()
                    .setChat(c.getChat())
                    .setUser(c.getUser())
                    .build();
        }
    }

    private static LastMemberLeftChat lastMemberLeftChat(LeaveChat c) {
        return LastMemberLeftChat
                .newBuilder()
                .setId(c.getChat())
                .setLastMember(c.getUser())
                .vBuild();
    }

    private static UserLeftChat userLeftChat(LeaveChat c) {
        return UserLeftChat
                .newBuilder()
                .setChat(c.getChat())
                .setUser(c.getUser())
                .vBuild();
    }
}
