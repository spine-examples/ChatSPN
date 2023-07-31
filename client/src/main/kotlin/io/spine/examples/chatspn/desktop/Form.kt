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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import java.awt.Cursor

/**
 * Displays the layout for the form.
 *
 * @param FormContent the content to be composed inside the form layout
 */
@Composable
public fun FormBox(
    FormContent: @Composable () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(10.dp),
        Alignment.Center
    ) {
        Column(
            Modifier
                .width(396.dp)
                .clip(MaterialTheme.shapes.medium),
        ) {
            Box(Modifier.background(MaterialTheme.colorScheme.surface)) {
                FormContent()
            }
        }
    }
}

/**
 * Displays the form header.
 */
@Composable
public fun FormHeader(text: String) {
    Text(
        text,
        modifier = Modifier
            .padding(bottom = 20.dp),
        style = MaterialTheme.typography.displayLarge,
    )
}

/**
 * Displays a form input field with the functionality to display an error.
 */
@Composable
public fun FormField(
    label: String,
    placeholder: String = "",
    valueState: MutableState<String>,
    errorState: MutableState<Boolean>,
    errorText: MutableState<String>
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FormFieldLabel(label)
        BasicTextField(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium),
            textStyle = MaterialTheme.typography.bodyMedium,
            keyboardActions = KeyboardActions(),
            value = valueState.value,
            onValueChange = {
                valueState.value = it
                errorState.value = false
            }
        ) { innerTextField ->
            FormFieldDecoratedInput(valueState, placeholder, innerTextField)
        }
        FormFieldError(errorState.value, errorText.value)
    }
}

/**
 * Displays the decorated text field for the form field.
 */
@Composable
private fun FormFieldDecoratedInput(
    valueState: MutableState<String>,
    placeholder: String = "",
    innerTextField: @Composable () -> Unit
) {
    Row(
        Modifier.background(MaterialTheme.colorScheme.background),
        Arrangement.SpaceBetween,
        Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
                .heightIn(15.dp, 192.dp)
                .weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (valueState.value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
            innerTextField()
        }
    }
}

/**
 * Displays the form field label.
 */
@Composable
private fun FormFieldLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 4.dp),
        style = MaterialTheme.typography.headlineMedium
    )
}

/**
 * Displays the form field error text.
 */
@Composable
private fun FormFieldError(isError: Boolean, text: String) {
    if (isError) {
        Row(Modifier.padding(start = 4.dp)) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

/**
 * Displays the primary button.
 */
@Composable
public fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text, style = MaterialTheme.typography.headlineMedium)
    }
}

/**
 * Displays the secondary button.
 */
@Composable
public fun SecondaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(text, style = MaterialTheme.typography.headlineMedium)
    }
}
