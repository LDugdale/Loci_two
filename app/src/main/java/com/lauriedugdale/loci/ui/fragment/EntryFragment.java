package com.lauriedugdale.loci.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EntryFragment extends Fragment {

    private GeoEntry mGeoEntry;

    private DataUtils mDataUtils;

    private TextView mTitle;
    private TextView mDescription;

    private ImageView mAuthorPic;
    private TextView mAuthor;
    private TextView mDate;

    private OnFragmentInteractionListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDataUtils = new DataUtils(getContext());

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

        // set description and title
        mTitle.setText(mGeoEntry.getTitle());
        mDescription.setText(mGeoEntry.getDescription());

        // set author name and picture
        mDataUtils.getNonLoggedInProfilePic(mGeoEntry.getCreator(), mAuthorPic, R.drawable.default_profile);
        mAuthor.setText(mGeoEntry.getCreatorName());

        // set the upload date
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.UK).format(new Date( mGeoEntry.getUploadDate()));
        mDate.setText(dateString);

        return view;
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
