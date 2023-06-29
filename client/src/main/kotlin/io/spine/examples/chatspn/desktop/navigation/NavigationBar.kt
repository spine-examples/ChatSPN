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

package io.spine.examples.chatspn.desktop.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Displays the left sidebar.
 */
@Composable
public fun NavigationBar(model: NavigationModel) {
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
                .width(256.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.width(10.dp))
                MenuButton(model)
                Spacer(Modifier.width(14.dp))
                UserSearchField(model)
            }
            ChatList(model)
        }
    }
}

/**
 * Displays the menu button.
 */
@Composable
private fun MenuButton(model: NavigationModel) {
    val currentPage = remember { model.currentPage }
    val isUserProfileOpen = currentPage.value == Page.PROFILE
    val isAuthenticatedUser = model.authenticatedUser
        .equals(model.profilePageState.userProfile.value)
    val containerColor = if (isUserProfileOpen && isAuthenticatedUser)
        MaterialTheme.colorScheme.inverseSurface
    else
        MaterialTheme.colorScheme.surface
    Button(
        modifier = Modifier.size(42.dp),
        onClick = {
            if (isUserProfileOpen && isAuthenticatedUser) {
                model.currentPage.value = Page.CHAT
                model.profilePageState.clear()
            } else {
                model.openUserProfile(model.authenticatedUser.id)
            }
        },
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor
        ),
        contentPadding = PaddingValues(6.dp)
    ) {
        Icon(
            Icons.Default.Menu,
            "Menu",
            Modifier.size(20.dp),
            MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * Displays the input field to find the user.
 */
@Composable
private fun UserSearchField(model: NavigationModel) {
    val viewScope = rememberCoroutineScope { Dispatchers.Default }
    val inputText = remember { model.userSearchFieldState.userEmailState }
    val onSearch = {
        val email = inputText.value
        viewScope.launch {
            if (email.trim().isNotEmpty()) {
                model.createPersonalChat(email.trim())
            }
        }
        inputText.value = ""
    }
    SearchField(inputText, onSearch)
}

/**
 * Displays the list of chat previews.
 */
@Composable
private fun ChatList(model: NavigationModel) {
    val chats by model.chats().collectAsState()
    val selectedChat = model.selectedChat
    LazyColumn(
        Modifier.fillMaxSize()
    ) {
        chats.forEachIndexed { index, chat ->
            item(key = index) {
                ChatPreviewPanel(
                    chat,
                    chat.id.equals(selectedChat.value)
                ) {
                    model.selectChat(chat.id)
                }
            }
        }
    }
}
