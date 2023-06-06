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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
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
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.protobuf.Timestamp
import io.spine.core.UserId
import io.spine.examples.chatspn.ChatId
import io.spine.examples.chatspn.MessageId
import io.spine.examples.chatspn.account.UserProfile
import io.spine.examples.chatspn.chat.ChatPreview
import io.spine.examples.chatspn.chat.MessagePreview
import io.spine.examples.chatspn.desktop.DesktopClient
import io.spine.examples.chatspn.message.MessageView
import io.spine.examples.chatspn.message.event.MessageMarkedAsDeleted
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors
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
public fun ChatsPage(client: DesktopClient) {
    val model = remember { ChatsPageModel(client) }
    Row {
        LeftSidebar(model)
        ChatContent(model)
    }
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
                .width(280.dp)
        ) {
            UserProfilePanel(model.authenticatedUser)
            UserSearchField(model)
            ChatList(model)
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
 * Represents the input field to find the user.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserSearchField(model: ChatsPageModel) {
    val viewScope = rememberCoroutineScope { Dispatchers.Default }
    var inputText by remember { model.userSearchFieldState.userEmailState }
    var isError by remember { model.userSearchFieldState.errorState }
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(0.dp, 5.dp),
        value = inputText,
        placeholder = {
            Text(text = "example@mail.com")
        },
        isError = isError,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email Icon"
            )
        },
        singleLine = true,
        onValueChange = {
            inputText = it
            isError = false
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
                        .clickable {
                            val email = inputText
                            viewScope.launch {
                                model.createPersonalChat(email)
                            }
                            inputText = ""
                        }
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
private fun ChatList(model: ChatsPageModel) {
    val chats by model.chats().collectAsState()
    val selectedChat by model.selectedChat().collectAsState()
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
                    model.selectChat(chat.id)
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
        ChatPreviewContent(chatName, lastMessage)
    }
}

/**
 * Represents the chat preview content in the chat preview panel.
 */
@Composable
private fun ChatPreviewContent(chatName: String, lastMessage: String) {
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
            ChatTopbar(model)
            Box(Modifier.weight(1f)) {
                ChatMessages(model)
            }
            SendMessageInput(model)
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
 * Represents the topbar of chat content.
 */
@Composable
private fun ChatTopbar(model: ChatsPageModel) {
    val selectedChat by model.selectedChat().collectAsState()
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
private fun ChatMessages(model: ChatsPageModel) {
    val selectedChat by model.selectedChat().collectAsState()
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatMessage(model: ChatsPageModel, message: MessageData) {
    val isMyMessage = message.sender.id
        .equals(model.authenticatedUser.id)
    val isMenuOpen = remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMyMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            modifier = Modifier
                .padding(4.dp)
                .onClick(
                    enabled = true,
                    matcher = PointerMatcher.mouse(PointerButton.Secondary),
                    onClick = {
                        isMenuOpen.value = true
                    }
                ),
            shape = RoundedCornerShape(size = 20.dp),
            elevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            MessageContent(message)
            MessageDropdownMenu(model, isMenuOpen, message)
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
    message: MessageData
) {
    val viewScope = rememberCoroutineScope { Dispatchers.Default }
    DropdownMenu(
        expanded = isMenuOpen.value,
        onDismissRequest = { isMenuOpen.value = false },
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
    ) {
        MessageMenuItem("Remove", Icons.Default.Delete) {
            viewScope.launch {
                model.removeMessage(message.id)
            }
            isMenuOpen.value = false
        }
    }
}

/**
 * Represents the item of the message's dropdown menu.
 */
@Composable
private fun MessageMenuItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        modifier = Modifier
            .height(30.dp),
        text = {
            Text(
                text,
                style = MaterialTheme.typography.labelMedium
            )
        },
        onClick = onClick,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = text
            )
        }
    )
}

/**
 * Represents the content of the message.
 */
@Composable
private fun MessageContent(message: MessageData) {
    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
        UserAvatar()
        Spacer(Modifier.size(8.dp))
        Column {
            Row {
                SenderName(message.sender.name)
                Spacer(Modifier.size(10.dp))
                PostedTime(message.whenPosted)
            }
            Spacer(Modifier.size(8.dp))
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium
            )
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
        style = MaterialTheme.typography.headlineSmall,
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
 * Represents the input for sending a message to the chat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SendMessageInput(model: ChatsPageModel) {
    val viewScope = rememberCoroutineScope { Dispatchers.Default }
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
                            val messageContent = inputText
                            viewScope.launch {
                                model.sendMessage(messageContent)
                            }
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

/**
 * UI Model for the [ChatsPage].
 *
 * UI Model is a layer between `@Composable` functions and client.
 */
private class ChatsPageModel(private val client: DesktopClient) {
    private val selectedChatState = MutableStateFlow<ChatId>(ChatId.getDefaultInstance())
    private val chatPreviewsState = MutableStateFlow<ChatList>(listOf())
    private val chatMessagesStateMap: MutableMap<ChatId, MutableMessagesState> = mutableMapOf()
    val userSearchFieldState: UserSearchFieldState = UserSearchFieldState()
    val authenticatedUser: UserProfile = client.authenticatedUser!!

    init {
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
     * @param chat ID of the chat to select
     */
    fun selectChat(chat: ChatId) {
        selectedChatState.value = chat
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
        if (chatMessages.contains(message)) {
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
     * Removes message from the selected chat.
     *
     * @param message ID of the message to remove
     */
    public fun removeMessage(message: MessageId) {
        client.removeMessage(selectedChatState.value, message)
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
}

/**
 * Data for the chat preview.
 */
public data class ChatData(
    val id: ChatId,
    val name: String,
    val lastMessage: MessageData?
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
        val lastMessage: MessageData?
        if (chatPreview.lastMessage.equals(MessagePreview.getDefaultInstance())) {
            lastMessage = null
        } else {
            lastMessage = chatPreview.lastMessage.toMessageData(client)
        }
        ChatData(
            chatPreview.id,
            chatPreview.name(client),
            lastMessage
        )
    }.collect(Collectors.toList())
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
