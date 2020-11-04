package com.example.chatapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton imgbSendMess;
    private EditText userMessInput;
    private ScrollView scrollView;
    private TextView displayMess;
    private FirebaseAuth mAuth;
    private DatabaseReference root,groupNameRef,groupMessKeyRef;
    private String currentGroupName, currentUserId, currentUserName,currentDate,curentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        root= FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);


        init();
        ActiveActionBar();
        GetUserInfor();
        imgbSendMess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageInfoToDatabase();
                userMessInput.setText("");
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    DisplayMessenger(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void init() {
        mToolbar = findViewById(R.id.group_chat_bar);
        imgbSendMess = findViewById(R.id.btn_send_mess);
        userMessInput = findViewById(R.id.edt_input_group_mess);
        displayMess = findViewById(R.id.tv_group_chat_display);
        scrollView = findViewById(R.id.scroll_group);
    }

    private void ActiveActionBar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);
    }

    private void GetUserInfor() {
        root.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUserName=dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void SaveMessageInfoToDatabase() {
        String mess=userMessInput.getText().toString();
        String messKey=groupNameRef.push().getKey();
        if(TextUtils.isEmpty(mess)){
            Toast.makeText(this,"Write message",Toast.LENGTH_SHORT).show();
        }else {
            Calendar calendarDate=Calendar.getInstance();
            SimpleDateFormat currentDateFormat=new SimpleDateFormat("MMM dd, yyyy");
            currentDate=currentDateFormat.format(calendarDate.getTime());

            Calendar calendarTime=Calendar.getInstance();
            SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");
            curentTime=currentTimeFormat.format(calendarTime.getTime());

            HashMap<String,Object> groupMapKey=new HashMap<>();
            groupNameRef.updateChildren(groupMapKey);
            groupMessKeyRef=groupNameRef.child(messKey);
            HashMap<String,Object> messInforMap=new HashMap<>();
            messInforMap.put("name",currentUserName);
            messInforMap.put("message",mess);
            messInforMap.put("date",currentDate);
            messInforMap.put("time",curentTime);
            groupMessKeyRef.updateChildren(messInforMap);



        }
    }
    private void DisplayMessenger(DataSnapshot dataSnapshot) {
        Iterator iterator=dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()){
            String chatDate=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatMess=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatName=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime=(String) ((DataSnapshot)iterator.next()).getValue();
            displayMess.append(chatName+" :\n"+chatMess+"\n"+chatTime+"    "+chatDate+"\n\n\n");
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }

    }

}
