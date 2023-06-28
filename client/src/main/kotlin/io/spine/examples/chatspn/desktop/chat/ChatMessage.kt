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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.onClick
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.google.protobuf.Timestamp
import io.spine.examples.chatspn.ChatId
import io.spine.examples.chatspn.account.UserProfile
import io.spine.examples.chatspn.desktop.DesktopClient
import io.spine.examples.chatspn.desktop.component.Avatar
import io.spine.examples.chatspn.desktop.component.DefaultDropdownMenu
import io.spine.examples.chatspn.desktop.component.DefaultDropdownMenuItem
import io.spine.examples.chatspn.desktop.navigation.toStringTime
import java.awt.Cursor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Displays a single message view in the chat.
 *
 * @param data message data to display
 * @param chatId id of the chat the message is from
 * @param isFirst whether it is the first message in the sequence
 * @param isLast whether it is the last message in the sequence
 * @param startMessageEditing function to start editing this message
 * @param openSenderProfile function to open profile of the user who sent this message
 * @param client desktop client
 */
@Composable
public fun ChatMessage(
    data: MessageData,
    chatId: ChatId,
    isFirst: Boolean,
    isLast: Boolean,
    startMessageEditing: () -> Unit,
    openSenderProfile: () -> Unit,
    client: DesktopClient
) {
    val model =
        MessageModel(data, chatId, isFirst, isLast, startMessageEditing, openSenderProfile, client)
    val alignment = if (model.isMyMessage) {
        Alignment.CenterEnd
    } else {
        Alignment.CenterStart
    }
    Box(
        Modifier.fillMaxWidth(),
        alignment
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            MessageSenderAvatar(
                model.data.sender,
                model,
                !model.isMyMessage && model.isLast
            )
            Column {
                MessageSurface(model)
                if (model.isLast) {
                    Spacer(Modifier.height(12.dp))
                }
            }
            MessageSenderAvatar(
                model.data.sender,
                model,
                model.isMyMessage && model.isLast
            )
        }
    }
}

/**
 * Displays an avatar of the user who sent the message.
 *
 * @param user user whose avatar to display
 * @param model message model
 * @param isVisible if `true` displays the avatar else displays the empty space
 */
@Composable
private fun MessageSenderAvatar(user: UserProfile, model: MessageModel, isVisible: Boolean) {
    val interactionSource = remember { MutableInteractionSource() }
    Column {
        if (isVisible) {
            Avatar(42f, user.name) {
                this.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        model.openSenderProfile()
                    }
            }
            Spacer(Modifier.width(4.dp))
        } else {
            Spacer(Modifier.width(42.dp))
        }
    }
}

/**
 * Displays message surface.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageSurface(
    model: MessageModel,
) {
    val isMenuOpen = remember { mutableStateOf(false) }
    val shape = MessageBubbleShape(
        16f,
        model.arrowWidth,
        if (model.isMyMessage) MessageBubbleArrowPlace.RIGHT_BOTTOM
        else MessageBubbleArrowPlace.LEFT_BOTTOM,
        !model.isLast
    )
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
        shape = shape,
        elevation = 4.dp,
        color = if (model.isMyMessage) {
            MaterialTheme.colorScheme.inverseSurface
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        MessageContent(model)
        MessageDropdownMenu(isMenuOpen, model)
    }
}

/**
 * Displays the context menu of the message.
 */
@Composable
private fun MessageDropdownMenu(
    isMenuOpen: MutableState<Boolean>,
    model: MessageModel,
) {
    val viewScope = rememberCoroutineScope { Dispatchers.Default }
    DefaultDropdownMenu(isMenuOpen) {
        if (model.isMyMessage) {
            DefaultDropdownMenuItem("Edit", Icons.Default.Edit) {
                model.startMessageEditing()
                isMenuOpen.value = false
            }
        }
        DefaultDropdownMenuItem("Remove", Icons.Default.Delete) {
            viewScope.launch {
                model.removeMessage()
            }
            isMenuOpen.value = false
        }
    }
}

/**
 * Displays the content of the message.
 */
@Composable
private fun MessageContent(model: MessageModel) {
    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.Bottom) {
        if (!model.isMyMessage) {
            Spacer(Modifier.width(model.arrowWidth.dp))
        }
        Column {
            if (model.isFirst) {
                SenderName(model.data.sender.name)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = model.data.content,
                    Modifier.widthIn(0.dp, 300.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Spacer(Modifier.size(8.dp))
        WhenMessagePosted(model.data.whenPosted)
        if (model.isMyMessage) {
            Spacer(Modifier.width(model.arrowWidth.dp))
        }
    }
}

/**
 * Displays the name of the user who sent this message.
 */
@Composable
private fun SenderName(username: String) {
    Text(
        text = username,
        style = MaterialTheme.typography.headlineMedium
    )
}

/**
 * Displays the time when the message was posted.
 */
@Composable
private fun WhenMessagePosted(time: Timestamp) {
    Text(
        text = time.toStringTime(),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSecondary
    )
}

/**
 * UI Model for the [ChatMessage].
 */
private class MessageModel(
    val data: MessageData,
    val chatId: ChatId,
    val isFirst: Boolean,
    val isLast: Boolean,
    val startMessageEditing: () -> Unit,
    val openSenderProfile: () -> Unit,
    private val client: DesktopClient
) {
    val isMyMessage: Boolean = data.sender.id.equals(client.authenticatedUser?.id)
    val arrowWidth: Float = 8f

    /**
     * Removes this message from the chat.
     */
    fun removeMessage() {
        client.removeMessage(chatId, data.id)
    }
}
