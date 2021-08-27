@file:Suppress("unused")

package lib.xfy9326.android.kit.io.file

import android.content.res.AssetFileDescriptor
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.RawRes
import lib.xfy9326.android.kit.io.IOManager

class RawResFile constructor(@RawRes val resId: Int) {
    private val resources by lazy { IOManager.resources }

    @Throws(Resources.NotFoundException::class)
    fun open() = resources.openRawResource(resId)

    @Throws(Resources.NotFoundException::class)
    fun open(value: TypedValue) = resources.openRawResource(resId, value)

    @Throws(Resources.NotFoundException::class)
    fun openFd(): AssetFileDescriptor? = resources.openRawResourceFd(resId)
}