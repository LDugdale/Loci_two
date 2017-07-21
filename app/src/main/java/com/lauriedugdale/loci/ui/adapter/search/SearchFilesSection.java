package com.lauriedugdale.loci.ui.adapter.search;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Created by mnt_x on 21/07/2017.
 */

public class SearchFilesSection  extends StatelessSection {

    private Context mContext;
    private List<GeoEntry> mEntries;
    private DataUtils mDataUtils;

    public SearchFilesSection(Context context) {
        // call constructor with layout resources for this Section header and items
        super(new SectionParameters.Builder(R.layout.search_item_group_section)
                .headerResourceId(R.layout.search_header_group_section)
                .build());

        mContext = context;
        mEntries = new ArrayList<GeoEntry>();
        mDataUtils = new DataUtils(context);

    }

    public void addToGroups(GeoEntry entry) {
        mEntries.add(entry);
    }

    public void clearData(){
        mEntries.clear();
    }

    @Override
    public int getContentItemsTotal() {
        if (mEntries == null) {
            return 0;
        }
        return mEntries.size();    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new SearchFilesSection.SearchGroupsViewholder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        SearchGroupsSection.SearchGroupsViewholder groupsViewholder = (SearchGroupsSection.SearchGroupsViewholder) viewHolder;

        final GeoEntry entry = mEntries.get(position);


    }

    class SearchGroupsViewholder extends RecyclerView.ViewHolder {

        // The UI elements
        public TextView mGroupName;
        public ImageView mGroupPic;

        public SearchGroupsViewholder(View itemView) {
            super(itemView);

            // Find the UI elements
            mGroupName = (TextView) itemView.findViewById(R.id.ig_group_name);
            mGroupPic = (ImageView) itemView.findViewById(R.id.ig_group_pic);
        }
    }
}