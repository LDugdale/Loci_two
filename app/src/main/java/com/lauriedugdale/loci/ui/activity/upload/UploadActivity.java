package com.lauriedugdale.loci.ui.activity.upload;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lauriedugdale.loci.ui.customviews.NoSwipeViewPager;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.dataobjects.Group;
import com.lauriedugdale.loci.ui.adapter.pageradapter.UploadActivityAdapter;
import com.lauriedugdale.loci.ui.fragment.upload.UploadPageOneFragment;
import com.lauriedugdale.loci.utils.InterfaceUtils;

/**
 * This activity holds the fragments for the user to upload an entry
 *
 * @author Laurie Dugdale
 */
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

        InterfaceUtils.setUpToolbar(this, R.id.toolbar, "Post entry");
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
