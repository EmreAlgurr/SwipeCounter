package com.emre.swipecounter.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emre.swipecounter.RevenueCatManager

@Composable
fun PaywallScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    val neonGold = Color(0xFFFFD700)
    
    // Dynamic price from RevenueCat, fallback to static price
    var priceText by remember { mutableStateOf("...") }
    
    LaunchedEffect(Unit) {
        RevenueCatManager.fetchOfferings(
            onSuccess = { pkg ->
                // Get formatted price from RevenueCat package
                priceText = "Aylık ${pkg.product.price.formatted}"
            },
            onError = {
                // Fallback to static price if offerings can't be fetched
                priceText = "Aylık 35.99 TL"
            }
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        // Close Button
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = Color.Gray,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(32.dp)
                .clickable { onClose() }
        )
        
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Premium",
                tint = neonGold,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Premium'a Geçin",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "• Detaylı Raporlar\n• Sınırsız Günlük Limit\n• Reklamsız Deneyim",
                color = Color.Gray,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = {
                    if (activity != null) {
                        RevenueCatManager.purchasePremium(
                            activity = activity,
                            onSuccess = {
                                Toast.makeText(context, "Developer Mode: Premium Activated!", Toast.LENGTH_SHORT).show()
                                onClose()
                            },
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = neonGold),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = priceText,
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Satın Alımları Geri Yükle",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    RevenueCatManager.restorePurchases(
                        onSuccess = { Toast.makeText(context, "Geri Yüklendi", Toast.LENGTH_SHORT).show(); onClose() },
                        onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                    )
                }
            )
        }
    }
}
