package com.example.chatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.model.Contact;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView rvFindFr;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        init();
    }

    private void init() {
        rvFindFr=findViewById(R.id.rv_find_friend);
        rvFindFr.setLayoutManager(new LinearLayoutManager(this));

        mToolbar=findViewById(R.id.find_friend_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friend");
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contact> option=new FirebaseRecyclerOptions.Builder<Contact>().setQuery(userRef,Contact.class)
                .build();
        FirebaseRecyclerAdapter<Contact,FindFriendViewHolder> adapter=new FirebaseRecyclerAdapter<Contact, FindFriendViewHolder>(option) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contact contact) {
                holder.tvName.setText(contact.getName());
                holder.tvStatus.setText(contact.getStatus());
                Glide.with(getApplicationContext()).load(contact.getImage())
                        .error(R.drawable.profile)
                        .into(holder.cirProfile);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String clickUserId=getRef(position).getKey();
                        Intent profileIntent=new Intent(FindFriendActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("clickUserId",clickUserId);
                        startActivity(profileIntent);

                    }
                });
            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_item_layout,parent,false);
                FindFriendViewHolder holder=new FindFriendViewHolder(view);
                return holder;
            }
        };
        rvFindFr.setAdapter(adapter);
        adapter.startListening();
    }
    public static  class FindFriendViewHolder extends RecyclerView.ViewHolder{
        CircleImageView cirProfile;
        TextView tvName,tvStatus;
        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            cirProfile=itemView.findViewById(R.id.user_profile_img);
            tvName=itemView.findViewById(R.id.tv_userName);
            tvStatus=itemView.findViewById(R.id.tv_status);
        }
    }
}
