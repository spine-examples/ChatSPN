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

package io.spine.examples.chatspn.desktop

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.google.protobuf.Timestamp
import io.spine.core.UserId
import io.spine.examples.chatspn.ChatId
import io.spine.examples.chatspn.MessageId
import io.spine.examples.chatspn.account.UserProfile
import io.spine.examples.chatspn.chat.Chat.ChatType.CT_PERSONAL
import io.spine.examples.chatspn.message.MessageView
import io.spine.examples.chatspn.message.event.MessageMarkedAsDeleted
import java.awt.Cursor
import java.awt.Cursor.getPredefinedCursor
import java.util.*
import java.util.stream.Collectors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Represents the 'Chat' page in the application.
 *
 * @param client desktop client
 */
@Composable
@Preview
public fun ChatPage(
    client: DesktopClient,
    chatState: MutableState<ChatData?>,
    openModal: (content: @Composable () -> Unit) -> Unit,
    closeModal: () -> Unit,
    openChatInfo: (chat: ChatId) -> Unit,
    openUserProfile: (user: UserId) -> Unit
) {
    val model = remember {
        ChatsPageModel(
            client,
            chatState,
            openModal,
            closeModal,
            openChatInfo,
            openUserProfile
        )
    }
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
}

/**
 * Represents the top bar of chat content.
 */
@Composable
private fun ChatTopBar(model: ChatsPageModel) {
    val chatData = remember { model.chatState }
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
                        model.openChatInfo(chatData.value!!.id)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(42f, chatData.value!!.name)
                Text(
                    chatData.value!!.name,
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            ChatMoreButton(model, chatData.value!!)
        }
    }
}

/**
 * Represents the 'More' button for the chat.
 */
@Composable
private fun ChatMoreButton(model: ChatsPageModel, chat: ChatData) {
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
        ChatDropdownMenu(model, isMenuOpen, chat)
    }
}

/**
 * Represents the context menu of the chat.
 */
@Composable
private fun ChatDropdownMenu(
    model: ChatsPageModel,
    isMenuOpen: MutableState<Boolean>,
    chat: ChatData
) {
    DropdownMenu(
        expanded = isMenuOpen.value,
        onDismissRequest = { isMenuOpen.value = false },
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
    ) {
        DefaultDropdownMenuItem(
            "Delete chat",
            Icons.Default.Delete,
            MaterialTheme.colorScheme.error
        ) {
            model.openModal(
                ChatDeletionDialog(
                    { model.closeModal() },
                    { model.deleteChat(chat.id) },
                    chat
                )
            )
            isMenuOpen.value = false
        }
    }
}

/**
 * Returns a representation of the chat deletion confirmation modal.
 */
public fun ChatDeletionDialog(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    chat: ChatData
): @Composable () -> Unit {
    return {
        val viewScope = rememberCoroutineScope()
        Column(
            Modifier.width(300.dp)
                .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 8.dp)
        ) {
            Text(
                buildAnnotatedString {
                    append("Are you sure you want to delete chat ")
                    if (chat.type == CT_PERSONAL) {
                        append("with ")
                    }
                    append(
                        AnnotatedString(
                            chat.name,
                            spanStyle = SpanStyle(fontWeight = FontWeight.Bold)
                        )
                    )
                    append("?")
                },
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "This action cannot be undone.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                TextButton("Cancel") {
                    onDismiss()
                }
                TextButton("Delete", MaterialTheme.colorScheme.error) {
                    viewScope.launch {
                        onDelete()
                    }
                    onDismiss()
                }
            }
        }
    }
}

/**
 * Represents the list of messages in the chat.
 */
@Composable
private fun ChatMessages(model: ChatsPageModel) {
    val chat = remember { model.chatState }
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
                val isFirst = messages.isFirstMemberMessage(index)
                val isLast = messages.isLastMemberMessage(index)
                ChatMessage(model, message, isFirst, isLast)
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
        }
    }
}

