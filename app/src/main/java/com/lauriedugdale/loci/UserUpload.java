package com.lauriedugdale.loci;

/**
 * Created by mnt_x on 15/06/2017.
 */

public class UserUpload {

    public double latitude;
    public double longitude;
    public String filePath;
    public int fileType;
    public boolean anyoneCanSee;

    public UserUpload() {
        // Default constructor required for calls to DataSnapshot.getValue(File.class)
    }

    public UserUpload(double latitude, double longitude, String filePath, int fileType, boolean anyoneCanSee) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.filePath = filePath;
        this.fileType = fileType;
        this.anyoneCanSee = anyoneCanSee;
    }
}
