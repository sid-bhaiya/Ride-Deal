package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Bike
import com.example.ui.theme.*

@Composable
fun StatusPill(status: String, modifier: Modifier = Modifier) {
    val colorPair = when (status) {
        "Available" -> Pair(StatusSuccess, StatusSuccess.copy(alpha = 0.12f))
        "Approved", "Returned" -> Pair(StatusSuccess, StatusSuccess.copy(alpha = 0.12f))
        "Active" -> Pair(LightPrimary, LightPrimary.copy(alpha = 0.12f))
        "Pending" -> Pair(StatusPending, StatusPending.copy(alpha = 0.12f))
        "Due Payment" -> Pair(StatusError, StatusError.copy(alpha = 0.12f))
        "Cancelled" -> Pair(LightTertiary, LightTertiary.copy(alpha = 0.12f))
        else -> Pair(StatusWarning, StatusWarning.copy(alpha = 0.12f))
    }

    Surface(
        color = colorPair.second,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(colorPair.first, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status,
                color = colorPair.first,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BikeCardItem(
    bike: Bike,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (bike.status) {
        "Available" -> StatusSuccess
        "Active" -> LightPrimary
        "Pending" -> StatusPending
        "Due Payment" -> StatusError
        else -> StatusWarning
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("bike_card_${bike.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                // Simulated Photo Box with stylish gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Background icon or styling
                    Icon(
                        imageVector = Icons.Default.TwoWheeler,
                        contentDescription = "Bike Icon",
                        tint = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.size(90.dp)
                    )

                    // Bottom gradient scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                )
                            )
                    )

                    // Top left badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        StatusPill(status = bike.status)
                    }

                    // Brand sticker top-right
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = bike.brand,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Bottom Info Overlay
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bike.model,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${bike.cc} cc Motor",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Price and spec details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = bike.specs,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Rent",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Text(
                                text = "৳ ${bike.rentalPrice.toInt()} / Day",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = onClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Book Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Absolute top-to-bottom status indicator bar on the right side
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(statusColor)
                    .align(Alignment.CenterEnd)
            )
        }
    }
}

/**
 * A finger drawing interactive Canvas component for e-signatures
 */
@Composable
fun ESignatureCanvas(
    modifier: Modifier = Modifier,
    onSignatureChanged: (String) -> Unit // returns serialized stroke details
) {
    val paths = remember { mutableStateListOf<Offset>() }
    var pointsString by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
    ) {
        if (paths.isEmpty()) {
            Text(
                text = "Draw Your E-Signature Here with Finger",
                color = Color.Gray.copy(alpha = 0.6f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            paths.add(offset)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val lastPoint = paths.lastOrNull()
                            if (lastPoint != null) {
                                val nextPoint = lastPoint + dragAmount
                                paths.add(nextPoint)
                            }
                        },
                        onDragEnd = {
                            // Serialize points to coordinates string to save to Room
                            val s = paths.joinToString(";") { "${it.x.toInt()},${it.y.toInt()}" }
                            pointsString = s
                            onSignatureChanged(s)
                        }
                    )
                }
        ) {
            if (paths.size > 1) {
                val path = Path()
                path.moveTo(paths[0].x, paths[0].y)
                for (i in 1 until paths.size) {
                    // Check if is a continuous line segment or connection
                    path.lineTo(paths[i].x, paths[i].y)
                }

                drawPath(
                    path = path,
                    color = Color.Black,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        IconButton(
            onClick = {
                paths.clear()
                pointsString = ""
                onSignatureChanged("")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Clear Signature",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Draws a gorgeous custom chart using raw Canvas coordinates
 */
@Composable
fun GlowLineChart(
    points: List<Float>, // float ratios [0..1]
    labels: List<String>,
    modifier: Modifier = Modifier,
    lineColor: Color = LightPrimary,
    glowColor: Color = LightPrimary.copy(alpha = 0.2f)
) {
    Canvas(modifier = modifier) {
        if (points.isEmpty()) return@Canvas

        val w = size.width
        val h = size.height
        val paddingX = 40f
        val paddingY = 40f

        val chartWidth = w - paddingX * 2
        val chartHeight = h - paddingY * 2

        val stepX = chartWidth / (points.size - 1).coerceAtLeast(1)

        // Draw horizontal grid lines
        val gridCount = 4
        for (g in 0..gridCount) {
            val yPos = paddingY + chartHeight * (g.toFloat() / gridCount)
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(paddingX, yPos),
                end = Offset(w - paddingX, yPos),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw the filled glow under track
        val fillPath = Path()
        fillPath.moveTo(paddingX, paddingY + chartHeight)
        
        for (i in points.indices) {
            val cx = paddingX + i * stepX
            val cy = paddingY + chartHeight * (1f - points[i])
            fillPath.lineTo(cx, cy)
        }
        fillPath.lineTo(paddingX + (points.size - 1) * stepX, paddingY + chartHeight)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(glowColor, Color.Transparent)
            )
        )

        // Draw line trace
        val linePath = Path()
        linePath.moveTo(paddingX, paddingY + chartHeight * (1f - points[0]))
        for (i in 1 until points.size) {
            val cx = paddingX + i * stepX
            val cy = paddingY + chartHeight * (1f - points[i])
            linePath.lineTo(cx, cy)
        }

        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw anchor points
        for (i in points.indices) {
            val cx = paddingX + i * stepX
            val cy = paddingY + chartHeight * (1f - points[i])
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = Offset(cx, cy)
            )
            drawCircle(
                color = lineColor,
                radius = 3.dp.toPx(),
                center = Offset(cx, cy)
            )
        }
    }
}

/**
 * Draw a beautiful simulated barcode/QR code block for printed invoices
 */
@Composable
fun SimulatedBarcode(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        var currentX = 0f
        val random = java.util.Random(42) // secure seed
        
        while (currentX < w) {
            val barWidth = (random.nextInt(4) + 1) * 3f
            val spaceWidth = (random.nextInt(3) + 1) * 3f
            
            drawRect(
                color = Color.Black,
                topLeft = Offset(currentX, 0f),
                size = androidx.compose.ui.geometry.Size(barWidth, h)
            )
            currentX += barWidth + spaceWidth
        }
    }
}

@Composable
fun SimulatedQrCode(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val minSize = minOf(w, h)
        val cells = 15
        val cellSize = minSize / cells

        val random = java.util.Random(1337)
        for (r in 0 until cells) {
            for (c in 0 until cells) {
                // Border squares (QR anchors)
                val isAnchor = (r < 4 && c < 4) || (r < 4 && c >= cells - 4) || (r >= cells - 4 && c < 4)
                val isAnchorBorder = isAnchor && (r == 0 || r == 3 || c == 0 || c == 3 || (c == cells - 1 || c == cells - 4) || (r == cells - 1 || r == cells - 4))
                val isAnchorCenter = isAnchor && (r == 1 && c == 1 || r == 1 && c == cells - 2 || r == cells - 2 && c == 1)

                val drawBlack = if (isAnchor) {
                    isAnchorBorder || isAnchorCenter
                } else {
                    random.nextBoolean()
                }

                if (drawBlack) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(c * cellSize, r * cellSize),
                        size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                    )
                }
            }
        }
    }
}
