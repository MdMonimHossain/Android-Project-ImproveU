package com.example.improveu;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    TextView alreadyUser;
    EditText signupEmail, signupPassword, signupConfirmPassword;
    Button signupBtn;
    ProgressBar progressBar;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        alreadyUser = findViewById(R.id.alreadyUserTextView);
        signupEmail = findViewById(R.id.signupEmailAddress);
        signupPassword = findViewById(R.id.signupPassword);
        signupConfirmPassword = findViewById(R.id.signupConfirmPassword);
        signupBtn = findViewById(R.id.signupBtn);
        progressBar = findViewById(R.id.progressBarSignup);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        alreadyUser.setOnClickListener(view -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
        signupBtn.setOnClickListener(view -> createUser());
    }

    private void createUser() {
        String email = signupEmail.getText().toString();
        String password = signupPassword.getText().toString();
        String confirmPassword = signupConfirmPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            signupEmail.setError("Email cannot be empty");
            signupEmail.requestFocus();
        }
        else if(TextUtils.isEmpty(password)){
            signupPassword.setError("Password cannot be empty");
            signupPassword.requestFocus();
        }
        else if(!password.equals(confirmPassword)){
            signupConfirmPassword.setError("Passwords didn't match");
            signupConfirmPassword.requestFocus();
        }
        else{
            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    sendEmailVerification(user);

                    if(user != null){
                        DocumentReference documentReference = firebaseFirestore.collection("taskCount").document();
                        Map<String, Object> taskCount = new HashMap<>();
                        taskCount.put("userId", user.getUid());
                        taskCount.put("doCount", 0);
                        taskCount.put("planCount", 0);
                        taskCount.put("delegateCount", 0);
                        taskCount.put("eliminateCount", 0);
                        documentReference.set(taskCount);
                    }

                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                    finish();
                }
                else{
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Sign up error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void sendEmailVerification(FirebaseUser user) {
        if(user != null){
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Verification email is sent. Verify and login", Toast.LENGTH_SHORT).show();
                    firebaseAuth.signOut();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Verification error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}