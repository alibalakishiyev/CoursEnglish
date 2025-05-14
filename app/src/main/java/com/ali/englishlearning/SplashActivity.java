package com.ali.englishlearning;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.ali.englishlearning.authenticator.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // VideoView komponentini tap
        VideoView videoView = findViewById(R.id.videoView);

        // Video faylını URI ilə təyin et
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.inside);

        videoView.setVideoURI(videoUri);

        // Videonu avtomatik oynatmaq
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true); // Videonu dövr edən vəziyyətə gətir
            videoView.start();  // Videonu başlad
        });

        Thread mSplashThread;
        mSplashThread=new Thread(){
            @Override public void run(){
                try{
                    synchronized (this){
                        wait(2500);
                    }
                }catch (InterruptedException ignored){

                }finally {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        // Əgər artıq login olubsa
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    } else {
                        // Login olmayıbsa
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                    finish();
                }
            }
        };
        mSplashThread.start();
    }
}