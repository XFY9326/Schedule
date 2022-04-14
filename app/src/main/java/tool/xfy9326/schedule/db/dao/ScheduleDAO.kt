package tool.xfy9326.schedule.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import lib.xfy9326.android.kit.ApplicationScope
import lib.xfy9326.kit.CHAR_ONE
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.beans.Schedule
import tool.xfy9326.schedule.beans.WeekDay
import tool.xfy9326.schedule.beans.WeekDay.Companion.value
import tool.xfy9326.schedule.content.utils.arrangeWeekNum
import tool.xfy9326.schedule.db.DBConst
import tool.xfy9326.schedule.db.query.CourseBundle
import tool.xfy9326.schedule.utils.schedule.ScheduleUtils

@Dao
abstract class ScheduleDAO {
    companion object {
        private fun Flow<List<CourseBundle>>.convertCourseList() = map { list ->
            list.map {
                it.course.times = it.courseTime
                it.course
            }
        }

        private fun Flow<CourseBundle?>.convertCourse() = map {
            if (it != null) {
                it.course.times = it.courseTime
                it.course
            } else {
                null
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

    fun getScheduleCourses(scheduleId: Long) = getCourses(scheduleId).convertCourseList().shareIn(ApplicationScope, SharingStarted.Lazily, 1)

    fun getScheduleCoursesWithoutId(scheduleId: Long, courseId: Long) = getCoursesWithoutId(scheduleId, courseId).convertCourseList()

    fun getScheduleCourse(courseId: Long) = getCourse(courseId).convertCourse()

    @Transaction
    open suspend fun getNextScheduleCourseTimeByDate(scheduleId: Long, weekNum: Int, weekDay: WeekDay, currentClassNum: Int): Pair<Course, CourseTime>? {
        if (weekNum < 1) return null

        val weekNumLike = buildString(weekNum + 2) {
            repeat(weekNum - 1) {
                append(DBConst.LIKE_SINGLE)
            }
            append(CHAR_ONE + DBConst.LIKE_MORE)
        }

        val courseTime = getNextCourseTimeByDate(scheduleId, weekNumLike, weekDay.value, currentClassNum) ?: return null
        val course = getSingleCourse(courseTime.courseId) ?: return null
        return course to courseTime
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
            time.weekNumArray = time.weekNumArray.arrangeWeekNum()
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
            time.weekNumArray = time.weekNumArray.arrangeWeekNum()
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

    @Query("select ${DBConst.TABLE_COURSE_TIME}.* from ${DBConst.TABLE_COURSE}, ${DBConst.TABLE_COURSE_TIME} where ${DBConst.COLUMN_SCHEDULE_ID}=:scheduleId and ${DBConst.TABLE_COURSE_TIME}.${DBConst.COLUMN_COURSE_ID}=${DBConst.TABLE_COURSE}.${DBConst.COLUMN_COURSE_ID} and ${DBConst.COLUMN_WEEK_DAY}=:weekDayInt and (${DBConst.COLUMN_SECTION_START} + ${DBConst.COLUMN_SECTION_DURATION} - 1) >= :minEndClassNum and ${DBConst.COLUMN_WEEK_NUM} like :weekNumLike order by ${DBConst.COLUMN_SECTION_START} asc limit 1")
    protected abstract suspend fun getNextCourseTimeByDate(scheduleId: Long, weekNumLike: String, weekDayInt: Int, minEndClassNum: Int): CourseTime?

    @Transaction
    @Query("select * from ${DBConst.TABLE_COURSE} where ${DBConst.COLUMN_SCHEDULE_ID}=:scheduleId and ${DBConst.COLUMN_COURSE_ID}!=:courseId")
    protected abstract fun getCoursesWithoutId(scheduleId: Long, courseId: Long): Flow<List<CourseBundle>>

    @Transaction
    @Query("select * from ${DBConst.TABLE_COURSE} where ${DBConst.COLUMN_COURSE_ID}=:courseId")
    protected abstract fun getCourse(courseId: Long): Flow<CourseBundle?>

    @Query("select * from ${DBConst.TABLE_COURSE} where ${DBConst.COLUMN_COURSE_ID}=:courseId limit 1")
    protected abstract suspend fun getSingleCourse(courseId: Long): Course?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun putSchedule(schedule: Schedule): Long

    @Query("select * from ${DBConst.TABLE_SCHEDULE}")
    abstract fun getSchedules(): Flow<List<Schedule>>

    @Query("select * from ${DBConst.TABLE_SCHEDULE} where ${DBConst.COLUMN_SCHEDULE_ID}=:scheduleId")
    abstract fun getSchedule(scheduleId: Long): Flow<Schedule?>

    @Query("select ${DBConst.COLUMN_SCHEDULE_ID}, ${DBConst.COLUMN_SCHEDULE_NAME} from ${DBConst.TABLE_SCHEDULE}")
    abstract fun getScheduleMin(): Flow<List<Schedule.Min>>

    @Query("select ${DBConst.COLUMN_SCHEDULE_TIMES} from ${DBConst.TABLE_SCHEDULE} where ${DBConst.COLUMN_SCHEDULE_ID}=:scheduleId")
    abstract fun getScheduleTimes(scheduleId: Long): Flow<Schedule.Times?>

    @Query("select count(*) from ${DBConst.TABLE_SCHEDULE}")
    abstract suspend fun getScheduleCount(): Long
}