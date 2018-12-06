package com.kai.wang.space.indicator.lib

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.view.*
import android.support.v4.widget.ViewDragHelper.INVALID_POINTER
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.OverScroller


/**
 * @author kai.w
 * @des  $des
 */
class MultiFlowIndicator : ViewGroup, NestedScrollingParent2, NestedScrollingChild2, OnDataChangedListener {
    private lateinit var mViewPager: ViewPager
    private var mMultiFlowAdapter: MultiFlowAdapter<Any>? = null

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
    private var mMeasureWidth = 0
    private var mMeasureHeight = 0

    /** 用于绘制显示器  */
    private var mPaddingHorizontal = 0
    private var mPaddingVertical = 0
    private val mIndicatorRect = Rect()
    private val mIndicatorDrawable = GradientDrawable()
    private var mTextSelectedColor = Color.RED
    private var mIconSelectedColor = Color.RED
    private var mTextUnSelectedColor = Color.BLACK
    private var mIconUnSelectedColor = Color.BLACK
    private var mTextSelectedSize = resources.getDimension(R.dimen.sp_10)
    private var mTextUnSelectedSize = resources.getDimension(R.dimen.sp_10)
    private var mIndicatorHeight = resources.getDimension(R.dimen.dimen_3)
    private var mIndicatorWidth = resources.getDimension(R.dimen.dimen_8)
    private var mIndicatorEqualsTitle = false
    private var mIndicatorStyle = STYLE_NORMAL
    private var mIndicatorStyleRadius = 0f
    private var mMaxHeight = -1f
    private var mMaxLines = -1f
    private var mIndicatorColor = Color.RED
    private val mPaint by lazy {
        Paint().apply {
            this.color = mIndicatorColor
            this.isAntiAlias = true
            this.flags = Paint.ANTI_ALIAS_FLAG
            this.style = Paint.Style.FILL
        }
    }
    private val mNestedScrollingChildHelper by lazy { NestedScrollingChildHelper(this) }
    private val mNestedScrollingParentHelper by lazy { NestedScrollingParentHelper(this) }
    private var mIsBeingDragged = false
    private var mIsNeedIntercept: Boolean = false
    private var mTouchSlop: Int = 0
    private var mMinimumVelocity: Int = 0
    private var mMaximumVelocity: Int = 0
    private var mOverscrollDistance: Int = 0
    private var mOverflingDistance: Int = 0
    private val mScrollOffset = IntArray(2)
    private val mScrollConsumed = IntArray(2)
    private var mNestedXOffset: Int = 0
    private var mNestedYOffset: Int = 0
    private var mLastScrollerX: Int = 0
    private var mLastScrollerY: Int = 0
    private var mLastX = 0f
    private var mLastY = 0f
    private var mLastMotionX = 0f
    private var mLastMotionY = 0f
    private var mDeltaX = 0f
    private var mDeltaY = 0f

    //    private var mVerticalScrollFactor: Float = 0.toFloat()
    private var mActivePointerId = INVALID_POINTER
    private val mOverScroller by lazy { OverScroller(context) }
    private lateinit var mVelocityTracker: VelocityTracker
    private var mMode = MultiFlowIndicator.MODE.INVALID
    private var mPreSelectedTab = 0
    private var mCurrentTab = 0
    private var mCurrentTabOffsetPixel = 0
    private var mCurrentTabOffset = 0f

    private var mItemClickCallback: ItemClickCallback? = null
    private var mOnLayoutChanged: OnLayoutChanged? = null

    fun setItemClickCallback(itemClickCallback: ItemClickCallback) {
        this.mItemClickCallback = itemClickCallback
    }

    fun setOnLayoutChanged(onLayoutChanged: OnLayoutChanged) {
        this.mOnLayoutChanged = onLayoutChanged
    }

    fun getMode() = mMode.name

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setWillNotDraw(false)
        isNestedScrollingEnabled = true
        val configuration = ViewConfiguration.get(context)
        mTouchSlop = configuration.scaledTouchSlop
        mMinimumVelocity = configuration.scaledMinimumFlingVelocity
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity
        mOverscrollDistance = configuration.scaledOverscrollDistance
        mOverflingDistance = configuration.scaledOverflingDistance

        obtainAttributes(attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)

