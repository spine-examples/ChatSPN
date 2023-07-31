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

package io.spine.examples.chatspn.desktop.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

/**
 * Displays a round avatar with the first letter of the provided name
 * on a gradient background.
 *
 * @param size width and height of the avatar
 * @param name name to write it first letter on the avatar and choose background
 * @param modifierExtender extension for the modifier
 */
@Composable
public fun Avatar(size: Float, name: String, modifierExtender: Modifier.() -> Modifier = { this }) {
    val gradients = listOf(
        listOf(Color(0xFFFFC371), Color(0xFFFF5F6D)),
        listOf(Color(0xFF95E4FC), Color(0xFF0F65D6)),
        listOf(Color(0xFF72F877), Color(0xFF259CF1)),
        listOf(Color(0xFF76DBFA), Color(0xFFD72DFD)),
        listOf(Color(0xFFA6FA85), Color(0xFF22AC00D)),
        listOf(Color(0xFFFD9696), Color(0xFFF11010))
    )
    val gradientIndex = abs(name.hashCode()) % gradients.size
    Box(contentAlignment = Alignment.Center) {
        Image(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .modifierExtender(),
            contentScale = ContentScale.Crop,
            painter = object : Painter() {
                override val intrinsicSize: Size = Size(size, size)
                override fun DrawScope.onDraw() {
                    drawRect(
                        Brush.linearGradient(gradients[gradientIndex]),
                        size = Size(size * 4, size * 4)
                    )
                }
            },
            contentDescription = "User picture"
        )
        Text(
            if (name.isNotEmpty()) name[0].toString() else "",
            color = Color.White,
            fontSize = (size * 0.5).sp,
            style = MaterialTheme.typography.headlineLarge
        )
    }
}
