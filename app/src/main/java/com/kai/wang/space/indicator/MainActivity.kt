package com.kai.wang.space.indicator

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.kai.wang.space.indicator.lib.MultiFlowAdapter
import kotlinx.android.synthetic.main.activity_test.*

class MainActivity : AppCompatActivity() {
    private val mTitles by lazy {
        mutableListOf<String>(
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
            "标题三十2222"
        )
    }

    private val mFragments by lazy {
        mutableListOf(
            IndicatorFragment()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        setContentView(R.layout.activity_test)

        btnMode.setOnClickListener {
            spaceFlowIndicator.changedMode()
        }

        viewPager.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(p0: Int): Fragment {
                return IndicatorFragment()
            }

            override fun getCount(): Int {
                return mTitles.size
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return mTitles[position]
            }

        }
        spaceFlowIndicator.setAdapter(object : MultiFlowAdapter(mTitles) {

            override fun getView(parent: ViewGroup, position: Int, t: String): View {
                val textView = TextView(applicationContext)
                textView.text = t
                textView.setPadding(
                    resources.getDimensionPixelOffset(R.dimen.dimen_8)
                    , resources.getDimensionPixelOffset(R.dimen.dimen_5)
                    , resources.getDimensionPixelOffset(R.dimen.dimen_8)
                    , resources.getDimensionPixelOffset(R.dimen.dimen_5)
                )
                return textView
            }

            override fun onSelected(view: View, position: Int) {
                (view as? TextView)?.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        android.R.color.holo_red_light
                    )
                )

            }

            override fun unSelected(view: View, position: Int) {
                (view as? TextView)?.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        android.R.color.black
                    )
                )
            }

        })
        spaceFlowIndicator.setViewPager(viewPager)
//
//        spaceIndicator.setViewPager(viewPager)
    }
}