        mMeasureWidth = 0
        mMeasureHeight = 0
        var lineHeight = 0
        var lines = 0
        var mLinesMaxHeight = 0
        when (mMode) {
            MultiFlowLayout.MODE.HORIZONL -> {
                for (i in 0 until childCount) {
                    val childView = getChildAt(i)
                    measureChild(
                        childView,
                        MeasureSpec.makeMeasureSpec(parentWidth - mPaddingHorizontal * 2, MeasureSpec.UNSPECIFIED),
                        heightMeasureSpec
                    )
                    val layoutParams = childView.layoutParams as MarginLayoutParams
                    mMeasureWidth += childView.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin + mPaddingHorizontal
                    if (mMeasureHeight < childView.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin) {
                        mMeasureHeight = childView.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin +
                                mPaddingVertical
                    }

                    if (i == childCount - 1) {
                        mMeasureWidth += mPaddingHorizontal
                        mMeasureHeight += mPaddingVertical
                    }
                }
            }
            MultiFlowLayout.MODE.VERTICAL -> {
                for (i in 0 until childCount) {
                    val childView = getChildAt(i)
                    measureChild(
                        childView,
                        MeasureSpec.makeMeasureSpec(parentWidth - mPaddingHorizontal * 2, MeasureSpec.AT_MOST),
                        heightMeasureSpec
                    )
                    val layoutParams = childView.layoutParams as MarginLayoutParams
                    val childSpaceWidth =
                        childView.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin + mPaddingHorizontal
                    val childSpaceHeight =
                        childView.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin + mPaddingVertical
                    mMeasureWidth += childSpaceWidth
                    if (mMeasureWidth + paddingRight + paddingLeft > parentWidth) {
                        mMeasureWidth = childSpaceWidth
                        mMeasureHeight += lineHeight + childSpaceHeight
                        if (lines < mMaxLines) {
                            mLinesMaxHeight += if (mMaxLines - lines < 1) {
                                (lineHeight * (mMaxLines - lines)).toInt()
                            } else {
                                lineHeight
                            }
                        }
                        lineHeight = 0
                        lines++
                    }
                    lineHeight = Math.max(lineHeight, childSpaceHeight)

                    if (i == childCount - 1) {
                        if (lines < mMaxLines) {
                            mLinesMaxHeight += Math.max(lineHeight, childSpaceHeight) + mPaddingVertical
                        }
                        mMeasureHeight += Math.max(lineHeight, childSpaceHeight) + mPaddingVertical
                    }
                }
            }
            else -> {
            }
        }

