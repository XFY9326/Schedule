package tool.xfy9326.schedule.tools

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

class JobQueue(context: CoroutineContext) {
    private val isRunning = AtomicBoolean(false)
    private val runningScope = CoroutineScope(context + SupervisorJob() + Dispatchers.Default)
    private val jobQueue = Channel<suspend () -> Unit>()

    fun submit(block: suspend () -> Unit) {
        jobQueue.offer(block)
    }

    fun allowRunning() {
        if (isRunning.compareAndSet(false, true)) {
            runningScope.launch {
                while (true) jobQueue.receive().invoke()
            }
        }
    }

    fun cancelAll() {
        if (isRunning.compareAndSet(true, false)) {
            runningScope.cancel()
        }
    }
}