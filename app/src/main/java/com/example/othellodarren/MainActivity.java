package com.example.othellodarren;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnVsAI = findViewById(R.id.btnVsAI);
        Button btnVsPlayer = findViewById(R.id.btnVsPlayer);

        btnVsPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OthelloGameActivity.class);
                startActivity(intent);
            }
        });

        btnVsAI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // AI mode (not implemented yet)
            }
        });
    }
}
