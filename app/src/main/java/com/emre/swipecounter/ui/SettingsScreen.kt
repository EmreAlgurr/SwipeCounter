package com.emre.swipecounter.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// App constants
private const val APP_PACKAGE_NAME = "com.emre.swipecounter"
private const val PRIVACY_POLICY_URL = "https://docs.google.com/document/d/e/2PACX-1vSOO0WmJzaYCYUegLYfiDTLm_R_6-n3z2UvzKfH204lUStiGaQFvOM0v4jB4oOsDtt0kvq9pbNAP3Ss/pub" 
private const val APP_VERSION = "1.0.0"

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onOpenPaywall: () -> Unit
) {
    val context = LocalContext.current
    val dailyLimit by viewModel.dailyLimit.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val totalSwipeCount by viewModel.totalSwipeCount.collectAsStateWithLifecycle()
    val showOverlay by viewModel.showOverlay.collectAsStateWithLifecycle()
    var showResetConfirmation by remember { mutableStateOf(false) }
    
    // Confirmation Dialog for Reset
    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("Verileri Sıfırla") },
            text = { Text("Tüm swipe verileriniz silinecek. Bu işlem geri alınamaz. Emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetData()
                        showResetConfirmation = false
                    }
                ) {
                    Text("Sil", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) {
                    Text("İptal")
                }
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Header
        Text(
            text = "Ayarlar",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // ==================== PREMIUM SECTION ====================
        SectionHeader(text = "Premium")
        Spacer(modifier = Modifier.height(12.dp))
        
        if (isPremium) {
            // Premium User Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Premium Aktif",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tüm özellikler açık",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "✨",
                        fontSize = 24.sp
                    )
                }
            }
        } else {
            // Upgrade Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .clickable { onOpenPaywall() }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Premium'a Geç",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Limitleri kaldır & Reklamları kapat",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "→",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 24.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(28.dp))
        
        // ==================== PREFERENCES SECTION ====================
        SectionHeader(text = "Tercihler")
        Spacer(modifier = Modifier.height(12.dp))
        
        // Daily Limit Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Günlük Limit",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isPremium) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$dailyLimit Swipe",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (!isPremium) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Premium özellik",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                var sliderValue by remember(dailyLimit) { mutableFloatStateOf(dailyLimit.toFloat()) }
                
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    enabled = isPremium,
                    onValueChangeFinished = {
                        val rounded = (sliderValue / 5).toInt() * 5
                        viewModel.updateDailyLimit(rounded.coerceAtLeast(5))
                    },
                    valueRange = 5f..2000f,
                    steps = 398,
                    colors = SliderDefaults.colors(
                        thumbColor = if (isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        activeTrackColor = if (isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha=0.2f)
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Floating Counter Toggle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Floating Counter",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Show counter over apps",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = showOverlay,
                    onCheckedChange = { viewModel.updateShowOverlay(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha=0.2f)
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(28.dp))
        
        // ==================== SUPPORT SECTION ====================
        SectionHeader(text = "Destek")
        Spacer(modifier = Modifier.height(12.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column {
                // Share with Friends
                SettingsRow(
                    icon = Icons.Default.Share,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Arkadaşlarınla Paylaş",
                    subtitle = "Uygulamayı öner",
                    onClick = {
                        val shareText = "Swipe Counter ile bugün $totalSwipeCount kez kaydırdım! 📱\n\nSen de dijital alışkanlıklarını takip et:\nhttps://play.google.com/store/apps/details?id=$APP_PACKAGE_NAME"
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Paylaş"))
                    }
                )
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.1f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                // Rate Us
                SettingsRow(
                    icon = Icons.Default.Star,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Bizi Değerlendir",
                    subtitle = "Play Store'da 5 yıldız verin",
                    onClick = {
                        try {
                            // Try to open Play Store app
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$APP_PACKAGE_NAME"))
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            // Fall back to browser
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$APP_PACKAGE_NAME"))
                            context.startActivity(intent)
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(28.dp))
        
        // ==================== LEGAL SECTION ====================
        SectionHeader(text = "Yasal")
        Spacer(modifier = Modifier.height(12.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column {
                // Privacy Policy
                SettingsRow(
                    icon = Icons.Outlined.Info,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Gizlilik Politikası",
                    subtitle = "Verilerinizi nasıl kullanıyoruz",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL))
                        context.startActivity(intent)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(28.dp))
        
        // ==================== DANGER ZONE ====================
        SectionHeader(text = "Tehlikeli Bölge")
        Spacer(modifier = Modifier.height(12.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
        ) {
            SettingsRow(
                icon = Icons.Default.Delete,
                iconTint = MaterialTheme.colorScheme.error,
                title = "Tüm Verileri Sıfırla",
                subtitle = "Swipe sayılarını temizle",
                onClick = { showResetConfirmation = true }
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // ==================== APP VERSION ====================
        Text(
            text = "Swipe Counter v$APP_VERSION",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
