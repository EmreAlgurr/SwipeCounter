package com.emre.swipecounter.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AccessibilityDisclosureDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Erişilebilirlik İzni Gerekli")
        },
        text = {
            Text(
                text = "Bu uygulama, Instagram ve TikTok gibi uygulamalarda kaydırma (swipe) hareketlerini saymak için Erişilebilirlik Servisi'ni kullanır.\n\n" +
                        "Hiçbir kişisel veri toplanmaz veya saklanmaz. Bu izin sadece kaydırma hareketlerini algılamak ve sayacı güncellemek için kullanılır.\n\n" +
                        "Devam etmek için lütfen ayarlardan Swipe Counter servisini etkinleştirin."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Ayarlara Git")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        },
        modifier = modifier
    )
}
