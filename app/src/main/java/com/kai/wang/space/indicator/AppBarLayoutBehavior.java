package com.kai.wang.space.indicator;

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.Keep;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.OverScroller;
import com.kai.wang.space.indicator.lib.MultiFlowIndicator;

import java.lang.reflect.Field;

/**
 * 用于修复AppBarLayout中的RecyclerView和ViewPager中的RecyclerView滑动冲突
 * 1.AppBarLayout中的RecyclerView快速上滑然后在ViewPager中的RecyclerView快速下滑，会出现抖动的问题
 * 2.ViewPager中的RecyclerView快速下滑,两个RecyclerView均滑动到顶部时ViewPager中的RecyclerView没有停止实践分发，
 * 导致滑动AppBarLayout中的RecyclerView卡顿
 */
@Keep
public class AppBarLayoutBehavior extends AppBarLayout.Behavior {
    private String       tag   = "AppBarLayoutBehavior";
    private String       cName = "AppBarLayoutBehavior";
    private OverScroller mScroller1;

    private ViewGroup          appbar;
    private ViewGroup          viewPager;
    private MultiFlowIndicator multiFlowIndicator;

    @Keep
    public AppBarLayoutBehavior() {
        super();
    }

    @Keep
    public AppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
        if (multiFlowIndicator != null && isTouchPointInView(multiFlowIndicator, (int) ev.getRawX(), (int) ev.getRawY())) {
            return false;
        } else {
            return super.onInterceptTouchEvent(parent, child, ev);
        }
    }

    private boolean isTouchPointInView(View view, int x, int y) {
        if (view == null) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        //view.isClickable() &&
        if (y >= top && y <= bottom && x >= left
            && x <= right) {
            return true;
        }
        return false;
    }
}
