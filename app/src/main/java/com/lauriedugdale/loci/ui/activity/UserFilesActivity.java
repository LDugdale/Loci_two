package com.lauriedugdale.loci.ui.activity;

import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.ui.fragment.files.Files;
import com.lauriedugdale.loci.ui.fragment.files.SharedFiles;

public class UserFilesActivity extends AppCompatActivity {

    // TODO consider adding folders or "collections" of entries

    private FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_files);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        mTabHost.addTab(mTabHost.newTabSpec("files").setIndicator("Files"),
                Files.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("sharedfiles").setIndicator("Shared"),
                SharedFiles.class, null);



        // my_child_toolbar is defined in the layout file
        Toolbar myChildToolbar =
                (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myChildToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        ab.setTitle("Your entries");

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
    }
}
