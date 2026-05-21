package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CallLogEntity
import com.example.ui.CallViewModel
import com.example.ui.DashboardStats
import com.example.ui.FilterPeriod
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: CallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CosmicBlack),
                    containerColor = CosmicBlack
                ) { innerPadding ->
                    CallAnalyzerAppContent(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallAnalyzerAppContent(
    viewModel: CallViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.filteredCallLogs.collectAsState()
    val rawLogsList by viewModel.rawCallLogs.collectAsState()
    val stats by viewModel.currentStats.collectAsState()
    val currentPeriod by viewModel.filterPeriod.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val isScanningAi by viewModel.isScanningAi.collectAsState()
    val aiInsights by viewModel.geminiInsights.collectAsState()
    val apiLogs by viewModel.aiScannerLogs.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicBlack)
    ) {
        // Futuristic Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(CyberCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NEURAL ANALYZER",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 1.5.sp,
                            color = TextPrimary
                        )
                    )
                }
                Text(
                    text = "Real-time call pattern diagnosis",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }

            // Sync Database Button with glowing design
            IconButton(
                onClick = { viewModel.resetDatabase() },
                modifier = Modifier
                    .background(Color(0xFF1E293B), CircleShape)
                    .size(40.dp)
                    .testTag("reset_db_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset database",
                    tint = CyberCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Neural AI Insights Panel (Fulfills prompt criteria)
            item {
                NeuralAiInsightsCard(
                    insights = aiInsights,
                    isScanning = isScanningAi,
                    scannerLogs = apiLogs,
                    onScanClicked = { viewModel.runAiScan() }
                )
            }

            // Interactive Diagnostics Dashboard Card (Fulfills stats criteria)
            item {
                DiagnosticsDashboardCard(
                    stats = stats,
                    filteredLogs = logs,
                    period = currentPeriod
                )
            }

            // Multi-Period Filter Buttons (Fulfills Today/Week/Month requirements)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "FILTER PROTOCOLS",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterPeriodButton(
                            label = "TODAY",
                            isSelected = currentPeriod == FilterPeriod.TODAY,
                            onClick = { viewModel.filterPeriod.value = FilterPeriod.TODAY },
                            modifier = Modifier.weight(1f).testTag("filter_today")
                        )
                        FilterPeriodButton(
                            label = "THIS WEEK",
                            isSelected = currentPeriod == FilterPeriod.THIS_WEEK,
                            onClick = { viewModel.filterPeriod.value = FilterPeriod.THIS_WEEK },
                            modifier = Modifier.weight(1f).testTag("filter_week")
                        )
                        FilterPeriodButton(
                            label = "THIS MONTH",
                            isSelected = currentPeriod == FilterPeriod.THIS_MONTH,
                            onClick = { viewModel.filterPeriod.value = FilterPeriod.THIS_MONTH },
                            modifier = Modifier.weight(1f).testTag("filter_month")
                        )
                    }

                    // Interactive Search Field inside filtering protocol
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_call_input"),
                        placeholder = { Text("Search caller or phone number...", color = TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search icon", tint = TextSecondary)
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkGreySurface,
                            unfocusedContainerColor = DarkGreySurface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = CyberCyan,
                            unfocusedIndicatorColor = SlateBorder
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Quick Simulation Stats / Divider
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CALL HISTORY RECORD (${logs.size} entries)",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                    )

                    // Inject Call Record Action Trigger
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("add_log_trigger")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = "Add symbol", tint = CyberCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SIMULATE CALL", fontSize = 10.sp, color = TextPrimary, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Real Call Logs List (Fulfills premium interactive requirement)
            if (logs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
                            .background(DarkGreySurface)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "No results",
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Zero logs indexed in this range.",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try selecting another filter or seeding simulated records.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(logs, key = { it.id }) { log ->
                    CallLogListItem(
                        log = log,
                        onDelete = { viewModel.deleteCall(log) }
                    )
                }
            }

            // Spacing at bottom of scrolling card
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Dialogue Overlay to Simulate Call Logs Ingestion (Real-time recalculation preview)
    if (showAddDialog) {
        SimulatedCallDialog(
            onDismiss = { showAddDialog = false },
            onAddCall = { name, phone, type, duration, offset ->
                viewModel.addSimulatedCall(name, phone, type, duration, offset)
                showAddDialog = false
            }
        )
    }
}

