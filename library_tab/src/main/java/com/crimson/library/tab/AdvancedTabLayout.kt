package com.crimson.library.tab

import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseBooleanArray
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.crimson.library.tab.attrs.DividerAttrs
import com.crimson.library.tab.attrs.IndicatorAttrs
import com.crimson.library.tab.attrs.TabAttrs
import com.crimson.library.tab.attrs.UnderlineAttrs
import com.crimson.library.tab.widget.MsgView
import com.crimson.library.tab.widget.show
import java.util.*

/**
 * AdvancedTabLayout,可设置ViewPager2 ：调用setViewPager2()
 * 如不想关联Viewpager2 调用：setTabData()
 */
class AdvancedTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    companion object {
        private const val STYLE_NORMAL = 0
        private const val STYLE_TRIANGLE = 1
        private const val STYLE_BLOCK = 2

        private const val TEXT_BOLD_NONE = 0
        private const val TEXT_BOLD_WHEN_SELECT = 1
        private const val TEXT_BOLD_BOTH = 2
    }

    private var mVP2: ViewPager2? = null
    private var mViewPager2PageChangeCallback: ViewPager2PageChangeCallback? = null
    private var mTitles = arrayListOf<String>()
    private val mTabsContainer: LinearLayout
    private var mCurrentTab = 0
    private var mCurrentPositionOffset = 0f
    private var tabCount = 0

    /**
     * 用于绘制显示器
     */
    private val mIndicatorRect = Rect()

    /**
     * 用于实现滚动居中
     */
    private val mTabRect = Rect()
    private val mIndicatorDrawable = GradientDrawable()
    private val mRectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mDividerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mTrianglePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mTrianglePath = Path()

    /**
     * tab
     */
    private var mTabPadding = 0f
    private var mTabSpaceEqual = false
    private var mTabWidth = 0f

    /**
     * indicator
     */
    private var mIndicatorColor = 0
    private var mIndicatorHeight = 0f
    private var mIndicatorWidth = 0f
    private var mIndicatorCornerRadius = 0f
    private var indicatorMarginLeft = 0f
    private var indicatorMarginTop = 0f
    private var indicatorMarginRight = 0f
    private var indicatorMarginBottom = 0f
    private var mIndicatorGravity = 0
    private var mIndicatorWidthEqualTitle = false
    private var mIndicatorStyle = STYLE_NORMAL


    /**
     * underline
     */
    private var mUnderlineColor = 0
    private var mUnderlineHeight = 0f
    private var mUnderlineGravity = 0

    /**
     * divider
     */
    private var mDividerColor = 0
    private var mDividerWidth = 0f
    private var mDividerPadding = 0f

    /**
     * tab text
     */
    private var mTextsize = 0f
    private var mTextSelectColor = 0
    private var mTextUnselectColor = 0
    private var mTextBold = 0
    private var mTextAllCaps = false

    private var mSnapOnTabClick = false
    private var mLastScrollX = 0
    private var mHeight = 0

    // show MsgView
    private val mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mInitSetMap = SparseBooleanArray()

    //tab点击回调
    private var onTabSelect: (Int) -> Unit = {}
    private var onTabReselect: (Int) -> Unit = {}

    //viewpager2滑动回调
    private var mVP2OnPageSelected: (Int) -> Unit = {}
    private var mVP2OnPageScrolled: (Int, Float, Int) -> Unit = { _, _, _ -> }
    private var mVP2OnPageScrollStateChanged: (state: Int) -> Unit = { _ -> }


    init {
        //设置滚动视图是否可以伸缩其内容以填充视口
        isFillViewport = true
        //重写onDraw方法,需要调用这个方法来清除flag
        setWillNotDraw(false)
        clipChildren = false
        clipToPadding = false
        mTabsContainer = LinearLayout(context)
        addView(mTabsContainer)
        obtainAttributes(context, attrs)

        //get layout_height
        val height =
            attrs?.getAttributeValue("http://schemas.android.com/apk/res/android", "layout_height")
        when (height) {
            ViewGroup.LayoutParams.MATCH_PARENT.toString() + "" -> {
            }
            ViewGroup.LayoutParams.WRAP_CONTENT.toString() + "" -> {
            }
            else -> {
                val systemAttrs = intArrayOf(android.R.attr.layout_height)
                val a = context.obtainStyledAttributes(attrs, systemAttrs)
                mHeight = a.getDimensionPixelSize(0, ViewGroup.LayoutParams.WRAP_CONTENT)
                a.recycle()
            }
        }
    }

    private fun obtainAttributes(
        context: Context,
        attrs: AttributeSet?
    ) {
        val ta =
            context.obtainStyledAttributes(attrs, R.styleable.AdvancedTabLayout)
        mIndicatorStyle = ta.getInt(
            R.styleable.AdvancedTabLayout_tl_indicator_style,
            STYLE_NORMAL
        )
        mIndicatorColor = ta.getColor(
            R.styleable.AdvancedTabLayout_tl_indicator_color,
            Color.parseColor(if (mIndicatorStyle == STYLE_BLOCK) "#4B6A87" else "#ffffff")
        )
        mIndicatorHeight = ta.getDimension(
            R.styleable.AdvancedTabLayout_tl_indicator_height,
            dp2px(if (mIndicatorStyle == STYLE_TRIANGLE) 4f else (if (mIndicatorStyle == STYLE_BLOCK) -1f else 2f)).toFloat()
        )
        mIndicatorWidth = ta.getDimension(
            R.styleable.AdvancedTabLayout_tl_indicator_width,
            dp2px(if (mIndicatorStyle == STYLE_TRIANGLE) 10f else (-1).toFloat()).toFloat()
        )
        mIndicatorCornerRadius = ta.getDimension(
            R.styleable.AdvancedTabLayout_tl_indicator_corner_radius,
            dp2px(if (mIndicatorStyle == STYLE_BLOCK) -1f else 0f).toFloat()
        )
        indicatorMarginLeft = ta.getDimension(
            R.styleable.AdvancedTabLayout_tl_indicator_margin_left,
            dp2px(0f).toFloat()
        )
        indicatorMarginTop = ta.getDimension(
            R.styleable.AdvancedTabLayout_tl_indicator_margin_top,
            dp2px(if (mIndicatorStyle == STYLE_BLOCK) 7f else 0f).toFloat()
        )
        indicatorMarginRight = ta.getDimension(
            R.styleable.AdvancedTabLayout_tl_indicator_margin_right,
            dp2px(0f).toFloat()
        )
        indicatorMarginBottom = ta.getDimension(
            R.styleable.AdvancedTabLayout_tl_indicator_margin_bottom,
            dp2px(if (mIndicatorStyle == STYLE_BLOCK) 7f else 0f).toFloat()
        )
        mIndicatorGravity = ta.getInt(
            R.styleable.AdvancedTabLayout_tl_indicator_gravity,
            Gravity.BOTTOM
        )
        mIndicatorWidthEqualTitle = ta.getBoolean(
            R.styleable.AdvancedTabLayout_tl_indicator_width_equal_title,
            false
        )
        mUnderlineColor = ta.getColor(
            R.styleable.AdvancedTabLayout_tl_underline_color,
            Color.parseColor("#ffffff")
        )
        mUnderlineHeight = ta.getDimension(
            R.styleable.AdvancedTabLayout_tl_underline_height,
            dp2px(0f).toFloat()
        )
        mUnderlineGravity = ta.getInt(
            R.styleable.AdvancedTabLayout_tl_underline_gravity,
            Gravity.BOTTOM
        )
        mDividerColor = ta.getColor(
            R.styleable.AdvancedTabLayout_tl_divider_color,
            Color.parseColor("#ffffff")
        )
        mDividerWidth = ta.getDimension(
            R.styleable.AdvancedTabLayout_tl_divider_width,
            dp2px(0f).toFloat()
        )
        mDividerPadding = ta.getDimension(
            R.styleable.AdvancedTabLayout_tl_divider_padding,
            dp2px(12f).toFloat()
        )
        mTextsize = ta.getDimension(
            R.styleable.AdvancedTabLayout_tl_textsize,
            sp2px(14f).toFloat()
        )
        mTextSelectColor = ta.getColor(
            R.styleable.AdvancedTabLayout_tl_textSelectColor,
            Color.parseColor("#ffffff")
        )
        mTextUnselectColor = ta.getColor(
            R.styleable.AdvancedTabLayout_tl_textUnselectColor,
            Color.parseColor("#AAffffff")
        )
        mTextBold = ta.getInt(
            R.styleable.AdvancedTabLayout_tl_textBold,
            TEXT_BOLD_NONE
        )
        mTextAllCaps =
            ta.getBoolean(R.styleable.AdvancedTabLayout_tl_textAllCaps, false)
        mTabSpaceEqual = ta.getBoolean(
            R.styleable.AdvancedTabLayout_tl_tab_space_equal,
            false
        )
        mTabWidth = ta.getDimension(
            R.styleable.AdvancedTabLayout_tl_tab_width,
            dp2px(-1f).toFloat()
        )
        mTabPadding = ta.getDimension(
            R.styleable.AdvancedTabLayout_tl_tab_padding,
            if (mTabSpaceEqual || mTabWidth > 0) dp2px(0f).toFloat() else dp2px(20f).toFloat()
        )
        ta.recycle()
    }


    /**
     * 设置ViewPager 如果不想绑定fragment 就无需设置 fa 和 fragments
     * @param vp2
     * @param fa
     * @param fragments
     * @param titles tab标题
     */
    fun setViewPager2(
        vp2: ViewPager2?,
        fa: FragmentActivity?,
        fragments: ArrayList<Fragment>?,
        titles: List<String>?
    ) {

        vp2?.also {
            mVP2 = it
            if (fa != null && fragments != null) {
                mVP2?.adapter = ViewPager2FragmentAdapter(fa, fragments)
            }
            if (mViewPager2PageChangeCallback == null) {
                mViewPager2PageChangeCallback = ViewPager2PageChangeCallback()
            }
            mViewPager2PageChangeCallback?.let { callback ->
                mVP2?.unregisterOnPageChangeCallback(callback)
                mVP2?.registerOnPageChangeCallback(callback)
            }

        }

        titles?.let {
            mTitles.addAll(titles)
        }

        notifyData()
    }

    /**
     * 只设置tab data
     */
    fun setTabData(titles: List<String>?) {
        titles?.let {
            mTitles.addAll(titles)
        }
        notifyData()
    }

    /**
     * 更新数据
     */
    fun notifyData() {
        mTabsContainer.removeAllViews()
        tabCount = mTitles.size
        var tabView: View
        for (i in 0 until tabCount) {
            tabView = View.inflate(context, R.layout.layout_tab, null)
            addTab(i, mTitles[i], tabView)
        }
        updateTabStyles()
    }

    fun addNewTab(title: String) {
        val tabView = View.inflate(context, R.layout.layout_tab, null)
        mTitles.add(title)
        addTab(tabCount, title, tabView)
        tabCount = mTitles.size
        updateTabStyles()
    }

    /**
     * 创建并添加tab
     */
    private fun addTab(
        position: Int, title: String?, tabView: View
    ) {

        val tvTabTitle =
            tabView.findViewById<View>(R.id.tv_tab_title) as? AppCompatTextView
        tvTabTitle?.text = title ?: ""

        tabView.setOnClickListener { v ->
            val position = mTabsContainer.indexOfChild(v)
            if (position != -1) {
                if (mVP2 != null) {
                    if (mVP2?.currentItem != position) {
                        if (mSnapOnTabClick) {
                            mVP2?.setCurrentItem(position, false)
                        } else {
                            mVP2?.currentItem = position
                        }

                        onTabSelect.invoke(position)
                    } else {
                        onTabReselect.invoke(position)
                    }
                } else {
                    if (mCurrentTab != position) {
                        mCurrentTab = position
                        scrollToCurrentTab()
                        updateTabSelection(position)
                        onTabSelect.invoke(position)
                    } else {
                        onTabReselect.invoke(position)
                    }
                }

            }
        }
        /** 每一个Tab的布局参数  */
        var lpTab = if (mTabSpaceEqual) LinearLayout.LayoutParams(
            0,
            LayoutParams.MATCH_PARENT,
            1.0f
        ) else LinearLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.MATCH_PARENT
        )
        if (mTabWidth > 0) {
            lpTab = LinearLayout.LayoutParams(mTabWidth.toInt(), LayoutParams.MATCH_PARENT)
        }
        mTabsContainer.addView(tabView, position, lpTab)
    }

    private fun updateTabStyles() {
        for (i in 0 until tabCount) {
            val v = mTabsContainer.getChildAt(i)
            //            v.setPadding((int) mTabPadding, v.getPaddingTop(), (int) mTabPadding, v.getPaddingBottom());
            val tvTabTitle =
                v?.findViewById<View>(R.id.tv_tab_title) as? AppCompatTextView

            if (tvTabTitle != null) {
                tvTabTitle.setTextColor(if (i == mCurrentTab) mTextSelectColor else mTextUnselectColor)
                if (mTextBold == TEXT_BOLD_BOTH) {
                    tvTabTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextsize)
                } else {
                    tvTabTitle.setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        if (i == mCurrentTab) sp2px(18f).toFloat() else mTextsize
                    )
                }
                tvTabTitle.setPadding(mTabPadding.toInt(), 0, mTabPadding.toInt(), 0)
                if (mTextAllCaps) {
                    tvTabTitle.text = tvTabTitle.text.toString().toUpperCase(Locale.getDefault())
                }
                if (mTextBold == TEXT_BOLD_BOTH) {
                    tvTabTitle.paint.isFakeBoldText = true
                } else if (mTextBold == TEXT_BOLD_NONE) {
                    tvTabTitle.paint.isFakeBoldText = false
                }
            }
        }
    }


    /**
     * HorizontalScrollView滚到当前tab,并且居中显示
     */
    private fun scrollToCurrentTab() {
        if (tabCount <= 0) {
            return
        }
        mTabsContainer.let {
            val offset = (mCurrentPositionOffset * it.getChildAt(mCurrentTab).width).toInt()

            /**当前Tab的left+当前Tab的Width乘以positionOffset */
            var newScrollX = it.getChildAt(mCurrentTab).left + offset
            if (mCurrentTab > 0 || offset > 0) {
                /**HorizontalScrollView移动到当前tab,并居中 */
                newScrollX -= width / 2 - paddingLeft
                calcIndicatorRect()
                newScrollX += (mTabRect.right - mTabRect.left) / 2
            }
            if (newScrollX != mLastScrollX) {
                mLastScrollX = newScrollX
                /** scrollTo（int x,int y）:x,y代表的不是坐标点,而是偏移量
                 * x:表示离起始位置的x水平方向的偏移量
                 * y:表示离起始位置的y垂直方向的偏移量
                 */
                scrollTo(newScrollX, 0)
            }
        }

    }

    private fun updateTabSelection(position: Int) {
        for (i in 0 until tabCount) {
            val tabView = mTabsContainer?.getChildAt(i)
            val isSelect = i == position
            val tabTitle =
                tabView?.findViewById<View>(R.id.tv_tab_title) as? AppCompatTextView
            if (tabTitle != null) {
                tabTitle.setTextColor(if (isSelect) mTextSelectColor else mTextUnselectColor)
                if (mTextBold == TEXT_BOLD_BOTH) {
                    tabTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextsize)
                } else {
                    tabTitle.setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        if (isSelect) sp2px(18f).toFloat() else mTextsize
                    )
                }
                if (mTextBold == TEXT_BOLD_WHEN_SELECT) {
                    tabTitle.paint.isFakeBoldText = isSelect
                }
            }
        }
        if (mViewPager2PageChangeCallback != null) {
            mViewPager2PageChangeCallback?.onPageScrolled(position, 0f, 0)
        }
    }

    private var margin = 0f

    private fun calcIndicatorRect() {
        val currentTabView = mTabsContainer.getChildAt(mCurrentTab)
        var left = currentTabView?.left?.toFloat() ?: 0f
        var right = currentTabView?.right?.toFloat() ?: 0f

        //for mIndicatorWidthEqualTitle
        if (mIndicatorStyle == STYLE_NORMAL && mIndicatorWidthEqualTitle) {
            val tabTitle =
                currentTabView?.findViewById<View>(R.id.tv_tab_title) as? AppCompatTextView
            mTextPaint.textSize = mTextsize
            val textWidth = mTextPaint.measureText(tabTitle?.text.toString())
            margin = (right - left - textWidth) / 2
        }
        if (mCurrentTab < tabCount - 1) {
            val nextTabView = mTabsContainer.getChildAt(mCurrentTab + 1)
            val nextTabLeft = nextTabView?.left?.toFloat() ?: 0f
            val nextTabRight = nextTabView?.right?.toFloat() ?: 0f
            left += mCurrentPositionOffset * (nextTabLeft - left)
            right += mCurrentPositionOffset * (nextTabRight - right)

            //for mIndicatorWidthEqualTitle
            if (mIndicatorStyle == STYLE_NORMAL && mIndicatorWidthEqualTitle) {
                val nextTabTitle =
                    nextTabView?.findViewById<View>(R.id.tv_tab_title) as? AppCompatTextView
                mTextPaint.textSize = mTextsize
                val nextTextWidth =
                    mTextPaint.measureText(nextTabTitle?.text.toString())
                val nextMargin = (nextTabRight - nextTabLeft - nextTextWidth) / 2
                margin += mCurrentPositionOffset * (nextMargin - margin)
            }
        }
        mIndicatorRect.left = left.toInt()
        mIndicatorRect.right = right.toInt()
        //for mIndicatorWidthEqualTitle
        if (mIndicatorStyle == STYLE_NORMAL && mIndicatorWidthEqualTitle) {
            mIndicatorRect.left = (left + margin - 1).toInt()
            mIndicatorRect.right = (right - margin - 1).toInt()
        }
        mTabRect.left = left.toInt()
        mTabRect.right = right.toInt()
        if (mIndicatorWidth < 0) {   //indicatorWidth小于0时,原jpardogo's PagerSlidingTabStrip
        } else { //indicatorWidth大于0时,圆角矩形以及三角形
            var indicatorLeft =
                currentTabView.left + (currentTabView.width - mIndicatorWidth) / 2
            if (mCurrentTab < tabCount - 1) {
                val nextTab = mTabsContainer.getChildAt(mCurrentTab + 1)
                indicatorLeft += mCurrentPositionOffset * (currentTabView.width / 2 + nextTab.width / 2)
            }
            mIndicatorRect.left = indicatorLeft.toInt()
            mIndicatorRect.right = (mIndicatorRect.left + mIndicatorWidth).toInt()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isInEditMode || tabCount <= 0) {
            return
        }
        val height = height
        val paddingLeft = paddingLeft
        // draw divider
        if (mDividerWidth > 0) {
            mDividerPaint.strokeWidth = mDividerWidth
            mDividerPaint.color = mDividerColor
            for (i in 0 until tabCount - 1) {
                val tab = mTabsContainer.getChildAt(i)
                canvas.drawLine(
                    paddingLeft + tab.right.toFloat(),
                    mDividerPadding,
                    paddingLeft + tab.right.toFloat(),
                    height - mDividerPadding,
                    mDividerPaint
                )
            }
        }

        // draw underline
        if (mUnderlineHeight > 0) {
            mRectPaint.color = mUnderlineColor
            if (mUnderlineGravity == Gravity.BOTTOM) {
                canvas.drawRect(
                    paddingLeft.toFloat(),
                    height - mUnderlineHeight,
                    mTabsContainer.width + paddingLeft.toFloat(),
                    height.toFloat(),
                    mRectPaint
                )
            } else {
                canvas.drawRect(
                    paddingLeft.toFloat(),
                    0f,
                    mTabsContainer.width + paddingLeft.toFloat(),
                    mUnderlineHeight,
                    mRectPaint
                )
            }
        }

        //draw indicator line
        calcIndicatorRect()
        if (mIndicatorStyle == STYLE_TRIANGLE) {
            if (mIndicatorHeight > 0) {
                mTrianglePaint.color = mIndicatorColor
                mTrianglePath.reset()
                mTrianglePath.moveTo(paddingLeft + mIndicatorRect.left.toFloat(), height.toFloat())
                mTrianglePath.lineTo(
                    paddingLeft + mIndicatorRect.left / 2 + (mIndicatorRect.right / 2).toFloat(),
                    height - mIndicatorHeight
                )
                mTrianglePath.lineTo(paddingLeft + mIndicatorRect.right.toFloat(), height.toFloat())
                mTrianglePath.close()
                canvas.drawPath(mTrianglePath, mTrianglePaint)
            }
        } else if (mIndicatorStyle == STYLE_BLOCK) {
            if (mIndicatorHeight < 0) {
                mIndicatorHeight = height - indicatorMarginTop - indicatorMarginBottom
            }
            if (mIndicatorHeight > 0) {
                if (mIndicatorCornerRadius < 0 || mIndicatorCornerRadius > mIndicatorHeight / 2) {
                    mIndicatorCornerRadius = mIndicatorHeight / 2
                }
                mIndicatorDrawable.setColor(mIndicatorColor)
                mIndicatorDrawable.setBounds(
                    paddingLeft + indicatorMarginLeft.toInt() + mIndicatorRect.left,
                    indicatorMarginTop.toInt(),
                    (paddingLeft + mIndicatorRect.right - indicatorMarginRight).toInt(),
                    (indicatorMarginTop + mIndicatorHeight).toInt()
                )
                mIndicatorDrawable.cornerRadius = mIndicatorCornerRadius
                mIndicatorDrawable.draw(canvas)
            }
        } else {
            /* mRectPaint.setColor(mIndicatorColor);
                calcIndicatorRect();
                canvas.drawRect(getPaddingLeft() + mIndicatorRect.left, getHeight() - mIndicatorHeight,
                        mIndicatorRect.right + getPaddingLeft(), getHeight(), mRectPaint);*/
            if (mIndicatorHeight > 0) {
                mIndicatorDrawable.setColor(mIndicatorColor)
                if (mIndicatorGravity == Gravity.BOTTOM) {
                    mIndicatorDrawable.setBounds(
                        paddingLeft + indicatorMarginLeft.toInt() + mIndicatorRect.left,
                        height - mIndicatorHeight.toInt() - indicatorMarginBottom.toInt(),
                        paddingLeft + mIndicatorRect.right - indicatorMarginRight.toInt(),
                        height - indicatorMarginBottom.toInt()
                    )
                } else {
                    mIndicatorDrawable.setBounds(
                        paddingLeft + indicatorMarginLeft.toInt() + mIndicatorRect.left,
                        indicatorMarginTop.toInt(),
                        paddingLeft + mIndicatorRect.right - indicatorMarginRight.toInt(),
                        mIndicatorHeight.toInt() + indicatorMarginTop.toInt()
                    )
                }
                mIndicatorDrawable.cornerRadius = mIndicatorCornerRadius
                mIndicatorDrawable.draw(canvas)
            }
        }
    }

    /**
     * 设置tab属性
     */
    fun setTabAttrs(attrs: TabAttrs = TabAttrs()): AdvancedTabLayout {
        mTabPadding = dp2px(attrs.padding.toFloat()).toFloat()
        mTabSpaceEqual = attrs.space_equal
        mTabWidth = dp2px(attrs.width.toFloat()).toFloat()
        mTextsize = sp2px(attrs.textsize.toFloat()).toFloat()
        mTextSelectColor = attrs.textSelectColor
        mTextUnselectColor = attrs.textUnselectColor
        mTextBold = attrs.textBold
        mTextAllCaps = attrs.textAllCaps
        invalidate()
        return this
    }

    /**
     * 设置指示器属性
     */
    fun setIndicatorAttrs(attrs: IndicatorAttrs = IndicatorAttrs()): AdvancedTabLayout {
        mIndicatorColor = attrs.color
        mIndicatorHeight = dp2px(attrs.height.toFloat()).toFloat()
        mIndicatorWidth = dp2px(attrs.width.toFloat()).toFloat()
        mIndicatorCornerRadius = dp2px(attrs.corner_radius.toFloat()).toFloat()
        indicatorMarginLeft = dp2px(attrs.margin_left.toFloat()).toFloat()
        indicatorMarginTop = dp2px(attrs.margin_top.toFloat()).toFloat()
        indicatorMarginRight = dp2px(attrs.margin_right.toFloat()).toFloat()
        indicatorMarginBottom = dp2px(attrs.margin_bottom.toFloat()).toFloat()
        mIndicatorGravity = attrs.gravity
        mIndicatorWidthEqualTitle = attrs.width_equal_title
        mIndicatorStyle = attrs.style
        invalidate()
        return this

    }


    /**
     * 设置下划线属性
     */
    fun setUnderlineAttrs(attrs: UnderlineAttrs = UnderlineAttrs()): AdvancedTabLayout {
        mUnderlineColor = attrs.color
        mUnderlineHeight = dp2px(attrs.height.toFloat()).toFloat()
        mUnderlineGravity = attrs.gravity
        invalidate()
        return this
    }

    /**
     * 设置分割线属性
     */
    fun setDividerLineAttrs(attrs: DividerAttrs = DividerAttrs()): AdvancedTabLayout {
        mDividerColor = attrs.color
        mDividerWidth = dp2px(attrs.width.toFloat()).toFloat()
        mDividerPadding = dp2px(attrs.padding.toFloat()).toFloat()
        invalidate()
        return this
    }

    /**
     * tab选中监听
     */
    fun setOnTabSelectListener(
        tabSelect: (Int) -> Unit = {},
        tabReselect: (Int) -> Unit = {}
    ): AdvancedTabLayout {
        onTabSelect = tabSelect
        onTabReselect = tabReselect
        return this
    }

    /**
     * viewPager2监听
     */
    fun setViewPage2ScrollListener(
        onPageSelected: (position: Int) -> Unit = { _ -> },
        onPageScrolled: (position: Int, positionOffset: Float, positionOffsetPixels: Int) -> Unit = { _, _, _ -> },
        onPageScrollStateChanged: (state: Int) -> Unit = { _ -> }
    ): AdvancedTabLayout {
        mVP2OnPageSelected = onPageSelected
        mVP2OnPageScrolled = onPageScrolled
        mVP2OnPageScrollStateChanged = onPageScrollStateChanged
        return this
    }

    /**
     * 选中当前tab
     */
    fun setCurrentTab(currentTab: Int, smoothScroll: Boolean) {
        mCurrentTab = currentTab
        mVP2?.setCurrentItem(currentTab, smoothScroll)
        updateTabSelection(currentTab)
    }


    /**
     * 根据索引获取title view
     */
    fun getTitleView(tab: Int): AppCompatTextView? {
        val tabView = mTabsContainer.getChildAt(tab)
        return tabView.findViewById<View>(R.id.tv_tab_title) as? AppCompatTextView
    }

    /**
     * 显示未读消息
     *
     * @param position 显示tab位置
     * @param num      num小于等于0显示红点,num大于0显示数字
     */
    fun showMsg(position: Int, num: Int) {
        var position = position
        if (position >= tabCount) {
            position = tabCount - 1
        }
        val tabView = mTabsContainer.getChildAt(position)
        val tipView =
            tabView.findViewById<View>(R.id.rtv_msg_tip) as? MsgView
        if (tipView != null) {
            tipView.show(num)
            if (mInitSetMap[position]) {
                return
            }
            setMsgMargin(position, 4f, 2f)
            mInitSetMap.put(position, true)
        }
    }

    /**
     * 显示未读红点
     *
     * @param position 显示tab位置
     */
    fun showDot(position: Int) {
        var position = position
        if (position >= tabCount) {
            position = tabCount - 1
        }
        showMsg(position, 0)
    }

    /**
     * 隐藏未读消息
     */
    fun hideMsg(position: Int) {
        var position = position
        if (position >= tabCount) {
            position = tabCount - 1
        }
        val tabView = mTabsContainer.getChildAt(position)
        val tipView =
            tabView.findViewById<View>(R.id.rtv_msg_tip) as? MsgView
        tipView?.visibility = View.GONE
    }

    /**
     * 设置未读消息偏移,原点为文字的右上角.当控件高度固定,消息提示位置易控制,显示效果佳
     */
    fun setMsgMargin(
        position: Int,
        leftPadding: Float,
        bottomPadding: Float
    ) {
        var position = position
        if (position >= tabCount) {
            position = tabCount - 1
        }
        val tabView = mTabsContainer.getChildAt(position)
        val tipView =
            tabView.findViewById<View>(R.id.rtv_msg_tip) as? MsgView
        if (tipView != null) {
            val tvTabTitle =
                tabView.findViewById<View>(R.id.tv_tab_title) as AppCompatTextView
            mTextPaint.textSize = mTextsize
            val textWidth = mTextPaint.measureText(tvTabTitle.text.toString())
            val textHeight = mTextPaint.descent() - mTextPaint.ascent()
            val lp = tipView.layoutParams as MarginLayoutParams
            lp.leftMargin =
                if (mTabWidth >= 0) (mTabWidth / 2 + textWidth / 2 + dp2px(leftPadding)).toInt() else (mTabPadding + textWidth + dp2px(
                    leftPadding
                )).toInt()
            lp.topMargin =
                if (mHeight > 0) (mHeight - textHeight).toInt() / 2 - dp2px(bottomPadding) else 0
            tipView.layoutParams = lp
        }

    }

    /**
     * 当前类只提供了少许设置未读消息属性的方法,可以通过该方法获取MsgView对象从而各种设置
     */
    fun getMsgView(position: Int): MsgView {
        var position = position
        if (position >= tabCount) {
            position = tabCount - 1
        }
        val tabView = mTabsContainer.getChildAt(position)
        return tabView.findViewById<View>(R.id.rtv_msg_tip) as MsgView
    }


    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("instanceState", super.onSaveInstanceState())
        bundle.putInt("mCurrentTab", mCurrentTab)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        var state: Parcelable? = state
        if (state is Bundle) {
            val bundle = state
            mCurrentTab = bundle.getInt("mCurrentTab")
            state = bundle.getParcelable("instanceState")
            if (mCurrentTab != 0 && mTabsContainer.childCount > 0) {
                updateTabSelection(mCurrentTab)
                scrollToCurrentTab()
            }
        }
        super.onRestoreInstanceState(state)
    }

    private fun dp2px(dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    private fun sp2px(sp: Float): Int {
        val scale = context.resources.displayMetrics.scaledDensity
        return (sp * scale + 0.5f).toInt()
    }

    /**
     * ViewPager2适配器
     */
    internal inner class ViewPager2FragmentAdapter(
        fa: FragmentActivity,
        val fragments: MutableList<Fragment>
    ) : FragmentStateAdapter(fa) {

        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]

        }

    }

    /**
     * viewPager2监听实现
     */
    internal inner class ViewPager2PageChangeCallback : ViewPager2.OnPageChangeCallback() {

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            updateTabSelection(position)
            mVP2OnPageSelected.invoke(position)
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)

            /**
             * position:当前View的位置
             * mCurrentPositionOffset:当前View的偏移量比例.[0,1)
             */
            mCurrentTab = position
            mCurrentPositionOffset = positionOffset
            scrollToCurrentTab()
            invalidate()

            mVP2OnPageScrolled.invoke(position, positionOffset, positionOffsetPixels)

        }

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            mVP2OnPageScrollStateChanged.invoke(state)
        }


    }

}