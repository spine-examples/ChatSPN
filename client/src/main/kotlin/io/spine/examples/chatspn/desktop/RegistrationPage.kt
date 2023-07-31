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
 * Displays the 'Registration' page in the application.
 *
 * @param client desktop client
 * @param toLogin navigation to the 'Login' page
 * @param toChats navigation to the 'Chats' page
 */
@Composable
public fun RegistrationPage(
    client: DesktopClient,
    toLogin: () -> Unit,
    toChats: () -> Unit,
) {
    val model = remember { RegistrationPageModel(client, toLogin, toChats) }
    val emailState = remember { model.emailState }
    val emailErrorState = remember { model.emailErrorState }
    val emailErrorText = remember { model.emailErrorText }
    val nameState = remember { model.nameState }
    val nameErrorState = remember { model.nameErrorState }
    val nameErrorText = remember { model.nameErrorText }
    FormBox {
        Column(
            Modifier.padding(24.dp),
            Arrangement.spacedBy(12.dp),
            Alignment.CenterHorizontally
        ) {
            FormHeader("Sign Up")
            FormField(
                "Email:",
                "john.doe@mail.com",
                emailState,
                emailErrorState,
                emailErrorText
            )
            FormField(
                "Name:",
                "John Doe",
                nameState,
                nameErrorState,
                nameErrorText
            )
            Spacer(Modifier.height(4.dp))
            SignUpButton(model)
            SecondaryButton("Already have an account?", model.toLogin)
        }
    }
}

/**
 * Displays the 'Sign Up' button.
 */
@Composable
private fun SignUpButton(model: RegistrationPageModel) {
    val viewScope = rememberCoroutineScope { Dispatchers.Default }
    val emailState = remember { model.emailState }
    val nameState = remember { model.nameState }
    PrimaryButton("Sign Up") {
        if (emailState.value.isEmpty()) {
            model.emailErrorState.value = true
            model.emailErrorText.value = "Email field must not be empty"
        }
        if (nameState.value.isEmpty()) {
            model.nameErrorState.value = true
            model.nameErrorText.value = "Name field must not be empty"
        }
        if (!model.emailErrorState.value && !model.nameErrorState.value) {
            viewScope.launch {
                model.register()
            }
        }
    }
}

/**
 * UI Model for the [RegistrationPage]`.
 */
private class RegistrationPageModel(
    private val client: DesktopClient,
    val toLogin: () -> Unit,
    private val toChats: () -> Unit,
) {
    val emailState: MutableState<String> = mutableStateOf("")
    val emailErrorState: MutableState<Boolean> = mutableStateOf(false)
    val emailErrorText: MutableState<String> = mutableStateOf("")
    val nameState: MutableState<String> = mutableStateOf("")
    val nameErrorState: MutableState<Boolean> = mutableStateOf(false)
    val nameErrorText: MutableState<String> = mutableStateOf("")

    /**
     * Registers a new user with the credentials specified in the form fields.
     */
    fun register() {
        val onFail = {
            emailErrorState.value = true
            emailErrorText.value = "An account with this email already exists"
        }
        client.register(
            nameState.value,
            emailState.value,
            toChats,
            onFail
        )
    }
}
