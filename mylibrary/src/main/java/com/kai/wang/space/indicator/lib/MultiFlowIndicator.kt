package com.kai.wang.space.indicator.lib

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.view.NestedScrollingChild
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.OverScroller

/**
 * @author kai.w
 * @des  $des
 */
class MultiFlowIndicator : ViewGroup, NestedScrollingChild {
    private lateinit var mViewPager: ViewPager
    private var mSpaceFlowAdapter: MultiFlowAdapter? = null
    private val mTitles by lazy { mutableListOf<String>() }

    private val mScreenWidth: Int
        get() {
            val displayMetrics = resources.displayMetrics
            val cf = resources.configuration
            val ori = cf.orientation
            return when (ori) {
                Configuration.ORIENTATION_LANDSCAPE -> displayMetrics.heightPixels
                Configuration.ORIENTATION_PORTRAIT -> displayMetrics.widthPixels
                else -> 0
            }
        }
    private val mScreenHeight: Int
        get() {
            val displayMetrics = resources.displayMetrics
            val cf = resources.configuration
            val ori = cf.orientation
            return when (ori) {
                Configuration.ORIENTATION_LANDSCAPE -> displayMetrics.widthPixels
                Configuration.ORIENTATION_PORTRAIT -> displayMetrics.heightPixels
                else -> 0
            }
        }
    private var mTextSelectedColor = Color.RED
    private var mTextUnSelectedColor = Color.BLACK
    private var mTextSelectedSize = resources.getDimension(R.dimen.sp_10)
    private var mTextUnSelectedSize = resources.getDimension(R.dimen.sp_10)
    private var mIndicatorHeight = resources.getDimension(R.dimen.dimen_3)
    private var mIndicatorWidth = resources.getDimension(R.dimen.dimen_8)
    private var mMaxHeight = mScreenHeight.toFloat()
    private var mIndicatorColor = Color.RED
    private val mPaint by lazy {
        Paint().apply {
            this.color = mIndicatorColor
            this.isAntiAlias = true
            this.flags = Paint.ANTI_ALIAS_FLAG
            this.style = Paint.Style.FILL
        }
    }
    private val mOverScroller by lazy { OverScroller(context) }
    private var mMode = MODE.HORIZONL
    private var mCurrentTab = 0
    private var mCurrentTabOffsetPixel = 0
    private var mCurrentTabOffset = 0f

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setWillNotDraw(false)
        obtainAttributes(attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        var measureWidth = 0
        var measureHeight = 0
        when (mMode) {
            MODE.HORIZONL -> {
                for (i in 0 until childCount) {
                    val childView = getChildAt(i)
                    measureChild(childView, widthMeasureSpec, heightMeasureSpec)
                    val layoutParams = childView.layoutParams as MarginLayoutParams
                    measureWidth += childView.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin
                    if (measureHeight < childView.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin) {
                        measureHeight = childView.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin
                    }
                }
            }
            MODE.VERTICAL -> {
                for (i in 0 until childCount) {
                    val childView = getChildAt(i)
                    measureChild(childView, widthMeasureSpec, heightMeasureSpec)
                    val layoutParams = childView.layoutParams as MarginLayoutParams
                    measureWidth += childView.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin
                    if (measureHeight < childView.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin) {
                        measureHeight = childView.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin
                    }
                    if (measureWidth > parentWidth) {
                        measureWidth = childView.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin
                        measureHeight += childView.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin
                    }

                }
            }
        }

        if (measureHeight > mMaxHeight) {
            measureHeight = mMaxHeight.toInt()
        }

        setMeasuredDimension(parentWidth, measureHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var left = l
        var top = t
        var right = 0
        var bottom = 0
        when (mMode) {
            MODE.HORIZONL -> {
                for (i in 0 until childCount) {
                    val childView = getChildAt(i)
                    val layoutParams = childView.layoutParams as MarginLayoutParams
                    top = layoutParams.topMargin
                    right = left + childView.measuredWidth
                    bottom = top + childView.measuredHeight
                    childView.layout(left, top, right, bottom)
                    left = right + layoutParams.rightMargin
                }
            }
            MODE.VERTICAL -> {
                for (i in 0 until childCount) {
                    val childView = getChildAt(i)
                    val layoutParams = childView.layoutParams as MarginLayoutParams
                    right = left + childView.measuredWidth
                    if (right > measuredWidth) {
                        left = l
                        right = left + childView.measuredWidth
                        top += childView.measuredHeight + layoutParams.bottomMargin
                    }
                    bottom = top + childView.measuredHeight
                    childView.layout(left, top + layoutParams.topMargin, right, bottom)
                    left = right + layoutParams.rightMargin
                }
            }
            else -> {

            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (childCount > mCurrentTab + 1) {
            val drawChildView = getChildAt(mCurrentTab)

            canvas?.drawRect(
                drawChildView.left.toFloat(), drawChildView.bottom.toFloat() - mIndicatorHeight
                , drawChildView.right.toFloat(), drawChildView.bottom.toFloat(), mPaint
            )
        }

    }

    var mDownX = 0f
    var mDownY = 0f
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_POINTER_DOWN -> {
            }
            MotionEvent.ACTION_DOWN -> {
                mDownX = event.rawX
                mDownY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                val moveX = event.rawX
                val moveY = event.rawY

                val delX = (mDownX - moveX).toInt()
                val delY = (mDownY - moveY).toInt()

                when {
                    Math.abs(delX) > Math.abs(delY)
                            && (canScrollHorizontally(-1)
                            || canScrollHorizontally(1)) -> {
                        if (scrollX + delX < 0) {
                            mOverScroller.startScroll(scrollX, scrollY, -scrollX, -scrollY)
                        } else {
//                            mOverScroller.startScroll(scrollX, scrollY, delX, 0)
                            scrollBy(delX, 0)
                        }
                    }

                    Math.abs(delX) < Math.abs(delY)
                            && (canScrollVertically(-1)
                            || canScrollVertically(1)) -> {
                        scrollBy(0, delY)
                    }
                    else -> {
                    }
                }

                mDownX = moveX
                mDownY = moveY
            }
            MotionEvent.ACTION_POINTER_UP -> {
            }
            MotionEvent.ACTION_UP -> {
                mDownX = 0f
                mDownY = 0f
            }
            else -> {
            }
        }
        return true
    }

    override fun computeScroll() {
        if (mOverScroller.computeScrollOffset()) {
            scrollTo(mOverScroller.currX, mOverScroller.currY)
            invalidate()
        }
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return if (direction > 0) {//down
            if (childCount > 0) {
                val childView = getChildAt(childCount - 1)
                childView.bottom > measuredHeight
            } else {
                false
            }
        } else {
            if (childCount > 0) {
                scrollY > 0
            } else {
                false
            }
        }
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return if (direction > 0) {//right
            if (childCount > 0) {
                val childView = getChildAt(childCount - 1)
                childView.right > measuredWidth && scrollX <= childView.right
            } else {
                false
            }
        } else {
            if (childCount > 0) {
                val childView = getChildAt(0)
                scrollX > 0 && childView.left == 0
            } else {
                false
            }
        }
    }


    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(MarginLayoutParams.WRAP_CONTENT, MarginLayoutParams.WRAP_CONTENT)
    }


