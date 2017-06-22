package com.lauriedugdale.loci.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.User;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.ui.adapter.SocialAdapter;

import java.util.List;

/**
 * @author Laurie Dugdale
 */

public class ChatFragment extends BaseFragment implements SocialAdapter.SocialAdapterOnClickHandler  {

    private SocialAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private DataUtils mDataUtils;

    public static ChatFragment create(){
        return new ChatFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        mDataUtils = new DataUtils(getActivity());

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_social);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);


        SocialAdapter mAdapter = new SocialAdapter(getActivity(), this);
        mRecyclerView.setAdapter(mAdapter);

        mDataUtils.fetchUserFriends(mAdapter, this);
        return view;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_chat;
    }

    @Override
    public void inOnCreateView(View root, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    }









    @Override
    /**
     * On click method when an item in the recyclerview is clicked this launches the ViewEntryActivity class
     * passes the Uri of the clicked entry
     */
    public void onClick(long date) {

    }
}
