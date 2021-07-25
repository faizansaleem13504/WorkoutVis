package com.example.workoutvisdraft;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Feedback extends AppCompatActivity {
    ImageButton homeButtom;
    VideoView videoView;
    TextView total,correct,incorrect,incorrectRepsNumber;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback);
        Intent in=getIntent();

        Uri myUri=in.getParcelableExtra("filePath");
        int [][]cycles=(int[][])in.getSerializableExtra("cycles");
        float []preds=(float[])in.getSerializableExtra("predictions");
        total=findViewById(R.id.totalRepsText);
        total.setText(String.valueOf(preds.length));
        MediaController mediaController = new MediaController(this);
        videoView=findViewById(R.id.videoView);
        correct=findViewById(R.id.correctRepsText);
        incorrect=findViewById(R.id.incorrectRepsText);
        incorrectRepsNumber=findViewById(R.id.incorrectRepsNumber);
        int correctCount=0;
        String incorrectNum="";
        int incorrectCount=0;
        if(preds!=null) {
            for (int i = 0; i < preds.length; i++) {
                if (preds[i] >= 0.52)
                    correctCount++;
                else {
                    incorrectCount++;
                    if (incorrectNum.equals("")) {
                        incorrectNum += String.valueOf(i + 1);
                    } else {
                        incorrectNum += ", ";
                        incorrectNum += String.valueOf(i + 1);
                    }
                }
            }
        }
        correct.setText(String.valueOf(correctCount));
        incorrect.setText(String.valueOf(incorrectCount));

        if(incorrectNum.equals(""))
            incorrectRepsNumber.setText("None");
        else{
            incorrectRepsNumber.setText(String.valueOf(incorrectNum));
        }
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
