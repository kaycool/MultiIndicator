package com.kai.wang.space.indicator

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.kai.wang.space.indicator.lib.MultiFlowAdapter
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_flow, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        flowLayout.setAdapter(object : MultiFlowAdapter(mFlows) {
            override fun getView(parent: ViewGroup, position: Int, t: String): View {
                val textView = TextView(activity!!.applicationContext)
                textView.text = t
                textView.setBackgroundColor(Color.BLUE)
                return textView
            }

            override fun onSelected(view: View, position: Int) {
                (view as TextView)?.apply {
                    this.setTextColor(Color.RED)
                }
            }

            override fun unSelected(view: View, position: Int) {
                (view as TextView)?.apply {
                    this.setTextColor(Color.GRAY)
                }
            }

        })
        btnMode.setOnClickListener {
            flowLayout.changedMode()
        }
    }
}