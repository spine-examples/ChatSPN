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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Displays the input field for sending and editing a message in the chat.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
public fun MessageInputField(
    inputText: MutableState<String>,
    icon: ImageVector,
    onSubmit: () -> Unit
) {
    val viewScope = rememberCoroutineScope { Dispatchers.Default }
    BasicTextField(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .onPreviewKeyEvent {
                when {
                    (it.type == KeyEventType.KeyDown &&
                            it.isShiftPressed &&
                            it.key == Key.Enter) -> {
                        viewScope.launch {
                            onSubmit()
                        }
                        true
                    }
                    else -> false
                }
            },
        textStyle = MaterialTheme.typography.bodyMedium,
        keyboardActions = KeyboardActions(),
        value = inputText.value,
        onValueChange = {
            inputText.value = it
        }
    ) { innerTextField ->
        DecoratedTextField(inputText, icon, onSubmit, innerTextField)
    }
}

/**
 * Displays decorated text field with placeholder and icon.
 *
 * @param inputText state of the text in the input field
 * @param icon icon to display
 * @param onSubmit callback that will be triggered when the icon clicked
 * @param innerTextField text field to decorate
 */
@Composable
private fun DecoratedTextField(
    inputText: MutableState<String>,
    icon: ImageVector,
    onSubmit: () -> Unit,
    innerTextField: @Composable () -> Unit
) {
    val viewScope = rememberCoroutineScope { Dispatchers.Default }
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .heightIn(15.dp, 192.dp)
                .weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (inputText.value.isEmpty()) {
                Text(
                    text = "Write a message...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
            innerTextField()
        }
        if (inputText.value.trim().isNotEmpty()) {
            IconButton(
                icon,
                MaterialTheme.colorScheme.primary,
                { this.padding(bottom = 12.dp, end = 12.dp) }
            ) {
                viewScope.launch {
                    onSubmit()
                }
            }
        }
    }
}
