package com.lauriedugdale.loci.ui.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.dataobjects.Group;
import com.lauriedugdale.loci.ui.adapter.FetchGroupsAdapter;
import com.lauriedugdale.loci.ui.adapter.SelectForGroupAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class UploadActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private int REQUEST_CAMERA = 0;
    private int SELECT_FILE = 1;
    private int SELECT_AUDIO = 2;

    // ui elements
    private ImageView mDone;
    private EditText mTitle;
    private EditText mDescription;
    private TextView mViewableSelection;

    // media pickers
    private ImageView mAudioItem;
    private ImageView mImageItem;
    private ImageView mCameraItem;

    // chosen media upload
    private int mChosenTask;

    private DataUtils mDataUtils;

    private Uri mUploadData;
    private int mUploadType;

    private int mPermissionType;

    // Recyclerview
    private FetchGroupsAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private Group mSelectedGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        mDataUtils = new DataUtils(this);
        mPermissionType = DataUtils.ANYONE;
        mUploadType = DataUtils.NO_MEDIA;

        // find ui elements
        mImageItem = (ImageView) findViewById(R.id.au_image_picker);
        mAudioItem = (ImageView) findViewById(R.id.au_audio_picker);
        mCameraItem = (ImageView) findViewById(R.id.au_camera_picker);
        mDone = (ImageView) findViewById(R.id.au_done);
        mTitle = (EditText) findViewById(R.id.au_entry_title);
        mDescription = (EditText) findViewById(R.id.au_entry_description);
        mViewableSelection = (TextView) findViewById(R.id.viewable_selection);

        onViewableSelectionClick();
        selectImage();
        selectAudio();
        selectCamera();
        uploadEntry();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(mChosenTask == REQUEST_CAMERA) {
                        cameraIntent();
                    } else if(mChosenTask == SELECT_FILE) {
                        galleryIntent();
                    } else if(mChosenTask == SELECT_AUDIO){
                        audioIntent();
                    }
                } else {
                    //code for deny
                }
                break;
        }
    }

    /**
     * Upload the details that have been entered and return to the main Fragment
     * uses mDataUtils to writeNerFile method to add the data to firebase.
     */
    private void uploadEntry(){
        mDone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mUploadData == null){
                    mDataUtils.writeEntry(
                            mPermissionType,
                            mTitle.getText().toString(),
                            mDescription.getText().toString(),
                            mUploadType,
                            mSelectedGroup.getGroupID()
                    );
                    finish();

                } else {

                    mDataUtils.writeEntryWithFile(
                            mPermissionType,
                            mTitle.getText().toString(),
                            mDescription.getText().toString(),
                            mUploadData,
                            mUploadType,
                            mSelectedGroup.getGroupID()
                    );
                    finish();
                }

            }
        });


    }

    private void selectImage() {
        mImageItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result=checkPermission();
                mChosenTask = SELECT_FILE;
                if(result) {
                    galleryIntent();
                }
            }
        });
    }

    public void selectAudio(){
        mAudioItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result=checkPermission();
                mChosenTask = SELECT_AUDIO;
                if(result) {
                    audioIntent();
                }
            }
        });
    }

    public void selectCamera(){
        mCameraItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result=checkPermission();
                mChosenTask = REQUEST_CAMERA;
                if(result) {
                    cameraIntent();
                }
            }
        });
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    private void audioIntent() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_AUDIO);
    }


    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            } else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult(data);
            } else if (requestCode == SELECT_AUDIO){
                onSelectAudioResult(data);
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mUploadData = Uri.parse(destination.toURI().toString());
        mUploadType = DataUtils.IMAGE;
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        mUploadData = data.getData();
        mUploadType = DataUtils.IMAGE;
    }

    private void onSelectAudioResult(Intent data) {
        mUploadData = data.getData();
        mUploadType = DataUtils.AUDIO;
    }

    public boolean checkPermission() {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=android.os.Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(UploadActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    android.support.v7.app.AlertDialog.Builder alertBuilder = new android.support.v7.app.AlertDialog.Builder(this);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(UploadActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    android.support.v7.app.AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    ActivityCompat.requestPermissions(UploadActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private void onViewableSelectionClick(){
        mViewableSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectFriendsPopup(mTitle);
            }
        });
    }


    public void showSelectFriendsPopup(View anchorView) {


        final View popupView = getLayoutInflater().inflate(R.layout.popup_viewable, null);

//        PopupWindow popupWindow = new PopupWindow(popupView, RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        final PopupWindow popupWindow = new PopupWindow(popupView, RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT , true);

        // If the PopupWindow should be focusable
        popupWindow.setFocusable(true);
        // If you need the PopupWindow to dismiss when when touched outside
        popupWindow.setBackgroundDrawable(new ColorDrawable());

        int location[] = new int[2];

        // Get the View's(the one that was clicked in the Fragment) location
        anchorView.getLocationOnScreen(location);

        // Using location, the PopupWindow will be displayed right under anchorView
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);

        TextView done = (TextView) popupView.findViewById(R.id.done);

        mRecyclerView = (RecyclerView) popupView.findViewById(R.id.rv_select_group);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FetchGroupsAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        mDataUtils.fetchUserGroups(mAdapter);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedGroup = mAdapter.getSelectedGroup();
                String groupName = mSelectedGroup.getGroupName();

                switch(groupName){
                    case "Everyone":
                        mPermissionType = DataUtils.ANYONE;
                        break;
                    case "Friends":
                        mPermissionType = DataUtils.FRIENDS;
                        break;
                    case "Just me":
                        mPermissionType = DataUtils.NO_ONE;
                        break;
                    default:
                        mPermissionType = DataUtils.GROUP;
                        break;
                }

                mViewableSelection.setText(groupName);
                popupWindow.dismiss();
            }
        });
    }
}
