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

package io.spine.examples.chatspn.server.message;

import io.spine.examples.chatspn.message.Message;
import io.spine.examples.chatspn.message.MessageView;
import io.spine.examples.chatspn.server.ChatsContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.chatspn.server.chat.given.ChatTestEnv.createDeletedGroupChatIn;
import static io.spine.examples.chatspn.server.message.given.MessageSendingTestEnv.chatCard;
import static io.spine.examples.chatspn.server.message.given.MessageSendingTestEnv.messageCannotBeSentFrom;
import static io.spine.examples.chatspn.server.message.given.MessageSendingTestEnv.messageFrom;
import static io.spine.examples.chatspn.server.message.given.MessageSendingTestEnv.messagePostedFrom;
import static io.spine.examples.chatspn.server.message.given.MessageSendingTestEnv.messageSentFrom;
import static io.spine.examples.chatspn.server.message.given.MessageSendingTestEnv.messageViewFrom;
import static io.spine.examples.chatspn.server.message.given.MessageSendingTestEnv.randomSendMessageCommand;
import static io.spine.examples.chatspn.server.message.given.MessageSendingTestEnv.sendMessageCommandWith;
import static io.spine.examples.chatspn.server.message.given.MessageTestEnv.createRandomChatIn;

@DisplayName("`MessageSending` should")
public final class MessageSendingTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return ChatsContext.newBuilder();
    }

    @Test
    @DisplayName("emit `MessageSent` event, and archive itself")
    void event() {
        var chat = createRandomChatIn(context());
        var command = randomSendMessageCommand(chat);
        context().receivesCommand(command);
        var expected = messageSentFrom(command);

        context().assertEvent(expected);
        context().assertEntity(expected.getId(), MessageSendingProcess.class)
                 .archivedFlag()
                 .isTrue();
    }

    @Test
    @DisplayName("reject when the message sender is not the chat member")
    void senderNotMember() {
        var chat = createRandomChatIn(context());
        var command = sendMessageCommandWith(chat, GivenUserId.generated());
        context().receivesCommand(command);
        var expected = messageCannotBeSentFrom(command);

        context().assertEvent(expected);
    }

    @Test
    @DisplayName("reject with the `MessageCannotBeSent` " +
            "if the chat does not exist or has been deleted")
    void chatNotExist() {
        var chat = createDeletedGroupChatIn(context());
        var command = sendMessageCommandWith(chat, chat.getOwner());
        context().receivesCommand(command);
        var expected = messageCannotBeSentFrom(command);

        context().assertEvent(expected);
    }

    @Test
    @DisplayName("produce `MessageView` with the expected state")
    void messageViewState() {
        var chat = createRandomChatIn(context());
        var command = randomSendMessageCommand(chat);
        context().receivesCommand(command);
        var expected = messageViewFrom(command);

        context().assertState(expected.getId(), MessageView.class)
                 .comparingExpectedFieldsOnly()
                 .isEqualTo(expected);
    }

    @Test
    @DisplayName("update the last message in each member's `ChatCard` projections")
    void updateLastMessage() {
        var chat = createRandomChatIn(context());
        var command = randomSendMessageCommand(chat);
        context().receivesCommand(command);
        var ownerChatCard = chatCard(chat, command, chat.getOwner());
        var memberChatCard = chatCard(chat, command, chat.getMember(1)
                                                         .getId());

        context().assertState(ownerChatCard.getCardId(), ownerChatCard);
        context().assertState(memberChatCard.getCardId(), memberChatCard);
    }

    @Nested
    @DisplayName("lead `MessageAggregate` to")
    class MessageAggregateBehaviour {

        @Test
        @DisplayName("produce a `Message` with the expected state")
        void state() {
            var chat = createRandomChatIn(context());
            var command = randomSendMessageCommand(chat);
            context().receivesCommand(command);
            var state = messageFrom(command);

            context().assertState(state.getId(), Message.class)
                     .comparingExpectedFieldsOnly()
                     .isEqualTo(state);
        }

        @Test
        @DisplayName("emission of the `MessagePosted` event")
        void event() {
            var chat = createRandomChatIn(context());
            var command = randomSendMessageCommand(chat);
            context().receivesCommand(command);
            var expected = messagePostedFrom(command);

            context().assertEvent(expected);
        }
    }
}
