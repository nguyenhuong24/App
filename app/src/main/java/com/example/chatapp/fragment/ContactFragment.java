package com.example.chatapp.fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.model.Contact;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends Fragment {

    private View contactView;
    private RecyclerView rvContactList;
    private DatabaseReference contactRef,userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    public ContactFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contactView= inflater.inflate(R.layout.fragment_contact, container, false);
        rvContactList=contactView.findViewById(R.id.rv_contact);
        rvContactList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        contactRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");

        return contactView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(contactRef,Contact.class)
                .build();
        FirebaseRecyclerAdapter<Contact,ContactViewHolder> adapter=new FirebaseRecyclerAdapter<Contact, ContactViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactViewHolder holder, int i, @NonNull Contact contact) {
                String userId=getRef(i).getKey();
                userRef.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if (dataSnapshot.child("userState").hasChild("state")){
                                String state=dataSnapshot.child("userState").child("state").getValue().toString();
                                String date=dataSnapshot.child("userState").child("date").getValue().toString();
                                String time=dataSnapshot.child("userState").child("time").getValue().toString();

                                if (state.equals("online")){
                                    holder.online.setVisibility(View.VISIBLE);
                                }else if (state.equals("offline")){
                                    holder.online.setVisibility(View.INVISIBLE);
                                }

                            }
                            else {
                                holder.online.setVisibility(View.INVISIBLE);
                            }


                            if(dataSnapshot.hasChild("image")){
                                String profileImage=dataSnapshot.child("image").getValue().toString();
                                String profileStatus=dataSnapshot.child("status").getValue().toString();
                                String profileName=dataSnapshot.child("name").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.status.setText(profileStatus);
                                Glide.with(getContext()).load(profileImage).error(R.drawable.profile).placeholder(R.drawable.profile).into(holder.profile);
                            }else {
                                String profileStatus=dataSnapshot.child("status").getValue().toString();
                                String profileName=dataSnapshot.child("name").getValue().toString();
                                holder.userName.setText(profileName);
                                holder.status.setText(profileStatus);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_item_layout,parent,false);
                ContactViewHolder holder=new ContactViewHolder(view);
                return holder;
            }
        };
        rvContactList.setAdapter(adapter);
        adapter.startListening();
    }
    public static class ContactViewHolder extends RecyclerView.ViewHolder{
        TextView userName,status;
        CircleImageView profile;
        ImageView online;
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.tv_userName);
            status=itemView.findViewById(R.id.tv_status);
            profile=itemView.findViewById(R.id.user_profile_img);
            online=itemView.findViewById(R.id.img_user_onl);
        }
    }
}
