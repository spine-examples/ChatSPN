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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.protobuf.Timestamp
import io.spine.examples.chatspn.ChatId
import io.spine.examples.chatspn.account.UserProfile
import java.text.SimpleDateFormat
import java.util.*

/**
 * Represents the 'Chats' page in the application.
 */
@Composable
@Preview
public fun ChatsPage(model: ChatsPageModel) {
    var selectedChat by remember { mutableStateOf(ChatId.getDefaultInstance()) }
    Row {
        LeftSidebar(model, selectedChat) { chat ->
            selectedChat = chat
        }
        ChatContent(model, selectedChat)
    }
}

/**
 * Represents the left sidebar on the `Chats` page.
 */
@Composable
private fun LeftSidebar(
    model: ChatsPageModel,
    selectedChat: ChatId,
    selectChat: (chat: ChatId) -> Unit
) {
    Surface(
        Modifier
            .padding(end = 1.dp)
            .drawBehind {
                drawLine(
                    color = Color.Gray,
                    start = Offset(size.width, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            },
    ) {
        Column(
            Modifier
                .fillMaxHeight()
                .width(280.dp)
        ) {
            UserProfilePanel(model.authorizedUser())
            UserSearchField()
            ChatList(model, selectedChat, selectChat)
        }
    }
}

/**
 * Represents the user's profile panel.
 */
@Composable
private fun UserProfilePanel(user: UserProfile) {
    Row(
        modifier = Modifier.padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar()
        Spacer(Modifier.size(5.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = user.email.value,
                color = MaterialTheme.colorScheme.onSecondary,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

/**
 * Represents the input to find the user.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserSearchField() {
    var inputText by remember { mutableStateOf("") }
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(0.dp, 5.dp),
        value = inputText,
        placeholder = {
            Text(text = "Type email ...")
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email Icon"
            )
        },
        singleLine = true,
        onValueChange = {
            inputText = it
        },
        label = { Text(text = "Find user by email") },
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary
        ),
        shape = MaterialTheme.shapes.small.copy(ZeroCornerSize),
        trailingIcon = {
            if (inputText.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .clickable { inputText = "" }
                        .padding(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Find",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Find")
                }
            }
        }
    )
}

/**
 * Represents the list of chat previews.
 */
@Composable
private fun ChatList(
    model: ChatsPageModel,
    selectedChat: ChatId,
    selectChat: (chat: ChatId) -> Unit
) {
    val chats by model.chats().collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chats.forEachIndexed { index, chat ->
            item(key = index) {
                ChatPreviewPanel(
                    chat.name,
                    if (chat.lastMessage != null) chat.lastMessage.content else "",
                    chat.id.equals(selectedChat)
                ) {
                    selectChat(chat.id)
                }
            }
        }
        item {
            Box(Modifier.height(50.dp))
        }
    }
}

/**
 * Represents the chat preview in the chats list.
 */
@Composable
private fun ChatPreviewPanel(
    chatName: String,
    lastMessage: String,
    isSelected: Boolean,
    select: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { select() }
            .background(
                color = if (isSelected)
                    MaterialTheme.colorScheme.surface
                else
                    MaterialTheme.colorScheme.background
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar()
            Spacer(Modifier.size(5.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = chatName,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    text = lastMessage,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Represents the content of the chosen chat.
 */
@Composable
private fun ChatContent(
    model: ChatsPageModel,
    selectedChat: ChatId
) {
    val chats by model.chats().collectAsState()
    val isChatSelected = chats
        .stream()
        .map { chat -> chat.id }
        .anyMatch { id -> id.equals(selectedChat) }
    if (!isChatSelected) {
        ChatNotChosenBox()
    } else {
        Column(
            Modifier.fillMaxSize()
        ) {
            ChatTopbar(model, selectedChat)
            Box(Modifier.weight(1f)) {
                ChatMessages(model, selectedChat)
            }
            SendMessageInput(selectedChat)
        }
    }
}

@Composable
private fun ChatNotChosenBox() {
    Box(
        Modifier.fillMaxSize(),
        Alignment.Center
    ) {
        Text(
            text = "Choose the chat",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSecondary,
        )
    }
}

/**
 * Represents the topbar of chat content.
 */
@Composable
private fun ChatTopbar(model: ChatsPageModel, selectedChat: ChatId) {
    val chats by model.chats().collectAsState()
    val chat = chats.stream().filter { chat -> chat.id.equals(selectedChat) }.findFirst()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            },
    ) {
        Row(
            Modifier.padding(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar()
            Text(
                chat.get().name,
                modifier = Modifier.padding(start = 5.dp),
                style = MaterialTheme.typography.headlineLarge,
            )
        }
    }
}

/**
 * Represents the list of messages in the chat.
 */
@Composable
private fun ChatMessages(
    model: ChatsPageModel,
    selectedChat: ChatId
) {
    val messages by model
        .messages(selectedChat)
        .collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp, 0.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        messages.forEach { message ->
            item(message.id) {
                ChatMessage(model, message)
            }
        }
        item {
            Box(Modifier.height(50.dp))
        }
    }
}

/**
 * Represents the single message view in the chat.
 */
@Composable
private fun ChatMessage(
    model: ChatsPageModel,
    message: MessageData
) {
    val isMyMessage = message.sender.id
        .equals(model.authorizedUser().id)
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMyMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            modifier = Modifier.padding(4.dp),
            shape = RoundedCornerShape(size = 20.dp),
            elevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                UserAvatar()
                Spacer(Modifier.size(8.dp))
                Column {
                    Row {
                        UserName(message.sender.name)
                        Spacer(Modifier.size(10.dp))
                        PostedTime(message.whenPosted)
                    }
                    Spacer(Modifier.size(8.dp))
                    MessageContent(message.content)
                }
            }
        }
    }
}

/**
 * Represents the name of the user who posted the message.
 */
@Composable
private fun UserName(username: String) {
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
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSecondary
    )
}

/**
 * Represents the content of the message.
 */
@Composable
private fun MessageContent(content: String) {
    Text(
        text = content,
        style = MaterialTheme.typography.bodyMedium
    )
}

/**
 * Converts `Timestamp` to the `hh:mm` string.
 */
private fun Timestamp.toStringTime(): String {
    val date = Date(this.seconds * 1000)
    val format = SimpleDateFormat("hh:mm", Locale.getDefault())
    return format.format(date)
}

/**
 * Represents the input for sending a message to the chat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SendMessageInput(chat: ChatId) {
    var inputText by remember { mutableStateOf("") }
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp),
        value = inputText,
        placeholder = {
            Text("Type message here")
        },
        onValueChange = {
            inputText = it
        },
        colors = TextFieldDefaults.textFieldColors(
            focusedIndicatorColor = MaterialTheme.colorScheme.onSecondary,
        ),
        trailingIcon = {
            if (inputText.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .clickable {
//                            chatProvider.sendMessage(chat, inputText)
                            inputText = ""
                        }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Send")
                }
            }
        }
    )
}
