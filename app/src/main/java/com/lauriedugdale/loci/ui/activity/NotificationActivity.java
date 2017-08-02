package com.lauriedugdale.loci.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.UserDatabase;
import com.lauriedugdale.loci.utils.DataUtils;
import com.lauriedugdale.loci.ui.adapter.NotificationFriendsAdapter;

public class NotificationActivity extends AppCompatActivity {

    private NotificationFriendsAdapter mFriendAdapter;
    private RecyclerView mFriendRecyclerView;
    private UserDatabase mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mUserDatabase = new UserDatabase(this);

        mFriendRecyclerView = (RecyclerView) findViewById(R.id.rv_friend_requests);
        mFriendRecyclerView.setLayoutManager(mLayoutManager);
        mFriendAdapter = new NotificationFriendsAdapter(this);
        mFriendRecyclerView.setAdapter(mFriendAdapter);

        mUserDatabase.downloadFriendRequests(mFriendAdapter);

    }
}
