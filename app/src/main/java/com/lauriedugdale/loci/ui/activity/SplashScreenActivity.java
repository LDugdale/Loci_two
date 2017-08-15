package com.lauriedugdale.loci.ui.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.ui.activity.auth.LoginActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The initial splash screen for the app
 * Used for checking initial permissions have been granted and promting for them if not
 * TODO consider caching information here initiially
 *
 * @author Laurie Dugdale
 */
public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = SplashScreenActivity.class.getSimpleName();

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 900; // constant to request multiple permissions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // If all permissions have been granted launch the LoginActivity
        if(checkRequestPermissions()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Performs a series of checks to see if permissions have been granted, if not we request permissions
     *
     * @return true if permissions have been granted
     */
    private boolean checkRequestPermissions() {

        Log.d(TAG, "Checking permissions");

        List<String> listPermissionsNeeded = new ArrayList<>();

        // collect the int values of the permission checks
        int readExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);


        // locationPermission has not been granted add to list
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        // readExternalStoragePermission has not been granted add to list
        if (readExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        // cameraPermission has not been granted add to list
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        // if list is not empty there is at least one permission required from the user
        if (!listPermissionsNeeded.isEmpty()) {
            // request permissions from user
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            // return false as permissions were required
            return false;
        }

        // if all checks fail return true as we already have the necessary permissions
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        Log.d(TAG, "Permission request callback");

        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                // create map and add permissions
                Map<String, Integer> perms = new HashMap<>();
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);

                // if grantResults contains... results
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // if all permissions have been granted
                    if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "permission granted");
                        // Launch LoginActivity
                        Intent intent = new Intent(this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.d(TAG, "permission was not granted");
                        // if permission has not been granted
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

                                showDialogOK("These Permissions are required for this app", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:
                                                        checkRequestPermissions();
                                                        break;
                                                    case DialogInterface.BUTTON_NEGATIVE:
                                                        // TODO handle cancelling permission, perhaps close the app
                                                        break;
                                                }
                                            }
                                        });
                        }
                        // permission has been denied, and do not ask again has been checked
                        else {
                            Toast.makeText(this, "Please enable permissions in settings", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        }
    }

    /**
     * Helper method to display dialog
     *
     * @param message
     * @param okListener
     */
    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }
}
