package com.lauriedugdale.loci.ui.fragment.social;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.data.dataobjects.User;
import com.lauriedugdale.loci.ui.activity.NotificationActivity;
import com.lauriedugdale.loci.ui.activity.social.UserProfileActivity;
import com.lauriedugdale.loci.ui.adapter.SocialAdapter;
import com.lauriedugdale.loci.ui.fragment.SocialFragment;
import com.lauriedugdale.loci.utils.LocationUtils;

/**
 * @author Laurie Dugdale
 */

public class FriendsFragment extends Fragment implements SocialAdapter.SocialAdapterOnClickHandler {

    private SocialAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private DataUtils mDataUtils;

    public static SocialFragment create(){
        return new SocialFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        setHasOptionsMenu(true);

        mDataUtils = new DataUtils(getActivity());

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_friends);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);


        mAdapter = new SocialAdapter(getActivity(), this);
        mRecyclerView.setAdapter(mAdapter);

        mDataUtils.fetchUserFriends(mAdapter);
        return view;
    }


    @Override
    /**
     * On click method when an item in the recyclerview is clicked this launches the ViewEntryActivity class
     * passes the Uri of the clicked entry
     */
    public void onSocialClick(User user) {
        Intent startViewEntryIntent = new Intent(getActivity(), UserProfileActivity.class);
        startViewEntryIntent.putExtra(Intent.ACTION_OPEN_DOCUMENT, user);
        getActivity().startActivity(startViewEntryIntent);
    }
}
