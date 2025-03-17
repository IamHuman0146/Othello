package com.example.othellodarren;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);  // ✅ Apply saved theme before anything loads
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnVsAI = findViewById(R.id.btnVsAI);
        Button btnVsPlayer = findViewById(R.id.btnVsPlayer);
        Button btnChangeTheme = findViewById(R.id.btnChangeTheme);  // ✅ Add theme button

        btnVsPlayer.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OthelloGameActivity.class);
            startActivity(intent);
        });

        btnChangeTheme.setOnClickListener(v -> {  // ✅ Open theme selection screen
            Intent intent = new Intent(MainActivity.this, ThemeSelectionActivity.class);
            startActivity(intent);
        });
    }
}