/**
 * Represents the single message view in the chat.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatMessage(
    model: ChatsPageModel,
    message: MessageData,
    isFirstMemberMessage: Boolean,
    isLastMemberMessage: Boolean
) {
    val isMyMessage = message.sender.id
        .equals(model.authenticatedUser.id)
    val isMenuOpen = remember { mutableStateOf(false) }
    val messageDisplaySettings = defineMessageDisplaySettings(isMyMessage, isLastMemberMessage)
    Box(
        Modifier.fillMaxWidth(),
        messageDisplaySettings.alignment
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            MessageSenderAvatar(model, !isMyMessage && isLastMemberMessage, message.sender)
            Column {
                Surface(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .onClick(
                            enabled = true,
                            matcher = PointerMatcher.mouse(PointerButton.Secondary),
                            onClick = {
                                isMenuOpen.value = true
                            }
                        ),
                    shape = messageDisplaySettings.shape,
                    elevation = 4.dp,
                    color = messageDisplaySettings.color
                ) {
                    MessageContent(
                        message,
                        isFirstMemberMessage,
                        isMyMessage,
                        messageDisplaySettings.arrowWidth.dp
                    )
                    MessageDropdownMenu(model, isMenuOpen, message, isMyMessage)
                }
                if (isLastMemberMessage) {
                    Spacer(Modifier.height(12.dp))
                }
            }
            MessageSenderAvatar(model, isMyMessage && isLastMemberMessage, message.sender)
        }
    }
}

/**
 * Defines the display settings of the message.
 */
@Composable
private fun defineMessageDisplaySettings(
    isMyMessage: Boolean,
    isLastMemberMessage: Boolean
): MessageDisplaySettings {
    val alignment: Alignment
    val color: Color
    val arrowWidth = 8f
    val shape: Shape = messageBubbleShape(
        16f,
        8f,
        if (isMyMessage) MessageBubbleArrowPlace.RIGHT_BOTTOM
        else MessageBubbleArrowPlace.LEFT_BOTTOM,
        !isLastMemberMessage
    )
    if (isMyMessage) {
        alignment = Alignment.CenterEnd
        color = MaterialTheme.colorScheme.inverseSurface
    } else {
        alignment = Alignment.CenterStart
        color = MaterialTheme.colorScheme.surface
    }
    return MessageDisplaySettings(color, shape, alignment, arrowWidth)
}

/**
 * Creates a shape of message bubble.
 *
 * @param cornerRadius corner radius of the bubble
 * @param messageArrowWidth width of the message arrow
 * @param arrowPlace place of the message arrow
 * @param skipArrow if is `true` the bubble shape will be without an arrow, but with space for it
 */
@Composable
private fun messageBubbleShape(
    cornerRadius: Float = 16f,
    messageArrowWidth: Float = 8f,
    arrowPlace: MessageBubbleArrowPlace,
    skipArrow: Boolean = false
): GenericShape {
    val density = LocalDensity.current.density
    return GenericShape { size: Size, _: LayoutDirection ->
        val contentWidth: Float = size.width
        val contentHeight: Float = size.height
        val arrowWidth: Float = (messageArrowWidth * density).coerceAtMost(contentWidth)
        val arrowHeight: Float = (arrowWidth * 3 / 4 * density).coerceAtMost(contentHeight)
        val arrowLeft: Float
        val arrowRight: Float
        val arrowTop = contentHeight - arrowHeight
        val arrowBottom = contentHeight

        if (skipArrow) {
            val rectStart = if (arrowPlace == MessageBubbleArrowPlace.LEFT_BOTTOM) arrowWidth
            else 0f
            val rectEnd = if (arrowPlace == MessageBubbleArrowPlace.LEFT_BOTTOM) contentWidth
            else contentWidth - arrowWidth
            addRoundRect(
                RoundRect(
                    rect = Rect(rectStart, 0f, rectEnd, contentHeight),
                    topLeft = CornerRadius(cornerRadius, cornerRadius),
                    topRight = CornerRadius(cornerRadius, cornerRadius),
                    bottomLeft = CornerRadius(cornerRadius, cornerRadius),
                    bottomRight = CornerRadius(cornerRadius, cornerRadius)
                )
            )
            return@GenericShape
        }

        when (arrowPlace) {
            MessageBubbleArrowPlace.LEFT_BOTTOM -> {
                arrowLeft = 0f
                arrowRight = arrowWidth
                addRoundRect(
                    RoundRect(
                        rect = Rect(arrowWidth, 0f, contentWidth, contentHeight),
                        topLeft = CornerRadius(cornerRadius, cornerRadius),
                        topRight = CornerRadius(cornerRadius, cornerRadius),
                        bottomRight = CornerRadius(cornerRadius, cornerRadius)
                    )
                )
            }
            MessageBubbleArrowPlace.RIGHT_BOTTOM -> {
                arrowLeft = contentWidth - arrowWidth
                arrowRight = contentWidth
                addRoundRect(
                    RoundRect(
                        rect = Rect(0f, 0f, contentWidth - arrowWidth, contentHeight),
                        topLeft = CornerRadius(cornerRadius, cornerRadius),
                        topRight = CornerRadius(cornerRadius, cornerRadius),
                        bottomLeft = CornerRadius(cornerRadius, cornerRadius)
                    )
                )
            }
        }

        val path = Path().apply {
            if (arrowPlace == MessageBubbleArrowPlace.LEFT_BOTTOM) {
                moveTo(arrowRight, arrowTop)
                lineTo(arrowLeft, arrowBottom)
                lineTo(arrowRight, arrowBottom)
            } else {
                moveTo(arrowLeft, arrowTop)
                lineTo(arrowRight, arrowBottom)
                lineTo(arrowLeft, arrowBottom)
            }
            close()
        }
        this.op(this, path, PathOperation.Union)
    }
}

