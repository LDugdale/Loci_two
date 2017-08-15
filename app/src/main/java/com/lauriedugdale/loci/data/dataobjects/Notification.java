package com.lauriedugdale.loci.data.dataobjects;

/**
 * Created by mnt_x on 12/08/2017.
 */

public class Notification {

    private String notificationID;
    private int seen; // 0 if seen 1 if not seen
    private int type;
    private String recepientID;
    private String recepientName;
    private String fromID;
    private String fromName;

    public Notification(){

    }

    public Notification(int seen, int type, String recepientID, String recepientName, String fromID, String fromName) {
        this.seen = seen;
        this.type = type;
        this.recepientID = recepientID;
        this.recepientName = recepientName;
        this.fromID = fromID;
        this.fromName = fromName;
    }

    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
    }

    public String getFromID() {
        return fromID;
    }

    public String getNotificationID() {
        return notificationID;
    }

    public int getSeen() {
        return seen;
    }

    public int getType() {
        return type;
    }

    public String getRecepientID() {
        return recepientID;
    }

    public String getRecepientName() {
        return recepientName;
    }

    public String getFromName() {
        return fromName;
    }

    public void setSeen(int seen) {
        this.seen = seen;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setRecepientID(String recepientID) {
        this.recepientID = recepientID;
    }

    public void setRecepientName(String recepientName) {
        this.recepientName = recepientName;
    }

    public void setFromID(String fromID) {
        this.fromID = fromID;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }
}
