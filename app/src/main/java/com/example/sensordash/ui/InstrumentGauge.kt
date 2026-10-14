package com.example.sensordash.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/** Scale layout. Angles are degrees clockwise from 12 o'clock. */
data class GaugeStyle(
    val a0: Float = -135f,
    val a1: Float = 135f,
    val max: Float = 100f,
    val labelY: Float = -0.30f,
    val readoutY: Float = 0.30f,
    val readoutSize: Float = 0.26f,
    val subY: Float = 0.60f,
)

internal fun polar(c: Offset, rad: Float, deg: Float): Offset {
    val t = Math.toRadians(deg.toDouble())
    return Offset(c.x + rad * sin(t).toFloat(), c.y - rad * cos(t).toFloat())
}

internal fun DrawScope.centeredText(
    tm: TextMeasurer, s: String, at: Offset, px: Float, color: Color,
    weight: FontWeight = FontWeight.Medium,
) {
    val l = tm.measure(s, TextStyle(color = color, fontSize = px.toSp(), fontWeight = weight))
    drawText(l, topLeft = Offset(at.x - l.size.width / 2f, at.y - l.size.height / 2f))
}

internal val Silver = listOf(
    Color(0xFFEEF3F9), Color(0xFF4A5360), Color(0xFFCFD8E3), Color(0xFF262C35), Color(0xFFEEF3F9),
)

/**
 * Reusable analog instrument. [value] is on the 0..style.max scale; null draws a dimmed,
 * parked needle (used later for unavailable readings). The needle animates on change.
 */
@Composable
fun InstrumentGauge(
    value: Float?,
    label: String,
    readout: String,
    sub: String?,
    modifier: Modifier = Modifier,
    style: GaugeStyle = GaugeStyle(),
) {
    val tm = rememberTextMeasurer()
    var target by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(value) { target = value ?: 0f }
    val shown by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "needle",
    )
    val dim = value == null

    Canvas(modifier) {
        val c = center
        val r = size.minDimension / 2f
        fun ang(v: Float) = style.a0 + (style.a1 - style.a0) * (v / style.max).coerceIn(0f, 1f)

        // Metallic bezel, inner ring, deep-black face
        drawCircle(Brush.sweepGradient(Silver, center = c), r)
        drawCircle(Color(0xFF05080E), r * 0.95f)
        drawCircle(Brush.sweepGradient(Silver, center = c), r * 0.935f, style = Stroke(r * 0.012f))
        drawCircle(
            Brush.radialGradient(listOf(Color(0xFF0B1220), Color.Black, Color(0xFF0A0F18)), center = c, radius = r * 0.9f),
            r * 0.9f,
        )

        // Colored zones: green -> yellow -> orange -> red
        val zr = r * 0.80f
        val n = 60
        val step = (style.a1 - style.a0) / n
        for (i in 0 until n) {
            val t = i / n.toFloat()
            drawArc(
                color = Color.hsv(125f * (1f - t.pow(1.3f)), 1f, 0.92f),
                startAngle = style.a0 + step * i - 90f,
                sweepAngle = step * 1.25f,
                useCenter = false,
                topLeft = Offset(c.x - zr, c.y - zr),
                size = Size(zr * 2f, zr * 2f),
                alpha = if (dim) 0.35f else 1f,
                style = Stroke(r * 0.07f),
            )
        }

        // 51 ticks (every 2 units), major every 20 with numerals
        for (i in 0..50) {
            val a = ang(style.max * i / 50f)
            val major = i % 10 == 0
            drawLine(
                Color.White.copy(alpha = if (major) 1f else 0.7f),
                polar(c, r * 0.735f, a),
                polar(c, r * (if (major) 0.62f else 0.68f), a),
                strokeWidth = r * (if (major) 0.014f else 0.006f),
            )
            if (major) {
                centeredText(tm, (style.max * i / 50f).toInt().toString(), polar(c, r * 0.52f, a), r * 0.085f, Color.White, FontWeight.SemiBold)
            }
        }

        // Name, digital readout, secondary pill
        if (label.isNotEmpty()) {
            centeredText(tm, label, Offset(c.x, c.y + style.labelY * r), r * 0.085f, Color.White, FontWeight.SemiBold)
        }
        centeredText(
            tm, readout, Offset(c.x, c.y + style.readoutY * r), r * style.readoutSize,
            if (dim) Dash.Muted else Color.White, FontWeight.Bold,
        )
        if (sub != null) {
            val sl = tm.measure(sub, TextStyle(color = Color(0xFF9FD8FF), fontSize = (r * 0.075f).toSp()))
            val pw = sl.size.width + r * 0.16f
            val ph = sl.size.height + r * 0.06f
            val tl = Offset(c.x - pw / 2f, c.y + style.subY * r - ph / 2f)
            drawRoundRect(Color(0x66101C2C), tl, Size(pw, ph), CornerRadius(ph / 2f))
            drawRoundRect(Color(0x4427B4FF), tl, Size(pw, ph), CornerRadius(ph / 2f), style = Stroke(1.2f))
            drawText(sl, topLeft = Offset(c.x - sl.size.width / 2f, tl.y + (ph - sl.size.height) / 2f))
        }

        // Needle + metallic hub
        rotate(ang(shown), pivot = c) {
            val p = Path().apply {
                moveTo(c.x - r * 0.022f, c.y + r * 0.12f)
                lineTo(c.x, c.y - r * 0.74f)
                lineTo(c.x + r * 0.022f, c.y + r * 0.12f)
                close()
            }
            drawPath(p, Dash.Red.copy(alpha = if (dim) 0.4f else 1f))
        }
        drawCircle(
            Brush.radialGradient(listOf(Color(0xFF9AA6B5), Color(0xFF2A313C)), center = c, radius = r * 0.07f),
            r * 0.07f,
        )
        drawCircle(Dash.Red, r * 0.025f)
    }
}
