package tool.xfy9326.schedule.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import tool.xfy9326.schedule.beans.ScheduleSync
import tool.xfy9326.schedule.data.AppSettingsDataStore
import tool.xfy9326.schedule.db.DBConst
import tool.xfy9326.schedule.db.query.ScheduleSyncsInfo

@Dao
abstract class ScheduleSyncDAO {
    @Transaction
    open suspend fun getScheduleSync(scheduleId: Long): Flow<ScheduleSync> {
        val data = getScheduleSyncByScheduleId(scheduleId)
        return if (data == null) {
            val newData = AppSettingsDataStore.getDefaultScheduleSyncFlow(scheduleId).first()
            newData.syncId = putScheduleSync(newData)
            flow {
                emit(newData)
            }
        } else {
            flow {
                emit(data)
            }
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun putScheduleSync(scheduleSync: ScheduleSync): Long

    @Update
    abstract suspend fun updateScheduleSync(scheduleSync: ScheduleSync)

    @Query("delete from ${DBConst.TABLE_SCHEDULE_SYNC}")
    abstract suspend fun clearAll()

    @Transaction
    @Query("select ${DBConst.COLUMN_SCHEDULE_ID}, ${DBConst.COLUMN_SCHEDULE_NAME} from ${DBConst.TABLE_SCHEDULE}")
    abstract fun getScheduleSyncsInfo(): Flow<List<ScheduleSyncsInfo>>

    @Query("select * from ${DBConst.TABLE_SCHEDULE_SYNC} where ${DBConst.COLUMN_SCHEDULE_ID}=:scheduleId")
    protected abstract fun getScheduleSyncByScheduleId(scheduleId: Long): ScheduleSync?
}