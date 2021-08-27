@file:Suppress("unused")

package lib.xfy9326.android.kit.io.kt

import android.net.Uri
import lib.xfy9326.kit.runOnlyResultIOJob
import lib.xfy9326.kit.runSafeIOJob

suspend fun Uri.writeText(text: String?) = runOnlyResultIOJob {
    sink()?.useBuffer {
        writeUtf8(text.orEmpty())
        true
    } ?: false
}

suspend fun Uri.readText() = runSafeIOJob {
    source()?.useBuffer {
        readUtf8()
    }
}