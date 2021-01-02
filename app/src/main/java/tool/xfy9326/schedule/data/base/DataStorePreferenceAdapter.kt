package tool.xfy9326.schedule.data.base

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

open class DataStorePreferenceAdapter(private val dataStore: DataStore<Preferences>, scope: CoroutineScope) : PreferenceDataStore() {
    private val prefScope = CoroutineScope(scope.coroutineContext + SupervisorJob() + Dispatchers.IO)

    private val dsData = dataStore.data.shareIn(prefScope, SharingStarted.Eagerly, 1)

    private fun <T> putData(key: Preferences.Key<T>, value: T?) {
        prefScope.launch {
            dataStore.edit {
                if (value != null) it[key] = value else it.remove(key)
            }
        }
    }

    private fun <T> readNullableData(key: Preferences.Key<T>, defValue: T?): T? {
        return runBlocking {
            dsData.map {
                it[key] ?: defValue
            }.firstOrNull()
        }
    }

    private fun <T> readNonNullData(key: Preferences.Key<T>, defValue: T): T {
        return runBlocking {
            dsData.map {
                it[key] ?: defValue
            }.first()
        }
    }

    override fun putString(key: String, value: String?) = putData(preferencesKey(key), value)

    override fun putStringSet(key: String, values: Set<String>?) = putData(preferencesSetKey(key), values)

    override fun putInt(key: String, value: Int) = putData(preferencesKey(key), value)

    override fun putLong(key: String, value: Long) = putData(preferencesKey(key), value)

    override fun putFloat(key: String, value: Float) = putData(preferencesKey(key), value)

    override fun putBoolean(key: String, value: Boolean) = putData(preferencesKey(key), value)


    override fun getString(key: String, defValue: String?): String? = readNullableData(preferencesKey(key), defValue)

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? = readNullableData(preferencesSetKey(key), defValues)

    override fun getInt(key: String, defValue: Int): Int = readNonNullData(preferencesKey(key), defValue)

    override fun getLong(key: String, defValue: Long): Long = readNonNullData(preferencesKey(key), defValue)

    override fun getFloat(key: String, defValue: Float): Float = readNonNullData(preferencesKey(key), defValue)

    override fun getBoolean(key: String, defValue: Boolean): Boolean = readNonNullData(preferencesKey(key), defValue)
}