package com.lauriedugdale.loci.ui.activity.upload;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.lauriedugdale.loci.NoSwipeViewPager;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.dataobjects.Group;
import com.lauriedugdale.loci.ui.adapter.pageradapter.UploadActivityAdapter;
import com.lauriedugdale.loci.ui.fragment.upload.UploadPageOneFragment;

public class UploadActivity extends AppCompatActivity implements UploadPageOneFragment.OnNextButtonClickedListener {

    private NoSwipeViewPager mViewPager;
    private UploadActivityAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        //find view pager and Adapter for managing fragments
        mViewPager = (NoSwipeViewPager) findViewById(R.id.au_view_pager);
        mViewPager.setPagingEnabled(false);
        mAdapter = new UploadActivityAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);

        // set initial fragment
        mViewPager.setCurrentItem(0);

        // my_child_toolbar is defined in the layout file
        Toolbar myChildToolbar =
                (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myChildToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.light_grey), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        ab.setTitle("Post entry");

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Method to move to fragment one
     */
    public void switchToFragmentOne(){
        if(mViewPager.getCurrentItem() != 1){
            mViewPager.setCurrentItem(1);
        }
    }

    /**
     * Method to move to fragment two
     */
    public void switchToFragmentTwo(){
        if(mViewPager.getCurrentItem() != 0){
            mViewPager.setCurrentItem(0);
        }
    }

    @Override
    public void onNextButtonClicked(int permissionType, String title, String description, Uri uploadData, int dataType, Group group) {
        mAdapter.onNextButtonClicked(permissionType, title, description, uploadData, dataType, group);
        switchToFragmentOne();
    }
}
