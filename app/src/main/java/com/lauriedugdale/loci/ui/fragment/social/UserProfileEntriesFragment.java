package com.lauriedugdale.loci.ui.fragment.social;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lauriedugdale.loci.AccessPermission;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.EntryDatabase;
import com.lauriedugdale.loci.data.UserDatabase;
import com.lauriedugdale.loci.data.dataobjects.User;
import com.lauriedugdale.loci.ui.adapter.FileAdapter;

/**
 * Created by mnt_x on 04/08/2017.
 */

public class UserProfileEntriesFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private FileAdapter mAdapter;

    private User mUser;

    private EntryDatabase mEntryDatabase;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEntryDatabase = new EntryDatabase(getActivity());

        Bundle arguments = getArguments();
        mUser = arguments.getParcelable("user");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_profile_entries, container, false);


        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_user_files);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FileAdapter(getActivity(), AccessPermission.VIEWER);
        mRecyclerView.setAdapter(mAdapter);
        mEntryDatabase.downloadProfileEntries(mAdapter, mUser.getUserID());
        mRecyclerView.setNestedScrollingEnabled(false);

        return rootView;
    }
}
