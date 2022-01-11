package tool.xfy9326.schedule.beans

import android.content.Context
import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize
import tool.xfy9326.schedule.beans.SectionTime.Companion.intersect
import tool.xfy9326.schedule.db.DBConst
import tool.xfy9326.schedule.tools.NumberPattern
import tool.xfy9326.schedule.utils.schedule.WeekNumPattern.getWeeksDescriptionText
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
        value = [DBConst.COLUMN_COURSE_ID, DBConst.COLUMN_WEEK_DAY, DBConst.COLUMN_SECTION_START, DBConst.COLUMN_SECTION_DURATION, DBConst.COLUMN_WEEK_NUM],
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
    var weekNum: BooleanArray,
    @Embedded
    var sectionTime: SectionTime,
    var location: String? = null,
) : Parcelable {

    companion object {
        infix fun CourseTime.intersect(courseTime: CourseTime): Boolean {
            for (i in 0 until min(weekNum.size, courseTime.weekNum.size)) {
                if (weekNum[i] && courseTime.weekNum[i] && sectionTime intersect courseTime.sectionTime) return true
            }
            return false
        }

        fun CourseTime.getWeeksDescriptionText(context: Context) = NumberPattern(weekNum).getWeeksDescriptionText(context)

        operator fun CourseTime.compareTo(courseTime: CourseTime): Int {
            for (i in 0 until min(weekNum.size, courseTime.weekNum.size)) {
                if (weekNum[i] && !courseTime.weekNum[i]) {
                    return 1
                } else if (!weekNum[i] && courseTime.weekNum[i]) {
                    return -1
                }
            }
            return sectionTime.compareTo(courseTime.sectionTime)
        }
    }

    @Ignore
    constructor(weekNum: BooleanArray, weekDay: WeekDay, start: Int, duration: Int, location: String? = null) :
            this(weekNum, SectionTime(weekDay, start, duration), location)

    @Ignore
    constructor(weekNum: BooleanArray, sectionTime: SectionTime, location: String? = null) :
            this(DBConst.DEFAULT_ID, DBConst.DEFAULT_ID, weekNum, sectionTime, location)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseTime

        if (timeId != other.timeId) return false
        if (courseId != other.courseId) return false
        if (!weekNum.contentEquals(other.weekNum)) return false
        if (sectionTime != other.sectionTime) return false
        if (location != other.location) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timeId.hashCode()
        result = 31 * result + courseId.hashCode()
        result = 31 * result + weekNum.contentHashCode()
        result = 31 * result + sectionTime.hashCode()
        result = 31 * result + (location?.hashCode() ?: 0)
        return result
    }
}