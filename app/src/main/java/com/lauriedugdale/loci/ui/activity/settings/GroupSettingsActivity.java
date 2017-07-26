package com.lauriedugdale.loci.ui.activity.settings;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.FilterOptions;
import com.lauriedugdale.loci.data.dataobjects.Group;
import com.lauriedugdale.loci.ui.activity.social.CreateGroup;
import com.lauriedugdale.loci.ui.adapter.MapClusterAdapter;
import com.lauriedugdale.loci.ui.adapter.SelectForGroupAdapter;
import com.lauriedugdale.loci.utils.SocialUtils;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class GroupSettingsActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    private DataUtils mDataUtils;

    private Group mGroup;

    private LinearLayout mUploadImage;
    private ImageView mCurrentImage;

    private LinearLayout mSelectAdmins;

    private RadioButton mEveryone;
    private RadioButton mAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDataUtils = new DataUtils(this);
        mGroup = getIntent().getParcelableExtra(Intent.ACTION_OPEN_DOCUMENT);

        mUploadImage = (LinearLayout) findViewById(R.id.upload_image_wrapper);
        mCurrentImage = (ImageView) findViewById(R.id.current_picture);
        mEveryone = (RadioButton) findViewById(R.id.radio_everyone);
        mAdmin = (RadioButton) findViewById(R.id.radio_admin);
        mSelectAdmins = (LinearLayout) findViewById(R.id.select_admins_wrapper);

        if(mGroup.getEveryoneAdmin().equals("admin")){
            mAdmin.setChecked(true);
            mEveryone.setChecked(false);
        } else if (mGroup.getEveryoneAdmin().equals("everyone")){
            mAdmin.setChecked(false);
            mEveryone.setChecked(true);
        }
        mDataUtils.getGroupPic(mCurrentImage, R.drawable.default_profile, mGroup.getProfilePicturePath());

        chooseProfilePicture();
        onSelectAdminClicked();
    }

    public void onSelectAdminClicked(){
        mSelectAdmins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterPopup(v);
            }
        });
    }
    public void showFilterPopup(View anchorView) {

        // TODO check boxes for media types and date pickers to and from dates
        View popupView = getLayoutInflater().inflate(R.layout.popup_choose_admins, null);

        // PopupWindow popupWindow = new PopupWindow(popupView, RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
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

        // connect time UI elements
        RecyclerView recyclerView = (RecyclerView) popupView.findViewById(R.id.rv_admins);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        final SelectForGroupAdapter adapter = new SelectForGroupAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        TextView apply = (TextView) popupView.findViewById(R.id.pca_apply);

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataUtils.changeAdminPermissions(mGroup, adapter.getCheckedItems());
                popupWindow.dismiss();
            }
        });

        mDataUtils.fetchUsersToSelect(adapter, mGroup);
    }

    public void onRadioButtonClicked(View view) {
        // Is button checked
        boolean checked = ((RadioButton) view).isChecked();

        // Check what was clicked
        switch(view.getId()) {
            case R.id.radio_everyone:
                if (checked)
                    mGroup.setEveryoneAdmin("everyone");
                    mDataUtils.changeWhoPosts(mGroup);
                    break;
            case R.id.radio_admin:
                if (checked)
                    mGroup.setEveryoneAdmin("admin");
                    mDataUtils.changeWhoPosts(mGroup);
                    break;
        }
    }

    public void chooseProfilePicture(){

        mUploadImage.setOnClickListener(new View.OnClickListener() {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                InputStream stream = null;
                try {

                    stream = getContentResolver().openInputStream(data.getData());
                    Bitmap bitmap = BitmapFactory.decodeStream(stream);

                    mDataUtils.setNewPofilePicture(mGroup, SocialUtils.postDataToFirebase(this, bitmap));

                    stream.close();
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

    public boolean checkPermission() {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=android.os.Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(GroupSettingsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    android.support.v7.app.AlertDialog.Builder alertBuilder = new android.support.v7.app.AlertDialog.Builder(this);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(GroupSettingsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    android.support.v7.app.AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {

                    ActivityCompat.requestPermissions(GroupSettingsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
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
