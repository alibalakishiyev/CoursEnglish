package com.ali.englishlearning.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.ali.englishlearning.MainActivity;
import com.ali.englishlearning.R;
import com.ali.englishlearning.authenticator.Login;
import com.ali.englishlearning.game.CrosswordList;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

public class MainPage extends AppCompatActivity {
    private ImageButton btnCros, btnQuest;
    private VideoView videoView;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_page);

        btnCros = findViewById(R.id.buttonCros);
        btnQuest = findViewById(R.id.buttonQuest);
        videoView = findViewById(R.id.videoV);
        fAuth = FirebaseAuth.getInstance();


        btnQuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainPage.this, CoursList.class));
            }
        });

        btnCros.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainPage.this, CrosswordList.class));
            }
        });

        // raw/england2.webm faylını VideoView-a yükləyirik
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.england2);
        videoView.setVideoURI(videoUri);
        videoView.start();

        // Video bitdikdə yenidən başlasın
        videoView.setOnCompletionListener(mp -> videoView.start());

        getOnBackPressedDispatcher().addCallback(this, callback);


    }

    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(MainPage.this);
            materialAlertDialogBuilder.setTitle(R.string.app_name);
            materialAlertDialogBuilder.setMessage("Are you sure want to exit the app?");
            materialAlertDialogBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.dismiss();
                }
            });
            materialAlertDialogBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {

                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();

                }
            });
            materialAlertDialogBuilder.show();
        }
    };
}