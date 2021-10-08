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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class EditNoteActivity extends AppCompatActivity {

    Intent data;
    EditText mEditNoteTitle, mEditNoteContent;
    TextView mEditNoteTime;
    FloatingActionButton mSaveEditedNoteFAB;
    ProgressBar progressBar;
    Toolbar toolbar;

    LinearLayout editNoteCheckBoxLayout;
    FloatingActionButton addCheckBoxFAB;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser user;

    int checkBoxCount;
    List<Boolean> completedList;
    List<AppCompatCheckBox> checkBoxList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        mEditNoteTitle = findViewById(R.id.editNoteTitle);
        mEditNoteContent = findViewById(R.id.editNoteContent);
        mEditNoteTime = findViewById(R.id.editNoteTime);
        mSaveEditedNoteFAB = findViewById(R.id.saveEditedNoteFAB);
        editNoteCheckBoxLayout = findViewById(R.id.editNoteCheckBoxLayout);
        addCheckBoxFAB = findViewById(R.id.addCheckBoxEditFAB);
        progressBar = findViewById(R.id.progressBarEditNote);

        toolbar = findViewById(R.id.editNoteToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        data = getIntent();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        String noteTitle = data.getStringExtra("title");
        String noteContent = data.getStringExtra("content");
        long noteCreated = data.getLongExtra("created", 0);
        checkBoxCount = data.getIntExtra("checkBoxCount", 0);
        completedList = (List<Boolean>)data.getSerializableExtra("completedList");
        checkBoxList = new ArrayList<>();

        if(checkBoxCount == 0){
            addCheckBoxFAB.setVisibility(View.INVISIBLE);
        }
        else{
            mEditNoteContent.setEnabled(false);

            for(int i = 0; i < completedList.size(); i++){
                AppCompatCheckBox newCheckBox = new AppCompatCheckBox(EditNoteActivity.this);
                newCheckBox.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                newCheckBox.setChecked(completedList.get(i));
                editNoteCheckBoxLayout.addView(newCheckBox);
                checkBoxList.add(newCheckBox);
            }
        }

        String noteTime = getTime(noteCreated);
        mEditNoteTime.setText(noteTime);
        mEditNoteTitle.setText(noteTitle);
        mEditNoteContent.setText(noteContent);

        mSaveEditedNoteFAB.setOnClickListener(v -> {

            if(checkBoxCount != 0){
                for(int i = 0; i < checkBoxList.size(); i++){
                    if(checkBoxList.get(i).isChecked()){
                        completedList.set(i, true);
                    }
                    else{
                        completedList.set(i, false);
                    }
                }
            }

            String title = mEditNoteTitle.getText().toString();
            String content = mEditNoteContent.getText().toString();
            long created = System.currentTimeMillis();

            if(content.isEmpty()){
                Toast.makeText(getApplicationContext(), "Note can not be empty", Toast.LENGTH_SHORT).show();
            }
            else if(title.isEmpty()){
                Toast.makeText(getApplicationContext(), "Title can not be empty", Toast.LENGTH_SHORT).show();
            }
            else{
                progressBar.setVisibility(View.VISIBLE);

                DocumentReference documentReference = firebaseFirestore.collection("notes").document(user.getUid()).collection("myNotes").document(data.getStringExtra("noteId"));
                Map <String, Object> note = new HashMap<>();
                note.put("title", title);
                note.put("content", content);
                note.put("created", created);
                note.put("checkBoxCount", checkBoxCount);
                note.put("completedList", completedList);

                documentReference.set(note).addOnSuccessListener(unused -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Note Updated", Toast.LENGTH_SHORT).show();
                    String time = getTime(created);
                    mEditNoteTime.setText(time);
                }).addOnFailureListener(e -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Failed to update", Toast.LENGTH_SHORT).show();
                });

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
                    checkBoxCount += 1;

                    AppCompatCheckBox newCheckBox = new AppCompatCheckBox(v1.getContext());
                    newCheckBox.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    editNoteCheckBoxLayout.addView(newCheckBox);
                    checkBoxList.add(newCheckBox);
                    completedList.add(false);

                    String prevItems = mEditNoteContent.getText().toString();
                    String newItems = prevItems + "\n\n" + itemText;
                    mEditNoteContent.setText(newItems);
                }

                bottomSheetDialog.dismiss();
            });

            bottomSheetDialog.show();

        });

    }

    private String getTime(long created) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM, yy hh:mm aa", Locale.US);
        Date date = new Date(created);
        return simpleDateFormat.format(date);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(EditNoteActivity.this, NotesActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            startActivity(new Intent(EditNoteActivity.this, NotesActivity.class));
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