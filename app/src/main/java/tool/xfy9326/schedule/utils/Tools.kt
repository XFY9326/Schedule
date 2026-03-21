package tool.xfy9326.schedule.utils

import android.content.res.TypedArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import kotlinx.coroutines.flow.Flow

fun <T> Flow<T>.asDistinctLiveData(): LiveData<T> = asLiveData().distinctUntilChanged()

fun <R> TypedArray.compatUse(block: (TypedArray) -> R): R {
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        if (exception != null) {
            try {
                recycle()
            } catch (closeException: Throwable) {
                exception.addSuppressed(closeException)
            }
        }
    }
}