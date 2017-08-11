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
import com.lauriedugdale.loci.data.GroupDatabase;
import com.lauriedugdale.loci.data.dataobjects.Group;
import com.lauriedugdale.loci.ui.adapter.FileAdapter;

/**
 * Created by mnt_x on 04/08/2017.
 */

public class GroupProfileEntriesFragment extends Fragment {

    private EntryDatabase mEntryDatabase;
    private RecyclerView mRecyclerView;
    private FileAdapter mAdapter;

    private Group mGroup;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEntryDatabase = new EntryDatabase(getActivity());

        Bundle arguments = getArguments();
        mGroup = arguments.getParcelable("group");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group_profile_entries, container, false);


        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_group_entries);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FileAdapter(getActivity(), AccessPermission.VIEWER);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setNestedScrollingEnabled(false);

        mEntryDatabase.downloadGroupProfileEntries(mAdapter, mGroup.getGroupID());

        return rootView;
    }
}
