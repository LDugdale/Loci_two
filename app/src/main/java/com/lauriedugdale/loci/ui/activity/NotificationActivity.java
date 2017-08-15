package com.lauriedugdale.loci.ui.activity;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.NotificationDatabase;
import com.lauriedugdale.loci.data.UserDatabase;
import com.lauriedugdale.loci.ui.adapter.notifications.NotificationFriendsAdapter;
import com.lauriedugdale.loci.ui.adapter.notifications.NotificationMainAdapter;
import com.lauriedugdale.loci.utils.InterfaceUtils;

/**
 * Displays the notifications for the user
 *
 * @author Laurie Dugdale
 */
public class NotificationActivity extends AppCompatActivity {

    private NotificationFriendsAdapter mFriendAdapter; // adapter for display friends requests
    private NotificationMainAdapter mNotificationAdapter; // adapter for displaying general notifications
    private RecyclerView mFriendRecyclerView; // recycler view for friend requests
    private RecyclerView mNotificationRecyclerView; // recycler view for general notifications
    private UserDatabase mUserDatabase; // access user section of database
    private NotificationDatabase mNotificationDatabase; // access notification section of database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        mUserDatabase = new UserDatabase(this);
        mNotificationDatabase = new NotificationDatabase(this);
        mFriendAdapter = new NotificationFriendsAdapter(this);
        mNotificationAdapter = new NotificationMainAdapter(this);

        // find recycler views
        mFriendRecyclerView = (RecyclerView) findViewById(R.id.rv_friend_requests);
        mNotificationRecyclerView = (RecyclerView) findViewById(R.id.rv_main_notifications);
        /// set the layoutmanagers for the recyclerviews
        mFriendRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mNotificationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // set the adapters for the recyclerviews
        mFriendRecyclerView.setAdapter(mFriendAdapter);
        mNotificationRecyclerView.setAdapter(mNotificationAdapter);
        // download the initial friend requests if any onCreate
        mUserDatabase.downloadFriendRequests(mFriendAdapter);
        // download the initial notifications if any onCreate
        mNotificationDatabase.downloadNotifications(mNotificationAdapter);

        // reset notification count
        mNotificationDatabase.uploadResetNotificationCount();

        // setup the toolbar
        InterfaceUtils.setUpToolbar(this, R.id.ar_toolbar, "Notifications");
    }
}
