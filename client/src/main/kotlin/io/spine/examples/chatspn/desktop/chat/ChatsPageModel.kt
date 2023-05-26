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

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.protobuf.Timestamp
import io.spine.core.UserId
import io.spine.examples.chatspn.ChatId
import io.spine.examples.chatspn.MessageId
import io.spine.examples.chatspn.account.UserProfile
import io.spine.examples.chatspn.chat.ChatPreview
import io.spine.examples.chatspn.chat.MessagePreview
import io.spine.examples.chatspn.desktop.DesktopClient
import io.spine.examples.chatspn.message.MessageView
import java.util.stream.Collectors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * UI Model for the `ChatsPage`.
 *
 * UI Model is a layer between `@Composable` functions and client.
 */
public class ChatsPageModel(private val client: DesktopClient) {
    private val selectedChatState = MutableStateFlow<ChatId>(ChatId.getDefaultInstance())
    private val chatPreviewsState = MutableStateFlow<ChatList>(listOf())
    private val chatMessagesStateMap: MutableMap<ChatId, MutableMessagesState> = mutableMapOf()
    public val userSearchState: UserSearchState = UserSearchState()
    public val authenticatedUser: UserProfile = client.authenticatedUser!!

    init {
        updateChats(client.readChats().toChatDataList())
        client.observeChats { state -> updateChats(state.chatList.toChatDataList()) }
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

    /**
     * Returns the state of selected chat.
     */
    public fun selectedChat(): StateFlow<ChatId> {
        return selectedChatState
    }

    /**
     * Selects provided chat.
     */
    public fun selectChat(chat: ChatId) {
        selectedChatState.value = chat
        updateMessages(chat, client.readMessages(chat).toMessageDataList())
        client.stopObservingMessages()
        client.observeMessages(
            chat,
            { messageView ->
                val message = messageView.toMessageData()
                val chatMessages = chatMessagesStateMap[chat]!!.value
                if (chatMessages.contains(message)) {
                    val newChatMessages = chatMessages.replaceMessage(message)
                    updateMessages(chat, newChatMessages)
                } else {
                    updateMessages(chat, chatMessages + message)
                }
            },
            { messageDeleted ->
                val chatMessages = chatMessagesStateMap[chat]!!.value
                val message = chatMessages.findMessage(messageDeleted.id)
                if (message != null) {
                    val messageIndex = chatMessages.indexOf(message)
                    val newChatMessages = chatMessages.remove(messageIndex)
                    updateMessages(chat, newChatMessages)
                }
            })
    }

    /**
     * Returns the new list with the replaced message.
     */
    private fun MessageList.replaceMessage(newMessage: MessageData): MessageList {
        val oldMessage = this.findMessage(newMessage.id)
        val messageIndex = this.indexOf(oldMessage)
        val leftPart = this.subList(0, messageIndex)
        val rightPart = this.subList(messageIndex + 1, this.size)
        return leftPart + newMessage + rightPart
    }

    /**
     * Returns the new list without an element on provided index.
     */
    private fun <T> List<T>.remove(index: Int): List<T> {
        return this.subList(0, index) + this.subList(index + 1, this.size)
    }

    /**
     * Finds message in the list by ID.
     */
    private fun MessageList.findMessage(id: MessageId): MessageData? {
        val message = this
            .stream()
            .filter { message -> message.id.equals(id) }
            .collect(Collectors.toList())
        if (message.isEmpty()) {
            return null
        }
        return message[0]
    }

    /**
     * Finds user by ID.
     */
    public fun findUser(userId: UserId): UserProfile? {
        return client.findUser(userId)
    }

    /**
     * Finds user by email.
     */
    public fun findUser(email: String): UserProfile? {
        return client.findUser(email)
    }

    /**
     * Sends message to the selected chat.
     */
    public fun sendMessage(content: String) {
        client.sendMessage(selectedChatState.value, content)
    }

    /**
     * Finds user by email and creates the personal chat.
     */
    public fun createPersonalChat(email: String) {
        val user = client.findUser(email)
        if (null != user) {
            client.createPersonalChat(user.id)
        } else {
            userSearchState.errorState.value = true
        }
    }

    /**
     * Updates the model with new chats.
     */
    private fun updateChats(chats: ChatList) {
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

    /**
     * Converts the `ChatPreview` list to the `ChatData` list.
     */
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

    /**
     * Converts the `MessageView` list to the `MessageData` list.
     */
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

    /**
     * Converts the `MessagePreview` to `MessageData`.
     */
    private fun MessagePreview.toMessageData(): MessageData {
        return MessageData(
            this.id,
            client.findUser(this.user)!!,
            this.content,
            this.whenPosted
        )
    }

    /**
     * Converts the `MessageView` to `MessageData`.
     */
    private fun MessageView.toMessageData(): MessageData {
        return MessageData(
            this.id,
            client.findUser(this.user)!!,
            this.content,
            this.whenPosted
        )
    }

    /**
     * Extracts the name of the chat.
     */
    private fun ChatPreview.name(): String {
        if (this.hasGroupChat()) {
            return this.groupChat.name
        }
        val creator = this.personalChat.creator
        val member = this.personalChat.member
        if (client.authenticatedUser!!.id.equals(creator)) {
            return client.findUser(member)!!.name
        }
        return client.findUser(creator)!!.name
    }

    public class UserSearchState {
        public val userEmailState: MutableState<String> = mutableStateOf("")
        public val errorState: MutableState<Boolean> = mutableStateOf(false)
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
