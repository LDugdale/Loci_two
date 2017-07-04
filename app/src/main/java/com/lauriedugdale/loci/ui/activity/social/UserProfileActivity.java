package com.lauriedugdale.loci.ui.activity.social;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

        mDataUtils.getProfilePic(mProfileImage, R.drawable.default_profile);
        mUsername.setText(mUser.getUsername());
        locateAll();

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_user_files);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FileAdapter(this, AccessPermission.VIEWER);
        mRecyclerView.setAdapter(mAdapter);
        mDataUtils.fetchProfileEntries(mAdapter, mUser.getUserID());
    }

    public void locateAll(){

        mLocateAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));

            }
        });
    }
}
