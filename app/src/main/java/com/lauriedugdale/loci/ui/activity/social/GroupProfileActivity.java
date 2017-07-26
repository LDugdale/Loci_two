package com.lauriedugdale.loci.ui.activity.social;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lauriedugdale.loci.AccessPermission;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.Group;
import com.lauriedugdale.loci.data.dataobjects.User;
import com.lauriedugdale.loci.ui.activity.MainActivity;
import com.lauriedugdale.loci.ui.activity.settings.GroupSettingsActivity;
import com.lauriedugdale.loci.ui.adapter.FileAdapter;

public class GroupProfileActivity extends AppCompatActivity {

    private static final String TAG = GroupProfileActivity.class.getSimpleName();

    private DataUtils mDataUtils;
    private Group mGroup;

    private ImageView mGroupImage;
    private TextView mGroupName;
    private ImageView mLocateAll;
    private RecyclerView mRecyclerView;
    private FileAdapter mAdapter;
    private ImageView mSettings;
    private TextView mJoinGroup;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);

        mDataUtils = new DataUtils(this);

        // get the GeoEntry to display info on this page
        mGroup = getIntent().getParcelableExtra(Intent.ACTION_OPEN_DOCUMENT);

        mGroupImage = (ImageView) findViewById(R.id.group_picture);
        mGroupName = (TextView) findViewById(R.id.profile_group_name);
        mLocateAll = (ImageView) findViewById(R.id.locate_all);
        mSettings = (ImageView) findViewById(R.id.settings_button);
        mJoinGroup = (TextView) findViewById(R.id.add_button);

        mGroupName.setText(mGroup.getGroupName());
        locateAll();

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_group_files);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FileAdapter(this, AccessPermission.VIEWER);
        mRecyclerView.setAdapter(mAdapter);

        mDataUtils.fetchGroupProfileEntries(mAdapter, mGroup.getGroupID());

        mDataUtils.checkGroupAdmin(mSettings, mGroup.getGroupID());

        // my_child_toolbar is defined in the layout file
        Toolbar myChildToolbar =
                (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(myChildToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        ab.setTitle("");

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        joinGroup();
        openSettings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGroupImage.setImageURI(null);
        mDataUtils.getGroupPic(mGroupImage, R.drawable.default_profile, mGroup.getProfilePicturePath());

    }

    //TODO there is one of these in user and group profiles consider adding to utils class
    public void locateAll(){

        mLocateAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "locateAll group entries");

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setAction("group_entries");
                intent.putExtra("group", mGroup);
                startActivity(intent);
            }
        });
    }

    public void openSettings(){

        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(GroupProfileActivity.this, GroupSettingsActivity.class);
                intent.putExtra(Intent.ACTION_OPEN_DOCUMENT, mGroup);
                startActivity(intent);
            }
        });
    }

    public void joinGroup(){
        mDataUtils.addGroupRequest(mJoinGroup, mGroup);

    }
}
