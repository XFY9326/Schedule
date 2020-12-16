package tool.xfy9326.schedule.db.query

import androidx.room.Embedded
import androidx.room.Relation
import tool.xfy9326.schedule.beans.Course
import tool.xfy9326.schedule.beans.CourseTime
import tool.xfy9326.schedule.db.DBConst

data class CourseBundle(
    @Embedded
    val course: Course,
    @Relation(parentColumn = DBConst.COLUMN_COURSE_ID, entityColumn = DBConst.COLUMN_COURSE_ID)
    val courseTime: List<CourseTime>,
)