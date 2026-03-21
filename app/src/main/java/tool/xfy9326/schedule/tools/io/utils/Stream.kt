@file:Suppress("unused")

package tool.xfy9326.schedule.tools.io.utils

import java.io.Closeable

fun Closeable.closeQuietly() {
    try {
        close()
    } catch (e: Exception) {
        // Ignore
    }
}