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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.protobuf.Timestamp
import io.spine.core.UserId
import io.spine.examples.chatspn.ChatId
import io.spine.examples.chatspn.account.UserProfile
import io.spine.examples.chatspn.chat.Chat
import io.spine.examples.chatspn.chat.ChatPreview
import io.spine.examples.chatspn.chat.MessagePreview
import java.awt.Cursor
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Represents the current page of the application.
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
            LeftSidebar(model)
            ConfiguredChatPage(client, model)
        }
        Page.PROFILE -> Row {
            LeftSidebar(model)
            ConfiguredProfilePage(client, model)
        }
    }
    CustomModalWindow(model)
}

/**
 * Represents 'Chat' page configured by navigation model
 */
@Composable
private fun ConfiguredChatPage(client: DesktopClient, model: NavigationModel) {
    val chats by model.chats().collectAsState()
    val selectedChat = remember { model.selectedChat() }
    val isChatSelected = chats
        .stream()
        .map { chat -> chat.id }
        .anyMatch { id -> id.equals(selectedChat.value?.id) }
    if (!isChatSelected) {
        ChatNotChosenBox()
    } else {
        ChatPage(
            client,
            model.selectedChat(),
            { content ->
                model.modalWindowState.isOpen.value = true
                model.modalWindowState.content = content
            },
            { model.modalWindowState.isOpen.value = false },
            { model.openChatInfo(it) },
            { model.openUserProfile(it) }
        )
    }
}

/**
 * Represents 'Profile' page configured by navigation model
 */
@Composable
private fun ConfiguredProfilePage(client: DesktopClient, model: NavigationModel) {
    ProfilePage(
        client,
        model.profilePageState.userProfile,
        model.profilePageState.chatState,
        { model.currentPage.value = Page.CHAT },
        {
            model.modalWindowState.isOpen.value = true
            model.modalWindowState.content = it
        },
        { model.modalWindowState.clear() },
        { model.currentPage.value = Page.REGISTRATION },
        { model.selectPersonalChat(it) }
    )
}

/**
 * Represents a modal window with content specified in the navigation model.
 */
@Composable
private fun CustomModalWindow(model: NavigationModel) {
    val isOpen = remember { model.modalWindowState.isOpen }
    ModalWindow(
        isOpen,
    ) {
        model.modalWindowState.content()
    }
}

/**
 * Represents the left sidebar.
 */
