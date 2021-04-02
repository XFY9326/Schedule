@file:Suppress("unused")

package tool.xfy9326.schedule.io.kt

import java.io.File

fun File.asParentOf(path: String) = File(this, path)

fun File.deleteAll() = when {
    isFile -> delete()
    isDirectory -> deleteRecursively()
    else -> true
}

fun List<File>.deleteAll() = forEach { it.deleteAll() }

fun File.createParentFolder(): Boolean {
    val parent = parentFile
    return if (parent?.exists() == false) parent.mkdirs() else true
}