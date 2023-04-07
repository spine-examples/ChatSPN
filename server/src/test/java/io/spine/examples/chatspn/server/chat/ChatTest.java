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
import io.spine.examples.chatspn.chat.Chat;
import io.spine.examples.chatspn.chat.command.AddMembers;
import io.spine.examples.chatspn.chat.command.CreateGroupChat;
import io.spine.examples.chatspn.chat.command.CreatePersonalChat;
import io.spine.examples.chatspn.chat.command.DeleteChat;
import io.spine.examples.chatspn.chat.command.RemoveMembers;
import io.spine.examples.chatspn.chat.event.ChatDeleted;
import io.spine.examples.chatspn.chat.event.GroupChatCreated;
import io.spine.examples.chatspn.chat.event.MembersAdded;
import io.spine.examples.chatspn.chat.event.MembersRemoved;
import io.spine.examples.chatspn.chat.event.PersonalChatCreated;
import io.spine.examples.chatspn.chat.rejection.Rejections.ChatCannotBeDeleted;
import io.spine.examples.chatspn.chat.rejection.Rejections.MembersCannotBeAdded;
import io.spine.examples.chatspn.chat.rejection.Rejections.MembersCannotBeRemoved;
import io.spine.examples.chatspn.server.ChatsContext;
import io.spine.examples.chatspn.server.chat.given.ChatTestEnv;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.addMembersCommand;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.addMembersCommandWith;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.chatAfterAddition;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.chatAfterRemoval;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.chatCannotBeDeletedFrom;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.chatDeletedFrom;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.chatFrom;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.createGroupChatCommand;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.createGroupChatIn;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.createPersonalChatCommand;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.createPersonalChatIn;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.deleteChatCommand;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.groupChatCreatedFrom;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.membersAddedFrom;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.membersCannotBeAddedFrom;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.membersCannotBeRemovedFrom;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.membersRemovedFrom;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.personalChatCreatedFrom;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.removeMembersCommandWith;

