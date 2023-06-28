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

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.protobuf.Timestamp
import io.spine.core.UserId
import io.spine.examples.chatspn.ChatId
import io.spine.examples.chatspn.MessageId
import io.spine.examples.chatspn.account.UserProfile
import io.spine.examples.chatspn.desktop.profile.ChatDeletionDialog
import io.spine.examples.chatspn.desktop.DesktopClient
import io.spine.examples.chatspn.desktop.component.Avatar
import io.spine.examples.chatspn.desktop.component.DefaultDropdownMenu
import io.spine.examples.chatspn.desktop.component.DefaultDropdownMenuItem
import io.spine.examples.chatspn.desktop.component.TopBar
import io.spine.examples.chatspn.desktop.navigation.ChatData
import io.spine.examples.chatspn.message.MessageView
import io.spine.examples.chatspn.message.event.MessageMarkedAsDeleted
import java.awt.Cursor
import java.awt.Cursor.getPredefinedCursor
import java.util.stream.Collectors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Displays the 'Chat' page in the application.
 *
 * @param client desktop client
 */
@Composable
@Preview
public fun ChatPage(
    client: DesktopClient,
    chatData: ChatData,
    openChatInfo: (chat: ChatId) -> Unit,
    openUserProfile: (user: UserId) -> Unit
) {
    val model = remember {
        ChatPageModel(
            client,
            chatData,
            openChatInfo,
            openUserProfile
        )
    }
    model.chatData = chatData
    val isChatDeletionDialogVisible = remember { model.chatDeletionModalState }
    model.observeMessages()
    Column(
        Modifier.fillMaxSize()
    ) {
        ChatTopBar(model)
        Box(Modifier.weight(1f)) {
            ChatMessages(model)
        }
        ChatBottomBar(model)
    }
    ChatDeletionDialog(
        isChatDeletionDialogVisible,
        { model.deleteChat(chatData.id) },
        chatData
    )
}

/**
 * Displays the top bar of chat content.
 */
@Composable
private fun ChatTopBar(model: ChatPageModel) {
    val chatData = model.chatData
    val interactionSource = remember { MutableInteractionSource() }
    TopBar {
        Row(
            Modifier.padding(6.dp),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            Row(
                Modifier
                    .pointerHoverIcon(PointerIcon(getPredefinedCursor(Cursor.HAND_CURSOR)))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        model.openChatInfo(chatData.id)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(42f, chatData.name)
                Text(
                    chatData.name,
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            ChatMoreButton(model)
        }
    }
}

/**
 * Displays the 'More' button for the chat.
 */
@Composable
private fun ChatMoreButton(model: ChatPageModel) {
    val isMenuOpen = remember { mutableStateOf(false) }
    Box(Modifier.clip(CircleShape)) {
        Icon(
            Icons.Default.MoreVert,
            "More",
            Modifier
                .size(24.dp)
                .pointerHoverIcon(PointerIcon(getPredefinedCursor(Cursor.HAND_CURSOR)))
                .clickable(enabled = !isMenuOpen.value) {
                    isMenuOpen.value = true
                }
        )
        ChatDropdownMenu(isMenuOpen, model)
    }
}

/**
 * Displays the context menu of the chat.
 */
@Composable
private fun ChatDropdownMenu(
    isMenuOpen: MutableState<Boolean>,
    model: ChatPageModel
) {
    DefaultDropdownMenu(isMenuOpen) {
        DefaultDropdownMenuItem(
            "Delete chat",
            Icons.Default.Delete,
            MaterialTheme.colorScheme.error
        ) {
            model.chatDeletionModalState.value = true
            isMenuOpen.value = false
        }
    }
}

/**
 * Displays the list of messages in the chat.
 */
@Composable
private fun ChatMessages(model: ChatPageModel) {
    val messages by model
        .messages()
        .collectAsState()
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Spacer(Modifier.height(4.dp))
        }
        messages.forEachIndexed { index, message ->
            item(message.id) {
                ChatMessage(
                    message,
                    model.chatData.id,
                    messages.isFirstMemberMessage(index),
                    messages.isLastMemberMessage(index),
                    { model.startMessageEditing(message) },
                    { model.openUserProfile(message.sender.id) },
                    model.client
                )
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
        }
    }
}

/**
 * Displays the bottom bar of the chat content.
 */
@Composable
private fun ChatBottomBar(model: ChatPageModel) {
    val isEditing by remember { model.messageInputFieldState.isEditingState }
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(48.dp, 224.dp)
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            },
    ) {
        if (isEditing) {
            EditMessagePanel(model)
        }
        ConfiguredMessageInputField(model)
    }
}

