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

package io.spine.examples.chatspn.chat

import ChatColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.protobuf.Timestamp
import io.spine.examples.chatspn.UserProvider
import java.text.SimpleDateFormat
import java.util.*

/**
 * Represents the single message view in the chat.
 */
@Composable
fun ChatMessage(
    message: MessagePreview,
    userProvider: UserProvider
) {
    val isMyMessage = message.user == userProvider.loggedUser().id
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMyMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            modifier = Modifier.padding(4.dp),
            shape = RoundedCornerShape(size = 20.dp),
            elevation = 8.dp
        ) {
            Box(
                Modifier.background(
                    brush = Brush.horizontalGradient(
                        ChatColors.MESSAGE_BACKGROUND_GRADIENT.map { Color(it) })
                ).padding(10.dp),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Column {
                        Row(verticalAlignment = Alignment.Top) {
                            UserAvatar()
                            Spacer(Modifier.size(8.dp))
                            Column {
                                Row {
                                    Text(
                                        text = userProvider.findUser(message.user).name,
                                        style = MaterialTheme.typography.h5
                                    )
                                    Spacer(Modifier.size(10.dp))
                                    Text(
                                        text = secondsToStringDate(message.whenPosted),
                                        style = MaterialTheme.typography.h6,
                                        color = ChatColors.SECONDARY
                                    )
                                }
                                Spacer(Modifier.size(8.dp))
                                Text(
                                    text = message.content,
                                    fontSize = 20.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun secondsToStringDate(timestamp: Timestamp): String {
    val date = Date(timestamp.seconds * 1000)
    val format = SimpleDateFormat("hh:mm", Locale.getDefault())
    return format.format(date)
}
