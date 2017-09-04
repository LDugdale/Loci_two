package com.lauriedugdale.loci.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lauriedugdale.loci.listeners.CommentUploadedListener;
import com.lauriedugdale.loci.listeners.EntriesDownloadedListener;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.CommentsDatabase;
import com.lauriedugdale.loci.data.UserDatabase;
import com.lauriedugdale.loci.utils.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.Comment;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.ui.adapter.CommentsAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EntryFragment extends Fragment {

    private GeoEntry mGeoEntry;

    private CommentsDatabase mCommentsDatabase;
    private UserDatabase mUserDatabase;

    private TextView mTitle;
    private TextView mDescription;
    private ImageView mAuthorPic;
    private TextView mAuthor;
    private TextView mDate;
    private EditText mComment;
    private ImageView mSend;

    private RecyclerView mRecyclerView;
    private CommentsAdapter mAdapter;

    private OnFragmentInteractionListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCommentsDatabase = new CommentsDatabase(getActivity());
        mUserDatabase = new UserDatabase(getActivity());
        //here is your arguments
        Bundle bundle=getArguments();
        //here is your list array
        mGeoEntry = bundle.getParcelable(Intent.ACTION_OPEN_DOCUMENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entry, container, false);

        mTitle = (TextView) view.findViewById(R.id.view_entry_title);
        mDescription = (TextView) view.findViewById(R.id.view_entry_description);
        mAuthorPic = (ImageView) view.findViewById(R.id.view_entry_author_pic);
        mAuthor = (TextView) view.findViewById(R.id.view_entry_author);
        mDate = (TextView) view.findViewById(R.id.view_entry_date);
        mComment = (EditText) view.findViewById(R.id.view_entry_comments);
        mSend = (ImageView) view.findViewById(R.id.view_entry_send);

        // Set up the recycler view
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_comments);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CommentsAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setNestedScrollingEnabled(false);

        // if description is empty hide it
        if (mGeoEntry.getDescription() == null){
            mDescription.setVisibility(View.GONE);
        } else {
            mDescription.setVisibility(View.VISIBLE);
        }

        // add comments to adapter
        mCommentsDatabase.downloadComments(mAdapter, mGeoEntry.getEntryID(), new EntriesDownloadedListener() {
            @Override
            public void onEntriesDownloaded() {

            }
        });

        // set description and title
        mTitle.setText(mGeoEntry.getTitle());
        mDescription.setText(mGeoEntry.getDescription());

        // set author name and picture
        mUserDatabase.downloadNonLoggedInProfilePic(mGeoEntry.getCreator(), mAuthorPic, R.drawable.default_profile);
        mAuthor.setText(mGeoEntry.getCreatorName());

        // set the upload date
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.UK).format(new Date( mGeoEntry.getUploadDate()));
        mDate.setText(dateString);

        sendComment();

        return view;
    }

    public void sendComment(){
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mComment.getText().toString().equals("") || mComment.getText().toString() == null ){
                    return;
                }

                mCommentsDatabase.uploadComment(
                        new Comment(
                                mGeoEntry.getEntryID(),
                                mComment.getText().toString(),
                                mCommentsDatabase.getCurrentUID(),
                                mCommentsDatabase.getCurrentUsername(),
                                DataUtils.getDateTime()),
                                mGeoEntry.getEntryID(),
                                new CommentUploadedListener() {
                                    @Override
                                    public void onCommentUploaded() {
                                        mComment.setText("");
                                    }
                                });
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
