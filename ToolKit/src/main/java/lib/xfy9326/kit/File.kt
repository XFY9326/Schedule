@file:Suppress("unused")

package lib.xfy9326.kit

import java.io.File

fun File.asParentOf(vararg path: String) = if (path.isEmpty()) this else File(this, path.joinToString(File.separator))

fun Collection<File>.deleteRecursively() = forEach { it.deleteRecursively() }

suspend fun <T> File.withPreparedDir(block: suspend (File) -> T): T? {
    val parent = parentFile
    if (parent == null || parent.exists() || parent.mkdirs()) {
        return block(this)
    }
    return null
}

fun File.takeIfExists() = takeIf { it.exists() }