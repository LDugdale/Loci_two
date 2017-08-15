package com.lauriedugdale.loci.data.dataobjects;

import com.lauriedugdale.loci.data.NotificationDatabase;

/**
 * Created by mnt_x on 12/08/2017.
 */

public class NotificationComment extends Notification {

    private String commentID;
    private String entryTitle;
    private String entryID;
    private long commentDate;

    public NotificationComment(){

    }

    public NotificationComment(int seen, String recepientID, String recepientName, String fromID, String fromName, String commentID, String entryTitle, String entryID, long commentDate) {
        super(seen, NotificationDatabase.NOTIFICATION_TYPE_COMMENT, recepientID, recepientName, fromID, fromName);
        this.commentID = commentID;
        this.entryTitle = entryTitle;
        this.entryID = entryID;
        this.commentDate = commentDate;
    }

    public String getCommentID() {
        return commentID;
    }

    public String getEntryTitle() {
        return entryTitle;
    }

    public long getCommentDate() {
        return commentDate;
    }

    public String getEntryID() {
        return entryID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }

    public void setEntryTitle(String entryTitle) {
        this.entryTitle = entryTitle;
    }

    public void setEntryID(String entryID) {
        this.entryID = entryID;
    }

    public void setCommentDate(long commentDate) {
        this.commentDate = commentDate;
    }
}
