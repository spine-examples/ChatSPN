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

import com.google.common.collect.ImmutableList;
import com.google.common.truth.Truth;
import io.spine.examples.chatspn.account.UserChats;
import io.spine.examples.chatspn.account.UserProfile;
import io.spine.examples.chatspn.chat.ChatPreview;
import io.spine.examples.chatspn.message.MessageView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static io.spine.examples.chatspn.server.e2e.given.PersonalChatTestEnv.chatPreview;
import static io.spine.examples.chatspn.server.e2e.given.PersonalChatTestEnv.createPersonalChat;
import static io.spine.examples.chatspn.server.e2e.given.PersonalChatTestEnv.createRandomAccount;
import static io.spine.examples.chatspn.server.e2e.given.PersonalChatTestEnv.deleteChat;
import static io.spine.examples.chatspn.server.e2e.given.PersonalChatTestEnv.editMessage;
import static io.spine.examples.chatspn.server.e2e.given.PersonalChatTestEnv.emptyChatPreview;
import static io.spine.examples.chatspn.server.e2e.given.PersonalChatTestEnv.findChatPreview;
import static io.spine.examples.chatspn.server.e2e.given.PersonalChatTestEnv.findMessageView;
import static io.spine.examples.chatspn.server.e2e.given.PersonalChatTestEnv.findProfile;
import static io.spine.examples.chatspn.server.e2e.given.PersonalChatTestEnv.findUserChats;
import static io.spine.examples.chatspn.server.e2e.given.PersonalChatTestEnv.readMessagesInChat;
import static io.spine.examples.chatspn.server.e2e.given.PersonalChatTestEnv.removeMessage;
import static io.spine.examples.chatspn.server.e2e.given.PersonalChatTestEnv.sendMessage;
import static io.spine.examples.chatspn.server.e2e.given.PersonalChatTestEnv.userChats;

final class InteractionWithMessagesInPersonalChatTest extends ClientAwareTest {

    @Test
    @DisplayName("Users should be able to create account, personal chat, send messages, " +
            "edit messages, remove messages, and delete a chat.")
    void messageInteractionInPersonalChat() {
        // Accounts creation.
        UserProfile vladExpectedProfile = createRandomAccount(client());
        UserProfile artemExpectedProfile = createRandomAccount(client());

        UserProfile vlad = findProfile(vladExpectedProfile.getEmail(), client());
        UserProfile artem = findProfile(artemExpectedProfile.getEmail(), client());

        assertThat(vlad).comparingExpectedFieldsOnly()
                        .isEqualTo(vladExpectedProfile);
        assertThat(artem).comparingExpectedFieldsOnly()
                         .isEqualTo(artemExpectedProfile);

        // Chat creation.
        ChatPreview expectedChatPreview = createPersonalChat(vlad.getId(), artem.getId(), client());

        ChatPreview chatPreview = findChatPreview(expectedChatPreview.getId(), client());
        UserChats vladChats = findUserChats(vlad.getId(), client());
        UserChats artemChats = findUserChats(artem.getId(), client());

        assertThat(chatPreview).isEqualTo(expectedChatPreview);
        assertThat(vladChats).isEqualTo(userChats(vlad.getId(), expectedChatPreview));
        assertThat(artemChats).isEqualTo(userChats(artem.getId(), expectedChatPreview));

        // Messages sending.
        MessageView expectedFirstMessage = sendMessage(chatPreview.getId(), vlad.getId(), client());
        MessageView expectedLastMessage = sendMessage(chatPreview.getId(), artem.getId(), client());
        expectedChatPreview = chatPreview(chatPreview, expectedLastMessage);

        MessageView firstMessage = findMessageView(expectedFirstMessage.getId(), client());
        MessageView lastMessage = findMessageView(expectedLastMessage.getId(), client());
        chatPreview = findChatPreview(chatPreview.getId(), client());

        assertThat(firstMessage).comparingExpectedFieldsOnly()
                                .isEqualTo(expectedFirstMessage);
        assertThat(lastMessage).comparingExpectedFieldsOnly()
                               .isEqualTo(expectedLastMessage);
        assertThat(chatPreview).comparingExpectedFieldsOnly()
                               .isEqualTo(expectedChatPreview);

        // Messages editing.
        expectedLastMessage = editMessage(lastMessage, client());
        expectedFirstMessage = editMessage(firstMessage, client());
        expectedChatPreview = chatPreview(chatPreview, expectedLastMessage);

        MessageView editedFirstMessage = findMessageView(expectedFirstMessage.getId(), client());
        MessageView editedLastMessage = findMessageView(expectedLastMessage.getId(), client());
        chatPreview = findChatPreview(chatPreview.getId(), client());

        assertThat(editedFirstMessage).isEqualTo(expectedFirstMessage);
        assertThat(editedLastMessage).isEqualTo(expectedLastMessage);
        assertThat(chatPreview).isEqualTo(expectedChatPreview);

        // Messages removal.
        removeMessage(lastMessage, client());
        removeMessage(firstMessage, client());
        expectedChatPreview = emptyChatPreview(chatPreview);

        chatPreview = findChatPreview(chatPreview.getId(), client());
        ImmutableList<MessageView> chatMessages = readMessagesInChat(chatPreview.getId(), client());

        assertThat(chatPreview).isEqualTo(expectedChatPreview);
        Truth.assertThat(chatMessages.size())
             .isEqualTo(0);

        // Chat deletion.
        deleteChat(chatPreview.getId(), vlad.getId(), client());
        UserChats vladExpectedChats = userChats(vlad.getId());
        UserChats artemExpectedChats = userChats(artem.getId());

        vladChats = findUserChats(vlad.getId(), client());
        artemChats = findUserChats(artem.getId(), client());

        assertThat(vladChats).isEqualTo(vladExpectedChats);
        assertThat(artemChats).isEqualTo(artemExpectedChats);
    }
}
