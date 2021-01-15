@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package tool.xfy9326.schedule.data.base

import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.createDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import tool.xfy9326.schedule.App
import tool.xfy9326.schedule.kt.tryEnumValueOf
import kotlin.properties.ReadOnlyProperty

abstract class AbstractDataStore(val name: String) {
    val dataStore = App.instance.createDataStore(name)
    protected val readOnlyFlow = dataStore.data.shareIn(GlobalScope, SharingStarted.Eagerly, 1)

    fun getPreferenceDataStore(scope: CoroutineScope) = DataStorePreferenceAdapter(dataStore, scope)

    fun <R> read(transform: suspend (pref: Preferences) -> R) = readOnlyFlow.map(transform).distinctUntilChanged()

    suspend fun edit(transform: suspend (MutablePreferences) -> Unit) = dataStore.edit(transform)

    suspend fun updateData(transform: suspend (Preferences) -> Preferences) = dataStore.updateData(transform)

    suspend fun clear() = edit {
        it.clear()
    }

    protected fun <T> Preferences.Key<T>.readAsFlow() = read {
        it[this]
    }

    protected fun <T> Preferences.Key<T>.readAsFlow(defaultValue: T) = read {
        it[this] ?: defaultValue
    }

    protected suspend fun Preferences.Key<Boolean>.readAsShowOnce() = read {
        val value = it[this] ?: false
        if (!value) {
            edit { editPref ->
                editPref[this] = true
            }
        }
        value
    }.first()

    protected inline fun <reified E : Enum<E>> Preferences.Key<String>.readEnumAsFlow(defaultValue: E) = read {
        tryEnumValueOf<E>(it[this]) ?: defaultValue
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

    protected suspend fun <T> Preferences.Key<T>.saveData(data: T) = dataStore.edit {
        it[this] = data
    }

    protected fun booleanPreferencesKey() =
        ReadOnlyProperty<Any, Preferences.Key<Boolean>> { _, property -> booleanPreferencesKey(property.name) }

    protected fun stringPreferencesKey() =
        ReadOnlyProperty<Any, Preferences.Key<String>> { _, property -> stringPreferencesKey(property.name) }

    protected fun intPreferencesKey() =
        ReadOnlyProperty<Any, Preferences.Key<Int>> { _, property -> intPreferencesKey(property.name) }

    protected fun longPreferencesKey() =
        ReadOnlyProperty<Any, Preferences.Key<Long>> { _, property -> longPreferencesKey(property.name) }

    protected fun floatPreferencesKey() =
        ReadOnlyProperty<Any, Preferences.Key<Float>> { _, property -> floatPreferencesKey(property.name) }

    protected fun doublePreferencesKey() =
        ReadOnlyProperty<Any, Preferences.Key<Double>> { _, property -> doublePreferencesKey(property.name) }

    protected fun stringSetPreferencesKey() =
        ReadOnlyProperty<Any, Preferences.Key<Set<String>>> { _, property -> stringSetPreferencesKey(property.name) }
}