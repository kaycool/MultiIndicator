package com.kai.wang.space.indicator.lib

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.view.ViewPager
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.widget.TextView

/**
 * @author kai.w
 * @des  $des
 */
class MultiIndicator : RecyclerView {
    private lateinit var mViewPager: ViewPager
    private val mTitles by lazy { mutableListOf<String>() }
    private var mTextSelectedColor = Color.RED
    private var mTextUnSelectedColor = Color.BLACK
    private var mTextSelectedSize = resources.getDimension(R.dimen.sp_10)
    private var mTextUnSelectedSize = resources.getDimension(R.dimen.sp_10)
    private var mIndicatorHeight = resources.getDimension(R.dimen.dimen_3)
    private var mIndicatorWidth = resources.getDimension(R.dimen.dimen_8)
    private var mIndicatorColor = Color.RED
    private val mPaint by lazy {
        Paint().apply {
            this.color = mIndicatorColor
            this.isAntiAlias = true
            this.flags = Paint.ANTI_ALIAS_FLAG
            this.style = Paint.Style.FILL
        }
    }
    private var mCurrentTab = 0
    private var mCurrentTabOffsetPixel = 0
    private var mCurrentTabOffset = 0f

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        setWillNotDraw(false)
        obtainAttributes(attrs)
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
    }

    override fun onDraw(c: Canvas?) {
        super.onDraw(c)
    }

    fun obtainAttributes(attrs: AttributeSet?) {
        attrs?.apply {
            val a = context.obtainStyledAttributes(attrs, R.styleable.MultiIndicator)

            mTextSelectedColor = a.getColor(R.styleable.MultiIndicator_multi_text_selected_color, Color.RED)
            mTextUnSelectedColor = a.getColor(R.styleable.MultiIndicator_multi_text_unselected_color, Color.BLACK)
            mTextSelectedSize = a.getDimension(
                R.styleable.MultiIndicator_multi_text_selected_size,
                resources.getDimension(R.dimen.sp_10)
            )
            mTextUnSelectedSize = a.getDimension(
                R.styleable.MultiIndicator_multi_text_unselected_size,
                resources.getDimension(R.dimen.sp_10)
            )
            mIndicatorHeight =
                    a.getDimension(
                        R.styleable.MultiIndicator_multi_indicator_height,
                        resources.getDimension(R.dimen.dimen_3)
                    )
            mIndicatorWidth =
                    a.getDimension(
                        R.styleable.MultiIndicator_multi_indicator_width,
                        resources.getDimension(R.dimen.dimen_8)
                    )
            mIndicatorColor =
                    a.getColor(R.styleable.MultiIndicator_multi_indicator_color, Color.RED)

            a.recycle()
        }
    }


    fun setViewPager(viewPager: ViewPager) {
        this.mViewPager = viewPager

        this.mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(p0: Int) {

            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

                (layoutManager as? LinearLayoutManager?)?.let {
                    val scrollChildIndex = if (p0 == mCurrentTab) {//右翻
                        p0 + 1
                    } else {
                        p0
                    }
                    it.findViewByPosition(scrollChildIndex)?.apply {

                        val parentCenterX = this@MultiIndicator.measuredWidth.toFloat() / 2
                        val centerLeftX = this.left + this.measuredWidth.toFloat() / 2

                        val scrollByXOffset = (centerLeftX - parentCenterX).toInt()

//                        val scrollByOffset = if (p0 == mCurrentTab) {//右翻
//                            (center - parentCenterX).toInt()
//                        } else {
//                            (parentCenterX - center).toInt()
//                        }

                        if (centerLeftX != parentCenterX) {
                            this@MultiIndicator.smoothScrollBy(scrollByXOffset, 0)
                        }
                    }
                }

                mCurrentTab = p0
                mCurrentTabOffset = p1
                mCurrentTabOffsetPixel = p2

                invalidate()
            }

            override fun onPageSelected(p0: Int) {
                scrollToPosition(p0)

                (layoutManager as? LinearLayoutManager?)?.apply {
                    for (i in 0 until this.childCount) {
                        val itemView = this.findViewByPosition(i)
                        if (itemView is TextView) {

                            if (i == p0) {
                                itemView.setTextColor(Color.RED)
                            } else {
                                itemView.setTextColor(Color.BLACK)
                            }
                        }
                    }
                }
            }
        })

        this.mViewPager.adapter?.apply {
            for (i in 0 until this.count) {
                mTitles.add(this.getPageTitle(i).toString())
            }
        }

        if (mTitles.isNotEmpty()) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = MultiIndicatorAdapter(context, mTitles)
        }
    }


    fun toVerticalTree() {
        layoutManager = GridLayoutManager(context, 4)
        adapter = MultiIndicatorAdapter(context, mTitles)
    }
}