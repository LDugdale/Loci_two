package com.lauriedugdale.loci.ui.adapter.notifications;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.listeners.SingleEntryDownloadListener;
import com.lauriedugdale.loci.data.EntryDatabase;
import com.lauriedugdale.loci.data.NotificationDatabase;
import com.lauriedugdale.loci.data.UserDatabase;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.data.dataobjects.Notification;
import com.lauriedugdale.loci.data.dataobjects.NotificationComment;
import com.lauriedugdale.loci.ui.activity.MainActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by mnt_x on 12/08/2017.
 */

public class NotificationMainAdapter extends RecyclerView.Adapter<NotificationMainAdapter.ViewHolder> {


    // Store the context and cursor for easy access
    private Context mContext;
    private List<Notification> mNotifications;
    private UserDatabase mUserDatabase;
    private NotificationDatabase mNotificationDatabase;

    /**
     * Entry adapter constructor
     *
     * @param context
     */
    public NotificationMainAdapter(Context context) {
        this.mContext = context;
        mNotifications = new ArrayList<>();
        mUserDatabase = new UserDatabase(context);
        mNotificationDatabase = new NotificationDatabase(context);
    }

    public void addToNotifications(Notification notification){
        mNotifications.add(notification);
        notifyDataSetChanged();
    }

    public void clearData(){
        mNotifications.clear();
        notifyDataSetChanged();
    }

    public void removeEntry(int position){
        mNotifications.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    /**
     * Inflates a layout depending on its position and returns a ViewHolder
     */
    public NotificationMainAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View contactView = null;
        // inflate second item layout & return that viewHolder
        contactView = inflater.inflate(R.layout.item_notification, parent, false);
        // Return a new holder instance
        return new NotificationMainAdapter.ViewHolder(contactView);
    }

    @Override
    /**
     * Populates data into the layout through the viewholder
     */
    public void onBindViewHolder(NotificationMainAdapter.ViewHolder viewHolder, final int position) {


        if (mNotifications.get(position) instanceof NotificationComment){
            final NotificationComment nc = (NotificationComment)mNotifications.get(position);
            //change background if item has been clicked
            if (nc.getSeen() == 0){
                viewHolder.mNotificationWrapper.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
            }
            // set notification title - the entry title
            viewHolder.mNotificationText.setText(nc.getEntryTitle());
            // set date
            String dateString = new java.text.SimpleDateFormat("EEE, d MMM 'at' HH:mm", Locale.UK).format(new Date( nc.getCommentDate()));
            viewHolder.mNotificationDate.setText(dateString);
            // set author image
            UserDatabase userDatabase = new UserDatabase(mContext);
            userDatabase.downloadNonLoggedInProfilePic(nc.getFromID(), viewHolder.mNotificationImage, R.drawable.default_profile);

            viewHolder.mNotificationWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    EntryDatabase entryDatabase = new EntryDatabase(mContext);
                    entryDatabase.downloadSingleEntry(nc.getEntryID(), new SingleEntryDownloadListener() {
                        @Override
                        public void onSingleEntryDownloaded(GeoEntry entry) {
                            mNotificationDatabase.uploadChangeSeenValue(nc.getNotificationID());
                            Intent intent = new Intent(mContext, MainActivity.class);
                            intent.setAction("single_entry");
                            intent.putExtra("entry", entry);
                            mContext.startActivity(intent);
                        }
                    });

                }
            });
        }
    }


    @Override
    /**
     * return the total count of items in the list
     */
    public int getItemCount() {
        if (mNotifications == null) {
            return 0;
        }
        return mNotifications.size();
    }

    /**
     * CLASS
     * Used to cache the views within the layout for quick access
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        // The UI elements
        public ImageView mNotificationImage;
        public TextView mNotificationText;
        public TextView mNotificationDate;
        public ConstraintLayout mNotificationWrapper;

        public ViewHolder(View itemView) {
            super(itemView);

            // Find the UI elements
            mNotificationImage = (ImageView) itemView.findViewById(R.id.notifcation_image);
            mNotificationText = (TextView) itemView.findViewById(R.id.notification_content);
            mNotificationDate = (TextView) itemView.findViewById(R.id.notification_date);
            mNotificationWrapper = (ConstraintLayout) itemView.findViewById(R.id.notification_wrapper);
        }
    }
}
