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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Surface
import androidx.compose.material.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.spine.examples.chatspn.ChatId
import io.spine.examples.chatspn.chat.ChatPreview
import io.spine.examples.chatspn.desktop.ChatColors
import io.spine.examples.chatspn.desktop.ChatProvider
import io.spine.examples.chatspn.desktop.UserProvider

/**
 * Represents the 'Chats' page in the application.
 */
@Composable
@Preview
public fun ChatsPage(userProvider: UserProvider, chatProvider: ChatProvider) {
    val chats by chatProvider.chats().collectAsState()
    var selectedChat by remember { mutableStateOf(ChatId.getDefaultInstance()) }
    Row {
        LeftSidebar(userProvider, chats, selectedChat) { chat ->
            selectedChat = chat
        }
        ChatContent(
            userProvider,
            chatProvider,
            selectedChat,
            chats
        )
    }
}

/**
 * Represents the left sidebar on the `Chats` page.
 */
@Composable
private fun LeftSidebar(
    userProvider: UserProvider,
    chats: List<ChatPreview>,
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
            UserProfilePanel(userProvider.loggedUser())
            UserSearchField()
            ChatList(userProvider, chats, selectedChat, selectChat)
        }
    }
}

/**
 * Represents the list of chat previews.
 */
@Composable
private fun ChatList(
    userProvider: UserProvider,
    chats: List<ChatPreview>,
    selectedChat: ChatId,
    selectChat: (chat: ChatId) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chats.forEachIndexed { index, chat ->
            item(key = index) {
                ChatPreviewPanel(
                    chat.name(userProvider),
                    chat.lastMessage.content,
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
 * Represents the content of the chosen chat.
 */
@Composable
private fun ChatContent(
    userProvider: UserProvider,
    chatProvider: ChatProvider,
    selectedChat: ChatId,
    chats: List<ChatPreview>
) {
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
            ChatTopbar(userProvider, chats, selectedChat)
            Box(Modifier.weight(1f)) {
                ChatMessages(userProvider, chatProvider, selectedChat)
            }
            SendMessageInput(selectedChat, chatProvider)
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
            color = ChatColors.SECONDARY,
            fontSize = 20.sp
        )
    }
}

/**
 * Represents the topbar of chat content.
 */
@Composable
private fun ChatTopbar(userProvider: UserProvider, chats: List<ChatPreview>, selectedChat: ChatId) {
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
                chat.get().name(userProvider),
                modifier = Modifier.padding(start = 5.dp),
                fontSize = 26.sp
            )
        }
    }
}

/**
 * Represents the list of messages in the chat.
 */
@Composable
private fun ChatMessages(
    userProvider: UserProvider,
    chatProvider: ChatProvider,
    selectedChat: ChatId
) {
    val messages by chatProvider
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
                ChatMessage(
                    message,
                    userProvider
                )
            }
        }
        item {
            Box(Modifier.height(50.dp))
        }
    }
}
