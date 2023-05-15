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

package io.spine.examples.chatspn.chat

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.spine.examples.chatspn.ChatColors
import io.spine.examples.chatspn.ChatId
import io.spine.examples.chatspn.ChatProvider
import io.spine.examples.chatspn.UserProvider

/**
 * Represents the 'Chats' page in the application.
 */
@Composable
@Preview
public fun ChatsPage(userProvider: UserProvider, chatProvider: ChatProvider) {
    val chats by chatProvider.chats().collectAsState()
    var selectedChat by remember { mutableStateOf(ChatId.getDefaultInstance()) }
    Row {
        Surface(
            elevation = 8.dp
        ) {
            Column(
                Modifier
                    .fillMaxHeight()
                    .width(280.dp)
            ) {
                UserProfilePanel(userProvider.loggedUser())
                UserSearchBar()
                LazyColumn( // Chats list
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chats.forEachIndexed { index, chat ->
                        item(key = index) {
                            ChatPreviewPanel(
                                chatName(chat, userProvider),
                                chat.lastMessage.content,
                                chat.id.equals(selectedChat)
                            ) {
                                selectedChat = chat.id
                            }
                        }
                    }
                    item {
                        Box(Modifier.height(50.dp))
                    }
                }
            }
        }
        val isChatSelected = chats
            .stream()
            .map { chat -> chat.id }
            .anyMatch { id -> id.equals(selectedChat) }
        if (!isChatSelected) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Choose the chat",
                    color = ChatColors.SECONDARY,
                    fontSize = 20.sp
                )
            }
        } else {
            val messages by chatProvider
                .messages(selectedChat)
                .collectAsState()
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(5.dp, 0.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        messages.forEach { message ->
                            item(key = message.id) {
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
                SendMessageInput(selectedChat, chatProvider)
            }
        }
    }
}

/**
 * Extracts the chat's name.
 */
private fun chatName(chat: ChatPreview, userProvider: UserProvider): String {
    if (chat.hasPersonalChat()) {
        val creator = chat
            .personalChat
            .creator
        val member = chat
            .personalChat
            .member
        val loggedUser = userProvider
            .loggedUser()
            .id
        if (creator.equals(loggedUser)) {
            return userProvider
                .findUser(member)
                .name
        } else {
            return userProvider
                .findUser(creator)
                .name
        }
    }
    return chat
        .groupChat
        .name
}
