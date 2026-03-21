package tool.xfy9326.schedule.tools.coroutines

import kotlinx.coroutines.sync.Mutex

@Throws(IllegalStateException::class)
inline fun Mutex.withTryLock(owner: Any? = null, action: () -> Unit): Boolean {
    return if (tryLock(owner)) {
        try {
            action()
        } finally {
            unlock(owner)
        }
        true
    } else {
        false
    }
}

@Throws(IllegalStateException::class)
inline fun <T> Mutex.withTryLock(owner: Any? = null, action: () -> T, onHasLocked: () -> T): T {
    return if (tryLock(owner)) {
        try {
            action()
        } finally {
            unlock(owner)
        }
    } else {
        onHasLocked()
    }
}