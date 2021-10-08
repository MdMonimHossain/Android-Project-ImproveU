package com.example.improveu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateTaskActivity extends AppCompatActivity {

    Intent data;

    EditText mCreateTaskTitle;
    RadioButton urgentBtn, notUrgentBtn, importantBtn, notImportantBtn;
    SwitchMaterial scheduleSwitch;
    TextView dateText, timeText, recurrenceText;
    Button setDateBtn, setTimeBtn;
    AppCompatSpinner recurrenceSpinner;
    FloatingActionButton mSaveTaskFAB;
    Toolbar toolbar;
    ProgressBar progressBar;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;

    Calendar calendar;

    long doCount, planCount, delegateCount, eliminateCount;
    String taskCountDocId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        mCreateTaskTitle = findViewById(R.id.createTaskTitle);
        urgentBtn = findViewById(R.id.urgentBtn);
        notUrgentBtn = findViewById(R.id.notUrgentBtn);
        importantBtn = findViewById(R.id.importantBtn);
        notImportantBtn = findViewById(R.id.notImportantBtn);
        scheduleSwitch = findViewById(R.id.scheduleSwitch);
        dateText = findViewById(R.id.dateText);
        timeText = findViewById(R.id.timeText);
        setDateBtn = findViewById(R.id.setDateBtn);
        setTimeBtn = findViewById(R.id.setTimeBtn);
        recurrenceSpinner = findViewById(R.id.recurrenceSpinner);
        recurrenceText = findViewById(R.id.recurrenceText);
        mSaveTaskFAB = findViewById(R.id.saveTaskFAB);
        progressBar = findViewById(R.id.progressBarCreateTask);

        toolbar = findViewById(R.id.createTaskToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Add Task");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user = firebaseAuth.getCurrentUser();
        calendar = Calendar.getInstance();

        data = getIntent();
        doCount = data.getLongExtra("doCount", 0);
        planCount = data.getLongExtra("planCount", 0);
        delegateCount = data.getLongExtra("delegateCount", 0);
        eliminateCount = data.getLongExtra("eliminateCount", 0);
        taskCountDocId = data.getStringExtra("taskCountDocId");

        dateText.setVisibility(View.INVISIBLE);
        timeText.setVisibility(View.INVISIBLE);
        setDateBtn.setVisibility(View.INVISIBLE);
        setTimeBtn.setVisibility(View.INVISIBLE);
        recurrenceSpinner.setVisibility(View.INVISIBLE);
        recurrenceText.setVisibility(View.INVISIBLE);

        scheduleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                dateText.setVisibility(View.VISIBLE);
                timeText.setVisibility(View.VISIBLE);
                setDateBtn.setVisibility(View.VISIBLE);
                setTimeBtn.setVisibility(View.VISIBLE);
                recurrenceSpinner.setVisibility(View.VISIBLE);
                recurrenceText.setVisibility(View.VISIBLE);
            }
            else{
                dateText.setVisibility(View.INVISIBLE);
                timeText.setVisibility(View.INVISIBLE);
                setDateBtn.setVisibility(View.INVISIBLE);
                setTimeBtn.setVisibility(View.INVISIBLE);
                recurrenceSpinner.setVisibility(View.INVISIBLE);
                recurrenceText.setVisibility(View.INVISIBLE);
            }
        });

        setDateBtn.setOnClickListener(v -> getDate());

        setTimeBtn.setOnClickListener(v -> getTime());

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.recurrence_array, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recurrenceSpinner.setAdapter(arrayAdapter);

        mSaveTaskFAB.setOnClickListener(v -> {
            String taskTitle = mCreateTaskTitle.getText().toString();

            if(taskTitle.isEmpty()){
                Toast.makeText(getApplicationContext(), "Task is empty", Toast.LENGTH_SHORT).show();
            }
            else{
                progressBar.setVisibility(View.VISIBLE);

                DocumentReference taskCountDocumentReference = firebaseFirestore.collection("taskCount").document(taskCountDocId);
                DocumentReference taskDocumentReference;
                if(urgentBtn.isChecked()){
                    if(importantBtn.isChecked()){
                        taskDocumentReference = firebaseFirestore.collection("tasks").document(user.getUid()).collection("do").document();
                        doCount += 1;
                    }
                    else{
                        taskDocumentReference = firebaseFirestore.collection("tasks").document(user.getUid()).collection("delegate").document();
                        delegateCount += 1;
                    }
                }
                else{
                    if(importantBtn.isChecked()){
                        taskDocumentReference = firebaseFirestore.collection("tasks").document(user.getUid()).collection("plan").document();
                        planCount += 1;
                    }
                    else{
                        taskDocumentReference = firebaseFirestore.collection("tasks").document(user.getUid()).collection("eliminate").document();
                        eliminateCount += 1;
                    }
                }
                Map<String, Object> taskCount = new HashMap<>();
                taskCount.put("userId", user.getUid());
                taskCount.put("doCount", doCount);
                taskCount.put("planCount", planCount);
                taskCount.put("delegateCount", delegateCount);
                taskCount.put("eliminateCount", eliminateCount);
                taskCountDocumentReference.set(taskCount);

                Map<String, Object> task = new HashMap<>();
                task.put("title", taskTitle);
                task.put("completed", false);
                task.put("created", System.currentTimeMillis());

                taskDocumentReference.set(task).addOnSuccessListener(unused -> {
                    Toast.makeText(getApplicationContext(), "Task added", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CreateTaskActivity.this, TasksActivity.class));
                    finish();
                }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed to add task", Toast.LENGTH_SHORT).show());

                if(scheduleSwitch.isChecked()){
                    String dateString = dateText.getText().toString();
                    String timeString = timeText.getText().toString();

                    if(dateString.equals("Set Date") || timeString.equals("Set Time")){
                        Toast.makeText(getApplicationContext(), "Set Date and Time", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Intent intent = new Intent(Intent.ACTION_INSERT);
                        intent.setData(CalendarContract.Events.CONTENT_URI);
                        intent.putExtra(CalendarContract.Events.TITLE, taskTitle);
                        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendar.getTimeInMillis());
                        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, calendar.getTimeInMillis() + 3600000);
                        if(!recurrenceSpinner.getSelectedItem().toString().equals("Do not repeat")){
                            intent.putExtra(CalendarContract.Events.RRULE, "FREQ=" + recurrenceSpinner.getSelectedItem().toString().toUpperCase());
                        }

                        try {
                           startActivity(intent);
                        }catch (ActivityNotFoundException e){
                            Toast.makeText(getApplicationContext(), "No supported app found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

            }
        });

    }

    private void getTime() {
        Calendar calendar1 = Calendar.getInstance();
        int startHour = calendar1.get(Calendar.HOUR_OF_DAY);
        if(startHour != 23){
            startHour += 1;
        }
        else {
            startHour = 0;
        }

        boolean is24HourFormat = DateFormat.is24HourFormat(this);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {

            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            CharSequence timeCharSequence = DateFormat.format("hh:mm a", calendar);
            timeText.setText(timeCharSequence);
        }, startHour, 0, is24HourFormat);

        timePickerDialog.show();
    }

    private void getDate() {
        Calendar calendar1 = Calendar.getInstance();
        int startYear = calendar1.get(Calendar.YEAR);
        int startMonth = calendar1.get(Calendar.MONTH);
        int startDate = calendar1.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {

            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DATE, dayOfMonth);

            CharSequence dateCharSequence = DateFormat.format("E, MMM d, yyyy", calendar);
            dateText.setText(dateCharSequence);
        }, startYear, startMonth, startDate);

        datePickerDialog.show();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(CreateTaskActivity.this, TasksActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            startActivity(new Intent(CreateTaskActivity.this, TasksActivity.class));
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