# AdvancedTabLayout

## 介绍

Tab 控件，支持AndroidX，支持绑定ViewPager2



<br>

![AdvancedTabLayout](https://github.com/crimson0829/AdvancedTabLayout/blob/master/snapshot/snapshot.gif)

<br>



## 引入


```
dependencies {
            
    implementation 'com.github.crimson0829:AdvancedTabLayout:1.4'	
}
	
```



## 使用

```
     <com.crimson.library.tab.AdvancedTabLayout
           android:id="@+id/tab_layout"
           android:layout_width="match_parent"
           android:layout_height="50dp"
           app:tl_smoothScroll_enable="true"
           app:tl_underline_height="1px"
           app:tl_underline_color="#999999"
           app:tl_textBold="SELECT"
           app:tl_textSelectColor="@color/colorPrimary"
           app:tl_textUnselectColor="#333333"
           app:tl_indicator_corner_radius="2dp"
           app:tl_indicator_width="30dp"
           app:tl_indicator_height="3dp"
           app:tl_indicator_color="@color/colorPrimary"
           app:tl_textsize="15sp"
           app:tl_textSelectSize="15sp"
            />
```


```
    
     // 设置绑定ViewPager2
     tab_layout.setViewPager2(view_pager2, fragmentActivty, fragments, titles)

     //设置ViewPager2滑动监听
     tab_layout.setViewPage2ScrollListener({
            //select
        }, { position, positionOffset, positionOffsetPixels ->
            //scroll
        }, {
            //state

        })

    // 只设置TabLayout标题
    tab_layout.setTabData(listOf(content, content, content))
    
    //设置TabLayout点击监听
    tab_layout.setOnTabSelectListener({
            //tabSelect
        }, {
            //tabReselect
        })


    //如果想设置fragment缓存，可设置RecyclerView缓存，这样fragment加载后就不会被回收
    (view_pager2.getChildAt(0) as? RecyclerView)?.setItemViewCacheSize(fragments.size)
    

```


## 属性


| 属性                   | 定义                     |
|:---------------------|:-----------------------|
| tl_indicator_color       | 指示器颜色 |
| tl_indicator_height       | 指示器高度 |
| tl_indicator_width      | 指示器宽度                  |
| tl_indicator_margin_left        | 指示器左margin                 |
| tl_indicator_margin_top       | 指示器上margin                   |
| tl_indicator_margin_right       | 指示器右margin                   |
| tl_indicator_margin_bottom       | 指示器下margin                   |
| tl_indicator_corner_radius                 | 指示器shape                 |
| tl_indicator_gravity                   | 指示器gravity                |
| tl_indicator_style                    | 指示器style -> 0:普通 1：三角形 2：块状       |
| tl_indicator_width_equal_title            | 指示器是否与标题相等                  |
| tl_underline_color                 | 下划线颜色                  |
| tl_underline_height                   | 下划线高度                  |
| tl_underline_gravity                | 下划线gravity                   |
| tl_divider_color                     | 分割线颜色                  |
| tl_divider_width                      | 分割线宽度                  |
| tl_divider_padding                    | 分割线padding                |
| tl_tab_padding                     | tab padding                   |
| tl_tab_space_equal                     | tab是否相等                 |
| tl_tab_width                  | tab宽度                |
| tl_smoothScroll_enable                  | tab点击是否平滑滑动                |
| tl_textsize                     | tab字体大小                  |
| tl_textSelectSize                     | tab选中字体大小                  |
| tl_textSelectColor                   | tab选中字体颜色               |
| tl_textUnselectColor                       | tab未选中字体颜色                  |
| tl_textBold                       | tab字体加粗 -> 0:不加粗 1：选中加粗 2：都加粗                |
| tl_textAllCaps                     | tab字体是否大写                 |


代码设置

```

    tab_layout
        //设置tab属性
        .setTabAttrs(TabAttrs())
        //设置指示器属性
        .setIndicatorAttrs(IndicatorAttrs())
        //设置下划线属性
        .setUnderlineAttrs(UnderlineAttrs())
        //设置分割线属性
        .setDividerLineAttrs(DividerAttrs())
 

```


## 方法

```
    //选中当前tab
    tab_layout.setCurrentTab(position)

    //根据索引获取title view
    tab_layout.getTitleView(position)

    /**
    * 显示未读消息
    *
    * @param position 显示tab位置
    * @param num      num小于等于0显示红点,num大于0显示数字
    */
    tab_layout.showMsg(position, num)

    //显示未读红点
    tab_layout.showDot(position)

    //隐藏未读消息
    tab_layout.hideMsg(position) 

    //设置未读消息偏移,原点为文字的右上角.当控件高度固定,消息提示位置易控制,显示效果佳
    tab_layout.setMsgMargin(position, leftPadding, bottomPadding)

    //当前类只提供了少许设置未读消息属性的方法,可以通过该方法获取MsgView对象从而各种设置
    tab_layout.getMsgView(position)


```

## 感谢


[FlycoTabLayout](https://github.com/H07000223/FlycoTabLayout)



## License

```
Copyright 2020 crimson0829
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```


