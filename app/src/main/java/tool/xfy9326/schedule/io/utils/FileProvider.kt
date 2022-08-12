package tool.xfy9326.schedule.io.utils

import android.net.Uri
import io.github.xfy9326.atools.io.utils.getUriByFileProvider
import tool.xfy9326.schedule.BuildConfig
import java.io.File

private const val FILE_PROVIDER_AUTH = "${BuildConfig.APPLICATION_ID}.file.provider"

fun File.getUriByFileProvider(): Uri? =
    runCatching { getUriByFileProvider(FILE_PROVIDER_AUTH) }.getOrNull()