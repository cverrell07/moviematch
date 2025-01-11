package com.example.moviematch;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ListenerRegistration;

public class ProfileFragment extends Fragment {

    private LinearLayout btnLogout, editProfileBtn;
    private TextView profileNameTV;
    private TextView profileUsernameTV;

    private FirebaseFirestore firestore;
    private ListenerRegistration userListener;

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        btnLogout = view.findViewById(R.id.profileLogoutBtn);
        profileNameTV = view.findViewById(R.id.profileNameTV);
        profileUsernameTV = view.findViewById(R.id.profileUsernameTV);
        editProfileBtn = view.findViewById(R.id.profileEditProfileBtn);

        firestore = FirebaseFirestore.getInstance();

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(getActivity(), SignInActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        editProfileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        int startColor = Color.parseColor("#FFB4B4");
        int endColor = Color.parseColor("#FEFFBB");

        LinearGradient linearGradient = new LinearGradient(
                0f, 0f, profileNameTV.getPaint().measureText(profileNameTV.getText().toString()), profileNameTV.getTextSize(),
                new int[]{startColor, endColor},
                null, Shader.TileMode.CLAMP);

        profileNameTV.getPaint().setShader(linearGradient);

        fetchUserData();

        return view;
    }

    private void fetchUserData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = firestore.collection("users").document(userId);

        userListener = userRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String username = documentSnapshot.getString("username");

                profileNameTV.setText(name);
                profileUsernameTV.setText(username);
            } else {
                profileNameTV.setText("Full Name");
                profileUsernameTV.setText("username");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userListener != null) {
            userListener.remove();
        }
    }
}