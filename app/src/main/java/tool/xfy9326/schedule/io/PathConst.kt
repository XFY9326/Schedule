package tool.xfy9326.schedule.io

import androidx.core.content.ContextCompat
import tool.xfy9326.schedule.App
import java.io.File

object PathConst {
    private const val DIR_EXTERNAL = "External"
    private const val DIR_LOG = "Log"

    const val ASSETS_LICENSE_FILE = "LICENSE.txt"
    const val ASSETS_EULA_FILE = "EULA.txt"

    fun getInternalStorageDir(vararg subDir: String): File =
        if (subDir.isEmpty()) {
            App.instance.filesDir
        } else {
            File(App.instance.filesDir, subDir.joinToString(File.separator))
        }

    private fun getExternalStorageDir(vararg subDir: String): File {
        val joinedSubDir = subDir.joinToString(File.separator)
        val dirList = ContextCompat.getExternalFilesDirs(App.instance, joinedSubDir).filterNotNull()
        return if (dirList.isEmpty()) {
            getInternalStorageDir(DIR_EXTERNAL + File.separator + joinedSubDir)
        } else {
            dirList[0]
        }
    }

    val LogPath = getExternalStorageDir(DIR_LOG)
}