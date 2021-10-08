package com.example.improveu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddExpenseActivity extends AppCompatActivity {

    TextView addExpenseDate;
    TextInputEditText addExpenseDescription, addExpenseAmount;
    FloatingActionButton mSaveExpenseFAB;
    ProgressBar progressBar;
    Toolbar toolbar;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;

    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        addExpenseDate = findViewById(R.id.addExpenseDate);
        addExpenseDescription = findViewById(R.id.addExpenseDescription);
        addExpenseAmount = findViewById(R.id.addExpenseAmount);
        mSaveExpenseFAB = findViewById(R.id.saveExpenseFAB);
        progressBar = findViewById(R.id.progressBarAddExpense);

        toolbar = findViewById(R.id.addExpenseToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Add Expense");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user = firebaseAuth.getCurrentUser();
        calendar = Calendar.getInstance();

        CharSequence dateCharSequence = DateFormat.format("MMM d, yyyy", calendar);
        addExpenseDate.setText(dateCharSequence);

        addExpenseDate.setOnClickListener(v -> getDate());

        mSaveExpenseFAB.setOnClickListener(v -> {

            String description = Objects.requireNonNull(addExpenseDescription.getText()).toString();
            String amountText = Objects.requireNonNull(addExpenseAmount.getText()).toString();

            if(description.isEmpty()){
                Toast.makeText(getApplicationContext(), "Description can not be empty", Toast.LENGTH_SHORT).show();
            }
            else if(amountText.isEmpty()){
                Toast.makeText(getApplicationContext(), "Amount can not be empty", Toast.LENGTH_SHORT).show();
            }
            else{
                progressBar.setVisibility(View.VISIBLE);

                double amount = Double.parseDouble(amountText);

                CollectionReference collectionReference = firebaseFirestore.collection("totalExpense").document(user.getUid()).collection("allTotalExpenses");
                Query query = collectionReference.whereEqualTo("month", calendar.get(Calendar.MONTH) + 1).whereEqualTo("year", calendar.get(Calendar.YEAR));
                query.limit(1).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if(task.getResult().isEmpty()){
                            DocumentReference documentReference = firebaseFirestore.collection("totalExpense").document(user.getUid()).collection("allTotalExpenses").document();
                            Map<String, Object> totalExpense = new HashMap<>();
                            totalExpense.put("budget", 0);
                            totalExpense.put("totalExpense", amount);
                            totalExpense.put("month", calendar.get(Calendar.MONTH) + 1);
                            totalExpense.put("year", calendar.get(Calendar.YEAR));
                            documentReference.set(totalExpense);
                        }
                        else{
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String docId = document.getId();
                                TotalExpense totalExpenseObj = document.toObject(TotalExpense.class);

                                DocumentReference documentReference = firebaseFirestore.collection("totalExpense").document(user.getUid()).collection("allTotalExpenses").document(docId);
                                Map<String, Object> totalExpense = new HashMap<>();
                                totalExpense.put("totalExpense", totalExpenseObj.getTotalExpense() + amount);
                                documentReference.update(totalExpense);
                            }

                        }

                    } else {
                        Log.d("failed", "Error getting documents: ", task.getException());
                    }
                });

                DocumentReference documentReference = firebaseFirestore.collection("expenses").document(user.getUid()).collection("allExpenses").document();
                Map<String, Object> expense = new HashMap<>();
                expense.put("description", description);
                expense.put("amount", amount);
                expense.put("date", calendar.get(Calendar.DATE));
                expense.put("month", calendar.get(Calendar.MONTH) + 1);
                expense.put("year", calendar.get(Calendar.YEAR));

                documentReference.set(expense).addOnSuccessListener(unused -> {
                    Toast.makeText(getApplicationContext(), "Expense saved", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AddExpenseActivity.this, ExpensesActivity.class));
                    finish();
                }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed to save expense", Toast.LENGTH_SHORT).show());
            }

        });

    }

    private void getDate() {
        int startYear = calendar.get(Calendar.YEAR);
        int startMonth = calendar.get(Calendar.MONTH);
        int startDate = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {

            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DATE, dayOfMonth);

            CharSequence dateCharSequence = DateFormat.format("MMM d, yyyy", calendar);
            addExpenseDate.setText(dateCharSequence);
        }, startYear, startMonth, startDate);

        datePickerDialog.show();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AddExpenseActivity.this, ExpensesActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            startActivity(new Intent(AddExpenseActivity.this, ExpensesActivity.class));
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