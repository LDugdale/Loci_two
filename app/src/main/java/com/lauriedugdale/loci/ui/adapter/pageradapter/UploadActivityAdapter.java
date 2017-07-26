package com.lauriedugdale.loci.ui.adapter.pageradapter;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.lauriedugdale.loci.data.dataobjects.Group;
import com.lauriedugdale.loci.ui.fragment.upload.UploadPageOneFragment;
import com.lauriedugdale.loci.ui.fragment.upload.UploadPageTwoFragment;

/**
 * Created by mnt_x on 22/07/2017.
 */

public class UploadActivityAdapter extends FragmentPagerAdapter implements UploadPageOneFragment.OnNextButtonClickedListener{

    private UploadPageTwoFragment mPageTwo;

    public UploadActivityAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return UploadPageOneFragment.create();
            case 1:
                mPageTwo =  UploadPageTwoFragment.create();
                return mPageTwo;
        }

        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public void onNextButtonClicked(int permissionType, String title, String description, Uri uploadData, int dataType, Group group) {
        mPageTwo.onNextButtonClicked(permissionType, title, description, uploadData, dataType, group);
    }
}