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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.spine.examples.chatspn.desktop.chat.ChatsPage
import io.spine.examples.chatspn.desktop.chat.ChatsPageModel

/**
 * Provides navigation through application and page composition.
 */
public class Navigation(private val client: DesktopClient) {
    private val currentPage: MutableState<Page> = mutableStateOf(Page.REGISTRATION)

    /**
     * Represents the current page of the application.
     *
     * It will be recomposed when the page changes.
     */
    @Composable
    public fun currentPage() {
        val page by remember { currentPage }
        when (page) {
            Page.REGISTRATION -> registrationPage()
            Page.LOGIN -> loginPage()
            Page.CHATS -> chatsPage()
        }
    }

    /**
     * Configures and composes the 'Registration' page.
     */
    @Composable
    private fun registrationPage() {
        val model = RegistrationPageModel(
            client,
            toLogin = { currentPage.value = Page.LOGIN },
            toChats = { currentPage.value = Page.CHATS }
        )
        RegistrationPage(model)
    }

    /**
     * Configures and composes the 'Login' page.
     */
    @Composable
    private fun loginPage() {
        val model = LoginPageModel(
            client,
            toRegistration = { currentPage.value = Page.REGISTRATION },
            toChats = { currentPage.value = Page.CHATS }
        )
        LoginPage(model)
    }

    /**
     * Configures and composes the 'Chats' page.l
     */
    @Composable
    private fun chatsPage() {
        val model = ChatsPageModel(client)
        ChatsPage(model)
    }
}

/**
 * Pages in the application.
 */
private enum class Page {
    CHATS, LOGIN, REGISTRATION
}