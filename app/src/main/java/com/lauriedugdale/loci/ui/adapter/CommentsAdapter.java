package com.lauriedugdale.loci.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.UserDatabase;
import com.lauriedugdale.loci.utils.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.Comment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Laurie Dugdale
 */

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
    //TODO add delete, locate and edit buttons (only edit if within distance)

    // Store the context and cursor for easy access
    private Context mContext;
    private List<Comment> mComments;
    private UserDatabase mUserDatabase;

    /**
     * Entry adapter constructor
     *
     * @param context
     */
    public CommentsAdapter(Context context) {
        this.mContext = context;
        mComments = new ArrayList<Comment>();
        mUserDatabase = new UserDatabase(context);

    }

    public void clearData(){
        mComments.clear();
        notifyDataSetChanged();
    }

    public void addToComments(Comment comment){
        mComments.add(comment);
        notifyDataSetChanged();
    }

    @Override
    /**
     * Inflates a layout depending on its position and returns a ViewHolder
     */
    public CommentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View contactView = null;
        // inflate second item layout & return that viewHolder
        contactView = inflater.inflate(R.layout.item_comments, parent, false);
        // Return a new holder instance
        return new CommentsAdapter.ViewHolder(contactView);
    }

    @Override
    /**
     * Populates data into the layout through the viewholder
     */
    public void onBindViewHolder(CommentsAdapter.ViewHolder viewHolder, final int position) {

        final Comment comment = mComments.get(position);

        // set author profile
        mUserDatabase.downloadProfilePic(viewHolder.mAuthorProfile, R.drawable.default_profile);
        // set author name
        viewHolder.mAuthor.setText(comment.getCommentAuthorName());
        // set date
        String dateString = new java.text.SimpleDateFormat("EEE, d MMM 'at' HH:mm", Locale.UK).format(new Date( comment.getDateTimePosted()));
        viewHolder.mDate.setText(dateString);
        // set comment
        viewHolder.mContent.setText(comment.getComment());
    }

    @Override
    /**
     * return the total count of items in the list
     */
    public int getItemCount() {
        if (mComments == null) {
            return 0;
        }
        return mComments.size();
    }

    /**
     * CLASS
     * Used to cache the views within the layout for quick access
     */
    class ViewHolder extends RecyclerView.ViewHolder{

        // The UI elements

        public ImageView mAuthorProfile;
        public TextView mAuthor;
        public TextView mDate;
        public TextView mContent;

        public ViewHolder(View itemView) {
            super(itemView);

            // Find the UI elements
            mAuthorProfile = (ImageView) itemView.findViewById(R.id.comment_profile_pic);
            mAuthor = (TextView) itemView.findViewById(R.id.comment_author);
            mDate = (TextView) itemView.findViewById(R.id.comment_date);
            mContent = (TextView) itemView.findViewById(R.id.comment_content);
        }
    }
}
