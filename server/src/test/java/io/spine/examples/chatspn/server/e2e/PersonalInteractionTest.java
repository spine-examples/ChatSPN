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

package io.spine.examples.chatspn.server.e2e;

import io.spine.examples.chatspn.account.UserChats;
import io.spine.examples.chatspn.account.UserProfile;
import io.spine.examples.chatspn.chat.ChatPreview;
import io.spine.examples.chatspn.message.MessageView;
import io.spine.examples.chatspn.server.e2e.TestUser.Conversation;
import io.spine.examples.chatspn.server.e2e.TestUser.Observer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static io.spine.examples.chatspn.server.ExpectedOnlyAssertions.assertExpectedFields;
import static io.spine.examples.chatspn.server.e2e.given.PersonalInteractionTestEnv.chatMessages;
import static io.spine.examples.chatspn.server.e2e.given.PersonalInteractionTestEnv.userChats;

final class PersonalInteractionTest extends ServerRunningTest {

    @Test
    @DisplayName("Users should be able to create account, personal chat, send messages, " +
            "edit messages, and delete a chat.")
    void messageInteractionInPersonalChat() {
        // Vlad and Artem passes registration.
        TestUser vlad = new TestUser(createClient());
        TestUser artem = new TestUser(createClient());

        // Vlad opens the app. `UserChats` are loaded and the observer is set.
        UserChats vladReadChats = vlad.readChats();
        Observer<UserChats> vladChatsObserver = vlad.observeChats();

        // Vlad finds Artem and creates a personal chat.
        UserProfile artemProfile = vlad.findUserBy(artem.email());
        assertThat(artemProfile).isEqualTo(artem.profile());
        Conversation conversation = vlad.createPersonalChatWith(artem);

        // Vlad opens a chat with Artem.
        // Chat messages are loaded and an observer of their changes is set.
        //
        ChatPreview vladChatView = vladChatsObserver.lastState()
                                                    .getChat(0);
        List<MessageView> vladReadMessages = vlad.readMessagesIn(vladChatView.getId());
        Observer<MessageView> vladMessageObserver = vlad.observeMessagesIn(vladChatView.getId());

        // Vlad sends messages.
        vlad.sendMessageTo(vladChatView.getId());
        vlad.sendMessageTo(vladChatView.getId());
        vlad.sendMessageTo(vladChatView.getId());

        // Artem opens the app. `UserChats` loaded and the observer is set.
        UserChats artemReadChats = artem.readChats();
        Observer<UserChats> artemChatsObserver = artem.observeChats();

        // Artem opens chat with Vlad.
        // Chat messages are loaded and an observer of their changes is set.
        //
        ChatPreview artemChatView = artemReadChats.getChat(0);
        List<MessageView> artemReadMessages = artem.readMessagesIn(artemChatView.getId());
        Observer<MessageView> artemMessageObserver = artem.observeMessagesIn(artemChatView.getId());

        // Artem sends messages.
        artem.sendMessageTo(artemChatView.getId());
        artem.sendMessageTo(artemChatView.getId());

        // Vlad edits the first message.
        List<MessageView> vladMessages = chatMessages(vladReadMessages, vladMessageObserver);
        vlad.editMessage(vladMessages.get(0));

        // Checking that the messages for both Artem and Vlad are expected and equal.
        assertExpectedFields(chatMessages(artemReadMessages, artemMessageObserver),
                             conversation.messages());
        assertExpectedFields(chatMessages(vladReadMessages, vladMessageObserver),
                             conversation.messages());

        // Vlad deletes the chat. Chat disappears for both Artem and Vlad.
        vlad.deleteChat(vladChatView.getId());
        assertExpectedFields(vladChatsObserver.lastState(), userChats(vlad.userId()));
        assertExpectedFields(artemChatsObserver.lastState(), userChats(artem.userId()));
    }
}
