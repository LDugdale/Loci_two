package com.lauriedugdale.loci.ui.activity.social;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.ui.adapter.SelectForGroupAdapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class CreateGroup extends AppCompatActivity {

    //TODO create permissions class
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    // Recyclerview
    private SelectForGroupAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private DataUtils mDataUtils;

    // UI elements
    private ImageView mGroupImage;
    private EditText mGroupName;
    private ImageView mNextButton;
    private Uri mUploadData;
    private Bitmap mBitmap;
    private CheckBox mPrivateGroup;

    private Boolean isPrivate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        mDataUtils = new DataUtils(this);
        isPrivate = false;

        // connect UI elements
        mGroupImage = (ImageView) findViewById(R.id.add_group_profile_pic);
        mGroupName = (EditText) findViewById(R.id.enter_group_name);
        mNextButton = (ImageView) findViewById(R.id.next_group_page);
        mPrivateGroup = (CheckBox) findViewById(R.id.cg_private_checkbox);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_select_for_group);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SelectForGroupAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        mDataUtils.fetchUserFriends(mAdapter);

        groupImageChoose();
        nextButton();
        privateCheckbox();
    }

    public void groupImageChoose(){
        mGroupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = checkPermission();
                if(result) {
                    galleryIntent();
                }
            }
        });
    }

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

    public void nextButton(){

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            if(mGroupName.getText().toString().length() < 1 ){
                return;
            }

            if (mUploadData == null){
                mDataUtils.createGroupWithoutPic(mAdapter.getCheckedItems(),
                        mGroupName.getText().toString(),
                        isPrivate);
            } else {
                mDataUtils.createGroupWithPic(mAdapter.getCheckedItems(),
                        mGroupName.getText().toString(),
                        mUploadData,
                        isPrivate);
            }
            finish();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                mUploadData = data.getData();
                InputStream stream = null;
                try {

                    stream = getContentResolver().openInputStream(data.getData());
                    mBitmap = BitmapFactory.decodeStream(stream);
                    stream.close();
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

    public boolean checkPermission() {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=android.os.Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(CreateGroup.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    android.support.v7.app.AlertDialog.Builder alertBuilder = new android.support.v7.app.AlertDialog.Builder(this);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(CreateGroup.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    android.support.v7.app.AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {

                    ActivityCompat.requestPermissions(CreateGroup.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}
