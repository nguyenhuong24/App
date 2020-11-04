package com.example.chatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private String receiveUserId, senderUserId, currentState;
    private CircleImageView userProfileImage;
    private TextView tvUserNameProfile, tvStatusProfile;
    private Button btnSendMess, btnCancelMessRequest;
    private DatabaseReference userRef, chatRequest, contactRef,notificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequest = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");


        receiveUserId = getIntent().getExtras().get("clickUserId").toString();
        senderUserId = mAuth.getCurrentUser().getUid();
        Toast.makeText(this, "User id: " + receiveUserId, Toast.LENGTH_SHORT).show();
        init();
        RetrieveUserInfor();
    }


    private void init() {
        userProfileImage = findViewById(R.id.cr_profile_image);
        tvUserNameProfile = findViewById(R.id.tv_click_useName);
        tvStatusProfile = findViewById(R.id.tv_click_status);
        btnSendMess = findViewById(R.id.btn_sendMesRequest);
        btnCancelMessRequest = findViewById(R.id.btn_declineMesRequest);
        currentState = "new";

    }

    private void RetrieveUserInfor() {
        userRef.child(receiveUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))) {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    Glide.with(getApplicationContext()).load(userImage).error(R.drawable.profile).into(userProfileImage);
                    tvUserNameProfile.setText(userName);
                    tvStatusProfile.setText(userStatus);

                    ManageChatRequest();
                } else {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    tvUserNameProfile.setText(userName);
                    tvStatusProfile.setText(userStatus);
                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequest() {
        chatRequest.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiveUserId)) {
                    String requestType = dataSnapshot.child(receiveUserId).child("requestType").getValue().toString();
                    if (requestType.equals("sent")) {
                        currentState = "requestSent";
                        btnSendMess.setText("Cancel Chat Request");
                    } else if (requestType.equals("receive")) {
                        currentState = "requestReceive";
                        btnSendMess.setText("Accept Chat Request");
                        btnCancelMessRequest.setVisibility(View.VISIBLE);
                        btnCancelMessRequest.setEnabled(true);
                        btnCancelMessRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelChatRequest();
                            }
                        });
                    }
                }else {
                    contactRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(receiveUserId)){
                                currentState="friend";
                                btnSendMess.setText("Remove this contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (!senderUserId.equals(receiveUserId)) {
            btnSendMess.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnSendMess.setEnabled(false);
                    if (currentState.equals("new")) {
                        SendChatRequest();
                    }
                    if (currentState.equals("requestSent")) {
                        CancelChatRequest();
                    }
                    if (currentState.equals("requestReceive")) {
                        AcceptChatRequest();
                    }
                    if (currentState.equals("friend")) {
                        RemoveContact();
                    }

                }
            });

        } else {
            btnSendMess.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveContact() {
        contactRef.child(senderUserId).child(receiveUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactRef.child(receiveUserId).child(senderUserId)
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        btnSendMess.setEnabled(true);
                                        currentState = "new";
                                        btnSendMess.setText("Send Message");


                                        btnCancelMessRequest.setVisibility(View.INVISIBLE);
                                        btnCancelMessRequest.setEnabled(false);
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {
        contactRef.child(senderUserId).child(receiveUserId)
                .child("Contacts").setValue("save").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    contactRef.child(receiveUserId).child(senderUserId)
                            .child("Contacts").setValue("save").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                chatRequest.child(senderUserId).child(receiveUserId)
                                        .removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    chatRequest.child(receiveUserId).child(senderUserId)
                                                            .removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        btnSendMess.setEnabled(true);
                                                                        currentState="friend";
                                                                        btnSendMess.setText("Remove this contact");
                                                                        btnCancelMessRequest.setVisibility(View.INVISIBLE);
                                                                        btnCancelMessRequest.setEnabled(false);

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
    }

    private void CancelChatRequest() {
        chatRequest.child(senderUserId).child(receiveUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRequest.child(receiveUserId).child(senderUserId)
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        btnSendMess.setEnabled(true);
                                        currentState = "new";
                                        btnSendMess.setText("Send Message");


                                        btnCancelMessRequest.setVisibility(View.INVISIBLE);
                                        btnCancelMessRequest.setEnabled(false);
                                    }
                                }
                            });
                        }
                    }
                });

    }

    private void SendChatRequest() {
        chatRequest.child(senderUserId).child(receiveUserId).child("requestType")
                .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    chatRequest.child(receiveUserId).child(senderUserId)
                            .child("requestType").setValue("receive")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        HashMap<String,String> notification=new HashMap<>();
                                        notification.put("from",senderUserId);
                                        notification.put("type","request");
                                        notificationRef.child(receiveUserId).push()
                                        .setValue(notification)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            btnSendMess.setEnabled(true);
                                                            currentState = "requestSent";
                                                            btnSendMess.setText("Cancel Chat Request");
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
