package com.ali.englishlearning.game;


import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.VideoView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.ali.englishlearning.R;
import com.ali.englishlearning.main.MainPage;
import com.ali.englishlearning.main.config.Algon;
import com.ali.englishlearning.main.config.AlgonAdapter;
import com.ali.englishlearning.main.config.AlgonListener;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

public class CrosswordList extends AppCompatActivity implements AlgonListener {

    private InterstitialAd mInterstitialAd;
    private long lastAdShownTime = 0; // Son reklamın göstərildiyi vaxt (millisaniyə ilə)
    private final long AD_INTERVAL = 60000; // Reklamlar arasındakı vaxt (60 saniyə)
    private AdView mAdView3;
    private VideoView videoView;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crassword_list);

        loadInterstitialAd();

        videoView = findViewById(R.id.videoView3);


        ArrayList<Algon> arrayList = new ArrayList<>();
        arrayList.add(new Algon(R.drawable.cross1, "Krossvord 1", Crossword.class,
                "HtmlPage/crossword1.html", "questionsJSON/questions1.json"));
        arrayList.add(new Algon(R.drawable.cross1, "Krossvord 2", Crossword.class,
                "HtmlPage/crossword2.html", "questionsJSON/questions2.json"));
        arrayList.add(new Algon(R.drawable.cross1, "Krossvord 3", Crossword.class,
                "HtmlPage/crossword3.html", "questionsJSON/questions3.json"));
        arrayList.add(new Algon(R.drawable.cross1, "Krossvord 4", Crossword.class,
                "HtmlPage/crossword4.html", "questionsJSON/questions4.json"));

        AlgonAdapter algoAdapter = new AlgonAdapter(arrayList, this);
        RecyclerView recyclerView = findViewById(R.id.main_recycler_view);
        recyclerView.setAdapter(algoAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // raw/england2.webm faylını VideoView-a yükləyirik
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.cours12);
        videoView.setVideoURI(videoUri);
        videoView.start();

        // Video bitdikdə yenidən başlasın
        videoView.setOnCompletionListener(mp -> videoView.start());


        mAdView3 = findViewById(R.id.adView3);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView3.loadAd(adRequest);

        getOnBackPressedDispatcher().addCallback(this, callback);


    }

    @Override
    public void onAlgoSelected(Algon algo) {
        Intent intent = new Intent(this, Crossword.class);
        intent.putExtra("htmlFile", algo.htmlFile); // "HtmlPage/crossword1.html"
        intent.putExtra("jsonFile", algo.jsonFile); // "questionsJSON/questions1.json"
        startActivity(intent);
    }


    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, "ca-app-pub-5367924704859976/2123097507", adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
                Log.d("CrosswordList", "Interstitial reklamı uğurla yükləndi.");
                showInterstitialAd();

                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d("CrosswordList", "Reklam bağlandı. Yeni reklam yüklənir...");
                        mInterstitialAd = null; // Mövcud reklam obyektini null edin.
                        loadInterstitialAd();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        Log.d("CrosswordList", "Reklam göstərilmədi: " + adError.getMessage());
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d("CrosswordList", "Reklam göstərilir.");
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
            mInterstitialAd.show(CrosswordList.this);
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
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(CrosswordList.this);
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
