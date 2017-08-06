package com.lauriedugdale.loci.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.ui.fragment.files.Files;
import com.lauriedugdale.loci.ui.fragment.files.SharedFiles;
import com.lauriedugdale.loci.ui.fragment.social.FriendsFragment;
import com.lauriedugdale.loci.ui.fragment.social.GroupsFragment;

public class UserFilesActivity extends AppCompatActivity {

    // TODO consider adding folders or "collections" of entries

    private FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_files);

        // my_child_toolbar is defined in the layout file
        Toolbar myChildToolbar =
                (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myChildToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.light_grey), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        ab.setTitle("Your entries");

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);


        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.user_tab_content);

        mTabHost.addTab(mTabHost.newTabSpec("files").setIndicator("Files"),
                Files.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("sharedfiles").setIndicator("Shared"),
                SharedFiles.class, null);

        for(int i=0;i < mTabHost.getTabWidget().getChildCount();i++) {
            mTabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(this, R.color.colorPrimaryDark))));
            TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(this, R.color.light_grey))));
            tv.setTextSize(18);
            tv.setAllCaps(false);
        }

        mTabHost.getTabWidget().setCurrentTab(1);
        TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(mTabHost.getCurrentTab()).findViewById(android.R.id.title);
        tv.setTextColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(this, R.color.colorSecondary))));

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                for(int i=0;i < mTabHost.getTabWidget().getChildCount();i++) {
                    TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
                    tv.setTextColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(UserFilesActivity.this, R.color.light_grey))));
                }
                TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(mTabHost.getCurrentTab()).findViewById(android.R.id.title);
                tv.setTextColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(UserFilesActivity.this, R.color.colorSecondary))));

            }
        });
    }

}
