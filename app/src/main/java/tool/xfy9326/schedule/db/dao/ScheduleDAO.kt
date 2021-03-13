package tool.xfy9326.schedule.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.content.utils.arrangeWeekNum
import tool.xfy9326.schedule.db.DBConst
import tool.xfy9326.schedule.db.query.CourseBundle
import tool.xfy9326.schedule.utils.schedule.ScheduleUtils

@Dao
abstract class ScheduleDAO {
    companion object {
        fun Flow<List<CourseBundle>>.convert() = map { list ->
            list.map {
                it.course.times = it.courseTime
                it.course
            }
        }
    }

    @Transaction
    open suspend fun updateScheduleCourses(schedule: Schedule, courses: List<Course>): Long {
        updateSchedule(schedule)
        deleteCourseByScheduleId(schedule.scheduleId)
        for (course in courses) {
            putCourse(schedule.scheduleId, course)
        }
        return schedule.scheduleId
    }

    @Transaction
    open suspend fun putNewScheduleCourses(schedule: Schedule, courses: List<Course>): Long {
        val scheduleId = putSchedule(schedule)
        for (course in courses) {
            putCourse(scheduleId, course)
        }
        return scheduleId
    }

    fun getScheduleCourses(scheduleId: Long) = getCourses(scheduleId).convert()

    fun getScheduleCoursesWithoutId(scheduleId: Long, courseId: Long) = getCoursesWithoutId(scheduleId, courseId).convert()

    fun getScheduleCourse(courseId: Long) = getCourse(courseId).map {
        if (it != null) {
            it.course.times = it.courseTime
            it.course
        } else {
            null
        }
    }

    @Transaction
    open suspend fun tryInitDefaultSchedule() =
        if (getScheduleCount() == 0L) {
            putSchedule(ScheduleUtils.createDefaultSchedule())
        } else {
            null
        }

    @Transaction
    open suspend fun putCourse(scheduleId: Long, course: Course): Long {
        course.scheduleId = scheduleId
        val courseId = putCourseRaw(course)
        for (time in course.times) {
            time.courseId = courseId
            time.weekNum = time.weekNum.arrangeWeekNum()
            putCourseTime(time)
        }
        return courseId
    }

    @Transaction
    open suspend fun updateCourse(course: Course): Long {
        deleteCourseTimeByCourseId(course.courseId)
        updateCourseRaw(course)
        for (time in course.times) {
            time.courseId = course.courseId
            time.weekNum = time.weekNum.arrangeWeekNum()
            putCourseTime(time)
        }
        return course.courseId
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun putCourseRaw(course: Course): Long

    @Update
    protected abstract suspend fun updateCourseRaw(course: Course)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun putCourseTime(courseTime: CourseTime): Long

    @Query("delete from ${DBConst.TABLE_COURSE_TIME} where ${DBConst.COLUMN_COURSE_ID}=:courseId")
    protected abstract suspend fun deleteCourseTimeByCourseId(courseId: Long)

    @Query("delete from ${DBConst.TABLE_COURSE} where ${DBConst.COLUMN_SCHEDULE_ID}=:scheduleId")
    protected abstract suspend fun deleteCourseByScheduleId(scheduleId: Long)

    @Update
    abstract suspend fun updateSchedule(schedule: Schedule)

    @Delete
    abstract suspend fun deleteSchedule(schedule: Schedule)

    @Delete
    abstract suspend fun deleteCourse(course: Course)

    @Transaction
    @Query("select * from ${DBConst.TABLE_COURSE} where ${DBConst.COLUMN_SCHEDULE_ID}=:scheduleId")
    protected abstract fun getCourses(scheduleId: Long): Flow<List<CourseBundle>>

    @Transaction
    @Query("select * from ${DBConst.TABLE_COURSE} where ${DBConst.COLUMN_SCHEDULE_ID}=:scheduleId and ${DBConst.COLUMN_COURSE_ID}!=:courseId")
    protected abstract fun getCoursesWithoutId(scheduleId: Long, courseId: Long): Flow<List<CourseBundle>>

    @Transaction
    @Query("select * from ${DBConst.TABLE_COURSE} where ${DBConst.COLUMN_COURSE_ID}=:courseId")
    protected abstract fun getCourse(courseId: Long): Flow<CourseBundle?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun putSchedule(schedule: Schedule): Long

    @Query("select * from ${DBConst.TABLE_SCHEDULE}")
    abstract fun getSchedules(): Flow<List<Schedule>>

    @Query("select * from ${DBConst.TABLE_SCHEDULE} where ${DBConst.COLUMN_SCHEDULE_ID}=:scheduleId")
    abstract fun getSchedule(scheduleId: Long): Flow<Schedule?>

    @Query("select ${DBConst.COLUMN_SCHEDULE_ID}, ${DBConst.COLUMN_SCHEDULE_NAME} from ${DBConst.TABLE_SCHEDULE}")
    abstract fun getScheduleMin(): Flow<List<Schedule.Min>>


    @Query("select count(*) from ${DBConst.TABLE_SCHEDULE}")
    abstract suspend fun getScheduleCount(): Long
}