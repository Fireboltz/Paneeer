package org.amfoss.paneeer.gallery.util

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import org.amfoss.paneeer.R

object ColorPalette {
    @JvmStatic
    fun getAccentColors(context: Context?): IntArray {
        return intArrayOf(
            ContextCompat.getColor(context!!, R.color.md_red_500),
            ContextCompat.getColor(context, R.color.md_purple_500),
            ContextCompat.getColor(context, R.color.md_deep_purple_500),
            ContextCompat.getColor(context, R.color.md_blue_500),
            ContextCompat.getColor(context, R.color.md_light_blue_500),
            ContextCompat.getColor(context, R.color.md_cyan_500),
            ContextCompat.getColor(context, R.color.md_teal_500),
            ContextCompat.getColor(context, R.color.md_green_500),
            ContextCompat.getColor(context, R.color.md_yellow_500),
            ContextCompat.getColor(context, R.color.md_orange_500),
            ContextCompat.getColor(context, R.color.md_deep_orange_500),
            ContextCompat.getColor(context, R.color.md_brown_500),
            ContextCompat.getColor(context, R.color.md_blue_grey_500)
        )
    }

    @JvmStatic
    fun getObscuredColor(c: Int): Int {
        val hsv = FloatArray(3)
        var color = c
        Color.colorToHSV(color, hsv)
        hsv[2] *= 0.85f // value component
        color = Color.HSVToColor(hsv)
        return color
    }

    @JvmStatic
    fun getLighterColor(c: Int): Int {
        val hsv = FloatArray(3)
        var color = c
        Color.colorToHSV(color, hsv)
        hsv[2] *= 1.35f // value component
        color = Color.HSVToColor(hsv)
        return color
    }

    @JvmStatic
    fun getTransparentColor(color: Int, alpha: Int): Int {
        return ColorUtils.setAlphaComponent(color, alpha)
    }
}