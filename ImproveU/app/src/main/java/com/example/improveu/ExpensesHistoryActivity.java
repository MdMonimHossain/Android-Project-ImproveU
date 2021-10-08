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
import android.widget.TextView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Calendar;
import java.util.Objects;

public class ExpensesHistoryActivity extends AppCompatActivity {

    Intent data;

    TextView totalExpenseText;
    Toolbar toolbar;
    RecyclerView historyRecyclerview;
    StaggeredGridLayoutManager staggeredGridLayoutManager;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;

    Calendar calendar;

    int month, year;

    FirestoreRecyclerAdapter<ExpenseModel, ExpensesHistoryActivity.ExpenseViewHolder> expenseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses_history);

        totalExpenseText = findViewById(R.id.historyTotalExpenseText);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user = firebaseAuth.getCurrentUser();
        calendar = Calendar.getInstance();

        data = getIntent();
        month = data.getIntExtra("month", calendar.get(Calendar.MONTH));
        year = data.getIntExtra("year", calendar.get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);

        toolbar = findViewById(R.id.historyToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        CharSequence dateCharSequence = DateFormat.format("MMMM, yyyy", calendar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(dateCharSequence);

        totalExpenseText.setVisibility(View.INVISIBLE);
        getTotalExpense();

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

                String dateText = date + "-" + month + "-" + year;
                expenseViewHolder.expenseDate.setText(dateText);
                expenseViewHolder.expenseDescription.setText(description);
                expenseViewHolder.expenseAmount.setText(String.valueOf(amount));
            }

            @NonNull
            @Override
            public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expenses_layout, parent, false);
                return new ExpenseViewHolder(view);
            }
        };

        historyRecyclerview = findViewById(R.id.historyRecyclerview);
        historyRecyclerview.setHasFixedSize(true);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        historyRecyclerview.setLayoutManager(staggeredGridLayoutManager);
        historyRecyclerview.setAdapter(expenseAdapter);

    }

    private void getTotalExpense() {
        CollectionReference collectionReference1 = firebaseFirestore.collection("totalExpense").document(user.getUid()).collection("allTotalExpenses");
        Query query1 = collectionReference1.whereEqualTo("month", calendar.get(Calendar.MONTH) + 1).whereEqualTo("year", calendar.get(Calendar.YEAR));
        query1.limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if(!task.getResult().isEmpty()){
                    TotalExpense totalExpenseObj = new TotalExpense();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        totalExpenseObj = document.toObject(TotalExpense.class);
                    }
                    String text = "Total Expense: " + totalExpenseObj.getTotalExpense();
                    totalExpenseText.setVisibility(View.VISIBLE);
                    totalExpenseText.setText(text);
                }
                else{
                    String text = "No Data Available";
                    totalExpenseText.setVisibility(View.VISIBLE);
                    totalExpenseText.setText(text);
                }
            }
            else{
                Log.d("failed", "Error getting documents: ", task.getException());
            }
        });
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
        startActivity(new Intent(ExpensesHistoryActivity.this, ExpensesActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            startActivity(new Intent(ExpensesHistoryActivity.this, ExpensesActivity.class));
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