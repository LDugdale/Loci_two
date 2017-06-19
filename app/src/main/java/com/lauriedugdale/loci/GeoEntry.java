package com.lauriedugdale.loci;

/**
 * Created by mnt_x on 15/06/2017.
 */

public class GeoEntry {

    private String title;
    private String description;
    private double latitude;
    private double longitude;
    private String filePath;
    private int fileType;
    private boolean anyoneCanSee;

    private String Id;

    public GeoEntry() {
        // Default constructor required for calls to DataSnapshot.getValue(File.class)
    }

    public GeoEntry(String title, String description, double latitude, double longitude, String filePath, int fileType, boolean anyoneCanSee) {
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.filePath = filePath;
        this.fileType = fileType;
        this.anyoneCanSee = anyoneCanSee;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getFileType() {
        return fileType;
    }

    public boolean isAnyoneCanSee() {
        return anyoneCanSee;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }
}
