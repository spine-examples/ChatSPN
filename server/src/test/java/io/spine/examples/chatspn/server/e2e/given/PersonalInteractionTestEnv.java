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

package io.spine.examples.chatspn.server.e2e.given;

import com.google.common.collect.ImmutableList;
import io.spine.core.UserId;
import io.spine.examples.chatspn.MessageId;
import io.spine.examples.chatspn.account.UserChats;
import io.spine.examples.chatspn.chat.ChatPreview;
import io.spine.examples.chatspn.chat.MessagePreview;
import io.spine.examples.chatspn.message.MessageView;
import io.spine.examples.chatspn.server.e2e.TestUser.Observer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PersonalInteractionTestEnv {

    /**
     * Prevents class instantiation.
     */
    private PersonalInteractionTestEnv() {
    }

    public static UserChats userChats(UserId userId, ChatPreview... chats) {
        UserChats userChats = UserChats
                .newBuilder()
                .setId(userId)
                .addAllChat(ImmutableList.copyOf(chats))
                .vBuild();
        return userChats;
    }

    public static ChatPreview chatPreview(ChatPreview chat, MessageView message) {
        MessagePreview messagePreview = MessagePreview
                .newBuilder()
                .setId(message.getId())
                .setUser(message.getUser())
                .setContent(message.getContent())
                .setWhenPosted(message.getWhenPosted())
                .buildPartial();
        ChatPreview chatPreview = chat
                .toBuilder()
                .setLastMessage(messagePreview)
                .vBuild();
        return chatPreview;
    }

    public static List<MessageView> chatMessages(List<MessageView> loadedMessages,
                                                 Observer<MessageView> observer) {
        Map<MessageId, MessageView> messages = new LinkedHashMap<>();
        loadedMessages.forEach(message -> messages.put(message.getId(), message));
        observer.allStates()
                .forEach(message -> messages.put(message.getId(), message));
        return ImmutableList.copyOf(messages.values());
    }
}
