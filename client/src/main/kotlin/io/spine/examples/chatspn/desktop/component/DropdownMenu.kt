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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Displays the default dropdown menu.
 *
 * @param isOpenState menu visibility state
 * @param content content of the dropdown menu
 */
@Composable
public fun DefaultDropdownMenu(
    isOpenState: MutableState<Boolean>,
    content: @Composable () -> Unit
) {
    DropdownMenu(
        expanded = isOpenState.value,
        onDismissRequest = { isOpenState.value = false },
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
    ) {
        content()
    }
}

/**
 * Displays the default item of the dropdown menu.
 *
 * @param label text to be displayed in the item
 * @param icon icon to be displayed on the inner left side of the element
 * @param color color of the text and icon
 * @param onClick callback that will be triggered when the item clicked
 */
@Composable
public fun DefaultDropdownMenuItem(
    label: String,
    icon: ImageVector,
    color: Color = Color.Black,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        modifier = Modifier
            .height(30.dp),
        text = {
            Text(
                label,
                color = color,
                style = MaterialTheme.typography.labelMedium
            )
        },
        onClick = onClick,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color
            )
        }
    )
}
