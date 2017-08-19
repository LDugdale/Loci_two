package com.lauriedugdale.loci.ui.fragment.social;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.UserDatabase;
import com.lauriedugdale.loci.data.dataobjects.Group;
import com.lauriedugdale.loci.data.dataobjects.User;
import com.lauriedugdale.loci.listeners.GroupDownloadedListener;
import com.lauriedugdale.loci.listeners.UsersDownloadedListener;
import com.lauriedugdale.loci.ui.adapter.social.FriendsAdapter;
import com.lauriedugdale.loci.ui.adapter.social.GroupMembersAdapter;

/**
 * Created by mnt_x on 04/08/2017.
 */

public class GroupProfileMembersFragment extends Fragment implements FriendsAdapter.SocialAdapterOnClickHandler, UsersDownloadedListener {

    private Group mGroup;
    private UserDatabase mUserDatabase;
    private RecyclerView mRecyclerView;
    private GroupMembersAdapter mAdapter;
    private ProgressBar mProgressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserDatabase = new UserDatabase(getActivity());

        Bundle arguments = getArguments();
        mGroup = arguments.getParcelable("group");
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group_profile_members, container, false);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.loading_indicator);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_group_members);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new GroupMembersAdapter(getActivity(), mGroup);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setNestedScrollingEnabled(false);

        mUserDatabase.downloadGroupMembers(mAdapter, mGroup, this);

        return rootView;
    }

    @Override
    public void onSocialClick(User user) {

    }

    @Override
    public void onUsersDownloaded() {
        mProgressBar.setVisibility(View.GONE);
    }
}
