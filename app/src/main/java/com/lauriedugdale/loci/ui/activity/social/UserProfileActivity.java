package com.lauriedugdale.loci.ui.activity.social;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.lauriedugdale.loci.AccessPermission;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.EntryDatabase;
import com.lauriedugdale.loci.data.UserDatabase;
import com.lauriedugdale.loci.ui.fragment.social.FriendsFragment;
import com.lauriedugdale.loci.ui.fragment.social.GroupsFragment;
import com.lauriedugdale.loci.ui.fragment.social.UserProfileEntriesFragment;
import com.lauriedugdale.loci.utils.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.User;
import com.lauriedugdale.loci.ui.activity.MainActivity;
import com.lauriedugdale.loci.ui.adapter.FileAdapter;

public class UserProfileActivity extends AppCompatActivity {

    private UserDatabase mUserDatabase;
    private EntryDatabase mEntryDatabase;
    private User mUser;

    private ImageView mProfileImage;
    private TextView mUsername;
    private TextView mBio;
    private ImageView mLocateAll;
    private RecyclerView mRecyclerView;
    private FileAdapter mAdapter;
    private TextView mAdd;

    private FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mEntryDatabase = new EntryDatabase(this);
        mUserDatabase = new UserDatabase(this);

        // get the GeoEntry to display info on this page
        mUser = getIntent().getParcelableExtra(Intent.ACTION_OPEN_DOCUMENT);

        mProfileImage = (ImageView) findViewById(R.id.profile_picture);
        mUsername = (TextView) findViewById(R.id.profile_username);
        mBio = (TextView) findViewById(R.id.profile_bio);
        mLocateAll = (ImageView) findViewById(R.id.locate_all);
        mAdd = (TextView) findViewById(R.id.add_button);

        mUserDatabase.downloadNonLoggedInProfilePic(mUser.getUserID(), mProfileImage, R.drawable.default_profile);
        mUsername.setText(mUser.getUsername());
        mBio.setText(mUser.getBio());
        locateAll();

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

    private void setUpTabs(){

        Bundle userBundle = new Bundle();
        userBundle.putParcelable("user", mUser);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.user_profile_tab_content);
        mTabHost.addTab(mTabHost.newTabSpec("friends").setIndicator("Friends"),
                FriendsFragment.class, userBundle);
        mTabHost.addTab(mTabHost.newTabSpec("entries").setIndicator("Entries"),
                UserProfileEntriesFragment.class, userBundle);

        for(int i=0;i < mTabHost.getTabWidget().getChildCount();i++) {
            mTabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(this, R.color.colorPrimaryDark))));
            TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(this, R.color.light_grey))));
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
                    tv.setTextColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(UserProfileActivity.this, R.color.light_grey))));
                }
                TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(mTabHost.getCurrentTab()).findViewById(android.R.id.title);
                tv.setTextColor(Color.parseColor("#"+Integer.toHexString(ContextCompat.getColor(UserProfileActivity.this, R.color.colorSecondary))));

            }
        });
    }

    public void addFriend(){

        mUserDatabase.uploadFriendRequest(mAdd, mUser.getUserID(), false);
    }
}
