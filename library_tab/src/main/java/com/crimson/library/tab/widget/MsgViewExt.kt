package com.crimson.library.tab.widget

import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout

/**
 * @author crimson
 * @date   2020/3/21
 * MsgView 扩展函数
 */

/**
 * 展示
 */
fun MsgView.show(num: Int) {

    val lp =
        layoutParams as? RelativeLayout.LayoutParams
    val dm = resources.displayMetrics
    visibility = View.VISIBLE
    if (num <= 0) {
        //圆点,设置默认宽高
        setStrokeWidth(0)
        text = ""
        lp?.width = (5 * dm.density).toInt()
        lp?.height = (5 * dm.density).toInt()
        lp?.let {
            layoutParams = it
        }

    } else {
        lp?.height = (18 * dm.density).toInt()
        when (num) {
            in 1..9 -> { //圆
                lp?.width = (18 * dm.density).toInt()
                text = num.toString()
            }
            in 10..99 -> { //圆角矩形,圆角是高度的一半,设置默认padding
                lp?.width = ViewGroup.LayoutParams.WRAP_CONTENT
                setPadding((6 * dm.density).toInt(), 0, (6 * dm.density).toInt(), 0)
                text = num.toString()
            }
            else -> { //数字超过两位,显示99+
                lp?.width = ViewGroup.LayoutParams.WRAP_CONTENT
                setPadding((6 * dm.density).toInt(), 0, (6 * dm.density).toInt(), 0)
                text = "99+"
            }
        }
        lp?.let {
            layoutParams = it
        }
    }
}

/**
 * 设置大小
 */
fun MsgView.setSize(size: Int){
    val lp = layoutParams as? RelativeLayout.LayoutParams
    lp?.width = size
    lp?.height = size
    lp?.let {
        layoutParams = it
    }

}
