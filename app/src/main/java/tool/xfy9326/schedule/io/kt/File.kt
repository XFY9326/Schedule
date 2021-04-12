@file:Suppress("unused")

package tool.xfy9326.schedule.io.kt

import java.io.File

fun File.asParentOf(vararg path: String) = if (path.isEmpty()) this else File(this, path.joinToString(File.separator))

fun Collection<File>.deleteRecursively() = forEach { it.deleteRecursively() }

fun <T> File.withPreparedDir(block: (File) -> T): T? {
    val parent = parentFile
    if (parent == null || parent.exists() || parent.mkdirs()) {
        return block(this)
    }
    return null
}

fun File.takeIfExists() = takeIf { it.exists() }