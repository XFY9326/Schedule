@file:Suppress("unused")

package tool.xfy9326.schedule.data.base

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import io.github.xfy9326.atools.base.tryEnumValueOf
import io.github.xfy9326.atools.core.AppContext
import io.github.xfy9326.atools.datastore.preference.adapter.DataStorePreferenceAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*

abstract class AbstractDataStore(val name: String) {
    private val Context.dataStore by preferencesDataStore(name)
    val dataStore by lazy { AppContext.dataStore }

    @OptIn(DelicateCoroutinesApi::class)
    protected val readOnlyFlow = dataStore.data.shareIn(GlobalScope, SharingStarted.Eagerly, 1)

    fun getPreferenceDataStore(scope: CoroutineScope) = DataStorePreferenceAdapter(dataStore, scope)

    fun <R> read(transform: suspend (pref: Preferences) -> R) = readOnlyFlow.map(transform).distinctUntilChanged()

    suspend fun edit(transform: suspend (MutablePreferences) -> Unit) = dataStore.edit(transform)

    suspend fun updateData(transform: suspend (Preferences) -> Preferences) = dataStore.updateData(transform)

    protected fun <T> Preferences.Key<T>.readAsFlow() = read {
        it[this]
    }

    protected fun <T> Preferences.Key<T>.readAsFlow(defaultValue: T) = read {
        it[this] ?: defaultValue
    }

    protected fun <T> Preferences.Key<T>.readAsFlowLazy(lazyValue: () -> T) = read {
        it[this] ?: lazyValue.invoke()
    }

    protected suspend fun Preferences.Key<Boolean>.readAsShownOnce() = read {
        val value = it[this] ?: false
        if (!value) {
            edit { editPref ->
                editPref[this] = true
            }
        }
        value
    }.first()

    protected inline fun <reified E : Enum<E>> Preferences.Key<String>.readEnumAsFlow(defaultValue: E) = read {
        it[this]?.let { value ->
            tryEnumValueOf(value)
        } ?: defaultValue
    }

    protected fun <T> Preferences.Key<T>.readAndInitAsFlow(initBlock: suspend () -> T?) = read {
        if (it.contains(this)) {
            it[this]
        } else {
            initBlock()?.let { default ->
                edit { editPref ->
                    editPref[this] = default
                }
            }
            null
        }
    }.filterNotNull()

    protected suspend fun <T> Preferences.Key<T>.saveData(data: T) {
        edit {
            it[this] = data
        }
    }

    protected suspend fun <T> Preferences.Key<T>.hasValue() = read {
        this in it
    }.first()

    protected suspend fun <T> Preferences.Key<T>.remove() {
        edit {
            it.remove(this)
        }
    }

    suspend fun clear() {
        edit {
            it.clear()
        }
    }
}