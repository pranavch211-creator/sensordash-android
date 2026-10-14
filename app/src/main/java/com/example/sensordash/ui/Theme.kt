package com.example.sensordash.ui

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Colors shared by every panel (same palette as the browser dashboard). */
object Dash {
    val Bg = Color(0xFF04070D)
    val Panel1 = Color(0xFF0C1828)
    val Panel2 = Color(0xFF070D18)
    val Line = Color(0x4D28AAFF)
    val Cyan = Color(0xFF27B4FF)
    val Text = Color(0xFFEAF2FF)
    val Muted = Color(0xFF8FA3BD)
    val Ok = Color(0xFF2FE36B)
    val Warn = Color(0xFFFFA23C)
    val Red = Color(0xFFFF2A2A)
}

/** Screen-height based scale so text and spacing follow the real display size. */
val LocalScale = compositionLocalOf { 1f }

@Composable
fun sc(v: Float): Dp = (v * LocalScale.current).dp

@Composable
fun T(
    text: String,
    size: Float,
    color: Color = Dash.Text,
    weight: FontWeight = FontWeight.Medium,
    modifier: Modifier = Modifier,
    italic: Boolean = false,
    align: TextAlign = TextAlign.Unspecified,
) {
    BasicText(
        text,
        modifier,
        TextStyle(
            color = color,
            fontSize = (size * LocalScale.current).sp,
            fontWeight = weight,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            textAlign = align,
        ),
        overflow = TextOverflow.Clip,
        maxLines = 1,
    )
}
