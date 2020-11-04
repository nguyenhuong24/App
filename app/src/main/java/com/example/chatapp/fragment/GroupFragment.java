package com.example.chatapp.fragment;


import android.content.Intent;
import android.icu.text.Edits;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.chatapp.R;
import com.example.chatapp.activity.GroupChatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment {
    private View groupFragmetView;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private List<String> listGroup=new ArrayList<>();
    private DatabaseReference groupRef;

    public GroupFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        groupFragmetView=inflater.inflate(R.layout.fragment_group,container,false);
        groupRef= FirebaseDatabase.getInstance().getReference().child("Groups");
        init();
        RetrieveAndDisplayGroup();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentGroupName=parent.getItemAtPosition(position).toString();
                Intent groupChat=new Intent(getContext(), GroupChatActivity.class);
                groupChat.putExtra("groupName",currentGroupName);
                startActivity(groupChat);
            }
        });
        return groupFragmetView;
    }



    private void init() {
        listView=groupFragmetView.findViewById(R.id.lv_group);
        arrayAdapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,listGroup);
        listView.setAdapter(arrayAdapter);
    }
    private void RetrieveAndDisplayGroup() {
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set=new HashSet<>();
                Iterator iterator=dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()){
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }
                listGroup.clear();
                listGroup.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
