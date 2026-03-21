package tool.xfy9326.schedule.tools.io.okio

import android.annotation.SuppressLint
import android.content.res.AssetManager
import android.content.res.Resources
import android.net.Uri
import android.util.TypedValue
import okio.Source
import okio.source
import tool.xfy9326.schedule.tools.io.IOManager
import tool.xfy9326.schedule.tools.io.file.AssetFile
import tool.xfy9326.schedule.tools.io.file.RawResFile
import java.io.IOException

@Throws(IOException::class)
fun AssetFile.source(accessMode: Int = AssetManager.ACCESS_STREAMING): Source =
    open(accessMode).source()

@Throws(Resources.NotFoundException::class)
fun RawResFile.source(): Source =
    open().source()

@Throws(Resources.NotFoundException::class)
fun RawResFile.source(typedValue: TypedValue): Source =
    open(typedValue).source()

@SuppressLint("Recycle")
@Throws(IOException::class)
fun Uri.source(): Source =
    IOManager.contentResolver.openInputStream(this)?.source() ?: throw IOException("$this: Uri source open failed.")