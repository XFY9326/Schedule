package tool.xfy9326.schedule.utils.file

import android.os.Environment
import lib.xfy9326.io.utils.asParentOf
import lib.xfy9326.io.utils.externalCacheDir
import lib.xfy9326.io.utils.externalFilesDir

object PathManager {
    private const val DIR_SHARE = "SharedFiles"
    private const val DIR_LOG = "Log"
    private const val DIR_PICTURE_APP = "App"
    const val DIR_SCHEDULE = "Schedule"

    val LogDir
        get() = externalFilesDir(DIR_LOG)

    val PictureAppDir
        get() = externalFilesDir(Environment.DIRECTORY_PICTURES, DIR_PICTURE_APP)

    val SharedFileDir
        get() = externalCacheDir().asParentOf(DIR_SHARE)
}