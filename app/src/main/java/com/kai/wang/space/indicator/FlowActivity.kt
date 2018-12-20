package com.kai.wang.space.indicator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * @author kai.w
 * @des  $des
 */
class FlowActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow)

        supportFragmentManager.beginTransaction().add(R.id.flContain, FlowFragment()).commitAllowingStateLoss()
    }


    companion object {

        fun startActivity(activity: Activity) {
            val intent = Intent(activity, FlowActivity::class.java)
            activity.startActivity(intent)
        }
    }

}