        setMeasuredDimension(
            parentWidth, when {
                mMaxHeight > 0 -> Math.min(mMeasureHeight + paddingTop + paddingBottom, mMaxHeight.toInt())
                mLinesMaxHeight > 0 -> mLinesMaxHeight + paddingTop + paddingBottom + mPaddingVertical
                else -> mMeasureHeight + paddingTop + paddingBottom
            }
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var left = 0
        var top = 0
        var right = 0
        var bottom = 0
        var lineHeight = 0
        when (mMode) {
            MultiFlowIndicator.MODE.HORIZONL -> {
                for (i in 0 until childCount) {
                    val childView = getChildAt(i)
                    val layoutParams = childView.layoutParams as MarginLayoutParams
                    left += layoutParams.leftMargin
                    if (i == 0) {
                        left += paddingLeft
                    }
                    top = layoutParams.topMargin + paddingTop
                    right = left + childView.measuredWidth
                    bottom = top + childView.measuredHeight
                    childView.layout(left, top, right, bottom)
                    left = right + layoutParams.rightMargin
                }
            }
            MultiFlowIndicator.MODE.VERTICAL -> {
                for (i in 0 until childCount) {
                    val childView = getChildAt(i)
                    val layoutParams = childView.layoutParams as MarginLayoutParams

                    left += layoutParams.leftMargin
                    if (i == 0) {
                        left += paddingLeft
                    }
                    right = left + childView.measuredWidth
                    if (right + paddingRight > measuredWidth) {
                        left = layoutParams.leftMargin + paddingLeft
                        right = left + childView.measuredWidth
                        bottom += lineHeight
                        lineHeight = 0
                    }
                    if (i == 0) {
                        bottom += paddingTop
                    }
                    top = bottom + layoutParams.topMargin

                    childView.layout(left, top, right, top + childView.measuredHeight)
                    left = right + layoutParams.rightMargin
                    lineHeight = Math.max(
                        lineHeight,
                        childView.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin
                    )
                }
            }
            else -> {

            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        calcIndicatorRect()
        when (mIndicatorStyle) {
            STYLE_NORMAL -> {
                canvas.drawRect(mIndicatorRect, mPaint)
            }

            STYLE_RECTANGLE -> {
                mIndicatorDrawable.setColor(mIndicatorColor)
                mIndicatorDrawable.bounds = mIndicatorRect
                mIndicatorDrawable.cornerRadius = mIndicatorStyleRadius
                mIndicatorDrawable.draw(canvas)
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        dealMultiTouchEvent(ev)
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                when {
                    getScrollRangeX() > 0 -> {
                        val isOnLeft = !canScrollHorizontally(-1)
                        val isOnRight = !canScrollHorizontally(1)
                        if (isOnLeft || isOnRight) {
                            mIsNeedIntercept = false
                        }
                    }

                    getScrollRangeY() > 0 -> {
                        val isOnTop = !canScrollVertically(-1)
                        val isOnBottom = !canScrollVertically(1)

                        if (isOnTop || isOnBottom) {
                            mIsNeedIntercept = false
                        }
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
//                mIsNeedIntercept = isNeedIntercept()
//                Log.d(TAG, " mIsNeedIntercept=$mIsNeedIntercept , mDeltaX=$mDeltaX , mDeltaY=$mDeltaY")
//                if (mIsNeedIntercept && !mIsBeingDragged) {
//                    mIsBeingDragged = true

//                    val obtain = MotionEvent.obtain(ev)
//                    obtain.action = MotionEvent.ACTION_DOWN
//                    dispatchTouchEvent(obtain)
//                }

                if (Math.abs(mDeltaX) > mTouchSlop || Math.abs(mDeltaY) > mTouchSlop) {
                    mIsNeedIntercept = true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                Log.d(TAG, "dispatchTouchEvent ===== MotionEvent.action = ACTION_UP,ACTION_CANCEL")
                mIsNeedIntercept = false
            }

            MotionEvent.ACTION_POINTER_UP -> {
            }

            else -> {
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return mIsNeedIntercept
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val pointerIndex = event.actionIndex
        if (pointerIndex < 0) {
            recycleVelocityTracker()
            return false
        }
        initVelocityTrackerIfNotExists()
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "onTouchEvent ===== MotionEvent.action = ACTION_DOWN")
                mLastMotionX = event.getX(pointerIndex)
                mLastMotionY = event.getY(pointerIndex)

//                if (mIsBeingDragged) {//回调down事件为己用
//                    initOrResetVelocityTracker()
//                    mVelocityTracker.addMovement(event)
//                }

                initVelocityTracker()
                mNestedXOffset = 0
                mNestedYOffset = 0
                event.offsetLocation(mNestedXOffset.toFloat(), mNestedYOffset.toFloat())

                mActivePointerId = event.getPointerId(0)

                this.startNestedScroll(2, 0)
            }
            MotionEvent.ACTION_MOVE -> {
                mNestedXOffset = 0
                mNestedYOffset = 0

                val moveX = event.getX(pointerIndex)
                val moveY = event.getY(pointerIndex)

                var delX = (mLastMotionX - moveX).toInt()
                var delY = (mLastMotionY - moveY).toInt()

                if (dispatchNestedPreScroll(delX, delY, mScrollConsumed, mScrollOffset)) {
                    Log.d(
                        TAG,
                        "dispatchNestedPreScroll ,mScrollConsumedX=${mScrollConsumed[0]},mScrollConsumedY=${mScrollConsumed[1]}"
                    )
                    delX -= mScrollConsumed[0]
                    delY -= mScrollConsumed[1]
                    event.offsetLocation(mScrollOffset[0].toFloat(), mScrollOffset[1].toFloat())
                    mNestedXOffset += mScrollOffset[0]
                    mNestedYOffset += mScrollOffset[1]
                }

                if (Math.abs(delX) > mTouchSlop || Math.abs(delY) > mTouchSlop) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                    val oldX = scrollX
                    val oldY = scrollY

                    if (Math.abs(delX) < mTouchSlop) {
                        delX = 0
                    }

                    if (Math.abs(delY) < mTouchSlop) {
                        delY = 0
                    }

                    if (this.overScrollByCompat(
                            delX, delY, scrollX, scrollY, mMeasureWidth,
                            mMeasureHeight, 0, 0, true
                        )
                        && !this.hasNestedScrollingParent(0)
                    ) {
                        this.mVelocityTracker.clear()
                    }

                    val scrolledDeltaX = scrollX - oldX
                    val scrolledDeltaY = scrollY - oldY
                    val unconsumedX = delX - scrolledDeltaX
                    val unconsumedY = delY - scrolledDeltaY
                    if (dispatchNestedScroll(scrolledDeltaX, scrolledDeltaY, unconsumedX, unconsumedY, mScrollOffset)) {
                        mLastMotionX -= mScrollOffset[0]
                        mLastMotionY -= mScrollOffset[1]
                        Log.d(
                            TAG,
                            "dispatchNestedScroll ,mScrollConsumedX=${mScrollConsumed[0]},mScrollConsumedY=${mScrollConsumed[1]}"
                        )
                        event.offsetLocation(0f, mScrollOffset[1].toFloat())
                        mNestedXOffset += mScrollOffset[0]
                        mNestedYOffset += mScrollOffset[1]
                    }
                    Log.d(TAG, "onTouchEvent ===== MotionEvent.action = ACTION_MOVE ,delX=$delX , delY = $delY")

                    mLastMotionX = moveX - mScrollOffset[0]
                    mLastMotionY = moveY - mScrollOffset[1]
                }

            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                Log.d(TAG, "onTouchEvent ===== MotionEvent.action = ACTION_UP,ACTION_CANCEL")
                initVelocityTrackerIfNotExists()
                val velocityTracker = mVelocityTracker
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                val velocityX = -velocityTracker.getXVelocity(mActivePointerId).toInt()
                val velocityY = -velocityTracker.getYVelocity(mActivePointerId).toInt()

                if (Math.abs(velocityX) > this.mMinimumVelocity || Math.abs(velocityY) > this.mMinimumVelocity) {
                    this.flingWithNestedDispatch(velocityX, velocityY)
                } else if (mOverScroller.springBack(
                        this.scrollX,
                        this.scrollY,
                        0,
                        getScrollRangeX(),
                        0,
                        getScrollRangeY()
                    )
                ) {
                    ViewCompat.postInvalidateOnAnimation(this)
                }
//                when {
//                    getScrollRangeX() > 0 && Math.abs(velocityX) > mMinimumVelocity -> {
//                        val canFling = (scrollX > 0 || velocityX > 0) && (scrollX < getScrollRangeX()
//                                || velocityX < 0)
//                        if (!dispatchNestedPreFling(velocityX.toFloat(), 0f)) {
//                            dispatchNestedFling(velocityX.toFloat(), 0f, canFling)
//                            if (canFling) {
//                                mOverScroller.fling(
//                                    scrollX, scrollY, velocityX, 0, 0, Math.max(0, getScrollRangeX()), 0,
//                                    0, measuredHeight / 3, 0
//                                )
//                                ViewCompat.postInvalidateOnAnimation(this)
//                            }
//                        }
//                    }
//                    getScrollRangeY() > 0 && Math.abs(velocityY) > mMinimumVelocity -> {
//                        val canFling = (scrollY > 0 || velocityY > 0) && (scrollY < getScrollRangeY() || velocityY < 0)
//                        if (!dispatchNestedPreFling(0f, velocityY.toFloat())) {
//                            dispatchNestedFling(0f, velocityY.toFloat(), canFling)
//                            if (canFling) {
//                                mOverScroller.fling(
//                                    scrollX, scrollY, 0, velocityY, 0, 0, 0,
//                                    Math.max(0, getScrollRangeY()), 0, measuredHeight / 3
//                                )
//                                ViewCompat.postInvalidateOnAnimation(this)
//                            }
//                        }
//                    }
//                    mOverScroller.springBack(
//                        this.scrollX,
//                        this.scrollY,
//                        0,
//                        getScrollRangeX(),
//                        0,
//                        getScrollRangeY()
//                    ) -> {
//                        ViewCompat.postInvalidateOnAnimation(this)
//                    }
//                }
                recycleVelocityTracker()
                stopNestedScroll()
                mLastMotionX = 0f
                mLastMotionY = 0f
                mNestedXOffset = 0
                mNestedYOffset = 0
                mIsBeingDragged = false
                mActivePointerId = INVALID_POINTER
            }
            else -> {
            }
        }

        mVelocityTracker.addMovement(event)
        return true
    }

    private fun overScrollByCompat(
        deltaX: Int,
        deltaY: Int,
        scrollX: Int,
        scrollY: Int,
        scrollRangeX: Int,
        scrollRangeY: Int,
        maxOverScrollX: Int,
        maxOverScrollY: Int,
        isTouchEvent: Boolean
    ): Boolean {
        var maxOverScrollX = maxOverScrollX
        var maxOverScrollY = maxOverScrollY
        val overScrollMode = this.overScrollMode
        val canScrollHorizontal = this.getScrollRangeX() > 0
        val canScrollVertical = this.getScrollRangeY() > 0
        val overScrollHorizontal = overScrollMode == 0 || overScrollMode == 1 && canScrollHorizontal
        val overScrollVertical = overScrollMode == 0 || overScrollMode == 1 && canScrollVertical
        var newScrollX = scrollX + deltaX
        if (!overScrollHorizontal) {
            maxOverScrollX = 0
        }

        var newScrollY = scrollY + deltaY
        if (!overScrollVertical) {
            maxOverScrollY = 0
        }

        val left = -maxOverScrollX
        val right = maxOverScrollX + scrollRangeX
        val top = -maxOverScrollY
        val bottom = maxOverScrollY + scrollRangeY
        var clampedX = false
        if (newScrollX > right) {
            newScrollX = right
            clampedX = true
        } else if (newScrollX < left) {
            newScrollX = left
            clampedX = true
        }

        var clampedY = false
        if (newScrollY > bottom) {
            newScrollY = bottom
            clampedY = true
        } else if (newScrollY < top) {
            newScrollY = top
            clampedY = true
        }

        if (clampedY && !this.hasNestedScrollingParent(1)) {
            this.mOverScroller.springBack(newScrollX, newScrollY, 0, getScrollRangeX(), 0, this.getScrollRangeY())
        }

        this.onOverScrolled(newScrollX, newScrollY, clampedX, clampedY)
        return clampedX || clampedY
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.scrollTo(scrollX, scrollY)
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
                mLastMotionX = event.getX(pointerIndex)
                mLastMotionY = event.getY(pointerIndex)
                mActivePointerId = event.getPointerId(0)

                Log.d(TAG, "dispatchTouchEvent ===== MotionEvent.action = ACTION_DOWN,mLastX=$mLastX,mLastY=$mLastY")
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val pointerId = event.getPointerId(pointerIndex)
                if (pointerId != mActivePointerId) {
                    mLastX = event.getX(pointerIndex)
                    mLastY = event.getY(pointerIndex)
                    mLastMotionX = event.getX(pointerIndex)
                    mLastMotionY = event.getY(pointerIndex)
                    mActivePointerId = pointerId
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

                Log.d(TAG, "dispatchTouchEvent ===== MotionEvent.action = ACTION_MOVE,moveX=$moveX,moveY=$moveY")
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
                    mLastMotionX = event.getX(newPointerIndex)
                    mLastMotionY = event.getY(newPointerIndex)
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

    private fun isNeedIntercept(): Boolean = when {
        getScrollRangeX() > 0 -> {
            val isOnLeft = !canScrollHorizontally(-1)
            val isOnRight = !canScrollHorizontally(1)
            when {
                mDeltaX == 0f -> false
                mDeltaX > 0 && isOnLeft -> false
                mDeltaX < 0 && isOnRight -> false
                else -> true
            }
        }

        getScrollRangeY() > 0 -> {
            val isOnTop = !canScrollVertically(-1)
            val isOnBottom = !canScrollVertically(1)

            when {
                mDeltaY == 0f -> false
                mDeltaY > 0 && isOnTop -> false
                mDeltaY < 0 && isOnBottom -> false
                else -> true
            }
        }
        else -> false
    }


    override fun computeScroll() {
        if (mOverScroller.computeScrollOffset()) {
//            Log.d("MultiFlowIndicator", "computeScroll")

            val x = mOverScroller.currX
            val y = mOverScroller.currY

            var dx = x - this.mLastScrollerX
            var dy = y - this.mLastScrollerY
            if (this.dispatchNestedPreScroll(dx, dy, this.mScrollConsumed, null as IntArray?, 1)) {
                dx -= this.mScrollConsumed[0]
                dy -= this.mScrollConsumed[1]
            }

            if (dx != 0 || dy != 0) {
                val oldScrollX = this.scrollX
                val oldScrollY = this.scrollY
                this.overScrollByCompat(
                    dx,
                    dy,
                    oldScrollX,
                    oldScrollY,
                    getScrollRangeX(),
                    getScrollRangeY(),
                    0,
                    0,
                    false
                )
                val scrolledDeltaX = this.scrollX - oldScrollX
                val scrolledDeltaY = this.scrollY - oldScrollY
                val unconsumedX = dx - scrolledDeltaY
                val unconsumedY = dy - scrolledDeltaY
                if (!this.dispatchNestedScroll(
                        scrolledDeltaX,
                        scrolledDeltaY,
                        unconsumedX,
                        unconsumedY,
                        null as IntArray?,
                        1
                    )
                ) {
                    val mode = this.overScrollMode
                    val canOverscroll = mode == 0 || mode == 1 && getScrollRangeY() > 0
//                    if (canOverscroll) {
//                        this.ensureGlows()
//                        if (y <= 0 && oldScrollY > 0) {
//                            this.mEdgeGlowTop.onAbsorb(this.mScroller.getCurrVelocity().toInt())
//                        } else if (y >= range && oldScrollY < range) {
//                            this.mEdgeGlowBottom.onAbsorb(this.mScroller.getCurrVelocity().toInt())
//                        }
//                    }
                }
            }

            this.mLastScrollerX = x
            this.mLastScrollerY = y


//            if (oldX != x || oldY != y) {
//                overScrollBy(
//                    x - oldX, y - oldY, oldX, oldY, getScrollRangeX(), getScrollRangeY(),
//                    mOverflingDistance, mOverflingDistance, false
//                )
//            }
//
//            scrollTo(mOverScroller.currX, mOverScroller.currY)
            ViewCompat.postInvalidateOnAnimation(this)
        } else {
            if (this.hasNestedScrollingParent(1)) {
                this.stopNestedScroll(1)
            }

            this.mLastScrollerX = 0
            this.mLastScrollerY = 0
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
                scrollX < getScrollRangeX()
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
        val bundle = Bundle()
        bundle.putParcelable(KEY_DEFAULT, super.onSaveInstanceState())
        bundle.putString(KEY_MODE, mMode.name)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            mMode = when (state.getString(KEY_MODE)) {
                MODE.HORIZONL.name -> MODE.VERTICAL
                else -> MODE.HORIZONL
            }
            changedMode()
            super.onRestoreInstanceState(state.getParcelable(KEY_DEFAULT))
        } else {
            super.onRestoreInstanceState(state)
        }
    }


    fun obtainAttributes(attrs: AttributeSet?) {
        attrs?.apply {
            val a = context.obtainStyledAttributes(attrs, R.styleable.MultiIndicator)

            mTextSelectedColor = a.getColor(R.styleable.MultiIndicator_multi_text_selected_color, Color.RED)
            mIconSelectedColor = a.getColor(R.styleable.MultiIndicator_multi_icon_selected_color, Color.RED)
            mTextUnSelectedColor = a.getColor(R.styleable.MultiIndicator_multi_text_unselected_color, Color.BLACK)
            mIconUnSelectedColor = a.getColor(R.styleable.MultiIndicator_multi_icon_unselected_color, Color.BLACK)
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
            mIndicatorWidth = a.getDimension(
                R.styleable.MultiIndicator_multi_indicator_width,
                resources.getDimension(R.dimen.dimen_8)
            )
            mIndicatorEqualsTitle = a.getBoolean(
                R.styleable.MultiIndicator_multi_indicator_equal_title,
                false
            )
            mIndicatorStyle = a.getInt(
                R.styleable.MultiIndicator_multi_indicator_style,
                STYLE_NORMAL
            )
            mIndicatorStyleRadius = a.getDimension(
                R.styleable.MultiIndicator_multi_indicator_radius,
                0f
            )
            mMaxHeight = a.getDimension(R.styleable.MultiIndicator_multi_max_height, -1f)
            mMaxLines = a.getFloat(R.styleable.MultiIndicator_multi_max_lines, -1f)
            mIndicatorColor =
                    a.getColor(R.styleable.MultiIndicator_multi_indicator_color, Color.GRAY)

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
        ViewCompat.postInvalidateOnAnimation(this)
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
        return if (mMeasureWidth - measuredWidth < 0) {
            0
        } else {
            mMeasureWidth - measuredWidth
        }
    }

    fun couldExtpand() = getScrollRangeX() > 0 || mMode == MODE.VERTICAL

    private fun initVelocityTracker() {
        if (!this::mVelocityTracker.isInitialized) {
            mVelocityTracker = VelocityTracker.obtain()
        }
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

    private fun calcIndicatorRect() {
        if (childCount > this.mCurrentTab) {
            val drawChildView = getChildAt(this.mCurrentTab)
            var left = drawChildView.left.toFloat()
            var right = drawChildView.right.toFloat()
            var top = drawChildView.top.toFloat()
            var bottom = drawChildView.bottom.toFloat()

            if (mIndicatorEqualsTitle) {
                left = drawChildView.left.toFloat() + drawChildView.paddingLeft
                right = drawChildView.right.toFloat() - drawChildView.paddingRight
            }

            when (mIndicatorStyle) {
                STYLE_NORMAL -> {
                    bottom = drawChildView.bottom.toFloat()
                    top = bottom - mIndicatorHeight
                }

                STYLE_RECTANGLE -> {
                    top = drawChildView.top.toFloat()
                    bottom = drawChildView.bottom.toFloat()
                }
            }

            if (this.mCurrentTab < childCount - 1) {
                val nextDrawChildView = getChildAt(this.mCurrentTab + 1)

                val nextTabLeft = nextDrawChildView.left
                val nextTabRight = nextDrawChildView.right
                val nextTabTop = nextDrawChildView.top
                val nextTabBottom = nextDrawChildView.bottom

                left += mCurrentTabOffset * (nextTabLeft - left)
                right += mCurrentTabOffset * (nextTabRight - right)

                when (mIndicatorStyle) {
                    STYLE_NORMAL -> {
                        bottom += mCurrentTabOffset * (nextTabBottom - bottom)
                        top = bottom - mIndicatorHeight
                    }

                    STYLE_RECTANGLE -> {
                        top += mCurrentTabOffset * (nextTabTop - top)
                        bottom += mCurrentTabOffset * (nextTabBottom - bottom)
                    }
                }
            }

            mIndicatorRect.left = left.toInt()
            mIndicatorRect.right = right.toInt()
            mIndicatorRect.top = top.toInt()
            mIndicatorRect.bottom = bottom.toInt()


//            val padding = drawChildView.measuredWidth.toFloat() / 2 - mIndicatorWidth / 2
        }
    }

    private fun autoScrollHorizontal() {
        if (childCount > this.mCurrentTab) {
            val drawChildView = getChildAt(this.mCurrentTab)
            var left = drawChildView.left.toFloat()
            var right = drawChildView.right.toFloat()
            var top = drawChildView.top.toFloat()
            var bottom = drawChildView.bottom.toFloat()

            if (mIndicatorEqualsTitle) {
                left = drawChildView.left.toFloat() + drawChildView.paddingLeft
                right = drawChildView.right.toFloat() - drawChildView.paddingRight
            }

            when (mIndicatorStyle) {
                STYLE_NORMAL -> {
                    bottom = drawChildView.bottom.toFloat()
                    top = bottom - mIndicatorHeight
                }

                STYLE_RECTANGLE -> {
                    top = drawChildView.top.toFloat()
                    bottom = drawChildView.bottom.toFloat()
                }
            }

            if (this.mCurrentTab < childCount - 1) {
                val nextDrawChildView = getChildAt(this.mCurrentTab + 1)

                val nextTabLeft = nextDrawChildView.left
                val nextTabRight = nextDrawChildView.right
                val nextTabTop = nextDrawChildView.top
                val nextTabBottom = nextDrawChildView.bottom

                left += mCurrentTabOffset * (nextTabLeft - left)
                right += mCurrentTabOffset * (nextTabRight - right)

                when (mIndicatorStyle) {
                    STYLE_NORMAL -> {
                        bottom += mCurrentTabOffset * (nextTabBottom - bottom)
                        top = bottom - mIndicatorHeight
                    }

                    STYLE_RECTANGLE -> {
                        top += mCurrentTabOffset * (nextTabTop - top)
                        bottom += mCurrentTabOffset * (nextTabBottom - bottom)
                    }
                }
            }

        }
    }

    fun setViewPager(viewPager: ViewPager) {
        this.mViewPager = viewPager
        this.mViewPager.addOnPageChangeListener(onPageChangeListener)
        if (this.mViewPager.currentItem < 0) {
            this.mViewPager.currentItem = 0
        }

        when {
            this.mViewPager.adapter == null -> throw IllegalArgumentException("MultiFlowIndicator must be set ViewPager adapter first")
            this.mMultiFlowAdapter == null -> throw IllegalArgumentException("MultiFlowIndicator must be set MultiFlowAdapter first")
            this.mViewPager.adapter?.count ?: 0 > this.mMultiFlowAdapter?.getItemCount() ?: 0 -> throw IllegalArgumentException(
                "MultiFlowIndicator title length must be > viewpager page length"
            )
            else -> {
                this.mMultiFlowAdapter?.also {
                    for (index in 0 until it.getItemCount()) {
                        if (index == this@MultiFlowIndicator.mViewPager.currentItem) {
                            it.onSelected(
                                getChildAt(index),
                                index,
                                mTextSelectedSize,
                                mTextSelectedColor,
                                mIconSelectedColor
                            )
                        } else {
                            it.unSelected(
                                getChildAt(index),
                                index,
                                mTextUnSelectedSize,
                                mTextUnSelectedColor,
                                mIconUnSelectedColor
                            )
                        }
                    }
                }
            }
        }
    }


    fun changedMode() {
        mOverScroller.startScroll(scrollX, scrollY, -scrollX, -scrollY)
        mMode = if (mMode != MultiFlowIndicator.MODE.HORIZONL) {
            MultiFlowIndicator.MODE.HORIZONL
        } else {
            MultiFlowIndicator.MODE.VERTICAL
        }

        this.mOnLayoutChanged?.changed(mMode.name)
        requestLayout()

        post {
            when (mMode) {
                MultiFlowIndicator.MODE.HORIZONL -> {
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
                        ViewCompat.postInvalidateOnAnimation(this)
                    }
                }
                MultiFlowIndicator.MODE.VERTICAL -> {
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
                        ViewCompat.postInvalidateOnAnimation(this)
                    }
                }
                else -> {
                }
            }
        }
    }

    fun setAdapter(multiFlowAdapter: MultiFlowAdapter<Any>) {
        this.mMultiFlowAdapter = multiFlowAdapter
        this.mMultiFlowAdapter?.setOnDataChangedListener(this)
        changeAdapter()
    }

    fun setPage(position: Int) {
        if (this.mViewPager.adapter?.count ?: return > position) {
            this.mViewPager.setCurrentItem(position, false)
        }
    }

    fun getPage() = this.mViewPager.currentItem

    fun getAdapter() = this.mMultiFlowAdapter

    fun changedIndicatorColor(indicatorColor: Int) {
        this.mIndicatorColor = indicatorColor
        ViewCompat.postInvalidateOnAnimation(this)
    }

    fun changedAdapterUi(
        textSelectColor: Int = mTextSelectedColor,
        iconSelectColor: Int = mIconSelectedColor,
        textSelectSize: Float = mTextSelectedSize,
        textUnSelectColor: Int = mTextUnSelectedColor,
        iconUnSelectColor: Int = mIconUnSelectedColor,
        textUnSelectSize: Float = mTextUnSelectedSize
    ) {
        this.mTextSelectedColor = textSelectColor
        this.mIconSelectedColor = iconSelectColor
        this.mTextSelectedSize = textSelectSize
        this.mTextUnSelectedColor = textUnSelectColor
        this.mIconUnSelectedColor = iconUnSelectColor
        this.mTextUnSelectedSize = textUnSelectSize

        for (index in 0 until childCount) {
            mMultiFlowAdapter?.apply {
                if (index == this@MultiFlowIndicator.mViewPager.currentItem) {
                    this.onSelected(
                        this@MultiFlowIndicator.getChildAt(index),
                        index,
                        mTextSelectedSize,
                        mTextSelectedColor,
                        iconSelectColor
                    )
                } else {
                    this.unSelected(
                        this@MultiFlowIndicator.getChildAt(index),
                        index,
                        mTextUnSelectedSize,
                        mTextUnSelectedColor,
                        iconUnSelectColor
                    )
                }
            }
        }
    }

    private fun changeAdapter() {
        this.mMultiFlowAdapter?.also {
            removeAllViews()
            for (index in 0 until it.getItemCount()) {
                val view = it.getView(this, index)
                view.layoutParams = generateDefaultLayoutParams()
                addView(view)
                view.setOnClickListener {
                    if (mItemClickCallback == null || mItemClickCallback?.callback(index) == true) {
                        this.mViewPager.setCurrentItem(index, false)
                    }
                }
            }
        }
    }

    override fun notifyChanged() {
        changeAdapter()
        changedAdapterUi()
    }

    override fun insert(positionStart: Int, count: Int) {
        this.mMultiFlowAdapter?.let {
            for (index in 0 until count) {
                val view = it.getView(this, positionStart + index)
                view.layoutParams = generateDefaultLayoutParams()
                addView(view, positionStart + index)
            }

            for (index in 0 until it.getItemCount()) {
                val view = getChildAt(index)
                view.setOnClickListener {
                    if (mItemClickCallback?.callback(index) == true) {
                        this.mViewPager.setCurrentItem(index, false)
                    }
                }
            }
        }
    }

    override fun remove(positionStart: Int, count: Int) {
        this.mMultiFlowAdapter?.let {
            for (index in 0 until count) {
                removeViewAt(positionStart + index)
            }
        }
    }

    private val onPageChangeListener = object : ViewPager.OnPageChangeListener {

        override fun onPageScrollStateChanged(p0: Int) {

        }

        override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            Log.d(TAG, "onPageScrolled p0=$p0  p1=$p1  p2=$p2")

            val scrollChildIndex = if (p0 == mCurrentTab && mCurrentTabOffset < p1) {//右翻
                p0 + 1
            } else {
                p0
            }

            when (mMode) {
                MultiFlowIndicator.MODE.HORIZONL -> {
                    if (childCount > scrollChildIndex) {
                        val childView = getChildAt(scrollChildIndex)
                        val centerLeftX = childView.left + childView.measuredWidth.toFloat() / 2
                        var dx = (centerLeftX - measuredWidth.toFloat() / 2).toInt()
                        dx = when {
                            dx < 0 -> -scrollX
                            dx > getScrollRangeX() -> getScrollRangeX() - scrollX
                            else -> dx - scrollX
                        }

                        overScrollByCompat(
                            dx,
                            0,
                            scrollX,
                            scrollY,
                            getScrollRangeX(),
                            getScrollRangeY(),
                            0,
                            0,
                            false
                        )

                    }
                }
                MultiFlowIndicator.MODE.VERTICAL -> {
                    if (childCount > scrollChildIndex) {
                        val childView = getChildAt(scrollChildIndex)
                        val centerTopY = childView.top + childView.measuredHeight.toFloat() / 2
                        var dy = (centerTopY - measuredHeight.toFloat() / 2).toInt()
                        dy = when {
                            dy < 0 -> -scrollY
                            dy > getScrollRangeY() -> getScrollRangeY() - scrollY
                            else -> dy - scrollY
                        }
                        overScrollByCompat(
                            0,
                            dy,
                            scrollX,
                            scrollY,
                            getScrollRangeX(),
                            getScrollRangeY(),
                            0,
                            0,
                            false
                        )
                    }
                }
                else -> {
                    changedMode()
                }
            }

            mCurrentTab = p0
            mCurrentTabOffset = p1
            mCurrentTabOffsetPixel = p2

            ViewCompat.postInvalidateOnAnimation(this@MultiFlowIndicator)
        }

        override fun onPageSelected(p0: Int) {
            mMultiFlowAdapter?.apply {
                if (mPreSelectedTab != p0) {
                    this.onSelected(
                        this@MultiFlowIndicator.getChildAt(p0),
                        p0,
                        mTextSelectedSize,
                        mTextSelectedColor,
                        mIconSelectedColor
                    )
                    this.unSelected(
                        this@MultiFlowIndicator.getChildAt(mPreSelectedTab),
                        mPreSelectedTab,
                        mTextUnSelectedSize,
                        mTextUnSelectedColor,
                        mIconUnSelectedColor
                    )
                }
            }
            mPreSelectedTab = p0
        }
    }

    // NestedScrollingChild
    public override fun startNestedScroll(axes: Int, type: Int): Boolean {
        return this.mNestedScrollingChildHelper.startNestedScroll(axes, type)
    }

    override fun stopNestedScroll(type: Int) {
        this.mNestedScrollingChildHelper.stopNestedScroll(type)
    }

    override fun hasNestedScrollingParent(type: Int): Boolean {
        return this.mNestedScrollingChildHelper.hasNestedScrollingParent(type)
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return this.mNestedScrollingChildHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow,
            type
        )
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return this.mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        this.mNestedScrollingChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return this.mNestedScrollingChildHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return this.startNestedScroll(axes, 0)
    }

    override fun stopNestedScroll() {
        this.stopNestedScroll(0)
    }

    override fun hasNestedScrollingParent(): Boolean {
        return this.hasNestedScrollingParent(0)
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?
    ): Boolean {
        return this.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, 0)
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        return this.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, 0)
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return this.mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return this.mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    // NestedScrollingParent
    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return axes and 2 != 0
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        this.mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes, type)
        this.startNestedScroll(2, type)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        this.mNestedScrollingParentHelper.onStopNestedScroll(target, type)
        this.stopNestedScroll(type)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        val oldScrollY = this.scrollY
        this.scrollBy(0, dyUnconsumed)
        val myConsumed = this.scrollY - oldScrollY
        val myUnconsumed = dyUnconsumed - myConsumed
        this.dispatchNestedScroll(0, myConsumed, 0, myUnconsumed, null as IntArray?, type)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        this.dispatchNestedPreScroll(dx, dy, consumed, null as IntArray?, type)
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return this.onStartNestedScroll(child, target, nestedScrollAxes, 0)
    }

