package com.lauriedugdale.loci.listeners;

import com.lauriedugdale.loci.data.dataobjects.GeoEntry;

/**
 * Created by mnt_x on 12/08/2017.
 */

public interface SingleEntryDownloadListener {

    void onSingleEntryDownloaded(GeoEntry entry);
}
