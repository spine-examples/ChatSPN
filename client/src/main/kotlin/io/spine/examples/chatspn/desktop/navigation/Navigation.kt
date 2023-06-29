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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.spine.examples.chatspn.desktop.DesktopClient
import io.spine.examples.chatspn.desktop.LoginPage
import io.spine.examples.chatspn.desktop.RegistrationPage
import io.spine.examples.chatspn.desktop.chat.ChatPage
import io.spine.examples.chatspn.desktop.profile.ProfilePage

/**
 * Displays the current page of the application.
 *
 * It will be recomposed when the page changes.
 */
@Composable
public fun CurrentPage(client: DesktopClient) {
    val model = remember { NavigationModel(client) }
    val currentPage = remember { model.currentPage }
    when (currentPage.value) {
        Page.REGISTRATION -> RegistrationPage(
            client,
            toLogin = { currentPage.value = Page.LOGIN },
            toChats = {
                currentPage.value = Page.CHAT
                model.observeChats()
            }
        )
        Page.LOGIN -> LoginPage(
            client,
            toRegistration = { currentPage.value = Page.REGISTRATION },
            toChats = {
                currentPage.value = Page.CHAT
                model.observeChats()
            }
        )
        Page.CHAT -> Row {
            NavigationBar(model)
            ConfiguredChatPage(client, model)
        }
        Page.PROFILE -> Row {
            NavigationBar(model)
            ConfiguredProfilePage(client, model)
        }
    }
}

/**
 * Displays 'Chat' page configured by navigation model.
 */
@Composable
private fun ConfiguredChatPage(client: DesktopClient, model: NavigationModel) {
    val chats by model.chats().collectAsState()
    val selectedChat = remember { model.selectedChat }
    val chatData = model.chatData(selectedChat.value)
    val isChatSelected = chats
        .stream()
        .map { chat -> chat.id }
        .anyMatch { id -> id.equals(selectedChat.value) }
    if (!isChatSelected) {
        ChatNotChosenBox()
    } else {
        ChatPage(
            client,
            chatData!!,
            { model.openChatInfo(it) },
            { model.openUserProfile(it) }
        )
    }
}

/**
 * Displays content when no one chat is selected.
 */
@Composable
private fun ChatNotChosenBox() {
    Box(
        Modifier.fillMaxSize(),
        Alignment.Center
    ) {
        Text(
            text = "Choose a chat",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSecondary,
        )
    }
}

/**
 * Displays 'Profile' page configured by navigation model.
 */
@Composable
private fun ConfiguredProfilePage(client: DesktopClient, model: NavigationModel) {
    ProfilePage(
        client,
        model.profilePageState.userProfile,
        model.profilePageState.chatState,
        { model.currentPage.value = Page.CHAT },
        { model.currentPage.value = Page.REGISTRATION },
        { model.selectPersonalChat(it) }
    )
}
