package com.lauriedugdale.loci.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.ui.activity.AugmentedActivity;
import com.lauriedugdale.loci.ui.activity.MainActivity;
import com.lauriedugdale.loci.ui.adapter.MapClusterAdapter;
import com.lauriedugdale.loci.ui.nav.LociNavView;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by mnt_x on 09/07/2017.
 */

public class PopupUtils {

    private static void setInfoBarImage(Context context, ImageView imageType, int type){
        // remove previous listener
        imageType.setOnClickListener(null);
        switch(type){
            case DataUtils.NO_MEDIA:
                imageType.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_text));
                break;
            case DataUtils.IMAGE:
                imageType.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_image));
                break;
            case DataUtils.AUDIO:
                imageType.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_audiotrack));
                break;
            default:
                imageType.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_text));
                break;
        }
    }


    public static void showClusterInfoPopup(Context context, View anchorView, ArrayList<?> clusterList, boolean mDisplayingCustomEntries, final LociNavView nav) {

        nav.setInvisible();
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

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                nav.setVisible();
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
        TextView entryDate = (TextView) popupView.findViewById(R.id.info_bar_marker_date);
        TextView entryAuthor = (TextView) popupView.findViewById(R.id.info_bar_marker_author);

        // set type image
        setInfoBarImage(context, entryImage, entry.getFileType());
        // set title
        entryTitle.setText(entry.getTitle());
        // display distance
        LocationUtils.displayDistance(entryDistance, context, entry.getLatitude(), entry.getLongitude());
        // set date
        String dateString = new java.text.SimpleDateFormat("EEE, d MMM 'at' HH:mm", Locale.UK).format(new Date( entry.getUploadDate()));
        entryDate.setText(dateString);
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
}
