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
import io.spine.examples.chatspn.chat.command.CreateGroupChat;
import io.spine.examples.chatspn.chat.command.CreatePersonalChat;
import io.spine.examples.chatspn.chat.command.RemoveMembers;
import io.spine.examples.chatspn.chat.event.GroupChatCreated;
import io.spine.examples.chatspn.chat.event.MembersRemoved;
import io.spine.examples.chatspn.chat.event.PersonalChatCreated;
import io.spine.examples.chatspn.chat.rejection.Rejections.MembersCannotBeRemoved;
import io.spine.examples.chatspn.server.ChatsContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.chatFrom;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.createGroupChatCommand;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.createGroupChatIn;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.createPersonalChatCommand;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.createPersonalChatIn;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.groupChatCreatedFrom;
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
            ImmutableList<UserId> membersToRemove =
                    ImmutableList.of(GivenUserId.generated(),
                                     chat.getMember(0),
                                     chat.getMember(1));
            RemoveMembers command = removeMembersCommandWith(chat, membersToRemove);
            context().receivesCommand(command);
            ImmutableList<UserId> remainingMembers =
                    ImmutableList.of(chat.getMember(0));
            MembersRemoved expected = membersRemovedFrom(command, remainingMembers);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and change state to expected if at least one member can be removed")
        void state() {
            Chat chat = createGroupChatIn(context());
            ImmutableList<UserId> membersToRemove =
                    ImmutableList.of(GivenUserId.generated(),
                                     chat.getMember(0),
                                     chat.getMember(1));
            RemoveMembers command = removeMembersCommandWith(chat, membersToRemove);
            context().receivesCommand(command);
            ImmutableList<UserId> remainingMembers =
                    ImmutableList.of(chat.getMember(0));
            Chat expected = chatFrom(chat, remainingMembers);

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
            RemoveMembers command = removeMembersCommandWith(chat, chat.getMember(0));
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
}
