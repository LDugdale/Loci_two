package com.lauriedugdale.loci.ui.activity.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.UserDatabase;
import com.lauriedugdale.loci.utils.InterfaceUtils;
import com.lauriedugdale.loci.utils.SocialUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Contains the UI logic for the profile settings activity
 *
 * @author Laurie Dugdale
 */
public class ProfileSettingsActivity extends AppCompatActivity {

    private UserDatabase mUserDatabase;

    private LinearLayout mUploadImage;
    private ImageView mCurrentImage;
    private EditText mBio;
    private Button mApplySettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        mUserDatabase = new UserDatabase(this);

        // find ui elements
        mUploadImage = (LinearLayout) findViewById(R.id.upload_image_wrapper);
        mCurrentImage = (ImageView) findViewById(R.id.current_picture);
        mBio = (EditText) findViewById(R.id.profile_bio);
        mApplySettings = (Button) findViewById(R.id.profile_apply);

        // fetch the current profile pic
        mUserDatabase.downloadProfilePic(mCurrentImage, R.drawable.default_profile);
        // fetch the current bio
        mUserDatabase.downloadProfileBio(mBio);

        // setup the toolbar
        InterfaceUtils.setUpToolbar(this, R.id.profile_settings_toolbar, "Profile settings");

        onProfileImageClick();
        onApplyButtonClick();
    }

    /**
     * Set up the listener for the apply button
     * just uploads the bio image is uploaded automatically
     */
    public void onApplyButtonClick(){

        mApplySettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // upload the bio on apply click
                mUserDatabase.uploadProfileBio(mBio.getText().toString());
                finish();
            }
        });
    }

    /**
     * Setup the listener for the group image, on click the user can select an image for the group
     */
    public void onProfileImageClick(){

        mUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryIntent();
            }
        });
    }

    /**
     * create intent for image chooser gallery
     */
    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // if successful
        if (resultCode == Activity.RESULT_OK) {
            // set the upload data to the field variable
            if (requestCode == 1) {
                InputStream stream = null;
                try {
                    // create bitmap from intent data
                    stream = getContentResolver().openInputStream(data.getData());
                    Bitmap bitmap = BitmapFactory.decodeStream(stream);
                    // upload the picture to the database
                    mUserDatabase.uploadNewPofilePicture(SocialUtils.postDataToFirebase(this, bitmap));
                    stream.close();
                    // add the bitmap to the image
                    mCurrentImage.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

                }
            }
        }
    }
}
