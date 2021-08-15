@file:Suppress("unused")

package lib.xfy9326.android.kit.io.kt

import android.content.res.AssetManager
import android.net.Uri
import android.util.TypedValue
import lib.xfy9326.android.kit.io.IOManager
import lib.xfy9326.android.kit.io.file.AssetFile
import lib.xfy9326.android.kit.io.file.RawResFile
import okio.*
import kotlin.io.use

fun AssetFile.source(accessMode: Int = AssetManager.ACCESS_STREAMING) = open(accessMode).source()

fun RawResFile.source() = open().source()

fun RawResFile.source(typedValue: TypedValue) = open(typedValue).source()

fun Uri.source() = IOManager.contentResolver.openInputStream(this)?.source()

fun Uri.sink() = IOManager.contentResolver.openOutputStream(this)?.sink()

inline fun <T> Source.useBuffer(crossinline block: BufferedSource.() -> T) = buffer().use(block)

inline fun <T> Sink.useBuffer(flush: Boolean = true, crossinline block: BufferedSink.() -> T) = buffer().use {
    val result = it.block()
    if (flush && it.isOpen) it.flush()
    result
}