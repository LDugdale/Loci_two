package com.lauriedugdale.loci.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.data.dataobjects.User;
import com.lauriedugdale.loci.services.GeoFencingService;
import com.lauriedugdale.loci.services.LociLocationService;
import com.lauriedugdale.loci.ui.activity.auth.LoginActivity;
import com.lauriedugdale.loci.ui.activity.social.UserProfileActivity;
import com.lauriedugdale.loci.ui.adapter.MainActivityAdapter;
import com.lauriedugdale.loci.ui.fragment.MainFragment;
import com.lauriedugdale.loci.ui.nav.LociNavView;
import com.lauriedugdale.loci.utils.LocationUtils;

import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout; // the application drawer for the side menu
    private Toolbar mToolbar; // the main toolbar

    private TextView mUsername; // Current logged in username
    private TextView mEmail; // Current logged in email
    private ImageView mMenuProfileImage; // Current logged in profile

    private ViewPager mViewPager;
    private MainActivityAdapter mAdapter;

    private FirebaseAuth mAuth;

    private DataUtils mDataUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDataUtils = new DataUtils(this);

        mAuth = FirebaseAuth.getInstance();

        //find view pager and Adapter for managing fragments
        mViewPager = (ViewPager) findViewById(R.id.am_view_pager);
        mAdapter = new MainActivityAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);

        // find navigation tabs and start listeners
        LociNavView tabsView = (LociNavView) findViewById(R.id.am_loci_tabs);
        tabsView.setUpWithViewPager(mViewPager, this);

        // set initial fragment
        mViewPager.setCurrentItem(1);

        // start geofence service
        Intent intent = new Intent(this, GeoFencingService.class);
        startService(intent);

        // check for intent from geofence notification
        if(getIntent().hasExtra("list")) {
            // get the GeoEntry to display info on this page
            ArrayList<GeoEntry> geofenceEntries = getIntent().getParcelableArrayListExtra("list");
            broadcastLocationsOnReceive(geofenceEntries);
        }

        // Find the toolbar and initialise the navigation drawer
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        initNavigationDrawer();
    }

    /**
     * When the main activity receives a geofence intent from the notifications this method is called
     * to send the MainFragment, the data needed to display the markers or the Entry View.
     * TODO consider using an interface instead of a LocalBroadcast
     * @param geofenceEntries
     */
    public void broadcastLocationsOnReceive(final ArrayList<GeoEntry> geofenceEntries){

        //TODO There is a bug here where sending geofence_entries doesnt work as it fires before the rest of the main fragment ui is loaded. Putting it inside the location on successlistener causes a delay and allows it to work but this is not ideal.
        // Get the last location
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                // If there is only one entry in the list
                if(geofenceEntries.size() == 1) {
                    Log.d(TAG, "Sending geofence single_entry");

                    final GeoEntry entry = geofenceEntries.get(0);
                    if (location != null) {
                        Float distance = LocationUtils.getDistanceInMeters(location.getLatitude(),
                                location.getLongitude(),
                                entry.getLatitude(),
                                entry.getLongitude());
                        // If marker is within distance go direct to the entry
                        if (distance <= LocationUtils.MAXIMUM_DISTANCE){
                            Intent startViewEntryIntent = new Intent(MainActivity.this, LocationUtils.getEntryDestinationClass(entry.getFileType()));
                            startViewEntryIntent.putExtra(Intent.ACTION_OPEN_DOCUMENT, entry);
                            startActivity(startViewEntryIntent);
                        // Else show the marker on the map
                        } else {
                            Intent intent = new Intent("single_entry");
                            intent.putExtra("entry", entry);
                            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                        }
                    }
                // Else if there is more that one entry in the list, pass all the information straight to the main fragment
                } else {
                    Log.d(TAG, "Sending geofence_entries");

                    Intent intent = new Intent("geofence_entries");
                    intent.putExtra("entries", geofenceEntries);
                    LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                }
            }
        });
    }

    /**
     * Getter for ViewPager
     * used by MainFragment to set the page number when a broadcast is received
     * @return
     */
    public ViewPager getViewPager() {
        return mViewPager;
    }

    /**
     * Initialises the navigation drawer and
     */
    public void initNavigationDrawer() {

        NavigationView navigationView = (NavigationView)findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                int id = menuItem.getItemId();
                switch (id){
                    case R.id.logout:
                        mAuth.signOut();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                        break;
                }
                return true;
            }
        });


        View header = navigationView.getHeaderView(0);
        mUsername = (TextView)header.findViewById(R.id.tv_username);
        mEmail = (TextView)header.findViewById(R.id.tv_email);
        mMenuProfileImage = (ImageView) header.findViewById(R.id.menu_profile_image);

        // Fetch the profile picture for the currently logged in user to display in the nav drawer
        mDataUtils.getProfilePic(mMenuProfileImage, R.drawable.default_profile);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users");
        ref.child(mDataUtils.getCurrentUID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    mUsername.setText(user.getUsername());
                    mEmail.setText(user.getEmail());

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,mToolbar,R.string.openDrawer,R.string.closeDrawer){

            @Override
            public void onDrawerClosed(View v){
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