    fun obtainAttributes(attrs: AttributeSet?) {
        attrs?.apply {
            val a = context.obtainStyledAttributes(attrs, R.styleable.MultiIndicator)

            mTextSelectedColor = a.getColor(R.styleable.MultiIndicator_si_text_selected_color, Color.RED)
            mTextUnSelectedColor = a.getColor(R.styleable.MultiIndicator_si_text_unselected_color, Color.BLACK)
            mTextSelectedSize = a.getDimension(
                R.styleable.MultiIndicator_si_text_selected_size,
                resources.getDimension(R.dimen.sp_10)
            )
            mTextUnSelectedSize = a.getDimension(
                R.styleable.MultiIndicator_si_text_unselected_size,
                resources.getDimension(R.dimen.sp_10)
            )
            mIndicatorHeight =
                    a.getDimension(
                        R.styleable.MultiIndicator_si_indicator_height,
                        resources.getDimension(R.dimen.dimen_3)
                    )
            mIndicatorWidth = a.getDimension(
                R.styleable.MultiIndicator_si_indicator_width,
                resources.getDimension(R.dimen.dimen_8)
            )
            mMaxHeight = a.getDimension(R.styleable.MultiIndicator_si_max_height, mScreenHeight.toFloat())
            mIndicatorColor =
                    a.getColor(R.styleable.MultiIndicator_si_indicator_color, Color.RED)

            a.recycle()
        }
    }


