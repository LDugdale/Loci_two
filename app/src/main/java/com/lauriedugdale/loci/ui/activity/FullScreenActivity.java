package com.lauriedugdale.loci.ui.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.lauriedugdale.loci.R;

import java.io.FileInputStream;

public class FullScreenActivity extends AppCompatActivity {

    private ImageView mFullscreenImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);

        mFullscreenImage = (ImageView) findViewById(R.id.fullscreen_image);

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

    }
}
