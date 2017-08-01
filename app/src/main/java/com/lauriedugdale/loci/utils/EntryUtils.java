package com.lauriedugdale.loci.utils;

import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;

/**
 * Created by mnt_x on 01/08/2017.
 */

public class EntryUtils {

    /**
     * display the appropriate mipmap for the entry type
     */
    public static void getFilePic(final ImageView image, GeoEntry entry) {

        image.setImageResource(chooseImage(entry));
    }

    private static int chooseImage(GeoEntry entry){
        int entryImage;
        switch (entry.getFileType()){
            case DataUtils.IMAGE:
                entryImage = R.mipmap.image_ar_marker;
                break;
            case DataUtils.AUDIO:
                entryImage = R.mipmap.audio_ar_marker;
                break;
            default:
                entryImage = R.mipmap.blank_ar_marker;
                break;
        }
        return entryImage;
    }
}
