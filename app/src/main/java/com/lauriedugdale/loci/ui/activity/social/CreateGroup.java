package com.lauriedugdale.loci.ui.activity.social;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.GroupDatabase;
import com.lauriedugdale.loci.data.UserDatabase;
import com.lauriedugdale.loci.ui.adapter.SelectForGroupAdapter;
import com.lauriedugdale.loci.utils.InterfaceUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
/**
 * Responsible for allowing the user to create a group
 *
 * @author Laurie Dugdale
 */
public class CreateGroup extends AppCompatActivity {

    // Recyclerview
    private SelectForGroupAdapter mAdapter;
    private RecyclerView mRecyclerView;

    // the required database methods
    private GroupDatabase mGroupDatabase;
    private UserDatabase mUserDatabase;

    // UI elements
    private ImageView mGroupImage;
    private EditText mGroupName;
    private ImageView mNextButton;
    private Uri mUploadData;
    private Bitmap mBitmap;
    private CheckBox mPrivateGroup;

    // true if the group is private
    private Boolean isPrivate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        mGroupDatabase = new GroupDatabase(this);
        mUserDatabase = new UserDatabase(this);

        isPrivate = false;

        // connect UI elements
        mGroupImage = (ImageView) findViewById(R.id.add_group_profile_pic);
        mGroupName = (EditText) findViewById(R.id.enter_group_name);
        mNextButton = (ImageView) findViewById(R.id.next_group_page);
        mPrivateGroup = (CheckBox) findViewById(R.id.cg_private_checkbox);

        // setup the recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_select_for_group);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SelectForGroupAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        // download user friend data for the recyclerview
        mUserDatabase.downloadUserFriends(mAdapter);

        InterfaceUtils.setUpToolbar(this, R.id.toolbar, "Create group");

        groupImageChoose();
        onDonButtonClicked();
        privateCheckbox();
    }

    /**
     * Setup the listener for the group image, on click the user can select an image for the group
     */
    public void groupImageChoose(){
        mGroupImage.setOnClickListener(new View.OnClickListener() {
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

    public void privateCheckbox(){
        mPrivateGroup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if (isChecked){
                isPrivate = true;
            } else {
                isPrivate = false;
            }
            }
        });
    }


    /**
     * Handles the click even for the done button
     */
    public void onDonButtonClicked(){

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // if group name is empty do not allow user to proceed
                if(mGroupName.getText().toString().length() < 1 ){
                    return;
                }

                // if upload data is null upload group without a pic
                if (mUploadData == null){
                    mGroupDatabase.uploadGroupWithoutPic(mAdapter.getCheckedItems(),
                            mGroupName.getText().toString(),
                            isPrivate);
                // else upload with a pic
                } else {
                    mGroupDatabase.uploadGroupWithPic(mAdapter.getCheckedItems(),
                            mGroupName.getText().toString(),
                            mUploadData,
                            isPrivate);
                }
                // finish the activity
                finish();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // if successful
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                // set the upload data to the field variable
                mUploadData = data.getData();
                InputStream stream = null;
                try {
                    // create bitmap from intent data
                    stream = getContentResolver().openInputStream(data.getData());
                    mBitmap = BitmapFactory.decodeStream(stream);
                    stream.close();
                    // add the bitmap to the image
                    mGroupImage.setImageBitmap(mBitmap);
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
