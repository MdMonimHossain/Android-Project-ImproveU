package com.example.improveu;

public class ExpenseModel {
    String description;
    int date, month, year;
    double amount;

    public ExpenseModel() {
    }

    public ExpenseModel(String description, int date, int month, int year, double amount) {
        this.description = description;
        this.date = date;
        this.month = month;
        this.year = year;
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
