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

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


/**
 * UI theme for the application.
 *
 * @see MaterialTheme
 */
@Composable
public fun ChatSpnTheme(content: @Composable () -> Unit) {
    MaterialTheme (
        colorScheme = lightColors,
        typography = typography,
        content = content
    )
}

/**
 * The light colors scheme of the application.
 */
private val lightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF037BFC),
    onPrimary = Color.White,
    onSecondary = Color.Gray,
    surface = Color(0xFFB0CBF5),
    background = Color.White,
    error = Color(0xFFFC3903),
)

/**
 * Text styles of the application.
 */
private val typography: Typography = Typography(
    headlineLarge = TextStyle(
        fontSize = 26.sp
    ),
    headlineMedium = TextStyle(
        fontSize = 22.sp
    ),
    headlineSmall= TextStyle(
        fontSize = 14.sp,
        color = Color.Gray
    ),
    bodyMedium = TextStyle(
        fontSize = 18.sp
    ),
)