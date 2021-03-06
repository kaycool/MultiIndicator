package com.kai.wang.space.indicator.lib

import android.view.View
import android.view.ViewGroup
import java.io.Serializable


/**
 * @author kai.w
 * @des  $des
 */
abstract class MultiFlowAdapter<out T>(val mutiDatas: MutableList<out T>) : Serializable {
    private var mOnDataChangedListener: OnDataChangedListener? = null

    fun setOnDataChangedListener(listener: OnDataChangedListener) {
        mOnDataChangedListener = listener
    }

    abstract fun getView(parent: ViewGroup, position: Int): View

    abstract fun onSelected(view: View,
                            position: Int,
                            selectTextSize: Float,
                            selectTextColor: Int,
                            selectIconColor: Int)

    abstract fun unSelected(view: View,
                            position: Int,
                            unSelectTextSize: Float,
                            unSelectTextColor: Int,
                            unSelectIconColor: Int)

    fun getItem(position: Int): T = mutiDatas[position]

    fun getItemCount(): Int = mutiDatas.size

    fun notifyDataChanged() {
        mOnDataChangedListener?.notifyChanged()
    }

    fun notifyItemInsert(position: Int) {
        mOnDataChangedListener?.insert(position, 1)
    }

    fun notifyItemInsert(positionStart: Int, count: Int) {
        mOnDataChangedListener?.insert(positionStart, count)
    }

    fun notifyItemRemoved(position: Int) {
        mOnDataChangedListener?.remove(position, 1)
    }

    fun notifyItemRemoved(positionStart: Int, count: Int) {
        mOnDataChangedListener?.remove(positionStart, count)
    }

}

interface OnDataChangedListener {

    fun notifyChanged()

    fun insert(positionStart: Int, count: Int)

    fun remove(positionStart: Int, count: Int)
}