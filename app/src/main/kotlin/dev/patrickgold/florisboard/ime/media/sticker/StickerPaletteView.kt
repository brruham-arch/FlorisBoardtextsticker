/*
 * Copyright (C) 2022-2025 The FlorisBoard Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.patrickgold.florisboard.ime.media.sticker

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.patrickgold.florisboard.editorInstance

private const val STICKER_SIZE_PX = 512

/**
 * Renders [text] onto a square bitmap: white background, centered black text.
 * Intentionally simple; visual styling can be extended later.
 */
private fun renderStickerBitmap(text: String): Bitmap {
    val bitmap = Bitmap.createBitmap(STICKER_SIZE_PX, STICKER_SIZE_PX, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
        textSize = 64f
    }

    // Wrap text into lines that fit the bitmap width with some padding.
    val maxWidth = STICKER_SIZE_PX - 64f
    val words = text.trim().split(Regex("\\s+"))
    val lines = mutableListOf<String>()
    var current = StringBuilder()
    for (word in words) {
        val candidate = if (current.isEmpty()) word else "$current $word"
        if (paint.measureText(candidate) > maxWidth && current.isNotEmpty()) {
            lines.add(current.toString())
            current = StringBuilder(word)
        } else {
            current = StringBuilder(candidate)
        }
    }
    if (current.isNotEmpty()) lines.add(current.toString())

    val lineHeight = paint.textSize * 1.2f
    val totalHeight = lineHeight * lines.size
    var y = (STICKER_SIZE_PX - totalHeight) / 2f + paint.textSize
    for (line in lines) {
        canvas.drawText(line, STICKER_SIZE_PX / 2f, y, paint)
        y += lineHeight
    }

    return bitmap
}

@Composable
fun StickerPaletteView(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val editorInstance by context.editorInstance()

    var inputText by remember { mutableStateOf("") }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Teks sticker") },
            singleLine = false,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        previewBitmap = renderStickerBitmap(inputText)
                    }
                },
                modifier = Modifier.fillMaxWidth(0.5f),
            ) {
                Text("Generate")
            }
            Button(
                onClick = {
                    val bitmap = previewBitmap
                    if (bitmap != null) {
                        editorInstance.commitStickerItem(bitmap)
                        previewBitmap = null
                        inputText = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = previewBitmap != null,
            ) {
                Text("Kirim")
            }
        }

        previewBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Sticker preview",
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .aspectRatio(1f),
            )
        }
    }
}
