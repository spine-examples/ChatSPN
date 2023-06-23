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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.spine.core.UserId
import io.spine.examples.chatspn.ChatId
import io.spine.examples.chatspn.account.UserProfile
import java.awt.Cursor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Represents the page with the user profile.
 */
@Composable
public fun ProfilePage(
    client: DesktopClient,
    profileState: MutableState<UserProfile>,
    chatState: MutableState<ChatData?>,
    onBack: () -> Unit,
    openModal: (content: @Composable () -> Unit) -> Unit,
    closeModal: () -> Unit,
    toRegistration: () -> Unit,
    openChat: (user: UserId) -> Unit
) {
    val model = remember {
        ProfilePageModel(
            client,
            profileState,
            chatState,
            onBack,
            openModal,
            closeModal,
            toRegistration,
            openChat
        )
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        Arrangement.Top,
        Alignment.CenterHorizontally
    ) {
        ProfileTopBar(model)
        ProfilePageContent(model)
    }
}

/**
 * Represents the top bar on the profile tab.
 */
@Composable
private fun ProfileTopBar(model: ProfilePageModel) {
    TopBar {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 6.dp),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            TextButton("< Back") {
                model.onBack()
            }
            Text("Info", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.width(76.dp))
        }
    }
}

/**
 * Represents the main content of the user profile page.
 */
@Composable
private fun ProfilePageContent(model: ProfilePageModel) {
    val user = remember { model.userProfile }
    val chat = remember { model.chatState }
    val isAuthenticatedUser = model.authenticatedUser
        .equals(user.value)
    Column(
        Modifier
            .widthIn(0.dp, 480.dp)
            .padding(16.dp, 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Avatar(96f, user.value.name)
        Spacer(Modifier.height(4.dp))
        Text(
            user.value.name,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(Modifier.height(12.dp))
        if (!isAuthenticatedUser) {
            MessageButton(model)
            Spacer(Modifier.height(8.dp))
        }
        EmailField(user.value.email.value)
        Spacer(Modifier.height(8.dp))
        if (isAuthenticatedUser) {
            LogOutButton(model)
        } else if (chat.value != null) {
            DeleteChatButton(model)
        }
    }
}

/**
 * Represents a button for opening a chat with a user.
 */
@Composable
private fun MessageButton(model: ProfilePageModel) {
    val user = remember { model.userProfile }
    InfoTabButton(
        "Message",
        Icons.Default.Send
    ) {
        model.openChat(user.value.id)
    }
}

/**
 * Represents a button for logging out.
 */
@Composable
private fun LogOutButton(model: ProfilePageModel) {
    InfoTabButton(
        "Log out",
        Icons.Default.ExitToApp,
        MaterialTheme.colorScheme.error
    ) {
        model.openModal(LogoutDialog(model))
    }
}

/**
 * Returns a representation of the logout confirmation modal.
 */
private fun LogoutDialog(
    model: ProfilePageModel,
): @Composable () -> Unit {
    return {
        val viewScope = rememberCoroutineScope { Dispatchers.Default }
        Column(
            Modifier.width(300.dp)
                .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 8.dp)
        ) {
            Text(
                "Are you sure you want to log out?",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                TextButton("Cancel") {
                    model.closeModal()
                }
                TextButton("Log out", MaterialTheme.colorScheme.error) {
                    viewScope.launch {
                        model.logOut()
                    }
                    model.closeModal()
                }
            }
        }
    }
}

/**
 * Represents a button to delete a chat with a user.
 */
@Composable
private fun DeleteChatButton(model: ProfilePageModel) {
    val chat = remember { model.chatState }
    InfoTabButton(
        "Delete chat",
        Icons.Default.Delete,
        MaterialTheme.colorScheme.error
    ) {
        val content = ChatDeletionDialog(
            {
                model.closeModal()
            },
            {
                model.deleteChat(chat.value!!.id)
            },
            chat.value!!
        )
        model.openModal(content)
    }
}

/**
 * Represents an email field.
 */
@Composable
private fun EmailField(email: String) {
    Box(Modifier.clip(MaterialTheme.shapes.small)) {
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .padding(12.dp, 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Email,
                    "Email",
                    Modifier.size(20.dp),
                    MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Email",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                email,
                style = MaterialTheme.typography.bodyLarge
            )
        }

    }
}

/**
 * Represents a button on an info tab.
 */
@Composable
private fun InfoTabButton(
    text: String = "",
    icon: ImageVector? = null,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Box(Modifier.clip(MaterialTheme.shapes.small)) {
        Button(
            onClick,
            Modifier
                .fillMaxWidth()
                .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = contentColor
            ),
            contentPadding = PaddingValues(12.dp, 8.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        icon,
                        text,
                        Modifier.size(20.dp),
                        contentColor
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text,
                    color = contentColor,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

private class ProfilePageModel(
    private val client: DesktopClient,
    val userProfile: MutableState<UserProfile>,
    val chatState: MutableState<ChatData?>,
    val onBack: () -> Unit,
    val openModal: (content: @Composable () -> Unit) -> Unit,
    val closeModal: () -> Unit,
    private val toRegistration: () -> Unit,
    val openChat: (user: UserId) -> Unit
) {
    val authenticatedUser: UserProfile
        get() {
            return client.authenticatedUser!!
        }

    /**
     * Logs out the user and navigates to the registration.
     */
    fun logOut() {
        client.logOut()
        toRegistration()
    }

    /**
     * Deletes the chat.
     *
     * @param chat ID of the chat to delete
     */
    fun deleteChat(chat: ChatId) {
        client.deleteChat(chat)
        chatState.value = null
    }
}
