package com.example.improveu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CreateNoteActivity extends AppCompatActivity {

    EditText mCreateNoteTitle, mCreateNoteContent;
    FloatingActionButton mSaveNoteFAB;
    ProgressBar progressBar;
    Toolbar toolbar;

    LinearLayout createNoteCheckBoxLayout;
    FloatingActionButton addCheckBoxFAB;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;

    int checkBoxCount;
    List<Boolean> completedList;
    List<AppCompatCheckBox> checkBoxList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        mCreateNoteTitle = findViewById(R.id.createNoteTitle);
        mCreateNoteContent = findViewById(R.id.createNoteContent);
        mSaveNoteFAB = findViewById(R.id.saveNoteFAB);
        createNoteCheckBoxLayout = findViewById(R.id.createNoteCheckBoxLayout);
        addCheckBoxFAB = findViewById(R.id.addCheckBoxCreateFAB);
        progressBar = findViewById(R.id.progressBarCreateNote);

        toolbar = findViewById(R.id.createNoteToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user = firebaseAuth.getCurrentUser();

        checkBoxCount = 0;
        completedList = new ArrayList<>();
        checkBoxList = new ArrayList<>();

        mSaveNoteFAB.setOnClickListener(v -> {

            if(completedList.isEmpty()){
                completedList.add(false);
            }
            else {
                for(int i = 0; i < checkBoxList.size(); i++){
                    if(checkBoxList.get(i).isChecked()){
                        completedList.set(i, true);
                    }
                }
            }

            String title = mCreateNoteTitle.getText().toString();
            String content = mCreateNoteContent.getText().toString();

            if(content.isEmpty()){
                Toast.makeText(getApplicationContext(), "Note can not be empty", Toast.LENGTH_SHORT).show();
            }
            else if(title.isEmpty()){
                Toast.makeText(getApplicationContext(), "Set a title", Toast.LENGTH_SHORT).show();
            }
            else{
                progressBar.setVisibility(View.VISIBLE);

                DocumentReference documentReference = firebaseFirestore.collection("notes").document(user.getUid()).collection("myNotes").document();
                Map<String, Object> note = new HashMap<>();
                note.put("title", title);
                note.put("content", content);
                note.put("created", System.currentTimeMillis());
                note.put("checkBoxCount", checkBoxCount);
                note.put("completedList", completedList);

                documentReference.set(note).addOnSuccessListener(unused -> {
                    Toast.makeText(getApplicationContext(), "Note saved", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CreateNoteActivity.this, NotesActivity.class));
                    finish();
                }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed to save note", Toast.LENGTH_SHORT).show());

            }
        });

        addCheckBoxFAB.setOnClickListener(v -> {

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(v.getContext());
            bottomSheetDialog.setContentView(R.layout.add_checkbox_layout);

            EditText addItemText = bottomSheetDialog.findViewById(R.id.addItemText);
            Button addItemBtn = bottomSheetDialog.findViewById(R.id.addItemBtn);

            Objects.requireNonNull(addItemBtn).setOnClickListener(v1 -> {
                String itemText = Objects.requireNonNull(addItemText).getText().toString();

                if(itemText.isEmpty()){
                    Toast.makeText(v1.getContext(), "Item is empty", Toast.LENGTH_SHORT).show();
                }
                else{

                    if(checkBoxCount == 0){
                        mCreateNoteContent.setText("");
                    }

                    checkBoxCount += 1;

                    mCreateNoteContent.setEnabled(false);

                    AppCompatCheckBox newCheckBox = new AppCompatCheckBox(v1.getContext());
                    newCheckBox.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    createNoteCheckBoxLayout.addView(newCheckBox);
                    checkBoxList.add(newCheckBox);
                    completedList.add(false);

                    String prevItems = mCreateNoteContent.getText().toString();
                    if(!prevItems.isEmpty()){
                        String newItems = prevItems + "\n\n" + itemText;
                        mCreateNoteContent.setText(newItems);
                    }
                    else {
                        mCreateNoteContent.setText(itemText);
                    }
                }

                bottomSheetDialog.dismiss();
            });

            bottomSheetDialog.show();

        });

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(CreateNoteActivity.this, NotesActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            startActivity(new Intent(CreateNoteActivity.this, NotesActivity.class));
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