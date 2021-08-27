package tool.xfy9326.schedule.io

import android.os.Environment
import lib.xfy9326.android.kit.AppDir
import lib.xfy9326.kit.asParentOf

object PathManager {
    private const val DIR_SHARE = "SharedFiles"
    private const val DIR_LOG = "Log"
    private const val DIR_JS_CONFIGS = "JSConfigs"
    private const val DIR_PICTURE_APP = "App"
    const val DIR_SCHEDULE = "Schedule"

    val LogDir
        get() = AppDir.externalFilesDir(DIR_LOG)

    val PictureAppDir
        get() = AppDir.externalFilesDir(Environment.DIRECTORY_PICTURES, DIR_PICTURE_APP)

    val SharedFileDir
        get() = AppDir.externalCacheDir.asParentOf(DIR_SHARE)

    val JSConfigs
        get() = AppDir.externalFilesDir(DIR_JS_CONFIGS)
}