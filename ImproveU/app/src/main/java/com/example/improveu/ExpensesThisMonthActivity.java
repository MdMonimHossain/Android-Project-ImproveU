package com.example.improveu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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

public class ExpensesThisMonthActivity extends AppCompatActivity {

    TextView totalExpenseText, budgetText, expensePercentText;
    ProgressBar budgetBar;
    Toolbar toolbar;
    RecyclerView thisMonthRecyclerview;
    StaggeredGridLayoutManager staggeredGridLayoutManager;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;

    Calendar calendar;

    TotalExpense totalExpenseObj;
    String totalExpenseDocId;

    FirestoreRecyclerAdapter<ExpenseModel, ExpensesThisMonthActivity.ExpenseViewHolder> expenseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses_this_month);

        budgetBar = findViewById(R.id.budgetBar);
        totalExpenseText = findViewById(R.id.totalExpenseText);
        budgetText = findViewById(R.id.budgetText);
        expensePercentText = findViewById(R.id.expensePercentText);

        budgetBar.setMax(100);

        toolbar = findViewById(R.id.thisMonthToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user = firebaseAuth.getCurrentUser();
        calendar = Calendar.getInstance();

        CharSequence dateCharSequence = DateFormat.format("MMMM, yyyy", calendar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(dateCharSequence);

        getTotalExpense();

        budgetText.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(v.getContext());
            bottomSheetDialog.setContentView(R.layout.set_budget_layout);

            EditText setBudgetText = bottomSheetDialog.findViewById(R.id.setBudgetText);
            Button saveBudgetBtn = bottomSheetDialog.findViewById(R.id.saveBudgetBtn);

            Objects.requireNonNull(setBudgetText).setText(String.valueOf(totalExpenseObj.getBudget()));

            Objects.requireNonNull(saveBudgetBtn).setOnClickListener(v1 -> {

                String newBudgetText = Objects.requireNonNull(setBudgetText).getText().toString();
                double budget = 0;
                if(!newBudgetText.isEmpty()){
                    budget = Double.parseDouble(newBudgetText);
                }
                setBudget(budget);
                bottomSheetDialog.dismiss();
            });

            bottomSheetDialog.show();
        });

        CollectionReference collectionReference2 = firebaseFirestore.collection("expenses").document(user.getUid()).collection("allExpenses");
        Query query = collectionReference2.whereEqualTo("month", calendar.get(Calendar.MONTH) + 1).whereEqualTo("year", calendar.get(Calendar.YEAR)).orderBy("date", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<ExpenseModel> thisMonthAllExpenses = new FirestoreRecyclerOptions.Builder<ExpenseModel>().setQuery(query, ExpenseModel.class).build();
        expenseAdapter = new FirestoreRecyclerAdapter<ExpenseModel, ExpenseViewHolder>(thisMonthAllExpenses) {
            @Override
            protected void onBindViewHolder(@NonNull ExpenseViewHolder expenseViewHolder, int i, @NonNull ExpenseModel expenseModel) {
                String description = expenseModel.getDescription();
                double amount = expenseModel.getAmount();
                int date = expenseModel.getDate();
                int month = expenseModel.getMonth();
                int year = expenseModel.getYear();
                String docId = expenseAdapter.getSnapshots().getSnapshot(i).getId();

                String dateText = date + "-" + month + "-" + year;
                expenseViewHolder.expenseDate.setText(dateText);
                expenseViewHolder.expenseDescription.setText(description);
                expenseViewHolder.expenseAmount.setText(String.valueOf(amount));

                expenseViewHolder.itemView.setOnClickListener(v -> {
                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(v.getContext());
                    bottomSheetDialog.setContentView(R.layout.edit_expense_layout);

                    TextInputEditText editExpenseDescription = bottomSheetDialog.findViewById(R.id.editExpenseDescription);
                    TextInputEditText editExpenseAmount = bottomSheetDialog.findViewById(R.id.editExpenseAmount);
                    Button deleteBtn = bottomSheetDialog.findViewById(R.id.expenseDeleteBtn);
                    Button saveBtn = bottomSheetDialog.findViewById(R.id.expenseSaveBtn);

                    Objects.requireNonNull(editExpenseDescription).setText(description);
                    Objects.requireNonNull(editExpenseAmount).setText(String.valueOf(amount));

                    Objects.requireNonNull(deleteBtn).setOnClickListener(v13 -> {
                        DocumentReference documentReference1 = firebaseFirestore.collection("expenses").document(user.getUid()).collection("allExpenses").document(docId);
                        documentReference1.delete().addOnSuccessListener(unused -> Toast.makeText(v13.getContext(), "Expense Deleted", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(v13.getContext(), "Failed to delete", Toast.LENGTH_SHORT).show());

                        double newTotalExpense = totalExpenseObj.getTotalExpense() - amount;
                        totalExpenseObj.setTotalExpense(newTotalExpense);
                        updateBudgetBar();
                        totalExpenseText.setText(String.valueOf(totalExpenseObj.getTotalExpense()));

                        DocumentReference documentReference2 = firebaseFirestore.collection("totalExpense").document(user.getUid()).collection("allTotalExpenses").document(totalExpenseDocId);
                        Map<String, Object> totalExpense = new HashMap<>();
                        totalExpense.put("totalExpense", totalExpenseObj.getTotalExpense());
                        documentReference2.update(totalExpense);

                        bottomSheetDialog.dismiss();
                    });

                    Objects.requireNonNull(saveBtn).setOnClickListener(v12 -> {

                        String editedDescription = Objects.requireNonNull(editExpenseDescription.getText()).toString();
                        String editedAmountText = Objects.requireNonNull(editExpenseAmount.getText()).toString();

                        if(editedDescription.isEmpty()){
                            Toast.makeText(getApplicationContext(), "Description can not be empty", Toast.LENGTH_SHORT).show();
                        }
                        else if(editedAmountText.isEmpty()){
                            Toast.makeText(getApplicationContext(), "Amount can not be empty", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            double newAmount = Double.parseDouble(editedAmountText);
                            double newTotalExpense = totalExpenseObj.getTotalExpense() - amount + newAmount;
                            totalExpenseObj.setTotalExpense(newTotalExpense);
                            updateBudgetBar();
                            totalExpenseText.setText(String.valueOf(totalExpenseObj.getTotalExpense()));

                            DocumentReference documentReference21 = firebaseFirestore.collection("expenses").document(user.getUid()).collection("allExpenses").document(docId);
                            Map<String, Object> expense = new HashMap<>();
                            expense.put("description", editedDescription);
                            expense.put("amount", newAmount);
                            documentReference21.update(expense);

                            DocumentReference documentReference2 = firebaseFirestore.collection("totalExpense").document(user.getUid()).collection("allTotalExpenses").document(totalExpenseDocId);
                            Map<String, Object> totalExpense = new HashMap<>();
                            totalExpense.put("totalExpense", totalExpenseObj.getTotalExpense());
                            documentReference2.update(totalExpense);
                        }

                        bottomSheetDialog.dismiss();
                    });

                    bottomSheetDialog.show();
                });

            }

            @NonNull
            @Override
            public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expenses_layout, parent, false);
                return new ExpenseViewHolder(view);
            }
        };

        thisMonthRecyclerview = findViewById(R.id.thisMonthRecyclerview);
        thisMonthRecyclerview.setHasFixedSize(true);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        thisMonthRecyclerview.setLayoutManager(staggeredGridLayoutManager);
        thisMonthRecyclerview.setAdapter(expenseAdapter);

    }

    private void getTotalExpense(){
        CollectionReference collectionReference1 = firebaseFirestore.collection("totalExpense").document(user.getUid()).collection("allTotalExpenses");
        Query query2 = collectionReference1.whereEqualTo("month", calendar.get(Calendar.MONTH) + 1).whereEqualTo("year", calendar.get(Calendar.YEAR));
        query2.limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if(task.getResult().isEmpty()){
                    DocumentReference documentReference3 = firebaseFirestore.collection("totalExpense").document(user.getUid()).collection("allTotalExpenses").document();
                    Map<String, Object> totalExpense = new HashMap<>();
                    totalExpense.put("budget", 0);
                    totalExpense.put("totalExpense", 0);
                    totalExpense.put("month", calendar.get(Calendar.MONTH) + 1);
                    totalExpense.put("year", calendar.get(Calendar.YEAR));
                    documentReference3.set(totalExpense);
                    totalExpenseObj = new TotalExpense(0, 0, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
                    totalExpenseDocId = documentReference3.getId();
                }
                else{
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        totalExpenseDocId = document.getId();
                        totalExpenseObj = document.toObject(TotalExpense.class);
                    }
                }
                updateBudgetBar();
                totalExpenseText.setText(String.valueOf(totalExpenseObj.getTotalExpense()));
                budgetText.setText(String.valueOf(totalExpenseObj.getBudget()));
            }
            else{
                Log.d("failed", "Error getting documents: ", task.getException());
            }
        });
    }

    private void updateBudgetBar(){
        double progress = 0;
        String text;
        if(totalExpenseObj.getBudget() != 0)
            progress = totalExpenseObj.getTotalExpense() / totalExpenseObj.getBudget() * 100;
        budgetBar.setProgress((int)progress);

        if(progress <= 100){
            text = "Expense (" + (int)progress + "%)";
        }
        else{
            text = "Expense";
        }
        expensePercentText.setText(text);
    }

    private void setBudget(double budget){
        totalExpenseObj.setBudget(budget);
        budgetText.setText(String.valueOf(budget));

        DocumentReference documentReference4 = firebaseFirestore.collection("totalExpense").document(user.getUid()).collection("allTotalExpenses").document(totalExpenseDocId);
        Map<String, Object> totalExpense = new HashMap<>();
        totalExpense.put("budget", budget);
        documentReference4.update(totalExpense);

        updateBudgetBar();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder{

        private final TextView expenseDate;
        private final TextView expenseDescription;
        private final TextView expenseAmount;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            expenseDate = itemView.findViewById(R.id.expenseDate);
            expenseDescription = itemView.findViewById(R.id.expenseDescription);
            expenseAmount = itemView.findViewById(R.id.expenseAmount);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        expenseAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(expenseAdapter != null){
            expenseAdapter.stopListening();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ExpensesThisMonthActivity.this, ExpensesActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            startActivity(new Intent(ExpensesThisMonthActivity.this, ExpensesActivity.class));
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