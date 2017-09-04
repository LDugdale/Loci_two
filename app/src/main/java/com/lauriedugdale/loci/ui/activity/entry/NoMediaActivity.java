package com.lauriedugdale.loci.ui.activity.entry;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.ui.activity.MainActivity;
import com.lauriedugdale.loci.ui.fragment.EntryFragment;
import com.lauriedugdale.loci.utils.InterfaceUtils;

/**
 * called for entries with no media
 *
 * @author Laurie Dugdale
 */
public class NoMediaActivity extends AppCompatActivity implements EntryFragment.OnFragmentInteractionListener {

    private static final String TAG = NoMediaActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_media);

        // attach the entry fragment EntryFragment
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            Fragment entryFragment = new EntryFragment();
            entryFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, entryFragment).commit();
        }

        // set up the toolbar
        InterfaceUtils.setUpToolbar(this, R.id.toolbar, "Entry");
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }
}