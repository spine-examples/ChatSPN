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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.protobuf.Timestamp
import java.text.SimpleDateFormat
import java.util.*

/**
 * Displays the chat preview panel.
 *
 * @param chat data of the chat to display
 * @param isHighlighted whether the panel is highlighted
 * @param onClick callback that will be triggered when the panel clicked
 */
@Composable
public fun ChatPreviewPanel(
    chat: ChatData,
    isHighlighted: Boolean,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                color = if (isHighlighted)
                    MaterialTheme.colorScheme.inverseSurface
                else
                    MaterialTheme.colorScheme.background
            ),
        Alignment.CenterStart,
    ) {
        ChatPreviewContent(chat)
    }
}

/**
 * Displays the chat preview content in the chat preview panel.
 */
@Composable
private fun ChatPreviewContent(chat: ChatData) {
    Row(
        Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(46f, chat.name)
        Spacer(Modifier.size(12.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween
            ) {
                Text(
                    text = chat.name,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = if (chat.lastMessage == null) ""
                    else chat.lastMessage.whenPosted.toStringTime(),
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(Modifier.size(9.dp))
            Text(
                text = if (chat.lastMessage == null) ""
                else chat.lastMessage.content.replace("\\s".toRegex(), " "),
                color = MaterialTheme.colorScheme.onSecondary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Converts `Timestamp` to the `hh:mm` string.
 */
public fun Timestamp.toStringTime(): String {
    val date = Date(this.seconds * 1000)
    val format = SimpleDateFormat("hh:mm", Locale.getDefault())
    return format.format(date)
}