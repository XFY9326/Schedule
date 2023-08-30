package tool.xfy9326.schedule.utils

import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.core.view.drawToBitmap
import io.github.xfy9326.atools.ui.setLightSystemBar
import tool.xfy9326.schedule.beans.SystemBarAppearance
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

fun Window.setSystemBarAppearance(systemBarAppearance: SystemBarAppearance) {
    when (systemBarAppearance) {
        SystemBarAppearance.FOLLOW_THEME -> setLightSystemBar(!decorView.context.isUsingNightMode())
        SystemBarAppearance.USE_LIGHT -> setLightSystemBar(true)
        SystemBarAppearance.USE_DARK -> setLightSystemBar(false)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun Window.pixelCopy(config: Bitmap.Config) = suspendCoroutine {
    val bitmap = Bitmap.createBitmap(decorView.width, decorView.height, config)
    PixelCopy.request(this, bitmap, { status ->
        if (status == PixelCopy.SUCCESS) {
            it.resume(bitmap)
        } else {
            it.resumeWithException(IllegalStateException("Pixel copy failed!"))
        }
    }, Handler(Looper.getMainLooper()))
}

suspend fun Window.drawToBitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        try {
            return pixelCopy(config)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return decorView.drawToBitmap()
}