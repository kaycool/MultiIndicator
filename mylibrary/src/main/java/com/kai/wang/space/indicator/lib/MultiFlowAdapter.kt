package com.kai.wang.space.indicator.lib

import android.view.View


/**
 * @author kai.w
 * @des  $des
 */
interface MultiFlowAdapter<T> {
//    private val mMutiDatas = mutableListOf<T>()

    fun getView(parent: MultiFlowIndicator, position: Int, t: T): View

    fun onSelected(view: View, position: Int)

    fun unSelected(view: View, position: Int)

    fun getItem(position: Int): T

    fun getItemCount(): Int
}