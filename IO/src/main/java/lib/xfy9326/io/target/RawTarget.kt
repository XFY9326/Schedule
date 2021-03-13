package lib.xfy9326.io.target

import androidx.annotation.RawRes
import lib.xfy9326.io.IOManager
import lib.xfy9326.io.target.base.InputTarget
import java.io.InputStream

fun createRawTarget(@RawRes rawId: Int) = RawTarget(rawId)

class RawTarget internal constructor(@RawRes private val rawId: Int) : InputTarget<InputStream> {
    override suspend fun openInputStream(): InputStream = IOManager.resources.openRawResource(rawId)
}