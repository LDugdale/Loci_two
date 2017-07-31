package com.lauriedugdale.loci.data.dataobjects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mnt_x on 31/07/2017.
 */

public class BusStop implements Parcelable{

    private String atcoCode;
    private String name;
    private String locality;
    private double latitude;
    private double longitude;
    private float distance;

    public BusStop(String atcoCode, String name, String locality, double latitude, double longitude, float distance) {
        this.atcoCode = atcoCode;
        this.name = name;
        this.locality = locality;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }

    public String getAtcoCode() {
        return atcoCode;
    }

    public void setAtcoCode(String atcoCode) {
        this.atcoCode = atcoCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }


    /*
     * Parcelable code
     */
    private BusStop(Parcel in) {
        atcoCode = in.readString();
        name = in.readString();
        locality = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        distance = in.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(atcoCode);
        dest.writeString(name);
        dest.writeString(locality);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeFloat(distance);
    }

    public static final Parcelable.Creator<BusStop> CREATOR
            = new Parcelable.Creator<BusStop>() {

        @Override
        public BusStop createFromParcel(Parcel in) {
            return new BusStop(in);
        }

        @Override
        public BusStop[] newArray(int size) {
            return new BusStop[size];
        }
    };
}
