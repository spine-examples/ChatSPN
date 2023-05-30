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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Represents the 'Login' page in the application.
 *
 * @param model UI model for the 'Login' page
 */
@Composable
public fun LoginPage(model: LoginPageModel) {
    val viewScope = rememberCoroutineScope { Dispatchers.Default }
    val emailState = remember { model.emailState }
    val emailErrorState = remember { model.emailErrorState }
    val emailErrorText = remember { model.emailErrorText }
    FormBox {
        FormHeader("Sign In")
        FormField(
            "Email:",
            "john.doe@mail.com",
            emailState,
            emailErrorState,
            emailErrorText
        )
        PrimaryButton("Sign In") {
            if (emailState.value.isEmpty()) {
                emailErrorState.value = true
                emailErrorText.value = "Email field must not be empty"
            }
            if (!emailErrorState.value) {
                viewScope.launch {
                    model.logIn()
                }
            }
        }
        SecondaryButton("Don't have an account?", model.toRegistration)
    }
}

/**
 * UI Model for the `[LoginPage]`.
 */
public class LoginPageModel(
    private val client: DesktopClient,
    public val toRegistration: () -> Unit,
    private val toChats: () -> Unit,
) {
    public val emailState: MutableState<String> = mutableStateOf("")
    public val emailErrorState: MutableState<Boolean> = mutableStateOf(false)
    public val emailErrorText: MutableState<String> = mutableStateOf("")

    /**
     * Authenticates the user with the credentials specified in the form fields.
     */
    public fun logIn() {
        val onFail = {
            emailErrorState.value = true
            emailErrorText.value = "Account with this credentials doesn't exist"
        }
        client.logIn(
            emailState.value,
            toChats,
            onFail
        )
    }
}
