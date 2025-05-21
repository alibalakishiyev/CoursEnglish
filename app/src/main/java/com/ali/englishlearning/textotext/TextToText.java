package com.ali.englishlearning.textotext;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ali.englishlearning.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

public class TextToText extends AppCompatActivity {


    TextView metin,textanswer;
    EditText editTextText;
    Button btnCheck, btnNextExample;

    ArrayList<String> azSentences = new ArrayList<>();
    ArrayList<String> enSentences = new ArrayList<>();
    int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_to_text);

        metin = findViewById(R.id.metin);
        textanswer = findViewById(R.id.textanswerAz);
        editTextText = findViewById(R.id.editTextText);
        btnCheck = findViewById(R.id.btnCheck);
        btnNextExample = findViewById(R.id.btnNextExample);

        loadJsonData();
        showNextSentence();

        btnNextExample.setOnClickListener(v -> showNextSentence());

        btnCheck.setOnClickListener(v -> {
            String userInput = editTextText.getText().toString().trim();
            String correctAnswer = enSentences.get(currentIndex);

            if (normalize(userInput).equals(normalize(correctAnswer))) {
                // ✅ Doğru cavab
                editTextText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                MediaPlayer.create(this, R.raw.bingo).start();
                Toast.makeText(this, "✅ Doğrudur!", Toast.LENGTH_SHORT).show();
            } else {
                // ❌ Səhv cavab
                editTextText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                MediaPlayer.create(this, R.raw.error).start();
                Toast.makeText(this, "❌ Səhvdir. Doğru: " + correctAnswer, Toast.LENGTH_LONG).show();
            }
        });




        metin.setOnClickListener(v -> {
            String correctAnswer = enSentences.get(currentIndex);
            textanswer.setText(correctAnswer);
            Toast.makeText(this, "Doğru cavab: " + correctAnswer, Toast.LENGTH_LONG).show();
        });

    }

    private void loadJsonData() {
        try {
            InputStream is = getAssets().open("text/texts.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String jsonStr = new String(buffer, StandardCharsets.UTF_8);
            Toast.makeText(this, "JSON uğurla oxundu!", Toast.LENGTH_LONG).show(); // ✅ Bu sətri əlavə et
            JSONArray jsonArray = new JSONArray(jsonStr);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                azSentences.add(obj.getString("az"));
                enSentences.add(obj.getString("en"));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Xəta baş verdi: " + e.getMessage(), Toast.LENGTH_LONG).show(); // ❗ Bu sətri əlavə et
            e.printStackTrace();
        }
    }

    private String normalize(String input) {
        return input
                .toLowerCase()                           // böyük/kiçik fərqi olmur
                .replaceAll("[^a-zA-Zəöçşğüıİ ]", "")     // yalnız hərfləri və boşluğu saxla
                .replaceAll("\\s+", " ")                 // çoxlu boşluqları bir boşluğa çevir
                .trim();                                 // əvvəl və axır boşluqları sil
    }



    private void showNextSentence() {
        if (azSentences.size() == 0) return;
        currentIndex = new Random().nextInt(azSentences.size());
        metin.setText(azSentences.get(currentIndex));
        editTextText.setText("");
        editTextText.setTextColor(getResources().getColor(android.R.color.black)); // 🔄 Reset rəng
    }

}