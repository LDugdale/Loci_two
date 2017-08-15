package com.lauriedugdale.loci.data;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.listeners.SingleEntryDownloadListener;
import com.lauriedugdale.loci.data.dataobjects.Comment;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.data.dataobjects.NotificationComment;
import com.lauriedugdale.loci.ui.adapter.notifications.NotificationMainAdapter;

/**
 * Created by mnt_x on 12/08/2017.
 */

public class NotificationDatabase  extends LociData {

    public static final int NOTIFICATION_TYPE_COMMENT = 800;

    public NotificationDatabase(Context context) {
        super(context);
    }

    /**
     * -------------------------------------------------------------------------------------
     * ---------------------------- Notificatiom Upload ------------------------------------
     * -------------------------------------------------------------------------------------
     */

    public void uploadCommentNotification(final Comment comment){

        final EntryDatabase entryDatabase = new EntryDatabase(getContext());


        entryDatabase.downloadSingleEntry(comment.getEntryID(), new SingleEntryDownloadListener() {
            @Override
            public void onSingleEntryDownloaded(GeoEntry entry) {

                String message = comment.getCommentAuthorName() + " commented in \"" + entry.getTitle() + "\"";

                NotificationComment notification = new NotificationComment(1,
                                                                        entry.getCreator(),
                                                                        entry.getCreatorName(),
                                                                        comment.getCommentAuthorID(),
                                                                        comment.getCommentAuthorName(),
                                                                        comment.getCommentID(),
                                                                        message,
                                                                        comment.getEntryID(),
                                                                        comment.getDateTimePosted());

                DatabaseReference entryRef = getDatabase().child("notification/" + entry.getCreator());
                DatabaseReference pushEntryRef = entryRef.push();
                notification.setNotificationID(pushEntryRef.getKey());
                pushEntryRef.setValue(notification);
            }
        });


        uploadIncrementNotificationCount();
    }

    public void uploadChangeSeenValue(String notificationID){
        getDatabase().child("notification").child(getCurrentUID() + "/" + notificationID + "/seen").setValue(0);
    }

    private void uploadIncrementNotificationCount(){
        final String uID = getCurrentUID();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("notification");

        ref.child(uID + "/unseen").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue()== null){
                    dataSnapshot.getRef().setValue(1);
                } else {
                    long value = (long) dataSnapshot.getValue();
                    value = value + 1;
                    dataSnapshot.getRef().setValue(value);
                }


            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void uploadResetNotificationCount(){
        getDatabase().child("notification").child(getCurrentUID() + "/unseen").setValue(0);

    }

    /**
     * -------------------------------------------------------------------------------------
     * ---------------------------- Notificatiom Download ----------------------------------
     * -------------------------------------------------------------------------------------
     */
    public void downloadNotifications(final NotificationMainAdapter adapter){

        final String uID = getCurrentUID();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("notification");

        ref.child(uID).orderByChild("recepientID").equalTo(uID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot d :dataSnapshot.getChildren()){
                    if ((long)d.child("type").getValue() == NotificationDatabase.NOTIFICATION_TYPE_COMMENT){
                        NotificationComment notification = d.getValue(NotificationComment.class);
                        adapter.addToNotifications(notification);
                    }
                }


            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void downloadUnseenNotifications(final RelativeLayout count){

        final TextView notificationCount = (TextView) count.findViewById(R.id.notification_count);
        final RelativeLayout notificationCountWrapper = (RelativeLayout) count.findViewById(R.id.notification_count_wrapper);


        final String uID = getCurrentUID();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("notification");

        ref.child(uID + "/unseen").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = (long)dataSnapshot.getValue();
                if (count > 0) {
                    notificationCount.setText(String.valueOf(count));
                    notificationCountWrapper.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        getDatabase().child("notification").child("unseen").setValue(0);

    }
}