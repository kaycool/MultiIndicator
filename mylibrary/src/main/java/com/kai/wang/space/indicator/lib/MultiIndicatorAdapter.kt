package com.kai.wang.space.indicator.lib

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * @author kai.w
 * @des  $des
 */
class MultiIndicatorAdapter(val mContext: Context, val mTitles: MutableList<String>) :
    RecyclerView.Adapter<MultiIndicatorHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MultiIndicatorHolder {
        return MultiIndicatorHolder(LayoutInflater.from(mContext).inflate(R.layout.item_space_inddicator, p0, false))
    }

    override fun getItemCount(): Int {
        return mTitles.size
    }

    override fun onBindViewHolder(p0: MultiIndicatorHolder, p1: Int) {
        p0.mTvTitle.text = mTitles[p1]
    }

}