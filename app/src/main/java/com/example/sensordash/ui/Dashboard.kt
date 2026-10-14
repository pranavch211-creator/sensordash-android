package com.example.sensordash.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sensordash.data.ClockState
import com.example.sensordash.data.DeviceState
import kotlin.math.roundToInt

// ===== PHASE 2: every value below is a fixed DEMO placeholder, not a live reading. =====

@Composable
fun DashboardScreen(state: DeviceState) {
    BoxWithConstraints(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0A1424), Dash.Bg))),
    ) {
        val scale = (maxHeight.value / 780f).coerceIn(0.7f, 1.8f)
        CompositionLocalProvider(LocalScale provides scale) {
            Column(
                Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = sc(10f), vertical = sc(5f)),
                verticalArrangement = Arrangement.spacedBy(sc(6f)),
            ) {
                Header()
                Cluster(state, Modifier.weight(1.75f))
                SensorRow(Modifier.weight(1f))
                InfoRow(state, Modifier.weight(0.85f))
            }
        }
    }
}

// ---------- Header ----------

@Composable
private fun Header() {
    Row(
        Modifier.fillMaxWidth().height(sc(42f)).drawBehind {
            drawLine(Dash.Line, Offset(0f, size.height), Offset(size.width, size.height), 1.5f)
        },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(Modifier.size(sc(46f), sc(34f))) {
            val c = Offset(size.width / 2f, size.height * 0.85f)
            val r = size.height * 0.78f
            drawArc(Dash.Cyan, 140f, 260f, false, Offset(c.x - r, c.y - r), Size(r * 2, r * 2), style = Stroke(3f, cap = StrokeCap.Round))
            drawLine(Dash.Cyan, c, polar(c, r * 0.8f, 35f), 3.5f, StrokeCap.Round)
            drawCircle(Color.White, 4f, c)
        }
        Spacer(Modifier.width(sc(8f)))
        Column(verticalArrangement = Arrangement.Center) {
            Row {
                T("Sensor", 22f, Color.White, FontWeight.Bold, italic = true)
                T("Dash", 22f, Dash.Cyan, FontWeight.Bold, italic = true)
            }
            T("Live device dashboard", 10.5f, Dash.Muted)
        }
        Spacer(Modifier.weight(1f))
        Chip("MIXED: panels marked Demo are placeholders")
        Spacer(Modifier.width(sc(10f)))
        IconBox { // fullscreen brackets
            val w = size.width
            val k = w * 0.28f
            val m = w * 0.22f
            for ((sx, sy) in listOf(1f to 1f, -1f to 1f, 1f to -1f, -1f to -1f)) {
                val cx = if (sx > 0) m else w - m
                val cy = if (sy > 0) m else w - m
                drawLine(Dash.Cyan, Offset(cx, cy), Offset(cx + sx * k, cy), 2.5f, StrokeCap.Round)
                drawLine(Dash.Cyan, Offset(cx, cy), Offset(cx, cy + sy * k), 2.5f, StrokeCap.Round)
            }
        }
        Spacer(Modifier.width(sc(8f)))
        IconBox { // theme sun
            val c = center
            val r = size.width * 0.17f
            drawCircle(Color(0xFFFFB020), r, c)
            for (i in 0 until 8) {
                drawLine(Color(0xFFFFB020), polar(c, r * 1.6f, i * 45f), polar(c, r * 2.2f, i * 45f), 2.5f, StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun IconBox(draw: DrawScope.() -> Unit) {
    val shape = RoundedCornerShape(sc(9f))
    Canvas(
        Modifier.size(sc(36f)).clip(shape)
            .background(Brush.verticalGradient(listOf(Dash.Panel1, Dash.Panel2)), shape)
            .border(1.dp, Dash.Line, shape),
    ) { draw() }
}

// ---------- Top cluster ----------

@Composable
private fun Cluster(state: DeviceState, modifier: Modifier) {
    Row(
        modifier.fillMaxWidth().drawBehind { drawCockpit() }.padding(horizontal = sc(30f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GaugeCell(1f, 0.84f, 0f) {
            val b = state.battery
            InstrumentGauge(b.percent?.toFloat(), "BATTERY", b.percent?.let { "$it%" } ?: "N/A", b.status, Modifier.fillMaxSize())
        }
        GaugeCell(1.32f, 1f, -5f) {
            val c = state.cpu
            InstrumentGauge(
                c.percent, "APP CPU", c.percent?.let { "${it.roundToInt()}%" } ?: "--", c.note, Modifier.fillMaxSize(),
                GaugeStyle(labelY = -0.36f, readoutY = 0.34f, readoutSize = 0.27f, subY = 0.66f),
            )
        }
        GaugeCell(1f, 0.84f, 0f) {
            val m = state.memory
            InstrumentGauge(m.percent, "MEMORY", m.percent?.let { "${it.roundToInt()}%" } ?: "N/A", m.detail, Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun RowScope.GaugeCell(weight: Float, heightFrac: Float, raiseDp: Float, content: @Composable () -> Unit) {
    Box(Modifier.weight(weight).fillMaxHeight(), contentAlignment = Alignment.Center) {
        Box(
            Modifier.offset(y = sc(raiseDp)).fillMaxHeight(heightFrac)
                .aspectRatio(1f, matchHeightConstraintsFirst = true),
        ) { content() }
    }
}

private fun DrawScope.drawCockpit() {
    val w = size.width
    val h = size.height
    val pts = listOf(
        0f to .55f, .03f to .22f, .12f to .12f, .30f to .20f, .40f to .06f, .60f to .06f,
        .70f to .20f, .88f to .12f, .97f to .22f, 1f to .55f, .97f to .80f, .88f to .90f,
        .70f to .84f, .60f to .98f, .40f to .98f, .30f to .84f, .12f to .90f, .03f to .80f,
    )
    val p = Path()
    pts.forEachIndexed { i, pt -> if (i == 0) p.moveTo(pt.first * w, pt.second * h) else p.lineTo(pt.first * w, pt.second * h) }
    p.close()
    drawPath(p, Brush.verticalGradient(listOf(Color(0xFF121C2B), Color(0xFF05080E))))
    drawPath(p, Color(0x3327B4FF), style = Stroke(1.5f))
    // restrained red accent chevrons at both ends
    for ((x0, dir) in listOf(0.022f to 1f, 0.978f to -1f)) {
        val x = w * x0
        val a = Path().apply {
            moveTo(x + dir * w * 0.016f, h * 0.30f)
            lineTo(x, h * 0.50f)
            lineTo(x + dir * w * 0.016f, h * 0.70f)
        }
        drawPath(a, Color(0x44FF2A2A), style = Stroke(16f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(a, Color(0xFFE01616), style = Stroke(4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

// ---------- Sensor row: 5 panels ----------

@Composable
private fun SensorRow(modifier: Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(sc(6f))) {
        Panel("Accelerometer", Modifier.weight(1.3f).fillMaxHeight()) {
            AxisBlock(Triple("X", 0.35f, "-0.02 g"), Triple("Y", 0.33f, "0.04 g"), Triple("Z", 0.50f, "-0.98 g"))
            Footer("Motion detection")
        }
        Panel("Gyroscope", Modifier.weight(1.25f).fillMaxHeight()) {
            AxisBlock(Triple("X", 0.33f, "0.3 °/s"), Triple("Y", 0.32f, "-0.6 °/s"), Triple("Z", 0.48f, "1.2 °/s"))
            Footer("Rotation tracking")
        }
        Panel("Compass", Modifier.weight(0.9f).fillMaxHeight()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CompassDial(248f, Modifier.fillMaxHeight().aspectRatio(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                T("SW", 11f, Dash.Cyan)
                T("Heading", 10.5f, Dash.Muted)
            }
        }
        Panel("Location", Modifier.weight(1f).fillMaxHeight()) {
            Row(Modifier.weight(1f).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1.1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    RadarView(Modifier.fillMaxHeight().aspectRatio(1f))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(sc(3f))) {
                    T("12.9716° N", 11.5f)
                    T("77.5946° E", 11.5f)
                    T("± 3 m", 11.5f, Dash.Cyan)
                    T("Accuracy", 10f, Dash.Muted)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { Chip("Demo") }
        }
        Panel("Sound", Modifier.weight(1f).fillMaxHeight()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                InstrumentGauge(
                    64f, "", "64", null, Modifier.fillMaxHeight().aspectRatio(1f),
                    GaugeStyle(a0 = -125f, a1 = 125f, readoutY = 0.40f, readoutSize = 0.30f),
                )
            }
            T("Estimated microphone level", 10f, Dash.Muted, modifier = Modifier.fillMaxWidth(), align = TextAlign.Center)
        }
    }
}

@Composable
private fun ColumnScope.AxisBlock(vararg rows: Triple<String, Float, String>) {
    val colors = listOf(Color(0xFF0A5BD6) to Dash.Cyan, Color(0xFF0D9A3A) to Color(0xFF3DF07A), Color(0xFFC32A0C) to Color(0xFFFF5A22))
    Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.SpaceEvenly) {
        rows.forEachIndexed { i, (axis, fill, value) ->
            val shape = RoundedCornerShape(50)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                T(axis, 11.5f, Dash.Muted, modifier = Modifier.width(sc(18f)))
                Box(Modifier.weight(1f).height(sc(10f)).clip(shape).background(Color(0x59000000)).border(1.dp, Dash.Line, shape)) {
                    Box(
                        Modifier.fillMaxWidth(fill).fillMaxHeight()
                            .background(Brush.horizontalGradient(listOf(colors[i].first, colors[i].second)), shape),
                    )
                }
                T(value, 11.5f, modifier = Modifier.width(sc(60f)), align = TextAlign.End)
            }
        }
    }
}

@Composable
private fun Footer(label: String, chip: String = "Demo", ok: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        T(label, 11f, Dash.Muted)
        Chip(chip, ok)
    }
}

@Composable
private fun CompassDial(heading: Float, modifier: Modifier) {
    val tm = rememberTextMeasurer()
    Canvas(modifier) {
        val c = center
        val r = size.minDimension / 2f
        drawCircle(Brush.sweepGradient(Silver, center = c), r)
        drawCircle(Color(0xFF05080E), r * 0.94f)
        for (i in 0 until 36) {
            val major = i % 9 == 0
            drawLine(Color.White.copy(alpha = if (major) 1f else 0.6f), polar(c, r * 0.9f, i * 10f), polar(c, r * (if (major) 0.78f else 0.84f), i * 10f), r * 0.012f)
        }
        centeredText(tm, "N", polar(c, r * 0.66f, 0f), r * 0.15f, Dash.Red, FontWeight.Bold)
        centeredText(tm, "E", polar(c, r * 0.66f, 90f), r * 0.13f, Color.White)
        centeredText(tm, "S", polar(c, r * 0.66f, 180f), r * 0.13f, Color.White)
        centeredText(tm, "W", polar(c, r * 0.66f, 270f), r * 0.13f, Color.White)
        rotate(heading, pivot = c) {
            val up = Path().apply { moveTo(c.x - r * 0.06f, c.y); lineTo(c.x, c.y - r * 0.55f); lineTo(c.x + r * 0.06f, c.y); close() }
            val dn = Path().apply { moveTo(c.x - r * 0.06f, c.y); lineTo(c.x, c.y + r * 0.55f); lineTo(c.x + r * 0.06f, c.y); close() }
            drawPath(up, Dash.Red)
            drawPath(dn, Color(0xFF8FA3BD))
        }
        drawCircle(Color(0xFFDDDDDD), r * 0.05f, c)
        centeredText(tm, "${heading.toInt()}°", Offset(c.x, c.y + r * 0.36f), r * 0.2f, Dash.Cyan, FontWeight.Bold)
    }
}

@Composable
private fun RadarView(modifier: Modifier) {
    val tm = rememberTextMeasurer()
    Canvas(modifier) {
        val c = center
        val r = size.minDimension / 2f
        drawCircle(Color(0xFF050A12), r)
        drawCircle(Dash.Cyan.copy(alpha = 0.6f), r * 0.98f, style = Stroke(2f))
        for (f in listOf(0.3f, 0.6f, 0.85f)) drawCircle(Dash.Cyan.copy(alpha = 0.35f), r * f, style = Stroke(1.2f))
        for (a in listOf(0f, 45f, 90f, 135f)) {
            drawLine(Dash.Cyan.copy(alpha = 0.4f), polar(c, r * 0.97f, a), polar(c, r * 0.97f, a + 180f), 1f)
        }
        centeredText(tm, "N", polar(c, r * 0.8f, 0f), r * 0.14f, Color.White)
        centeredText(tm, "E", polar(c, r * 0.8f, 90f), r * 0.14f, Color.White)
        centeredText(tm, "S", polar(c, r * 0.8f, 180f), r * 0.14f, Color.White)
        centeredText(tm, "W", polar(c, r * 0.8f, 270f), r * 0.14f, Color.White)
        drawCircle(Dash.Cyan, r * 0.1f, c)
        drawCircle(Dash.Cyan, r * 0.2f, c, style = Stroke(2f))
    }
}

// ---------- Info row: 4 panels ----------

@Composable
private fun InfoRow(state: DeviceState, modifier: Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(sc(6f))) {
        Panel("Network", Modifier.weight(1f).fillMaxHeight()) {
            KvBlock("Connection" to state.network.connection, "Downlink" to state.network.down, "Uplink" to state.network.up)
            Footer("Status", state.network.status, state.network.online)
        }
        Panel("Clock", Modifier.weight(1.1f).fillMaxHeight()) {
            Row(Modifier.weight(1f).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(0.8f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    ClockFace(state.clock, Modifier.fillMaxHeight(0.95f).aspectRatio(1f))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(sc(2f))) {
                    T(state.clock.time, 26f, weight = FontWeight.Bold)
                    T(state.clock.date, 12f, Dash.Muted)
                    T(state.clock.weekday, 12f, Dash.Muted)
                }
            }
        }
        Panel("Device", Modifier.weight(1.1f).fillMaxHeight()) {
            KvBlock("Platform" to state.device.platform, "Model" to state.device.model, "Language" to state.device.language, "Screen" to state.device.screenDp)
        }
        Panel("Display", Modifier.weight(0.85f).fillMaxHeight()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                DisplayRing(state.display.resolution, Modifier.fillMaxHeight().aspectRatio(1f))
            }
            KvLine("Pixel ratio", state.display.density)
            KvLine("Orientation", state.display.orientation)
        }
    }
}

@Composable
private fun ColumnScope.KvBlock(vararg rows: Pair<String, String>) {
    Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.SpaceEvenly) {
        rows.forEach { (k, v) ->
            val shape = RoundedCornerShape(sc(6f))
            Row(
                Modifier.fillMaxWidth().clip(shape).background(Color(0x38000000)).border(1.dp, Dash.Line, shape)
                    .padding(horizontal = sc(8f), vertical = sc(3f)),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                T(k, 11f, Dash.Muted)
                T(v, 11.5f)
            }
        }
    }
}

@Composable
private fun KvLine(k: String, v: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = sc(1f)), horizontalArrangement = Arrangement.SpaceBetween) {
        T(k, 11f, Dash.Muted)
        T(v, 11.5f)
    }
}

@Composable
private fun ClockFace(clock: ClockState, modifier: Modifier) {
    val tm = rememberTextMeasurer()
    val hourDeg = (clock.hour % 12) * 30f + clock.minute * 0.5f + clock.second / 120f
    val minDeg = clock.minute * 6f + clock.second * 0.1f
    val secDeg = clock.second * 6f
    Canvas(modifier) {
        val c = center
        val r = size.minDimension / 2f
        drawCircle(Brush.sweepGradient(Silver, center = c), r)
        drawCircle(Color(0xFF070D18), r * 0.93f)
        for (h in 1..12) {
            val major = h % 3 == 0
            drawLine(Color.White, polar(c, r * 0.86f, h * 30f), polar(c, r * (if (major) 0.74f else 0.80f), h * 30f), r * (if (major) 0.03f else 0.015f))
        }
        centeredText(tm, "12", polar(c, r * 0.6f, 0f), r * 0.14f, Color.White)
        centeredText(tm, "3", polar(c, r * 0.6f, 90f), r * 0.14f, Color.White)
        centeredText(tm, "6", polar(c, r * 0.6f, 180f), r * 0.14f, Color.White)
        centeredText(tm, "9", polar(c, r * 0.6f, 270f), r * 0.14f, Color.White)
        drawLine(Color.White, c, polar(c, r * 0.45f, hourDeg), r * 0.06f, StrokeCap.Round)
        drawLine(Color.White, c, polar(c, r * 0.68f, minDeg), r * 0.04f, StrokeCap.Round)
        drawLine(Dash.Red, polar(c, -r * 0.15f, secDeg), polar(c, r * 0.76f, secDeg), r * 0.015f)
        drawCircle(Dash.Red, r * 0.04f, c)
    }
}

@Composable
private fun DisplayRing(resolution: String, modifier: Modifier) {
    val tm = rememberTextMeasurer()
    Canvas(modifier) {
        val c = center
        val r = size.minDimension / 2f
        drawCircle(Color(0xFF050A12), r * 0.9f)
        drawCircle(Color(0x6627B4FF), r * 0.9f, style = Stroke(3f))
        drawArc(Dash.Cyan, -110f, 130f, false, Offset(c.x - r * 0.9f, c.y - r * 0.9f), Size(r * 1.8f, r * 1.8f), style = Stroke(r * 0.05f, cap = StrokeCap.Round))
        centeredText(tm, resolution, Offset(c.x, c.y - r * 0.1f), r * 0.2f, Color.White, FontWeight.SemiBold)
        centeredText(tm, "pixels", Offset(c.x, c.y + r * 0.18f), r * 0.14f, Dash.Muted)
    }
}

// ---------- Shared panel pieces ----------

@Composable
private fun Panel(title: String, modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(sc(10f))
    Column(
        modifier.clip(shape)
            .background(Brush.linearGradient(listOf(Dash.Panel1, Dash.Panel2)), shape)
            .border(1.dp, Dash.Line, shape)
            .padding(horizontal = sc(10f), vertical = sc(6f)),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.size(sc(9f))) {
                val p = Path().apply {
                    moveTo(size.width / 2f, 0f); lineTo(size.width, size.height / 2f)
                    lineTo(size.width / 2f, size.height); lineTo(0f, size.height / 2f); close()
                }
                drawPath(p, Dash.Cyan)
            }
            Spacer(Modifier.width(sc(7f)))
            T(title, 13f, weight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(sc(3f)))
        content()
    }
}

@Composable
private fun Chip(text: String, ok: Boolean = false) {
    val col = if (ok) Dash.Ok else Dash.Warn
    Row(
        Modifier.clip(CircleShape).background(col.copy(alpha = 0.12f))
            .border(1.dp, col.copy(alpha = 0.5f), CircleShape)
            .padding(horizontal = sc(8f), vertical = sc(2f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(sc(6f)).background(col, CircleShape))
        Spacer(Modifier.width(sc(5f)))
        T(text, 10.5f, col)
    }
}
