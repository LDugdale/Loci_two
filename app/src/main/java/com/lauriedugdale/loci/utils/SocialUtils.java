package com.lauriedugdale.loci.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;

/**
 * Created by mnt_x on 23/07/2017.
 */

public class SocialUtils {

    public static final int VIEWER = 10;
    public static final int ADMIN = 11;
    public static final int CREATOR = 12;
    public static final int EVERYONE_POSTS = 100;


    public static Uri postDataToFirebase(Context context, Bitmap bitmap) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "imagePath", null);
        Uri uri = Uri.parse(path);

        return uri;
    }
}
