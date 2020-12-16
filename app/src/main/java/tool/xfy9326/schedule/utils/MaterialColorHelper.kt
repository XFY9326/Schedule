package tool.xfy9326.schedule.utils

import androidx.annotation.FloatRange
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt

@Suppress("SpellCheckingInspection")
object MaterialColorHelper {
    @FloatRange(from = 0.0, to = 1.0)
    private const val LUMINANCE_IS_LIGHT_COLOR = 0.7f

    private val MATERIAL_COLORS = arrayOf(
        // material_red_300
        "#E57373",
        // material_pink_300
        "#F06292",
        // material_purple_300
        "#BA68C8",
        // material_deep_purple_300
        "#9575CD",

        // material_indigo_300
        "#7986CB",
        // material_blue_300
        "#64B5F6",
        // material_light_blue_300
        "#4FC3F7",
        // material_cyan_300
        "#4DD0E1",

        // material_teal_300
        "#4DB6AC",
        // material_green_300
        "#81C784",
        // material_light_green_400
        "#66BB6A",
        // material_lime_400
        "#D4E157",

        // material_amber_400
        "#FFCA28",
        // material_orange_300
        "#FFB74D",
        // material_deep_orange_300
        "#FF8A65"
    )

    fun all() = MATERIAL_COLORS.map {
        it.toColorInt()
    }.toTypedArray().toIntArray()

    fun random() = MATERIAL_COLORS.random().toColorInt()

    fun isLightColor(color: Int) = ColorUtils.calculateLuminance(color) >= LUMINANCE_IS_LIGHT_COLOR
}