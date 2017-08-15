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
import com.lauriedugdale.loci.ui.fragment.social.GroupProfileEntriesFragment;
import com.lauriedugdale.loci.ui.fragment.social.GroupProfileMembersFragment;
import com.lauriedugdale.loci.ui.fragment.social.GroupsFragment;
import com.lauriedugdale.loci.ui.fragment.social.UserProfileEntriesFragment;
import com.lauriedugdale.loci.ui.fragment.social.UserProfileFriendsFragment;
import com.lauriedugdale.loci.utils.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.User;
import com.lauriedugdale.loci.ui.activity.MainActivity;
import com.lauriedugdale.loci.ui.adapter.FileAdapter;
import com.lauriedugdale.loci.utils.InterfaceUtils;
/**
 * The Activity for the User Profile handles all the UI logic to display the user profile
 *
 * @author Laurie Dugdale
 */
public class UserProfileActivity extends AppCompatActivity {

    private UserDatabase mUserDatabase;
    private User mUser;

    private ImageView mProfileImage;
    private TextView mUsername;
    private TextView mBio;
    private ImageView mLocateAll;
    private TextView mAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mUserDatabase = new UserDatabase(this);

        // get the GeoEntry to display info on this page
        mUser = getIntent().getParcelableExtra(Intent.ACTION_OPEN_DOCUMENT);

        // find the UI elements
        mProfileImage = (ImageView) findViewById(R.id.profile_picture);
        mUsername = (TextView) findViewById(R.id.profile_username);
        mBio = (TextView) findViewById(R.id.profile_bio);
        mLocateAll = (ImageView) findViewById(R.id.locate_all);
        mAdd = (TextView) findViewById(R.id.add_button);

        // download the profile pic for the user
        mUserDatabase.downloadNonLoggedInProfilePic(mUser.getUserID(), mProfileImage, R.drawable.default_profile);
        // set the username
        mUsername.setText(mUser.getUsername());

        // if Bio is empty hide the text view
        if (mUser.getBio() == null){
            mBio.setVisibility(View.GONE);
        // else set the bio text
        } else {
            mBio.setText(mUser.getBio());
        }

        mBio.setText(mUser.getBio());

        // set up the toolbar
        InterfaceUtils.setUpToolbar(this, R.id.profile_toolbar, "");
        // set up tabs
        Bundle userBundle = new Bundle();
        userBundle.putParcelable("user", mUser);
        InterfaceUtils.setUpTabs(this, "Entries", "entries", UserProfileEntriesFragment.class, "Friends", "friends", UserProfileFriendsFragment.class, userBundle);

        locateAll();
        addFriend();
    }

    /**
     * Listener for the locate all ImageView displays all group posts on the map
     */
    public void locateAll(){

        mLocateAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create intent and action to back to MainActivity to display the markers
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setAction("user_entries");
                intent.putExtra("user_entries", mUser);
                startActivity(intent);
            }
        });
    }

    /**
     * handles the add friend button
     */
    public void addFriend(){

        mUserDatabase.uploadFriendRequest(mAdd, mUser.getUserID(), false);
    }
}
