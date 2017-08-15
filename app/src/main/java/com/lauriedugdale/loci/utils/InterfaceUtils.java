package com.lauriedugdale.loci.utils;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TabHost;
import android.widget.TextView;

import com.lauriedugdale.loci.R;

/**
 * Helper methods for the user interface
 *
 * @author Laurie Dugdale
 */
public class InterfaceUtils {

    /**
     * Method for setting up the toolbar for an activity
     *
     * @param activity The activity for toolbar to be displayon
     * @param toolBarID The id of the toolbar to be displayed
     * @param title The title of the toolbar to display
     */
    public static void setUpToolbar(AppCompatActivity activity, int toolBarID, String title){

        Toolbar myChildToolbar = (Toolbar) activity.findViewById(toolBarID);
        activity.setSupportActionBar(myChildToolbar);

        // fetch the drawable
        final Drawable upArrow = activity.getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(activity.getResources().getColor(R.color.light_grey), PorterDuff.Mode.SRC_ATOP);
        // get the actionbar for this toolbar
        ActionBar ab = activity.getSupportActionBar();
        // set the up arrow
        ab.setHomeAsUpIndicator(upArrow);
        // Set the title
        ab.setTitle(title);
        // enable back button
        ab.setDisplayHomeAsUpEnabled(true);
    }

    public static void setUpTabs(final AppCompatActivity activity, String tabOneIndicator, String tabOneSpec, Class tabOneClass, String tabTwoIndicator, String tabTwoSpec, Class tabTwoClass, Bundle bundle){
        // find tab host and setup
        final FragmentTabHost mTabHost = (FragmentTabHost) activity.findViewById(android.R.id.tabhost);
        mTabHost.setup(activity, activity.getSupportFragmentManager(), R.id.user_tab_content);

        // add the tabs to the tab host
        mTabHost.addTab(mTabHost.newTabSpec(tabOneSpec).setIndicator(tabOneIndicator),
                tabOneClass, bundle);
        mTabHost.addTab(mTabHost.newTabSpec(tabTwoSpec).setIndicator(tabTwoIndicator),
                tabTwoClass, bundle);


        for(int i=0;i < mTabHost.getTabWidget().getChildCount();i++) {
            mTabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(activity, R.color.colorPrimaryDark))));
            TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(activity, R.color.light_grey))));
            tv.setTextSize(18);
            tv.setAllCaps(false);
        }

        mTabHost.getTabWidget().setCurrentTab(1);
        TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(mTabHost.getCurrentTab()).findViewById(android.R.id.title);
        tv.setTextColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(activity, R.color.colorSecondary))));

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                for(int i=0;i < mTabHost.getTabWidget().getChildCount();i++) {
                    TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
                    tv.setTextColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(activity, R.color.light_grey))));
                }
                TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(mTabHost.getCurrentTab()).findViewById(android.R.id.title);
                tv.setTextColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(activity, R.color.colorSecondary))));

            }
        });
    }
}
