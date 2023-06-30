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

package io.spine.examples.chatspn.desktop.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.spine.examples.chatspn.chat.Chat
import io.spine.examples.chatspn.desktop.component.ModalWindow
import io.spine.examples.chatspn.desktop.component.TextButton
import io.spine.examples.chatspn.desktop.navigation.ChatData
import kotlinx.coroutines.launch

/**
 * A modal dialog allowing to delete some chat.
 *
 * @param isVisibleState mutable state of dialog visibility
 * @param onDelete callback that will be triggered when the 'delete' button clicked
 * @param chat chat which data display in the dialog
 */
@Composable
public fun ChatDeletionDialog(
    isVisibleState: MutableState<Boolean>,
    onDelete: () -> Unit,
    chat: ChatData
) {
    ModalWindow(isVisibleState) {
        Column(
            Modifier.width(300.dp)
                .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 8.dp)
        ) {
            Text(
                buildAnnotatedString {
                    append("Are you sure you want to delete chat ")
                    if (chat.type == Chat.ChatType.CT_PERSONAL) {
                        append("with ")
                    }
                    append(
                        AnnotatedString(
                            chat.name,
                            spanStyle = SpanStyle(fontWeight = FontWeight.Bold)
                        )
                    )
                    append("?")
                },
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "This action cannot be undone.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))
            ChatDeletionDialogButtons(isVisibleState, onDelete)
        }
    }
}

/**
 * Displays the buttons of the `ChatDeletionDialog`.
 *
 * @param isVisibleState mutable state of dialog visibility
 * @param onDelete callback that will be triggered when the 'delete' button clicked
 */
@Composable
private fun ChatDeletionDialogButtons(
    isVisibleState: MutableState<Boolean>,
    onDelete: () -> Unit
) {
    val viewScope = rememberCoroutineScope()
    Row(
        Modifier.fillMaxWidth(),
        Arrangement.spacedBy(8.dp, Alignment.End)
    ) {
        TextButton("Cancel") {
            isVisibleState.value = false
        }
        TextButton("Delete", MaterialTheme.colorScheme.error) {
            viewScope.launch {
                onDelete()
            }
            isVisibleState.value = false
        }
    }
}
