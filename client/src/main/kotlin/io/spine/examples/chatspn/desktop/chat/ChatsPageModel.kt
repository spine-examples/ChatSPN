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

package io.spine.examples.chatspn.desktop.chat

import com.google.protobuf.Timestamp
import io.spine.examples.chatspn.ChatId
import io.spine.examples.chatspn.MessageId
import io.spine.examples.chatspn.account.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * UI Model for the `ChatsPage`.
 *
 * UI Model stores data that may be displayed by `@Composable` functions and updated by client.
 */
public class ChatsPageModel {
    private var authorizedUser: UserProfile? = null
    private val chatPreviewsState = MutableStateFlow<ChatList>(listOf())
    private val chatMessagesStateMap: MutableMap<ChatId, MutableMessagesState> = mutableMapOf()

    /**
     * Returns the state of the user's chats.
     *
     * It may be used as state to recompose component on changes.
     */
    public fun chats(): StateFlow<ChatList> {
        return chatPreviewsState
    }

    /**
     * Returns the state of messages in the chat.
     *
     * It may be used as state to recompose component on changes.
     */
    public fun messages(chat: ChatId): MessagesState {
        if (!chatMessagesStateMap.containsKey(chat)) {
            throw IllegalStateException("Chat not found")
        }
        return chatMessagesStateMap[chat]!!
    }

    /**
     * Returns profile of the currently authorized user.
     */
    public fun authorizedUser(): UserProfile {
        return authorizedUser!!
    }

    /**
     * Adds the profile of the authorized user.
     */
    public fun authorizedUser(user: UserProfile) {
        authorizedUser = user
    }

    /**
     * Updates the model by new chats.
     */
    public fun updateChats(chats: ChatList) {
        chatPreviewsState.value = chats
    }

    /**
     * Updates the model by new messages.
     */
    public fun updateMessages(chat: ChatId, messages: MessageList) {
        if (chatMessagesStateMap.containsKey(chat)) {
            chatMessagesStateMap[chat]!!.value = messages
        } else {
            chatMessagesStateMap[chat] = MutableStateFlow(messages)
        }
    }
}

/**
 * Data for the chat preview.
 */
public data class ChatData(
    val id: ChatId,
    val name: String,
    val lastMessage: MessageData?
)

/**
 * Data for the single message view.
 */
public data class MessageData(
    val id: MessageId,
    val sender: UserProfile,
    val content: String,
    val whenPosted: Timestamp
)

/**
 * List of `ChatData`.
 */
public typealias ChatList = List<ChatData>

/**
 * List of `MessageData`.
 */
public typealias MessageList = List<MessageData>

/**
 * Mutable state of messages in the chat.
 */
private typealias MutableMessagesState = MutableStateFlow<MessageList>

/**
 * Immutable state of messages in the chat.
 */
public typealias MessagesState = StateFlow<MessageList>
