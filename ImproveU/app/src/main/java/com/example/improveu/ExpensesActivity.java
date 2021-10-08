package com.example.improveu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.whiteelephant.monthpicker.MonthPickerDialog;
import java.util.Calendar;
import java.util.Objects;

public class ExpensesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    CardView thisMonthBtn, historyBtn, analyticsBtn;
    FloatingActionButton mAddExpenseFAB;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    View headerView;
    TextView userEmailText;

    final int home_id = R.id.nav_home;
    final int tasks_id = R.id.nav_tasks;
    final int notes_id = R.id.nav_notes;
    final int expenses_id = R.id.nav_expenses;
    final int logout_id = R.id.nav_logout;
    final int share_id = R.id.nav_share;
    final int feedback_id = R.id.nav_feedback;
    final int aboutUs_id = R.id.nav_aboutUs;

    FirebaseAuth mAuth;
    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        mAuth = FirebaseAuth.getInstance();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        thisMonthBtn = findViewById(R.id.thisMonthBtn);
        historyBtn = findViewById(R.id.historyBtn);
        analyticsBtn = findViewById(R.id.analyticsBtn);
        mAddExpenseFAB = findViewById(R.id.addExpenseFAB);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Expenses");

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toolbar.setNavigationIcon(R.drawable.ic_menu);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(expenses_id);
        headerView = navigationView.getHeaderView(0);
        userEmailText = headerView.findViewById(R.id.userEmailText);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userEmail = user.getEmail();
            userEmailText.setText(userEmail);
        }

        thisMonthBtn.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), ExpensesThisMonthActivity.class));
            finish();
        });

        historyBtn.setOnClickListener(v -> getMonthAndYear());

        analyticsBtn.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), ExpensesAnalyticsActivity.class));
            finish();
        });

        mAddExpenseFAB.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), AddExpenseActivity.class));
            finish();
        });

    }

    private void getMonthAndYear() {
        Calendar calendar = Calendar.getInstance();
        int startYear = calendar.get(Calendar.YEAR);
        int startMonth = calendar.get(Calendar.MONTH);

        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(ExpensesActivity.this, (selectedMonth, selectedYear) -> {
            Intent intent = new Intent(getApplicationContext(), ExpensesHistoryActivity.class);
            intent.putExtra("month", selectedMonth);
            intent.putExtra("year", selectedYear);
            startActivity(intent);
            finish();
        }, startYear, startMonth);

        builder.setMinYear(1990)
                .setMaxYear(2050)
                .setTitle("Select Month and Year")
                .build().show();

    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }

    public void shareApp(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, new AppCommunication().getShareApp());
        intent.setType("text/plain");
        intent = Intent.createChooser(intent, "Share by");
        startActivity(intent);
    }

    public void sendFeedback(){
        String[] supportEmails = new AppCommunication().getSupportMails();
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:?subject=" + "Feedback on ImproveU App" + "&to=" + supportEmails[0] + ", " + supportEmails[1]));
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()){
            case expenses_id:
                break;
            case home_id:
                onBackPressed();
                finish();
                break;
            case notes_id:
                intent = new Intent(getApplicationContext(), NotesActivity.class);
                startActivity(intent);
                finish();
                break;
            case tasks_id:
                intent = new Intent(getApplicationContext(), TasksActivity.class);
                startActivity(intent);
                finish();
                break;
            case logout_id:
                mAuth.signOut();
                GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut();

                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                break;
            case share_id:
                shareApp();
                break;
            case feedback_id:
                sendFeedback();
                break;
            case aboutUs_id:
                intent = new Intent(getApplicationContext(), AboutUsActivity.class);
                startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

}