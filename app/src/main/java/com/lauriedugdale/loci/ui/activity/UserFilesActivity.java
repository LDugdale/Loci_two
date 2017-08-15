package com.lauriedugdale.loci.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.ui.fragment.files.Files;
import com.lauriedugdale.loci.ui.fragment.files.SharedFiles;
import com.lauriedugdale.loci.ui.fragment.social.FriendsFragment;
import com.lauriedugdale.loci.ui.fragment.social.GroupsFragment;
import com.lauriedugdale.loci.utils.InterfaceUtils;

/**
 * Displays the user files
 * Uses a FragmentTabHost to display the Files and SharedFiles fragments
 *
 * @author Laurie Dugdale
 */
public class UserFilesActivity extends AppCompatActivity {

    // TODO consider adding folders or "collections" of entries

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_files);

        // setup toolbar
        InterfaceUtils.setUpToolbar(this, R.id.toolbar, "Your entries");
        // setup tabs
        InterfaceUtils.setUpTabs(this, "Files", "files", Files.class, "Shared", "sharedfiles", SharedFiles.class, null);
    }

}
