package com.lauriedugdale.loci.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.lauriedugdale.loci.ui.fragment.ChatFragment;
import com.lauriedugdale.loci.ui.fragment.MainFragment;
import com.lauriedugdale.loci.ui.fragment.StatsFragment;

/**
 * Adapter for managing the Main Activity fragments
 * @author Laurie Dugdale
 */

public class MainActivityAdapter extends FragmentPagerAdapter {

    public MainActivityAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ChatFragment.create();
            case 1:
                return MainFragment.create();
            case 2:
                return StatsFragment.create();

        }

        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
