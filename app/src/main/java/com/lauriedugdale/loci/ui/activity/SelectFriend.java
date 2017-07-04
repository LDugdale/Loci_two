package com.lauriedugdale.loci.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.data.DataUtils;
import com.lauriedugdale.loci.ui.adapter.SelectFriendsAdapter;

public class SelectFriend extends AppCompatActivity  {

    private SelectFriendsAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private DataUtils mDataUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);

        mDataUtils = new DataUtils(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_select_friends);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SelectFriendsAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        Button enterButton = (Button) findViewById(R.id.search_user_button);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText enterUsername = (EditText) findViewById(R.id.enter_username);

                String username = enterUsername.getText().toString();

                mDataUtils.searchUsers(mAdapter, username);

            }
        });


    }
}
