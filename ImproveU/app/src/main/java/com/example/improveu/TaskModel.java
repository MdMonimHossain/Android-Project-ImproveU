package com.example.improveu;

public class TaskModel {

    private String title;
    private boolean completed;
    private long created;

    public TaskModel() {
    }

    public TaskModel(String title, boolean completed, long created) {
        this.title = title;
        this.completed = completed;
        this.created = created;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }
}
