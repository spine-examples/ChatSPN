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

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
public fun LoginPage(
    toRegister: () -> Unit,
    toChats: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var isEmailError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .width(500.dp)
                .padding(10.dp)
        ) {
            Text(
                "Sign In",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 20.dp),
                fontSize = 30.sp,
            )
            Row {
                Text(
                    text = "Email:",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .width(50.dp)
                )
                TextField(
                    modifier = Modifier.fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(start = 10.dp),
                    value = email,
                    placeholder = {
                        Text("john.doe@mail.com")
                    },
                    onValueChange = {
                        email = it
                        isEmailError = false
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        errorIndicatorColor = MaterialTheme.colorScheme.error
                    ),
                    isError = isEmailError
                )
            }
            Button(
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp),
                onClick = {
                    isEmailError = email.isEmpty()
                    if (!isEmailError) {
                        toChats()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Login")
            }
            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp),
                onClick = toRegister,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Don't have an account?")
            }
        }
    }
}
