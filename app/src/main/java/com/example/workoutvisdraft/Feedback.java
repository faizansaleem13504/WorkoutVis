package com.example.workoutvisdraft;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Feedback extends AppCompatActivity {
    ImageButton homeButtom;
    VideoView videoView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback);
        Intent i=getIntent();
        Uri myUri=i.getParcelableExtra("imageUri");
        MediaController mediaController = new MediaController(this);
        videoView=findViewById(R.id.videoView);
        videoView.setMediaController(mediaController);
        if(myUri!=null) {
            videoView.setVideoURI(myUri);
            videoView.start();
        }
        homeButtom=findViewById(R.id.homeButton);
        homeButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Feedback.this,MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }


}