@Composable
private fun LeftSidebar(model: NavigationModel) {
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
        modifier = Modifier
            .size(42.dp),
        onClick = {
            if (isUserProfileOpen && isAuthenticatedUser) {
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
 * Represents the input field to find the user.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun UserSearchField(model: NavigationModel) {
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
            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
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
private fun ChatList(model: NavigationModel) {
    val chats by model.chats().collectAsState()
    val selectedChat = model.selectedChat()
    LazyColumn(
        Modifier.fillMaxSize()
    ) {
        chats.forEachIndexed { index, chat ->
            item(key = index) {
                ChatPreviewPanel(
                    chat.name,
                    chat.lastMessage,
                    chat.id.equals(selectedChat.value?.id)
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
 * UI Model for the [Navigation].
 */
private class NavigationModel(private val client: DesktopClient) {
    private val selectedChatState: MutableState<ChatData?> = mutableStateOf(null)
    private val chatPreviewsState = MutableStateFlow<ChatList>(listOf())
    val modalWindowState: ModalWindowState = ModalWindowState()
    val userSearchFieldState: UserSearchFieldState = UserSearchFieldState()
    val currentPage: MutableState<Page> = mutableStateOf(Page.REGISTRATION)
    val profilePageState: ProfilePageState = ProfilePageState()
    val authenticatedUser: UserProfile
        get() {
            return client.authenticatedUser!!
        }

    /**
     * Reads chat previews and subscribes to their updates.
     */
    fun observeChats() {
        updateChats(client.readChats().toChatDataList(client))
        client.observeChats { state -> updateChats(state.chatList.toChatDataList(client)) }
    }

    /**
     * Returns the state of the user's chats.
     */
    fun chats(): StateFlow<ChatList> {
        return chatPreviewsState
    }

    /**
     * Returns the state of the selected chat.
     */
    fun selectedChat(): MutableState<ChatData?> {
        return selectedChatState
    }

    /**
     * Selects provided chat and opens the 'Chat' page.
     *
     * @param chat ID of the chat to select
     */
    fun selectChat(chat: ChatId) {
        selectedChatState.value = getChatData(chat)
        currentPage.value = Page.CHAT
        profilePageState.clear()
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
     * Selects the personal chat between the authenticated and the provided user,
     * if the chat doesn't exist, creates it.
     *
     * @param user ID of the user with whom to select a personal chat
     */
    fun selectPersonalChat(user: UserId) {
        val chat = findPersonalChat(user)
        if (chat != null) {
            selectChat(chat.id)
        } else {
            client.createPersonalChat(user) { event ->
                selectChat(event.id)
            }
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
     * Returns the data of the personal chat with the provided user,
     * or `null` if the chat doesn't exist.
     *
     * @param user ID of the user with whom to find the personal chat
     */
    private fun findPersonalChat(user: UserId): ChatData? {
        val chat = chatPreviewsState.value.find { chatData ->
            chatData.type == Chat.ChatType.CT_PERSONAL && chatData.members.contains(user)
        }
        return chat
    }

    /**
     * Opens a page with a user profile.
     *
     * @param userId id of the user to open profile
     */
    fun openUserProfile(userId: UserId) {
        val user = client.findUser(userId)
        profilePageState.userProfile.value = user ?: UserProfile.getDefaultInstance()
        profilePageState.chatState.value = if (null == user) null else findPersonalChat(user.id)
        currentPage.value = Page.PROFILE
    }

    /**
     * Opens a page with chat info. If the chat is personal, a user profile will be opened.
     *
     * @param chatId ID of the chat which info to open
     */
    fun openChatInfo(chatId: ChatId) {
        val chat = chatPreviewsState.value.find { chatData -> chatId.equals(chatData.id) } ?: return
        if (chat.type == Chat.ChatType.CT_PERSONAL) {
            val userId = chat.members.find { user -> !user.equals(authenticatedUser.id) }
            val user = client.findUser(userId!!)
            profilePageState.userProfile.value = user!!
            profilePageState.chatState.value = getChatData(chat.id)
            currentPage.value = Page.PROFILE
        }
    }
}

/**
 * State of the modal window with the user profile.
 */
private class ProfilePageState {
    val userProfile: MutableState<UserProfile> = mutableStateOf(UserProfile.getDefaultInstance())
    val chatState: MutableState<ChatData?> = mutableStateOf(null)

    /**
     * Clears the state.
     */
    fun clear() {
        userProfile.value = UserProfile.getDefaultInstance()
        chatState.value = null
    }
}

/**
 * State of the user search field.
 */
private class UserSearchFieldState {
    val userEmailState: MutableState<String> = mutableStateOf("")
    val errorState: MutableState<Boolean> = mutableStateOf(false)
}

/**
 * State of the modal window.
 */
private class ModalWindowState {
    val isOpen: MutableState<Boolean> = mutableStateOf(false)
    var content: @Composable () -> Unit = {}

    /**
     * Clears the state.
     */
    fun clear() {
        isOpen.value = false
        content = { }
    }
}

/**
 * Converts `Timestamp` to the `hh:mm` string.
 */
public fun Timestamp.toStringTime(): String {
    val date = Date(this.seconds * 1000)
    val format = SimpleDateFormat("hh:mm", Locale.getDefault())
    return format.format(date)
}

/**
 * Pages in the application.
 */
private enum class Page {
    CHAT, LOGIN, REGISTRATION, PROFILE
}

/**
 * Data for the chat preview.
 */
public data class ChatData(
    val id: ChatId,
    val name: String,
    val lastMessage: MessageData?,
    val members: List<UserId>,
    val type: Chat.ChatType
)

/**
 * List of `ChatData`.
 */
public typealias ChatList = List<ChatData>

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
private fun ChatPreview.type(): Chat.ChatType {
    return if (this.hasGroupChat()) {
        Chat.ChatType.CT_GROUP
    } else {
        Chat.ChatType.CT_PERSONAL
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
