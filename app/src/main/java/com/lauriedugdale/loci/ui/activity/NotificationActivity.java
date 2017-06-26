package com.lauriedugdale.loci.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.ui.adapter.SelectFriendsAdapter;

public class NotificationActivity extends AppCompatActivity {

    private SelectFriendsAdapter mFriendAdapter;
    private RecyclerView mFriendRecyclerView;
    private DataUtils mDataUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mDataUtils = new DataUtils(this);

        mFriendRecyclerView = (RecyclerView) findViewById(R.id.rv_friend_requests);
        mFriendRecyclerView.setLayoutManager(mLayoutManager);
        mFriendAdapter = new SelectFriendsAdapter(this);
        mFriendRecyclerView.setAdapter(mFriendAdapter);
        mDataUtils.fetchFriendRequests(mFriendAdapter);

    }
}
