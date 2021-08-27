@file:Suppress("unused")

package lib.xfy9326.android.kit

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.EmptyCoroutineContext

private var localAppInstance: Application? = null

val ApplicationInstance: Application by lazy {
    localAppInstance ?: error("Application instance hasn't been initialized yet! You should call Application.initializeToolKit() first!")
}

val ApplicationScope: CoroutineScope by lazy {
    CoroutineScope(EmptyCoroutineContext + SupervisorJob())
}

fun Application.initializeToolKit() {
    localAppInstance = this
}