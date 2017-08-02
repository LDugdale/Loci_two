package com.lauriedugdale.loci.data;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.utils.DataUtils;

/**
 * Created by mnt_x on 29/07/2017.
 */

public class EntryStorage {

    private Context mContext;
    private FirebaseStorage mStorage;

    public EntryStorage(Context mContext) {
        this.mContext = mContext;
        mStorage = FirebaseStorage.getInstance();
    }

    /**
     * For adding a bitmap from firebase to an imageview
     */
    public void getFilePic(final ImageView image, GeoEntry entry) {

        Uri filePath = Uri.parse(entry.getFilePath());

        if(entry.getFileType() == DataUtils.IMAGE){

            StorageReference storageRef = mStorage.getReferenceFromUrl(filePath.toString());
            Glide.with(mContext)
                    .using(new FirebaseImageLoader())
                    .load(storageRef)
                    .into(image);
        } else {
            image.setImageResource(chooseImage(entry));
        }
    }

    private int chooseImage(GeoEntry entry){
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
