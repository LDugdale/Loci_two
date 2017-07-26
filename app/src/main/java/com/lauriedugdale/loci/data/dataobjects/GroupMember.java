package com.lauriedugdale.loci.data.dataobjects;

/**
 * Created by mnt_x on 23/07/2017.
 */

public class GroupMember {

    private String userID;
    private String username;

    public GroupMember(String userID, String username) {
        this.userID = userID;
        this.username = username;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
