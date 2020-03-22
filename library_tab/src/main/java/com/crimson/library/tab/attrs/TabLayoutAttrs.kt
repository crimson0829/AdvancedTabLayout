package com.crimson.library.tab.attrs

import android.graphics.Color
import android.view.Gravity

/**
 * @author crimson
 * @date   2020/3/22
 */


/**
 * Tab属性
 */
data class TabAttrs(
    var padding: Int = 20,
    var width: Int = -1,
    var space_equal: Boolean = false,
    var textsize: Int = 14,
    var textSelectColor: Int = Color.parseColor("#ffffff"),
    var textUnselectColor: Int = Color.parseColor("#AAffffff"),
    var textBold: Int = 0,
    val textAllCaps: Boolean = false
)

/**
 * 指示器属性
 */
data class IndicatorAttrs(
    var color: Int = Color.parseColor("#ffffff"),
    var height: Int = 4,
    var width: Int = -1,
    var margin_left: Int = 0,
    var margin_top: Int = 0,
    var margin_right: Int = 0,
    var margin_bottom: Int = 0,
    var corner_radius: Int = 0,
    var gravity: Int = Gravity.BOTTOM,
    var style: Int = 0,
    val width_equal_title: Boolean = false
)

/**
 * 下划线属性
 */
data class UnderlineAttrs(
    var color: Int = Color.parseColor("#ffffff"),
    var height: Int = 0,
    var gravity: Int = Gravity.BOTTOM
)

/**
 * 分割线属性
 */
data class DividerAttrs(
    var color: Int = Color.parseColor("#ffffff"),
    var width: Int = 0,
    var padding: Int = 12
)