package com.emre.swipecounter.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emre.swipecounter.ui.theme.VividAccent
import com.emre.swipecounter.ui.theme.VividError
import com.emre.swipecounter.ui.theme.BorderColor

@Composable
fun CircularLimitIndicator(
    currentCount: Int,
    dailyLimit: Int,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 20.dp
) {
    // 1. Calculate Progress
    val progress = if (dailyLimit > 0) {
        (currentCount.toFloat() / dailyLimit.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    // 2. Determine Color based on progress
    val targetColor = when {
        progress >= 1f -> VividError // Pure Red (Limit Reached)
        progress >= 0.8f -> Color(0xFFFF9900) // Orange (Warning) - Keep for now, looks good on black
        else -> VividAccent // Cyan/Teal (Safe)
    }

    // 3. Animations
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "ProgressAnimation"
    )
    
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 500),
        label = "ColorAnimation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Ensure it's a circle
            .padding(16.dp)
    ) {
        // Canvas for the Circular Progress
        Canvas(modifier = Modifier.fillMaxSize()) {
            val componentSize = size.minDimension
            val stroke = strokeWidth.toPx()
            // val radius = (componentSize - stroke) / 2 // unused
            
            // Draw Background Track
            drawArc(
                color = BorderColor, // Subtle Dark Gray (333333)
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(
                    (size.width - componentSize) / 2 + stroke / 2,
                    (size.height - componentSize) / 2 + stroke / 2
                ),
                size = Size(componentSize - stroke, componentSize - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Draw Progress Arc
            drawArc(
                color = animatedColor,
                startAngle = 270f, // Start from top
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = Offset(
                    (size.width - componentSize) / 2 + stroke / 2,
                    (size.height - componentSize) / 2 + stroke / 2
                ),
                size = Size(componentSize - stroke, componentSize - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        // Center Content: Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$currentCount",
                style = MaterialTheme.typography.displayLarge
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "/ $dailyLimit",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
