package com.example.chatapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;

public class ImageViewActivity extends AppCompatActivity {
    private ImageView imageView;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        imageView=findViewById(R.id.img_image);
        imageUrl=getIntent().getStringExtra("url");
        Glide.with(getApplicationContext()).load(imageUrl).into(imageView);
    }
}
