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
import io.spine.examples.chatspn.server.ChatsContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.addMembersCommand;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.addMembersCommandWith;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.chat;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.chatAfterAddition;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.chatAfterRemoval;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.chatCardId;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.chatDeleted;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.createDeletedGroupChatIn;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.createGroupChatCommand;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.createGroupChatIn;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.createPersonalChatCommand;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.createPersonalChatIn;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.groupChatCard;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.groupChatCreatedFrom;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.lastMemberLeftChat;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.leaveChat;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.membersAdded;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.membersCannotBeAddedFrom;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.membersCannotBeRemovedFrom;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.membersRemoved;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.personalChatCard;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.personalChatCreatedFrom;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.removeMembersCommandWith;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.userCannotLeaveChat;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.userLeftChat;

@DisplayName("`Chat` should")
final class ChatTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return ChatsContext.newBuilder();
    }

    @Test
    @DisplayName("allow creation as personal and emit the `PersonalChatCreated` event")
    void personalChatCreation() {
        var command = createPersonalChatCommand();
        context().receivesCommand(command);

        var expectedEvent = personalChatCreatedFrom(command);
        var expectedState = chat(command);

        context().assertEvent(expectedEvent);
        context().assertState(command.getId(), expectedState);
    }

    @Test
    @DisplayName("create `ChatCard` projection projections for each member " +
            "after personal chat creation")
    void updateProjectionsAfterPersonalChatCreation() {
        var command = createPersonalChatCommand();
        context().receivesCommand(command);
        var creatorChatCard = personalChatCard(command, command.getCreator());
        var membersChatCard = personalChatCard(command, command.getMember());

        context().assertState(creatorChatCard.getId(), creatorChatCard);
        context().assertState(membersChatCard.getId(), membersChatCard);
    }

    @Test
    @DisplayName("allow creation as group and emit the `GroupChatCreated` event")
    void groupChatCreation() {
        var command = createGroupChatCommand();
        context().receivesCommand(command);

        var expectedEvent = groupChatCreatedFrom(command);
        var expectedState = chat(command);

        context().assertEvent(expectedEvent);
        context().assertState(command.getId(), expectedState);
    }

    @Test
    @DisplayName("create `ChatCard` projections for each member " +
            "after group chat creation")
    void updateProjectionsAfterGroupChatCreation() {
        var chat = createGroupChatIn(context());
        var ownerChatCard = groupChatCard(chat, chat.getOwner());
        var memberChatCard = groupChatCard(chat, chat.getMember(1));

        context().assertState(ownerChatCard.getId(), ownerChatCard);
        context().assertState(memberChatCard.getId(), memberChatCard);
    }

    @Nested
    @DisplayName("handle `RemoveMembers` ")
    class MembersRemovalHandlerBehaviour {

        @Test
        @DisplayName("and emit the `MembersRemoved` if at least one member can be removed")
        void event() {
            var chat = createGroupChatIn(context());
            var randomUser = GivenUserId.generated();
            var chatOwner = chat.getOwner();
            var commonChatMember = chat.getMember(1);
            var membersToRemove = ImmutableList.of(randomUser, chatOwner, commonChatMember);
            var command = removeMembersCommandWith(chat, membersToRemove);
            context().receivesCommand(command);
            var remainingMembers = ImmutableList.of(chatOwner);
            var removedMembers = ImmutableList.of(commonChatMember);
            var expected = membersRemoved(command, remainingMembers, removedMembers);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and change state to expected if at least one member can be removed")
        void state() {
            var chat = createGroupChatIn(context());
            var randomUser = GivenUserId.generated();
            var chatOwner = chat.getOwner();
            var commonChatMember = chat.getMember(1);
            var membersToRemove = ImmutableList.of(randomUser, chatOwner, commonChatMember);
            var command = removeMembersCommandWith(chat, membersToRemove);
            context().receivesCommand(command);
            var remainingMembers = ImmutableList.of(chatOwner);
            var expected = chatAfterRemoval(chat, remainingMembers);

            context().assertState(chat.getId(), expected);
        }

        @Test
        @DisplayName("and delete `ChatCard` projections of removed members")
        void deleteChatCardProjection() {
            var chat = createGroupChatIn(context());
            var membersToRemove = ImmutableList.of(chat.getMember(1));
            var command = removeMembersCommandWith(chat, membersToRemove);
            context().receivesCommand(command);
            var chatCardId = chatCardId(chat.getId(), membersToRemove.get(0));

            context().assertEntity(chatCardId, ChatCardProjection.class)
                     .deletedFlag()
                     .isTrue();
        }

        @Test
        @DisplayName("and reject with the `MembersCannotBeRemoved` " +
                "if the chat is deleted")
        void rejectIfChatDeleted() {
            var chat = createDeletedGroupChatIn(context());
            var command = removeMembersCommandWith(chat, chat.getOwner());
            context().receivesCommand(command);
            var expected = membersCannotBeRemovedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and reject with the `MembersCannotBeRemoved` " +
                "if the user who removes is not a chat owner")
        void rejectIfNotOwner() {
            var chat = createGroupChatIn(context());
            var command = removeMembersCommandWith(chat, chat.getMember(1));
            context().receivesCommand(command);
            var expected = membersCannotBeRemovedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and reject with the `MembersCannotBeRemoved` " +
                "if chat isn't a group")
        void rejectIfNotGroup() {
            var chat = createPersonalChatIn(context());
            var commonChatMember = chat.getMember(1);
            var command = removeMembersCommandWith(chat, commonChatMember);
            context().receivesCommand(command);
            var expected = membersCannotBeRemovedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and reject with the `MembersCannotBeRemoved` " +
                "if all members to remove are not in the chat")
        void rejectIfNoOneToRemove() {
            var chat = createGroupChatIn(context());
            var membersToRemove = ImmutableList.of(GivenUserId.generated());
            var command = removeMembersCommandWith(chat, membersToRemove);
            context().receivesCommand(command);
            var expected = membersCannotBeRemovedFrom(command);

            context().assertEvent(expected);
        }
    }

    @Nested
    @DisplayName("handle `AddMembers` ")
    class MembersAdditionHandlerBehaviour {

        @Test
        @DisplayName("and emit the `MembersAdded` if at least one member can be added")
        void event() {
            var chat = createGroupChatIn(context());
            var membersToAdd = ImmutableList.of(GivenUserId.generated(), chat.getMember(0));
            var command = addMembersCommandWith(chat, membersToAdd);
            context().receivesCommand(command);
            var addedMembers = ImmutableList.of(membersToAdd.get(0));
            var expected = membersAdded(command, chat.getName(), addedMembers);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and change state to expected if at least one member can be added")
        void state() {
            var chat = createGroupChatIn(context());
            var membersToAdd = ImmutableList.of(GivenUserId.generated(), chat.getMember(0));
            var command = addMembersCommandWith(chat, membersToAdd);
            context().receivesCommand(command);
            var addedMembers = ImmutableList.of(membersToAdd.get(0));
            var expected = chatAfterAddition(chat, addedMembers);

            context().assertState(chat.getId(), expected);
        }

        @Test
        @DisplayName("and create the `ChatCard` projection for added members")
        void createChatCardProjection() {
            var chat = createGroupChatIn(context());
            var membersToAdd = ImmutableList.of(GivenUserId.generated());
            var command = addMembersCommandWith(chat, membersToAdd);
            context().receivesCommand(command);
            var chatCard = groupChatCard(chat, membersToAdd.get(0));

            context().assertState(chatCard.getId(), chatCard);
        }

        @Test
        @DisplayName("and reject with the `MembersCannotBeAdded` " +
                "if the chat is deleted")
        void rejectIfChatDeleted() {
            var chat = createDeletedGroupChatIn(context());
            var command = addMembersCommandWith(chat, GivenUserId.generated());
            context().receivesCommand(command);
            var expected = membersCannotBeAddedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and reject with the `MembersCannotBeAdded` " +
                "if the user who adds is not a chat member")
        void rejectIfNotMember() {
            var chat = createGroupChatIn(context());
            var command = addMembersCommandWith(chat, GivenUserId.generated());
            context().receivesCommand(command);
            var expected = membersCannotBeAddedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and reject with the `MembersCannotBeAdded` " +
                "if chat isn't a group")
        void rejectIfNotGroup() {
            var chat = createPersonalChatIn(context());
            var command = addMembersCommand(chat);
            context().receivesCommand(command);
            var expected = membersCannotBeAddedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and reject with the `MembersCannotBeAdded` " +
                "if all members to add already in the chat")
        void rejectIfAlreadyMembers() {
            var chat = createGroupChatIn(context());
            var membersToAdd = ImmutableList.of(chat.getMember(0), chat.getMember(1));
            var command = addMembersCommandWith(chat, membersToAdd);
            context().receivesCommand(command);
            var expected = membersCannotBeAddedFrom(command);

            context().assertEvent(expected);
        }
    }

    @Nested
    @DisplayName("handle `LeaveChat`")
    class ChatLeavingHandlerBehaviour {

        @Test
        @DisplayName("and emit the `UserLeftChat` if the chat is a group and the user is a member")
        void event() {
            var chat = createGroupChatIn(context());
            var command = leaveChat(chat, chat.getMember(1));
            context().receivesCommand(command);
            var expected = userLeftChat(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and change state to the expected")
        void state() {
            var chat = createGroupChatIn(context());
            var command = leaveChat(chat, chat.getMember(1));
            context().receivesCommand(command);
            var expected = chat(chat, command);

            context().assertState(expected.getId(), expected);
        }

        @Test
        @DisplayName("and delete member's `ChatCard` projection after the member leaves")
        void deleteChatCardProjection() {
            var chat = createGroupChatIn(context());
            var user = chat.getMember(1);
            var command = leaveChat(chat, user);
            context().receivesCommand(command);

            var chatCardId = chatCardId(chat.getId(), user);

            context().assertEntity(chatCardId, ChatCardProjection.class)
                     .deletedFlag()
                     .isTrue();
        }

        @Test
        @DisplayName("and additionally delete the chat" +
                " if the last member leaves the chat")
        void lastMemberLeftTheChat() {
            var chat = createGroupChatIn(context());
            var removeMembers = removeMembersCommandWith(chat, ImmutableList.of(chat.getMember(1)));
            context().receivesCommand(removeMembers);
            var command = leaveChat(chat, chat.getMember(0));
            context().receivesCommand(command);
            var lastMemberLeftChat = lastMemberLeftChat(command);
            var chatDeleted = chatDeleted(command);
            var userLeftChat = userLeftChat(command);

            context().assertEvent(userLeftChat);
            context().assertEvent(lastMemberLeftChat);
            context().assertEvent(chatDeleted);
            context().assertEntity(chat.getId(), ChatAggregate.class)
                     .deletedFlag()
                     .isTrue();
        }

        @Test
        @DisplayName("and reject with the `UserCannotLeaveChat` " +
                "if the user is not a member")
        void rejectIfNotMember() {
            var chat = createGroupChatIn(context());
            var command = leaveChat(chat, GivenUserId.generated());
            context().receivesCommand(command);
            var expected = userCannotLeaveChat(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and reject with the `UserCannotLeaveChat` " +
                "if the chat is not a group")
        void rejectIfNotGroup() {
            var chat = createPersonalChatIn(context());
            var command = leaveChat(chat, chat.getMember(0));
            context().receivesCommand(command);
            var expected = userCannotLeaveChat(command);

            context().assertEvent(expected);
        }
    }
}
