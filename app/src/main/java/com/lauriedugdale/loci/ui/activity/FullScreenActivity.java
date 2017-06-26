package com.lauriedugdale.loci.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;

import java.io.FileInputStream;

public class FullScreenActivity extends AppCompatActivity {

    private ImageView mFullscreenImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);

        mFullscreenImage = (ImageView) findViewById(R.id.fullscreen_image);

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
            DataUtils dataUtils = new DataUtils(this);
            dataUtils.readEntry(mFullscreenImage, geoEntry.getEntryID(), geoEntry.getFilePath());
        }

    }
}
