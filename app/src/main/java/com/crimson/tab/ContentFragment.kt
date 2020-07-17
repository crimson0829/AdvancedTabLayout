package com.crimson.tab

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_content.*

/**
 * @author crimson
 * @date   2020/3/21
 */
class ContentFragment(val content: String = ContentFragment::class.java.simpleName) :
    Fragment(R.layout.fragment_content) {

    val TAG = ContentFragment::class.java.simpleName

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        tab_layout.setTabData(listOf(content, content, content,content,content,content))
        tab_layout.showDot(0)
        tab_layout.showMsg(1, 10)
        tab_layout.setMsgMargin(1, 30f, 5f)
        tab_layout.showMsg(2, 100)
        tab_layout.setMsgMargin(2, 30f, 10f)

        tab_layout.setOnTabSelectListener({

            Log.w(TAG, "tabSelect -> $it")

            tab_layout.hideMsg(it)
            tv_content.text = "select -> $it"

        }, {

            Log.w(TAG, "tabReselect -> $it")

            tv_content.text = "Reselect -> $it"

        })
    }
}


