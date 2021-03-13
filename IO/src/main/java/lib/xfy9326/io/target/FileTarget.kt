@file:Suppress("BlockingMethodInNonBlockingContext")

package lib.xfy9326.io.target

import lib.xfy9326.io.target.base.InputTarget
import lib.xfy9326.io.target.base.OutputTarget
import lib.xfy9326.io.utils.createParentFolder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

fun File.asTarget(appendOutput: Boolean = false) = FileTarget(this, appendOutput)

class FileTarget internal constructor(private val file: File, private val appendOutput: Boolean) :
    InputTarget<FileInputStream>, OutputTarget<FileOutputStream> {

    override suspend fun openInputStream(): FileInputStream = FileInputStream(file)

    override suspend fun openOutputStream(): FileOutputStream =
        if (file.createParentFolder()) {
            FileOutputStream(file, appendOutput)
        } else {
            error("File folder create failed! File: $file")
        }
}