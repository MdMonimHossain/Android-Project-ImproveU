package com.example.improveu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class NotesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    FloatingActionButton mCreateNoteFAB;
    RecyclerView mRecyclerView;
    StaggeredGridLayoutManager staggeredGridLayoutManager;

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

    FirestoreRecyclerAdapter<NoteModel, NoteViewHolder> noteAdapter;

    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        mAuth = FirebaseAuth.getInstance();

        mCreateNoteFAB = findViewById(R.id.createNoteFAB);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("All Notes");

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toolbar.setNavigationIcon(R.drawable.ic_menu);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(notes_id);
        headerView = navigationView.getHeaderView(0);
        userEmailText = headerView.findViewById(R.id.userEmailText);
        user = mAuth.getCurrentUser();
        if (user != null) {
            userEmail = user.getEmail();
            userEmailText.setText(userEmail);
        }

        firebaseFirestore = FirebaseFirestore.getInstance();

        Query query = firebaseFirestore.collection("notes").document(user.getUid()).collection("myNotes").orderBy("created", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<NoteModel> userAllNotes = new FirestoreRecyclerOptions.Builder<NoteModel>().setQuery(query, NoteModel.class).build();
        noteAdapter = new FirestoreRecyclerAdapter<NoteModel, NoteViewHolder>(userAllNotes) {

            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull NoteModel noteModel) {

                ImageView popupBtn = noteViewHolder.itemView.findViewById(R.id.menuPopupBtn);

                String title = noteModel.getTitle();
                String content = noteModel.getContent();
                long created = noteModel.getCreated();
                int checkBoxCount = noteModel.getCheckBoxCount();
                List<Boolean> completedList = noteModel.getCompletedList();
                String docId = noteAdapter.getSnapshots().getSnapshot(i).getId();

                noteViewHolder.noteTitle.setText(title);
                noteViewHolder.noteContent.setText(content);

                noteViewHolder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), EditNoteActivity.class);
                    intent.putExtra("title", title);
                    intent.putExtra("content", content);
                    intent.putExtra("created", created);
                    intent.putExtra("checkBoxCount", checkBoxCount);
                    intent.putExtra("completedList", (Serializable) completedList);
                    intent.putExtra("noteId", docId);
                    v.getContext().startActivity(intent);
                    finish();
                });

                popupBtn.setOnClickListener(v -> {
                    PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        popupMenu.setGravity(Gravity.END);
                    }
                    popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(item -> {

                        DocumentReference documentReference = firebaseFirestore.collection("notes").document(user.getUid()).collection("myNotes").document(docId);
                        documentReference.delete().addOnSuccessListener(unused -> Toast.makeText(v.getContext(), "Note Deleted", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(v.getContext(), "Failed to delete", Toast.LENGTH_SHORT).show());

                        return false;
                    });

                    popupMenu.getMenu().add("Share").setOnMenuItemClickListener(item -> {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_TEXT, content);
                        intent.setType("text/plain");
                        intent = Intent.createChooser(intent, "Share by");
                        startActivity(intent);
                        return false;
                    });

                    popupMenu.show();
                });

            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_layout, parent, false);
                return new NoteViewHolder(view);
            }
        };

        mRecyclerView = findViewById(R.id.notesRecyclerview);
        mRecyclerView.setHasFixedSize(true);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        mRecyclerView.setAdapter(noteAdapter);

        mCreateNoteFAB.setOnClickListener(v -> {
            startActivity(new Intent(NotesActivity.this, CreateNoteActivity.class));
            finish();
        });

    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder{

        private final TextView noteTitle;
        private final TextView noteContent;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.noteTitle);
            noteContent = itemView.findViewById(R.id.noteContent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(noteAdapter != null){
            noteAdapter.stopListening();
        }
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
            case notes_id:
                break;
            case tasks_id:
                intent = new Intent(getApplicationContext(), TasksActivity.class);
                startActivity(intent);
                finish();
                break;
            case home_id:
                onBackPressed();
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