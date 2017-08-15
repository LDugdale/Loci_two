package com.lauriedugdale.loci.ui.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.SearchDatabase;
import com.lauriedugdale.loci.utils.DataUtils;
import com.lauriedugdale.loci.ui.adapter.search.SearchEntriesSection;
import com.lauriedugdale.loci.ui.adapter.search.SearchGroupsSection;
import com.lauriedugdale.loci.ui.adapter.search.SearchUsersSection;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

/**
 * This activity handles the main search functionality for the app
 *
 * @author Laurie Dugdale
 */
public class SearchActivity extends AppCompatActivity {

    private ImageView mClose;
    private EditText mSearch;

    // the various sections and adapters required for displaying results
    private SectionedRecyclerViewAdapter mSectionAdapter;
    private RecyclerView mRecyclerView;
    private SearchDatabase mSearchDatabase;
    private SearchUsersSection mUsersSection;
    private SearchGroupsSection mGroupsSection;
    private SearchEntriesSection mFilesSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchDatabase = new SearchDatabase(this);

        mClose = (ImageView) findViewById(R.id.search_close);
        mSearch = (EditText) findViewById(R.id.main_search);

        mSectionAdapter = new SectionedRecyclerViewAdapter();
        // Add user section
        mUsersSection = new SearchUsersSection(this);
        mSectionAdapter.addSection(mUsersSection);
        mUsersSection.setVisible(false);
        // Add files section
        mFilesSection = new SearchEntriesSection(this);
        mSectionAdapter.addSection(mFilesSection);
        mFilesSection.setVisible(false);
        // Add group section
        mGroupsSection = new SearchGroupsSection(this);
        mSectionAdapter.addSection(mGroupsSection);
        mGroupsSection.setVisible(false);


        // Set up your RecyclerView with the SectionedRecyclerViewAdapter
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_search_friends);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mSectionAdapter);

        // when close is clicked finish activity
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        handleSearch();
    }


    public void handleSearch(){

        // listen for changes in the EditText mSearch
        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // if string is empty clear data from the sections and hide them
                if(s.toString().equals("")){

                    mUsersSection.clearData();
                    mUsersSection.setVisible(false);
                    mGroupsSection.clearData();
                    mGroupsSection.setVisible(false);
                    mFilesSection.clearData();
                    mFilesSection.setVisible(false);

                    mSectionAdapter.notifyDataSetChanged();
                // else perform a search
                } else {
                    mSearchDatabase.search(mSectionAdapter, mUsersSection, mGroupsSection, mFilesSection, s.toString().toUpperCase());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
