@file:Suppress("unused")

package tool.xfy9326.schedule.io.file

import android.content.res.AssetManager
import tool.xfy9326.schedule.io.IOManager
import java.io.IOException

class AssetFile constructor(val path: String) {
    private val assetManager by lazy { IOManager.assetManager }

    @Throws(IOException::class)
    fun open(accessMode: Int = AssetManager.ACCESS_STREAMING) = assetManager.open(path, accessMode)

    @Throws(IOException::class)
    fun openFd() = assetManager.openFd(path)

    @Throws(IOException::class)
    fun openNonAssetFd(cookie: Int = 0) = assetManager.openNonAssetFd(cookie, path)

    @Throws(IOException::class)
    fun openXmlResourceParser(cookie: Int = 0) = assetManager.openXmlResourceParser(cookie, path)
}