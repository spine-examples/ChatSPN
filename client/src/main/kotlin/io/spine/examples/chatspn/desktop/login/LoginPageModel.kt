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

package io.spine.examples.chatspn.desktop.login

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.spine.examples.chatspn.desktop.DesktopClient

/**
 * UI Model for the `LoginPage`.
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
