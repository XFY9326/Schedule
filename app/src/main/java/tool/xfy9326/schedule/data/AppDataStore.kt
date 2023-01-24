package tool.xfy9326.schedule.data

import io.github.xfy9326.atools.datastore.preference.booleanPrefKey
import io.github.xfy9326.atools.datastore.preference.intPrefKey
import io.github.xfy9326.atools.datastore.preference.longPrefKey
import tool.xfy9326.schedule.data.base.AbstractDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider

object AppDataStore : AbstractDataStore("App") {
    private val currentScheduleId by longPrefKey()
    private val acceptEULAVersion by intPrefKey()
    private val shownCalendarSyncAttention by booleanPrefKey()
    private val agreeCourseImportPolicy by booleanPrefKey()
    private val ignoreUpdateVersionCode by intPrefKey()
    private val readOnlineImportAttention by booleanPrefKey()
    private val apkUpdateDownloadId by longPrefKey()
    private val ignorePackageInstallPermission by booleanPrefKey()

    val currentScheduleIdFlow = currentScheduleId.readAndInitAsFlow {
        ScheduleDBProvider.db.scheduleDAO.tryInitDefaultSchedule()
    }

    suspend fun hasCurrentScheduleId() = currentScheduleId.hasValue()

    suspend fun hasAcceptedEULA() = acceptEULAVersion.hasValue()

    val acceptEULAVersionFlow = acceptEULAVersion.readAsFlow(0)

    val ignoreUpdateVersionCodeFlow = ignoreUpdateVersionCode.readAsFlow(0)

    val readOnlineImportAttentionFlow = readOnlineImportAttention.readAsFlow(false)

    val agreeCourseImportPolicyFlow = agreeCourseImportPolicy.readAsFlow(false)

    val apkUpdateDownloadIdFlow = apkUpdateDownloadId.readAsFlow()

    val ignorePackageInstallPermissionFlow = ignorePackageInstallPermission.readAsFlow(false)

    suspend fun setIgnorePackageInstallPermission(data: Boolean) = ignorePackageInstallPermission.saveData(data)

    suspend fun setApkUpdateDownloadId(data: Long) = apkUpdateDownloadId.saveData(data)

    suspend fun removeApkUpdateDownloadId() = apkUpdateDownloadId.remove()

    suspend fun setReadOnlineImportAttention(data: Boolean) = readOnlineImportAttention.saveData(data)

    suspend fun hasShownCalendarSyncAttention() = shownCalendarSyncAttention.readAsShownOnce()

    suspend fun setAgreeCourseImportPolicy() = agreeCourseImportPolicy.saveData(true)

    suspend fun setAcceptEULAVersion(data: Int) = acceptEULAVersion.saveData(data)

    suspend fun setIgnoreUpdateVersionCode(data: Int) = ignoreUpdateVersionCode.saveData(data)

    suspend fun setCurrentScheduleId(data: Long) = currentScheduleId.saveData(data)
}