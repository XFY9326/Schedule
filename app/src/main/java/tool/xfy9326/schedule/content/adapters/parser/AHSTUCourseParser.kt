package tool.xfy9326.schedule.content.adapters.parser

import org.json.JSONObject
import tool.xfy9326.schedule.beans.*
import tool.xfy9326.schedule.content.base.NetworkCourseParser
import tool.xfy9326.schedule.content.utils.CourseParseResult
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AHSTUCourseParser : NetworkCourseParser<Nothing>() {


    override fun onParseScheduleTimes(importOption: Int, content: String?) =
        ScheduleTime.listOf(
            8, 0, 8, 45,
            8, 55, 9, 40,
            10, 0, 10, 45,
            10, 55, 11, 40,

            14, 0, 14, 45,
            14, 55, 15, 40,
            16, 0, 16, 45,
            16, 55, 17, 40,

            19, 0, 19, 45,
            19, 55, 20, 40,
            20, 50, 21, 35,
            21, 45, 22, 30
        )

    // 将在解析课程时调用
    // 参数：导入参数，待解析的HTML
    // 返回：课程解析结果
    override fun onParseCourses(importOption: Int, content: String?): CourseParseResult {
        if (content == null) return CourseParseResult.EMPTY

        val result = parse(content)
        val builder = CourseParseResult.Builder(result.size)

        for (i in result.values) {
            val courseTimes: ArrayList<CourseTime> = ArrayList()
            for (j in i) {//同一classid的课程
                val tp_hmap = j["time"] as HashMap<Int, IntArray>
                for (k: Int in (tp_hmap).keys) {//星期几  [第几节课,持续几节]
                    courseTimes.add(CourseTime(j["week"] as BooleanArray, WeekDay.of(k), tp_hmap[k]!![0], tp_hmap[k]!![1], j["classRoom"].toString()))
                }

            }
            val tp = Course(i[0]["className"].toString(), i[0]["teacherName"].toString(), courseTimes.toList())
            builder.add(tp)
        }

        return builder.build()
    }


    override fun onParseTerm(importOption: Int, content: String?): Pair<Date, Date>? {
        return null
    }


    //xk
    private fun parse_Week(str: String): BooleanArray {
        //00000100011111100000000
        val tp = BooleanArray(str.length - 1)
        var i = 0
        while (i < tp.size) {
            tp[i] = str[i + 1] == '1'
            i++
        }
        return tp
    }


    private fun parse(course_js: String): HashMap<String, ArrayList<JSONObject>> {
        val ret: HashMap<String, ArrayList<JSONObject>> = HashMap()//课程id
        val items = course_js.split(Regex(split_regex))
        var i = 1
        while (i < items.size) {
            val tp1 = parseItem(items[i])
            val classId: String = tp1["classId"].toString()
            if (ret.containsKey(classId)) {
                ret[classId]?.add(tp1)
            } else {
                ret.put(classId, arrayListOf(tp1))
            }
            i++
        }
        return ret
    }

    var pattern_teacher = Pattern.compile("\\[\\{id:\\d+,name:\"(.*?)\",lab:(false|true)\\}\\];")
    var pattern_course_info = Pattern.compile("actTeacherName.join\\(','\\),\"(.*?)\",\"(.*?)\",\".*?\",\"(.*?)\",\"(.*?)\",.*?assistantName,\".*?\",\"(.*?)\"")
    var pattern_day = Pattern.compile("(\\d+)\\*[\\s]*unitCount[\\s]*\\+(\\d+)")
    var split_regex = "var\\s*teachers\\s*=\\s*\\[.*?\\];"


    private fun parseItem(item: String): JSONObject {
        val ret = JSONObject()
        //匹配教师姓名
        val matcher_teacher: Matcher = pattern_teacher.matcher(item)
        matcher_teacher.find()
        ret.put("teacherName", matcher_teacher.group(1))//教师姓名
        //匹配课程信息
        val matcher_info = pattern_course_info.matcher(item)
        matcher_info.find()

        var tpname = ""
        if (matcher_info.group(5) != "") {
            tpname = " 组" + matcher_info.group(5)
        }
        ret.put("classId", matcher_info.group(1))
        ret.put("className", parseClassname(matcher_info.group(2)))
        ret.put("classRoom", matcher_info.group(3) + tpname)
        ret.put("week", parse_Week(matcher_info.group(4)))


        val tp: HashMap<Int, IntArray> = HashMap()//星期几  [第几节课,持续几节]
        val matcher_time = pattern_day.matcher(item)
        while (matcher_time.find()) {
            val w: Int = matcher_time.group(1).toInt() + 1
            if (tp.containsKey(w)) {
                tp[w]!![1] = tp[w]!![1] + 1
            } else {
                tp[w] = intArrayOf(matcher_time.group(2).toInt() + 1, 1)
            }
        }
        ret.put("time", tp)
        return ret
    }

    private fun parseClassname(ClassName: String): String {
        var tp_i = 0
        for (i in ClassName.length - 1 downTo 0) {
            when (ClassName[i]) {
                ')' -> tp_i++
                '(' -> tp_i--
                else -> continue
            }
            if (tp_i == 0) {
                return ClassName.substring(0, i)
            }
        }
        return ""
    }
}