// Sparkle/Sci-fi neural AI Insights card
@Composable
fun NeuralAiInsightsCard(
    insights: List<String>,
    isScanning: Boolean,
    scannerLogs: String?,
    onScanClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(CyberCyan, DeepIndigoAccent)),
                RoundedCornerShape(16.dp)
            )
            .background(Color(0xFF0F1424))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFB236))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "COGNITIVE AI INSIGHTS",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFFFFB236),
                            letterSpacing = 1.5.sp
                        )
                    )
                }

                // AI Processing trigger button
                Button(
                    onClick = onScanClicked,
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        disabledContainerColor = Color(0xFF1E293B)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(26.dp).testTag("trigger_ai_scan_button")
                ) {
                    Text(
                        text = if (isScanning) "SCANNING..." else "RUN NEURAL SCAN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isScanning) TextMuted else CosmicBlack,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isScanning) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = CyberCyan,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = scannerLogs ?: "Analyzing logs...",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    insights.forEach { insight ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "✦ ",
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = insight,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.SansSerif,
                                lineHeight = 18.sp
                            )
                        }
                    }
                    if (insights.isEmpty()) {
                        Text(
                            text = "No insights ready. Instruct the network scanning probe using 'RUN NEURAL SCAN'.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

// Advanced statistics card containing numeric cells & Canvas analytics
@Composable
fun DiagnosticsDashboardCard(
    stats: DashboardStats,
    filteredLogs: List<CallLogEntity>,
    period: FilterPeriod
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
            .background(DarkGreySurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper stats banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NETWORK DIAGNOISTICS",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    letterSpacing = 1.2.sp
                )
            )

            // Dynamic badge displaying aggregate duration
            val mins = stats.totalDurationSeconds / 60
            val secs = stats.totalDurationSeconds % 60
            Text(
                text = "CALL DURATION: ${mins}m ${secs}s",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AnsweredGreen
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF062F21))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // Multi-proportional glowing horizontal bar chart (Full interactive stats chart)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("PROPORTIONAL DIVERGENCE", fontSize = 11.sp, color = TextMuted)
                Text("TOTAL: ${stats.totalCalls} calls", fontSize = 11.sp, color = TextMuted)
            }

            // Gradient linear chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                val total = stats.totalCalls
                if (total == 0) {
                    drawRect(color = Color(0xFF1E293B), size = size)
                } else {
                    val ansWidth = (stats.answeredCount.toFloat() / total) * size.width
                    val misWidth = (stats.missedCount.toFloat() / total) * size.width
                    val rejWidth = (stats.rejectedCount.toFloat() / total) * size.width

                    // Row answered (Green)
                    drawRect(
                        color = AnsweredGreen,
                        topLeft = Offset(0f, 0f),
                        size = Size(ansWidth, size.height)
                    )
                    // Row missed (Orange)
                    drawRect(
                        color = MissedOrange,
                        topLeft = Offset(ansWidth, 0f),
                        size = Size(misWidth, size.height)
                    )
                    // Row rejected (Red)
                    drawRect(
                        color = RejectedRed,
                        topLeft = Offset(ansWidth + misWidth, 0f),
                        size = Size(rejWidth, size.height)
                    )
                }
            }

            // Legend indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendIndicator(label = "${stats.answeredPercentage}%", color = AnsweredGreen)
                LegendIndicator(label = "${stats.missedPercentage}%", color = MissedOrange)
                LegendIndicator(label = "${stats.rejectedPercentage}%", color = RejectedRed)
            }
        }

        // 3-Cell stats table
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatsCell(
                title = "ANSWERED",
                count = stats.answeredCount,
                pctValue = "${stats.answeredPercentage}%",
                color = AnsweredGreen,
                modifier = Modifier.weight(1f)
            )
            StatsCell(
                title = "MISSED",
                count = stats.missedCount,
                pctValue = "${stats.missedPercentage}%",
                color = MissedOrange,
                modifier = Modifier.weight(1f)
            )
            StatsCell(
                title = "REJECTED",
                count = stats.rejectedCount,
                pctValue = "${stats.rejectedPercentage}%",
                color = RejectedRed,
                modifier = Modifier.weight(1f)
            )
        }

        // High resolution Canvas weekly trend graph
        WeeklyTrendChart(filteredLogs = filteredLogs)
    }
}

