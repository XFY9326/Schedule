@file:Suppress("unused")

package lib.xfy9326.android.kit.io.kt

import androidx.annotation.RawRes
import lib.xfy9326.android.kit.io.file.AssetFile
import lib.xfy9326.android.kit.io.file.RawResFile

fun assetFile(path: String) = AssetFile(path)

fun rawResFile(@RawRes resId: Int) = RawResFile(resId)