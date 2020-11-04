package com.example.chatapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.adapter.MessageAdapter;
import com.example.chatapp.model.Messages;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String messReceiveId,messReceiveName,messReceiveImage,messSenderId;
    private TextView userName,lastSeen;
    private CircleImageView imgProfile;
    private Toolbar chatToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private ImageButton sendMess,sendFile;
    private EditText edtMess;

    private final List<Messages> list=new ArrayList<>();
    private LinearLayoutManager manager;
    private MessageAdapter adapter;
    private RecyclerView rvChat;
    private String saveCurrentTime,saveCurrentDate;
    private String check="", myUrl="";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth=FirebaseAuth.getInstance();
        rootRef= FirebaseDatabase.getInstance().getReference();
        messSenderId=mAuth.getCurrentUser().getUid();
        messReceiveId=getIntent().getExtras().get("visit_user_id").toString();
        messReceiveName=getIntent().getExtras().get("visit_user_name").toString();
        messReceiveImage=getIntent().getExtras().get("visit_image").toString();
        init();
        userName.setText(messReceiveName);
        Glide.with(getApplicationContext()).load(messReceiveImage).into(imgProfile);
        sendMess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        displayLastSeen();

        sendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence[] option=new CharSequence[]{
                  "Image",
                  "PDF",
                  "Word Files"
                };
                AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File");
                builder.setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0){
                            check="image";
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select Image"),438);
                        }
                        if (which==1){
                            check="pdf";
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent,"Select PDF File"),438);
                        }
                        if (which==2){
                            check="word";
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent,"Select Word File"),438);

                        }

                    }
                });
                builder.show();
            }
        });

        loadMess();
    }

    private void init() {
        chatToolBar=findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolBar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=layoutInflater.inflate(R.layout.custom_chat_bar_layout,null);
        actionBar.setCustomView(view);

        imgProfile=findViewById(R.id.custom_image);
        userName=findViewById(R.id.custom_name);
        lastSeen=findViewById(R.id.custom_last_seen);

        sendMess=findViewById(R.id.btn_sendMess);
        sendFile=findViewById(R.id.send_file);
        edtMess=findViewById(R.id.edt_input_mess);

        adapter=new MessageAdapter(list,getApplicationContext());
        rvChat=findViewById(R.id.rv_private_mess);
        manager=new LinearLayoutManager(this);
        rvChat.setLayoutManager(manager);
        rvChat.setAdapter(adapter);
        loading=new ProgressDialog(this);

        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat curretnDate=new SimpleDateFormat("dd MMM, yyyy");
        saveCurrentDate=curretnDate.format(calendar.getTime());

        SimpleDateFormat curretTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=curretTime.format(calendar.getTime());


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            loading.setTitle("Sending File");
            loading.setMessage("please wait");
            loading.setCanceledOnTouchOutside(false);
            loading.show();
            fileUri=data.getData();
            if (!check.equals("image")){
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Document File");

                final String messSenderRef="Messages/" + messSenderId + "/" + messReceiveId;
                final String messReceiveRef="Messages/" + messReceiveId + "/" + messSenderId;

                DatabaseReference userMessageKey=rootRef.child("Messages").child(messSenderId)
                        .child(messReceiveId).push();
                final String messagePushid=userMessageKey.getKey();
                final StorageReference filePath=storageReference.child(messagePushid+"."+check);
                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl=uri.toString();
                                Map messBody=new HashMap();
                                messBody.put("message",downloadUrl);
                                messBody.put("name",fileUri.getLastPathSegment());
                                messBody.put("type",check);
                                messBody.put("from",messSenderId);
                                messBody.put("to",messReceiveId);
                                messBody.put("messageId",messagePushid);
                                messBody.put("time",saveCurrentTime);
                                messBody.put("date",saveCurrentDate);

                                Map messDetail=new HashMap();
                                messDetail.put(messSenderRef+ "/" + messagePushid,messBody);
                                messDetail.put(messReceiveRef+ "/" + messagePushid,messBody);
                                rootRef.updateChildren(messDetail);
                                loading.dismiss();
                            }
                        }) .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loading.dismiss();
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double p=(100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                        loading.setMessage((int)p+" % Upload...");
                    }
                });
            }else  if (check.equals("image")){
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Image File");

                final String messSenderRef="Messages/" + messSenderId + "/" + messReceiveId;
                final String messReceiveRef="Messages/" + messReceiveId + "/" + messSenderId;

                DatabaseReference userMessageKey=rootRef.child("Messages").child(messSenderId)
                        .child(messReceiveId).push();
                final String messagePushid=userMessageKey.getKey();
                final StorageReference filePath=storageReference.child(messagePushid+"."+"jpg");
                uploadTask=filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downloadUrl=task.getResult();
                            myUrl=downloadUrl.toString();

                            Map messBody=new HashMap();
                            messBody.put("message",myUrl);
                            messBody.put("name",fileUri.getLastPathSegment());
                            messBody.put("type",check);
                            messBody.put("from",messSenderId);
                            messBody.put("to",messReceiveId);
                            messBody.put("messageId",messagePushid);
                            messBody.put("time",saveCurrentTime);
                            messBody.put("date",saveCurrentDate);

                            Map messDetail=new HashMap();
                            messDetail.put(messSenderRef+ "/" + messagePushid,messBody);
                            messDetail.put(messReceiveRef+ "/" + messagePushid,messBody);

                            rootRef.updateChildren(messDetail).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){
                                        loading.dismiss();
                                        Toast.makeText(ChatActivity.this, "mess send success", Toast.LENGTH_SHORT).show();
                                    }else {
                                        loading.dismiss();
                                        Toast.makeText(ChatActivity.this, "error", Toast.LENGTH_SHORT).show();
                                    }
                                    edtMess.setText("");
                                }
                            });

                        }
                    }
                });


            }else {
                loading.dismiss();
                Toast.makeText(this, "Nothing select ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayLastSeen(){
        rootRef.child("Users").child(messReceiveId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("userState").hasChild("state")){
                            String state=dataSnapshot.child("userState").child("state").getValue().toString();
                            String date=dataSnapshot.child("userState").child("date").getValue().toString();
                            String time=dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online")){
                                lastSeen.setText("online");
                            }else if (state.equals("offline")){
                                lastSeen.setText("Last seen: "+date+" "+time);
                            }

                        }
                        else {
                            lastSeen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    protected void loadMess(){
        rootRef.child("Messages").child(messSenderId).child(messReceiveId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages=dataSnapshot.getValue(Messages.class);
                        list.add(messages);
                        adapter.notifyDataSetChanged();
                        rvChat.smoothScrollToPosition(rvChat.getAdapter().getItemCount());
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

    private void sendMessage(){
        String mess=edtMess.getText().toString();
        if(TextUtils.isEmpty(mess)){
            Toast.makeText(this, "Please write your mess.....", Toast.LENGTH_SHORT).show();
        }else{
            String messSenderRef="Messages/" + messSenderId + "/" + messReceiveId;
            String messReceiveRef="Messages/" + messReceiveId + "/" + messSenderId;

            DatabaseReference userMessageKey=rootRef.child("Messages").child(messSenderId)
                    .child(messReceiveId).push();
            String messagePushid=userMessageKey.getKey();
            Map messBody=new HashMap();
            messBody.put("message",mess);
            messBody.put("type","text");
            messBody.put("from",messSenderId);
            messBody.put("to",messReceiveId);
            messBody.put("messageId",messagePushid);
            messBody.put("time",saveCurrentTime);
            messBody.put("date",saveCurrentDate);

            Map messDetail=new HashMap();
            messDetail.put(messSenderRef+ "/" + messagePushid,messBody);
            messDetail.put(messReceiveRef+ "/" + messagePushid,messBody);

            rootRef.updateChildren(messDetail).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "mess send success", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(ChatActivity.this, "error", Toast.LENGTH_SHORT).show();
                    }
                    edtMess.setText("");
                }
            });
        }
    }
}
