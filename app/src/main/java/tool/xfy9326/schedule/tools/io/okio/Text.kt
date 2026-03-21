package tool.xfy9326.schedule.tools.io.okio

import android.net.Uri
import okio.sink
import okio.source
import tool.xfy9326.schedule.tools.io.file.AssetFile
import tool.xfy9326.schedule.tools.io.file.RawResFile
import java.io.File
import java.io.IOException

@Throws(IOException::class)
fun AssetFile.readText(): String =
    source().useBuffer {
        readUtf8()
    }

@Throws(IOException::class)
fun RawResFile.readText(): String =
    source().useBuffer {
        readUtf8()
    }

@Throws(IOException::class)
fun Uri.writeText(text: String, append: Boolean = false): Unit =
    sink(if (append) "wa" else "w").useBuffer {
        writeUtf8(text)
    }

@Throws(IOException::class)
fun Uri.readText(): String =
    source().useBuffer {
        readUtf8()
    }

@Throws(IOException::class)
fun File.writeText(text: String, append: Boolean = false): Unit =
    sink(append).useBuffer {
        writeUtf8(text)
    }

@Throws(IOException::class)
fun File.readText(): String =
    source().useBuffer {
        readUtf8()
    }