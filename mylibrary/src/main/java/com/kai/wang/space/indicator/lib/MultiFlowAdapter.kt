package com.kai.wang.space.indicator.lib

import android.view.View
import android.view.ViewGroup


/**
 * @author kai.w
 * @des  $des
 */
abstract class MultiFlowAdapter(mutiDatas: MutableList<String>) {
    private val mMutiDatas = mutiDatas
    private var mOnDataChangedListener: OnDataChangedListener? = null

    fun setOnDataChangedListener(listener: OnDataChangedListener) {
        mOnDataChangedListener = listener
    }

    abstract fun getView(parent: ViewGroup, position: Int, t: String): View

    abstract fun onSelected(view: View, position: Int)

    abstract fun unSelected(view: View, position: Int)

    fun getItem(position: Int): String = mMutiDatas[position]

    fun getItemCount(): Int = mMutiDatas.size


    fun notifyDataChanged() {
        mOnDataChangedListener?.onChanged()
    }
}

interface OnDataChangedListener {
    fun onChanged()
}