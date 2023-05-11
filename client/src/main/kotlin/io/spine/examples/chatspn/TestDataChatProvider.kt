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

package io.spine.examples.chatspn

import io.spine.base.Time.currentTime
import io.spine.core.UserId
import io.spine.examples.chatspn.chat.ChatPreview
import io.spine.examples.chatspn.chat.MessagePreview
import io.spine.examples.chatspn.chat.PersonalChatView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * `ChatProvider` for the manual testing purposes.
 */
class TestDataChatProvider : ChatProvider {
    private val chatsFlow = MutableStateFlow<List<ChatPreview>>(listOf())
    private val chatMessagesFlow: MutableMap<ChatId, MutableStateFlow<List<MessagePreview>>> =
        mutableMapOf()
    private val vlad = userId("vladId")
    private val artem = userId("artemId")
    private val alex = userId("alexId")
    private val artemChat = chatId("chat1")
    private val alexChat = chatId("chat2")

    init {
        val artemChatPreview = ChatPreview
            .newBuilder()
            .setPersonalChat(
                PersonalChatView.newBuilder()
                    .setCreator(vlad)
                    .setMember(artem)
                    .vBuild()
            )
            .setId(artemChat)
            .vBuild()
        val alexChatPreview = ChatPreview
            .newBuilder()
            .setPersonalChat(
                PersonalChatView
                    .newBuilder()
                    .setCreator(alex)
                    .setMember(vlad)
                    .vBuild()
            )
            .setId(alexChat)
            .vBuild()

        chatsFlow.value = listOf(artemChatPreview, alexChatPreview)

        chatMessagesFlow[artemChat] = MutableStateFlow(listOf())
        chatMessagesFlow[alexChat] = MutableStateFlow(listOf())
    }

    override fun messages(chat: ChatId): StateFlow<List<MessagePreview>> {
        if (!chatMessagesFlow.containsKey(chat)) {
            throw IllegalStateException("Chat not found")
        }
        return chatMessagesFlow.getOrDefault(chat, MutableStateFlow(mutableListOf()))
    }

    override fun chats(): StateFlow<List<ChatPreview>> {
        return chatsFlow
    }

    override fun sendMessage(chat: ChatId, content: String) {
        sendMessage(vlad, chat, content)
    }

    private fun sendMessage(user: UserId, chat: ChatId, content: String) {
        val message = createMessage(user, content)
        chatMessagesFlow[chat]?.value = chatMessagesFlow[chat]?.value?.plus(listOf(message))!!
        val optionalChat = chatsFlow
            .value
            .stream()
            .filter { chatPreview -> chatPreview.id.equals(chat) }
            .findFirst()
        if (optionalChat.isPresent) {
            val chatPreview = optionalChat.get()
            val newChat = chatPreview
                .toBuilder()
                .setLastMessage(message)
                .vBuild()
            val chats = chatsFlow.value
            val newList = chats.subList(0, chats.indexOf(chatPreview)) +
                    listOf(newChat) +
                    chats.subList(chats.indexOf(chatPreview) + 1, chats.size)
            chatsFlow.value = newList
        }
    }

    /**
     * Periodically sends messages to the chat.
     */
    suspend fun startChatting() {
        while (true) {
            sendMessage(artem, artemChat, "Hello${(Math.random() * 100).toInt()}")
            delay(1000)
            sendMessage(alex, alexChat, "Hello${(Math.random() * 100).toInt()}")
            delay(3000)
        }
    }

    private fun userId(id: String): UserId {
        return UserId
            .newBuilder()
            .setValue(id)
            .vBuild()
    }

    private fun chatId(id: String): ChatId {
        return ChatId
            .newBuilder()
            .setUuid(id)
            .vBuild()
    }

    private fun createMessage(user: UserId, content: String): MessagePreview {
        return MessagePreview.newBuilder()
            .setId(MessageId.generate())
            .setContent(content)
            .setUser(user)
            .setWhenPosted(currentTime())
            .vBuild()
    }
}
