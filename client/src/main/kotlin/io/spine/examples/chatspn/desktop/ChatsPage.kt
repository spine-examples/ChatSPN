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
import androidx.compose.foundation.Image
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.google.protobuf.Timestamp
import io.spine.core.UserId
import io.spine.examples.chatspn.ChatId
import io.spine.examples.chatspn.MessageId
import io.spine.examples.chatspn.account.UserProfile
import io.spine.examples.chatspn.chat.Chat.ChatType
import io.spine.examples.chatspn.chat.Chat.ChatType.CT_GROUP
import io.spine.examples.chatspn.chat.Chat.ChatType.CT_PERSONAL
import io.spine.examples.chatspn.chat.ChatPreview
import io.spine.examples.chatspn.chat.MessagePreview
import io.spine.examples.chatspn.message.MessageView
import io.spine.examples.chatspn.message.event.MessageMarkedAsDeleted
import java.awt.Cursor
import java.awt.Cursor.getPredefinedCursor
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Represents the 'Chats' page in the application.
 *
 * @param client desktop client
 */
@Composable
@Preview
public fun ChatsPage(client: DesktopClient, toRegistration: () -> Unit) {
    val model = remember { ChatsPageModel(client, toRegistration) }
    val isProfileTabOpen by remember { model.profileInfoTabState.isVisibleState }
    Row {
        LeftSidebar(model)
        if (isProfileTabOpen) {
            ProfileTab(model)
        } else {
            ChatContent(model)
        }
    }
    ChatDeletionDialog(model)
    LogoutDialog(model)
}

/**
 * Represents the left sidebar on the `Chats` page.
 */
@Composable
private fun LeftSidebar(model: ChatsPageModel) {
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
 * Represents the menu button.
 */
@Composable
private fun MenuButton(model: ChatsPageModel) {
    val isUserProfileOpen by remember { model.profileInfoTabState.isVisibleState }
    val isAuthenticatedUser = model.authenticatedUser
        .equals(model.profileInfoTabState.userProfile.value)
    val containerColor = if (isUserProfileOpen && isAuthenticatedUser)
        MaterialTheme.colorScheme.inverseSurface
    else
        MaterialTheme.colorScheme.surface
    Button(
        modifier = Modifier
            .size(42.dp),
        onClick = {
            if (isUserProfileOpen && isAuthenticatedUser) {
                model.profileInfoTabState.clear()
            } else {
                model.openUserProfileTab(model.authenticatedUser)
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
 * Represents the input field to find the user.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun UserSearchField(model: ChatsPageModel) {
    val viewScope = rememberCoroutineScope { Dispatchers.Default }
    var inputText by remember { model.userSearchFieldState.userEmailState }
    var isError by remember { model.userSearchFieldState.errorState }
    val onSearch = {
        val email = inputText
        viewScope.launch {
            if (email.trim().isNotEmpty()) {
                model.createPersonalChat(email.trim())
            }
        }
        inputText = ""
    }
    BasicTextField(
        modifier = Modifier
            .size(182.dp, 30.dp)
            .background(MaterialTheme.colorScheme.background)
            .onPreviewKeyEvent {
                when {
                    (it.key == Key.Enter) -> {
                        onSearch()
                        true
                    }
                    else -> false
                }
            },
        value = inputText,
        singleLine = true,
        onValueChange = {
            inputText = it
            isError = false
        }
    ) { innerTextField ->
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(size = 3.dp)
                )
                .padding(all = 8.dp)
        ) {
            if (inputText.isEmpty()) {
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f)) {
                    innerTextField()
                }
                if (inputText.trim().isNotEmpty()) {
                    SearchIcon(onSearch)
                }
            }
        }
    }
}

/**
 * Represents the icon button for the `UserSearchField`.
 */
@Composable
private fun SearchIcon(onSearch: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Icon(
        Icons.Default.Search,
        "Search",
        Modifier
            .padding(horizontal = 4.dp)
            .pointerHoverIcon(PointerIcon(getPredefinedCursor(Cursor.HAND_CURSOR)))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onSearch
            ),
        MaterialTheme.colorScheme.onSecondary
    )
}

/**
 * Represents the list of chat previews.
 */
