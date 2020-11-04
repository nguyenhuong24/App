package com.example.chatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.adapter.TabAccessPagerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ViewPager pagerMain;
    private TabLayout tabLayoutMain;
    private TabAccessPagerAdapter pagerAdapter;
    private FirebaseAuth mAuth;
    private DatabaseReference root;
    private String currentUserId;
    private FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
//        currentUserId=mAuth.getCurrentUser().getUid();
        root= FirebaseDatabase.getInstance().getReference();
        inIt();
        actionToolBar();
        actionPager();
    }

    private void actionPager() {
        pagerAdapter=new TabAccessPagerAdapter(getSupportFragmentManager());
        pagerMain.setAdapter(pagerAdapter);
        tabLayoutMain.setupWithViewPager(pagerMain);
    }

    private void actionToolBar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ChatApp");
    }

    private void inIt() {
        mToolbar=findViewById(R.id.main_page_toolbar);
        pagerMain=findViewById(R.id.viewPagerMain);
        tabLayoutMain=findViewById(R.id.tab_main);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser==null){
            sendUserToLoginActivity();
        }else {
            updateUserStatus("online");
            VerifyUserExist();
        }
    }


    @Override
    protected void onDestroy() {
        if (currentUser!=null){
            updateUserStatus("offline");
        }
        super.onDestroy();
    }

    private void VerifyUserExist() {
        String currentUserId=mAuth.getCurrentUser().getUid();
        root.child("Users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("name").exists()){
                    Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_SHORT).show();
                }else {
                    sendUserToSettingActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.opotions,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.item_logout){
            updateUserStatus("offline");
            mAuth.signOut();
            sendUserToLoginActivity();

        }else if(item.getItemId()==R.id.item_find_friend){
            sendUserToFindFriendActivity();

        }else if(item.getItemId()==R.id.item_setting){
            sendUserToSettingActivity();
        }else if(item.getItemId()==R.id.item_creat_group){
            RequestNewGroup();
        }
        return true;
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group name: ");
        final EditText groupNameField=new EditText(MainActivity.this);
        groupNameField.setHint("My Group");
        builder.setView(groupNameField);
        builder.setPositiveButton("Creat", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName=groupNameField.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(MainActivity.this,"Please write namre group",Toast.LENGTH_LONG).show();

                }else {
                    CreateNewGroup(groupName);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void CreateNewGroup(String groupName) {
        root.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this,"group is creat success",Toast.LENGTH_SHORT).show();;
                }
            }
        });
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    private void sendUserToSettingActivity() {
        Intent settingIntent=new Intent(MainActivity.this,SettingActivity.class);
      //  settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingIntent);
     //   finish();
    }
    private void sendUserToFindFriendActivity() {
        Intent findFrIntent=new Intent(MainActivity.this,FindFriendActivity.class);
       // findFrIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(findFrIntent);
    //    finish();
    }
    private void updateUserStatus(String state){
        currentUserId=currentUser.getUid();
        String saveCurrentTime,saveCurrentDate;
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat curretnDate=new SimpleDateFormat("dd MMM, yyyy");
        saveCurrentDate=curretnDate.format(calendar.getTime());

        SimpleDateFormat curretTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=curretTime.format(calendar.getTime());

        HashMap<String,Object> onlineState=new HashMap<>();
        onlineState.put("time",saveCurrentTime);
        onlineState.put("date",saveCurrentDate);
        onlineState.put("state",state);
        root.child("Users").child(currentUserId).child("userState").updateChildren(onlineState);


    }
}
