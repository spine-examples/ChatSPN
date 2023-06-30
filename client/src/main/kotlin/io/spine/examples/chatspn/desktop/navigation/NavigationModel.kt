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

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.spine.core.UserId
import io.spine.examples.chatspn.ChatId
import io.spine.examples.chatspn.account.UserProfile
import io.spine.examples.chatspn.chat.Chat
import io.spine.examples.chatspn.chat.ChatPreview
import io.spine.examples.chatspn.chat.MessagePreview
import io.spine.examples.chatspn.desktop.DesktopClient
import io.spine.examples.chatspn.desktop.chat.MessageData
import java.util.stream.Collectors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * UI Model for the navigation.
 */
public class NavigationModel(private val client: DesktopClient) {
    private val chatPreviewsState = MutableStateFlow<ChatList>(listOf())
    public val selectedChat: MutableState<ChatId> = mutableStateOf(ChatId.getDefaultInstance())
    public val searchState: SearchState = SearchState()
    public val currentPage: MutableState<Page> = mutableStateOf(Page.REGISTRATION)
    public val profilePageState: ProfilePageState = ProfilePageState()
    public val authenticatedUser: UserProfile
        get() {
            return client.authenticatedUser!!
        }

    /**
     * Reads chat previews and subscribes to their updates.
     */
    public fun observeChats() {
        updateChats(client.readChats().toChatDataList(client))
        client.observeChats { state -> updateChats(state.chatList.toChatDataList(client)) }
    }

    /**
     * Returns the state of the user's chats.
     */
    public fun chats(): StateFlow<ChatList> {
        return chatPreviewsState
    }

    /**
     * Selects provided chat and opens the 'Chat' page.
     *
     * @param chat ID of the chat to select
     */
    public fun selectChat(chat: ChatId) {
        selectedChat.value = chat
        currentPage.value = Page.CHAT
        profilePageState.clear()
        searchState.clear()
    }

    /**
     * Returns the data of the chat by id, or `null` if the chat doesn't exist.
     *
     * @param chatId ID of the chat
     */
    public fun chatData(chatId: ChatId): ChatData? {
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
    public fun selectPersonalChat(user: UserId) {
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
     * Searches chats and users by provided search word.
     *
     * Search rules:
     * - the chat will be found if its name or name parts starts with `searchWord`;
     * - the user will be found if his e-mail address is equal to `searchWord`.
     *
     * The results will update the model search state.
     */
    public fun search(searchWord: String) {
        if (searchWord.isEmpty()) {
            searchState.localChats.value = listOf()
            searchState.globalUsers.value = listOf()
            return
        }
        val localChats = chatPreviewsState.value.filter { chat ->
            (chat.name.split(" ") + chat.name)
                .find { it.startsWith(searchWord, true) } != null
        }.toMutableList()
        val userWithSearchedEmail = client.findUser(searchWord)
        searchState.localChats.value = localChats
        if (null != userWithSearchedEmail) {
            val personalChat = findPersonalChat(userWithSearchedEmail.id)
            if (personalChat == null) {
                searchState.globalUsers.value = listOf(userWithSearchedEmail)
            } else {
                localChats += personalChat
            }
        } else {
            searchState.globalUsers.value = listOf()
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
    public fun openUserProfile(userId: UserId) {
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
    public fun openChatInfo(chatId: ChatId) {
        val chat = chatPreviewsState.value.find { chatData -> chatId.equals(chatData.id) } ?: return
        if (chat.type == Chat.ChatType.CT_PERSONAL) {
            val userId = chat.members.find { user -> !user.equals(authenticatedUser.id) }
            val user = client.findUser(userId!!)
            profilePageState.userProfile.value = user!!
            profilePageState.chatState.value = chatData(chat.id)
            currentPage.value = Page.PROFILE
        }
    }
}

/**
 * State of the modal window with the user profile.
 */
public class ProfilePageState {
    public val userProfile: MutableState<UserProfile> =
        mutableStateOf(UserProfile.getDefaultInstance())
    public val chatState: MutableState<ChatData?> = mutableStateOf(null)

    /**
     * Clears the state.
     */
    public fun clear() {
        userProfile.value = UserProfile.getDefaultInstance()
        chatState.value = null
    }
}

/**
 * State of the chats search.
 */
public class SearchState {
    public val inputState: MutableState<String> = mutableStateOf("")
    public val localChats: MutableState<List<ChatData>> = mutableStateOf(listOf())
    public val globalUsers: MutableState<List<UserProfile>> = mutableStateOf(listOf())

    /**
     * Clears the state.
     */
    public fun clear() {
        inputState.value = ""
        localChats.value = listOf()
        globalUsers.value = listOf()
    }
}

/**
 * Pages in the application.
 */
public enum class Page {
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
