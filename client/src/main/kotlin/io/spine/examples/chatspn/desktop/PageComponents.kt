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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import java.awt.Cursor
import kotlin.math.abs

/**
 * Displays the top bar with the configurable content.
 *
 * @param content content of the top bar
 */
@Composable
public fun TopBar(content: @Composable () -> Unit) {
    Surface(
        Modifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(bottom = 1.dp)
            .drawBehind {
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            },
    ) {
        content()
    }
}

/**
 * Displays the text button without a background and with an optional icon.
 *
 * @param text text on the button
 * @param contentColor color of the text and icon
 * @param icon icon to be displayed before text
 * @param onClick callback that will be triggered when the button clicked
 */
@Composable
public fun TextButton(
    text: String,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Button(
        onClick,
        Modifier.pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(16.dp, 4.dp)
    ) {
        if (icon != null) {
            Icon(
                icon,
                text,
                Modifier.size(20.dp),
                contentColor
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Displays the default modal window.
 *
 * @param isVisibleState mutable state of modal window visibility
 * @param content content of the modal window
 */
@Composable
public fun ModalWindow(
    isVisibleState: MutableState<Boolean>,
    onDismiss: () -> Unit = {},
    content: @Composable () -> Unit
) {
    if (isVisibleState.value) {
        ShadedBackground()
        Popup(
            popupPositionProvider = WindowCenterOffsetPositionProvider(),
            onDismissRequest = {
                isVisibleState.value = false
                onDismiss()
            },
            focusable = true
        ) {
            Surface(
                elevation = 8.dp,
                shape = MaterialTheme.shapes.small
            ) {
                content()
            }
        }
    }
}

/**
 * [PopupPositionProvider] with the position in the center of the window.
 */
private class WindowCenterOffsetPositionProvider(
    private val x: Int = 0,
    private val y: Int = 0
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        return IntOffset(
            (windowSize.width - popupContentSize.width) / 2 + x,
            (windowSize.height - popupContentSize.height) / 2 + y
        )
    }
}

/**
 * Displays the partially transparent black background.
 *
 * The user cannot click on elements behind it.
 */
@Composable
public fun ShadedBackground() {
    Popup {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0x33000000))
        )
    }
}

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

/**
 * Displays the icon button.
 *
 * @param icon icon to display
 * @param onClick callback that will be triggered when the icon clicked
 * @param color icon color
 * @param modifierExtender extension for the modifier
 */
@Composable
public fun IconButton(
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    modifierExtender: Modifier.() -> Modifier = { this },
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Icon(
        icon,
        "Input icon",
        Modifier
            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .modifierExtender(),
        color
    )
}
