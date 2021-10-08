package com.example.improveu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class DashboardActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    TextView quoteText, authorText;
    CardView tasksBtn, notesBtn, expensesBtn;

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

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    String quote, author, userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        quoteText = findViewById(R.id.quote);
        authorText = findViewById(R.id.author);
        tasksBtn = findViewById(R.id.tasksBtn);
        notesBtn = findViewById(R.id.notesBtn);
        expensesBtn = findViewById(R.id.expensesBtn);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toolbar.setNavigationIcon(R.drawable.ic_menu);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(home_id);
        headerView = navigationView.getHeaderView(0);
        userEmailText = headerView.findViewById(R.id.userEmailText);

        if (user != null) {
            userEmail = user.getEmail();
            userEmailText.setText(userEmail);
        }

        tasksBtn.setOnClickListener(this);
        notesBtn.setOnClickListener(this);
        expensesBtn.setOnClickListener(this);

        getQuote();
        quoteText.setText(quote);
        authorText.setText(author);

    }

    private void getQuote(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("appPrefs", MODE_PRIVATE);
        quote = pref.getString("quote", "No Quote Available");
        author = pref.getString("author", "");
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.tasksBtn){
            Intent intent = new Intent(DashboardActivity.this, TasksActivity.class);
            startActivity(intent);
        }
        if(v.getId() == R.id.notesBtn){
            Intent intent = new Intent(DashboardActivity.this, NotesActivity.class);
            startActivity(intent);
        }
        if(v.getId() == R.id.expensesBtn){
            Intent intent = new Intent(DashboardActivity.this, ExpensesActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            //super.onBackPressed();
            moveTaskToBack(true); // same as pressing home button
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(home_id);
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
            case home_id:
                break;
            case tasks_id:
                intent = new Intent(getApplicationContext(), TasksActivity.class);
                startActivity(intent);
                break;
            case notes_id:
                intent = new Intent(getApplicationContext(), NotesActivity.class);
                startActivity(intent);
                break;
            case expenses_id:
                intent = new Intent(getApplicationContext(), ExpensesActivity.class);
                startActivity(intent);
                break;
            case logout_id:
                firebaseAuth.signOut();
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