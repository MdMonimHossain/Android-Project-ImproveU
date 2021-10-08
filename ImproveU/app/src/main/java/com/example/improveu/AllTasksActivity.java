package com.example.improveu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AllTasksActivity extends AppCompatActivity {

    Intent data;

    ConstraintLayout allTasksLayout;
    Toolbar toolbar;
    RecyclerView mRecyclerView;
    StaggeredGridLayoutManager staggeredGridLayoutManager;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;

    long doCount, planCount, delegateCount, eliminateCount;
    String taskCountDocId, taskType;

    int backgroundColor;

    FirestoreRecyclerAdapter<TaskModel, AllTasksActivity.TaskViewHolder> taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_tasks);

        allTasksLayout = findViewById(R.id.allTasksLayout);

        data = getIntent();
        taskType = data.getStringExtra("taskType");
        doCount = data.getLongExtra("doCount", 0);
        planCount = data.getLongExtra("planCount", 0);
        delegateCount = data.getLongExtra("delegateCount", 0);
        eliminateCount = data.getLongExtra("eliminateCount", 0);
        taskCountDocId = data.getStringExtra("taskCountDocId");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        switch (taskType) {
            case "Do":
                backgroundColor = R.color.pastelRed;
                break;
            case "Plan":
                backgroundColor = R.color.pastelGreen;
                break;
            case "Delegate":
                backgroundColor = R.color.pastelYellow;
                break;
            default:
                backgroundColor = R.color.pastelBlue;
                break;
        }
        Objects.requireNonNull(getSupportActionBar()).setTitle(taskType);
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(backgroundColor)));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //allTasksLayout.setBackgroundColor(getResources().getColor(backgroundColor));  //Background color set

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user = firebaseAuth.getCurrentUser();

        Query query = firebaseFirestore.collection("tasks").document(user.getUid()).collection(taskType.toLowerCase()).orderBy("completed", Query.Direction.ASCENDING).orderBy("created", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<TaskModel> userAllTasks = new FirestoreRecyclerOptions.Builder<TaskModel>().setQuery(query, TaskModel.class).build();
        taskAdapter = new FirestoreRecyclerAdapter<TaskModel, TaskViewHolder>(userAllTasks) {
            @Override
            protected void onBindViewHolder(@NonNull TaskViewHolder taskViewHolder, int i, @NonNull TaskModel taskModel) {

                String title = taskModel.getTitle();
                boolean completed = taskModel.isCompleted();
                long created = taskModel.getCreated();
                String docId = taskAdapter.getSnapshots().getSnapshot(i).getId();

                taskViewHolder.taskTitle.setText(title);
                taskViewHolder.checkBox.setChecked(completed);
                if(completed){
                    taskViewHolder.taskTitle.setPaintFlags(taskViewHolder.taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    taskViewHolder.taskTitle.setTextColor(getResources().getColor(R.color.grey));
                }else {
                    taskViewHolder.taskTitle.setPaintFlags(taskViewHolder.taskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    taskViewHolder.taskTitle.setTextColor(getResources().getColor(R.color.lightBlack));
                }

                //taskViewHolder.taskLayout.setBackgroundColor(getResources().getColor(backgroundColor));   //individual task background color set

                taskViewHolder.checkBox.setOnClickListener(v -> {
                    boolean isChecked;
                    isChecked = taskViewHolder.checkBox.isChecked();
                    DocumentReference documentReference = firebaseFirestore.collection("tasks").document(user.getUid()).collection(taskType.toLowerCase()).document(docId);
                    Map<String, Object> task = new HashMap<>();
                    task.put("completed", isChecked);
                    //task.put("created", System.currentTimeMillis());
                    documentReference.update(task);

                    if(isChecked){
                        reduceTaskCount();
                    }
                    else {
                        increaseTaskCount();
                    }
                    updateTaskCount();
                });

                taskViewHolder.itemView.setOnClickListener(v -> {

                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(v.getContext());
                    bottomSheetDialog.setContentView(R.layout.edit_task_layout);

                    EditText titleText = bottomSheetDialog.findViewById(R.id.editTaskTitle);
                    Button deleteBtn = bottomSheetDialog.findViewById(R.id.editTaskDeleteBtn);
                    Button saveBtn = bottomSheetDialog.findViewById(R.id.editTaskSaveBtn);

                    Objects.requireNonNull(titleText).setText(title);
                    titleText.requestFocus();

                    Objects.requireNonNull(deleteBtn).setOnClickListener(v12 -> {
                        DocumentReference documentReference = firebaseFirestore.collection("tasks").document(user.getUid()).collection(taskType.toLowerCase()).document(docId);
                        documentReference.delete().addOnSuccessListener(unused -> Toast.makeText(v12.getContext(), "Task Deleted", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(v12.getContext(), "Failed to delete", Toast.LENGTH_SHORT).show());

                        if(!completed){
                            reduceTaskCount();
                            updateTaskCount();
                        }

                        bottomSheetDialog.dismiss();
                    });

                    Objects.requireNonNull(saveBtn).setOnClickListener(v1 -> {
                        String editedTitle = titleText.getText().toString();
                        if(editedTitle.isEmpty()){
                            Toast.makeText(v1.getContext(), "Task is empty", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            DocumentReference documentReference = firebaseFirestore.collection("tasks").document(user.getUid()).collection(taskType.toLowerCase()).document(docId);
                            Map<String, Object> task = new HashMap<>();
                            task.put("title", editedTitle);
                            //task.put("created", System.currentTimeMillis());
                            documentReference.update(task).addOnSuccessListener(unused -> Toast.makeText(v1.getContext(), "Task updated", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(v1.getContext(), "Failed to update", Toast.LENGTH_SHORT).show());

                            bottomSheetDialog.dismiss();
                        }
                    });

                    bottomSheetDialog.show();
                });

            }

            @NonNull
            @Override
            public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tasks_layout, parent, false);
                return new TaskViewHolder(view);
            }
        };

        mRecyclerView = findViewById(R.id.tasksRecyclerview);
        mRecyclerView.setHasFixedSize(true);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        mRecyclerView.setAdapter(taskAdapter);

    }

    private void reduceTaskCount() {
        switch (taskType) {
            case "Do":
                doCount -= 1;
                break;
            case "Plan":
                planCount -= 1;
                break;
            case "Delegate":
                delegateCount -= 1;
                break;
            default:
                eliminateCount -= 1;
                break;
        }
    }

    private void increaseTaskCount() {
        switch (taskType) {
            case "Do":
                doCount += 1;
                break;
            case "Plan":
                planCount += 1;
                break;
            case "Delegate":
                delegateCount += 1;
                break;
            default:
                eliminateCount += 1;
                break;
        }
    }

    private void updateTaskCount(){
        DocumentReference taskCountDocumentReference = firebaseFirestore.collection("taskCount").document(taskCountDocId);
        Map<String, Object> taskCount = new HashMap<>();
        taskCount.put("userId", user.getUid());
        taskCount.put("doCount", doCount);
        taskCount.put("planCount", planCount);
        taskCount.put("delegateCount", delegateCount);
        taskCount.put("eliminateCount", eliminateCount);
        taskCountDocumentReference.set(taskCount);
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder{

        private final TextView taskTitle;
        private final MaterialCheckBox checkBox;
        private final LinearLayout taskLayout;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            checkBox = itemView.findViewById(R.id.isCompletedBox);
            taskLayout = itemView.findViewById(R.id.taskLayout);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        taskAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(taskAdapter != null){
            taskAdapter.stopListening();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AllTasksActivity.this, TasksActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            startActivity(new Intent(AllTasksActivity.this, TasksActivity.class));
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