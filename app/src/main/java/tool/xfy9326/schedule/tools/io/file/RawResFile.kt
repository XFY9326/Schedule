@file:Suppress("unused")

package tool.xfy9326.schedule.tools.io.file

import android.content.res.AssetFileDescriptor
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.RawRes
import tool.xfy9326.schedule.tools.io.IOManager

fun rawResFile(@RawRes resId: Int) = RawResFile(resId)

class RawResFile(@param:RawRes val resId: Int) {
    @Throws(Resources.NotFoundException::class)
    fun open() = IOManager.resources.openRawResource(resId)

    @Throws(Resources.NotFoundException::class)
    fun open(value: TypedValue) = IOManager.resources.openRawResource(resId, value)

    @Throws(Resources.NotFoundException::class)
    fun openFd(): AssetFileDescriptor? = IOManager.resources.openRawResourceFd(resId)
}