package com.kai.wang.space.indicator.lib

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

/**
 * @author kai.w
 * @des  $des
 */
class MultiIndicatorHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val mTvTitle by lazy { itemView.findViewById<TextView>(R.id.tvIndicatorTitle) }
}
