package com.lauriedugdale.loci.ui.activity.upload;

import android.net.Uri;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.dataobjects.Group;
import com.lauriedugdale.loci.ui.adapter.pageradapter.UploadActivityAdapter;
import com.lauriedugdale.loci.ui.fragment.upload.UploadPageOneFragment;

public class UploadActivity extends AppCompatActivity implements UploadPageOneFragment.OnNextButtonClickedListener {

    private ViewPager mViewPager;
    private UploadActivityAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        //find view pager and Adapter for managing fragments
        mViewPager = (ViewPager) findViewById(R.id.au_view_pager);
        mAdapter = new UploadActivityAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);

        // set initial fragment
        mViewPager.setCurrentItem(0);
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
