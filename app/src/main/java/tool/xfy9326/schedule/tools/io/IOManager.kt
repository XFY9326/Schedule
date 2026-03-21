package tool.xfy9326.schedule.tools.io

import android.content.ContentResolver
import android.content.res.AssetManager
import android.content.res.Resources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.xfy9326.schedule.tools.core.AppContext
import tool.xfy9326.schedule.tools.io.utils.deleteRecursively

object IOManager {
    val contentResolver: ContentResolver by lazy { AppContext.contentResolver }
    val assetManager: AssetManager by lazy { AppContext.assets }
    val resources: Resources by lazy { AppContext.resources }

    suspend fun clearAllCache(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            AppDir.cacheDir.deleteRecursively()
            AppDir.codeCacheDir.deleteRecursively()
            AppDir.externalCacheDirs.deleteRecursively()
        }.isSuccess
    }
}