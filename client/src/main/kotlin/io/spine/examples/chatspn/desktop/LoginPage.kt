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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Represents the 'Login' page in the application.
 *
 * @param client desktop client
 * @param toRegistration navigation to the 'Registration' page
 * @param toChats navigation to the 'Chats' page
 */
@Composable
public fun LoginPage(
    client: DesktopClient,
    toRegistration: () -> Unit,
    toChats: () -> Unit
) {
    val model = remember { LoginPageModel(client, toRegistration, toChats) }
    val emailState = remember { model.emailState }
    val emailErrorState = remember { model.emailErrorState }
    val emailErrorText = remember { model.emailErrorText }
    FormBox {
        Column(
            Modifier.padding(24.dp),
            Arrangement.spacedBy(12.dp),
            Alignment.CenterHorizontally
        ) {
            FormHeader("Sign In")
            FormField(
                "Email:",
                "john.doe@mail.com",
                emailState,
                emailErrorState,
                emailErrorText
            )
            Spacer(Modifier.height(4.dp))
            SignInButton(model)
            SecondaryButton("Don't have an account?", model.toRegistration)
        }
    }
}

/**
 * Displays the 'Sign In' button.
 */
@Composable
private fun SignInButton(model: LoginPageModel) {
    val viewScope = rememberCoroutineScope { Dispatchers.Default }
    val emailState = remember { model.emailState }
    PrimaryButton("Sign In") {
        if (emailState.value.isEmpty()) {
            model.emailErrorState.value = true
            model.emailErrorText.value = "Email field must not be empty"
        }
        if (!model.emailErrorState.value) {
            viewScope.launch {
                model.logIn()
            }
        }
    }
}

/**
 * UI Model for the `[LoginPage]`.
 */
private class LoginPageModel(
    private val client: DesktopClient,
    val toRegistration: () -> Unit,
    private val toChats: () -> Unit
) {
    val emailState: MutableState<String> = mutableStateOf("")
    val emailErrorState: MutableState<Boolean> = mutableStateOf(false)
    val emailErrorText: MutableState<String> = mutableStateOf("")

    /**
     * Authenticates the user with the credentials specified in the form fields.
     */
    fun logIn() {
        val onFail = {
            emailErrorState.value = true
            emailErrorText.value = "Account with these credentials doesn't exist"
        }
        client.logIn(
            emailState.value,
            toChats,
            onFail
        )
    }
}
