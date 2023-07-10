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
import io.spine.examples.chatspn.chat.ChatCard
import io.spine.examples.chatspn.desktop.DesktopClient
import io.spine.examples.chatspn.desktop.chat.remove
import java.util.stream.Collectors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * UI Model for the navigation.
 */
public class NavigationModel(private val client: DesktopClient) {
    private val chatCards = MutableStateFlow<ChatList>(listOf())
    public val selectedChat: MutableState<ChatId> = mutableStateOf(ChatId.getDefaultInstance())
    public val userSearchFieldState: UserSearchFieldState = UserSearchFieldState()
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
        chatCards.value = client.readChats()
        client.observeChats(
            { chatCard -> updateChatsState(chatCard) },
            { removedCardId ->
                val chats = chatCards.value
                val cardIndex = chats.indexOfFirst { card ->
                    removedCardId.equals(card.cardId)
                }
                chatCards.value = chats.remove(cardIndex)
            }
        )
    }

    /**
     * Returns the state of the user's chats.
     */
    public fun chats(): StateFlow<ChatList> {
        return chatCards
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
    }

    /**
     * Returns the card of the chat by id, or `null` if the chat doesn't exist.
     *
     * @param chatId ID of the chat
     */
    public fun chatCard(chatId: ChatId): ChatCard? {
        val chat = chatCards.value.find { chatCard ->
            chatCard.chatId.equals(chatId)
        }
        return chat
    }

    /**
     * Updates the state of chats by adding a new chat
     * or editing existing one if chat ID matches.
     *
     * @param chat card of the chat to update the state
     */
    private fun updateChatsState(chat: ChatCard) {
        val chats = chatCards.value
        if (null != chats.findChat(chat.chatId)) {
            val newChats = chats.replaceChat(chat)
            chatCards.value = newChats
        } else {
            chatCards.value = chats + chat
        }
    }

    /**
     * Selects the personal chat between the authenticated and the provided user,
     * if the chat doesn't exist, creates it.
     *
     * @param user ID of the user with whom to select a personal chat
     */
    public fun selectPersonalChat(user: UserId) {
        val chat = findPersonalChat(user)
        if (null != chat) {
            selectChat(chat.chatId)
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
    public fun createPersonalChat(email: String) {
        val user = client.findUser(email)
        if (null != user) {
            client.createPersonalChat(user.id)
        } else {
            userSearchFieldState.errorState.value = true
        }
    }

    /**
     * Returns the card of the personal chat with the provided user,
     * or `null` if the chat doesn't exist.
     *
     * @param user ID of the user with whom to find the personal chat
     */
    private fun findPersonalChat(user: UserId): ChatCard? {
        val providedUserChats = client.readPersonalChats(user)
        val authenticatedUserChats = client.readPersonalChats(authenticatedUser.id)
        val chat = authenticatedUserChats.find { chat ->
            providedUserChats.any { providedUserChat -> providedUserChat.chatId.equals(chat.chatId) }
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
        profilePageState.chatState.value =
            if (null == user) null else findPersonalChat(user.id)
        currentPage.value = Page.PROFILE
    }

    /**
     * Opens a page with chat info. If the chat is personal, a user profile will be opened.
     *
     * @param chatId ID of the chat which info to open
     */
    public fun openChatInfo(chatId: ChatId) {
        val chat = chatCards.value.find { chatCard -> chatId.equals(chatCard.chatId) } ?: return
        if (chat.type == Chat.ChatType.CT_PERSONAL) {
            val userId = client
                .readChatMembers(chatId)
                .find { user -> !user.equals(authenticatedUser.id) }
            val user = client.findUser(userId!!)
            profilePageState.userProfile.value = user!!
            profilePageState.chatState.value = chatCard(chat.chatId)
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
    public val chatState: MutableState<ChatCard?> = mutableStateOf(null)

    /**
     * Clears the state.
     */
    public fun clear() {
        userProfile.value = UserProfile.getDefaultInstance()
        chatState.value = null
    }
}

/**
 * State of the user search field.
 */
public class UserSearchFieldState {
    public val userEmailState: MutableState<String> = mutableStateOf("")
    public val errorState: MutableState<Boolean> = mutableStateOf(false)
}

/**
 * Pages in the application.
 */
public enum class Page {
    CHAT, LOGIN, REGISTRATION, PROFILE
}

/**
 * List of `ChatData`.
 */
public typealias ChatList = List<ChatCard>

/**
 * Finds chat in the list by ID.
 *
 * @param id ID of the chat to find
 * @return found chat or `null` if chat is not found
 */
private fun ChatList.findChat(id: ChatId): ChatCard? {
    val message = this
        .stream()
        .filter { chat -> chat.chatId.equals(id) }
        .collect(Collectors.toList())
    if (message.isEmpty()) {
        return null
    }
    return message[0]
}

/**
 * Returns the new list with the replaced chat with the same chat ID.
 *
 * @param newChat chat to replace
 */
private fun ChatList.replaceChat(newChat: ChatCard): ChatList {
    val oldChat = this.findChat(newChat.chatId)
    val chatIndex = this.indexOf(oldChat)
    val leftPart = this.subList(0, chatIndex)
    val rightPart = this.subList(chatIndex + 1, this.size)
    return leftPart + newChat + rightPart
}
