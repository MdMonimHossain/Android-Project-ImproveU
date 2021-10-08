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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Objects;

public class TasksActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener{

    CardView doCard, planCard, delegateCard, eliminateCard;
    TextView doCountText, planCountText, delegateCountText, eliminateCountText;
    FloatingActionButton mCreateTaskFAB;

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
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;
    String userEmail;

    long doCount, planCount, delegateCount, eliminateCount;
    String taskCountDocId;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        doCard = findViewById(R.id.doCard);
        planCard = findViewById(R.id.planCard);
        delegateCard = findViewById(R.id.delegateCard);
        eliminateCard = findViewById(R.id.eliminateCard);
        doCountText = findViewById(R.id.doCount);
        planCountText = findViewById(R.id.planCount);
        delegateCountText = findViewById(R.id.delegateCount);
        eliminateCountText = findViewById(R.id.eliminateCount);
        mCreateTaskFAB = findViewById(R.id.createTaskFAB);

        mCreateTaskFAB.setClickable(false);
        doCard.setClickable(false);
        planCard.setClickable(false);
        delegateCard.setClickable(false);
        eliminateCard.setClickable(false);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Eisenhower Matrix");

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toolbar.setNavigationIcon(R.drawable.ic_menu);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(tasks_id);
        headerView = navigationView.getHeaderView(0);
        userEmailText = headerView.findViewById(R.id.userEmailText);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userEmail = user.getEmail();
            userEmailText.setText(userEmail);
        }

        SharedPreferences pref1 = getApplicationContext().getSharedPreferences("appPrefs", MODE_PRIVATE);
        doCountText.setText(pref1.getString("doCount", "0"));
        planCountText.setText(pref1.getString("planCount", "0"));
        delegateCountText.setText(pref1.getString("delegateCount", "0"));
        eliminateCountText.setText(pref1.getString("eliminateCount", "0"));

        CollectionReference collectionReference = firebaseFirestore.collection("taskCount");
        Query query = collectionReference.whereEqualTo("userId", Objects.requireNonNull(user).getUid());
        query.limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {

                    doCount = document.getLong("doCount");
                    planCount = document.getLong("planCount");
                    delegateCount = document.getLong("delegateCount");
                    eliminateCount = document.getLong("eliminateCount");
                    taskCountDocId = document.getId();

                    mCreateTaskFAB.setClickable(true);
                    doCard.setClickable(true);
                    planCard.setClickable(true);
                    delegateCard.setClickable(true);
                    eliminateCard.setClickable(true);

                    doCountText.setText(String.valueOf(doCount));
                    planCountText.setText(String.valueOf(planCount));
                    delegateCountText.setText(String.valueOf(delegateCount));
                    eliminateCountText.setText(String.valueOf(eliminateCount));

                    SharedPreferences pref = getApplicationContext().getSharedPreferences("appPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("doCount", String.valueOf(doCount));
                    editor.putString("planCount", String.valueOf(planCount));
                    editor.putString("delegateCount", String.valueOf(delegateCount));
                    editor.putString("eliminateCount", String.valueOf(eliminateCount));
                    editor.apply();
                }
            } else {
                Log.d("failed", "Error getting documents: ", task.getException());
            }
        });

        doCard.setOnClickListener(this);
        planCard.setOnClickListener(this);
        delegateCard.setOnClickListener(this);
        eliminateCard.setOnClickListener(this);
        mCreateTaskFAB.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        Intent intent;

        if(v.getId() == R.id.doCard){
            intent = new Intent(TasksActivity.this, AllTasksActivity.class);
            intent.putExtra("taskType", "Do");
        }
        else if(v.getId() == R.id.planCard){
            intent = new Intent(TasksActivity.this, AllTasksActivity.class);
            intent.putExtra("taskType", "Plan");
        }
        else if(v.getId() == R.id.delegateCard){
            intent = new Intent(TasksActivity.this, AllTasksActivity.class);
            intent.putExtra("taskType", "Delegate");
        }
        else if(v.getId() == R.id.eliminateCard){
            intent = new Intent(TasksActivity.this, AllTasksActivity.class);
            intent.putExtra("taskType", "Eliminate");
        }
        else{
            intent = new Intent(TasksActivity.this, CreateTaskActivity.class);
        }

        intent.putExtra("doCount", doCount);
        intent.putExtra("planCount", planCount);
        intent.putExtra("delegateCount", delegateCount);
        intent.putExtra("eliminateCount", eliminateCount);
        intent.putExtra("taskCountDocId", taskCountDocId);
        startActivity(intent);
        finish();

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
            case tasks_id:
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
            case expenses_id:
                intent = new Intent(getApplicationContext(), ExpensesActivity.class);
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