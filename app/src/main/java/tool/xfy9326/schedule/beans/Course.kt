package tool.xfy9326.schedule.beans

import androidx.annotation.ColorInt
import androidx.room.*
import tool.xfy9326.schedule.db.DBConst
import tool.xfy9326.schedule.tools.MaterialColorHelper
import java.io.Serializable

@Entity(
    tableName = DBConst.TABLE_COURSE,
    foreignKeys = [ForeignKey(
        entity = Schedule::class,
        parentColumns = [DBConst.COLUMN_SCHEDULE_ID],
        childColumns = [DBConst.COLUMN_SCHEDULE_ID],
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )]
)
data class Course(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DBConst.COLUMN_COURSE_ID)
    var courseId: Long,
    @ColumnInfo(name = DBConst.COLUMN_SCHEDULE_ID, index = true)
    var scheduleId: Long,
    var name: String,
    var teacher: String?,
    @ColorInt
    var color: Int,
    @Ignore
    var times: List<CourseTime>,
) : Serializable {

    constructor(courseId: Long, scheduleId: Long, name: String, teacher: String?, color: Int) :
            this(courseId, scheduleId, name, teacher, color, emptyList())

    constructor(name: String, teacher: String?, times: List<CourseTime>) :
            this(DBConst.DEFAULT_ID, DBConst.DEFAULT_ID, name, teacher, MaterialColorHelper.random(), times)

    fun clone(scheduleId: Long): Course {
        val timesList = ArrayList<CourseTime>(times.size)
        times.forEach {
            timesList.add(it.copy(timeId = DBConst.DEFAULT_ID, courseId = DBConst.DEFAULT_ID, classTime = it.classTime.copy()))
        }
        return copy(scheduleId = scheduleId, courseId = DBConst.DEFAULT_ID, times = timesList)
    }
}