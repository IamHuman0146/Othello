package com.example.othellodarren;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnVsAI = findViewById(R.id.btnVsAI);
        Button btnVsPlayer = findViewById(R.id.btnVsPlayer);
        Button btnChangeTheme = findViewById(R.id.btnChangeTheme);
        Button btnBlack = findViewById(R.id.btnBlack);
        Button btnWhite = findViewById(R.id.btnWhite);
        TextView titleText = findViewById(R.id.titleText);
        titleText.setVisibility(View.VISIBLE);

        btnBlack.setVisibility(View.GONE);
        btnWhite.setVisibility(View.GONE);


        btnVsAI.setOnClickListener(v -> {

            btnVsAI.setVisibility(View.GONE);
            btnVsPlayer.setVisibility(View.GONE);
            btnChangeTheme.setVisibility(View.GONE);
            titleText.setVisibility(View.GONE);


            btnBlack.setVisibility(View.VISIBLE);
            btnWhite.setVisibility(View.VISIBLE);

            alignButtonsWithTheme();
        });

        btnBlack.setOnClickListener(v -> startGame('X'));
        btnWhite.setOnClickListener(v -> startGame('O'));


        btnVsPlayer.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OthelloGameActivity.class);
            startActivity(intent);
        });

        btnChangeTheme.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ThemeSelectionActivity.class);
            startActivity(intent);
        });
    }
    private void startGame(char playerColor) {
        Intent intent = new Intent(MainActivity.this, OthelloGameActivity.class);
        intent.putExtra("playerColor", playerColor);
        startActivity(intent);
    }
    private void alignButtonsWithTheme() {
        Button btnBlack = findViewById(R.id.btnBlack);
        Button btnWhite = findViewById(R.id.btnWhite);


        btnBlack.setBackgroundColor(getResources().getColor(R.color.tile_black_green));
        btnWhite.setBackgroundColor(getResources().getColor(R.color.tile_black_green));
    }

}
