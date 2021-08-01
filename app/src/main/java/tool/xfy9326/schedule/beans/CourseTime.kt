package tool.xfy9326.schedule.beans

import android.content.Context
import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize
import tool.xfy9326.schedule.beans.ClassTime.Companion.intersect
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
        value = [DBConst.COLUMN_COURSE_ID, DBConst.COLUMN_WEEK_DAY, DBConst.COLUMN_CLASS_START_TIME, DBConst.COLUMN_CLASS_DURATION, DBConst.COLUMN_WEEK_NUM],
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
    var classTime: ClassTime,
    var location: String? = null,
) : Parcelable {

    companion object {
        infix fun CourseTime.intersect(courseTime: CourseTime): Boolean {
            for (i in 0 until min(weekNum.size, courseTime.weekNum.size)) {
                if (weekNum[i] && courseTime.weekNum[i] && classTime intersect courseTime.classTime) return true
            }
            return false
        }

        fun CourseTime.getWeeksDescriptionText(context: Context) = NumberPattern(weekNum).getWeeksDescriptionText(context)
    }

    constructor(weekNum: BooleanArray, weekDay: WeekDay, classStartTime: Int, classDuration: Int, location: String? = null) :
            this(weekNum, ClassTime(weekDay, classStartTime, classDuration), location)

    constructor(weekNum: BooleanArray, classTime: ClassTime, location: String? = null) :
            this(DBConst.DEFAULT_ID, DBConst.DEFAULT_ID, weekNum, classTime, location)

    operator fun compareTo(courseTime: CourseTime): Int {
        for (i in 0 until min(weekNum.size, courseTime.weekNum.size)) {
            if (weekNum[i] && !courseTime.weekNum[i]) {
                return 1
            } else if (!weekNum[i] && courseTime.weekNum[i]) {
                return -1
            }
        }
        return classTime.compareTo(courseTime.classTime)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseTime

        if (timeId != other.timeId) return false
        if (courseId != other.courseId) return false
        if (!weekNum.contentEquals(other.weekNum)) return false
        if (classTime != other.classTime) return false
        if (location != other.location) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timeId.hashCode()
        result = 31 * result + courseId.hashCode()
        result = 31 * result + weekNum.contentHashCode()
        result = 31 * result + classTime.hashCode()
        result = 31 * result + (location?.hashCode() ?: 0)
        return result
    }
}