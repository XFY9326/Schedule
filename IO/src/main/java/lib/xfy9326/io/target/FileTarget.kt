@file:Suppress("NOTHING_TO_INLINE")

package lib.xfy9326.io.target

import lib.xfy9326.io.target.base.InputTarget
import lib.xfy9326.io.target.base.OutputTarget
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

fun File.asTarget(appendOutput: Boolean = false) = FileTarget(this, appendOutput)

class FileTarget internal constructor(private val file: File, private val appendOutput: Boolean) :
    InputTarget<FileInputStream>, OutputTarget<FileOutputStream> {

    override fun openInputStream(): FileInputStream = FileInputStream(file)

    override fun openOutputStream(): FileOutputStream = FileOutputStream(file, appendOutput)
}