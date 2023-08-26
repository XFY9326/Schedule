package tool.xfy9326.schedule.beans

import android.content.Context
import android.os.Parcelable
import androidx.annotation.IntRange
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import tool.xfy9326.schedule.beans.SectionTime.Companion.intersect
import tool.xfy9326.schedule.beans.WeekDay.Companion.orderedValue
import tool.xfy9326.schedule.content.utils.hasCourse
import tool.xfy9326.schedule.db.DBConst
import tool.xfy9326.schedule.tools.NumberPattern
import tool.xfy9326.schedule.utils.schedule.WeekNumPattern.getWeeksDescriptionText
import kotlin.math.max
import kotlin.math.min

@Entity(
    tableName = DBConst.TABLE_COURSE_TIME,
    foreignKeys = [ForeignKey(
        entity = Course::class,
        parentColumns = [DBConst.COLUMN_COURSE_ID],
        childColumns = [DBConst.COLUMN_COURSE_ID],
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(
        value = [
            DBConst.COLUMN_COURSE_ID,
            DBConst.COLUMN_WEEK_DAY,
            DBConst.COLUMN_SECTION_START,
            DBConst.COLUMN_SECTION_DURATION,
            DBConst.COLUMN_WEEK_NUM,
            DBConst.COLUMN_LOCATION
        ],
        unique = true
    )]
)
@Parcelize
data class CourseTime(
    @ColumnInfo(name = DBConst.COLUMN_TIME_ID)
    @PrimaryKey(autoGenerate = true)
    var timeId: Long,
    @ColumnInfo(name = DBConst.COLUMN_COURSE_ID, index = true)
    var courseId: Long,
    @ColumnInfo(name = DBConst.COLUMN_WEEK_NUM)
    var weekNumArray: BooleanArray,
    @Embedded
    var sectionTime: SectionTime,
    @ColumnInfo(name = DBConst.COLUMN_LOCATION)
    var location: String? = null,
) : Parcelable {

    companion object {
        infix fun CourseTime.intersect(courseTime: CourseTime): Boolean {
            for (i in 0 until min(weekNumArray.size, courseTime.weekNumArray.size)) {
                if (weekNumArray[i] && courseTime.weekNumArray[i] && sectionTime intersect courseTime.sectionTime) return true
            }
            return false
        }

        fun CourseTime.getWeeksDescriptionText(context: Context) = NumberPattern(weekNumArray).getWeeksDescriptionText(context)

        fun CourseTime.hasThisWeekCourse(
            weekNum: Int,
            maxWeekNum: Int,
            startWeekDay: WeekDay,
            endWeekDay: WeekDay,
            weekStart: WeekDay,
        ): Boolean =
            if (this.weekNumArray.hasCourse(weekNum)) {
                when (weekNum) {
                    1 -> startWeekDay.orderedValue(weekStart) <= sectionTime.weekDay.orderedValue(weekStart)
                    maxWeekNum -> endWeekDay.orderedValue(weekStart) >= sectionTime.weekDay.orderedValue(weekStart)
                    else -> true
                }
            } else {
                false
            }

        // Previous first
        @IntRange(from = 0)
        fun CourseTime.getFromWeekNum(@IntRange(from = 1) weekNum: Int): Int? {
            // Input error
            if (weekNum <= 0) return null
            if (weekNumArray.isEmpty()) return 0
            val currentIndex = weekNum - 1
            // This week
            if (currentIndex < weekNumArray.size && weekNumArray[currentIndex]) return weekNum
            // Previous week
            if (currentIndex > 0) {
                val fromIndex = if (currentIndex > weekNumArray.lastIndex) {
                    weekNumArray.lastIndex
                } else {
                    currentIndex - 1
                }
                var preIndex = -1
                for (i in fromIndex downTo 0) {
                    if (this.weekNumArray[i]) {
                        preIndex = i
                        break
                    }
                }
                if (preIndex >= 0) {
                    return preIndex + 1
                }
            }
            // Post week
            if (currentIndex < weekNumArray.lastIndex) {
                var postIndex = -1
                for (i in (currentIndex + 1) until weekNumArray.size) {
                    if (this.weekNumArray[i]) {
                        postIndex = i
                        break
                    }
                }
                if (postIndex >= 0) {
                    return postIndex + 1
                }
            }
            // Not found
            return 0
        }
    }

    @Ignore
    constructor(weekNum: BooleanArray, weekDay: WeekDay, start: Int, duration: Int, location: String? = null) :
            this(weekNum, SectionTime(weekDay, start, duration), location)

    @Ignore
    constructor(weekNum: BooleanArray, sectionTime: SectionTime, location: String? = null) :
            this(DBConst.DEFAULT_ID, DBConst.DEFAULT_ID, weekNum, sectionTime, location)

    operator fun compareTo(courseTime: CourseTime): Int {
        if (this === courseTime) return 0
        // Sort by start week order
        for (i in 0 until max(weekNumArray.size, courseTime.weekNumArray.size)) {
            val thisWeekNum = if (i < weekNumArray.size) weekNumArray[i] else false
            val otherWeekNum = if (i < courseTime.weekNumArray.size) courseTime.weekNumArray[i] else false
            if (thisWeekNum && !otherWeekNum) {
                return -1
            } else if (!thisWeekNum && otherWeekNum) {
                return 1
            }
        }
        return sectionTime.compareTo(courseTime.sectionTime)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseTime

        if (timeId != other.timeId) return false
        if (courseId != other.courseId) return false
        if (!weekNumArray.contentEquals(other.weekNumArray)) return false
        if (sectionTime != other.sectionTime) return false
        if (location != other.location) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timeId.hashCode()
        result = 31 * result + courseId.hashCode()
        result = 31 * result + weekNumArray.contentHashCode()
        result = 31 * result + sectionTime.hashCode()
        result = 31 * result + (location?.hashCode() ?: 0)
        return result
    }
}