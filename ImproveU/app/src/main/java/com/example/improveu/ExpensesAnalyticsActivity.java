package com.example.improveu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.whiteelephant.monthpicker.MonthPickerDialog;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class ExpensesAnalyticsActivity extends AppCompatActivity {

    TextView yearText, noDataText;
    Button searchBtn;
    Toolbar toolbar;

    BarChart barChart;
    BarData barData;
    BarDataSet barDataSet;
    ArrayList<BarEntry> barEntries;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;

    int year;
    ArrayList<TotalExpense> totalExpenses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses_analytics);

        yearText = findViewById(R.id.yearText);
        noDataText = findViewById(R.id.noDataText);
        searchBtn = findViewById(R.id.searchBtn);
        barChart = findViewById(R.id.barChart);

        toolbar = findViewById(R.id.analyticsToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Analytics");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user = firebaseAuth.getCurrentUser();

        yearText.setOnClickListener(v -> getYear());

        searchBtn.setOnClickListener(v -> {
            String yearStr = yearText.getText().toString();
            if(yearStr.equals("Set Year")){
                Toast.makeText(getApplicationContext(),"Set Year", Toast.LENGTH_SHORT).show();
            }
            else{
                noDataText.setVisibility(View.INVISIBLE);
                getAllTotalExpenses();
            }
        });

        noDataText.setVisibility(View.INVISIBLE);
        initializeBarEntries();

        barDataSet.setColors(ColorTemplate.LIBERTY_COLORS);
        barDataSet.setDrawValues(false);

        Description description = barChart.getDescription();
        description.setEnabled(false);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(getResources().getColor(R.color.darkBlue));

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setAxisMinimum(0f);
        rightAxis.setTextColor(getResources().getColor(R.color.darkBlue));

        XAxis xAxis = barChart.getXAxis();
        xAxis.setTextColor(getResources().getColor(R.color.darkBlue));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);


    }

    private void getAllTotalExpenses() {
        CollectionReference collectionReference = firebaseFirestore.collection("totalExpense").document(user.getUid()).collection("allTotalExpenses");
        Query query = collectionReference.whereEqualTo("year", year);
        query.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(!task.getResult().isEmpty()){
                    totalExpenses = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        TotalExpense totalExpense = document.toObject(TotalExpense.class);
                        totalExpenses.add(totalExpense);
                    }
                    updateBarChart();
                    noDataText.setVisibility(View.INVISIBLE);
                }
                else{
                    initializeBarEntries();
                    noDataText.setVisibility(View.VISIBLE);
                }
            }
            else{
                Log.d("failed", "Error getting documents: ", task.getException());
            }
        });

    }

    private void updateBarChart() {

        for(TotalExpense te: totalExpenses){
            barEntries.set(te.getMonth() - 1, new BarEntry(te.getMonth(), (int) te.getTotalExpense()));
        }

        barDataSet = new BarDataSet(barEntries, "Months");
        barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.animateY(3000, Easing.EaseOutBack);
    }

    private void initializeBarEntries(){
        barEntries = new ArrayList<>();

        for(int i = 1; i <= 12; i++){
            barEntries.add(new BarEntry(i, 0));
        }

        barDataSet = new BarDataSet(barEntries, "Months");
        barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.animateY(3000, Easing.EaseOutBack);
    }

    private void getYear() {
        Calendar calendar = Calendar.getInstance();
        int startYear = calendar.get(Calendar.YEAR);
        int startMonth = calendar.get(Calendar.MONTH);

        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(ExpensesAnalyticsActivity.this, (selectedMonth, selectedYear) -> {
            year = selectedYear;
            yearText.setText(String.valueOf(year));
            yearText.setTextColor(getResources().getColor(R.color.darkBlue));
        }, startYear, startMonth);

        builder.setMinYear(1990)
                .setMaxYear(2050)
                .showYearOnly()
                .setTitle("Select Year")
                .build().show();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ExpensesAnalyticsActivity.this, ExpensesActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            startActivity(new Intent(ExpensesAnalyticsActivity.this, ExpensesActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

}