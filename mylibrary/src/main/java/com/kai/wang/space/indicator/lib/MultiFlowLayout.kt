package com.kai.wang.space.indicator.lib

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.nfc.Tag
import android.os.Build
import android.os.Parcelable
import android.support.v4.view.NestedScrollingChild
import android.support.v4.view.NestedScrollingChildHelper
import android.support.v4.view.NestedScrollingParentHelper
import android.support.v4.view.ViewCompat.*
import android.support.v4.widget.ViewDragHelper.INVALID_POINTER
import android.text.method.Touch.scrollTo
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.OverScroller


/**
 * @author kai.w
 * @des  $des
 */
class MultiFlowLayout : ViewGroup, OnDataChangedListener {
    private var mMultiFlowAdapter: MultiFlowAdapter<Any>? = null
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


    private var mMeasureWidth = 0
    private var mMeasureHeight = 0
    /** 用于绘制显示器  */
    private var mPaddingHorizontal = 0
    private var mPaddingVertical = 0
    private var mMaxHeight = -1f
    private var mMaxLines = -1f
    private var mMaxSelectedCount = -1
    private var mMaxSelectedTips = ""
    private var mTextSelectedColor = Color.RED
    private var mIconSelectedColor = Color.RED
    private var mTextUnSelectedColor = Color.BLACK
    private var mIconUnSelectedColor = Color.BLACK
    private var mTextSelectedSize = resources.getDimension(R.dimen.sp_10)
    private var mTextUnSelectedSize = resources.getDimension(R.dimen.sp_10)


    private var mItemClickCallback: MultiFlowLayout.Companion.ItemClickCallback? = null

    fun setItemClickCallback(itemClickCallback: MultiFlowLayout.Companion.ItemClickCallback) {
        this.mItemClickCallback = itemClickCallback
    }


    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setWillNotDraw(false)

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
        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            measureChild(childView,
                MeasureSpec.makeMeasureSpec(parentWidth - mPaddingHorizontal * 2, MeasureSpec.AT_MOST),
                heightMeasureSpec)
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

        setMeasuredDimension(parentWidth, when {
            mMaxHeight > 0 -> Math.min(mMeasureHeight + paddingTop + paddingBottom, mMaxHeight.toInt())
            mLinesMaxHeight > 0 -> mLinesMaxHeight + paddingTop + paddingBottom + mPaddingVertical
            else -> mMeasureHeight + paddingTop + paddingBottom
        })
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var left = 0
        var top = 0
        var right = 0
        var bottom = 0
        var lineHeight = 0
        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            val layoutParams = childView.layoutParams as MarginLayoutParams

            left += layoutParams.leftMargin + mPaddingHorizontal
            if (i == 0) {
                left += paddingLeft
            }
            right = left + childView.measuredWidth
            if (right + paddingRight > measuredWidth) {
                left = layoutParams.leftMargin + paddingLeft + mPaddingHorizontal
                right = left + childView.measuredWidth
                bottom += lineHeight
                lineHeight = 0
            }
            if (i == 0) {
                bottom += paddingTop + mPaddingVertical
            }
            top = bottom + layoutParams.topMargin

            childView.layout(left, top, right, top + childView.measuredHeight)
            left = right + layoutParams.rightMargin
            lineHeight = Math.max(lineHeight,
                childView.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin + mPaddingVertical)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
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
            mPaddingHorizontal = a.getDimensionPixelOffset(R.styleable.MultiFlowLayout_multi_flow_padding_horizontal,
                context.resources.getDimensionPixelOffset(R.dimen.dimen_5))
            mPaddingVertical = a.getDimensionPixelOffset(R.styleable.MultiFlowLayout_multi_flow_padding_vertical,
                context.resources.getDimensionPixelOffset(R.dimen.dimen_5))
            mMaxHeight = a.getDimension(R.styleable.MultiFlowLayout_multi_flow_max_height, -1f)
            mMaxLines = a.getFloat(R.styleable.MultiFlowLayout_multi_flow_max_lines, -1f)
            mMaxSelectedCount = a.getInt(R.styleable.MultiFlowLayout_multi_max_selected_count, -1)
            mMaxSelectedTips = a.getString(R.styleable.MultiFlowLayout_multi_max_selected_Tips) ?: ""
            a.recycle()
        }
    }


    fun setAdapter(multiFlowAdapter: MultiFlowAdapter<Any>) {
        this.mMultiFlowAdapter = multiFlowAdapter
        this.mMultiFlowAdapter?.setOnDataChangedListener(this)
        changeAdapter()
    }

    private fun changeAdapter() {
        this.mMultiFlowAdapter?.apply {
            removeAllViews()
            for (index in 0 until this.getItemCount()) {
                val view = this.getView(this@MultiFlowLayout, index)
                view.layoutParams = generateDefaultLayoutParams()
                addView(view)

                if (mSelectedView.contains(index)) {
                    this.onSelected(view, index, mTextSelectedSize, mTextSelectedColor, mIconSelectedColor)
                }

                view.setOnClickListener {
                    if (mItemClickCallback?.callback(index) == true) {
                        var hasMaxLimit = false
                        if (mSelectedView.contains(index)) {
                            mSelectedView.remove(index)
                            this.unSelected(view,
                                index,
                                mTextUnSelectedSize,
                                mTextUnSelectedColor,
                                mIconUnSelectedColor)
                        } else {
                            hasMaxLimit = mMaxSelectedCount > 0 && mSelectedView.size >= mMaxSelectedCount
                            if (!hasMaxLimit) {
                                mSelectedView.add(index)
                                this.onSelected(view, index, mTextSelectedSize, mTextSelectedColor, mIconSelectedColor)
                            }
                        }
                        if (hasMaxLimit) {
                            mItemClickCallback?.limitClick()
                        }
                    }
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

    override fun notifyChanged() {
        changeAdapter()
    }

    override fun insert(positionStart: Int, count: Int) {
        this.mMultiFlowAdapter?.apply {
            for (index in 0 until count) {
                val view = this.getView(this@MultiFlowLayout, positionStart + index)
                view.layoutParams = generateDefaultLayoutParams()
                addView(view, positionStart + index)
            }

            for (index in 0 until this.getItemCount()) {
                val view = getChildAt(index)
                if (mSelectedView.contains(index)) {
                    this.onSelected(view, index, mTextSelectedSize, mTextSelectedColor, mIconSelectedColor)
                }

                view.setOnClickListener {
                    if (mItemClickCallback?.callback(index) == true) {
                        var hasMaxLimit = false
                        if (mSelectedView.contains(index)) {
                            mSelectedView.remove(index)
                            this.unSelected(view, index, mTextSelectedSize, mTextSelectedColor, mIconUnSelectedColor)
                        } else {
                            hasMaxLimit = mMaxSelectedCount > 0 && mSelectedView.size <= mMaxSelectedCount
                            if (hasMaxLimit) {
                                mSelectedView.add(index)
                                this.onSelected(view, index, mTextSelectedSize, mTextSelectedColor, mIconSelectedColor)
                            }
                        }

                        if (hasMaxLimit) {
                            mItemClickCallback?.limitClick()
                        }
                    }
                }
            }
        }
    }

    override fun remove(positionStart: Int, count: Int) {
        this.mMultiFlowAdapter?.apply {
            for (index in 0 until count) {
                removeViewAt(positionStart + index)
            }
        }
    }

    companion object {
        val TAG = "MultiFlowLayout"


        interface ItemClickCallback {
            fun callback(position: Int): Boolean = true

            fun limitClick()
        }
    }
}