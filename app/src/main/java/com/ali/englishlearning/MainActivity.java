package com.ali.englishlearning;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ali.englishlearning.User.EditProfile;
import com.ali.englishlearning.authenticator.Login;
import com.ali.englishlearning.authenticator.Register;
import com.ali.englishlearning.main.MainPage;
import com.ali.englishlearning.stt.SpeechToText;
import com.ali.englishlearning.textotext.TextToText;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnCours,menuButton,btnExample,btnTTT;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private long lastAdShownTime = 0; // Son reklamın göstərildiyi vaxt (millisaniyə ilə)
    private final long AD_INTERVAL = 60000; // Reklamlar arasındakı vaxt (60 saniyə)
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        loadInterstitialAd();

        btnCours = findViewById(R.id.imgBtn);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        menuButton = findViewById(R.id.menu_button);
        btnExample = findViewById(R.id.btnExample);
        btnTTT = findViewById(R.id.btnTTT);



        btnCours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MainPage.class);
             startActivity(intent);
            }
        });

        btnExample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SpeechToText.class);
                startActivity(intent);
            }
        });

        btnTTT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this , TextToText.class);
                startActivity(intent);
            }
        });

        // İstifadəçi İkonu
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // PopupMenu yarat
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, menuButton);
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
                            startActivity(new Intent(MainActivity.this, EditProfile.class));
                            return true;
                        } else if (item.getItemId() == R.id.menu_logout) {

                            // Çıxışdan əvvəl tarixçəni saxlama flagini sıfırlayaq
                            SharedPreferences prefs = getSharedPreferences("ChatPrefs", MODE_PRIVATE);
                            prefs.edit().putBoolean("keep_history", false).apply();


                            fAuth.signOut();
                            startActivity(new Intent(MainActivity.this, Login.class));
                            Toast.makeText(MainActivity.this, "Uğurla çıxış edildi!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, Login.class));
                            menuButton.setVisibility(View.VISIBLE);
                            return true;
                        } else if (item.getItemId() == R.id.menu_login) {
                            startActivity(new Intent(MainActivity.this, Login.class));
                            return true;
                        } else if (item.getItemId() == R.id.menu_signup) {
                            startActivity(new Intent(MainActivity.this, Register.class));
                            return true;
                        }
                        return false;
                    }
                });

                // PopupMenu göstər
                popupMenu.show();
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        getOnBackPressedDispatcher().addCallback(this, callback);

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
            mInterstitialAd.show(MainActivity.this);
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
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(MainActivity.this);
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
                    finish();

                }
            });
            materialAlertDialogBuilder.show();
        }
    };

}