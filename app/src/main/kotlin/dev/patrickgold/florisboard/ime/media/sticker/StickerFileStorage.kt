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

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Simple on-demand storage for generated text-stickers.
 * Unlike ClipboardFileStorage this has no database/history: a sticker is
 * written right before it is committed and is safe to overwrite each time.
 */
object StickerFileStorage {
    private const val STICKERS_DIR = "stickers"
    private const val AUTHORITY_SUFFIX = ".provider.file"

    private val Context.stickerFilesDir: File
        get() = File(this.cacheDir, STICKERS_DIR).also { it.mkdirs() }

    /**
     * Writes [bitmap] to a fresh file in the sticker cache dir and returns a
     * content:// Uri for it via the app's existing FileProvider.
     */
    fun saveStickerAndGetUri(context: Context, bitmap: Bitmap): Uri {
        val file = File(context.stickerFilesDir, "sticker_${System.nanoTime()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val authority = context.packageName + AUTHORITY_SUFFIX
        return FileProvider.getUriForFile(context, authority, file)
    }

    /**
     * Deletes all cached sticker files. Safe to call periodically (e.g. on
     * keyboard hide) since stickers are regenerated on demand.
     */
    fun clearAll(context: Context) {
        context.stickerFilesDir.listFiles()?.forEach { it.delete() }
    }
}
