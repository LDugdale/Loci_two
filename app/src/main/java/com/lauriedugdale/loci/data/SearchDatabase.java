package com.lauriedugdale.loci.data;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.data.dataobjects.Group;
import com.lauriedugdale.loci.data.dataobjects.User;
import com.lauriedugdale.loci.ui.adapter.search.SearchEntriesSection;
import com.lauriedugdale.loci.ui.adapter.search.SearchGroupsSection;
import com.lauriedugdale.loci.ui.adapter.search.SearchUsersSection;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

/**
 * Created by mnt_x on 02/08/2017.
 */

public class SearchDatabase extends LociData {

    public SearchDatabase(Context context) {
        super(context);
    }


    public void search(final SectionedRecyclerViewAdapter adapter, final SearchUsersSection usersSection, final SearchGroupsSection groupsSection, final SearchEntriesSection entriesSection, final String soFar){

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users");

        ref.orderByChild("queryUsername").startAt(soFar).endAt(soFar + "\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usersSection.clearData();
                adapter.notifyDataSetChanged();

                if (!dataSnapshot.hasChildren()){
                    usersSection.setVisible(false);
                } else if (dataSnapshot.hasChildren() && !usersSection.isVisible()){
                    usersSection.setVisible(true);
                }

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User user = postSnapshot.getValue(User.class);
                    usersSection.addToUsers(user);
                }
                // notify the adapter that data has been changed in order for it to be displayed in recyclerview
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        String groupSoFar = "PUBLIC__" + soFar;
        ref = database.getReference("groups");
        ref.orderByChild("queryGroupName").startAt(groupSoFar).endAt(groupSoFar + "\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                groupsSection.clearData();
                adapter.notifyDataSetChanged();
                if (!dataSnapshot.hasChildren()){

                    groupsSection.setVisible(false);
                } else if (dataSnapshot.hasChildren() && !groupsSection.isVisible()){

                    groupsSection.setVisible(true);
                }

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Group group = postSnapshot.getValue(Group.class);
                    groupsSection.addToGroups(group);
                    adapter.notifyDataSetChanged();

                }
                // notify the adapter that data has been changed in order for it to be displayed in recyclerview
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        final String currentUID = getCurrentUID();
        ref = database.getReference("file_permission");
        final DatabaseReference finalRef = ref;
        ref.child(currentUID).orderByChild("queryTitle").startAt(soFar).endAt(soFar + "\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot userDataSnapshot) {
                entriesSection.clearData();
                adapter.notifyDataSetChanged();
                for (DataSnapshot postSnapshot : userDataSnapshot.getChildren()) {
                    GeoEntry entry = postSnapshot.getValue(GeoEntry.class);
                    entriesSection.addToEntries(entry);
                }
                adapter.notifyDataSetChanged();

                finalRef.child("anyone").orderByChild("queryTitle").startAt(soFar).endAt(soFar + "\uf8ff").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (!dataSnapshot.hasChildren() && !userDataSnapshot.hasChildren()){
                            entriesSection.setVisible(false);
                        } else if (( dataSnapshot.hasChildren() || userDataSnapshot.hasChildren() ) && !entriesSection.isVisible()){
                            entriesSection.setVisible(true);
                        }

                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            GeoEntry entry = postSnapshot.getValue(GeoEntry.class);
                            entriesSection.addToEntries(entry);

                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
