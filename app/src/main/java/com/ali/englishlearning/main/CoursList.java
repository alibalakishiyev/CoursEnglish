package com.ali.englishlearning.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ali.englishlearning.MainActivity;
import com.ali.englishlearning.R;
import com.ali.englishlearning.cours.Course;
import com.ali.englishlearning.cours.Pernouns;
import com.ali.englishlearning.cours.QuestionsCourse;
import com.ali.englishlearning.utils.Algo;
import com.ali.englishlearning.utils.AlgoAdapter;
import com.ali.englishlearning.utils.AlgoListener;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.util.ArrayList;

public class CoursList extends AppCompatActivity implements AlgoListener {

    private ArrayList<Course> courses = new ArrayList<>();
    private AdView mAdView4;
    private InterstitialAd mInterstitialAd;
    private long lastAdShownTime = 0; // Son reklamın göstərildiyi vaxt (millisaniyə ilə)
    private final long AD_INTERVAL = 60000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cours_list);
        loadInterstitialAd();


        // Kursları avtomatik əlavə edirik
        for (int i = 1; i <= 10; i++) {
            String courseName = "cours-" + i + ".json";
            String filePath = "Cours_A1/cours-" + i + "/" + courseName; // Kurs yolu düzəldildi


            // Kursları əlavə edirik
            courses.add(new Course(courseName, filePath));
        }


        ArrayList<Algo> arrayList = new ArrayList<>();
        try {
            String[] jsonDirs = getAssets().list("Cours_A1"); // 'Cours_A1' qovluğunu oxuyuruq
            if (jsonDirs != null) {
                for (String dirName : jsonDirs) {
                    String fullDirPath = "Cours_A1/" + dirName;
                    String[] jsonFiles = getAssets().list(fullDirPath);

                    if (jsonFiles != null) {
                        for (String fileName : jsonFiles) {
                            if (fileName.endsWith(".json")) {
                                String courseName = fileName.replace(".json", "").replace("-", " ").toUpperCase();
                                Class<?> activityClass;

                                if (dirName.startsWith("pernouns")) {
                                    activityClass = Pernouns.class;
                                } else if (dirName.startsWith("cours-")) {
                                    activityClass = QuestionsCourse.class;
                                } else {
                                    continue; // Digər qovluqları atla
                                }

                                arrayList.add(new Algo(
                                        R.drawable.cours1,
                                        courseName,
                                        activityClass,
                                        fullDirPath + "/" + fileName
                                ));
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Toast ilə kursların sayını göstəririk
        Toast.makeText(this, "Courses Loaded: " + arrayList.size(), Toast.LENGTH_SHORT).show();

        AlgoAdapter algoAdapter = new AlgoAdapter(arrayList, this);
        RecyclerView recyclerView = findViewById(R.id.main_recycler_view);
        recyclerView.setAdapter(algoAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        mAdView4 = findViewById(R.id.adView4);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView4.loadAd(adRequest);

        getOnBackPressedDispatcher().addCallback(this, callback);

    }

    @Override
    public void onAlgoSelected(Algo algo) {
        Intent intent = new Intent(this, algo.activityClazz);
        intent.putExtra("jsonFile", algo.jsonFile);
        intent.putExtra("name", algo.algoText);

        // Seçilən kurs obyektini tapıb göndəririk
        Course selectedCourse = getCourseByName(algo.algoText);
        if (selectedCourse != null) {
            intent.putExtra("coursePath", selectedCourse.getCoursePath());
        }

        startActivity(intent);
    }

    // Kursu ad ilə tapmaq üçün metod
    private Course getCourseByName(String courseName) {
        for (Course course : courses) {
            if (course.getCourseName().equals(courseName)) {
                return course;
            }
        }
        return null; // Əgər kurs tapılmasa
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, "ca-app-pub-5367924704859976/2123097507", adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
                Log.d("MainActivity", "Interstitial reklamı uğurla yükləndi.");
                showInterstitialAd();

                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d("MainActivity", "Reklam bağlandı. Yeni reklam yüklənir...");
                        mInterstitialAd = null; // Mövcud reklam obyektini null edin.
                        loadInterstitialAd();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        Log.d("MainActivity", "Reklam göstərilmədi: " + adError.getMessage());
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d("MainActivity", "Reklam göstərilir.");
                    }

                });

            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                mInterstitialAd = null;
                Log.d("MainActivity", "Interstitial reklamı yüklənmədi: " + loadAdError.getMessage());
            }
        });


    }

    private void showInterstitialAd() {
        long currentTime = System.currentTimeMillis();
        if (mInterstitialAd != null && (currentTime - lastAdShownTime >= AD_INTERVAL)) {
            Log.d("MainActivity", "Reklam göstərilir...");
            mInterstitialAd.show(CoursList.this);
            lastAdShownTime = currentTime; // Son reklam göstərilmə vaxtını yeniləyin
        } else if (mInterstitialAd == null) {
            Log.d("MainActivity", "Reklam hazır deyil.");
        } else {
            Log.d("MainActivity", "Reklam vaxtı tamamlanmayıb. Gözlənilir...");
        }
    }

    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(CoursList.this);
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
                    startActivity(new Intent(getApplicationContext(), MainPage.class));
                    finish();
                }
            });
            materialAlertDialogBuilder.show();
        }
    };
}
