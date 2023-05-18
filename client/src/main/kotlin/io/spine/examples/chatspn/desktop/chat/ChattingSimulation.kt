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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.spine.base.Time.currentTime
import io.spine.core.UserId
import io.spine.examples.chatspn.ChatId
import io.spine.examples.chatspn.MessageId
import io.spine.examples.chatspn.account.UserProfile
import io.spine.examples.chatspn.desktop.chat.ChatData
import io.spine.examples.chatspn.desktop.chat.ChatsPageModel
import io.spine.examples.chatspn.desktop.chat.MessageData
import io.spine.net.EmailAddress
import kotlinx.coroutines.delay

/**
 * Starts up the chatting simulation for manual testing.
 */
@Composable
public fun startChattingSimulation(model: ChatsPageModel) {
    val vlad = "Vlad".toUserProfile()
    model.authorizedUser(vlad)
    val artem = "Artem".toUserProfile()
    val alex = "Alex".toUserProfile()
    val artemChatId = ChatId.generate()
    val alexChatId = ChatId.generate()
    val artemMessages = mutableListOf(
        MessageData(
            MessageId.generate(),
            artem,
            "Artem messageContent",
            currentTime()
        )
    )
    val alexMessages = mutableListOf<MessageData>()
    val chats = mutableListOf(
        ChatData(artemChatId, artem.name, artemMessages.last()),
        ChatData(alexChatId, alex.name, null),
    )

    model.updateChats(chats.toList())
    model.updateMessages(artemChatId, artemMessages.toList())
    model.updateMessages(alexChatId, alexMessages.toList())
    LaunchedEffect(Unit) {
        while (true) {
            val newMessage = MessageData(
                MessageId.generate(),
                artem,
                "Hello${(Math.random() * 100).toInt()}",
                currentTime()
            )
            artemMessages.add(newMessage)
            model.updateMessages(artemChatId, artemMessages.toList())
            chats[0] = ChatData(artemChatId, artem.name, newMessage)
            model.updateChats(chats.toList())
            delay(2000)
        }
    }
}

/**
 * Creates `UserProfile` with provided string as its id, name and email part.
 */
private fun String.toUserProfile(): UserProfile {
    return UserProfile
        .newBuilder()
        .setId(this.toUserId())
        .setEmail("${this}@teamdev.com".toEmail())
        .setName(this)
        .vBuild()
}

/**
 * Creates `UserId` with provided string as its value.
 */
private fun String.toUserId(): UserId {
    return UserId
        .newBuilder()
        .setValue(this)
        .vBuild()
}

/**
 * Creates `EmailAddress` with provided string as its value.
 */
private fun String.toEmail(): EmailAddress {
    return EmailAddress
        .newBuilder()
        .setValue(this)
        .vBuild()
}

