package com.crimson.tab

import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.crimson.library.tab.attrs.DividerAttrs
import com.crimson.library.tab.attrs.IndicatorAttrs
import com.crimson.library.tab.attrs.TabAttrs
import com.crimson.library.tab.attrs.UnderlineAttrs
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
    }

    private fun initView() {

        val fragments = arrayListOf<Fragment>()

        val titles = listOf(
            "content1",
            "content2",
            "content3",
            "content4",
            "content5",
            "content6",
            "content7"
        ).apply {
            forEach {
                fragments.add(ContentFragment(it))
            }
        }

        tab_layout.setViewPager2(view_pager2, this, fragments, titles)

        tab_layout.setViewPage2ScrollListener({
            //select
            Log.w(TAG, "select -> $it")

        }, { position, positionOffset, positionOffsetPixels ->
            //scroll
            Log.w(
                TAG,
                "sroll : position-> $position  positionOffset -> $positionOffset  positionOffsetPixels->$positionOffsetPixels"
            )

        }, {
            //state
            Log.w(TAG, "state -> $it")

        })

        //设置viewpager2 条目缓存数量
        tab_layout.setViewPager2ItemCacheSize(fragments.size)

//        tab_layout.setTabAttrs(TabAttrs().apply {
//            textSelectColor=ContextCompat.getColor(baseContext,R.color.colorPrimary)
//            // tab点击是否马上切换,如果设置true，那么smoothscroll将失效
//            snap_tab_click=true
//        })







    }


}
