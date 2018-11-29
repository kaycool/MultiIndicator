package com.kai.wang.space.indicator

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.kai.wang.space.indicator.lib.MultiFlowAdapter
import com.kai.wang.space.indicator.lib.MultiFlowLayout
import kotlinx.android.synthetic.main.fragment_flow.*

/**
 * @author kai.w
 * @des  $des
 */
class FlowFragment : Fragment() {
    private val mFlows by lazy {
        mutableListOf(
            "标题一",
            "标题二222222222标题二222222222标题二222222222标题二222222222",
            "标题三",
            "标题四标题十五22222222222标题十五22222222222标题十五22222222222标题十五22222222222标题十五22222222222标题十五22222222222标题十五22222222222标题十五22222222222标题十五22222222222标题十五22222222222",
            "标题五",
            "标题六",
            "标题七2222222222222",
            "标题八",
            "标题九",
            "标题九",
            "标题十22222222",
            "标题十一",
            "标题十二",
            "标题十三",
            "标题十四2222222",
            "标题十五22222222222",
            "标题十六22",
            "标题十七",
            "标题十八",
            "标题十九",
            "标题二十22222222222",
            "标题二十一",
            "标题二十二222222222222",
            "标题二十四222222222222222",
            "标题二十五222222222222222222",
            "标题二十六222",
            "标题二十七22222",
            "标题二十八2222",
            "标题二十九22222",
            "标题三十2222",
            "标题三十一"
        )
    }

    private val mAdapter = object : MultiFlowAdapter<String>(mFlows) {

        override fun getView(parent: ViewGroup, position: Int): View {
            val textView = TextView(activity!!.applicationContext)
            textView.text = getItem(position)
            textView.setBackgroundColor(Color.BLUE)
            return textView
        }

        override fun onSelected(view: View, position: Int, selectTextSize: Float, selectTextColor: Int) {
            (view as TextView)?.apply {
                this.setTextColor(Color.RED)
            }
        }

        override fun unSelected(view: View, position: Int, unSelectTextSize: Float, unSelectTextColor: Int) {
            (view as TextView)?.apply {
                this.setTextColor(Color.GRAY)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_flow, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        flowLayout.setAdapter(mAdapter)
        flowLayout.setMaxSelectCount(3)
        flowLayout.setItemClickCallback(object : MultiFlowLayout.Companion.ItemClickCallback {

            override fun callback(position: Int): Boolean {
                return if (position == flowLayout.childCount - 1) {
                    Toast.makeText(activity, "自定义点击事件", Toast.LENGTH_SHORT).show()
                    false
                } else {
                    super.callback(position)
                }
            }

            override fun limitClick() {
                Toast.makeText(activity, "hahah", Toast.LENGTH_SHORT).show()
            }

        })
        btnMode.setOnClickListener {
            flowLayout.changedMode()
        }
        btnAdd.setOnClickListener {
            mFlows.add(1, "11111111")
            mFlows.add(2, "22222222")
            mFlows.add(3, "33333333")
            mAdapter.notifyItemInsert(1, 3)
        }
        btnRemove.setOnClickListener {
            mFlows.removeAt(1)
            mFlows.removeAt(2)
            mFlows.removeAt(3)
            mAdapter.notifyItemRemoved(1, 3)
        }

        mAdapter.mutiDatas
    }
}