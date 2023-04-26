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

import io.spine.examples.chatspn.ChatId;
import io.spine.examples.chatspn.account.UserChats;
import io.spine.examples.chatspn.account.UserProfile;
import io.spine.examples.chatspn.chat.ChatPreview;
import io.spine.examples.chatspn.message.MessageView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.spine.examples.chatspn.server.ExpectedOnlyAssertions.assertExpectedFieldsEqual;
import static io.spine.examples.chatspn.server.e2e.given.PersonalChatTestEnv.chatPreview;
import static io.spine.examples.chatspn.server.e2e.given.PersonalChatTestEnv.userChats;

final class InteractionWithMessagesInPersonalChatTest extends ServerRunningTest {

    @Test
    @DisplayName("Users should be able to create account, personal chat, send messages, " +
            "edit messages, remove messages, and delete a chat.")
    void messageInteractionInPersonalChat() throws InterruptedException {
        // Vlad and Artem passed registration.
        TestUser vlad = new TestUser();
        TestUser artem = new TestUser();

        // Vlad finds Artem, creates personal chat, and sees it preview in `UserChats`.
        UserProfile artemProfile = vlad.findUserBy(artem.email());
        assertExpectedFieldsEqual(artemProfile, artem.profile());
        ChatPreview expectedChat = vlad.createPersonalChatWith(artemProfile.getId());
        ChatPreview chatVladView = vlad.readChats()
                                       .getChat(0);
        assertExpectedFieldsEqual(chatVladView, expectedChat);

        // Vlad sends messages and sees them in chat.
        List<MessageView> expectedMessages = new ArrayList<>();
        expectedMessages.add(vlad.sendMessageTo(chatVladView.getId()));
        expectedMessages.add(vlad.sendMessageTo(chatVladView.getId()));
        expectedMessages.add(vlad.sendMessageTo(chatVladView.getId()));
        assertMessagesInChatEquality(vlad, chatVladView.getId(), expectedMessages);

        // The last message will be shown in the chat preview.
        expectedChat = chatPreview(expectedChat, expectedMessages.get(2));
        chatVladView = vlad.readChats()
                           .getChat(0);
        assertExpectedFieldsEqual(chatVladView, expectedChat);

        // Artem reads messages in the chat.
        ChatPreview chatArtemView = artem.readChats()
                                         .getChat(0);
        assertExpectedFieldsEqual(chatArtemView, expectedChat);
        assertMessagesInChatEquality(artem, chatArtemView.getId(), expectedMessages);

        // Artem sends a message to the chat. Both Vlad and Artem will see the new message.
        expectedMessages.add(artem.sendMessageTo(chatArtemView.getId()));
        assertMessagesInChatEquality(vlad, chatVladView.getId(), expectedMessages);
        assertMessagesInChatEquality(artem, chatArtemView.getId(), expectedMessages);

        // Artem edits the last message. Both Vlad and Artem will see changes.
        MessageView editedMessage = artem.editMessage(expectedMessages.get(3));
        expectedMessages.set(3, editedMessage);
        assertMessagesInChatEquality(vlad, chatVladView.getId(), expectedMessages);
        assertMessagesInChatEquality(artem, chatArtemView.getId(), expectedMessages);

        // Vlad removes the first message. Both Vlad and Artem will see changes.
        vlad.removeMessage(expectedMessages.get(0));
        expectedMessages.remove(0);
        assertMessagesInChatEquality(vlad, chatVladView.getId(), expectedMessages);
        assertMessagesInChatEquality(artem, chatArtemView.getId(), expectedMessages);

        // Artem deletes the chat. Chat will disappear from both `UserChats`.
        artem.deleteChat(chatArtemView.getId());
        UserChats expectedVladChats = userChats(vlad.userId());
        UserChats expectedArtemChats = userChats(artem.userId());
        assertExpectedFieldsEqual(vlad.readChats(), expectedVladChats);
        assertExpectedFieldsEqual(artem.readChats(), expectedArtemChats);

        // Vlad and Artem close their connections to the server.
        vlad.closeConnection();
        artem.closeConnection();
    }

    private static void assertMessagesInChatEquality(TestUser user, ChatId chat,
                                                     List<MessageView> expectedMessages) {
        List<MessageView> messagesUserView = user.readMessagesIn(chat);
        assertExpectedFieldsEqual(messagesUserView, expectedMessages);
    }
}
