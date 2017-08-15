package com.lauriedugdale.loci.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.UserDatabase;
import com.lauriedugdale.loci.data.dataobjects.BusStop;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.ui.adapter.MapClusterAdapter;
import com.lauriedugdale.loci.ui.nav.LociNavView;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by mnt_x on 09/07/2017.
 */

public class PopupUtils {


    public static void showClusterInfoPopup(Context context, View anchorView, ArrayList<?> clusterList, boolean mDisplayingCustomEntries, final LociNavView nav) {

        if (nav != null){
            nav.setInvisible();
        }
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });

        int actionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        int topMargin = 0;
        if (mDisplayingCustomEntries){
            topMargin = actionBarSize + 62 + 102;
        } else {
            topMargin = actionBarSize + 62;

        }

        LayoutInflater inflater = LayoutInflater.from(context);

        View popupView = inflater.inflate(R.layout.popup_map_cluster_info, null);
        // PopupWindow popupWindow = new PopupWindow(popupView, RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        final PopupWindow popupWindow = new PopupWindow(popupView, RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT , true);
        // If the PopupWindow should be focusable
        popupWindow.setFocusable(true);
        // If you need the PopupWindow to dismiss when when touched outside
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        int location[] = new int[2];
        // Get the View's(the one that was clicked in the Fragment) location
        anchorView.getLocationOnScreen(location);

        popupWindow.showAtLocation(anchorView, Gravity.TOP, 0, topMargin);

        ImageView close = (ImageView) popupView.findViewById(R.id.close);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (nav != null) {
                    nav.setVisible();
                }
            }
        });

        RecyclerView recyclerView = (RecyclerView) popupView.findViewById(R.id.rv_cluster);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        MapClusterAdapter adapter = new MapClusterAdapter(context, clusterList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    public static void showMarkerInfoPopup(final Context context, View anchorView, final GeoEntry entry, boolean mDisplayingCustomEntries) {

        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });

        int actionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        int topMargin = 0;
        if (mDisplayingCustomEntries){
            topMargin = actionBarSize + 62 + 102;
        } else {
            topMargin = actionBarSize + 62;

        }
        LayoutInflater inflater = LayoutInflater.from(context);

        View popupView = inflater.inflate(R.layout.popup_map_entry_info, null);

        final PopupWindow popupWindow = new PopupWindow(popupView, RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT , true);

        // If the PopupWindow should be focusable
        popupWindow.setFocusable(true);
        // If you need the PopupWindow to dismiss when when touched outside
        popupWindow.setBackgroundDrawable(new ColorDrawable());

        int location[] = new int[2];

        // Get the View's(the one that was clicked in the Fragment) location
        anchorView.getLocationOnScreen(location);

        // Using location, the PopupWindow will be displayed right under anchorView
        popupWindow.showAtLocation(anchorView, Gravity.TOP, 0, topMargin);

        // connect time UI elements
        TextView entryTitle = (TextView) popupView.findViewById(R.id.info_bar_title);
        ImageView entryImage = (ImageView) popupView.findViewById(R.id.info_bar_type);
        TextView showEntry = (TextView) popupView.findViewById(R.id.info_bar_show_entry);
        TextView entryDistance = (TextView) popupView.findViewById(R.id.info_bar_marker_distance);
        TextView entryAuthor = (TextView) popupView.findViewById(R.id.info_bar_marker_author);

        // set type image
        UserDatabase userDatabase = new UserDatabase(context);
        userDatabase.downloadNonLoggedInProfilePic(entry.getCreator(), entryImage, R.drawable.default_profile);
        // set title
        entryTitle.setText(entry.getTitle());
        // display distance
        LocationUtils.displayDistance(entryDistance, context, entry.getLatitude(), entry.getLongitude());
        // set author
        entryAuthor.setText(entry.getCreatorName());

        LocationUtils.checkDistance(context, showEntry, entry.getLatitude(), entry.getLongitude());

        showEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent startViewEntryIntent = new Intent(context, LocationUtils.getEntryDestinationClass(entry.getFileType()));
                startViewEntryIntent.putExtra(Intent.ACTION_OPEN_DOCUMENT, entry);
                context.startActivity(startViewEntryIntent);
            }

        });
    }

    public static void showBusMarkerInfoPopup(final Context context, View anchorView, final BusStop busStop, boolean mDisplayingCustomEntries) {

        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });

        int actionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        int topMargin = 0;
        if (mDisplayingCustomEntries){
            topMargin = actionBarSize + 62 + 102;
        } else {
            topMargin = actionBarSize + 62;

        }
        LayoutInflater inflater = LayoutInflater.from(context);

        View popupView = inflater.inflate(R.layout.popup_map_entry_info, null);

        final PopupWindow popupWindow = new PopupWindow(popupView, RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT , true);

        // If the PopupWindow should be focusable
        popupWindow.setFocusable(true);
        // If you need the PopupWindow to dismiss when when touched outside
        popupWindow.setBackgroundDrawable(new ColorDrawable());

        int location[] = new int[2];

        // Get the View's(the one that was clicked in the Fragment) location
        anchorView.getLocationOnScreen(location);

        // Using location, the PopupWindow will be displayed right under anchorView
        popupWindow.showAtLocation(anchorView, Gravity.TOP, 0, topMargin);

        // connect time UI elements
        TextView entryTitle = (TextView) popupView.findViewById(R.id.info_bar_title);
        ImageView entryImage = (ImageView) popupView.findViewById(R.id.info_bar_type);
        TextView showEntry = (TextView) popupView.findViewById(R.id.info_bar_show_entry);
        TextView entryDistance = (TextView) popupView.findViewById(R.id.info_bar_marker_distance);
        TextView entryAuthor = (TextView) popupView.findViewById(R.id.info_bar_marker_author);

        // Dont need the image or show entry
        showEntry.setVisibility(View.GONE);
        entryImage.setVisibility(View.GONE);
        // set title
        entryTitle.setText(busStop.getName());
        // display distance
        LocationUtils.displayDistance(entryDistance, context, busStop.getLatitude(), busStop.getLongitude());
        // set author
        entryAuthor.setText(busStop.getLocality());
    }
}
