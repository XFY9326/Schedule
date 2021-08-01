@file:Suppress("unused")

package tool.xfy9326.schedule.io.kt

import androidx.annotation.RawRes
import tool.xfy9326.schedule.io.file.AssetFile
import tool.xfy9326.schedule.io.file.RawResFile

fun assetFile(path: String) = AssetFile(path)

fun rawResFile(@RawRes resId: Int) = RawResFile(resId)