    override fun onNestedScrollAccepted(child: View, target: View, nestedScrollAxes: Int) {
        this.onNestedScrollAccepted(child, target, nestedScrollAxes, 0)
    }

    override fun onStopNestedScroll(target: View) {
        this.onStopNestedScroll(target, 0)
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        this.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, 0)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        this.onNestedPreScroll(target, dx, dy, consumed, 0)
    }

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return if (!consumed) {
            this.flingWithNestedDispatch(velocityX.toInt(), velocityY.toInt())
            true
        } else {
            false
        }
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return this.dispatchNestedPreFling(velocityX, velocityY)
    }

    private fun flingWithNestedDispatch(velocityX: Int, velocityY: Int) {
        when (mMode) {
            MODE.HORIZONL -> {
                val canFling = (scrollX > 0 || velocityX > 0) && (scrollX < getScrollRangeX() || velocityX < 0)
                if (!dispatchNestedPreFling(velocityX.toFloat(), 0f)) {
                    dispatchNestedFling(velocityX.toFloat(), 0f, canFling)
                    fling(velocityX, 0)
                }
            }
            else -> {
                val canFling = (scrollY > 0 || velocityY > 0) && (scrollY < getScrollRangeY() || velocityY < 0)
                if (!dispatchNestedPreFling(0f, velocityY.toFloat())) {
                    dispatchNestedFling(0f, velocityY.toFloat(), canFling)
                    fling(0, velocityY)
                }
            }
        }
    }

    fun fling(velocityX: Int, velocityY: Int) {
        if (childCount > 0) {
            when (mMode) {
                MODE.HORIZONL -> {
                    startNestedScroll(ViewCompat.SCROLL_AXIS_HORIZONTAL, ViewCompat.TYPE_NON_TOUCH)
                    mOverScroller.fling(
                        scrollX, scrollY, velocityX, 0, -2147483648, 2147483647,
                        0,
                        0, 0, 0
                    )
                }
                else -> {
                    startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH)
                    mOverScroller.fling(
                        scrollX, scrollY, 0, velocityY, 0, 0,
                        -2147483648,
                        2147483647, 0, 0
                    )
                }
            }

            this.mLastScrollerX = this.scrollX
            this.mLastScrollerY = this.scrollY
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    enum class MODE {
        HORIZONL, VERTICAL, INVALID
    }

    companion object {
        val TAG = "MultiFlowIndicator"
        private val STYLE_NORMAL = 0
        private val STYLE_RECTANGLE = 1
        private val KEY_DEFAULT = "key_default"
        private val KEY_MODE = "key_mode"
        private val KEY_ADAPTER = "key_adapter"

        interface ItemClickCallback {
            fun callback(position: Int): Boolean = true
        }

        interface OnLayoutChanged {
            fun changed(mode: String)
        }
    }
}