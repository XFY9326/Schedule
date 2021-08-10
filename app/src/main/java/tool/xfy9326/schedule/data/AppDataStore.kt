package tool.xfy9326.schedule.data

import tool.xfy9326.schedule.data.base.AbstractDataStore
import tool.xfy9326.schedule.db.provider.ScheduleDBProvider

object AppDataStore : AbstractDataStore("App") {
    private val currentScheduleId by longPreferencesKey()
    private val acceptEULA by booleanPreferencesKey()
    private val shownFeedbackAttention by booleanPreferencesKey()
    private val shownCalendarSyncAttention by booleanPreferencesKey()
    private val agreeCourseImportPolicy by booleanPreferencesKey()
    private val ignoreUpdateVersionCode by intPreferencesKey()
    private val readOnlineImportAttention by booleanPreferencesKey()
    private val apkUpdateDownloadId by longPreferencesKey()
    private val ignorePackageInstallPermission by booleanPreferencesKey()
    private val showAppWidgetAttention by booleanPreferencesKey()

    val currentScheduleIdFlow = currentScheduleId.readAndInitAsFlow {
        ScheduleDBProvider.db.scheduleDAO.tryInitDefaultSchedule()
    }

    suspend fun hasCurrentScheduleId() = currentScheduleId.hasValue()

    val acceptEULAFlow = acceptEULA.readAsFlow(false)

    val ignoreUpdateVersionCodeFlow = ignoreUpdateVersionCode.readAsFlow(0)

    val readOnlineImportAttentionFlow = readOnlineImportAttention.readAsFlow(false)

    val agreeCourseImportPolicyFlow = agreeCourseImportPolicy.readAsFlow(false)

    val apkUpdateDownloadIdFlow = apkUpdateDownloadId.readAsFlow()

    val ignorePackageInstallPermissionFlow = ignorePackageInstallPermission.readAsFlow(false)

    val showAppWidgetAttentionFlow = showAppWidgetAttention.readAsFlow(true)

    suspend fun setShowAppWidgetAttention(data: Boolean) = showAppWidgetAttention.saveData(data)

    suspend fun setIgnorePackageInstallPermission(data: Boolean) = ignorePackageInstallPermission.saveData(data)

    suspend fun setApkUpdateDownloadId(data: Long) = apkUpdateDownloadId.saveData(data)

    suspend fun removeApkUpdateDownloadId() = apkUpdateDownloadId.remove()

    suspend fun setReadOnlineImportAttention(data: Boolean) = readOnlineImportAttention.saveData(data)

    suspend fun hasShownFeedbackAttention() = shownFeedbackAttention.readAsShownOnce()

    suspend fun hasShownCalendarSyncAttention() = shownCalendarSyncAttention.readAsShownOnce()

    suspend fun setAgreeCourseImportPolicy() = agreeCourseImportPolicy.saveData(true)

    suspend fun setAcceptEULA(data: Boolean) = acceptEULA.saveData(data)

    suspend fun setIgnoreUpdateVersionCode(data: Int) = ignoreUpdateVersionCode.saveData(data)

    suspend fun setCurrentScheduleId(data: Long) = currentScheduleId.saveData(data)
}