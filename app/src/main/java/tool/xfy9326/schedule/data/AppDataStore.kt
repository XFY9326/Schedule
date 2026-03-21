package tool.xfy9326.schedule.data

import tool.xfy9326.schedule.data.base.AbstractDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider
import tool.xfy9326.schedule.tools.datastore.preference.key.booleanKey
import tool.xfy9326.schedule.tools.datastore.preference.key.intKey
import tool.xfy9326.schedule.tools.datastore.preference.key.longKey

object AppDataStore : AbstractDataStore("App") {
    private val currentScheduleId by longKey()
    private val acceptEULAVersion by intKey()
    private val shownCalendarSyncAttention by booleanKey()
    private val agreeCourseImportPolicy by booleanKey()
    private val ignoreUpdateVersionCode by intKey()
    private val readOnlineImportAttention by booleanKey()
    private val apkUpdateDownloadId by longKey()
    private val ignorePackageInstallPermission by booleanKey()

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

    suspend fun setAgreeCourseImportPolicy(agree: Boolean) = agreeCourseImportPolicy.saveData(agree)

    suspend fun setAcceptEULAVersion(data: Int) = acceptEULAVersion.saveData(data)

    suspend fun setIgnoreUpdateVersionCode(data: Int) = ignoreUpdateVersionCode.saveData(data)

    suspend fun setCurrentScheduleId(data: Long) = currentScheduleId.saveData(data)
}