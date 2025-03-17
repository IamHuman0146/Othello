package com.example.othellodarren;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ThemeSelectionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_selection);

        Button btnGreen = findViewById(R.id.btnGreen);
        Button btnBlueGray = findViewById(R.id.btnBlueGray);
        Button btnDark = findViewById(R.id.btnDark);
        Button btnEarth = findViewById(R.id.btnEarth);

        btnGreen.setOnClickListener(v -> changeTheme("Green"));
        btnBlueGray.setOnClickListener(v -> changeTheme("BlueGray"));
        btnDark.setOnClickListener(v -> changeTheme("Dark"));
        btnEarth.setOnClickListener(v -> changeTheme("Earth"));
    }

    private void changeTheme(String themeName) {
        ThemeUtils.setTheme(this, themeName);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
