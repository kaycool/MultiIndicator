package com.kai.wang.space.indicator.lib

import android.view.View
import android.view.ViewGroup

/**
 * @author kai.w
 * @des  $des
 */
interface MultiFlowAdapter {

    fun onCreateIndicatorView(parent: ViewGroup): View

    fun selectView(view: View, position: Int)

    fun unSelectView(view: View, position: Int)

    fun getItemCount(): Int = 0
}