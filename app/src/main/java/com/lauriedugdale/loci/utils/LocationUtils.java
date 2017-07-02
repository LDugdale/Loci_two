package com.lauriedugdale.loci.utils;

import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.ui.activity.entry.AudioEntryActivity;
import com.lauriedugdale.loci.ui.activity.entry.ImageEntryActivity;
import com.lauriedugdale.loci.ui.activity.entry.NoMediaActivity;

/**
 * @author Laurie Dugdale
 */

public class LocationUtils {

    public static Class getEntryDestinationClass(int entryType){
        Class destination = null;
        switch(entryType){
            case DataUtils.IMAGE:
                destination = ImageEntryActivity.class;
                break;
            case DataUtils.AUDIO:
                destination = AudioEntryActivity.class;
                break;
            default:
                destination = NoMediaActivity.class;

                break;
        }
        return destination;
    }
}
