package tool.xfy9326.schedule.io

import android.os.Environment
import tool.xfy9326.schedule.io.kt.asParentOf
import tool.xfy9326.schedule.io.utils.DirUtils

object PathManager {
    private const val DIR_SHARE = "SharedFiles"
    private const val DIR_LOG = "Log"
    private const val DIR_JS_CONFIGS = "JSConfigs"
    private const val DIR_PICTURE_APP = "App"
    const val DIR_SCHEDULE = "Schedule"

    val LogDir
        get() = DirUtils.externalFilesDir(DIR_LOG)

    val PictureAppDir
        get() = DirUtils.externalFilesDir(Environment.DIRECTORY_PICTURES, DIR_PICTURE_APP)

    val SharedFileDir
        get() = DirUtils.externalCacheDir.asParentOf(DIR_SHARE)

    val JSConfigs
        get() = DirUtils.externalFilesDir(DIR_JS_CONFIGS)
}