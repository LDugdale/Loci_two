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

public class GroupProfileActivity extends AppCompatActivity {

    private static final String TAG = GroupProfileActivity.class.getSimpleName();

    private GroupDatabase mGroupDatabase;
    private EntryDatabase mEntryDatabase;

    private Group mGroup;

    private ImageView mGroupImage;
    private TextView mGroupName;
    private ImageView mLocateAll;
    private ImageView mSettings;
    private TextView mJoinGroup;

    private FragmentTabHost mTabHost;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);

        mGroupDatabase = new GroupDatabase(this);
        mEntryDatabase = new EntryDatabase(this);

        // get the GeoEntry to display info on this page
        mGroup = getIntent().getParcelableExtra(Intent.ACTION_OPEN_DOCUMENT);

        mGroupImage = (ImageView) findViewById(R.id.group_picture);
        mGroupName = (TextView) findViewById(R.id.profile_group_name);
        mLocateAll = (ImageView) findViewById(R.id.locate_all);
        mSettings = (ImageView) findViewById(R.id.settings_button);
        mJoinGroup = (TextView) findViewById(R.id.add_button);

        mGroupName.setText(mGroup.getGroupName());
        locateAll();

        mGroupDatabase.checkGroupAdmin(mSettings, mGroup.getGroupID());

        // my_child_toolbar is defined in the layout file
        Toolbar myChildToolbar =
                (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(myChildToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        ab.setTitle("");

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.light_grey), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);


        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        setUpTabs();
        joinGroup();
        openSettings();
    }

    private void setUpTabs() {

        Bundle groupBundle = new Bundle();
        groupBundle.putParcelable("group", mGroup);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.user_profile_tab_content);
        mTabHost.addTab(mTabHost.newTabSpec("entries").setIndicator("Entries"),
                GroupProfileEntriesFragment.class, groupBundle);
        mTabHost.addTab(mTabHost.newTabSpec("members").setIndicator("Members"),
                GroupProfileMembersFragment.class, groupBundle);

        for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {
            mTabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorPrimaryDark))));
            TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(this, R.color.light_grey))));
            tv.setTextSize(18);
            tv.setAllCaps(false);
        }

        mTabHost.getTabWidget().setCurrentTab(1);
        TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(mTabHost.getCurrentTab()).findViewById(android.R.id.title);
        tv.setTextColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(this, R.color.colorSecondary))));

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                for(int i=0;i < mTabHost.getTabWidget().getChildCount();i++) {
                    TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
                    tv.setTextColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(GroupProfileActivity.this, R.color.light_grey))));
                }
                TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(mTabHost.getCurrentTab()).findViewById(android.R.id.title);
                tv.setTextColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(GroupProfileActivity.this, R.color.colorSecondary))));

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGroupImage.setImageURI(null);
        mGroupDatabase.downloadGroupPic(mGroupImage, R.drawable.default_profile, mGroup.getProfilePicturePath());

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
        mGroupDatabase.uploadGroupRequest(mJoinGroup, mGroup);

    }
}
