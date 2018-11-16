package com.kai.wang.space.indicator

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v7.app.AppCompatActivity
import com.kai.wang.space.indicator.lib.MultiFlowIndicator
import kotlinx.android.synthetic.main.activity_test.*

class MainActivity : AppCompatActivity() {
    private val mTitles by lazy {
        mutableListOf(
            "标题一",
            "标题二",
            "标题三",
            "标题四",
            "标题五",
            "标题六",
            "标题七",
            "标题八",
            "标题九",
            "标题十",
            "标题十一",
            "标题十二",
            "标题十三",
            "标题十四",
            "标题十五",
            "标题十六",
            "标题十七",
            "标题十八",
            "标题十九",
            "标题二十",
            "标题二十一",
            "标题二十二",
            "标题二十四",
            "标题二十五",
            "标题二十六",
            "标题二十七",
            "标题二十八",
            "标题二十九",
            "标题三十"
        )
    }

    private val mFragments by lazy {
        mutableListOf(
            TestFragment()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        setContentView(R.layout.activity_test)

        btnMode.setOnClickListener {
            spaceFlowIndicator.changedMode(MultiFlowIndicator.MODE.VERTICAL)
        }

        viewPager.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(p0: Int): Fragment {
                return TestFragment()
            }

            override fun getCount(): Int {
                return mTitles.size
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return mTitles[position]
            }

        }
        spaceFlowIndicator.setViewPager(viewPager)
//
//        spaceIndicator.setViewPager(viewPager)
    }
}
