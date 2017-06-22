package com.lauriedugdale.loci.ui.nav;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.lauriedugdale.loci.R;

/**
 * Created by mnt_x on 21/06/2017.
 */

public class LociTitleView extends Toolbar implements ViewPager.OnPageChangeListener {

    public LociTitleView(@NonNull Context context) {
        this(context, null);
    }

    public LociTitleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LociTitleView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initialise the ImageViews
     */
    private void init() {
//        LayoutInflater.from(getContext()).inflate(R.layout.view_loci_title, this, true);
    }

    /**
     * listeners for the tabbed icons (ImageView)
     * @param viewPager
     */
    public void setUpWithViewPager(final ViewPager viewPager, final Context context) {
        viewPager.addOnPageChangeListener(this);

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}