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

package io.spine.examples.chatspn.desktop.chat

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection

/**
 * Creates a shape of message bubble.
 *
 * @param cornerRadius corner radius of the bubble
 * @param arrowWidth width of the message arrow
 * @param arrowPlace place of the message arrow
 * @param skipArrow if is `true` the bubble shape will be without an arrow, but with space for it
 */
@Composable
public fun MessageBubbleShape(
    cornerRadius: Float = 16f,
    arrowWidth: Float = 8f,
    arrowPlace: MessageBubbleArrowPlace,
    skipArrow: Boolean = false
): GenericShape {
    val density = LocalDensity.current.density
    return GenericShape { size: Size, _: LayoutDirection ->
        val settings = MessageShapeSettings(
            density,
            cornerRadius,
            arrowWidth,
            size.width,
            size.height,
            arrowPlace
        )
        if (skipArrow) {
            addRoundRect(roundedRectangle(settings))
            return@GenericShape
        }
        val arrowPath: Path
        when (arrowPlace) {
            MessageBubbleArrowPlace.LEFT_BOTTOM -> {
                addRoundRect(leftBottomArrowRectangle(settings))
                arrowPath = leftBottomArrowPath(settings)
            }
            MessageBubbleArrowPlace.RIGHT_BOTTOM -> {
                addRoundRect(rightBottomArrowRectangle(settings))
                arrowPath = rightBottomArrowPath(settings)
            }
        }
        this.op(this, arrowPath, PathOperation.Union)
    }
}

/**
 * Position of the message arrow.
 */
public enum class MessageBubbleArrowPlace {
    LEFT_BOTTOM, RIGHT_BOTTOM
}

/**
 * Returns a rectangle with all rounded edges and space allocated for the message arrow.
 */
private fun roundedRectangle(settings: MessageShapeSettings): RoundRect {
    val roundCorner = CornerRadius(settings.cornerRadius)
    val rectStart = when (settings.arrowPlace) {
        MessageBubbleArrowPlace.LEFT_BOTTOM -> settings.arrowWidth
        MessageBubbleArrowPlace.RIGHT_BOTTOM -> 0f
    }
    val rectEnd =
        when (settings.arrowPlace) {
            MessageBubbleArrowPlace.LEFT_BOTTOM -> settings.contentWidth
            MessageBubbleArrowPlace.RIGHT_BOTTOM -> settings.contentWidth - settings.arrowWidth
        }
    return RoundRect(
        rect = Rect(rectStart, 0f, rectEnd, settings.contentHeight),
        topLeft = roundCorner,
        topRight = roundCorner,
        bottomLeft = roundCorner,
        bottomRight = roundCorner
    )
}

/**
 * Returns a rectangle with rounded edges except for the left bottom edge
 * and with space allocated for the message arrow.
 */
private fun leftBottomArrowRectangle(settings: MessageShapeSettings): RoundRect {
    val roundCorner = CornerRadius(settings.cornerRadius)
    return RoundRect(
        rect = Rect(settings.arrowWidth, 0f, settings.contentWidth, settings.contentHeight),
        topLeft = roundCorner,
        topRight = roundCorner,
        bottomRight = roundCorner
    )
}

/**
 * Returns a rectangle with rounded edges except for the right bottom edge
 * and with space allocated for the message arrow.
 */
private fun rightBottomArrowRectangle(settings: MessageShapeSettings): RoundRect {
    val roundCorner = CornerRadius(settings.cornerRadius)
    return RoundRect(
        rect = Rect(
            0f,
            0f,
            settings.contentWidth - settings.arrowWidth,
            settings.contentHeight
        ),
        topLeft = roundCorner,
        topRight = roundCorner,
        bottomLeft = roundCorner
    )
}

/**
 * Returns `Path` to draw a message arrow on the left bottom edge of the message rectangle.
 */
private fun leftBottomArrowPath(settings: MessageShapeSettings): Path {
    return Path().apply {
        moveTo(settings.arrowRight, settings.arrowTop)
        lineTo(settings.arrowLeft, settings.arrowBottom)
        lineTo(settings.arrowRight, settings.arrowBottom)
        close()
    }
}

/**
 * Returns `Path` to draw a message arrow on the right bottom edge of the message rectangle.
 */
private fun rightBottomArrowPath(settings: MessageShapeSettings): Path {
    return Path().apply {
        moveTo(settings.arrowLeft, settings.arrowTop)
        lineTo(settings.arrowRight, settings.arrowBottom)
        lineTo(settings.arrowLeft, settings.arrowBottom)
        close()
    }
}

/**
 * Settings for the message shape creation.
 */
private class MessageShapeSettings(
    density: Float,
    val cornerRadius: Float,
    arrowWidth: Float,
    val contentWidth: Float,
    val contentHeight: Float,
    val arrowPlace: MessageBubbleArrowPlace,
) {
    val arrowWidth = (arrowWidth * density).coerceAtMost(contentWidth)
    val arrowHeight = (this.arrowWidth * 3 / 4 * density).coerceAtMost(contentHeight)
    val arrowLeft = when (arrowPlace) {
        MessageBubbleArrowPlace.LEFT_BOTTOM -> 0f
        MessageBubbleArrowPlace.RIGHT_BOTTOM -> contentWidth - this.arrowWidth
    }
    val arrowRight = when (arrowPlace) {
        MessageBubbleArrowPlace.LEFT_BOTTOM -> this.arrowWidth
        MessageBubbleArrowPlace.RIGHT_BOTTOM -> contentWidth
    }
    val arrowTop = contentHeight - arrowHeight
    val arrowBottom = contentHeight
}