private enum class MessageBubbleArrowPlace {
    LEFT_BOTTOM, RIGHT_BOTTOM
}

/**
 * Represents the avatar of the user who send the message.
 *
 * @param isVisible if `true` displays the avatar else displays the empty space
 */
@Composable
private fun MessageSenderAvatar(model: ChatsPageModel, isVisible: Boolean, user: UserProfile) {
    val interactionSource = remember { MutableInteractionSource() }
    Column {
        if (isVisible) {
            Avatar(42f, user.name) {
                this.pointerHoverIcon(PointerIcon(getPredefinedCursor(Cursor.HAND_CURSOR)))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        model.openUserProfile(user.id)
                    }
            }
            Spacer(Modifier.width(4.dp))
        } else {
            Spacer(Modifier.width(42.dp))
        }
    }
}

/**
 * Represents the context menu of the message.
 */
@Composable
private fun MessageDropdownMenu(
    model: ChatsPageModel,
    isMenuOpen: MutableState<Boolean>,
    message: MessageData,
    isMyMessage: Boolean
) {
    val viewScope = rememberCoroutineScope { Dispatchers.Default }
    DropdownMenu(
        expanded = isMenuOpen.value,
        onDismissRequest = { isMenuOpen.value = false },
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
    ) {
        if (isMyMessage) {
            DefaultDropdownMenuItem("Edit", Icons.Default.Edit) {
                model.messageInputFieldState.isEditingState.value = true
                model.messageInputFieldState.editingMessage.value = message
                model.messageInputFieldState.inputText.value = message.content
                isMenuOpen.value = false
            }
        }
        DefaultDropdownMenuItem("Remove", Icons.Default.Delete) {
            viewScope.launch {
                model.removeMessage(message.id)
            }
            isMenuOpen.value = false
        }
    }
}

/**
 * Represents the default item of the dropdown menu.
 */
@Composable
private fun DefaultDropdownMenuItem(
    text: String,
    icon: ImageVector,
    color: Color = Color.Black,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        modifier = Modifier
            .height(30.dp),
        text = {
            Text(
                text,
                color = color,
                style = MaterialTheme.typography.labelMedium
            )
        },
        onClick = onClick,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = color
            )
        }
    )
}

/**
 * Represents the content of the message.
 */
