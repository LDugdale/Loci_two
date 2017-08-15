package com.lauriedugdale.loci.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.NotificationDatabase;
import com.lauriedugdale.loci.ui.activity.NotificationActivity;
import com.lauriedugdale.loci.ui.activity.social.CreateGroup;
import com.lauriedugdale.loci.ui.fragment.social.FriendsFragment;
import com.lauriedugdale.loci.ui.fragment.social.GroupsFragment;

/**
 * @author Laurie Dugdale
 */

public class SocialFragment extends BaseFragment {

    private FragmentTabHost mTabHost;

    private NotificationDatabase mNotificationDatabase;

    public static SocialFragment create(){

        return new SocialFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mNotificationDatabase = new NotificationDatabase(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_social, container, false);


        mTabHost = (FragmentTabHost)rootView.findViewById(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.social_tab_content);
        mTabHost.addTab(mTabHost.newTabSpec("friends").setIndicator("Friends"),
                FriendsFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("groups").setIndicator("Groups"),
                GroupsFragment.class, null);

        for(int i=0;i < mTabHost.getTabWidget().getChildCount();i++) {
            mTabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark))));
            TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
                tv.setTextColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getActivity(), R.color.light_grey))));
                tv.setTextSize(18);
                tv.setAllCaps(false);
        }

        mTabHost.getTabWidget().setCurrentTab(1);
        TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(mTabHost.getCurrentTab()).findViewById(android.R.id.title);
            tv.setTextColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getActivity(), R.color.colorSecondary))));

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                for(int i=0;i < mTabHost.getTabWidget().getChildCount();i++) {
                    TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
                        tv.setTextColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getActivity(), R.color.light_grey))));
                }
                TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(mTabHost.getCurrentTab()).findViewById(android.R.id.title);
                       tv.setTextColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(getActivity(), R.color.colorSecondary))));

            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_social;
    }

    @Override
    public void inOnCreateView(View root, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem filterItem = menu.findItem(R.id.action_filter);
        MenuItem locationItem = menu.findItem(R.id.action_location);
        MenuItem arItem = menu.findItem(R.id.action_ar);

        filterItem.setVisible(false);
        locationItem.setVisible(false);
        arItem.setVisible(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem notificationItem = menu.findItem(R.id.action_notification);
        MenuItemCompat.setActionView(notificationItem, R.layout.menu_item_notification);
        RelativeLayout count = (RelativeLayout) MenuItemCompat.getActionView(notificationItem);

        count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NotificationActivity.class);
                startActivity(intent);
            }
        });

        mNotificationDatabase.downloadUnseenNotifications(count);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_notification) {
            Intent intent = new Intent(getActivity(), NotificationActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_add_group) {
            Intent intent = new Intent(getActivity(), CreateGroup.class);
            startActivity(intent);
            return true;
        }

        return false;
    }
}
