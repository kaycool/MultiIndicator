package com.kai.wang.space.indicator.lib

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.os.Build
import android.os.Parcelable
import android.support.v4.view.NestedScrollingChild
import android.support.v4.view.NestedScrollingChildHelper
import android.support.v4.view.NestedScrollingParentHelper
import android.support.v4.widget.ViewDragHelper.INVALID_POINTER
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.OverScroller


/**
 * @author kai.w
 * @des  $des
 */
class MultiFlowLayout : ViewGroup, NestedScrollingChild, OnDataChangedListener {
    private var mMultiFlowAdapter: MultiFlowAdapter? = null
    private val mSelectedView = HashSet<Int>()

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

    /** 用于绘制显示器  */
    private var mPaddingHorizontal = 0
    private var mPaddingVertical = 0
    private var mMaxHeight = mScreenHeight.toFloat()
    private var mMaxSelectedCount = -1
    private var mMaxSelectedTips = ""

    private val mNestedScrollingChildHelper by lazy { NestedScrollingChildHelper(this) }
    private val mNestedScrollingParentHelper by lazy { NestedScrollingParentHelper(this) }
    private var mIsBeingDragged = false
    private var mTouchSlop: Int = 0
    private var mMinimumVelocity: Int = 0
    private var mMaximumVelocity: Int = 0
    private var mOverscrollDistance: Int = 0
    private var mOverflingDistance: Int = 0
    private val mScrollOffset = IntArray(2)
    private val mScrollConsumed = IntArray(2)
    private var mNestedYOffset: Int = 0
    private var mLastX = 0f
    private var mLastY = 0f
    private var mDeltaX = 0f
    private var mDeltaY = 0f

    //    private var mVerticalScrollFactor: Float = 0.toFloat()
    private var mActivePointerId = INVALID_POINTER
    private val mOverScroller by lazy { OverScroller(context) }
    private lateinit var mVelocityTracker: VelocityTracker
    private var mMode = MultiFlowLayout.MODE.HORIZONL
    private var mPreSelectedTab = 0
    private var mCurrentTab = 0
    private var mCurrentTabOffsetPixel = 0
    private var mCurrentTabOffset = 0f

    private var mSelectCallback: SelectCallback? = null

    fun setSelectedCallback(selectCallback: SelectCallback) {
        this.mSelectCallback = selectCallback
    }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setWillNotDraw(false)
        val configuration = ViewConfiguration.get(context)
        mTouchSlop = configuration.scaledTouchSlop
        mMinimumVelocity = configuration.scaledMinimumFlingVelocity
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity
        mOverscrollDistance = configuration.scaledOverscrollDistance
        mOverflingDistance = configuration.scaledOverflingDistance
//        mVerticalScrollFactor = configuration.scaledVerticalScrollFactor

        obtainAttributes(attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)

