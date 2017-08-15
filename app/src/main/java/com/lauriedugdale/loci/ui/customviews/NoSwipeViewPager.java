package com.lauriedugdale.loci.ui.customviews;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Used for the ViewPager in the EntryUploadActivity to disable swiping between fragments
 *
 * @author Laurie Dugdale
 */

public class NoSwipeViewPager extends ViewPager {

    private boolean mEnabled;

    public NoSwipeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mEnabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mEnabled) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mEnabled) {
            return super.onInterceptTouchEvent(event);
        }
        return false;
    }

    public void setPagingEnabled(boolean enabled) {
        mEnabled = enabled;
    }
}