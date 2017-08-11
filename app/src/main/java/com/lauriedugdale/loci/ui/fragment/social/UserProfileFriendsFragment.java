package com.lauriedugdale.loci.ui.fragment.social;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.UserDatabase;
import com.lauriedugdale.loci.data.dataobjects.User;
import com.lauriedugdale.loci.ui.adapter.social.FriendsAdapter;

/**
 * Created by mnt_x on 04/08/2017.
 */

public class UserProfileFriendsFragment extends Fragment implements FriendsAdapter.SocialAdapterOnClickHandler{

    private RecyclerView mRecyclerView;
    private FriendsAdapter mAdapter;

    private User mUser;

    private UserDatabase mUserDatabase;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserDatabase = new UserDatabase(getActivity());

        Bundle arguments = getArguments();
        mUser = arguments.getParcelable("user");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_profile_friends, container, false);


        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_user_friends);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FriendsAdapter(getActivity(), this, false);
        mRecyclerView.setAdapter(mAdapter);
        mUserDatabase.downloadUserFriendsForProfile(mUser.getUserID(), mAdapter);
        mRecyclerView.setNestedScrollingEnabled(false);


        return rootView;
    }

    @Override
    public void onSocialClick(User user) {

    }
}
