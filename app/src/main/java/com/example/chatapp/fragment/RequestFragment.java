package com.example.chatapp.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.model.Contact;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestFragment extends Fragment {
    private View RequestsFragmentView;
    private RecyclerView myRequestsList;

    private DatabaseReference chatRequestRef, userRef, contactRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestsFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        myRequestsList = RequestsFragmentView.findViewById(R.id.rv_request);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));


        return RequestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contact> options = new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(chatRequestRef.child(currentUserId), Contact.class)
                .build();
        FirebaseRecyclerAdapter<Contact, RequestViewolder> adapter = new FirebaseRecyclerAdapter<Contact, RequestViewolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewolder holder, int i, @NonNull final Contact contact) {
                holder.itemView.findViewById(R.id.btn_accept).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.btn_cancel).setVisibility(View.VISIBLE);

                final String LIST_USER_ID = getRef(i).getKey();
                DatabaseReference getType = getRef(i).child("requestType").getRef();
                getType.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String type = dataSnapshot.getValue().toString();
                            if (type.equals("receive")) {
                                userRef.child(LIST_USER_ID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("image")) {
                                            final String profileImage = dataSnapshot.child("image").getValue().toString();

                                            Glide.with(getContext()).load(profileImage).into(holder.profile);
                                        }
                                        final String userName = dataSnapshot.child("name").getValue().toString();
                                        final String statusUser = dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(userName);

                                        holder.status.setText("want to connect with you");

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence option[] = new CharSequence[]{
                                                        "Accept",
                                                        "Cancel"
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle(userName + " Chat Request");
                                                builder.setItems(option, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        if (which == 0) {
                                                            contactRef.child(currentUserId).child(LIST_USER_ID).child("Contact").setValue("save")
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                contactRef.child(LIST_USER_ID).child(currentUserId).child("Contact").setValue("save")
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    chatRequestRef.child(currentUserId).child(LIST_USER_ID)
                                                                                                            .removeValue()
                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                    if (task.isSuccessful()){
                                                                                                                        chatRequestRef.child(LIST_USER_ID).child(currentUserId)
                                                                                                                                .removeValue()
                                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                    @Override
                                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                        if (task.isSuccessful()){
                                                                                                                                            Toast.makeText(getContext(), "New contact save", Toast.LENGTH_SHORT).show();

                                                                                                                                        }
                                                                                                                                    }
                                                                                                                                });
                                                                                                                    }
                                                                                                                }
                                                                                                            });

                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });
                                                        } else if (which == 1) {
                                                            chatRequestRef.child(currentUserId).child(LIST_USER_ID)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                chatRequestRef.child(LIST_USER_ID).child(currentUserId)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()){
                                                                                                    Toast.makeText(getContext(), "Contact delete", Toast.LENGTH_SHORT).show();

                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });


                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            else if (type.equals("sent")){
                                Button request_send=holder.itemView.findViewById(R.id.btn_accept);
                                request_send.setText("Req sent");
                                holder.itemView.findViewById(R.id.btn_cancel).setVisibility(View.INVISIBLE);
                                userRef.child(LIST_USER_ID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("image")) {
                                            final String profileImage = dataSnapshot.child("image").getValue().toString();

                                            Glide.with(getContext()).load(profileImage).into(holder.profile);
                                        }
                                        final String userName = dataSnapshot.child("name").getValue().toString();
                                        final String statusUser = dataSnapshot.child("status").getValue().toString();

                                        holder.userName.setText(userName);
                                        holder.status.setText("you have sent request to "+userName);

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence option[] = new CharSequence[]{
                                                        "Cancel Chat Request"
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Already send chat request");
                                                builder.setItems(option, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        if (which == 0)
                                                        {
                                                            chatRequestRef.child(currentUserId).child(LIST_USER_ID)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                chatRequestRef.child(LIST_USER_ID).child(currentUserId)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()){
                                                                                                    Toast.makeText(getContext(), "You have canceled request", Toast.LENGTH_SHORT).show();

                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });


                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
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
            public RequestViewolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.user_display_item_layout, parent, false);

                return new RequestViewolder(view);
            }
        };
        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewolder extends RecyclerView.ViewHolder {
        TextView userName, status;
        CircleImageView profile;
        Button accept, cancel;

        public RequestViewolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.tv_userName);
            status = itemView.findViewById(R.id.tv_status);
            profile = itemView.findViewById(R.id.user_profile_img);
            accept = itemView.findViewById(R.id.btn_accept);
            cancel = itemView.findViewById(R.id.btn_cancel);
        }
    }
}



