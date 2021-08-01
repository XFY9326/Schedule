@file:Suppress("unused")

package lib.xfy9326.kit

import kotlinx.coroutines.sync.Mutex

inline fun <T> Mutex.withTryLock(owner: Any? = null, action: () -> T): T? {
    if (tryLock(owner)) {
        try {
            return action()
        } finally {
            unlock(owner)
        }
    }
    return null
}