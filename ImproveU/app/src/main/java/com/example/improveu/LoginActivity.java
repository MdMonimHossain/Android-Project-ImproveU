package com.example.improveu;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.shobhitpuri.custombuttons.GoogleSignInButton;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    TextView newUser;
    EditText signInEmail, signInPassword;
    Button signInBtn;
    TextView forgotPassword;
    GoogleSignInButton googleSignInBtn;
    ProgressBar progressBar;

    GoogleSignInOptions gso;
    GoogleSignInClient signInClient;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    public static final int GOOGLE_SIGN_IN_CODE = 10005;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        newUser = findViewById(R.id.newUserTextView);
        signInEmail = findViewById(R.id.signInEmailAddress);
        signInPassword = findViewById(R.id.signInPassword);
        signInBtn = findViewById(R.id.signInBtn);
        forgotPassword = findViewById(R.id.forgotPassword);
        googleSignInBtn = findViewById(R.id.googleSignIn);
        progressBar = findViewById(R.id.progressBarLogin);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("639799173043-t0mdd1asfp75s118ct1o8rj8ef97du5a.apps.googleusercontent.com")
                .requestEmail()
                .build();

        signInClient = GoogleSignIn.getClient(this, gso);

        //Not needed as it is already checked if a user is logged in or not in IntroActivity
        /*
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(signInAccount != null || firebaseAuth.getCurrentUser() != null){
            Toast.makeText(this, "Already logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
            finish();
        }
        */

        googleSignInBtn.setOnClickListener(v -> {
            Intent signIn = signInClient.getSignInIntent();
            //noinspection deprecation
            startActivityForResult(signIn, GOOGLE_SIGN_IN_CODE);
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        newUser.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });

        signInBtn.setOnClickListener(view -> loginUser());

        forgotPassword.setOnClickListener(v -> {

            EditText resetMail = new EditText((v.getContext()));
            resetMail.setHint("Enter your email address");

            AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
            passwordResetDialog.setTitle("Reset Password?");
            passwordResetDialog.setMessage("A recovery email will be sent to your email address");
            passwordResetDialog.setView(resetMail);

            passwordResetDialog.setPositiveButton("OK", (dialog, which) -> {
                String email = resetMail.getText().toString();
                if(!TextUtils.isEmpty(email)){
                    firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener(unused -> Toast.makeText(LoginActivity.this, "Reset link sent to your email", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });

            passwordResetDialog.setNegativeButton("BACK", (dialog, which) -> {

            });

            passwordResetDialog.create().show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GOOGLE_SIGN_IN_CODE){
            Task<GoogleSignInAccount> googleSignInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount googleSignInAccount = googleSignInAccountTask.getResult(ApiException.class);

                AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);

                progressBar.setVisibility(View.VISIBLE);

                firebaseAuth.signInWithCredential(authCredential).addOnSuccessListener(e -> {

                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    CollectionReference collectionReference = firebaseFirestore.collection("taskCount");
                    Query query = collectionReference.whereEqualTo("userId", Objects.requireNonNull(user).getUid());
                    query.limit(1).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if(task.getResult().isEmpty()){
                                DocumentReference documentReference = firebaseFirestore.collection("taskCount").document();
                                Map<String, Object> taskCount = new HashMap<>();
                                taskCount.put("userId", user.getUid());
                                taskCount.put("doCount", 0);
                                taskCount.put("planCount", 0);
                                taskCount.put("delegateCount", 0);
                                taskCount.put("eliminateCount", 0);
                                documentReference.set(taskCount);
                            }
                        } else {
                            Log.d("failed", "Error getting documents: ", task.getException());
                        }
                    });

                    Toast.makeText(getApplicationContext(), "Google account connected to ImproveU", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    finish();
                }).addOnFailureListener(e -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Login error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void loginUser() {
        String email = signInEmail.getText().toString();
        String password = signInPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            signInEmail.setError("Email cannot be empty");
            signInEmail.requestFocus();
        }
        else if(TextUtils.isEmpty(password)){
            signInPassword.setError("Password cannot be empty");
            signInPassword.requestFocus();
        }
        else{
            progressBar.setVisibility(View.VISIBLE);
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    checkEmailVerified();
                }
                else{
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Login error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void checkEmailVerified() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            if(user.isEmailVerified()){
                Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                finish();
            }
            else{
                progressBar.setVisibility(View.INVISIBLE);
                //Toast.makeText(getApplicationContext(), "Verify your email", Toast.LENGTH_SHORT).show();


                AlertDialog.Builder verifyEmailDialog = new AlertDialog.Builder(LoginActivity.this);
                verifyEmailDialog.setTitle("Verify Your Email");
                verifyEmailDialog.setMessage("A verification email was sent to your email address");
                verifyEmailDialog.setPositiveButton("OK", (dialog, which) -> {
                    firebaseAuth.signOut();
                });
                verifyEmailDialog.setNegativeButton("RESEND", (dialog, which) -> user.sendEmailVerification().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(getApplicationContext(), "Verification email is sent. Verify and login", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Verification error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    firebaseAuth.signOut();
                }));
                verifyEmailDialog.setCancelable(false);
                verifyEmailDialog.create().show();

            }
        }
    }
}