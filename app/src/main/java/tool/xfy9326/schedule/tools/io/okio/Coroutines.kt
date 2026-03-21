@file:Suppress("unused")

package tool.xfy9326.schedule.tools.io.okio

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.tools.io.file.AssetFile
import tool.xfy9326.schedule.tools.io.file.RawResFile
import java.io.File
import java.io.IOException

suspend fun AssetFile.readBitmapAsync(outPadding: Rect? = null, opts: BitmapFactory.Options? = null): Result<Bitmap> =
    withContext(Dispatchers.IO) {
        runCatching {
            readBitmap(outPadding, opts) ?: throw IOException("Bitmap decode failed! Uri: ${this@readBitmapAsync}")
        }
    }

suspend fun RawResFile.readBitmapAsync(outPadding: Rect? = null, opts: BitmapFactory.Options? = null): Result<Bitmap> =
    withContext(Dispatchers.IO) {
        runCatching {
            readBitmap(outPadding, opts) ?: throw IOException("Bitmap decode failed! Uri: ${this@readBitmapAsync}")
        }
    }

suspend fun AssetFile.readTextAsync(): Result<String> =
    withContext(Dispatchers.IO) {
        runCatching {
            readText()
        }
    }

suspend fun RawResFile.readTextAsync(): Result<String> =
    withContext(Dispatchers.IO) {
        runCatching {
            readText()
        }
    }

suspend fun Uri.writeTextAsync(text: String, append: Boolean = false): Result<Unit> =
    withContext(Dispatchers.IO) {
        runCatching {
            writeText(text, append)
        }
    }

suspend fun Uri.readTextAsync(): Result<String> =
    withContext(Dispatchers.IO) {
        runCatching {
            readText()
        }
    }

suspend fun File.writeTextAsync(text: String, append: Boolean = false): Result<Unit> =
    withContext(Dispatchers.IO) {
        runCatching {
            writeText(text, append)
        }
    }

suspend fun File.readTextAsync(): Result<String> =
    withContext(Dispatchers.IO) {
        runCatching {
            readText()
        }
    }

suspend fun Uri.writeBitmapAsync(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int = 100, flush: Boolean = true): Result<Unit> =
    withContext(Dispatchers.IO) {
        runCatching {
            writeBitmap(bitmap, format, quality, flush)
        }
    }

suspend fun Uri.readBitmapAsync(outPadding: Rect? = null, opts: BitmapFactory.Options? = null): Result<Bitmap> =
    withContext(Dispatchers.IO) {
        runCatching {
            readBitmap(outPadding, opts) ?: throw IOException("Bitmap decode failed! Uri: ${this@readBitmapAsync}")
        }
    }

suspend fun File.writeBitmapAsync(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int = 100, flush: Boolean = true): Result<Unit> =
    withContext(Dispatchers.IO) {
        runCatching {
            writeBitmap(bitmap, format, quality, flush)
        }
    }

suspend fun File.readBitmapAsync(outPadding: Rect? = null, opts: BitmapFactory.Options? = null): Result<Bitmap> =
    withContext(Dispatchers.IO) {
        runCatching {
            readBitmap(outPadding, opts) ?: throw IOException("Bitmap decode failed! File: ${this@readBitmapAsync}")
        }
    }