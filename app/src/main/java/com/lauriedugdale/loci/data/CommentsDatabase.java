package com.lauriedugdale.loci.data;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lauriedugdale.loci.listeners.CommentUploadedListener;
import com.lauriedugdale.loci.listeners.EntriesDownloadedListener;
import com.lauriedugdale.loci.data.dataobjects.Comment;
import com.lauriedugdale.loci.ui.adapter.CommentsAdapter;

/**
 * Created by mnt_x on 02/08/2017.
 */

public class CommentsDatabase extends LociData {

    public CommentsDatabase(Context context) {
        super(context);
    }

    /**
     * -------------------------------------------------------------------------------------
     * --------------------------------- Comment Upload ------------------------------------
     * -------------------------------------------------------------------------------------
     */

    public void uploadComment(Comment comment, String entryID, final CommentUploadedListener listener) {

        DatabaseReference entryRef = getDatabase().child("comments/" + entryID);
        DatabaseReference pushEntryRef = entryRef.push();
        comment.setCommentID(pushEntryRef.getKey());
        pushEntryRef.setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listener.onCommentUploaded();
            }
        });

        NotificationDatabase notificationDatabase = new NotificationDatabase(getContext());
        notificationDatabase.uploadCommentNotification(comment);
    }

    /**
     * -------------------------------------------------------------------------------------
     * --------------------------------- Comment Download ----------------------------------
     * -------------------------------------------------------------------------------------
     */

    public void downloadComments(final CommentsAdapter adapter, String entryID, final EntriesDownloadedListener listener) {

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("comments");

        ref.child(entryID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapter.clearData();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Comment comment = postSnapshot.getValue(Comment.class);
                    adapter.addToComments(comment);
                }
                listener.onEntriesDownloaded();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
