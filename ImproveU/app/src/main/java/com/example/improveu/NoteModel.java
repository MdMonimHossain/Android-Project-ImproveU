package com.example.improveu;

import java.util.List;

public class NoteModel {

    private String title;
    private String content;
    private long created;
    private int checkBoxCount;
    private List<Boolean> completedList;

    public NoteModel() {
    }

    public NoteModel(String title, String content, long created, int checkBoxCount, List<Boolean> completedList) {
        this.title = title;
        this.content = content;
        this.created = created;
        this.checkBoxCount = checkBoxCount;
        this.completedList = completedList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public int getCheckBoxCount() {
        return checkBoxCount;
    }

    public void setCheckBoxCount(int checkBoxCount) {
        this.checkBoxCount = checkBoxCount;
    }

    public List<Boolean> getCompletedList() {
        return completedList;
    }

    public void setCompletedList(List<Boolean> completedList) {
        this.completedList = completedList;
    }
}