@Composable
private fun MessageContent(
    message: MessageData,
    withName: Boolean,
    isMyMessage: Boolean,
    horizontalPadding: Dp
) {
    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.Bottom) {
        if (!isMyMessage) {
            Spacer(Modifier.width(horizontalPadding))
        }
        Column {
            if (withName) {
                SenderName(message.sender.name)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = message.content,
                    Modifier.widthIn(0.dp, 300.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Spacer(Modifier.size(8.dp))
        PostedTime(message.whenPosted)
        if (isMyMessage) {
            Spacer(Modifier.width(horizontalPadding))
        }
    }
}

/**
 * Represents the name of the user who sent this message.
 */
@Composable
private fun SenderName(username: String) {
    Text(
        text = username,
        style = MaterialTheme.typography.headlineMedium
    )
}

/**
 * Represents the time when the message was posted.
 */
@Composable
private fun PostedTime(time: Timestamp) {
    Text(
        text = time.toStringTime(),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSecondary
    )
}

/**
 * Represents the bottom bar of the chat content.
 */
@Composable
private fun ChatBottomBar(model: ChatsPageModel) {
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
        MessageInputField(model)
    }
}

/**
 * Represents the input field for sending and editing a message in the chat.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MessageInputField(model: ChatsPageModel) {
    val viewScope = rememberCoroutineScope { Dispatchers.Default }
    var inputText by remember { model.messageInputFieldState.inputText }
    var isEditing by remember { model.messageInputFieldState.isEditingState }
    var editingMessage by remember { model.messageInputFieldState.editingMessage }
    val onSend = {
        if (inputText.trim().isNotEmpty()) {
            if (isEditing) {
                model.editMessage(editingMessage!!.id, inputText.trim())
            } else {
                model.sendMessage(inputText.trim())
            }
        }
        isEditing = false
        editingMessage = null
        inputText = ""
    }

    BasicTextField(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .onPreviewKeyEvent {
                when {
                    (it.type == KeyEventType.KeyDown &&
                            it.isShiftPressed &&
                            it.key == Key.Enter) -> {
                        viewScope.launch {
                            onSend()
                        }
                        true
                    }
                    else -> false
                }
            },
        textStyle = MaterialTheme.typography.bodyMedium,
        keyboardActions = KeyboardActions(),
        value = inputText,
        onValueChange = {
            inputText = it
        }
    ) { innerTextField ->
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
                    .heightIn(15.dp, 192.dp)
                    .weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (inputText.isEmpty()) {
                    Text(
                        text = "Write a message...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
                innerTextField()
            }
            if (inputText.trim().isNotEmpty()) {
                MessageInputFieldIcon(model, onSend)
            }
        }
    }
}

/**
 * Represents the icon button to send or edit the message.
 */
@Composable
private fun MessageInputFieldIcon(model: ChatsPageModel, onPressed: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val viewScope = rememberCoroutineScope { Dispatchers.Default }
    val inputText by remember { model.messageInputFieldState.inputText }
    val isEditing by remember { model.messageInputFieldState.isEditingState }
    val icon = if (isEditing) Icons.Default.Check else Icons.Default.Send

    if (inputText.trim().isNotEmpty()) {
        Icon(
            icon,
            "Send",
            Modifier
                .pointerHoverIcon(PointerIcon(getPredefinedCursor(Cursor.HAND_CURSOR)))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    viewScope.launch {
                        onPressed()
                    }
                }
                .padding(bottom = 12.dp, end = 12.dp),
            MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Represents the view of the message in editing state.
 */
@Composable
private fun EditMessagePanel(model: ChatsPageModel) {
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
private class ChatsPageModel(
    private val client: DesktopClient,
    val chatState: MutableState<ChatData?>,
    val openModal: (content: @Composable () -> Unit) -> Unit,
    val closeModal: () -> Unit,
    val openChatInfo: (chat: ChatId) -> Unit,
    val openUserProfile: (user: UserId) -> Unit
) {
    private val messagesState: MutableMessagesState = MutableStateFlow(listOf())
    val messageInputFieldState: MessageInputFieldState = MessageInputFieldState()
    val authenticatedUser: UserProfile
        get() {
            return client.authenticatedUser!!
        }

    /**
     * Reads messages in the chat and subscribes to their updates.
     */
    fun observeMessages() {
        messageInputFieldState.clear()
        messagesState.value = client.readMessages(chatState.value!!.id).toMessageDataList(client)
        client.stopObservingMessages()
        client.observeMessages(
            chatState.value!!.id,
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
        client.sendMessage(chatState.value!!.id, content)
    }

    /**
     * Removes message from the selected chat.
     *
     * @param message ID of the message to remove
     */
    fun removeMessage(message: MessageId) {
        client.removeMessage(chatState.value!!.id, message)
    }

    /**
     * Edits message in the selected chat.
     *
     * @param message ID of the message to edit
     * @param newContent new text content for the message
     */
    fun editMessage(message: MessageId, newContent: String) {
        client.editMessage(chatState.value!!.id, message, newContent)
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

/**
 * Settings to display the single message.
 */
private data class MessageDisplaySettings(
    val color: Color,
    val shape: Shape,
    val alignment: Alignment,
    val arrowWidth: Float
)
