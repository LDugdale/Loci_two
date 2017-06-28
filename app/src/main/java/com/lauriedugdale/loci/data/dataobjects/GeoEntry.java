package com.lauriedugdale.loci.data.dataobjects;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mnt_x on 15/06/2017.
 */

public class GeoEntry implements Parcelable{

    private String creator;
    private String title;
    private String description;
    private double latitude;
    private double longitude;
    private String filePath;
    private int fileType;
    private String entryID;
    private long uploadDate;
    private Bitmap image;

    public GeoEntry() {
        // Default constructor required for calls to DataSnapshot.getValue(File.class)
    }

    private GeoEntry(Parcel in) {
        creator = in.readString();
        title = in.readString();
        description = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        filePath = in.readString();
        fileType = in.readInt();
        entryID = in.readString();
        uploadDate = in.readLong();

    }

    public GeoEntry(String creator, String title, String description, double latitude, double longitude, String filePath, int fileType, long uploadDate) {
        this.creator = creator;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.filePath = filePath;
        this.fileType = fileType;
        this.uploadDate = uploadDate;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getCreator() {
        return creator;
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

    public String getEntryID() {
        return entryID;
    }

    public void setEntryID(String entryID) {
        this.entryID = entryID;
    }

    public long getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(long uploadDate) {
        this.uploadDate = uploadDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(creator);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(filePath);
        dest.writeInt(fileType);
        dest.writeString(entryID);
        dest.writeLong(uploadDate);

    }

    // After implementing the `Parcelable` interface, we need to create the
    // `Parcelable.Creator<MyParcelable> CREATOR` constant for our class;
    // Notice how it has our class specified as its type.
    public static final Parcelable.Creator<GeoEntry> CREATOR
            = new Parcelable.Creator<GeoEntry>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public GeoEntry createFromParcel(Parcel in) {
            return new GeoEntry(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public GeoEntry[] newArray(int size) {
            return new GeoEntry[size];
        }
    };
}
