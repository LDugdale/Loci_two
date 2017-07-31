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
            image.setImageResource(R.drawable.default_profile);
        }

    }

}
