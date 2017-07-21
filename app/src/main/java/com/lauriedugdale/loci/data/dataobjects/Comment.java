package com.lauriedugdale.loci.data.dataobjects;

/**
 * Created by mnt_x on 13/07/2017.
 */

public class Comment {

    private String entryID;
    private String commentID;
    private String comment;
    private String commentAuthorID;
    private String commentAuthorName;
    private long dateTimePosted;
    private String commentMediaPath;

    public Comment(){
    }

    public Comment(String entryID, String comment, String commentAuthorID, String commentAuthorName, long dateTimePosted) {
        this.entryID = entryID;
        this.comment = comment;
        this.commentAuthorID = commentAuthorID;
        this.commentAuthorName = commentAuthorName;
        this.dateTimePosted = dateTimePosted;
    }

    public String getEntryID() {
        return entryID;
    }

    public void setEntryID(String entryID) {
        this.entryID = entryID;
    }

    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentAuthorID() {
        return commentAuthorID;
    }

    public void setCommentAuthorID(String commentAuthorID) {
        this.commentAuthorID = commentAuthorID;
    }

    public String getCommentAuthorName() {
        return commentAuthorName;
    }

    public void setCommentAuthorName(String commentAuthorName) {
        this.commentAuthorName = commentAuthorName;
    }

    public long getDateTimePosted() {
        return dateTimePosted;
    }

    public void setDateTimePosted(long dateTimePosted) {
        this.dateTimePosted = dateTimePosted;
    }

    public String getCommentMediaPath() {
        return commentMediaPath;
    }

    public void setCommentMediaPath(String commentMediaPath) {
        this.commentMediaPath = commentMediaPath;
    }
}
