package com.example.moviematch;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    TextView yourTaste;
    Button goToSignIn, goToSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        goToSignIn = findViewById(R.id.splashGoToSignInBtn);
        goToSignUp = findViewById(R.id.splashGoToSignUpBtn);
        yourTaste = findViewById(R.id.signUpHelloTV);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
            return;
        }

        int startColor = Color.parseColor("#FFB4B4");
        int endColor = Color.parseColor("#FEFFBB");

        LinearGradient linearGradient = new LinearGradient(
                0f, 0f, yourTaste.getPaint().measureText(yourTaste.getText().toString()), yourTaste.getTextSize(),
                new int[] {startColor, endColor},
                null, Shader.TileMode.CLAMP);

        yourTaste.getPaint().setShader(linearGradient);

        goToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });

        goToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SplashActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}