@Composable
private fun ChatList(model: ChatsPageModel) {
    val chats by model.chats().collectAsState()
    val selectedChat by model.selectedChat().collectAsState()
    LazyColumn(
        Modifier.fillMaxSize()
    ) {
        chats.forEachIndexed { index, chat ->
            item(key = index) {
                ChatPreviewPanel(
                    chat.name,
                    chat.lastMessage,
                    chat.id.equals(selectedChat)
                ) {
                    model.selectChat(chat.id)
                }
            }
        }
    }
}

/**
 * Represents the chat preview in the chats list.
 */
@Composable
private fun ChatPreviewPanel(
    chatName: String,
    lastMessage: MessageData?,
    isSelected: Boolean,
    select: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clickable { select() }
            .background(
                color = if (isSelected)
                    MaterialTheme.colorScheme.inverseSurface
                else
                    MaterialTheme.colorScheme.background
            ),
        Alignment.CenterStart,
    ) {
        ChatPreviewContent(chatName, lastMessage)
    }
}

/**
 * Represents the chat preview content in the chat preview panel.
 */
@Composable
private fun ChatPreviewContent(chatName: String, lastMessage: MessageData?) {
    Row(
        Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(46f, chatName)
        Spacer(Modifier.size(12.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween
            ) {
                Text(
                    text = chatName,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = if (lastMessage == null) "" else lastMessage.whenPosted.toStringTime(),
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(Modifier.size(9.dp))
            Text(
                text = if (lastMessage == null) ""
                else lastMessage.content.replace("\\s".toRegex(), " "),
                color = MaterialTheme.colorScheme.onSecondary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Represents the default avatar.
 */
@Composable
public fun Avatar(size: Float, name: String, modifierExtender: Modifier.() -> Modifier = { this }) {
    val gradients = listOf(
        listOf(Color(0xFFFFC371), Color(0xFFFF5F6D)),
        listOf(Color(0xFF95E4FC), Color(0xFF0F65D6)),
        listOf(Color(0xFF72F877), Color(0xFF259CF1)),
        listOf(Color(0xFF76DBFA), Color(0xFFD72DFD)),
        listOf(Color(0xFFA6FA85), Color(0xFF22AC00D)),
        listOf(Color(0xFFFD9696), Color(0xFFF11010))
    )
    val gradientIndex = abs(name.hashCode()) % gradients.size
    Box(contentAlignment = Alignment.Center) {
        Image(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .modifierExtender(),
            contentScale = ContentScale.Crop,
            painter = object : Painter() {
                override val intrinsicSize: Size = Size(size, size)
                override fun DrawScope.onDraw() {
                    drawRect(
                        Brush.linearGradient(gradients[gradientIndex]),
                        size = Size(size * 4, size * 4)
                    )
                }
            },
            contentDescription = "User picture"
        )
        Text(
            name[0].toString(),
            color = Color.White,
            fontSize = (size * 0.5).sp,
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

/**
 * Represents the content of the selected chat.
 */
@Composable
private fun ChatContent(model: ChatsPageModel) {
    val selectedChat by model.selectedChat().collectAsState()
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
            ChatTopBar(model)
            Box(Modifier.weight(1f)) {
                ChatMessages(model)
            }
            ChatBottomBar(model)
        }
    }
}

/**
 * Represents content when no one chat is selected.
 */
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
 * Represents the info tab with the user profile.
 */
@Composable
private fun ProfileTab(model: ChatsPageModel) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        Arrangement.Top,
        Alignment.CenterHorizontally
    ) {
        ProfileTopBar(model)
        ProfileTabContent(model)
    }
}

/**
 * Represents the main content of the user profile tab.
 */
@Composable
private fun ProfileTabContent(model: ChatsPageModel) {
    val user by remember { model.profileInfoTabState.userProfile }
    if (null == user) {
        model.profileInfoTabState.isVisibleState.value = false
        return
    }
    val chat by remember { model.profileInfoTabState.chatState }
    val isAuthenticatedUser = model.authenticatedUser
        .equals(model.profileInfoTabState.userProfile.value)
    Column(
        Modifier
            .widthIn(0.dp, 480.dp)
            .padding(16.dp, 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Avatar(96f, user!!.name)
        Spacer(Modifier.height(4.dp))
        Text(
            user!!.name,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(Modifier.height(12.dp))
        if (!isAuthenticatedUser) {
            ProfileTabMessageButton(model)
            Spacer(Modifier.height(8.dp))
        }
        ProfileTabEmailField(user!!.email.value)
        Spacer(Modifier.height(8.dp))
        if (isAuthenticatedUser) {
            ProfileTabLogOutButton(model)
        } else if (null != chat) {
            ProfileTabDeleteChatButton(model)
        }
    }
}

/**
 * Represents a button for opening a chat with a user on the user's profile tab.
 */
@Composable
private fun ProfileTabMessageButton(model: ChatsPageModel) {
    val user by remember { model.profileInfoTabState.userProfile }
    val chat by remember { model.profileInfoTabState.chatState }
    InfoTabButton(
        "Message",
        Icons.Default.Send
    ) {
        if (null != chat) {
            model.selectChat(chat!!.id)
        } else {
            model.createPersonalChat(user!!.id)
        }
    }
}

/**
 * Represents a button for logging out on the authenticated user profile tab.
 */
@Composable
private fun ProfileTabLogOutButton(model: ChatsPageModel) {
    InfoTabButton(
        "Log out",
        Icons.Default.ExitToApp,
        MaterialTheme.colorScheme.error
    ) {
        model.isLogoutDialogVisible.value = true
    }
}

/**
 * Represents a button to delete a chat with a user on the user profile tab.
 */
@Composable
private fun ProfileTabDeleteChatButton(model: ChatsPageModel) {
    val chat by remember { model.profileInfoTabState.chatState }
    InfoTabButton(
        "Delete chat",
        Icons.Default.Delete,
        MaterialTheme.colorScheme.error
    ) {
        model.chatInDeletionState.value = chat
    }
}

/**
 * Represents an email field on the user profile tab.
 */
@Composable
private fun ProfileTabEmailField(email: String) {
    Box(Modifier.clip(RoundedCornerShape(8.dp))) {
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
    Box(Modifier.clip(RoundedCornerShape(8.dp))) {
        Button(
            onClick,
            Modifier
                .fillMaxWidth()
                .pointerHoverIcon(PointerIcon(getPredefinedCursor(Cursor.HAND_CURSOR))),
            shape = RoundedCornerShape(8.dp),
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

/**
 * Represents the top bar on the profile tab.
 */
@Composable
private fun ProfileTopBar(model: ChatsPageModel) {
    TopBar {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 6.dp),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            TextButton("< Back") {
                model.profileInfoTabState.clear()
            }
            Text("Info", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.width(76.dp))
        }
    }
}

/**
 * Represents the top bar of chat content.
 */
@Composable
private fun ChatTopBar(model: ChatsPageModel) {
    val selectedChat by model.selectedChat().collectAsState()
    val chat = model.getChatData(selectedChat)!!
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
                        model.openChatInfoTab(chat.id)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(42f, chat.name)
                Text(
                    chat.name,
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            ChatMoreButton(model, chat)
        }
    }
}

/**
 * Represents the top bar with the configurable content.
 */
@Composable
private fun TopBar(content: @Composable () -> Unit) {
    Surface(
        Modifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(bottom = 1.dp)
            .drawBehind {
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            },
    ) {
        content()
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
            model.chatInDeletionState.value = chat
            isMenuOpen.value = false
        }
    }
}

/**
 * Represents a chat deletion confirmation modal.
 */
@Composable
private fun ChatDeletionDialog(
    model: ChatsPageModel,
) {
    val viewScope = rememberCoroutineScope { Dispatchers.Default }
    val chat by remember { model.chatInDeletionState }
    val isVisible = remember { mutableStateOf(false) }
    isVisible.value = model.chatInDeletionState.value != null
    ModalWindow(isVisible, { model.chatInDeletionState.value = null }) {
        Column(
            Modifier.width(300.dp)
                .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 8.dp)
        ) {
            Text(
                buildAnnotatedString {
                    append("Are you sure you want to delete chat ")
                    if (chat?.type == CT_PERSONAL) {
                        append("with ")
                    }
                    append(
                        AnnotatedString(
                            chat?.name ?: "",
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
                    model.chatInDeletionState.value = null
                }
                TextButton("Delete", MaterialTheme.colorScheme.error) {
                    val id = chat!!.id
                    viewScope.launch {
                        model.deleteChat(id)
                    }
                    model.chatInDeletionState.value = null
                }
            }
        }
    }
}

/**
 * Represents a logout confirmation modal.
 */
@Composable
private fun LogoutDialog(
    model: ChatsPageModel,
) {
    val viewScope = rememberCoroutineScope { Dispatchers.Default }
    val isVisible = remember { model.isLogoutDialogVisible }
    ModalWindow(isVisible, { isVisible.value = false }) {
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
                    isVisible.value = false
                }
                TextButton("Log out", MaterialTheme.colorScheme.error) {
                    viewScope.launch {
                        model.logOut()
                    }
                    isVisible.value = false
                }
            }
        }
    }
}

/**
 * Represents the text button without a background and with an optional icon.
 *
 * @param text text on the button
 * @param contentColor color of the text and icon
 * @param icon icon to be displayed before text
 * @param onClick callback that will be triggered when the button clicked
 */
@Composable
private fun TextButton(
    text: String,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Button(
        onClick,
        Modifier.pointerHoverIcon(PointerIcon(getPredefinedCursor(Cursor.HAND_CURSOR))),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(16.dp, 4.dp)
    ) {
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
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Represents the list of messages in the chat.
 */
@Composable
private fun ChatMessages(model: ChatsPageModel) {
    val selectedChat by model.selectedChat().collectAsState()
    val messages by model
        .messages(selectedChat)
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
                        model.openUserProfileTab(user)
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
 * Converts `Timestamp` to the `hh:mm` string.
 */
private fun Timestamp.toStringTime(): String {
    val date = Date(this.seconds * 1000)
    val format = SimpleDateFormat("hh:mm", Locale.getDefault())
    return format.format(date)
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
 * Represents the default modal window.
 *
 * @param isVisibleState mutable state of modal window visibility
 * @param content content of the modal window
 */
@Composable
private fun ModalWindow(
    isVisibleState: MutableState<Boolean>,
    onDismiss: () -> Unit = {},
    content: @Composable () -> Unit
) {
    if (isVisibleState.value) {
        ShadedBackground()
        Popup(
            alignment = Alignment.Center,
            onDismissRequest = {
                isVisibleState.value = false
                onDismiss()
            },
            focusable = true
        ) {
            Surface(
                elevation = 8.dp,
                shape = RoundedCornerShape(10)
            ) {
                content()
            }
        }
    }
}

/**
 * Represents the partially transparent black background.
 *
 * The user cannot click on elements behind it.
 */
@Composable
private fun ShadedBackground() {
    Popup {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0x33000000))
        )
    }
}

/**
 * UI Model for the [ChatsPage].
 *
 * UI Model is a layer between `@Composable` functions and client.
 */
private class ChatsPageModel(
    private val client: DesktopClient,
    private val toRegistration: () -> Unit
) {
    private val selectedChatState = MutableStateFlow<ChatId>(ChatId.getDefaultInstance())
    private val chatPreviewsState = MutableStateFlow<ChatList>(listOf())
    private val chatMessagesStateMap: MutableMap<ChatId, MutableMessagesState> = mutableMapOf()
    val userSearchFieldState: UserSearchFieldState = UserSearchFieldState()
    val messageInputFieldState: MessageInputFieldState = MessageInputFieldState()
    val chatInDeletionState: MutableState<ChatData?> = mutableStateOf(null)
    val isLogoutDialogVisible: MutableState<Boolean> = mutableStateOf(false)
    val profileInfoTabState: ProfileInfoTabState = ProfileInfoTabState()
    val authenticatedUser: UserProfile = client.authenticatedUser!!

    init {
        updateChats(client.readChats().toChatDataList(client))
        client.observeChats { state -> updateChats(state.chatList.toChatDataList(client)) }
    }

    /**
     * Logs out the user and navigates to the registration.
     */
    fun logOut() {
        client.logOut()
        toRegistration()
    }

    /**
     * Returns the state of the user's chats.
     */
    fun chats(): StateFlow<ChatList> {
        return chatPreviewsState
    }

    /**
     * Returns the state of messages in the chat.
     *
     * @param chat ID of the chat to get messages from
     */
    fun messages(chat: ChatId): MessagesState {
        if (!chatMessagesStateMap.containsKey(chat)) {
            throw IllegalStateException("Chat not found")
        }
        return chatMessagesStateMap[chat]!!
    }

    /**
     * Returns the state of the selected chat.
     */
    fun selectedChat(): StateFlow<ChatId> {
        return selectedChatState
    }

    /**
     * Selects provided chat and subscribes to message changes in it.
     *
     * Also clears the message input field state.
     *
     * @param chat ID of the chat to select
     */
    fun selectChat(chat: ChatId) {
        selectedChatState.value = chat
        messageInputFieldState.clear()
        profileInfoTabState.clear()
        updateMessages(chat, client.readMessages(chat).toMessageDataList(client))
        client.stopObservingMessages()
        client.observeMessages(
            chat,
            { messageView -> chat.updateMessagesState(messageView) },
            { messageDeleted -> chat.updateMessagesState(messageDeleted) })
    }

    /**
     * Updates the state of chat messages by adding a new message,
     * or editing existing one if message ID matches.
     *
     * @param messageView message to update the state
     */
    private fun ChatId.updateMessagesState(messageView: MessageView) {
        val message = messageView.toMessageData(client)
        val chatMessages = chatMessagesStateMap[this]!!.value
        if (chatMessages.findMessage(message.id) != null) {
            val newChatMessages = chatMessages.replaceMessage(message)
            updateMessages(this, newChatMessages)
        } else {
            updateMessages(this, chatMessages + message)
        }
    }

    /**
     * Updates the state of chat messages by removing a message.
     *
     * @param messageDeleted event about message deletion
     */
    private fun ChatId.updateMessagesState(messageDeleted: MessageMarkedAsDeleted) {
        val chatMessages = chatMessagesStateMap[this]!!.value
        val message = chatMessages.findMessage(messageDeleted.id)
        if (message != null) {
            val messageIndex = chatMessages.indexOf(message)
            val newChatMessages = chatMessages.remove(messageIndex)
            updateMessages(this, newChatMessages)
        }
    }

    /**
     * Finds user by ID.
     *
     * @param id ID of the user to find
     * @return found user profile or `null` if user is not found
     */
    fun findUser(id: UserId): UserProfile? {
        return client.findUser(id)
    }

    /**
     * Finds user by email.
     *
     * @param email email of the user to find
     * @return found user profile or `null` if user not found
     */
    fun findUser(email: String): UserProfile? {
        return client.findUser(email)
    }

    /**
     * Sends a message to the selected chat.
     *
     * @param content message text content
     */
    fun sendMessage(content: String) {
        client.sendMessage(selectedChatState.value, content)
    }

    /**
     * Deletes the chat.
     *
     * @param chat ID of the chat to delete
     */
    fun deleteChat(chat: ChatId) {
        client.deleteChat(chat)
        if (profileInfoTabState.chatState.value?.id?.equals(chat) ?: false) {
            profileInfoTabState.chatState.value = null
        }
    }

    /**
     * Removes message from the selected chat.
     *
     * @param message ID of the message to remove
     */
    fun removeMessage(message: MessageId) {
        client.removeMessage(selectedChatState.value, message)
    }

    /**
     * Edits message in the selected chat.
     *
     * @param message ID of the message to edit
     * @param newContent new text content for the message
     */
    fun editMessage(message: MessageId, newContent: String) {
        client.editMessage(selectedChatState.value, message, newContent)
    }

    /**
     * Creates the personal chat between the authenticated and the provided user.
     *
     * Selects the created chat.
     *
     * @param user ID of the user with whom to create a personal chat
     */
    fun createPersonalChat(user: UserId) {
        client.createPersonalChat(user) { event ->
            selectChat(event.id)
        }
    }

    /**
     * Finds user by email and creates the personal chat between authenticated and found user.
     *
     * @param email email of the user with whom to create a personal chat
     */
    fun createPersonalChat(email: String) {
        val user = client.findUser(email)
        if (null != user) {
            client.createPersonalChat(user.id)
        } else {
            userSearchFieldState.errorState.value = true
        }
    }

    /**
     * Opens a tab with a user profile.
     *
     * @param user profile of the user to open
     */
    fun openUserProfileTab(user: UserProfile) {
        profileInfoTabState.isVisibleState.value = true
        profileInfoTabState.userProfile.value = user
        profileInfoTabState.chatState.value = findPersonalChat(user.id)
    }

    /**
     * Opens a tab with chat info. If the chat is personal, a user profile will be opened.
     *
     * @param chatId ID of the chat which info to open
     */
    fun openChatInfoTab(chatId: ChatId) {
        val chat = chatPreviewsState.value.find { chatData -> chatId.equals(chatData.id) } ?: return
        if (chat.type == CT_PERSONAL) {
            val userId = chat.members.find { user -> !user.equals(authenticatedUser.id) }
            val user = client.findUser(userId!!)
            profileInfoTabState.isVisibleState.value = true
            profileInfoTabState.userProfile.value = user
            profileInfoTabState.chatState.value = getChatData(chat.id)
        }
    }

    /**
     * Returns the data of the personal chat with the provided user,
     * or `null` if the chat doesn't exist.
     *
     * @param user ID of the user with whom to find the personal chat
     */
    private fun findPersonalChat(user: UserId): ChatData? {
        val chat = chatPreviewsState.value.find { chatData ->
            chatData.type == CT_PERSONAL && chatData.members.contains(user)
        }
        return chat
    }

    /**
     * Returns the data of the chat by id, or `null` if the chat doesn't exist.
     *
     * @param chatId ID of the chat
     */
    fun getChatData(chatId: ChatId): ChatData? {
        val chat = chatPreviewsState.value.find { chatData ->
            chatData.id.equals(chatId)
        }
        return chat
    }

    /**
     * Updates the model with new chats.
     *
     * @param chats new list of user chats
     */
    private fun updateChats(chats: ChatList) {
        chatPreviewsState.value = chats
    }

    /**
     * Updates the model with new messages.
     *
     * @param chat ID of the chat to update messages in
     * @param messages new list of messages in the chat
     */
    private fun updateMessages(chat: ChatId, messages: MessageList) {
        if (chatMessagesStateMap.containsKey(chat)) {
            chatMessagesStateMap[chat]!!.value = messages
        } else {
            chatMessagesStateMap[chat] = MutableStateFlow(messages)
        }
    }

    /**
     * State of the user search field.
     */
    class UserSearchFieldState {
        val userEmailState: MutableState<String> = mutableStateOf("")
        val errorState: MutableState<Boolean> = mutableStateOf(false)
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

    /**
     * State of the modal window with the user profile.
     */
    class ProfileInfoTabState {
        val userProfile: MutableState<UserProfile?> = mutableStateOf(null)
        val chatState: MutableState<ChatData?> = mutableStateOf(null)
        val isVisibleState: MutableState<Boolean> = mutableStateOf(false)

        /**
         * Clears the state.
         */
        fun clear() {
            userProfile.value = null
            chatState.value = null
            isVisibleState.value = false
        }
    }
}

/**
 * Data for the chat preview.
 */
public data class ChatData(
    val id: ChatId,
    val name: String,
    val lastMessage: MessageData?,
    val members: List<UserId>,
    val type: ChatType
)

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
 * Creates the `ChatData` list from the `ChatPreview` list.
 *
 * @param client desktop client to find user profiles
 */
private fun List<ChatPreview>.toChatDataList(client: DesktopClient): ChatList {
    return this.stream().map { chatPreview ->
        ChatData(
            chatPreview.id,
            chatPreview.name(client),
            chatPreview.lastMessageData(client),
            chatPreview.members(client),
            chatPreview.type()
        )
    }.collect(Collectors.toList())
}

/**
 * Retrieves the last message from the `ChatPreview` and creates the `MessageData` from it.
 */
private fun ChatPreview.lastMessageData(client: DesktopClient): MessageData? {
    val isMessageDefault = this.lastMessage.equals(MessagePreview.getDefaultInstance())
    return if (isMessageDefault) {
        null
    } else {
        this.lastMessage.toMessageData(client)
    }
}

/**
 * Retrieves chat type.
 */
private fun ChatPreview.type(): ChatType {
    return if (this.hasGroupChat()) {
        CT_GROUP
    } else {
        CT_PERSONAL
    }
}

/**
 * Retrieves members of the chat.
 */
private fun ChatPreview.members(client: DesktopClient): List<UserId> {
    return client.readChatMembers(this.id)
}

/**
 * Creates the `MessageData` from the `MessagePreview`.
 *
 * @param client desktop client to find user profiles
 */
private fun MessagePreview.toMessageData(client: DesktopClient): MessageData {
    return MessageData(
        this.id,
        client.findUser(this.user)!!,
        this.content,
        this.whenPosted
    )
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
 * Retrieves the display name of the chat.
 *
 * @param client desktop client to find user profiles
 */
private fun ChatPreview.name(client: DesktopClient): String {
    if (this.hasGroupChat()) {
        return this.groupChat.name
    }
    val creator = this.personalChat.creator
    val member = this.personalChat.member
    if (client.authenticatedUser!!.id.equals(creator)) {
        return client.findUser(member)!!.name
    }
    return client.findUser(creator)!!.name
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
 * List of `ChatData`.
 */
public typealias ChatList = List<ChatData>

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
