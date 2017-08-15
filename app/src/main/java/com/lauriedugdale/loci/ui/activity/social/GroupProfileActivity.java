package com.lauriedugdale.loci.ui.activity.social;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.EntryDatabase;
import com.lauriedugdale.loci.data.GroupDatabase;
import com.lauriedugdale.loci.ui.fragment.social.GroupProfileEntriesFragment;
import com.lauriedugdale.loci.ui.fragment.social.GroupProfileMembersFragment;
import com.lauriedugdale.loci.data.dataobjects.Group;
import com.lauriedugdale.loci.ui.activity.MainActivity;
import com.lauriedugdale.loci.ui.activity.settings.GroupSettingsActivity;
import com.lauriedugdale.loci.utils.InterfaceUtils;

/**
 * The Activity for the Group Profile handles all the UI logic to display the group profile
 *
 * @author Laurie Dugdale
 */
public class GroupProfileActivity extends AppCompatActivity {

    private static final String TAG = GroupProfileActivity.class.getSimpleName();

    private GroupDatabase mGroupDatabase;

    private Group mGroup;

    private ImageView mGroupImage;
    private TextView mGroupName;
    private ImageView mLocateAll;
    private ImageView mSettings;
    private TextView mJoinGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);

        mGroupDatabase = new GroupDatabase(this);

        // get the GeoEntry to display info on this page
        mGroup = getIntent().getParcelableExtra(Intent.ACTION_OPEN_DOCUMENT);

        // find views
        mGroupImage = (ImageView) findViewById(R.id.group_picture);
        mGroupName = (TextView) findViewById(R.id.profile_group_name);
        mLocateAll = (ImageView) findViewById(R.id.locate_all);
        mSettings = (ImageView) findViewById(R.id.settings_button);
        mJoinGroup = (TextView) findViewById(R.id.add_button);

        // set group name
        mGroupName.setText(mGroup.getGroupName());
        // locate all button
        locateAll();

        // check if group admin, and display settings button if they are
        mGroupDatabase.checkGroupAdmin(mSettings, mGroup.getGroupID(), null);

        InterfaceUtils.setUpToolbar(this, R.id.profile_toolbar, "");

        // set up tabs
        Bundle groupBundle = new Bundle();
        groupBundle.putParcelable("group", mGroup);
        InterfaceUtils.setUpTabs(this, "Entries", "entries", GroupProfileEntriesFragment.class, "Members", "members", GroupProfileMembersFragment.class, groupBundle);

        joinGroup();
        openSettings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGroupImage.setImageURI(null);
        mGroupDatabase.downloadGroupPic(mGroupImage, R.drawable.default_profile, mGroup.getProfilePicturePath());
    }

    /**
     * Listener for the locate all ImageView displays all group posts on the map
     */
    public void locateAll(){

        mLocateAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "locateAll group entries");

                // create intent and action to back to MainActivity to display the markers
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setAction("group_entries");
                intent.putExtra("group", mGroup);
                startActivity(intent);
            }
        });
    }

    /**
     * Listener for the settings button, launches the GroupSettingsActivity
     */
    public void openSettings(){

        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create intent and start GroupSettingsActivity
                Intent intent = new Intent(GroupProfileActivity.this, GroupSettingsActivity.class);
                intent.putExtra(Intent.ACTION_OPEN_DOCUMENT, mGroup);
                startActivity(intent);
            }
        });
    }

    /**
     * handles the join group button
     */
    public void joinGroup(){
        mGroupDatabase.uploadGroupRequest(mJoinGroup, mGroup);

    }
}
