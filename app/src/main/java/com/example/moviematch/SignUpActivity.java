package com.example.moviematch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText nameSignUpET, usernameSignUpET, emailSignUpET, passwordSignUpET, confirmPasswordSignUpET;
    private Button signUpBtn;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView goToSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameSignUpET = findViewById(R.id.nameSignUpET);
        usernameSignUpET = findViewById(R.id.usernameSignUpET);
        emailSignUpET = findViewById(R.id.emailSignUpET);
        passwordSignUpET = findViewById(R.id.passwordSignUpET);
        confirmPasswordSignUpET = findViewById(R.id.confirmPasswordSignUpET);
        signUpBtn = findViewById(R.id.signUpBtn);
        goToSignIn = findViewById(R.id.goToSignIn);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

        goToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });
    }

    private void createAccount() {
        String name = nameSignUpET.getText().toString().trim();
        String username = usernameSignUpET.getText().toString().trim();
        String email = emailSignUpET.getText().toString().trim();
        String password = passwordSignUpET.getText().toString().trim();
        String confirmPassword = confirmPasswordSignUpET.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Oops, you need to fill all fields...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Can you please enter the same password?", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("name", name);
                    userData.put("username", username);
                    userData.put("email", email);
                    userData.put("userId", user.getUid());

                    new Handler().postDelayed(() -> {
                        db.collection("users").document(user.getUid()).set(userData).addOnCompleteListener(dbTask -> {
                            if (dbTask.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, "Yay, succeed to create your account!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                                finish();
                            } else {
                                Toast.makeText(SignUpActivity.this, "Oops, there's an error: " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }, 200);
                }
            } else {
                Toast.makeText(SignUpActivity.this, "Account failed to be create: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}