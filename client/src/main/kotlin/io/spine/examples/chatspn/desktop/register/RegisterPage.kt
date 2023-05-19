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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.spine.examples.chatspn.desktop.FormBox
import io.spine.examples.chatspn.desktop.FormField
import io.spine.examples.chatspn.desktop.FormHeader
import io.spine.examples.chatspn.desktop.PrimaryButton
import io.spine.examples.chatspn.desktop.SecondaryButton

@Composable
public fun RegisterPage(
    toLogin: () -> Unit,
    toChats: () -> Unit,
) {
    val emailState = remember { mutableStateOf("") }
    val emailErrorState = remember { mutableStateOf(false) }
    val nameState = remember { mutableStateOf("") }
    val nameErrorState = remember { mutableStateOf(false) }
    FormBox {
        FormHeader("Sign Up")
        FormField("Email:", "john.doe@mail.com", emailState, emailErrorState)
        FormField("Name:", "John Doe", nameState, nameErrorState)
        PrimaryButton("Sign Up") {
            emailErrorState.value = emailState.value.isEmpty()
            nameErrorState.value = nameState.value.isEmpty()
            if (!emailErrorState.value && !nameErrorState.value) {
                toChats()
            }
        }
        SecondaryButton("Already have an account?", toLogin)
    }
}