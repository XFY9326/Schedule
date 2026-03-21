package tool.xfy9326.schedule.io.utils

import android.net.Uri
import tool.xfy9326.schedule.BuildConfig
import tool.xfy9326.schedule.tools.io.utils.getUriByFileProvider
import java.io.File

private const val FILE_PROVIDER_AUTH = "${BuildConfig.APPLICATION_ID}.file.provider"

fun File.getUriByFileProvider(): Uri? =
    runCatching { getUriByFileProvider(FILE_PROVIDER_AUTH) }.getOrNull()