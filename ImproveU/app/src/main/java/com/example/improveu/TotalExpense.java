package com.example.improveu;

public class TotalExpense {
    double budget;
    double totalExpense;
    int month;
    int year;

    public TotalExpense() {
    }

    public TotalExpense(double budget, double totalExpense, int month, int year) {
        this.budget = budget;
        this.totalExpense = totalExpense;
        this.month = month;
        this.year = year;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(double totalExpense) {
        this.totalExpense = totalExpense;
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
}
