package com.ali.englishlearning.cours;

public class Course {

    private String courseName;
    private String coursePath;

    public Course(String courseName, String coursePath) {
        this.courseName = courseName;
        this.coursePath = coursePath;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getCoursePath() {
        return coursePath;
    }


}
