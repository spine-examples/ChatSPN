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
import io.spine.examples.chatspn.chat.command.IncludeMembers;
import io.spine.examples.chatspn.chat.event.GroupChatCreated;
import io.spine.examples.chatspn.chat.event.MembersIncluded;
import io.spine.examples.chatspn.chat.event.PersonalChatCreated;
import io.spine.examples.chatspn.chat.rejection.Rejections.MembersCannotBeIncluded;
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
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.includeMembersCommand;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.includeMembersCommandWith;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.membersCannotBeIncludedFrom;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.membersIncludedFrom;
import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.personalChatCreatedFrom;

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
    @DisplayName("handle `IncludeMembers` ")
    class MembersInclusionHandlerBehaviour {

        @Test
        @DisplayName("and emit the `MembersIncluded` if at least one member can be included")
        void event() {
            Chat chat = createGroupChatIn(context());
            ImmutableList<UserId> membersToInclude =
                    ImmutableList.of(GivenUserId.generated(), chat.getMember(0));
            IncludeMembers command = includeMembersCommandWith(chat, membersToInclude);
            context().receivesCommand(command);
            ImmutableList<UserId> includedMembers =
                    ImmutableList.of(membersToInclude.get(0));
            MembersIncluded expected = membersIncludedFrom(command, includedMembers);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and change state to expected if at least one member can be included")
        void state() {
            Chat chat = createGroupChatIn(context());
            ImmutableList<UserId> membersToInclude =
                    ImmutableList.of(GivenUserId.generated(), chat.getMember(0));
            IncludeMembers command = includeMembersCommandWith(chat, membersToInclude);
            context().receivesCommand(command);
            ImmutableList<UserId> includedMembers =
                    ImmutableList.of(membersToInclude.get(0));
            Chat expected = chatFrom(chat, includedMembers);

            context().assertState(chat.getId(), expected);
        }

        @Test
        @DisplayName("and reject with the `MembersCannotBeIncluded` " +
                "if the user who includes is not a chat member")
        void rejectIfNotMember() {
            Chat chat = createGroupChatIn(context());
            IncludeMembers command = includeMembersCommandWith(chat, GivenUserId.generated());
            context().receivesCommand(command);
            MembersCannotBeIncluded expected = membersCannotBeIncludedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and reject with the `MembersCannotBeIncluded` " +
                "if chat type isn't a `CT_GROUP`")
        void rejectIfNotGroup() {
            Chat chat = createPersonalChatIn(context());
            IncludeMembers command = includeMembersCommand(chat);
            context().receivesCommand(command);
            MembersCannotBeIncluded expected = membersCannotBeIncludedFrom(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("and reject with the `MembersCannotBeIncluded` " +
                "if all members to include already in the chat")
        void rejectIfAlreadyMembers() {
            Chat chat = createGroupChatIn(context());
            ImmutableList<UserId> membersToInclude =
                    ImmutableList.of(chat.getMember(0), chat.getMember(1));
            IncludeMembers command = includeMembersCommandWith(chat, membersToInclude);
            context().receivesCommand(command);
            MembersCannotBeIncluded expected = membersCannotBeIncludedFrom(command);

            context().assertEvent(expected);
        }
    }
}
