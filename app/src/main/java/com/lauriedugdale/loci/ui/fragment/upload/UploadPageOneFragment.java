package com.lauriedugdale.loci.ui.fragment.upload;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.GroupDatabase;
import com.lauriedugdale.loci.listeners.GroupDownloadedListener;
import com.lauriedugdale.loci.ui.activity.auth.LoginActivity;
import com.lauriedugdale.loci.utils.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.Group;
import com.lauriedugdale.loci.ui.adapter.FetchGroupsAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by mnt_x on 22/07/2017.
 */

public class UploadPageOneFragment extends Fragment {

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private int REQUEST_CAMERA = 0;
    private int SELECT_FILE = 1;
    private int SELECT_AUDIO = 2;

    // ui elements
    private ImageView mNext;
    private EditText mTitle;
    private EditText mDescription;
    private TextView mViewableSelection;

    // media pickers
    private ImageView mAudioItem;
    private ImageView mImageItem;
    private ImageView mCameraItem;

    // chosen media upload
    private int mChosenTask;
    private LinearLayout mChosenUploadWrapper;
    private ImageView mImageUpload;
    private TextView mUploadTitle;

    private GroupDatabase mGroupDtabase;

    private Uri mUploadData;
    private int mUploadType;

    private int mPermissionType;

    // Recyclerview
    private FetchGroupsAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private Group mSelectedGroup;

    private OnNextButtonClickedListener mCallback;

    public static UploadPageOneFragment create() {
        return new UploadPageOneFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_page_one, container, false);
        mSelectedGroup = new Group("Everyone");

        mGroupDtabase = new GroupDatabase(getActivity());
        mPermissionType = DataUtils.ANYONE;
        mUploadType = DataUtils.NO_MEDIA;

        // find ui elements
        mImageItem = (ImageView) view.findViewById(R.id.au_image_picker);
        mAudioItem = (ImageView) view.findViewById(R.id.au_audio_picker);
        mCameraItem = (ImageView) view.findViewById(R.id.au_camera_picker);
        mNext = (ImageView) view.findViewById(R.id.next_upload_page);
        mTitle = (EditText) view.findViewById(R.id.au_entry_title);
        mDescription = (EditText) view.findViewById(R.id.au_entry_description);
        mViewableSelection = (TextView) view.findViewById(R.id.viewable_selection);
        mChosenUploadWrapper = (LinearLayout) view.findViewById(R.id.upload_info_wrapper);
        mImageUpload = (ImageView) view.findViewById(R.id.uploaded_image);
        mUploadTitle = (TextView) view.findViewById(R.id.filename);

        onViewableSelectionClick();
        selectImage();
        selectAudio();
        selectCamera();
        nextPage();
        return view;
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

    private void nextPage(){

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if user has not entered a title return
                if (mTitle.getText().toString().equals("") || mTitle.getText().toString() == null){
                    Toast.makeText(getActivity(), getString(R.string.empty_title), Toast.LENGTH_LONG).show();
                    return;
                }

                mCallback.onNextButtonClicked(mPermissionType,
                        mTitle.getText().toString(),
                        mDescription.getText().toString(),
                        mUploadData,
                        mUploadType,
                        mSelectedGroup);
            }
        });
    }

    private void selectImage() {
        mImageItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            mChosenTask = SELECT_FILE;
            galleryIntent();
            }
        });
    }

    public void selectAudio(){
        mAudioItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            mChosenTask = SELECT_AUDIO;
            audioIntent();
            }
        });
    }

    public void selectCamera(){
        mCameraItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            mChosenTask = REQUEST_CAMERA;
            cameraIntent();
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

        String path = destination.toURI().toString();


        mUploadData = Uri.parse(path);
        mUploadType = DataUtils.IMAGE;
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        mUploadData = data.getData();
        mUploadType = DataUtils.IMAGE;

        Uri uri = data.getData();
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);

            mImageUpload.setImageBitmap(bitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }

        displayUploadedMedia(getFilename(data), bitmap);
    }

    private void onSelectAudioResult(Intent data) {
        mUploadData = data.getData();
        mUploadType = DataUtils.AUDIO;

        displayUploadedMedia(getFilename(data), null);


    }


    private void displayUploadedMedia(String path, Bitmap bitmap){
        String filename = path.substring(path.lastIndexOf("/")+1);

        mUploadTitle.setText(filename);
        if (bitmap != null) {
            mImageUpload.setImageBitmap(bitmap);
        }
    }

    private String getFilename(Intent data){

        Uri uri = data.getData();
        String uriString = uri.toString();
        File myFile = new File(uriString);
        String displayName = null;

        if (uriString.startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        } else if (uriString.startsWith("file://")) {
            displayName = myFile.getName();
        }

        return  displayName;
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

        final View popupView = getActivity().getLayoutInflater().inflate(R.layout.popup_viewable, null);

        final PopupWindow popupWindow = new PopupWindow(popupView, RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT , true);

        // If the PopupWindow should be focusable
        popupWindow.setFocusable(true);
        // If you need the PopupWindow to dismiss when when touched outside
        popupWindow.setBackgroundDrawable(new ColorDrawable());

        int location[] = new int[2];

        // Get the View's(the one that was clicked in the Fragment) location
        anchorView.getLocationOnScreen(location);

        // Using location, the PopupWindow will be displayed right under anchorView
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);

        final ProgressBar progressBar = (ProgressBar) popupView.findViewById(R.id.loading_indicator);
        TextView done = (TextView) popupView.findViewById(R.id.done);
        View background = popupView.findViewById(R.id.pv_background);

        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        mRecyclerView = (RecyclerView) popupView.findViewById(R.id.rv_select_group);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FetchGroupsAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        mGroupDtabase.downloadUserAcessibleGroups(mAdapter, new GroupDownloadedListener() {
            @Override
            public void onGroupDownloaded() {
                progressBar.setVisibility(View.GONE);
            }
        });

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

    public interface OnNextButtonClickedListener {

        void onNextButtonClicked(int permissionType, String title, String description, Uri uploadData, int dataType, Group group);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = null;

        if (context instanceof Activity){
            activity = (Activity)context;
        }

        mCallback = (OnNextButtonClickedListener) activity;
    }
}
