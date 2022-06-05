package tool.xfy9326.schedule.io.utils

import android.net.Uri
import androidx.core.content.FileProvider
import io.github.xfy9326.atools.core.AppContext
import tool.xfy9326.schedule.BuildConfig
import java.io.File

private const val FILE_PROVIDER_AUTH = "${BuildConfig.APPLICATION_ID}.file.provider"

fun File.getUriByFileProvider(): Uri? =
    runCatching {
        FileProvider.getUriForFile(AppContext, FILE_PROVIDER_AUTH, this)
    }.getOrNull()