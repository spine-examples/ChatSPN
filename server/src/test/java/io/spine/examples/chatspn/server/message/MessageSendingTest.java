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

import io.spine.examples.chatspn.chat.Chat;
import io.spine.examples.chatspn.message.Message;
import io.spine.examples.chatspn.message.event.MessageSent;
import io.spine.examples.chatspn.message.rejection.SendingRejections.MessageCannotBeSent;
import io.spine.examples.chatspn.server.ChatsContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.chatspn.server.given.MessageTestEnv.createRandomChat;
import static io.spine.examples.chatspn.server.given.MessageTestEnv.sendMessage;

@DisplayName("`MessageSending` should")
public final class MessageSendingTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return ChatsContext.newBuilder();
    }

    @Test
    @DisplayName("emit `MessageSent` event")
    void event() {
        Chat chat = createRandomChat(context());
        Message message = sendMessage(chat.getId(),
                                      chat.getMember(0),
                                      context());
        MessageSent expected = MessageSent
                .newBuilder()
                .setId(message.getId())
                .setChat(message.getChat())
                .setUser(message.getUser())
                .setContent(message.getContent())
                .vBuild();

        context().assertEvents()
                 .withType(MessageSent.class)
                 .hasSize(1);
        context().assertEvent(expected);
    }

    @Test
    @DisplayName("produce a `Message` with the expected state")
    void state() {
        Chat chat = createRandomChat(context());
        Message message = sendMessage(chat.getId(),
                                      chat.getMember(0),
                                      context());
        context().assertState(message.getId(), Message.class)
                 .comparingExpectedFieldsOnly()
                 .isEqualTo(message);
    }

    @Test
    @DisplayName("reject when the message sender is not the chat member")
    void rejection() {
        Chat chat = createRandomChat(context());
        Message message = sendMessage(chat.getId(),
                                      GivenUserId.generated(),
                                      context());

        MessageCannotBeSent expected = MessageCannotBeSent
                .newBuilder()
                .setId(message.getId())
                .setChat(message.getChat())
                .setUser(message.getUser())
                .setContent(message.getContent())
                .vBuild();

        context().assertEvent(expected);
    }

    @Test
    @DisplayName("archive itself once the message is sent")
    void archiving() {
        Chat chat = createRandomChat(context());
        Message message = sendMessage(chat.getId(),
                                      chat.getMember(0),
                                      context());
        context().assertEntity(message.getId(), MessageSendingProcess.class)
                 .archivedFlag()
                 .isTrue();
    }
}
