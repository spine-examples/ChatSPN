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
import io.spine.core.UserId
import io.spine.examples.chatspn.ChatId
import io.spine.examples.chatspn.MessageId
import io.spine.examples.chatspn.account.UserProfile
import io.spine.examples.chatspn.chat.ChatPreview
import io.spine.examples.chatspn.chat.MessagePreview
import io.spine.examples.chatspn.desktop.client.ClientFacade.Companion.client
import io.spine.examples.chatspn.message.MessageView
import java.util.stream.Collectors
import java.util.stream.Collectors.toList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * UI Model for the `ChatsPage`.
 *
 * UI Model stores data that may be displayed by `@Composable` functions and updated by client.
 */
public class ChatsPageModel(public val authorizedUser: UserProfile) {
    private val selectedChatState = MutableStateFlow<ChatId>(ChatId.getDefaultInstance())
    private val chatPreviewsState = MutableStateFlow<ChatList>(listOf())
    private val chatMessagesStateMap: MutableMap<ChatId, MutableMessagesState> = mutableMapOf()

    init {
        updateChats(client.readChats().toChatDataList())
        client.subscribeOnChats { state -> updateChats(state.chatList.toChatDataList()) }
    }

    /**
     * Returns the state of the user's chats.
     */
    public fun chats(): StateFlow<ChatList> {
        return chatPreviewsState
    }

    /**
     * Returns the state of messages in the chat.
     */
    public fun messages(chat: ChatId): MessagesState {
        if (!chatMessagesStateMap.containsKey(chat)) {
            throw IllegalStateException("Chat not found")
        }
        return chatMessagesStateMap[chat]!!
    }

    public fun selectedChat(): StateFlow<ChatId> {
        return selectedChatState
    }

    public fun selectChat(chat: ChatId) {
        selectedChatState.value = chat
        updateMessages(chat, client.readMessages(chat).toMessageDataList())
        client.clearMessagesSubscriptions()
        client.subscribeOnMessages(chat,
            { messageView ->
                val message = messageView.toMessageData()
                val chatMessages = chatMessagesStateMap[chat]!!.value
                if (chatMessages.contains(message)) {
                    val messageIndex = chatMessages.indexOf(message)
                    val newChatMessages = chatMessages.subList(0, messageIndex) +
                            message +
                            chatMessages.subList(messageIndex + 1, chatMessages.size)
                    updateMessages(chat, newChatMessages)
                } else {
                    updateMessages(chat, chatMessages + message)
                }
            },
            { messageDeleted ->
                val chatMessages = chatMessagesStateMap[chat]!!.value
                val deletedMessages = chatMessages
                    .stream()
                    .filter { message -> message.id.equals(messageDeleted.id) }
                    .collect(toList())
                if (!deletedMessages.isEmpty()) {
                    val messageIndex = chatMessages.indexOf(deletedMessages[0])
                    val newChatMessages = chatMessages.subList(0, messageIndex) +
                            chatMessages.subList(messageIndex + 1, chatMessages.size)
                    updateMessages(chat, newChatMessages)
                }
            })
    }

    /**
     * Updates the model with new chats.
     */
    public fun updateChats(chats: ChatList) {
        chatPreviewsState.value = chats
    }

    /**
     * Updates the model with new messages.
     */
    private fun updateMessages(chat: ChatId, messages: MessageList) {
        if (chatMessagesStateMap.containsKey(chat)) {
            chatMessagesStateMap[chat]!!.value = messages
        } else {
            chatMessagesStateMap[chat] = MutableStateFlow(messages)
        }
    }
}

private fun List<ChatPreview>.toChatDataList(): ChatList {
    return this.stream().map { chatPreview ->
        val lastMessage: MessageData?
        if (chatPreview.lastMessage.equals(MessagePreview.getDefaultInstance())) {
            lastMessage = null
        } else {
            lastMessage = chatPreview.lastMessage.toMessageData();
        }
        ChatData(
            chatPreview.id,
            chatPreview.name(),
            lastMessage
        )
    }.collect(Collectors.toList())
}

private fun List<MessageView>.toMessageDataList(): MessageList {
    val users = mutableMapOf<UserId, UserProfile>();
    val messages = this.stream().map { message ->
        val user: UserProfile
        if (users.containsKey(message.user)) {
            user = users[message.user]!!
        } else {
            user = client.findUser(message.user)!!
            users[message.user] = user
        }
        MessageData(
            message.id,
            user,
            message.content,
            message.whenPosted
        )
    }.collect(Collectors.toList())
    return messages
}

private fun MessagePreview.toMessageData(): MessageData {
    return MessageData(
        this.id,
        client.findUser(this.user)!!,
        this.content,
        this.whenPosted
    )
}

private fun MessageView.toMessageData(): MessageData {
    return MessageData(
        this.id,
        client.findUser(this.user)!!,
        this.content,
        this.whenPosted
    )
}

private fun ChatPreview.name(): String {
    if (this.hasGroupChat()) {
        return this.groupChat.name
    }
    val creator = this.personalChat.creator
    val member = this.personalChat.member
    if (client.authorizedUser!!.id.equals(creator)) {
        return client.findUser(member)!!.name
    } else {
        return client.findUser(creator)!!.name
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
