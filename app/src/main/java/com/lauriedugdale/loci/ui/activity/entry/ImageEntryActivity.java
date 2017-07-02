package com.lauriedugdale.loci.ui.activity.entry;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.ui.activity.FullScreenActivity;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageEntryActivity extends AppCompatActivity {
    public static final String TAG = "ImageEntryActivity";

    private DataUtils mDataUtils;

    private GeoEntry mGeoEntry;

    private TextView mTitle;
    private TextView mDescription;
    private ImageView mHeroImage;

    private ImageView mAuthorPic;
    private TextView mAuthor;
    private TextView mDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_entry);

        mDataUtils = new DataUtils(this);

        // get the GeoEntry to display info on this page
        mGeoEntry = getIntent().getParcelableExtra(Intent.ACTION_OPEN_DOCUMENT);



        mTitle = (TextView) findViewById(R.id.view_entry_title);
        mDescription = (TextView) findViewById(R.id.view_entry_description);
        mHeroImage = (ImageView) findViewById(R.id.view_entry_hero_image);

        // set description and title
        mTitle.setText(mGeoEntry.getTitle());
        mDescription.setText(mGeoEntry.getDescription());

        // fetch Image from database and display it
        mDataUtils.readEntry(mHeroImage, mGeoEntry.getEntryID(), mGeoEntry.getFilePath());
        imageListener();

        authorDetails();

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

    public void authorDetails(){
        mAuthorPic = (ImageView) findViewById(R.id.view_entry_author_pic);
        mAuthor = (TextView) findViewById(R.id.view_entry_author);
        mDate = (TextView) findViewById(R.id.view_entry_date);

        mDataUtils.getNonLoggedInProfilePic(mGeoEntry.getCreator(), mAuthorPic, R.drawable.default_profile);
        mAuthor.setText(mGeoEntry.getCreatorName());

        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.UK).format(new Date( mGeoEntry.getUploadDate()));
        mDate.setText(dateString);

    }
}
