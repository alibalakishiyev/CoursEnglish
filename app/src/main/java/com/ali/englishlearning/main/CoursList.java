package com.ali.englishlearning.main;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ali.englishlearning.R;
import com.ali.englishlearning.cours.Course;
import com.ali.englishlearning.cours.QuestionsCourse;
import com.ali.englishlearning.utils.Algo;
import com.ali.englishlearning.utils.AlgoAdapter;
import com.ali.englishlearning.utils.AlgoListener;

import java.io.IOException;
import java.util.ArrayList;

public class CoursList extends AppCompatActivity implements AlgoListener {

    private ArrayList<Course> courses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cours_list);

        // Kursları avtomatik əlavə edirik
        for (int i = 1; i <= 10; i++) {
            String courseName = "cours-" + i + ".json";
            String filePath = "Cours_A1/cours-" + i + "/" + courseName; // Kurs yolu düzəldildi

            // Kursları əlavə edirik
            courses.add(new Course(courseName, filePath));
        }

        // Kursların məlumatlarını yoxlayırıq
        for (Course course : courses) {
            Log.d("Course", "Course Name: " + course.getCourseName() + ", File Path: " + course.getCoursePath());
        }

        ArrayList<Algo> arrayList = new ArrayList<>();
        try {
            String[] jsonDirs = getAssets().list("Cours_A1"); // 'Cours_A1' qovluğunu oxuyuruq
            if (jsonDirs != null) {
                for (String dirName : jsonDirs) {
                    if (dirName.startsWith("cours-")) { // 'cours-' ilə başlayan qovluqları tapırıq
                        String[] jsonFiles = getAssets().list("Cours_A1/" + dirName); // Həmin qovluqdakı faylları oxuyuruq
                        for (String fileName : jsonFiles) {
                            if (fileName.endsWith(".json")) {
                                String courseName = fileName.replace(".json", "").replace("-", " ").toUpperCase();
                                arrayList.add(new Algo(
                                        R.drawable.cours1,
                                        courseName,
                                        QuestionsCourse.class,
                                        "Cours_A1/" + dirName + "/" + fileName // Düzgün yol
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
}