        var measureWidth = 0
        var measureHeight = 0
        var lineHeight = 0
        when (mMode) {
            MultiFlowLayout.MODE.HORIZONL -> {
                for (i in 0 until childCount) {
                    val childView = getChildAt(i)
                    measureChild(childView, widthMeasureSpec, heightMeasureSpec)
                    val layoutParams = childView.layoutParams as MarginLayoutParams
                    measureWidth += childView.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin + mPaddingHorizontal
                    if (measureHeight < childView.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin) {
                        measureHeight = childView.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin +
                                mPaddingVertical
                    }

                    if (i == childCount - 1) {
                        measureWidth += mPaddingHorizontal
                        measureHeight += mPaddingVertical
                    }
                }
            }
            MultiFlowLayout.MODE.VERTICAL -> {
                for (i in 0 until childCount) {
                    val childView = getChildAt(i)
                    measureChild(
                        childView,
                        MeasureSpec.makeMeasureSpec(
                            MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight - mPaddingHorizontal * 2,
                            MeasureSpec.AT_MOST
                        ),
                        heightMeasureSpec
                    )
                    val layoutParams = childView.layoutParams as MarginLayoutParams
                    val childSpaceWidth =
                        childView.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin + mPaddingHorizontal
                    val childSpaceHeight =
                        childView.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin + mPaddingVertical
                    measureWidth += childSpaceWidth
                    lineHeight = Math.max(lineHeight, childSpaceHeight)

                    if (measureWidth + paddingRight + paddingLeft > parentWidth) {
                        measureWidth = childSpaceWidth
                        measureHeight += lineHeight
                        lineHeight = 0
                    }

                    if (i == childCount - 1) {
                        measureHeight += Math.max(lineHeight, childSpaceHeight) + mPaddingVertical
                    }
                }
            }
        }
        setMeasuredDimension(
            Math.max(measureWidth + paddingLeft + paddingRight, parentWidth)
            , Math.min(measureHeight + paddingTop + paddingBottom, mMaxHeight.toInt())
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var left = 0
        var top = 0
        var right = 0
        var bottom = 0
        var lineHeight = 0
        when (mMode) {
            MultiFlowLayout.MODE.HORIZONL -> {
                for (i in 0 until childCount) {
                    val childView = getChildAt(i)
                    val layoutParams = childView.layoutParams as MarginLayoutParams

                    left += layoutParams.leftMargin + mPaddingHorizontal
                    top = layoutParams.topMargin + mPaddingVertical + paddingTop
                    right = left + childView.measuredWidth
                    bottom = top + childView.measuredHeight
                    childView.layout(left, top, right, bottom)
                    left = right + layoutParams.rightMargin
                }
            }
            MultiFlowLayout.MODE.VERTICAL -> {
                for (i in 0 until childCount) {
                    val childView = getChildAt(i)
                    val layoutParams = childView.layoutParams as MarginLayoutParams

                    left += layoutParams.leftMargin + mPaddingHorizontal
                    if (i == 0) {
                        left += paddingLeft
                    }
                    right = left + childView.measuredWidth
                    if (right + paddingRight > measuredWidth) {
                        left = layoutParams.leftMargin + mPaddingHorizontal + paddingLeft
                        right = left + childView.measuredWidth
                        bottom += lineHeight
                        lineHeight = 0
                    }
                    if (i == 0) {
                        bottom += paddingTop
                    }
                    top = bottom + layoutParams.topMargin + mPaddingVertical

                    childView.layout(left, top, right, top + childView.measuredHeight)
                    left = right + layoutParams.rightMargin
                    lineHeight = Math.max(
                        lineHeight,
                        childView.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin + mPaddingVertical
                    )
                }
            }
            else -> {

            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }


    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        dealMultiTouchEvent(ev)
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                if (mIsBeingDragged) {//回调down事件为己用
                    onTouchEvent(ev)
                    initOrResetVelocityTracker()
                    mVelocityTracker.addMovement(ev)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                // disable move when header not reach top
                val activePointerId = mActivePointerId
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    return mIsBeingDragged
                }

                val pointerIndex = ev.findPointerIndex(activePointerId)
                if (pointerIndex == -1) {
                    return mIsBeingDragged
                }
                if (Math.abs(mDeltaX) > mTouchSlop || Math.abs(mDeltaY) > mTouchSlop) {
                    mIsBeingDragged = true
                    initVelocityTrackerIfNotExists()
                    mVelocityTracker.addMovement(ev)
                    mNestedYOffset = 0
                    val parent = parent
                    parent?.requestDisallowInterceptTouchEvent(true)

                    ev.action = MotionEvent.ACTION_CANCEL
                    val obtain = MotionEvent.obtain(ev)
                    obtain.action = MotionEvent.ACTION_DOWN
                    dispatchTouchEvent(ev)
                    return dispatchTouchEvent(obtain)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mIsBeingDragged = false
            }

            else -> {
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (scrollY == 0 && !canScrollVertically(1)
            && scrollX == 0 && !canScrollHorizontally(1)
        ) {
            mIsBeingDragged = false
        }
        return mIsBeingDragged && isEnabled
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        initVelocityTrackerIfNotExists()
        mActivePointerId = event.getPointerId(0)
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mLastX = event.x
                mLastY = event.y

                mNestedYOffset = 0
                event.offsetLocation(0f, mNestedYOffset.toFloat())

                mActivePointerId = event.getPointerId(0)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startNestedScroll(View.SCROLL_AXIS_VERTICAL)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val activePointerId = mActivePointerId
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    return false
                }

                val pointerIndex = event.findPointerIndex(activePointerId)
                if (pointerIndex == -1) {
                    return false
                }

                val moveX = event.getX(pointerIndex)
                val moveY = event.getY(pointerIndex)

                var delX = (mLastX - moveX).toInt()
                var delY = (mLastY - moveY).toInt()


                if (dispatchNestedPreScroll(delX, delY, mScrollConsumed, mScrollOffset)) {
                    delX -= mScrollConsumed[0]
                    delY -= mScrollConsumed[1]
                    event.offsetLocation(0f, mScrollOffset[1].toFloat())
                    mNestedYOffset += mScrollOffset[1]
                }
                mLastX = moveX - mScrollOffset[0]
                mLastY = moveY - mScrollOffset[1]

                val oldY = scrollY
                if (overScrollBy(
                        delX,
                        delY,
                        scrollX,
                        scrollY,
                        getScrollRangeX(),
                        getScrollRangeY(),
                        mOverscrollDistance,
                        mOverscrollDistance,
                        true
                    ) && !hasNestedScrollingParent()
                ) {
                    // Break our velocity if we hit a scroll barrier.
                    mVelocityTracker.clear()
                }

                val scrolledDeltaY = scrollY - oldY
                val unconsumedY = delY - scrolledDeltaY
                if (dispatchNestedScroll(0, scrolledDeltaY, 0, unconsumedY, mScrollOffset)) run {
                    mLastX -= mScrollOffset[0]
                    mLastY -= mScrollOffset[1]
                    event.offsetLocation(0f, mScrollOffset[1].toFloat())
                    mNestedYOffset += mScrollOffset[1]
                } else {
                    when {
                        Math.abs(delX) > Math.abs(delY)
                                && (canScrollHorizontally(-1)
                                || canScrollHorizontally(1)) -> {
                            val dx = when {
                                scrollX + delX < 0 -> -scrollX
                                scrollX + delX > getScrollRangeX() -> getScrollRangeX() - scrollX
                                else -> delX
                            }
                            mOverScroller.startScroll(
                                scrollX,
                                0,
                                dx,
                                0
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                postInvalidateOnAnimation()
                            } else {
                                postInvalidate()
                            }
                        }

                        Math.abs(delY) > Math.abs(delX)
                                && (canScrollVertically(-1)
                                || canScrollVertically(1)) -> {

                            val dy = when {
                                scrollY + delY < 0 -> -scrollY
                                scrollY + delY > getScrollRangeY() -> getScrollRangeY() - scrollY
                                else -> delY
                            }

                            mOverScroller.startScroll(
                                0,
                                scrollY,
                                0,
                                dy
                            )

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                postInvalidateOnAnimation()
                            } else {
                                postInvalidate()
                            }
                        }
                        else -> {
                        }
                    }
                }

                mLastX = moveX
                mLastY = moveY
            }
            MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                Log.d(TAG, "MotionEvent.ACTION_UP or MotionEvent.ACTION_CANCEL")
                mLastX = 0f
                mLastY = 0f

