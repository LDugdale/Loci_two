package com.lauriedugdale.loci.ui.activity.entry;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.lauriedugdale.loci.data.EntryStorage;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.ui.fragment.EntryFragment;
import com.lauriedugdale.loci.utils.InterfaceUtils;

import java.io.FileOutputStream;
/**
 * called for entries with an Image file
 *
 * @author Laurie Dugdale
 */
public class ImageEntryActivity extends AppCompatActivity implements EntryFragment.OnFragmentInteractionListener  {

    private static final String TAG = ImageEntryActivity.class.getSimpleName();

    private EntryStorage mEntryStorage;
    private GeoEntry mGeoEntry; // stores the GeoEntry for this particular activity
    private ImageView mHeroImage; // The Hero image displaying the image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_entry);

        mEntryStorage = new EntryStorage(this);

        // get the GeoEntry to display info on this page
        mGeoEntry = getIntent().getParcelableExtra(Intent.ACTION_OPEN_DOCUMENT);
        // find view
        mHeroImage = (ImageView) findViewById(R.id.view_entry_hero_image);
        // fetch Image from database and display it
        mEntryStorage.getFilePic(mHeroImage,mGeoEntry);
        // attach the entry fragment EntryFragment
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            Fragment entryFragment = new EntryFragment();
            entryFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, entryFragment).commit();
        }

        InterfaceUtils.setUpToolbar(this, R.id.toolbar, "Entry");

        imageListener();
    }

    /**
     * Set up lister for mHeroImage. On click the full screen activity is launched
     * and the bitmap is passed to it
     */
    public void imageListener(){

        mHeroImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHeroImage.buildDrawingCache();
                Bitmap bitmap = mHeroImage.getDrawingCache();

                try {
                    // write the file
                    String file = "bitmap.png";
                    FileOutputStream stream = openFileOutput(file, Context.MODE_PRIVATE);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    // close the and clean
                    stream.close();
                    bitmap.recycle();
                    // create intent, pass it the image file, and start the activity
                    Intent intent = new Intent(v.getContext(), FullScreenActivity.class);
                    intent.putExtra("image", file);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // clean
                mHeroImage.destroyDrawingCache();
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
