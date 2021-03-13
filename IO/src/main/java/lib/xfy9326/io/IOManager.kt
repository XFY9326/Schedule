package lib.xfy9326.io

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import lib.xfy9326.io.utils.cacheDir
import lib.xfy9326.io.utils.codeCacheDir
import lib.xfy9326.io.utils.deleteAll
import lib.xfy9326.io.utils.externalCacheDirs


object IOManager {
    private var application: Context? = null
    internal val context: Context
        get() = application.let {
            require(it != null) { "IOManager hasn't initialized!" }
            it
        }

    val contentResolver: ContentResolver by lazy {
        context.contentResolver
    }
    val assetManager: AssetManager by lazy {
        context.assets
    }
    val resources: Resources by lazy {
        context.resources
    }

    fun init(context: Context) {
        application = context.applicationContext
    }

    suspend fun clearAllCache() {
        cacheDir().deleteAll()
        codeCacheDir().deleteAll()
        externalCacheDirs().forEach {
            it.deleteAll()
        }
    }
}