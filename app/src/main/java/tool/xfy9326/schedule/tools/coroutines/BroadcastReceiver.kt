package tool.xfy9326.schedule.tools.coroutines

import android.content.BroadcastReceiver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
fun BroadcastReceiver.goAsync(
    coroutineScope: CoroutineScope = GlobalScope,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    block: suspend () -> Unit,
) {
    val result = goAsync()
    coroutineScope.launch(dispatcher) {
        try {
            block()
        } finally {
            result.finish()
        }
    }
}