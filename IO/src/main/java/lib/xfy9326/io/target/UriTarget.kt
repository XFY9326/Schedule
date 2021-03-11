@file:Suppress("NOTHING_TO_INLINE")

package lib.xfy9326.io.target

import android.net.Uri
import lib.xfy9326.io.IOManager
import lib.xfy9326.io.target.base.InputTarget
import lib.xfy9326.io.target.base.OutputTarget
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

enum class UriWriteMode(internal val mode: String) {
    WRITE("w"),
    WRITE_APPEND("wa"),
    READ_WRITE("rw"),
    READ_WRITE_STICKY("rwt");
}

fun Uri.asTarget(writeMode: UriWriteMode = UriWriteMode.WRITE) = UriTarget(this, writeMode)

class UriTarget internal constructor(private val uri: Uri, private val writeMode: UriWriteMode) : InputTarget<InputStream>, OutputTarget<OutputStream> {
    override fun openInputStream(): InputStream =
        IOManager.contentResolver.openInputStream(uri) ?: throw IOException("Input stream open failed! Uri: $uri")

    override fun openOutputStream(): OutputStream =
        IOManager.contentResolver.openOutputStream(uri, writeMode.mode) ?: throw IOException("Output stream open failed! Uri: $uri")
}