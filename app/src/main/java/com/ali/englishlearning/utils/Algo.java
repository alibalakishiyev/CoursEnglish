package com.ali.englishlearning.utils;

import com.ali.englishlearning.R;

public class Algo {
    public int algoImage;
    public String algoText;
    public Class<?> activityClazz;
    public String jsonFile; // əlavə olunur

    public Algo(int algoImage, String algoText, Class<?> activityClazz, String jsonFile) {
        this.algoImage = algoImage;
        this.algoText = algoText;
        this.activityClazz = activityClazz;
        this.jsonFile = jsonFile;
    }
}
