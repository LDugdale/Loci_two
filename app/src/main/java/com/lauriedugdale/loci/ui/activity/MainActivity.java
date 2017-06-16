package com.lauriedugdale.loci.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.services.LociLocationService;
import com.lauriedugdale.loci.ui.adapter.MainActivityAdapter;
import com.lauriedugdale.loci.ui.nav.LociNavView;

import android.support.v4.view.ViewPager;

public class MainActivity extends AppCompatActivity{

    private Location mLocation;
    private LociLocationService mLocationService;
    private boolean isBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find view pager and Adapter for managing fragments
        ViewPager viewPager = (ViewPager) findViewById(R.id.am_view_pager);
        MainActivityAdapter adapter = new MainActivityAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        // find navigation tabs and start listeners
        LociNavView sensedTabsView = (LociNavView) findViewById(R.id.am_sensed_tabs);
        sensedTabsView.setUpWithViewPager(viewPager, this);

        // set initial fragment
        viewPager.setCurrentItem(1);

        // start location service
        Intent intent = new Intent(this, LociLocationService.class);
        bindService(intent, locationConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection locationConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LociLocationService.LocationBinder binder = (LociLocationService.LocationBinder) service;
            mLocationService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

}
