package com.lauriedugdale.loci.ui.activity.social;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.lauriedugdale.loci.R;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mUsername;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
    }
}
