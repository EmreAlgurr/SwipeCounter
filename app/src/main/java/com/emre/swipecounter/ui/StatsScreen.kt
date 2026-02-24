package com.emre.swipecounter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf

// App package constants
private const val TIKTOK_PACKAGE = "com.zhiliaoapp.musically"
private const val TIKTOK_PACKAGE_ALT = "com.ss.android.ugc.trill"
private const val INSTAGRAM_PACKAGE = "com.instagram.android"

// App colors
private val TikTokColor = Color(0xFF00E5FF) // Cyan (matches our accent)
private val InstagramColor = Color(0xFFE040FB) // Purple/Pink

@Composable
fun StatsScreen(viewModel: StatsViewModel = viewModel()) {
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val totalWeeklySwipes by viewModel.totalWeeklySwipes.collectAsState()
    val appBreakdown by viewModel.appBreakdown.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()

    // Accent color from theme
    val accentColor = MaterialTheme.colorScheme.primary

    // Prepare chart data
    val chartEntryModelProducer = remember(weeklyStats) {
        if (weeklyStats.isEmpty()) {
            ChartEntryModelProducer(listOf(listOf(entryOf(0f, 0f))))
        } else {
            ChartEntryModelProducer(
                listOf(
                    weeklyStats.mapIndexed { index, stat ->
                        entryOf(index.toFloat(), stat.count.toFloat())
                    }
                )
            )
        }
    }

    // Day labels for X-axis
    val dayLabels = remember(weeklyStats) { weeklyStats.map { it.dayName } }
    val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        dayLabels.getOrElse(value.toInt()) { "" }
    }

    // Label colors
    val labelColor = Color.LightGray
    val guidelineColor = Color(0xFF333333)

    Scaffold(
        containerColor = Color.Black
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Haftalık İstatistikler",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Light
                )
            }

            // Total Weekly Swipes Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "Bu Hafta Toplam",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$totalWeeklySwipes",
                            style = MaterialTheme.typography.displayLarge,
                            color = accentColor,
                            fontWeight = FontWeight.Light
                        )
                        Text(
                            text = "kaydırma",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Chart Section
            item {
                Column {
                    Text(
                        text = "Günlük Dağılım",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp)
                    ) {
                        if (weeklyStats.isEmpty()) {
                            // Empty state
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Henüz veri yok",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            Chart(
                                chart = columnChart(
                                    columns = listOf(
                                        lineComponent(
                                            color = accentColor,
                                            thickness = 24.dp,
                                            shape = com.patrykandpatrick.vico.core.component.shape.Shapes.roundedCornerShape(
                                                topLeftPercent = 25,
                                                topRightPercent = 25
                                            )
                                        )
                                    )
                                ),
                                chartModelProducer = chartEntryModelProducer,
                                startAxis = rememberStartAxis(
                                    label = textComponent(
                                        color = labelColor,
                                        textSize = 12.sp
                                    ),
                                    axis = null,
                                    tick = null,
                                    guideline = lineComponent(
                                        color = guidelineColor,
                                        thickness = 1.dp
                                    )
                                ),
                                bottomAxis = rememberBottomAxis(
                                    label = textComponent(
                                        color = labelColor,
                                        textSize = 12.sp
                                    ),
                                    axis = null,
                                    tick = null,
                                    guideline = null,
                                    valueFormatter = bottomAxisValueFormatter
                                ),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // App Breakdown Section
            item {
                Column {
                    Text(
                        text = "Uygulama Dağılımı",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp)
                    ) {
                        if (appBreakdown.isEmpty()) {
                            // Empty state
                            Text(
                                text = "Henüz veri yok",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        } else {
                            val totalSwipes = appBreakdown.sumOf { it.totalCount }.coerceAtLeast(1)
                            
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                appBreakdown.forEach { breakdown ->
                                    val appName = when {
                                        breakdown.packageName == TIKTOK_PACKAGE || 
                                        breakdown.packageName == TIKTOK_PACKAGE_ALT -> "TikTok"
                                        breakdown.packageName == INSTAGRAM_PACKAGE -> "Instagram"
                                        else -> breakdown.packageName.substringAfterLast(".")
                                    }
                                    
                                    val appColor = when {
                                        breakdown.packageName == TIKTOK_PACKAGE || 
                                        breakdown.packageName == TIKTOK_PACKAGE_ALT -> TikTokColor
                                        breakdown.packageName == INSTAGRAM_PACKAGE -> InstagramColor
                                        else -> Color.Gray
                                    }
                                    
                                    val progress = breakdown.totalCount.toFloat() / totalSwipes
                                    val percentage = (progress * 100).toInt()
                                    
                                    AppBreakdownRow(
                                        appName = appName,
                                        count = breakdown.totalCount,
                                        percentage = percentage,
                                        progress = progress,
                                        color = appColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Daily Breakdown List (Only for Premium)
            item {
                Column {
                    Text(
                        text = "Günlük Detaylar",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (isPremium) {
                        if (weeklyStats.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Henüz veri yok",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                weeklyStats.forEachIndexed { index, stat ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stat.dayName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${stat.count}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = accentColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (index < weeklyStats.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp)
                                                .height(1.dp)
                                                .background(Color(0xFF333333))
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Locked State for Free Users
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Premium Özellik",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun AppBreakdownRow(
    appName: String,
    count: Int,
    percentage: Int,
    progress: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Color indicator dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = appName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.bodyLarge,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "($percentage%)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Color(0xFF333333)
        )
    }
}
