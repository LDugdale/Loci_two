package com.lauriedugdale.loci.data.dataobjects;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mnt_x on 15/06/2017.
 */

public class GeoEntry implements Parcelable{



    private String creator;
    private String creatorName;
    private String title;
    private String description;
    private double latitude;
    private double longitude;
    private double altitude;
    private String filePath;
    private int fileType;
    private String entryID;
    private long uploadDate;
    private Bitmap image;
    private String queryTitle;
    private String groupName;
    private String groupID;
    private int fromWho;
    private int likes;

    public GeoEntry() {
        // Default constructor required for calls to DataSnapshot.getValue(File.class)
    }

    private GeoEntry(Parcel in) {
        creator = in.readString();
        creatorName = in.readString();
        title = in.readString();
        description = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        altitude = in.readDouble();
        filePath = in.readString();
        fileType = in.readInt();
        entryID = in.readString();
        uploadDate = in.readLong();
        queryTitle = in.readString();
        groupName = in.readString();
        groupID = in.readString();
        fromWho = in.readInt();
        likes = in.readInt();

    }

    public GeoEntry(String creator, String creatorName, String title, String description, double latitude, double longitude, double altitude, String filePath, int fileType, long uploadDate, int fromWho) {
        this.creator = creator;
        this.creatorName = creatorName;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.filePath = filePath;
        this.fileType = fileType;
        this.uploadDate = uploadDate;
        this.queryTitle = title.toUpperCase();
        this.fromWho = fromWho;
    }

    public GeoEntry(String creator, String creatorName, String title, String description, double latitude, double longitude, double altitude, String filePath, int fileType, long uploadDate, String groupName, String groupID, int fromWho) {
        this.creator = creator;
        this.creatorName = creatorName;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.filePath = filePath;
        this.fileType = fileType;
        this.uploadDate = uploadDate;
        this.queryTitle = title.toUpperCase();
        this.groupName = groupName;
        this.groupID = groupID;
        this.fromWho = fromWho;
    }

    public String getQueryTitle() {
        return queryTitle;
    }

    public void setQueryTitle(String queryTitle) {
        this.queryTitle = queryTitle;
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

    public double getAltitude() {
        return altitude;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getFromWho() {
        return fromWho;
    }

    public void setFromWho(int fromWho) {
        this.fromWho = fromWho;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(creator);
        dest.writeString(creatorName);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(altitude);
        dest.writeString(filePath);
        dest.writeInt(fileType);
        dest.writeString(entryID);
        dest.writeLong(uploadDate);
        dest.writeString(queryTitle);
        dest.writeString(groupName);
        dest.writeString(groupID);
        dest.writeInt(fromWho);
        dest.writeInt(likes);


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
