package com.ali.englishlearning.utils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ali.englishlearning.R;

public class BottomAppBarActivity extends AppCompatActivity {

    public class BaseActivity extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_base);
        }

        public void setContentLayout(int layoutResID) {
            FrameLayout container = findViewById(R.id.container);
            LayoutInflater.from(this).inflate(layoutResID, container, true);
        }
    }
}