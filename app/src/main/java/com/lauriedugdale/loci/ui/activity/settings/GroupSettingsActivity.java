package com.lauriedugdale.loci.ui.activity.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.GroupDatabase;
import com.lauriedugdale.loci.data.dataobjects.Group;
import com.lauriedugdale.loci.utils.InterfaceUtils;
import com.lauriedugdale.loci.utils.SocialUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * The activity for group settings contains the UI logic
 *
 * @author Laurie Dugdale
 */
public class GroupSettingsActivity extends AppCompatActivity {

    private GroupDatabase mGroupDatabase;

    private Group mGroup; // the current group that the settings belong too

    private LinearLayout mUploadImage; // the group profile wrapper
    private ImageView mCurrentImage;  // group profile image
    private Button mApplySettings; // apply settings button

    // the radio buttons for setting who can post
    private RadioButton mEveryone;
    private RadioButton mAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mGroupDatabase = new GroupDatabase(this);

        // fetch the parcelable extra and set it to mGroup
        mGroup = getIntent().getParcelableExtra(Intent.ACTION_OPEN_DOCUMENT);

        // find the ui elements
        mUploadImage = (LinearLayout) findViewById(R.id.upload_image_wrapper);
        mCurrentImage = (ImageView) findViewById(R.id.current_picture);
        mEveryone = (RadioButton) findViewById(R.id.radio_everyone);
        mAdmin = (RadioButton) findViewById(R.id.radio_admin);
        mApplySettings = (Button) findViewById(R.id.apply_settings_group);

        // check the appropriate check boxes
        if(mGroup.getEveryoneAdmin().equals("admin")){
            mAdmin.setChecked(true);
            mEveryone.setChecked(false);
        } else if (mGroup.getEveryoneAdmin().equals("everyone")){
            mAdmin.setChecked(false);
            mEveryone.setChecked(true);
        }

        // download the current group profile picture
        mGroupDatabase.downloadGroupPic(mCurrentImage, R.drawable.default_profile, mGroup.getProfilePicturePath());

        InterfaceUtils.setUpToolbar(this, R.id.toolbar, "Group settings");

        chooseProfilePicture();
        onApplySettingsClicked();
    }

    /**
     * Set up the listener for the apply settings button
     */
    public void onApplySettingsClicked(){
        mApplySettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

//    public void onSelectAdminClicked(){
//        mSelectAdmins.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showFilterPopup(v);
//            }
//        });
//    }
//    public void showFilterPopup(View anchorView) {
//
//        View popupView = getLayoutInflater().inflate(R.layout.popup_choose_admins, null);
//
//        // PopupWindow popupWindow = new PopupWindow(popupView, RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
//        final PopupWindow popupWindow = new PopupWindow(popupView, RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT , true);
//
//        // If the PopupWindow should be focusable
//        popupWindow.setFocusable(true);
//        // If you need the PopupWindow to dismiss when when touched outside
//        popupWindow.setBackgroundDrawable(new ColorDrawable());
//
//        int location[] = new int[2];
//
//        // Get the View's(the one that was clicked in the Fragment) location
//        anchorView.getLocationOnScreen(location);
//
//        // Using location, the PopupWindow will be displayed right under anchorView
//        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
//
//        // connect time UI elements
//        RecyclerView recyclerView = (RecyclerView) popupView.findViewById(R.id.rv_admins);
//        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(mLayoutManager);
//        final SelectForGroupAdapter adapter = new SelectForGroupAdapter(this);
//        recyclerView.setAdapter(adapter);
//        adapter.notifyDataSetChanged();
//
//        TextView apply = (TextView) popupView.findViewById(R.id.pca_apply);
//
//        apply.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mGroupDatabase.changeAdminPermissions(mGroup, adapter.getCheckedItems());
//                popupWindow.dismiss();
//            }
//        });
//
//        mUserDatabase.downloadUsersToSelect(adapter, mGroup);
//    }

    /**
     * radio button logic
     * method called from the XML layout
     *
     * @param view the radiobutton view
     */
    public void onRadioButtonClicked(View view) {
        // Is button checked
        boolean checked = ((RadioButton) view).isChecked();

        // Check what was clicked
        switch(view.getId()) {
            case R.id.radio_everyone:
                if (checked)
                    mGroup.setEveryoneAdmin("everyone");
                    mGroupDatabase.changeWhoPosts(mGroup);
                    break;
            case R.id.radio_admin:
                if (checked)
                    mGroup.setEveryoneAdmin("admin");
                    mGroupDatabase.changeWhoPosts(mGroup);
                    break;
        }
    }

    /**
     * Setup the listener for the group image, on click the user can select an image for the group
     */
    public void chooseProfilePicture(){

        mUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on click allow user to select file
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
            if (requestCode == 1) {
                // set the upload data to the field variable
                InputStream stream = null;
                try {
                    // create bitmap from intent data
                    stream = getContentResolver().openInputStream(data.getData());
                    Bitmap bitmap = BitmapFactory.decodeStream(stream);
                    // upload the picture to the database
                    mGroupDatabase.uploadNewPofilePicture(mGroup, SocialUtils.postDataToFirebase(this, bitmap));
                    stream.close();

                    // add the bitmap to the image view
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
