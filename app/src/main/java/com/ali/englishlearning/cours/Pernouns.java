package com.ali.englishlearning.cours;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ali.englishlearning.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Locale;

public class Pernouns extends AppCompatActivity {

    private LinearLayout wordContainer;
    private String coursePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions_course); // Layout faylı aşağıda


        wordContainer = findViewById(R.id.wordContainer);

        coursePath = getIntent().getStringExtra("jsonFile");
        if (coursePath != null) {
            loadWordsFromJson(coursePath); // JSON və path birdir
        }

    }

    private void loadWordsFromJson(String path) {
        try {
            AssetManager assetManager = getAssets();
            InputStream is = assetManager.open(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            String jsonString = builder.toString();
            JSONObject rootObject = new JSONObject(jsonString);

            // Bütün əsas kateqoriyaları (personal_pronouns, possessive_pronouns, reflexive_pronouns) oxuyuruq
            Iterator<String> mainCategories = rootObject.keys();

            while (mainCategories.hasNext()) {
                String mainCategoryKey = mainCategories.next();
                JSONObject mainCategoryObject = rootObject.getJSONObject(mainCategoryKey);

                // Hər bir əsas kateqoriyanın içində singular/plural var
                Iterator<String> numberKeys = mainCategoryObject.keys();

                while (numberKeys.hasNext()) {
                    String numberKey = numberKeys.next();
                    JSONObject numberObject = mainCategoryObject.getJSONObject(numberKey);

                    // singular/plural içində şəxs növləri var
                    Iterator<String> personKeys = numberObject.keys();

                    while (personKeys.hasNext()) {
                        String personKey = personKeys.next();
                        JSONObject personObject = numberObject.getJSONObject(personKey);

                        String en = personObject.getString("en");
                        String az = personObject.getString("az");

                        // Burada istəsən mainCategoryKey və numberKey də əlavə edə bilərsən göstərmək üçün
                        addTextViewToLayout(mainCategoryKey, numberKey, personKey, en, az);
                    }
                }
            }

            reader.close();
            is.close();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Log.e("LOAD_JSON_ERROR", "Fayl yüklənmədi: " + e.getMessage());
        }
    }

    private void addTextViewToLayout(String category, String number, String person, String en, String az) {
        TextView textView = new TextView(this);
        // İstəyə görə, göstərmək üçün:
        String displayText = category + " - " + number + " - " + person + "\nEN: " + en + " - AZ: " + az;
        textView.setText(displayText);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(16);
        textView.setPadding(20, 15, 20, 15);
        textView.setBackgroundColor(getColorByPerson(person));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 10);
        textView.setLayoutParams(params);

        wordContainer.addView(textView);
    }

    private int getColorByPerson(String person) {
        switch (person) {
            case "first_person":
                return Color.parseColor("#D0E8FF"); // Açıq mavi
            case "second_person":
                return Color.parseColor("#D0FFD6"); // Açıq yaşıl
            case "third_person":
            case "third_person_male":
            case "third_person_female":
            case "third_person_neutral":
                return Color.parseColor("#FFEFD0"); // Açıq narıncı
            default:
                return Color.parseColor("#F0F0F0"); // Açıq boz
        }
    }








}