/**
 * Displays the input field for sending and editing a message in the chat.
 */
@Composable
private fun ConfiguredMessageInputField(model: ChatPageModel) {
    val inputText = remember { model.messageInputFieldState.inputText }
    var isEditing by remember { model.messageInputFieldState.isEditingState }
    var editingMessage by remember { model.messageInputFieldState.editingMessage }
    val onSubmit = {
        if (inputText.value.trim().isNotEmpty()) {
            if (isEditing) {
                model.editMessage(editingMessage!!.id, inputText.value.trim())
            } else {
                model.sendMessage(inputText.value.trim())
            }
        }
        isEditing = false
        editingMessage = null
        inputText.value = ""
    }
    if (isEditing) {
        inputText.value = editingMessage!!.content
        MessageInputField(inputText, Icons.Default.Check, onSubmit)
    } else {
        MessageInputField(inputText, Icons.Default.Send, onSubmit)
    }
}

/**
 * Displays the view of the message in editing state.
 */
@Composable
private fun EditMessagePanel(model: ChatPageModel) {
    val interactionSource = remember { MutableInteractionSource() }
    val message by remember { model.messageInputFieldState.editingMessage }
    Row(
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 16.dp, top = 8.dp, end = 12.dp),
        Arrangement.SpaceBetween,
        Alignment.CenterVertically
    ) {
        Row(
            Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Edit,
                "Edit",
                Modifier.size(16.dp),
                MaterialTheme.colorScheme.primary
            )
            Text(
                "Edit message: ",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                message!!.content.replace("\\s".toRegex(), " "),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            Modifier
                .padding(start = 12.dp)
                .pointerHoverIcon(PointerIcon(getPredefinedCursor(Cursor.HAND_CURSOR)))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    model.messageInputFieldState.clear()
                },
            MaterialTheme.colorScheme.onSecondary
        )
    }
}

/**
 * UI Model for the [ChatPage].
 *
 * UI Model is a layer between `@Composable` functions and client.
 */
private class ChatPageModel(
    val client: DesktopClient,
    var chatData: ChatData,
    val openChatInfo: (chat: ChatId) -> Unit,
    val openUserProfile: (user: UserId) -> Unit
) {
    private val messagesState: MutableMessagesState = MutableStateFlow(listOf())
    val messageInputFieldState: MessageInputFieldState = MessageInputFieldState()
    val chatDeletionModalState: MutableState<Boolean> = mutableStateOf(false)

    /**
     * Reads messages in the chat and subscribes to their updates.
     */
    fun observeMessages() {
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
    fun messages(): MessagesState {
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
    fun sendMessage(content: String) {
        client.sendMessage(chatData.id, content)
    }

    /**
     * Opens the message editing panel.
     *
     * @param message message data to start editing
     */
    fun startMessageEditing(message: MessageData) {
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
    fun editMessage(message: MessageId, newContent: String) {
        client.editMessage(chatData.id, message, newContent)
    }

    /**
     * Deletes the chat.
     *
     * @param chat ID of the chat to delete
     */
    fun deleteChat(chat: ChatId) {
        client.deleteChat(chat)
    }

    /**
     * State of the message input field.
     */
    class MessageInputFieldState {
        val inputText: MutableState<String> = mutableStateOf("")
        val isEditingState: MutableState<Boolean> = mutableStateOf(false)
        val editingMessage: MutableState<MessageData?> = mutableStateOf(null)

        /**
         * Clears the state.
         */
        fun clear() {
            inputText.value = ""
            isEditingState.value = false
            editingMessage.value = null
        }
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

/**
 * Defines whether the message at the provided index is the first in the message sequence
 * from a particular user in the last 10 minutes.
 */
private fun MessageList.isFirstMemberMessage(index: Int): Boolean {
    if (this.size - 1 < index) {
        throw IndexOutOfBoundsException()
    }
    return index == 0 ||
            !this[index].sender.equals(this[index - 1].sender) ||
            (this[index].whenPosted.seconds - this[index - 1].whenPosted.seconds > 600)
}

/**
 * Defines whether the message at the provided index is the last in the message sequence
 * from a particular user in the last 10 minutes.
 */
private fun MessageList.isLastMemberMessage(index: Int): Boolean {
    if (this.size - 1 < index) {
        throw IndexOutOfBoundsException()
    }
    return index == this.size - 1 ||
            !this[index].sender.equals(this[index + 1].sender) ||
            (this[index + 1].whenPosted.seconds - this[index].whenPosted.seconds > 600)
}
