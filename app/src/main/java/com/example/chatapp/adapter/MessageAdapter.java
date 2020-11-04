package com.example.chatapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.activity.ImageViewActivity;
import com.example.chatapp.activity.MainActivity;
import com.example.chatapp.model.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> messList;
    private Context mContext;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    public MessageAdapter(List<Messages> messList, Context mContext) {
        this.messList = messList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_mess_layout, parent, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        String messSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = messList.get(position);

        String fromUserId = messages.getFrom();
        String fromMessType = messages.getType();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    String receiveProfileImage = dataSnapshot.child("image").getValue().toString();
                    Glide.with(mContext).load(receiveProfileImage).into(holder.receiveImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        holder.tvReceiveMess.setVisibility(View.GONE);
        holder.receiveImage.setVisibility(View.GONE);
        holder.tvTimeReceive.setVisibility(View.GONE);
        holder.tvSendMess.setVisibility(View.GONE);
        holder.tvTimeSend.setVisibility(View.GONE);
        holder.messSendImg.setVisibility(View.GONE);
        holder.messReceiveImg.setVisibility(View.GONE);
        if (fromMessType.equals("text")) {
            if (fromUserId.equals(messSenderId)) {
                holder.tvSendMess.setVisibility(View.VISIBLE);
                holder.tvTimeSend.setVisibility(View.VISIBLE);
                holder.tvSendMess.setBackgroundResource(R.drawable.send_mess_layout);
                holder.tvSendMess.setText(messages.getMessage() );
                holder.tvTimeSend.setText(messages.getTime());
            }
            else {
                holder.tvReceiveMess.setVisibility(View.VISIBLE);
                holder.receiveImage.setVisibility(View.VISIBLE);
                holder.tvTimeReceive.setVisibility(View.VISIBLE);
                holder.tvReceiveMess.setBackgroundResource(R.drawable.receive_mess_layout);
                holder.tvReceiveMess.setText(messages.getMessage());
                holder.tvTimeReceive.setText(messages.getTime());
            }
        }
        else if (fromMessType.equals("image")) {
            if (fromUserId.equals(messSenderId)) {
                holder.messSendImg.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(messages.getMessage()).into(holder.messSendImg);
            }
            else {
                holder.receiveImage.setVisibility(View.VISIBLE);
                holder.messReceiveImg.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(messages.getMessage()).into(holder.messReceiveImg);
            }
        }
        else if (fromMessType.equals("pdf") || fromMessType.equals("docx")) {
            if (fromUserId.equals(messSenderId)) {
                holder.messSendImg.setVisibility(View.VISIBLE);
                holder.messSendImg.setBackgroundResource(R.drawable.file_image);

            } else {
                holder.receiveImage.setVisibility(View.VISIBLE);
                holder.messReceiveImg.setVisibility(View.VISIBLE);
                holder.messReceiveImg.setBackgroundResource(R.drawable.file_image);
            }
        }
        if (fromUserId.equals(messSenderId)) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (messList.get(position).getType().equals("pdf") || messList.get(position).getType().equals("docx")) {
                        CharSequence option[] = new CharSequence[]{
                                "Delete for me",
                                "Download and view this document",
                                "Cancel",
                                "Delete for everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    deleteSentMess(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                } else if (i == 1) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (i == 3) {
                                    deleteMessForEveryone(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();

                    } else if (messList.get(position).getType().equals("text")) {
                        CharSequence option[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel",
                                "Delete for everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {

                                    deleteSentMess(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (i == 2) {
                                    deleteMessForEveryone(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();

                    }
                    else if (messList.get(position).getType().equals("image")) {
                        CharSequence option[] = new CharSequence[]{
                                "Delete for me",
                                "View this Image",
                                "Cancel",
                                "Delete for everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    deleteSentMess(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                } else if (i == 1) {
                                    Intent intent=new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                                    intent.putExtra("url",messList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                } else if (i == 3) {
                                    deleteMessForEveryone(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();

                    }
                }
            });
        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (messList.get(position).getType().equals("pdf") || messList.get(position).getType().equals("docx")) {
                        CharSequence option[] = new CharSequence[]{
                                "Delete for me",
                                "Download and view this document",
                                "Cancel"

                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    deleteReceiveMess(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                } else if (i == 1) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(messList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();

                    } else if (messList.get(position).getType().equals("text")) {
                        CharSequence option[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    deleteReceiveMess(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();

                    }
                    else if (messList.get(position).getType().equals("image")) {
                        CharSequence option[] = new CharSequence[]{
                                "Delete for me",
                                "View this Image",
                                "Cancel"

                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");

                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if (i == 0) {
                                    deleteReceiveMess(position,holder);
                                    Intent intent=new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                } else if (i == 1) {
                                    Intent intent=new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                                    intent.putExtra("url",messList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();

                    }
                }
            });
        }

    }
    private void deleteSentMess(final  int position,final MessageViewHolder holder){
        DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(messList.get(position).getFrom())
                .child(messList.get(position).getTo())
                .child(messList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(mContext, "Delete success", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void deleteReceiveMess(final  int position,final MessageViewHolder holder){
        DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(messList.get(position).getTo())
                .child(messList.get(position).getFrom())
                .child(messList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(mContext, "Delete success", Toast.LENGTH_SHORT).show();
                }
                else  {
                    Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteMessForEveryone(final  int position,final MessageViewHolder holder){
        final DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(messList.get(position).getTo())
                .child(messList.get(position).getFrom())
                .child(messList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    rootRef.child("Messages").child(messList.get(position).getFrom())
                            .child(messList.get(position).getTo())
                            .child(messList.get(position).getMessageId())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(mContext, "Delete success", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return messList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvReceiveMess, tvSendMess, tvTimeReceive, tvTimeSend;
        CircleImageView receiveImage;
        ImageView messSendImg, messReceiveImg;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReceiveMess = itemView.findViewById(R.id.tv_receive_mess);
            tvTimeReceive = itemView.findViewById(R.id.tv_time_receive);
            tvSendMess = itemView.findViewById(R.id.tv_send_mess);
            tvTimeSend = itemView.findViewById(R.id.tv_time_send);
            receiveImage = itemView.findViewById(R.id.mess_profile);
            messSendImg = itemView.findViewById(R.id.message_sender_image_view);
            messReceiveImg = itemView.findViewById(R.id.message_receiver_image_view);
        }
    }
}
