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
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.ui.adapter.search.SearchGroupsSection;
import com.lauriedugdale.loci.ui.adapter.search.SearchUsersSection;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class SearchActivity extends AppCompatActivity {

    private ImageView mClose;
    private EditText mSearch;

//    private SelectFriendsAdapter mAdapter;
    private SectionedRecyclerViewAdapter mSectionAdapter;
    private RecyclerView mRecyclerView;
    private DataUtils mDataUtils;
    private SearchUsersSection mUsersSection;
    private SearchGroupsSection mGroupsSection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mDataUtils = new DataUtils(this);

        mClose = (ImageView) findViewById(R.id.search_close);
        mSearch = (EditText) findViewById(R.id.main_search);

        mSectionAdapter = new SectionedRecyclerViewAdapter();
        // Add user section
        mUsersSection = new SearchUsersSection(this);
        mSectionAdapter.addSection(mUsersSection);
        // add group section
        mGroupsSection = new SearchGroupsSection(this);
        mSectionAdapter.addSection(mGroupsSection);

        // Set up your RecyclerView with the SectionedRecyclerViewAdapter
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_search_friends);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mSectionAdapter);

        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        handleSearch();
    }


    public void handleSearch(){
        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // if string is empty
                if(s.toString().equals("")){
                    mUsersSection.clearData();
                    mSectionAdapter.notifyDataSetChanged();
                } else {
                    mDataUtils.search(mSectionAdapter, mUsersSection, mGroupsSection, s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