@DisplayName("`Chat` should")
final class ChatTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return ChatsContext.newBuilder();
    }

    @Test
    @DisplayName("allow creation as personal and emit the `PersonalChatCreated` event")
    void personalChatCreation() {
        CreatePersonalChat command = createPersonalChatCommand();
        context().receivesCommand(command);

        PersonalChatCreated expectedEvent = personalChatCreatedFrom(command);
        Chat expectedState = chatFrom(command);

        context().assertEvent(expectedEvent);
        context().assertState(command.getId(), expectedState);
    }

    @Test
    @DisplayName("allow creation as group and emit the `GroupChatCreated` event")
    void groupChatCreation() {
        CreateGroupChat command = createGroupChatCommand();
        context().receivesCommand(command);

        GroupChatCreated expectedEvent = groupChatCreatedFrom(command);
        Chat expectedState = chatFrom(command);

        context().assertEvent(expectedEvent);
        context().assertState(command.getId(), expectedState);
    }

    @Nested
    @DisplayName("handle `RemoveMembers` ")
    class MembersRemovalHandlerBehaviour {

        @Test
        @DisplayName("and emit the `MembersRemoved` if at least one member can be removed")
        void event() {
            Chat chat = createGroupChatIn(context());
            UserId randomUser = GivenUserId.generated();
            UserId chatOwner = chat.getOwner();
            UserId commonChatMember = chat.getMember(1);
            ImmutableList<UserId> membersToRemove =
                    ImmutableList.of(randomUser, chatOwner, commonChatMember);
            RemoveMembers command = removeMembersCommandWith(chat, membersToRemove);
            context().receivesCommand(command);
            ImmutableList<UserId> remainingMembers = ImmutableList.of(chatOwner);
            MembersRemoved expected = membersRemovedFrom(command, remainingMembers);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and change state to expected if at least one member can be removed")
        void state() {
            Chat chat = createGroupChatIn(context());
            UserId randomUser = GivenUserId.generated();
            UserId chatOwner = chat.getOwner();
            UserId commonChatMember = chat.getMember(1);
            ImmutableList<UserId> membersToRemove =
                    ImmutableList.of(randomUser, chatOwner, commonChatMember);
            RemoveMembers command = removeMembersCommandWith(chat, membersToRemove);
            context().receivesCommand(command);
            ImmutableList<UserId> remainingMembers = ImmutableList.of(chatOwner);
            Chat expected = chatAfterRemoval(chat, remainingMembers);

            context().assertState(chat.getId(), expected);
        }

        @Test
        @DisplayName("and reject with the `MembersCannotBeRemoved` " +
                "if the user who removes is not a chat owner")
        void rejectIfNotOwner() {
            Chat chat = createGroupChatIn(context());
            RemoveMembers command = removeMembersCommandWith(chat, chat.getMember(1));
            context().receivesCommand(command);
            MembersCannotBeRemoved expected = membersCannotBeRemovedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and reject with the `MembersCannotBeRemoved` " +
                "if chat isn't a group")
        void rejectIfNotGroup() {
            Chat chat = createPersonalChatIn(context());
            UserId commonChatMember = chat.getMember(1);
            RemoveMembers command = removeMembersCommandWith(chat, commonChatMember);
            context().receivesCommand(command);
            MembersCannotBeRemoved expected = membersCannotBeRemovedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and reject with the `MembersCannotBeRemoved` " +
                "if all members to remove are not in the chat")
        void rejectIfNoOneToRemove() {
            Chat chat = createGroupChatIn(context());
            ImmutableList<UserId> membersToRemove =
                    ImmutableList.of(GivenUserId.generated());
            RemoveMembers command = removeMembersCommandWith(chat, membersToRemove);
            context().receivesCommand(command);
            MembersCannotBeRemoved expected = membersCannotBeRemovedFrom(command);

            context().assertEvent(expected);
        }
    }

    @Nested
    @DisplayName("handle `AddMembers` ")
    class MembersAdditionHandlerBehaviour {

        @Test
        @DisplayName("and emit the `MembersAdded` if at least one member can be added")
        void event() {
            Chat chat = createGroupChatIn(context());
            ImmutableList<UserId> membersToAdd =
                    ImmutableList.of(GivenUserId.generated(), chat.getMember(0));
            AddMembers command = ChatTestEnv.addMembersCommandWith(chat, membersToAdd);
            context().receivesCommand(command);
            ImmutableList<UserId> addedMembers =
                    ImmutableList.of(membersToAdd.get(0));
            MembersAdded expected = membersAddedFrom(command, addedMembers);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and change state to expected if at least one member can be added")
        void state() {
            Chat chat = createGroupChatIn(context());
            ImmutableList<UserId> membersToAdd =
                    ImmutableList.of(GivenUserId.generated(), chat.getMember(0));
            AddMembers command = ChatTestEnv.addMembersCommandWith(chat, membersToAdd);
            context().receivesCommand(command);
            ImmutableList<UserId> addedMembers =
                    ImmutableList.of(membersToAdd.get(0));
            Chat expected = chatAfterAddition(chat, addedMembers);

            context().assertState(chat.getId(), expected);
        }

        @Test
        @DisplayName("and reject with the `MembersCannotBeAdded` " +
                "if the user who adds is not a chat member")
        void rejectIfNotMember() {
            Chat chat = createGroupChatIn(context());
            AddMembers command = addMembersCommandWith(chat, GivenUserId.generated());
            context().receivesCommand(command);
            MembersCannotBeAdded expected = membersCannotBeAddedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and reject with the `MembersCannotBeAdded` " +
                "if chat isn't a group")
        void rejectIfNotGroup() {
            Chat chat = createPersonalChatIn(context());
            AddMembers command = addMembersCommand(chat);
            context().receivesCommand(command);
            MembersCannotBeAdded expected = membersCannotBeAddedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and reject with the `MembersCannotBeAdded` " +
                "if all members to add already in the chat")
        void rejectIfAlreadyMembers() {
            Chat chat = createGroupChatIn(context());
            ImmutableList<UserId> membersToAdd =
                    ImmutableList.of(chat.getMember(0), chat.getMember(1));
            AddMembers command = ChatTestEnv.addMembersCommandWith(chat, membersToAdd);
            context().receivesCommand(command);
            MembersCannotBeAdded expected = membersCannotBeAddedFrom(command);

            context().assertEvent(expected);
        }
    }

    @Nested
    @DisplayName("handle `DeleteChat` ")
    class ChatDeletionBehaviour {

        @Test
        @DisplayName("and emit the `ChatDeleted` if chat can be deleted")
        void event() {
            Chat chat = createGroupChatIn(context());
            DeleteChat command = deleteChatCommand(chat, chat.getOwner());
            context().receivesCommand(command);
            ChatDeleted expected = chatDeletedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and change state to expected if chat can be deleted")
        void state() {
            Chat chat = createPersonalChatIn(context());
            DeleteChat command = deleteChatCommand(chat, chat.getMember(1));
            context().receivesCommand(command);

            context().assertEntity(command.getId(), ChatAggregate.class)
                     .deletedFlag()
                     .isTrue();
        }

        @Test
        @DisplayName("and reject with the `ChatConnotBeDeleted` " +
                "if chat is personal and the user who deletes is not a member")
        void rejectIfNotMemberInPersonal() {
            Chat chat = createPersonalChatIn(context());
            DeleteChat command = deleteChatCommand(chat, GivenUserId.generated());
            context().receivesCommand(command);
            ChatCannotBeDeleted expected = chatCannotBeDeletedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and reject with the `ChatConnotBeDeleted` " +
                "if chat is group and the user who deletes is not an owner")
        void rejectIfNotOwnerInGroup() {
            Chat chat = createGroupChatIn(context());
            DeleteChat command = deleteChatCommand(chat, chat.getMember(1));
            context().receivesCommand(command);
            ChatCannotBeDeleted expected = chatCannotBeDeletedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and reject with the `MembersCannotBeAdded` " +
                "if chat is already deleted")
        void rejectIfAlreadyDeleted() {
            Chat chat = createGroupChatIn(context());
            DeleteChat command = deleteChatCommand(chat, chat.getOwner());
            context().receivesCommand(command);
            context().receivesCommand(command);
            ChatCannotBeDeleted expected = chatCannotBeDeletedFrom(command);

            context().assertEvent(expected);
        }
    }
}
