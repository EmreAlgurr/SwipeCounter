package com.emre.swipecounter.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Colors
private val ColorBlack = Color(0xFF000000)
private val ColorWhite = Color(0xFFFFFFFF)
private val ColorNeonError = Color(0xFFFF3B30) // Neon Red
private val ColorNeonSuccess = Color(0xFF34C759) // Neon Green
private val ColorDarkGray = Color(0xFF1C1C1E)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    // Store survey answers if needed later
    val answers = remember { mutableStateListOf<Int>() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ColorBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                },
                label = "onboarding_flow"
            ) { currentStep ->
                when (currentStep) {
                    0 -> SurveyStep(
                        question = "Günde kaç saat Reels izliyorsun?",
                        options = listOf("< 1 saat", "1-3 saat", "3+ saat"),
                        onOptionSelected = {
                            answers.add(it)
                            step++
                        }
                    )
                    1 -> SurveyStep(
                        question = "Bu seni nasıl etkiliyor?",
                        options = listOf("Odak Sorunu", "Uykusuzluk", "Stres"),
                        onOptionSelected = {
                            answers.add(it)
                            step++
                        }
                    )
                    2 -> SurveyStep(
                        question = "Bırakmayı denedin mi?",
                        options = listOf("Evet", "Hayır"),
                        onOptionSelected = {
                            answers.add(it)
                            step++
                        }
                    )
                    3 -> SurveyStep(
                        question = "Hedefin ne?",
                        options = listOf("Tamamen Bırakmak", "Azaltmak"),
                        onOptionSelected = {
                            answers.add(it)
                            step++
                        }
                    )
                    4 -> CalculationStep(
                        onFinished = { step++ }
                    )
                    5 -> RiskChartStep(
                        onNext = { step++ }
                    )
                    6 -> ProjectionChartStep(
                        onNext = { step++ }
                    )
                    7 -> PermissionStep(
                        onFinished = onFinished
                    )
                }
            }
        }
    }
}

@Composable
fun SurveyStep(
    question: String,
    options: List<String>,
    onOptionSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = question,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = ColorWhite,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        options.forEachIndexed { index, option ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ColorDarkGray)
                    .clickable { onOptionSelected(index) }
                    .padding(vertical = 20.dp, horizontal = 24.dp)
            ) {
                Text(
                    text = option,
                    fontSize = 18.sp,
                    color = ColorWhite,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CalculationStep(onFinished: () -> Unit) {
    var message by remember { mutableStateOf("Veriler işleniyor...") }
    val infiniteTransition = rememberInfiniteTransition(label = "loading_icon")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable<Float>(
            animation = tween<Float>(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    LaunchedEffect(Unit) {
        delay(1000)
        message = "Alışkanlıklar analiz ediliyor..."
        delay(1000)
        message = "Risk skoru hesaplanıyor..."
        delay(1000)
        onFinished()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Refresh, // Using Refresh icon as a generic spinner placeholder
            contentDescription = "Loading",
            tint = ColorWhite,
            modifier = Modifier
                .size(64.dp)
                .rotate(rotation)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            fontSize = 18.sp,
            color = ColorWhite.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RiskChartStep(onNext: () -> Unit) {
    var animationPlayed by remember { mutableStateOf(false) }
    
    // Animate bar heights (0f to 1f)
    val userBarHeight by animateFloatAsState(
        targetValue = if (animationPlayed) 0.85f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 200),
        label = "userBar"
    )
    val otherBarHeight by animateFloatAsState(
        targetValue = if (animationPlayed) 0.15f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 400),
        label = "otherBar"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Risk Seviyeniz Çok Yüksek!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = ColorNeonError,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(40.dp))

        // Chart Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // User Bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${(userBarHeight * 100).toInt()}%",
                    color = ColorWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(300.dp * userBarHeight) // Dynamic height
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(ColorNeonError)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Sen", color = ColorWhite, fontWeight = FontWeight.SemiBold)
            }

            // Others Bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${(otherBarHeight * 100).toInt()}%",
                    color = ColorWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(300.dp * otherBarHeight)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(ColorNeonSuccess)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Diğerleri", color = ColorWhite, fontWeight = FontWeight.SemiBold)
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorWhite),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "Çözümü Gör",
                color = ColorBlack,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProjectionChartStep(onNext: () -> Unit) {
    val progress = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Geleceğini Seç",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = ColorWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Line Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(ColorDarkGray, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val startX = 0f
                val startY = height / 2

                // Red Line (Chaos/Upwards)
                val redPath = Path().apply {
                    moveTo(startX, startY)
                    // Bezier curve upwards
                    cubicTo(
                        width * 0.3f, startY - height * 0.1f,
                        width * 0.6f, startY - height * 0.6f,
                        width, 0f
                    )
                }

                // Green Line (Control/Downwards)
                val greenPath = Path().apply {
                    moveTo(startX, startY)
                    // Bezier curve downwards
                    cubicTo(
                        width * 0.3f, startY + height * 0.1f,
                        width * 0.6f, startY + height * 0.6f,
                        width, height
                    )
                }

                // Draw limited by progress
                drawPath(
                    path = redPath, // Note: For complex path animation we normally use PathMeasure, 
                                   // but for simplicity we'll just clip or reveal.
                                   // Since PathMeasure is complex in Compose Canvas without native support,
                                   // we will simulate the "drawing" by masking used width.
                    color = ColorNeonError,
                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                )

                 drawPath(
                    path = greenPath,
                    color = ColorNeonSuccess,
                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                )
                
                 // Masking rectangle to reveal from left to right
                 drawRect(
                     color = ColorDarkGray,
                     topLeft = Offset(width * progress.value, 0f),
                     size = Size(width * (1 - progress.value), height)
                 )
            }
            
            // Labels (Conditional visibility based on progress could be added, but static is fine for layout)
            if (progress.value > 0.8f) {
                Text(
                    text = "Müdahale Edilmezse",
                    color = ColorNeonError,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
                Text(
                    text = "Swipe Counter ile",
                    color = ColorNeonSuccess,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
             Text(
                text = "Bugün",
                color = ColorWhite,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Mevcut gidişatınız bağımlılığın artacağını gösteriyor. Ama bunu tersine çevirebiliriz.",
            fontSize = 16.sp,
            color = ColorWhite.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorWhite),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "Gidişatı Değiştir",
                color = ColorBlack,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}



@Composable
fun PermissionStep(onFinished: () -> Unit) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
         Text(
            text = "Son Bir Adım: İzinler",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = ColorWhite,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Sayacın çalışabilmesi için uygulamanın diğer uygulamalar üzerinde çalışmasına izin verin.",
            fontSize = 18.sp,
            color = ColorWhite.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )
        
        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                onFinished()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorWhite),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "İzni Ver ve Başla",
                color = ColorBlack,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
