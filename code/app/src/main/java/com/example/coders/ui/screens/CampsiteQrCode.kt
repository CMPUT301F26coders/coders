package com.example.coders.ui.screens


import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coders.Campsite
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Returns the deep-link URI encoded in a campsite's QR code.
 * Falls back to building the link from [Campsite.id] if [Campsite.qr] is blank.
 */
fun campsiteQrLink(campsiteId: String, existingQr: String = ""): String =
    existingQr.ifBlank { "coders://campsite/$campsiteId" }

/**
 * Generates a square QR code [Bitmap] for the given [content] string.
 */

//Following function was assisted by Google, Gemini, "(Given the context of data.kt ) How to generate a QR bitmap in kotlin"
fun generateQrBitmap(content: String, sizePx: Int = 512): Bitmap? {
    if (content.isBlank()) return null
    return try {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val pixels = IntArray(sizePx * sizePx)
        for (y in 0 until sizePx) {
            val offset = y * sizePx
            for (x in 0 until sizePx) {
                pixels[offset + x] = if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
            }
        }
        Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, sizePx, 0, 0, sizePx, sizePx)
        }
    } catch (_: Exception) {
        null
    }
}

/**
 * Displays a scannable QR code for [campsite]. Tapping it opens a larger dialog
 * so it is easy to scan with another phone's camera.
 */
@Composable
fun CampsiteQrCodeImage(
    campsite: Campsite,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    expandOnClick: Boolean = true
) {
    val qrContent = remember(campsite.id, campsite.qr) {
        campsiteQrLink(campsite.id, campsite.qr)
    }
    val bitmap = remember(qrContent) {
        generateQrBitmap(qrContent, sizePx = 512)
    }
    var showEnlarged by remember { mutableStateOf(false) }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "QR code for ${campsite.name}",
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .then(
                    if (expandOnClick) Modifier.clickable { showEnlarged = true } else Modifier
                )
        )
    }

    if (showEnlarged && bitmap != null) {
        AlertDialog(
            onDismissRequest = { showEnlarged = false },
            title = {
                Text(
                    text = campsite.name.ifBlank { "Campsite QR Code" },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Enlarged QR code for ${campsite.name}",
                        modifier = Modifier
                            .size(240.dp)
                            .background(Color.White)
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scan to view campsite details",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showEnlarged = false }) {
                    Text("Close")
                }
            }
        )
    }
}