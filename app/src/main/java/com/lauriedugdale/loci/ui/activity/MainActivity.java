package com.lauriedugdale.loci.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Calendar;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.User;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.services.LociLocationService;
import com.lauriedugdale.loci.ui.activity.auth.LoginActivity;
import com.lauriedugdale.loci.ui.adapter.MainActivityAdapter;
import com.lauriedugdale.loci.ui.adapter.SocialAdapter;
import com.lauriedugdale.loci.ui.fragment.ChatFragment;
import com.lauriedugdale.loci.ui.nav.LociNavView;
import com.lauriedugdale.loci.ui.nav.LociTitleView;

import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;



    private Context mContext = this;

    private FirebaseAuth mAuth;

    private DataUtils mDataUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss", Locale.UK);
        String currentDateandTime = sdf.format(c.getTime());
        Long dateInLong = 0L;
        try {
            Date date = sdf.parse(currentDateandTime);
            dateInLong = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }





        Date date = new Date(dateInLong);
        Format format = new SimpleDateFormat("yyyyMMdd HHmmss", Locale.UK);

        format.format(date);





        mAuth = FirebaseAuth.getInstance();

        //find view pager and Adapter for managing fragments
        ViewPager viewPager = (ViewPager) findViewById(R.id.am_view_pager);
        MainActivityAdapter adapter = new MainActivityAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        // find navigation tabs and start listeners
        LociNavView tabsView = (LociNavView) findViewById(R.id.am_loci_tabs);
        tabsView.setUpWithViewPager(viewPager, this);

        // set initial fragment
        viewPager.setCurrentItem(1);

        // start location service
        Intent intent = new Intent(this, LociLocationService.class);
        startService(intent);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        initNavigationDrawer();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            public void setUpWithViewPager(final ViewPager viewPager, final Context context) {
                viewPager.addOnPageChangeListener(this);
            }
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                System.out.println((int) (positionOffset * 100));
                if (position == 0) {
                    mToolbar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.light_grey));
                    mToolbar.getBackground().setAlpha((int) (positionOffset * 100));
                } else if (position == 1){
                     mToolbar.getBackground().setAlpha(0);
                } else if (position == 2){
                    mToolbar.setBackgroundColor(ContextCompat.getColor(mContext, R.color.light_grey));

                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void initNavigationDrawer() {

        NavigationView navigationView = (NavigationView)findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                int id = menuItem.getItemId();

                switch (id){
                    case R.id.home:
                        Toast.makeText(getApplicationContext(),"Home",Toast.LENGTH_SHORT).show();
                        mDrawerLayout.closeDrawers();
                        break;
                    case R.id.settings:
                        Toast.makeText(getApplicationContext(),"Settings",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.trash:
                        Toast.makeText(getApplicationContext(),"Trash",Toast.LENGTH_SHORT).show();
                        mDrawerLayout.closeDrawers();
                        break;
                    case R.id.logout:
                        mAuth.signOut();

                        // this listener will be called when there is change in firebase user session
                        FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
                            @Override
                            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                if (user == null) {
                                    // user auth state is changed - user is null
                                    // launch login activity
                                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                }
                            }
                        };
//                        finish();
                        break;

                }
                return true;
            }
        });


        View header = navigationView.getHeaderView(0);
        TextView tv_email = (TextView)header.findViewById(R.id.tv_email);
        tv_email.setText("raj.amalw@learn2crack.com");
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_friend) {
            Intent intent = new Intent(this, SelectFriend.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
