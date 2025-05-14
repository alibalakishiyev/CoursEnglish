package com.ali.englishlearning.game;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.ali.englishlearning.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Crossword extends AppCompatActivity {

    private static final String PREFS_NAME = "CrosswordPrefs";    private AdView mAdView;
    private LinearLayout horizontalCont, verticalCon;
    private Map<String, String> answerMap = new HashMap<>();
    private WebView webView;
    private MediaPlayer mediaPlayer;
    private String htmlFile;
    private String jsonFile;
    final MaterialButton[] selectedButton = {null};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crossword);



        // Intent-dən məlumatları al
        Intent intent = getIntent();
        htmlFile = getIntent().getStringExtra("htmlFile");
        jsonFile = getIntent().getStringExtra("jsonFile");

        // UI komponentlərini tapırıq
        horizontalCont = findViewById(R.id.horizontalText);
        verticalCon = findViewById(R.id.verticalText);
        webView = findViewById(R.id.webView);


        loadWebPage();
        // JSON-dan sözləri yüklə
        loadWordsFromJson(jsonFile);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);

        // JavaScript interfeysini əlavə edin
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        // Xətaları logla
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("WebView", consoleMessage.message() + " at " +
                        consoleMessage.sourceId() + ":" + consoleMessage.lineNumber());
                return true;
            }
        });





        // Reklam
        mAdView = findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    }



    private void loadWebPage() {
        if (htmlFile != null) {
            String url = "file:///android_asset/" + htmlFile;
            webView.loadUrl(url);
        } else {
            // Default fayl yüklə
            webView.loadUrl("file:///android_asset/HtmlPage/crossword1.html");
        }
    }



    public class WebAppInterface {
        private final Activity activity;

        private AlertDialog currentDialog;



        public WebAppInterface(Activity activity) {
            this.activity = activity;
        }

        @JavascriptInterface
        public String getJsonFileName() {
            return jsonFile; // JSON fayl adını qaytarır
        }


        @JavascriptInterface
        public void playBingo() {
            runOnUiThread(() -> {
                try {
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                    }

                    // Fayl yolunu dinamik et
                    String soundFile = "sounds/bingo_" + htmlFile.replace(".html", ".mp3");
                    try {
                        AssetFileDescriptor afd = getAssets().openFd(soundFile);
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        mediaPlayer.prepareAsync();
                        // ... qalan kod eyni qalır ...
                    } catch (IOException e) {
                        // Əgər xüsusi fayl tapılmasa, standart səs işlə
                        AssetFileDescriptor afd = getAssets().openFd("sounds/bingo.mp3");
                        // ... qalan kod eyni qalır ...
                    }
                } catch (IOException e) {
                    Log.e("Audio", "Səs faylı oxunarkən xəta", e);
                }
            });
        }


        @JavascriptInterface
        public void clearData() {
            SharedPreferences.Editor editor = activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();
            activity.runOnUiThread(() ->
                    Toast.makeText(activity, "Bütün cavablar silindi", Toast.LENGTH_SHORT).show()
            );
        }

        @JavascriptInterface
        public void onCellClick(int row, int col, String number, String direction) {
            Log.d("WebApp", "onCellClick çağırıldı: row=" + row + ", col=" + col);

            activity.runOnUiThread(() -> showLetterDialog(row, col));
        }


        private void showLetterDialog(int row, int col) {
            // Əvvəlki dialoqu bağla
            if (currentDialog != null && currentDialog.isShowing()) {
                currentDialog.dismiss();
            }

            // Custom dialog layout yüklə
            View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_letter_selection, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setView(dialogView);
            builder.setCancelable(true); // Geri düyməsi ilə bağlana bilsin

            currentDialog = builder.create();
            currentDialog.show();
            currentDialog.setCanceledOnTouchOutside(true); // Qırağa basanda bağlansın

            // Dialog ayarları
            Window window = currentDialog.getWindow();
            if (window != null) {
                window.setLayout(1100, 1300);
                window.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                webView.clearFocus();


            }

            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = activity.getCurrentFocus();
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            MaterialButton btnVertical = dialogView.findViewById(R.id.btn_vertical);
            MaterialButton btnHorizontal = dialogView.findViewById(R.id.btn_horizontal);

// Initialize with default background
            btnVertical.setBackgroundResource(R.drawable.letter_button_bg);
            btnHorizontal.setBackgroundResource(R.drawable.letter_button_bg);

            btnVertical.setOnClickListener(v -> {
                webView.evaluateJavascript("setWritingDirection('V')", null);
                Toast.makeText(activity, "Soldan-sağa yazma rejimi", Toast.LENGTH_SHORT).show();

                // Clear previous selection
                if (selectedButton[0] != null) {
                    selectedButton[0].setBackgroundResource(R.drawable.letter_button_bg);
                    selectedButton[0].setBackgroundTintList(null);
                }

                // Set new selection
                btnVertical.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                selectedButton[0] = btnVertical;

                // Ensure horizontal button is reset
                btnHorizontal.setBackgroundResource(R.drawable.letter_button_bg);
                btnHorizontal.setBackgroundTintList(null);
            });

            btnHorizontal.setOnClickListener(v -> {
                webView.evaluateJavascript("setWritingDirection('H')", null);
                Toast.makeText(activity, "Yuxarıdan-aşağı yazma rejimi", Toast.LENGTH_SHORT).show();

                // Clear previous selection
                if (selectedButton[0] != null) {
                    selectedButton[0].setBackgroundResource(R.drawable.letter_button_bg);
                    selectedButton[0].setBackgroundTintList(null);
                }

                // Set new selection
                btnHorizontal.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                selectedButton[0] = btnHorizontal;

                // Ensure vertical button is reset
                btnVertical.setBackgroundResource(R.drawable.letter_button_bg);
                btnVertical.setBackgroundTintList(null);
            });

            // Hərf düymələri
            GridLayout letterContainer = dialogView.findViewById(R.id.letter_container);
            String[] azLetters = {
                    "Q", "Ü", "E", "R", "T", "Y", "U", "İ",
                    "O", "P", "A", "S", "D", "F", "G", "H",
                    "J", "K", "L", "Z", "X", "C", "V", "B",
                    "N", "M", "Ç", "Ş", "Ə", "Ğ", "I", "Ö",
                    "←"
            };

            for (String letter : azLetters) {
                Button button = new Button(activity);
                button.setText(letter);
                button.setTextSize(18f);
                button.setTextColor(Color.WHITE);
                button.setBackground(ContextCompat.getDrawable(activity, R.drawable.letter_button_bg));
                button.setPadding(0, 20, 0, 20);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                params.setMargins(8, 8, 8, 8);
                button.setLayoutParams(params);

                if (letter.equals("←")) {
                    button.setBackground(ContextCompat.getDrawable(activity, R.drawable.letter_button_bg_red));
                }

                button.setOnClickListener(v -> {
                    if (letter.equals("←")) {
                        button.setTextColor(Color.RED); // ← düyməsinin rəngi qırmızı olsun

                        // Silmə əməliyyatı - dialoqu açıq saxlayırıq
                        webView.evaluateJavascript("if(window.clearSelectedCell) window.clearSelectedCell(true)", null);

                        // Yeni hüceyrə üçün dialoqu yenilə
                        webView.evaluateJavascript("if(window.getCurrentCellCoords) window.getCurrentCellCoords()",
                                new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String value) {
                                        if (value != null && !value.equals("null")) {
                                            String[] coords = value.replace("[", "").replace("]", "").split(",");
                                            if (coords.length == 2) {
                                                int newRow = Integer.parseInt(coords[0].trim());
                                                int newCol = Integer.parseInt(coords[1].trim());
                                                showLetterDialog(newRow, newCol);
                                            }
                                        }
                                    }
                                });
                    } else {
                        // Hərf seçimi - LƏKİN dialoqu bağlamırıq
                        String js = String.format("if(window.insertLetter) window.insertLetter('%s')", letter);
                        webView.evaluateJavascript(js, new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                // Yeni hüceyrə koordinatlarını alıb dialoqu yenilə
                                if (value != null && value.contains("filled")) {
                                    // Hamısı dolub - dialogu bağla
                                    if (currentDialog != null && currentDialog.isShowing()) {
                                        currentDialog.dismiss();
                                    }
                                } else if (value != null && !value.equals("null")) {
                                    String[] coords = value.replace("[", "").replace("]", "").split(",");
                                    if (coords.length == 2) {
                                        int newRow = Integer.parseInt(coords[0].trim());
                                        int newCol = Integer.parseInt(coords[1].trim());
                                        showLetterDialog(newRow, newCol);

                                    }
                                }
                            }
                        });
                    }
                });



                letterContainer.addView(button);

            }

            // Bağlama düyməsi
            ImageButton closeButton = dialogView.findViewById(R.id.close_dialog);
            if (closeButton != null) {
                closeButton.setOnClickListener(v -> currentDialog.dismiss());
            }
        }

        @JavascriptInterface
        public void closeDialog() {
            activity.runOnUiThread(() -> {
                if (currentDialog != null && currentDialog.isShowing()) {

                }
            });
        }
    }


    private void loadWordsFromJson(String fileName) {
        try {
            InputStream is = getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(jsonString);

            // Across words
            JSONArray acrossWords = jsonObject.getJSONArray("across");
            TextView acrossHeader = new TextView(this);
            acrossHeader.setText("Soldan Sağa:");
            acrossHeader.setTextSize(21f);
            acrossHeader.setTextColor(Color.parseColor("#3F51B5"));
            horizontalCont.addView(acrossHeader);

            for (int i = 0; i < acrossWords.length(); i++) {
                JSONObject wordObj = acrossWords.getJSONObject(i);
                addQuestion(wordObj, horizontalCont, "across");
            }

            // Down words
            JSONArray downWords = jsonObject.getJSONArray("down");
            TextView downHeader = new TextView(this);
            downHeader.setText("\nYuxarıdan Aşağıya:");
            downHeader.setTextSize(21f);
            downHeader.setTextColor(Color.parseColor("#3F51B5"));
            verticalCon.addView(downHeader);

            for (int i = 0; i < downWords.length(); i++) {
                JSONObject wordObj = downWords.getJSONObject(i);
                addQuestion(wordObj, verticalCon, "down");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addQuestion(JSONObject wordObj, LinearLayout layout, String direction) throws Exception {
        int position = wordObj.getInt("position");
        String question = wordObj.getString("question");
        String answer = wordObj.getString("answer");

        TextView wordText = new TextView(this);
        wordText.setText(position + ". " + question);
        wordText.setTextSize(20f);
        wordText.setTextColor(direction.equals("across") ? Color.BLACK : Color.BLACK);
        layout.addView(wordText);

        answerMap.put(direction + "_" + position, answer.trim().toLowerCase());
    }
}

