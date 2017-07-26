package com.lauriedugdale.loci.ui.activity.social;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lauriedugdale.loci.AccessPermission;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.User;
import com.lauriedugdale.loci.ui.activity.MainActivity;
import com.lauriedugdale.loci.ui.activity.auth.LoginActivity;
import com.lauriedugdale.loci.ui.adapter.FileAdapter;

public class UserProfileActivity extends AppCompatActivity {

    private DataUtils mDataUtils;
    private User mUser;

    private ImageView mProfileImage;
    private TextView mUsername;
    private ImageView mLocateAll;
    private RecyclerView mRecyclerView;
    private FileAdapter mAdapter;
    private TextView mAdd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mDataUtils = new DataUtils(this);

        // get the GeoEntry to display info on this page
        mUser = getIntent().getParcelableExtra(Intent.ACTION_OPEN_DOCUMENT);

        mProfileImage = (ImageView) findViewById(R.id.profile_picture);
        mUsername = (TextView) findViewById(R.id.profile_username);
        mLocateAll = (ImageView) findViewById(R.id.locate_all);
        mAdd = (TextView) findViewById(R.id.add_button);

        mDataUtils.getNonLoggedInProfilePic(mUser.getUserID(), mProfileImage, R.drawable.default_profile);
        mUsername.setText(mUser.getUsername());
        locateAll();

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_user_files);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FileAdapter(this, AccessPermission.VIEWER);
        mRecyclerView.setAdapter(mAdapter);
        mDataUtils.fetchProfileEntries(mAdapter, mUser.getUserID());

        // my_child_toolbar is defined in the layout file
        Toolbar myChildToolbar =
                (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(myChildToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        ab.setTitle("");

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        addFriend();
    }

    public void locateAll(){

        mLocateAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setAction("user_entries");
                intent.putExtra("user_entries", mUser);
                startActivity(intent);
            }
        });
    }

    public void addFriend(){

        mDataUtils.addFriendRequest(mAdd, mUser.getUserID(), true);
    }
}
