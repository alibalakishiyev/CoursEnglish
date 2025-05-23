package com.ali.englishlearning.stt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ali.englishlearning.R;
import com.ali.englishlearning.cours.SentenceItem;

import org.json.JSONArray;
import org.json.JSONObject;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.StorageService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpeechToText extends AppCompatActivity implements RecognitionListener {

    private static final int STATE_START = 0;
    private static final int STATE_READY = 1;
    private static final int STATE_DONE = 2;
    private static final int STATE_MIC = 3;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int UPDATE_INTERVAL_MS = 300;

    private List<SentenceItem> sentenceList;
    private int currentIndex = 0;
    private boolean showingEnglish = true;
    private Random random;

    private TextView resultOut, textExample, textAnswer, textTimer;
    private ImageButton btnMic, btnResult, btnNextExam;
    private ToggleButton btnStop;
    private Model model;
    private SpeechService speechService;
    private StringBuilder fullTranscription = new StringBuilder();
    private boolean isRecording = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private String lastPartialResult = "";
    private long startTime = 0;
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRecording) {
                long millis = System.currentTimeMillis() - startTime;
                String time = String.format(Locale.getDefault(), "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millis),
                        TimeUnit.MILLISECONDS.toSeconds(millis) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
                textTimer.setText(time);
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speach_to_text);

        initializeViews();
        setupClickListeners();
        checkPermissions();
        LibVosk.setLogLevel(LogLevel.INFO);

        sentenceList = loadSentencesFromJson();
        random = new Random();


        if (!sentenceList.isEmpty()) {
            currentIndex = random.nextInt(sentenceList.size());
            textExample.setText(sentenceList.get(currentIndex).english);
            textAnswer.setText("\"Click on the example to see the translation.\"");
            showingEnglish = true;
        }

        // İngilis cümləyə toxunanda tərcüməni göstər
        textExample.setOnClickListener(v -> {
            SentenceItem current = sentenceList.get(currentIndex);
            if (showingEnglish) {
                textAnswer.setText(current.azerbaijani);
                showingEnglish = false;
            }
        });

        // "Next" düyməsinə basanda random cümlə göstər və tərcüməni təmizlə
        btnNextExam.setOnClickListener(v -> {
            if (!sentenceList.isEmpty()) {
                int newIndex;
                do {
                    newIndex = random.nextInt(sentenceList.size());
                } while (newIndex == currentIndex); // eyni cümlə olmasın

                currentIndex = newIndex;
                textExample.setText(sentenceList.get(currentIndex).english);
                textAnswer.setText("\"Click on the example to see the translation.\"");
                showingEnglish = true;
            }
        });


    }

    private void initializeViews() {
        resultOut = findViewById(R.id.textOutput);
        textExample = findViewById(R.id.textExample);
        textTimer = findViewById(R.id.textTimer);
        textAnswer = findViewById(R.id.textAnswer);
        btnMic = findViewById(R.id.btnMic);
        btnResult = findViewById(R.id.btnResult);
        btnStop = findViewById(R.id.btnStop);
        btnNextExam = findViewById(R.id.btnNext);

        resultOut.setMovementMethod(new ScrollingMovementMethod());
        setUiState(STATE_START);
    }

    private void setupClickListeners() {
        btnMic.setOnClickListener(v -> toggleRecording());
        btnResult.setOnClickListener(v -> compareWithExample());
        btnStop.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (speechService != null) {
                speechService.setPause(isChecked);
            }
        });
    }

    private void toggleRecording() {
        if (isRecording) {
            stopRecording();
        } else {
            startRecording();
        }
    }

    private void startRecording() {
        if (model == null) {
            Toast.makeText(this, "Model yükleniyor, lütfen bekleyin...", Toast.LENGTH_SHORT).show();
            return;
        }

        isRecording = true;
        startTime = System.currentTimeMillis();
        btnMic.setImageResource(R.drawable.micro64);
        fullTranscription.setLength(0);
        resultOut.setText("Dinliyirem...");
        textTimer.setText("00:00");

        try {
            Recognizer recognizer = new Recognizer(model, 16000.0f);
            speechService = new SpeechService(recognizer, 16000.0f);
            speechService.startListening(this);
            setUiState(STATE_MIC);

            // Start updates
            handler.post(updateRunnable);
            timerHandler.post(timerRunnable);
        } catch (IOException e) {
            setErrorState("Mikrofon hatası: " + e.getMessage());
        }
    }

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRecording && !lastPartialResult.isEmpty()) {
                fullTranscription.append(lastPartialResult).append(" ");

                resultOut.setText(fullTranscription.toString());

                scrollToBottom();
                lastPartialResult = "";
            }
            if (isRecording) {
                handler.postDelayed(this, UPDATE_INTERVAL_MS);
            }
        }
    };

    private void stopRecording() {
        isRecording = false;
        btnMic.setImageResource(R.drawable.redmic64);
        handler.removeCallbacks(updateRunnable);
        timerHandler.removeCallbacks(timerRunnable);

        if (speechService != null) {
            speechService.stop();
            speechService = null;
        }

        if (!lastPartialResult.isEmpty()) {
            fullTranscription.append(lastPartialResult);
            resultOut.setText(fullTranscription.toString());
            scrollToBottom();
        }

        setUiState(STATE_DONE);
    }

    private void compareWithExample() {
        String userText = fullTranscription.toString().trim();
        String exampleText = textExample.getText().toString().trim();

        if (userText.isEmpty()) {
            Toast.makeText(this, "Qarshilashdirmaq ucun metin yoxdur", Toast.LENGTH_SHORT).show();
            return;
        }

        if (exampleText.isEmpty()) {
            Toast.makeText(this, "Teqdim Olunan metin", Toast.LENGTH_SHORT).show();
            return;
        }

        SpannableString comparisonResult = compareTexts(userText, exampleText);
        resultOut.setText(comparisonResult);
    }

    private SpannableString compareTexts(String userText, String exampleText) {
        String[] userWords = userText.split("\\s+");
        String[] exampleWords = exampleText.split("\\s+");

        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("Sizin Danishmaqiniz:\n");

        SpannableString spannableResult = new SpannableString(resultBuilder.toString() + userText +
                "\n\nTeqdim Olunmush Metin:\n" + exampleText);

        int diffStart = resultBuilder.length();
        highlightDifferences(spannableResult, userWords, exampleWords, diffStart);

        return spannableResult;
    }

    private void highlightDifferences(SpannableString spannable, String[] userWords, String[] exampleWords, int offset) {
        int maxLength = Math.max(userWords.length, exampleWords.length);

        for (int i = 0; i < maxLength; i++) {
            if (i < userWords.length) {
                String userWord = userWords[i];
                String exampleWord = i < exampleWords.length ? exampleWords[i] : "";

                int start = findWordPosition(spannable.toString(), userWord, offset);
                if (start >= 0) {
                    int end = start + userWord.length();

                    if (userWord.equalsIgnoreCase(exampleWord)) {
                        // ✅ Düzgün söz → yaşıl
                        spannable.setSpan(new ForegroundColorSpan(Color.BLUE),
                                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        // ❌ Səhv söz → qırmızı
                        spannable.setSpan(new ForegroundColorSpan(Color.RED),
                                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }

                offset = start + userWord.length(); // Növbəti axtarış üçün ofseti güncəllə
            }
        }
    }


    private int findWordPosition(String text, String word, int startFrom) {
        String lowerText = text.toLowerCase();
        String lowerWord = word.toLowerCase();

        int index = lowerText.indexOf(lowerWord, startFrom);
        while (index != -1) {
            // Orijinal mətnin həmin hissəsinin uzunluğu word ilə eynidirsə və uyğun gəlirsə, return et
            String originalSegment = text.substring(index, Math.min(index + word.length(), text.length()));
            if (originalSegment.equalsIgnoreCase(word)) {
                return index;
            }

            // Tapmadısa, axtarışa davam et
            index = lowerText.indexOf(lowerWord, index + 1);
        }

        return -1;
    }



    @Override
    public void onPartialResult(String hypothesis) {
//        lastPartialResult = hypothesis;
    }

    @Override
    public void onResult(String hypothesis) {


        if (hypothesis != null && !hypothesis.trim().isEmpty()) {
            // Müvafiq "text":"..." hissələrini regex ilə tap
            Pattern pattern = Pattern.compile("\"text\"\\s*:\\s*\"(.*?)\"");
            Matcher matcher = pattern.matcher(hypothesis);

            while (matcher.find()) {
                String text = matcher.group(1).trim();
                if (!text.isEmpty()) {
                    fullTranscription.append(text).append(" ");
                }
            }

            updateDisplay();
        }


    }

    @Override
    public void onFinalResult(String hypothesis) {

        if (hypothesis != null && !hypothesis.trim().isEmpty()) {
            // Müvafiq "text":"..." hissələrini regex ilə tap
            Pattern pattern = Pattern.compile("\"text\"\\s*:\\s*\"(.*?)\"");
            Matcher matcher = pattern.matcher(hypothesis);

            while (matcher.find()) {
                String text = matcher.group(1).trim();
                if (!text.isEmpty()) {
                    fullTranscription.append(text).append(" ");
                }
            }

            updateDisplay();
        }
    }

    private void updateDisplay() {

        runOnUiThread(() -> {
            resultOut.setText(fullTranscription.toString().trim());
            scrollToBottom();
        });
    }

    private void scrollToBottom() {
        if (resultOut.getLayout() != null) {
            int scrollAmount = resultOut.getLayout().getLineTop(resultOut.getLineCount()) - resultOut.getHeight();
            if (scrollAmount > 0) {
                resultOut.scrollTo(0, scrollAmount);
            }
        }
    }

    @Override
    public void onError(Exception e) {
        runOnUiThread(() -> setErrorState("Hata: " + e.getMessage()));
    }

    @Override
    public void onTimeout() {
        runOnUiThread(() -> setUiState(STATE_DONE));
    }

    private void setUiState(int state) {
        runOnUiThread(() -> {
            switch (state) {
                case STATE_START:
                    resultOut.setText("Model hazırlanıyor...");
                    btnMic.setEnabled(false);
                    btnResult.setEnabled(false);
                    btnStop.setEnabled(false);
                    break;
                case STATE_READY:
                    resultOut.setText("Konuşmaya başlamak için mikrofona basın");
                    btnMic.setEnabled(true);
                    btnResult.setEnabled(false);
                    btnStop.setEnabled(false);
                    break;
                case STATE_DONE:
                    btnMic.setEnabled(true);
                    btnResult.setEnabled(true);
                    btnStop.setEnabled(false);
                    btnStop.setChecked(false);
                    break;
                case STATE_MIC:
                    resultOut.setText("Dinliyorum...");
                    btnMic.setEnabled(true);
                    btnResult.setEnabled(false);
                    btnStop.setEnabled(true);
                    break;
            }
        });
    }

    private void setErrorState(String message) {
        runOnUiThread(() -> {
            resultOut.setText(message);
            btnMic.setEnabled(false);
            btnResult.setEnabled(false);
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            initModel();
        }
    }

    private void initModel() {
        StorageService.unpack(this, "model-en", "",
                (model) -> {
                    this.model = model;
                    setUiState(STATE_READY);
                },
                (exception) -> setErrorState("Model yüklenemedi: " + exception.getMessage()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initModel();
            } else {
                setErrorState("Mikrofon izni gerekiyor");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
        timerHandler.removeCallbacks(timerRunnable);
        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
        }
        if (model != null) {
            model.close();
        }
    }

    private List<SentenceItem> loadSentencesFromJson() {
        List<SentenceItem> list = new ArrayList<>();
        try {
            InputStream is = getAssets().open("Sentence/sentences1.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(json);
            JSONArray sentences = jsonObject.getJSONArray("sentences");

            for (int i = 0; i < sentences.length(); i++) {
                JSONObject obj = sentences.getJSONObject(i);
                SentenceItem item = new SentenceItem();
                item.english = obj.getString("english");
                item.azerbaijani = obj.getString("azerbaijani");
                list.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}