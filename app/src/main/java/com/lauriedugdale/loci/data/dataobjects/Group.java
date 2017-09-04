package com.lauriedugdale.loci.data.dataobjects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mnt_x on 28/06/2017.
 */

public class Group implements Parcelable{

    private String groupName;
    private String queryGroupName;
    private String groupID;
    private String groupDescription;
    private String profilePicturePath;

    private String privatePublic;
    private String everyoneAdmin;

    public Group(){

    }

    public Group(String groupName) {
        this.groupName = groupName;
        this.queryGroupName = "PUBLIC__" + groupName.toUpperCase();
        everyoneAdmin = "admin";
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public String getQueryGroupName() {
        return queryGroupName;
    }

    public void setQueryGroupName(String queryGroupName) {
        this.queryGroupName = queryGroupName;
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

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public String getPrivatePublic() {
        return privatePublic;
    }

    public void setPrivatePublic(String privatePublic) {

        String query = getQueryGroupName();
        String subString = query.substring(7);

        if (privatePublic.equals("public")){
            setQueryGroupName("PUBLIC__" + subString);
        } else if (privatePublic.equals("private")){
            setQueryGroupName("PRIVATE_" + subString);
        }

        this.privatePublic = privatePublic;
    }

    public String getEveryoneAdmin() {
        return everyoneAdmin;
    }

    public void setEveryoneAdmin(String everyoneAdmin) {
        this.everyoneAdmin = everyoneAdmin;
    }

    /*
     * Parcelable code
     */
    private Group(Parcel in) {
        groupName = in.readString();
        queryGroupName = in.readString();
        groupID = in.readString();
        groupDescription = in.readString();
        profilePicturePath = in.readString();
        privatePublic = in.readString();
        everyoneAdmin = in.readString();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(groupName);
        dest.writeString(queryGroupName);
        dest.writeString(groupID);
        dest.writeString(groupDescription);
        dest.writeString(profilePicturePath);
        dest.writeString(privatePublic);
        dest.writeString(everyoneAdmin);
    }

    public static final Parcelable.Creator<Group> CREATOR
            = new Parcelable.Creator<Group>() {

        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };
}
