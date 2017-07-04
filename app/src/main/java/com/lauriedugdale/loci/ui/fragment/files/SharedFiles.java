package com.lauriedugdale.loci.ui.fragment.files;

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
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.ui.adapter.FileAdapter;

/**
 * Created by mnt_x on 26/06/2017.
 */

public class SharedFiles extends Fragment {

    private FileAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private DataUtils mDataUtils;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shared_files, container, false);
        setHasOptionsMenu(true);

        mDataUtils = new DataUtils(getActivity());
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_files);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FileAdapter(getActivity(), AccessPermission.OWNER);
        mRecyclerView.setAdapter(mAdapter);

        // TODO sort out duplication issue or make singlevalueeventlistner trigger twice when switching between fragments
        mDataUtils.fetchUserSharedFiles(mAdapter);
        return view;
    }
}
