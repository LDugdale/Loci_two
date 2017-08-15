package com.lauriedugdale.loci.ui.activity.entry;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.lauriedugdale.loci.data.EntryDatabase;
import com.lauriedugdale.loci.data.EntryStorage;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.ui.fragment.EntryFragment;
import com.lauriedugdale.loci.utils.DataUtils;

import java.io.FileInputStream;

/**
 * This activity is used for displaying the full screen image
 *
 * @author Laurie Dugdale
 */
public class FullScreenActivity extends AppCompatActivity implements EntryFragment.OnFragmentInteractionListener {

    private static final String TAG = FullScreenActivity.class.getSimpleName();

    private ImageView mFullscreenImage; // displays the main image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);

        // find view
        mFullscreenImage = (ImageView) findViewById(R.id.fullscreen_image);

        // if there is an extra with the image tag
        if(getIntent().hasExtra("image")){
            // add the bitmap to the ImageView
            Bitmap bmp = null;
            String filename = getIntent().getStringExtra("image");
            try {
                FileInputStream is = this.openFileInput(filename);
                bmp = BitmapFactory.decodeStream(is);
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mFullscreenImage.setImageBitmap(bmp);

        } else if (getIntent().hasExtra(Intent.ACTION_OPEN_DOCUMENT)) {
            // get the GeoEntry to display info on this page
            GeoEntry geoEntry = getIntent().getParcelableExtra(Intent.ACTION_OPEN_DOCUMENT);
            EntryStorage entryStorage = new EntryStorage(this);
            entryStorage.getFilePic(mFullscreenImage, geoEntry);
        }

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
