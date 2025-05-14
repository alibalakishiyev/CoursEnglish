package com.ali.englishlearning.cours;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color; // Bu sətri yuxarıda import etməyi unutma


import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.ali.englishlearning.MainActivity;
import com.ali.englishlearning.R;
import com.ali.englishlearning.User.EditProfile;
import com.ali.englishlearning.authenticator.Login;
import com.ali.englishlearning.authenticator.Register;
import com.ali.englishlearning.main.CoursList;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public class QuestionsCourse extends AppCompatActivity {

    private LinearLayout wordContainer;
    private MediaPlayer mediaPlayer;
    private ImageButton menuButton;
    private AdView mAdView2;
    private FirebaseAuth fAuth;
    private String coursePath;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions_course);

        wordContainer = findViewById(R.id.wordContainer);

        coursePath = getIntent().getStringExtra("jsonFile");
        if (coursePath != null) {
            loadWordsFromJson(coursePath); // JSON və path birdir
        }

        menuButton = findViewById(R.id.menu_button);
        fAuth = FirebaseAuth.getInstance();


        // İstifadəçi İkonu
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // PopupMenu yarat
                PopupMenu popupMenu = new PopupMenu(QuestionsCourse.this, menuButton);
                popupMenu.getMenuInflater().inflate(R.menu.bottom_nav_menu, popupMenu.getMenu());

                // **Anonim istifadəçi yoxlanışı**
                if (fAuth.getCurrentUser() == null) {
                    // Anonim istifadəçidirsə, yalnız Login və Sign Up göstər
                    popupMenu.getMenu().findItem(R.id.menu_profile).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.menu_logout).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.menu_login).setVisible(true);
                    popupMenu.getMenu().findItem(R.id.menu_signup).setVisible(true);
                } else {
                    // İstifadəçi daxil olubsa, yalnız Profil və Logout göstər
                    popupMenu.getMenu().findItem(R.id.menu_profile).setVisible(true);
                    popupMenu.getMenu().findItem(R.id.menu_logout).setVisible(true);
                    popupMenu.getMenu().findItem(R.id.menu_login).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.menu_signup).setVisible(false);
                }

                // PopupMenu itemlərinə klik hadisəsi
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.menu_profile) {
                            startActivity(new Intent(QuestionsCourse.this, EditProfile.class));
                            return true;
                        } else if (item.getItemId() == R.id.menu_logout) {

                            // Çıxışdan əvvəl tarixçəni saxlama flagini sıfırlayaq
                            SharedPreferences prefs = getSharedPreferences("ChatPrefs", MODE_PRIVATE);
                            prefs.edit().putBoolean("keep_history", false).apply();


                            fAuth.signOut();
                            Toast.makeText(QuestionsCourse.this, "Uğurla çıxış edildi!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(QuestionsCourse.this, Login.class));
                            menuButton.setVisibility(View.VISIBLE);
                            return true;
                        } else if (item.getItemId() == R.id.menu_login) {
                            startActivity(new Intent(QuestionsCourse.this, Login.class));
                            return true;
                        } else if (item.getItemId() == R.id.menu_signup) {
                            startActivity(new Intent(QuestionsCourse.this, Register.class));
                            return true;
                        }
                        return false;
                    }
                });

                // PopupMenu göstər
                popupMenu.show();
            }
        });

        mAdView2 = findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView2.loadAd(adRequest);

        getOnBackPressedDispatcher().addCallback(this, callback);


    }

    private void loadWordsFromJson(String fileName) {
        try {
            AssetManager assetManager = getAssets();
            InputStream is = assetManager.open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonString = new String(buffer, "UTF-8");

            JSONObject jsonObject = new JSONObject(jsonString);

            String lesson = jsonObject.getString("lesson");
            String level = jsonObject.getString("level");
            String source = jsonObject.getString("source");

            TextView lessonInfo = new TextView(this);
            lessonInfo.setText("Lesson: " + lesson + "\nLevel: " + level + "\nSource: " + source);
            lessonInfo.setTextSize(20f);
            lessonInfo.setTextColor(Color.parseColor("#3F51B5")); // Göy rəng
            wordContainer.addView(lessonInfo);

            String readingPractice = jsonObject.getString("readingPractice");
            TextView readingText = new TextView(this);
            readingText.setText("\nReading Practice:\n" + readingPractice);
            readingText.setTextSize(20f);
            readingText.setTextColor(Color.parseColor("#4CAF50")); // Yaşıl rəng
            wordContainer.addView(readingText);

            JSONArray targetWords = jsonObject.getJSONArray("targetWords");
            TextView targetHeader = new TextView(this);
            targetHeader.setText("\nTarget Words:");
            targetHeader.setTextSize(22f);
            targetHeader.setTextColor(Color.parseColor("#FF5722")); // Narıncı rəng
            wordContainer.addView(targetHeader);

            for (int i = 0; i < targetWords.length(); i++) {
                JSONObject wordObj = targetWords.getJSONObject(i);
                String word = wordObj.getString("word");
                String meaning = wordObj.getString("meaning");
                String example = wordObj.getString("example");

                // Sadəcə sözü göstər
                TextView wordText = new TextView(this);
                wordText.setText("Word: " + word);
                wordText.setTextSize(22f);
                wordText.setTextColor(Color.parseColor("#000000")); // Qara
                wordText.setPadding(0, 20, 0, 10);

                // Meaning və Example ayrıca, amma GİZLİ (visibility: GONE)
                TextView meaningText = new TextView(this);
                meaningText.setText("Meaning: " + meaning);
                meaningText.setTextSize(20f);
                meaningText.setVisibility(View.GONE);

                TextView exampleText = new TextView(this);
                exampleText.setText("Example: " + example);
                exampleText.setTextSize(20f);
                exampleText.setVisibility(View.GONE);

                // Sözə klikləyəndə meaning və example görünsün
                wordText.setOnClickListener(v -> {
                    if (meaningText.getVisibility() == View.GONE) {
                        meaningText.setVisibility(View.VISIBLE);
                        exampleText.setVisibility(View.VISIBLE);
                    } else {
                        meaningText.setVisibility(View.GONE);
                        exampleText.setVisibility(View.GONE);
                    }
                });

                wordContainer.addView(wordText);
                wordContainer.addView(meaningText);
                wordContainer.addView(exampleText);


                Button playBtn = new Button(this);
                playBtn.setText("Play");
                String jsonFolder = coursePath.substring(0, coursePath.lastIndexOf("/")); // məsələn Cours_A1/cours-1
                String audioPath = jsonFolder + "/" + word.toLowerCase(Locale.ROOT) + ".mp3";

                playBtn.setOnClickListener(v -> playAudio(audioPath));
                wordContainer.addView(playBtn);


                Button stopBtn = new Button(this);
                stopBtn.setText("Stop");
                stopBtn.setOnClickListener(v -> stopAudio());
                wordContainer.addView(stopBtn);
            }

            JSONArray otherWords = jsonObject.getJSONArray("otherWords");
            TextView otherHeader = new TextView(this);
            otherHeader.setText("\nOther Words:");
            otherHeader.setTextSize(21f);
            otherHeader.setTextColor(Color.parseColor("#9C27B0")); // Bənövşəyi rəng
            wordContainer.addView(otherHeader);

            for (int i = 0; i < otherWords.length(); i++) {
                JSONObject wordObj = otherWords.getJSONObject(i);
                String word = wordObj.getString("word");
                String meaning = wordObj.getString("meaning");

                // Word TextView
                TextView wordText = new TextView(this);
                wordText.setText("• " + word);
                wordText.setTextSize(20f);
                wordText.setTextColor(Color.parseColor("#F00606")); // Qırmızı
                wordText.setPadding(0, 20, 0, 10);

                // Meaning TextView - gizli
                TextView meaningText = new TextView(this);
                meaningText.setText("Meaning: " + meaning);
                meaningText.setTextSize(19f);
                meaningText.setTextColor(Color.DKGRAY);
                meaningText.setVisibility(View.GONE);

                // Klik hadisəsi: göstər/gizlət
                wordText.setOnClickListener(v -> {
                    if (meaningText.getVisibility() == View.GONE) {
                        meaningText.setVisibility(View.VISIBLE);
                    } else {
                        meaningText.setVisibility(View.GONE);
                    }
                });

                // Layout-a əlavə et
                wordContainer.addView(wordText);
                wordContainer.addView(meaningText);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void playAudio(String audioPath) {
        try {
            stopAudio();
            AssetFileDescriptor afd = getAssets().openFd(audioPath);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Toast.makeText(this, "Səs tapılmadı: " + audioPath, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }







    private void stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAudio();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudio();
    }


    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(QuestionsCourse.this);
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

                    if (fAuth.getCurrentUser() != null) {
                        startActivity(new Intent(getApplicationContext(), CoursList.class));
                        finish();
                    }else{
                        startActivity(new Intent(QuestionsCourse.this, Login.class));
                        finish();
                    }

                }
            });
            materialAlertDialogBuilder.show();
        }
    };




}
