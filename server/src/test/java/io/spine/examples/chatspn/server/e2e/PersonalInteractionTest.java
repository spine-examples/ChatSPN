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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.chatspn.server.e2e.TestUser.registerUser;

/**
 * End-to-end test that describes such a scenario:
 *
 * <ol>
 *     <li>Two users registered accounts.</li>
 *     <li>First user finds the second and creates a personal chat.</li>
 *     <li>First user sends messages to the chat.</li>
 *     <li>Second user opens the chat and sends own messages.</li>
 *     <li>Second user edits the message.</li>
 *     <li>First user removes message and deletes the chat.</li>
 * </ol>
 */
final class PersonalInteractionTest extends ServerRunningTest {

    @Test
    @DisplayName("Users should be able to create account, personal chat, send messages, " +
            "edit messages, and delete a chat.")
    void messageInteractionInPersonalChat() {
        // Vlad and Artem passes registration.
        var vlad = registerUser("Vlad", "vlad@teamdev.com", createClient());
        var artem = registerUser("Artem", "artem@teamdev.com", createClient());

        // Vlad finds Artem and creates a personal chat.
        var artemProfile = vlad.findUserBy(artem.email());
        var vladConversation = vlad.createPersonalChatWith(artemProfile);

        // Vlad sends messages.
        vladConversation.sendMessage("Hello");
        vladConversation.sendMessage("How are you");
        vladConversation.sendMessage("?");

        // Artem opens the chat and sends messages.
        var artemChats = artem.chats();
        var artemConversation = artem.openChat(artemChats.get(0)
                                                         .getChatId());
        artemConversation.sendMessage("Hi!");
        artemConversation.sendMessage("I'm fine");
        artemConversation.sendMessage("And you");

        // Artem edits the last message.
        var artemMessages = artemConversation.messages();
        artemConversation.editMessage(artemMessages.get(artemMessages.size() - 1), "And you?");

        // Vlad removes the 3rd message in the chat.
        var vladMessages = vladConversation.messages();
        vladConversation.removeMessage(vladMessages.get(2));

        // Vlad deletes the chat.
        vladConversation.deleteChat();
    }
}