                val velocityTracker = mVelocityTracker
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                val velocityX = -velocityTracker.getXVelocity(mActivePointerId).toInt()
                val velocityY = -velocityTracker.getYVelocity(mActivePointerId).toInt()

                when {
                    Math.abs(velocityX) > Math.abs(velocityY) && Math.abs(velocityX) > mMinimumVelocity -> {
                        val canFling = (scrollX > 0 || velocityX > 0) && (scrollX < getScrollRangeX()
                                || velocityX < 0)
                        if (!dispatchNestedPreFling(velocityX.toFloat(), 0f)) {
                            dispatchNestedFling(velocityX.toFloat(), 0f, canFling)
                            if (canFling) {
                                mOverScroller.fling(
                                    scrollX, scrollY, velocityX, 0, 0, Math.max(0, getScrollRangeX()), 0,
                                    0, mScreenWidth / 3, 0
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    postInvalidateOnAnimation()
                                } else {
                                    postInvalidate()
                                }
                            }
                        }
                    }
                    Math.abs(velocityX) < Math.abs(velocityY) && Math.abs(velocityY) > mMinimumVelocity -> {
                        val canFling = (scrollY > 0 || velocityY > 0) && (scrollY < getScrollRangeY() || velocityY < 0)
                        if (!dispatchNestedPreFling(0f, velocityY.toFloat())) {
                            dispatchNestedFling(0f, velocityY.toFloat(), canFling)
                            if (canFling) {
                                mOverScroller.fling(
                                    scrollX, scrollY, 0, velocityY, 0, 0, 0,
                                    Math.max(0, getScrollRangeY()), 0, measuredHeight / 3
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    postInvalidateOnAnimation()
                                } else {
                                    postInvalidate()
                                }
                            }
                        }
                    }
                    else -> {
                    }
                }
                recycleVelocityTracker()
                stopNestedScroll()
            }
            else -> {
            }
        }

        mVelocityTracker.addMovement(event)
        return true
    }


    //多手势触发
    private fun dealMultiTouchEvent(event: MotionEvent) {
        val actionMasked = event.actionMasked
        val pointerIndex = event.actionIndex
        if (pointerIndex < 0) {
            return
        }

        when (actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mLastX = event.getX(pointerIndex)
                mLastY = event.getY(pointerIndex)
                mDeltaX = 0f
                mDeltaY = 0f
                mActivePointerId = event.getPointerId(0)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val pointerId = event.getPointerId(pointerIndex)
                if (pointerId != mActivePointerId) {
                    mLastX = event.getX(pointerIndex)
                    mLastY = event.getY(pointerIndex)
                    mDeltaX = 0f
                    mDeltaY = 0f
                    mActivePointerId = event.getPointerId(pointerIndex)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                val pointerIndex1 = event.findPointerIndex(mActivePointerId)
                val moveX = event.getX(pointerIndex1)
                val moveY = event.getY(pointerIndex1)
                mDeltaX = moveX - mLastX
                mDeltaY = moveY - mLastY
                mLastX = moveX
                mLastY = moveY
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val pointerId = event.getPointerId(pointerIndex)
                if (mActivePointerId == pointerId) {
                    val newPointerIndex = if (pointerIndex == 0) {
                        1
                    } else {
                        0
                    }
                    mLastX = event.getX(newPointerIndex)
                    mLastY = event.getY(newPointerIndex)
                    mActivePointerId = event.getPointerId(newPointerIndex)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mActivePointerId = MotionEvent.INVALID_POINTER_ID
            }

            else -> {
            }
        }
    }

    override fun computeScroll() {
        if (mOverScroller.computeScrollOffset()) {
//            Log.d(TAG, "computeScroll")

            val oldX = scrollX
            val oldY = scrollY
            val x = mOverScroller.currX
            val y = mOverScroller.currY

            if (oldX != x || oldY != y) {
                overScrollBy(
                    x - oldX, y - oldY, oldX, oldY, getScrollRangeX(), getScrollRangeY(),
                    mOverflingDistance, mOverflingDistance, false
                )
            }

            scrollTo(mOverScroller.currX, mOverScroller.currY)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                postInvalidateOnAnimation()
            } else {
                postInvalidate()
            }
        }
    }

    override fun canScrollVertically(direction: Int): Boolean {
        return if (direction > 0) {//down
            if (childCount > 0) {
                val childView = getChildAt(childCount - 1)
                scrollY < childView.bottom - measuredHeight
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
                scrollX < measuredWidth - mScreenWidth
            } else {
                false
            }
        } else {
            if (childCount > 0) {
                scrollX > 0
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun onSaveInstanceState(): Parcelable? {
        //todo save Bundle
        return super.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        //todo Restore Bundle
        super.onRestoreInstanceState(state)
    }


    fun obtainAttributes(attrs: AttributeSet?) {
        attrs?.apply {
            val a = context.obtainStyledAttributes(attrs, R.styleable.MultiFlowLayout)
            mPaddingHorizontal = a.getDimensionPixelOffset(
                R.styleable.MultiFlowLayout_multi_flow_padding_horizontal,
                context.resources.getDimensionPixelOffset(R.dimen.dimen_5)
            )
            mPaddingVertical = a.getDimensionPixelOffset(
                R.styleable.MultiFlowLayout_multi_flow_padding_vertical,
                context.resources.getDimensionPixelOffset(R.dimen.dimen_5)
            )
            mMaxHeight = a.getDimension(R.styleable.MultiFlowLayout_multi_flow_max_height, mScreenHeight.toFloat())
            mMaxSelectedCount = a.getInt(R.styleable.MultiFlowLayout_multi_max_selected_count, -1)
            mMaxSelectedTips = a.getString(R.styleable.MultiFlowLayout_multi_max_selected_Tips) ?: ""
            a.recycle()
        }
    }


    //调用此方法滚动到目标位置
    fun smoothScrollTo(fx: Int, fy: Int) {
        val dx = fx - mOverScroller.finalX
        val dy = fy - mOverScroller.finalY
        smoothScrollBy(dx, dy)
    }

    //调用此方法设置滚动的相对偏移
    fun smoothScrollBy(dx: Int, dy: Int) {
        //设置mScroller的滚动偏移量
        mOverScroller.startScroll(mOverScroller.finalX, mOverScroller.finalY, dx, dy)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            postInvalidateOnAnimation()
        } else {
            postInvalidate()
        }
    }

    fun getScrollRangeY(): Int {
        val childView = if (childCount > 0) {
            getChildAt(childCount - 1)
        } else {
            getChildAt(0)
        }
        return childView.bottom - measuredHeight
    }

    fun getScrollRangeX(): Int {
        return measuredWidth - mScreenWidth
    }

    private fun initOrResetVelocityTracker() {
        if (!this::mVelocityTracker.isInitialized) {
            mVelocityTracker = VelocityTracker.obtain()
        } else {
            mVelocityTracker.clear()
        }
    }

    private fun initVelocityTrackerIfNotExists() {
        if (!this::mVelocityTracker.isInitialized) {
            mVelocityTracker = VelocityTracker.obtain()
        }
    }

    private fun recycleVelocityTracker() {
        if (!this::mVelocityTracker.isInitialized) {
            mVelocityTracker.recycle()
        }
    }

    fun changedMode() {
        mOverScroller.startScroll(scrollX, scrollY, -scrollX, -scrollY)
        mMode = if (mMode != MultiFlowLayout.MODE.HORIZONL) {
            MultiFlowLayout.MODE.HORIZONL
        } else {
            MultiFlowLayout.MODE.VERTICAL
        }

        requestLayout()

        post {
            when (mMode) {
                MultiFlowLayout.MODE.HORIZONL -> {
                    if (childCount > mCurrentTab) {
                        val childView = getChildAt(mCurrentTab)
                        val centerLeftX = childView.left + childView.measuredWidth.toFloat() / 2
                        var dx = (centerLeftX - mScreenWidth.toFloat() / 2).toInt()
                        dx = when {
                            scrollX + dx < 0 -> -scrollX
                            scrollX + dx > getScrollRangeX() -> getScrollRangeX() - scrollX
                            else -> dx - scrollX
                        }
                        mOverScroller.startScroll(
                            scrollX,
                            scrollY,
                            dx,
                            -scrollY
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            postInvalidateOnAnimation()
                        } else {
                            postInvalidate()
                        }
                    }
                }
                MultiFlowLayout.MODE.VERTICAL -> {
                    if (childCount > mCurrentTab) {
                        val childView = getChildAt(mCurrentTab)
                        val centerTopY = childView.top + childView.measuredHeight.toFloat() / 2
                        var dy = (centerTopY - measuredHeight.toFloat() / 2).toInt()
                        dy = when {
                            scrollY + dy < 0 -> -scrollY
                            scrollY + dy > getScrollRangeY() -> getScrollRangeY() - scrollY
                            else -> dy - scrollY
                        }
                        mOverScroller.startScroll(
                            scrollX,
                            scrollY,
                            -scrollX,
                            dy
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            postInvalidateOnAnimation()
                        } else {
                            postInvalidate()
                        }
                    }
                }
                else -> {
                }
            }
        }
    }

    fun setAdapter(multiFlowAdapter: MultiFlowAdapter) {
        this.mMultiFlowAdapter = multiFlowAdapter
        this.mMultiFlowAdapter?.setOnDataChangedListener(this)
        changeAdapter()
    }

    private fun changeAdapter() {
        this.mMultiFlowAdapter?.apply {
            removeAllViews()
            for (index in 0 until this.getItemCount()) {
                val view = this.getView(this@MultiFlowLayout, index, this.getItem(index))
                view.layoutParams = generateDefaultLayoutParams()
                addView(view)

                if (mSelectedView.contains(index)) {
                    this.onSelected(view, index)
                }

                view.setOnClickListener {
                    var hasMaxLimit = false
                    if (mSelectedView.contains(index)) {
                        mSelectedView.remove(index)
                        this.unSelected(view, index)
                    } else {
                        hasMaxLimit = mMaxSelectedCount > 0 && mSelectedView.size <= mMaxSelectedCount
                        if (hasMaxLimit) {
                            mSelectedView.add(index)
                            this.onSelected(view, index)
                        }
                    }
//                    mSelectCallback?.callback(hasMaxLimit, index)
                }
            }
        }
    }

    fun setMaxSelectCount(maxCount: Int) {
        if (mSelectedView.size > maxCount) {
            Log.w(TAG, "you has already select more than $maxCount views , so it will be clear .")
            mSelectedView.clear()
        }
        mMaxSelectedCount = maxCount
    }

    fun getSelectedList(): Set<Int> {
        return HashSet(mSelectedView)
    }

    override fun onChanged() {
        changeAdapter()
    }

    enum class MODE {
        HORIZONL, VERTICAL
    }

    companion object {
        val TAG = "MultiFlowLayout"


        interface SelectCallback {
            fun callback(hasMaxLimit: Boolean)
        }
    }
}