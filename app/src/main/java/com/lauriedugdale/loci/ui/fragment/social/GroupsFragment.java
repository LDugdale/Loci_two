package com.lauriedugdale.loci.ui.fragment.social;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.GroupDatabase;
import com.lauriedugdale.loci.utils.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.Group;
import com.lauriedugdale.loci.ui.activity.social.GroupProfileActivity;
import com.lauriedugdale.loci.ui.adapter.GroupsAdapter;

/**
 * Created by mnt_x on 27/06/2017.
 */

public class GroupsFragment extends Fragment implements GroupsAdapter.GroupAdapterOnClickHandler {

    private GroupsAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private GroupDatabase mGroupDatabase;

    public static GroupsFragment create(){
        return new GroupsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        setHasOptionsMenu(true);

        mGroupDatabase = new GroupDatabase(getActivity());

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_groups);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new GroupsAdapter(getActivity(), this);
        mRecyclerView.setAdapter(mAdapter);

        mGroupDatabase.downloadUserAcessibleGroups(mAdapter);
        return view;
    }


    @Override
    /**
     * On click method when an item in the recyclerview is clicked this launches the ViewEntryActivity class
     * passes the Uri of the clicked entry
     */
    public void onGroupClick(Group group) {
        Intent startViewEntryIntent = new Intent(getActivity(), GroupProfileActivity.class);
        startViewEntryIntent.putExtra(Intent.ACTION_OPEN_DOCUMENT, group);
        getActivity().startActivity(startViewEntryIntent);
    }

}