    fun setViewPager(viewPager: ViewPager) {
        this.mViewPager = viewPager

        this.mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(p0: Int) {

            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                val scrollChildIndex = if (p0 == mCurrentTab) {//右翻
                    p0 + 1
                } else {
                    p0
                }

                when (mMode) {
                    MODE.HORIZONL -> {
                        if (childCount > scrollChildIndex + 1) {
                            val childView = getChildAt(scrollChildIndex)

                            val centerLeftX = childView.left + childView.measuredWidth.toFloat() / 2 - scrollX
                            if (centerLeftX != measuredWidth.toFloat() / 2 && scrollX >= 0) {

                                var dx = (centerLeftX - measuredWidth.toFloat() / 2).toInt()
                                if (scrollX + dx < 0) {
                                    dx = -scrollX
                                }
                                mOverScroller.startScroll(
                                    scrollX,
                                    scrollY,
                                    dx,
                                    0
                                )
                            }
                        }
                    }
                    MODE.VERTICAL -> {
                        if (childCount > scrollChildIndex + 1) {
                            val childView = getChildAt(scrollChildIndex)

                            val centerTopY = childView.top + childView.measuredHeight.toFloat() - scrollY
                            if (centerTopY != measuredHeight.toFloat() / 2) {
                                mOverScroller.startScroll(
                                    scrollX,
                                    scrollY,
                                    0,
                                    (centerTopY - measuredHeight.toFloat() / 2).toInt()
                                )
                            }
                        }
                    }
                    else -> {
                        changedMode(MODE.HORIZONL)
                    }
                }

                mCurrentTab = p0
                mCurrentTabOffset = p1
                mCurrentTabOffsetPixel = p2

                invalidate()
            }

            override fun onPageSelected(p0: Int) {
            }
        })

        this.mViewPager.adapter?.apply {
            for (i in 0 until this.count) {
                mTitles.add(this.getPageTitle(i).toString())
            }
        }

    }

    fun changedMode(mode: MODE) {
        mOverScroller.startScroll(scrollX, scrollY, -scrollX, -scrollY)
//        mMode = mode
        mMode = if (mMode != MODE.HORIZONL) {
            MODE.HORIZONL
        } else {
            MODE.VERTICAL
        }

        when (mMode) {
            MODE.HORIZONL -> {
//                if (childCount > mCurrentTab) {
//                    val childView = getChildAt(mCurrentTab)
//                    val centerLeftX = childView.left + childView.measuredWidth.toFloat() / 2 - scrollX
//                    mOverScroller.startScroll(scrollX, scrollY, (centerLeftX - measuredWidth.toFloat() / 2).toInt(),  -scrollY)
//                }
            }
            MODE.VERTICAL -> {
//                if (childCount > mCurrentTab) {
//                    val childView = getChildAt(mCurrentTab)
//                    val centerLeftX = childView.left + childView.measuredWidth.toFloat() / 2
//                    mOverScroller.startScroll(scrollX, scrollY, (centerLeftX - measuredWidth.toFloat() / 2).toInt(), 0)
//                }
            }
            else -> {
            }
        }

        requestLayout()
    }

    fun setAdapter(spaceFlowAdapter: MultiFlowAdapter) {
        this.mSpaceFlowAdapter = spaceFlowAdapter
    }


    enum class MODE {
        HORIZONL, VERTICAL
    }

}