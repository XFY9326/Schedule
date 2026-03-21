package tool.xfy9326.schedule.tools.io.okio

import android.net.Uri
import okio.Sink
import okio.sink
import tool.xfy9326.schedule.tools.io.IOManager
import java.io.IOException

@Throws(IOException::class)
fun Uri.sink(mode: String? = null): Sink =
    IOManager.contentResolver.let {
        if (mode == null) {
            it.openOutputStream(this)
        } else {
            it.openOutputStream(this, mode)
        }
    }?.sink() ?: throw IOException("$this: Uri sink open failed.")