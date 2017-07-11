package com.lauriedugdale.loci.ui.activity.entry;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.ui.activity.FullScreenActivity;
import com.lauriedugdale.loci.ui.fragment.EntryFragment;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageEntryActivity extends AppCompatActivity implements EntryFragment.OnFragmentInteractionListener  {
    public static final String TAG = "ImageEntryActivity";

    private DataUtils mDataUtils;

    private GeoEntry mGeoEntry;

    private ImageView mHeroImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_entry);

        mDataUtils = new DataUtils(this);

        // get the GeoEntry to display info on this page
        mGeoEntry = getIntent().getParcelableExtra(Intent.ACTION_OPEN_DOCUMENT);

        mHeroImage = (ImageView) findViewById(R.id.view_entry_hero_image);

        // fetch Image from database and display it
        mDataUtils.readEntry(mHeroImage, mGeoEntry.getEntryID(), mGeoEntry.getFilePath());
        imageListener();

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            Fragment entryFragment = new EntryFragment();

            entryFragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, entryFragment).commit();
        }
    }

    public void imageListener(){

        mHeroImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mHeroImage.buildDrawingCache();
                Bitmap bitmap = mHeroImage.getDrawingCache();

                try {
                    //Write file
                    String filename = "bitmap.png";
                    FileOutputStream stream = openFileOutput(filename, Context.MODE_PRIVATE);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                    //Cleanup
                    stream.close();
                    bitmap.recycle();

                    //Pop intent
                    Intent in1 = new Intent(v.getContext(), FullScreenActivity.class);
                    in1.putExtra("image", filename);
                    startActivity(in1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mHeroImage.destroyDrawingCache();
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
