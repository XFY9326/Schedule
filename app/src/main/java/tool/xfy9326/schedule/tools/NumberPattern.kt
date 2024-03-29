package tool.xfy9326.schedule.tools

import io.github.xfy9326.atools.base.asArray
import tool.xfy9326.schedule.beans.TimePeriod
import tool.xfy9326.schedule.tools.NumberPattern.PatternType.EMPTY
import tool.xfy9326.schedule.tools.NumberPattern.PatternType.MESSY
import tool.xfy9326.schedule.tools.NumberPattern.PatternType.SERIAL
import tool.xfy9326.schedule.tools.NumberPattern.PatternType.SINGLE
import tool.xfy9326.schedule.tools.NumberPattern.PatternType.SPACED

class NumberPattern(boolArray: BooleanArray) {
    enum class PatternType {
        /**
         * Empty week number array
         * Useless properties: start, end, interval
         * amount = 0
         * timePeriodArray empty
         *
         * @constructor Create EMPTY
         */
        EMPTY,

        /**
         * Single week number
         * Useless properties: interval
         * start == end
         * amount = 1
         * timePeriodArray.size = 1
         *
         * @constructor Create SINGLE
         */
        SINGLE,

        /**
         * Serial week number array
         * interval = 1
         * timePeriodArray.size = 1
         *
         * @constructor Create SERIAL
         */
        SERIAL,

        /**
         * Spaced week number array
         * timePeriodArray.size = amount
         *
         * @constructor Create SPACED
         */
        SPACED,

        /**
         * Messy week number array
         * Useless properties: start, end, interval, amount
         *
         * @constructor Create MESSY
         */
        MESSY;
    }

    companion object {
        private fun parseTimePeriodArray(boolArray: BooleanArray): Array<TimePeriod> {
            val result = ArrayList<TimePeriod>()
            var i = 0
            while (i < boolArray.size) {
                if (boolArray[i]) {
                    var j = i + 1
                    while (j < boolArray.size && boolArray[j]) {
                        j++
                    }
                    j--
                    result.add(TimePeriod(i, j))
                    i += j - i
                }
                i++
            }
            return result.toTypedArray()
        }

        private fun parseSpacedTimePeriodArray(amount: Int, start: Int, interval: Int) = Array(amount) {
            TimePeriod(start + it * interval)
        }
    }

    val type: PatternType
    val start: Int
    val end: Int
    val interval: Int
    val amount: Int
    val timePeriodArray: Array<TimePeriod>

    init {
        var startIndex = 0
        var endIndex = 0
        var indexInterval = 0

        var metFirst = false
        var setInterval = false
        var messy = false

        for ((i, b) in boolArray.withIndex()) {
            if (b) {
                if (metFirst) {
                    if (setInterval) {
                        if (i - endIndex != indexInterval) {
                            messy = true
                            break
                        }
                    } else {
                        setInterval = true
                        indexInterval = i - startIndex
                    }
                } else {
                    metFirst = true
                    startIndex = i
                }
                endIndex = i
            }
        }

        type = when {
            messy -> {
                start = -1
                end = -1
                interval = -1
                amount = -1
                timePeriodArray = parseTimePeriodArray(boolArray)
                MESSY
            }

            !metFirst -> {
                start = -1
                end = -1
                interval = -1
                amount = 0
                timePeriodArray = emptyArray()
                EMPTY
            }

            startIndex == endIndex && !setInterval -> {
                start = startIndex
                end = startIndex
                interval = -1
                amount = 1
                timePeriodArray = TimePeriod(start).asArray()
                SINGLE
            }

            indexInterval == 1 -> {
                start = startIndex
                end = endIndex
                interval = 1
                amount = endIndex - startIndex + 1
                timePeriodArray = TimePeriod(start, end).asArray()
                SERIAL
            }

            else -> {
                start = startIndex
                end = endIndex
                interval = indexInterval
                amount = ((endIndex - startIndex) / indexInterval) + 1
                timePeriodArray = parseSpacedTimePeriodArray(amount, start, interval)
                SPACED
            }
        }
    }
}