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

package io.spine.examples.chatspn.desktop.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLocalization
import androidx.compose.ui.unit.dp
import io.spine.examples.chatspn.desktop.component.IconButton

/**
 * Displays the input field for searching.
 *
 * @param inputText state of the text in the input field
 */
@Composable
public fun SearchField(
    inputText: MutableState<String>
) {
    BasicTextField(
        modifier = Modifier
            .size(182.dp, 30.dp)
            .background(MaterialTheme.colorScheme.background),
        value = inputText.value,
        singleLine = true,
        onValueChange = {
            inputText.value = it
        }
    ) { innerTextField ->
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(size = 3.dp)
                )
                .padding(all = 8.dp)
        ) {
            DecoratedTextField(inputText, "Search", Icons.Default.Close, innerTextField) {
                inputText.value = ""
            }
        }
    }
}

/**
 * Displays decorated text field with placeholder and icon.
 *
 * @param inputText state of the text in the input field
 * @param placeholder placeholder text to display
 * @param icon icon to display
 * @param innerTextField text field to decorate
 * @param onIconClick callback that will be triggered when the icon clicked
 */
@Composable
private fun DecoratedTextField(
    inputText: MutableState<String>,
    placeholder: String = "",
    icon: ImageVector,
    innerTextField: @Composable () -> Unit,
    onIconClick: () -> Unit
) {
    if (inputText.value.isEmpty()) {
        Text(
            text = placeholder,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
    Row(
        Modifier.fillMaxWidth(),
        Arrangement.SpaceBetween,
        Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f)) {
            innerTextField()
        }
        if (inputText.value.trim().isNotEmpty()) {
            IconButton(
                icon,
                MaterialTheme.colorScheme.onSecondary,
                { this.padding(horizontal = 4.dp) },
                onIconClick
            )
        }
    }
}
