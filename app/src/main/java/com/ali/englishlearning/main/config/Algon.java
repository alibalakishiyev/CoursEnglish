package com.ali.englishlearning.main.config;

public class Algon {
    public int algoImage;
    public String algoText;
    public Class<?> activityClazz;
    public String htmlFile;
    public String jsonFile; // əlavə olunur

    public Algon(int algoImage, String algoText, Class<?> activityClazz, String htmlFile, String jsonFile) {
        this.algoImage = algoImage;
        this.algoText = algoText;
        this.activityClazz = activityClazz;
        this.htmlFile = htmlFile;
        this.jsonFile = jsonFile;

    }


}
