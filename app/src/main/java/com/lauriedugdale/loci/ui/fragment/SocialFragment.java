package com.lauriedugdale.loci.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.ui.activity.NotificationActivity;
import com.lauriedugdale.loci.ui.activity.SelectFriend;
import com.lauriedugdale.loci.ui.adapter.SocialAdapter;

/**
 * @author Laurie Dugdale
 */

public class SocialFragment extends BaseFragment implements SocialAdapter.SocialAdapterOnClickHandler  {

    private SocialAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private DataUtils mDataUtils;

    public static SocialFragment create(){
        return new SocialFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_social, container, false);
        setHasOptionsMenu(true);

        mDataUtils = new DataUtils(getActivity());

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_social);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);


        SocialAdapter mAdapter = new SocialAdapter(getActivity(), this);
        mRecyclerView.setAdapter(mAdapter);

        mDataUtils.fetchUserFriends(mAdapter);
        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem filterItem = menu.findItem(R.id.action_filter);
        MenuItem locationItem = menu.findItem(R.id.action_location);
        filterItem.setVisible(false);
        locationItem.setVisible(false);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_social;
    }

    @Override
    public void inOnCreateView(View root, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

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
        return false;
    }

    @Override
    /**
     * On click method when an item in the recyclerview is clicked this launches the ViewEntryActivity class
     * passes the Uri of the clicked entry
     */
    public void onSocialClick(long date) {

    }
}
