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
import io.spine.examples.chatspn.desktop.DesktopClient
import io.spine.examples.chatspn.desktop.navigation.ChatData
import io.spine.examples.chatspn.message.MessageView
import io.spine.examples.chatspn.message.event.MessageMarkedAsDeleted
import java.util.stream.Collectors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * UI Model for the [ChatPage].
 *
 * UI Model is a layer between `@Composable` functions and client.
 *
 * @param client desktop client
 * @param chatData data of the chat to display
 * @param openChatInfo function to open the chat info
 * @param openUserProfile function to open the user profile
 */
public class ChatPageModel(
    public val client: DesktopClient,
    public var chatData: ChatData,
    public val openChatInfo: (chat: ChatId) -> Unit,
    public val openUserProfile: (user: UserId) -> Unit
) {
    private val messagesState: MutableMessagesState = MutableStateFlow(listOf())
    public val messageInputFieldState: MessageInputFieldState = MessageInputFieldState()
    public val chatDeletionModalState: MutableState<Boolean> = mutableStateOf(false)

    /**
     * Reads messages in the chat and subscribes to their updates.
     */
    public fun observeMessages() {
        messageInputFieldState.clear()
        messagesState.value = client.readMessages(chatData.id).toMessageDataList(client)
        client.stopObservingMessages()
        client.observeMessages(
            chatData.id,
            { messageView -> updateMessagesState(messageView) },
            { messageDeleted -> updateMessagesState(messageDeleted) })
    }

    /**
     * Returns the state of messages in the chat.
     */
    public fun messages(): MessagesState {
        return messagesState
    }

    /**
     * Updates the state of chat messages by adding a new message,
     * or editing existing one if message ID matches.
     *
     * @param messageView message to update the state
     */
    private fun updateMessagesState(messageView: MessageView) {
        val message = messageView.toMessageData(client)
        val chatMessages = messagesState.value
        if (chatMessages.findMessage(message.id) != null) {
            val newChatMessages = chatMessages.replaceMessage(message)
            messagesState.value = newChatMessages
        } else {
            messagesState.value = chatMessages + message
        }
    }

    /**
     * Updates the state of chat messages by removing a message.
     *
     * @param messageDeleted event about message deletion
     */
    private fun updateMessagesState(messageDeleted: MessageMarkedAsDeleted) {
        val chatMessages = messagesState.value
        val message = chatMessages.findMessage(messageDeleted.id)
        if (message != null) {
            val messageIndex = chatMessages.indexOf(message)
            val newChatMessages = chatMessages.remove(messageIndex)
            messagesState.value = newChatMessages
        }
    }

    /**
     * Sends a message to the selected chat.
     *
     * @param content message text content
     */
    public fun sendMessage(content: String) {
        client.sendMessage(chatData.id, content)
    }

    /**
     * Opens the message editing panel.
     *
     * @param message message data to start editing
     */
    public fun startMessageEditing(message: MessageData) {
        messageInputFieldState.isEditingState.value = true
        messageInputFieldState.editingMessage.value = message
        messageInputFieldState.inputText.value = message.content
    }

    /**
     * Edits message in the selected chat.
     *
     * @param message ID of the message to edit
     * @param newContent new text content for the message
     */
    public fun editMessage(message: MessageId, newContent: String) {
        client.editMessage(chatData.id, message, newContent)
    }

    /**
     * Deletes the chat.
     *
     * @param chat ID of the chat to delete
     */
    public fun deleteChat(chat: ChatId) {
        client.deleteChat(chat)
    }
}

/**
 * State of the message input field.
 */
public class MessageInputFieldState {
    public val inputText: MutableState<String> = mutableStateOf("")
    public val isEditingState: MutableState<Boolean> = mutableStateOf(false)
    public val editingMessage: MutableState<MessageData?> = mutableStateOf(null)

    /**
     * Clears the state.
     */
    public fun clear() {
        inputText.value = ""
        isEditingState.value = false
        editingMessage.value = null
    }
}

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
 * Finds message in the list by ID.
 *
 * @param id ID of the message to find
 * @return found message or `null` if message is not found
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
 * Returns the new list with the replaced message.
 *
 * @param newMessage message to replace
 */
private fun MessageList.replaceMessage(newMessage: MessageData): MessageList {
    val oldMessage = this.findMessage(newMessage.id)
    val messageIndex = this.indexOf(oldMessage)
    val leftPart = this.subList(0, messageIndex)
    val rightPart = this.subList(messageIndex + 1, this.size)
    return leftPart + newMessage + rightPart
}

/**
 * Creates the `MessageData` from the `MessageView`.
 *
 * @param client desktop client to find user profiles
 */
private fun MessageView.toMessageData(client: DesktopClient): MessageData {
    return MessageData(
        this.id,
        client.findUser(this.user)!!,
        this.content,
        this.whenPosted
    )
}

/**
 * Creates the `MessageData` list. from the `MessageView` list.
 *
 * @param client desktop client to find user profiles
 */
private fun List<MessageView>.toMessageDataList(client: DesktopClient): MessageList {
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

/**
 * Returns the new list without an element on provided index.
 *
 * @param index index of the element to remove
 */
private fun <T> List<T>.remove(index: Int): List<T> {
    return this.subList(0, index) + this.subList(index + 1, this.size)
}
