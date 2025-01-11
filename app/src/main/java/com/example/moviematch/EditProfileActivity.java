package com.example.moviematch;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editName, editUsername;
    private Button saveProfileBtn;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editName = findViewById(R.id.editNameET);
        editUsername = findViewById(R.id.editUsernameET);
        saveProfileBtn = findViewById(R.id.editSaveProfileBtn);

        firestore = FirebaseFirestore.getInstance();

        loadUserData();

        saveProfileBtn.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadUserData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = firestore.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                editName.setText(documentSnapshot.getString("name"));
                editUsername.setText(documentSnapshot.getString("username"));
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveProfileChanges() {
        String newName = editName.getText().toString().trim();
        String newUsername = editUsername.getText().toString().trim();

        if (newName.isEmpty() || newUsername.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = firestore.collection("users").document(userId);

        userRef.update("name", newName, "username", newUsername)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                });
    }
}