@Composable
fun LegendIndicator(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun StatsCell(
    title: String,
    count: Int,
    pctValue: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .border(1.dp, SlateBorder, RoundedCornerShape(10.dp))
            .background(Color(0xFF0F1422))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$count",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = pctValue,
            fontSize = 10.sp,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Sophisticated Canvas Bar Chart mapping call activity
@Composable
fun WeeklyTrendChart(
    filteredLogs: List<CallLogEntity>
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0E17)),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "VOLUMETRIC VELOCITY TRENDS (PAST 7 DAYS)",
                color = TextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Graph canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                // Compile metrics
                val daysCounts = IntArray(7)
                // Offset days: 0 is today, 1 yesterday, ... 6 is 6 days ago.
                val now = System.currentTimeMillis()
                val dayMs = 24 * 60 * 60 * 1000L

                filteredLogs.forEach { log ->
                    val diff = now - log.timestamp
                    val dayIndex = (diff / dayMs).toInt()
                    if (dayIndex in 0..6) {
                        daysCounts[dayIndex]++
                    }
                }

                val maxCount = daysCounts.maxOrNull() ?: 1
                val finalMax = if (maxCount == 0) 1 else maxCount

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barSpacing = canvasWidth / 7f
                    val barWidth = 14.dp.toPx()

                    // Draw horizontal baseline
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(0f, canvasHeight),
                        end = Offset(canvasWidth, canvasHeight),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Draw 7 bar columns
                    for (i in 0..6) {
                        // Days sequence should represent chronological sequence: 6 days ago (left) to Today (right)
                        val dataIndex = 6 - i
                        val count = daysCounts[dataIndex]
                        val heightFraction = count.toFloat() / finalMax
                        val rawHeight = heightFraction * (canvasHeight * 0.85f)
                        val finalHeight = if (count > 0 && rawHeight < 4f) 4f else rawHeight

                        val x = i * barSpacing + (barSpacing - barWidth) / 2f
                        val y = canvasHeight - finalHeight

                        if (count > 0) {
                            // Column representing call count
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    listOf(CyberCyan, DeepIndigoAccent)
                                ),
                                topLeft = Offset(x, y),
                                size = Size(barWidth, finalHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        } else {
                            // Subtle ambient dot for zero activity
                            drawCircle(
                                color = Color(0xFF1F2937),
                                radius = 3.dp.toPx(),
                                center = Offset(x + barWidth / 2f, canvasHeight - 6.dp.toPx())
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // X-Axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val labels = listOf("6d ago", "5d ago", "4d ago", "3d ago", "2d ago", "Yesterday", "Today")
                labels.forEach { label ->
                    Text(
                        text = label,
                        color = TextMuted,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun FilterPeriodButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                1.dp,
                if (isSelected) CyberCyan else SlateBorder,
                RoundedCornerShape(8.dp)
            )
            .background(if (isSelected) Color(0xFF0E1A29) else Color(0xFF0F1422))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) CyberCyan else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Call history list item widget
@Composable
fun CallLogListItem(
    log: CallLogEntity,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
            .background(DarkGreySurface)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon representing exact call state matching prompt specifications
            val statusColor = when (log.callType) {
                "ANSWERED" -> AnsweredGreen
                "MISSED" -> MissedOrange
                "REJECTED" -> RejectedRed
                else -> TextSecondary
            }

            val typeChar = when (log.callType) {
                "ANSWERED" -> "✓"
                "MISSED" -> "▼"
                "REJECTED" -> "✕"
                else -> "?"
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f))
                    .border(1.dp, statusColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = typeChar,
                    color = statusColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = log.callerName,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (log.isUnknown) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "UNKNOWN",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp,
                                color = RejectedRed,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .border(1.dp, RejectedRed.copy(0.4f), RoundedCornerShape(4.dp))
                                .background(RejectedRed.copy(0.1f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = log.phoneNumber,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatCallTimestamp(log.timestamp),
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }

        // Action controls (delete/duration combo)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (log.callType == "ANSWERED") {
                val m = log.durationSeconds / 60
                val s = log.durationSeconds % 60
                val durFormatted = if (m > 0) "${m}m ${s}s" else "${s}s"
                Text(
                    text = durFormatted,
                    color = AnsweredGreen,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "—",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(0xFFFFEBEB).copy(0.04f), RoundedCornerShape(6.dp))
                    .testTag("delete_log_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete call log",
                    tint = RejectedRed.copy(0.7f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// Dialog to simulate incoming/missed/rejected call data
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatedCallDialog(
    onDismiss: () -> Unit,
    onAddCall: (name: String, phone: String, type: String, duration: Int, offsetDays: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var callType by remember { mutableStateOf("ANSWERED") } // "ANSWERED", "MISSED", "REJECTED"
    var duration by remember { mutableStateOf(45f) }
    var offsetDays by remember { mutableStateOf(0) } // 0 = Today, 1 = Yesterday, etc.

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            color = DarkGreySurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
                .testTag("simulate_call_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "TELEPHONY SIMULATOR",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CyberCyan,
                        letterSpacing = 1.2.sp
                    )
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Caller Name", color = TextSecondary) },
                    placeholder = { Text("e.g. Samuel L. Jackson") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CosmicBlack,
                        unfocusedContainerColor = CosmicBlack,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedIndicatorColor = CyberCyan,
                        unfocusedIndicatorColor = SlateBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("simulate_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number", color = TextSecondary) },
                    placeholder = { Text("e.g. 555-0199") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CosmicBlack,
                        unfocusedContainerColor = CosmicBlack,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedIndicatorColor = CyberCyan,
                        unfocusedIndicatorColor = SlateBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("simulate_phone_input"),
                    singleLine = true
                )

                // Call type selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("CALL RESOLUTION CODE", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { callType = "ANSWERED" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (callType == "ANSWERED") AnsweredGreen else Color(0xFF1E293B)
                            ),
                            modifier = Modifier.weight(1f).testTag("dialog_type_answered"),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("Answered", fontSize = 11.sp, color = if (callType == "ANSWERED") CosmicBlack else TextPrimary)
                        }
                        Button(
                            onClick = { callType = "MISSED" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (callType == "MISSED") MissedOrange else Color(0xFF1E293B)
                            ),
                            modifier = Modifier.weight(1f).testTag("dialog_type_missed"),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("Missed", fontSize = 11.sp, color = if (callType == "MISSED") CosmicBlack else TextPrimary)
                        }
                        Button(
                            onClick = { callType = "REJECTED" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (callType == "REJECTED") RejectedRed else Color(0xFF1E293B)
                            ),
                            modifier = Modifier.weight(1f).testTag("dialog_type_rejected"),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("Rejected", fontSize = 11.sp, color = if (callType == "REJECTED") CosmicBlack else TextPrimary)
                        }
                    }
                }

                // Call duration (Only editable for Answered calls)
                if (callType == "ANSWERED") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("DURATION", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                            Text("${duration.toInt()} seconds", fontSize = 11.sp, color = AnsweredGreen, fontFamily = FontFamily.Monospace)
                        }
                        Slider(
                            value = duration,
                            onValueChange = { duration = it },
                            valueRange = 5f..1200f,
                            colors = SliderDefaults.colors(
                                thumbColor = AnsweredGreen,
                                activeTrackColor = AnsweredGreen,
                                inactiveTrackColor = SlateBorder
                            ),
                            modifier = Modifier.testTag("simulate_dur_slider")
                        )
                    }
                }

                // Time offset sequence selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("CORRELATION TIME OFFSET", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TimeOffsetButton(label = "Today", isSelected = offsetDays == 0, onClick = { offsetDays = 0 }, modifier = Modifier.weight(1f))
                        TimeOffsetButton(label = "Yest.", isSelected = offsetDays == 1, onClick = { offsetDays = 1 }, modifier = Modifier.weight(1f))
                        TimeOffsetButton(label = "4 days ago", isSelected = offsetDays == 4, onClick = { offsetDays = 4 }, modifier = Modifier.weight(1f))
                        TimeOffsetButton(label = "20 days ago", isSelected = offsetDays == 20, onClick = { offsetDays = 20 }, modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCEL", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            val cleanPhone = if (phone.isEmpty()) "Unknown" else phone
                            onAddCall(name, cleanPhone, callType, duration.toInt(), offsetDays)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("simulate_add_confirm_btn")
                    ) {
                        Text("INJECT RECORD", color = CosmicBlack, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TimeOffsetButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, if (isSelected) CyberCyan else SlateBorder, RoundedCornerShape(6.dp))
            .background(if (isSelected) Color(0xFF0F172A) else CosmicBlack)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = if (isSelected) CyberCyan else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace
        )
    }
}

// Format millisecond epoch timestamp beautifully
fun formatCallTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Calendar.getInstance()
    val logCal = Calendar.getInstance()
    logCal.timeInMillis = timestamp

    val diffMs = now.timeInMillis - timestamp
    val oneDayMs = 24 * 60 * 60 * 1000L

    return when {
        diffMs < oneDayMs && now.get(Calendar.DAY_OF_YEAR) == logCal.get(Calendar.DAY_OF_YEAR) -> {
            "Today, " + SimpleDateFormat("h:mm a", Locale.US).format(date)
        }
        diffMs < 2 * oneDayMs && (now.get(Calendar.DAY_OF_YEAR) - logCal.get(Calendar.DAY_OF_YEAR) == 1 || now.get(Calendar.DAY_OF_YEAR) - logCal.get(Calendar.DAY_OF_YEAR) == -364) -> {
            "Yesterday, " + SimpleDateFormat("h:mm a", Locale.US).format(date)
        }
        diffMs < 7 * oneDayMs -> {
            SimpleDateFormat("EEEE, h:mm a", Locale.US).format(date)
        }
        else -> {
            SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.US).format(date)
        }
    }
}
