@file:Suppress("NOTHING_TO_INLINE")

package lib.xfy9326.io.target

import android.content.res.AssetManager
import lib.xfy9326.io.IOManager
import lib.xfy9326.io.target.base.InputTarget
import java.io.InputStream

fun createAssetTarget(path: String, accessMode: Int = AssetManager.ACCESS_STREAMING) = AssetTarget(path, accessMode)

class AssetTarget internal constructor(private val path: String, private val accessMode: Int) : InputTarget<InputStream> {
    override fun openInputStream(): InputStream = IOManager.assetManager.open(path, accessMode)
}