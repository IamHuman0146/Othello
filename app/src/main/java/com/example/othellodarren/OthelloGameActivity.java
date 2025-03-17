package com.example.othellodarren;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.util.TypedValue;


public class OthelloGameActivity extends AppCompatActivity {

    private static final int BOARD_SIZE = 8;
    private ImageView[][] boardCells = new ImageView[BOARD_SIZE][BOARD_SIZE];
    private OthelloGame game;
    private TextView player1Tiles, player2Tiles, gameTimer, pausedText, gameOverText, notificationText;
    private LinearLayout player1Panel, player2Panel, gameOverScreen, hintContainer, notificationContainer;
    private ImageView pauseButton, backToMenuButton;
    private GridLayout boardGrid;
    private Switch hintSwitch;

    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private int secondsRemaining = 30;
    private boolean isPaused = false;
    private boolean showHints = true;
    private boolean timerExpired = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtils.applyTheme(this); // ✅ Apply selected theme
        setContentView(R.layout.activity_othello_game);

        // ✅ Initialize views
        boardGrid = findViewById(R.id.boardGrid);
        player1Tiles = findViewById(R.id.player1Tiles);
        player2Tiles = findViewById(R.id.player2Tiles);
        gameTimer = findViewById(R.id.gameTimer);
        player1Panel = findViewById(R.id.player1Panel);
        player2Panel = findViewById(R.id.player2Panel);
        pauseButton = findViewById(R.id.pauseButton);
        pausedText = findViewById(R.id.pausedText);
        gameOverScreen = findViewById(R.id.gameOverScreen);
        gameOverText = findViewById(R.id.gameOverText);
        hintContainer = findViewById(R.id.hintContainer);
        notificationContainer = findViewById(R.id.notificationContainer);
        notificationText = findViewById(R.id.notificationText);
        hintSwitch = findViewById(R.id.hintSwitch);
        backToMenuButton = findViewById(R.id.backToMenuButton);

        hintSwitch.setChecked(true);
        showHints = true;
        notificationContainer.setVisibility(View.GONE);

        // ✅ Apply board background dynamically
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.board_background, typedValue, true);
        int boardBackgroundColor = typedValue.data;

        View rootView = findViewById(android.R.id.content);
        rootView.setBackgroundColor(boardBackgroundColor);
        boardGrid.setBackgroundColor(boardBackgroundColor);

        pauseButton.setOnClickListener(v -> togglePause());
        findViewById(R.id.rematchButton).setOnClickListener(v -> restartGame());
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        backToMenuButton.setOnClickListener(v -> showExitConfirmationDialog());

        hintSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showHints = isChecked;
            updateBoard();
        });

        // ✅ Initialize game and UI
        game = new OthelloGame();
        setupBoard();
        updateBoard();
        updatePlayerInfo();
        startGameTimer();
    }





    private void showExitConfirmationDialog() {
        // Pause the game while dialog is showing
        boolean wasPaused = isPaused;
        if (!isPaused) {
            togglePause();
        }

        // Create an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit Game");
        builder.setMessage("Are you sure you want to exit? Your game progress will be lost.");

        // Add buttons
        builder.setPositiveButton("Exit", (dialog, which) -> {
            // Return to main menu
            finish();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Resume the game if it wasn't paused before
            if (!wasPaused) {
                togglePause();
            }
        });

        // Show the dialog
        builder.create().show();
    }

    private void setupBoard() {
        int screenSize = Math.min(getResources().getDisplayMetrics().widthPixels,
                getResources().getDisplayMetrics().heightPixels) - 100;
        int cellSize = screenSize / BOARD_SIZE;

        // ✅ Get board border color dynamically from the theme
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.tile_border, typedValue, true);
        int borderColor = typedValue.data;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ImageView cell = new ImageView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                params.setMargins(2, 2, 2, 2);
                cell.setLayoutParams(params);
                cell.setBackgroundColor(borderColor);
                cell.setImageResource(R.drawable.circle_empty);

                final int finalRow = row, finalCol = col;
                cell.setOnClickListener(v -> {
                    if (!isPaused && game.makeMove(finalRow, finalCol)) {
                        updateBoard(); // ✅ Apply new theme colors dynamically
                        updatePlayerInfo();
                        resetTimer();
                    }
                });

                boardGrid.addView(cell);
                boardCells[row][col] = cell;
            }
        }
    }


    private void updateBoard() {
        char[][] board = game.getBoard();
        boolean[][] validMoves = game.getValidMoves();

        // ✅ Get tile colors dynamically from theme
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.tile_black, typedValue, true);
        int blackTileColor = typedValue.data;

        getTheme().resolveAttribute(R.attr.tile_white, typedValue, true);
        int whiteTileColor = typedValue.data;

        getTheme().resolveAttribute(R.attr.tile_border, typedValue, true);
        int borderColor = typedValue.data;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] == 'X') {
                    boardCells[row][col].setImageResource(R.drawable.circle_black);
                    boardCells[row][col].setColorFilter(blackTileColor); // ✅ Apply dynamic theme color
                } else if (board[row][col] == 'O') {
                    boardCells[row][col].setImageResource(R.drawable.circle_white);
                    boardCells[row][col].setColorFilter(whiteTileColor); // ✅ Apply dynamic theme color
                } else if (showHints && validMoves[row][col]) {
                    boardCells[row][col].setImageResource(R.drawable.circle_hint);
                } else {
                    boardCells[row][col].setImageResource(R.drawable.circle_empty);
                }
                boardCells[row][col].setBackgroundColor(borderColor); // ✅ Ensure border color updates dynamically
            }
        }
    }





    private void updatePlayerInfo() {
        player1Tiles.setText(getString(R.string.player_tiles, game.getPlayer1Tiles()));
        player2Tiles.setText(getString(R.string.player_tiles, game.getPlayer2Tiles()));

        // ✅ Get primary theme color dynamically
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.panel_background, typedValue, true);
        int panelColor = typedValue.data;

        // ✅ Apply the theme color to player panels
        player1Panel.setBackgroundColor(panelColor);
        player2Panel.setBackgroundColor(panelColor);

        if (game.getCurrentPlayer() == 'X') {
            player1Panel.setAlpha(1.0f);
            player2Panel.setAlpha(0.5f);
        } else if (game.getCurrentPlayer() == 'O') {
            player1Panel.setAlpha(0.5f);
            player2Panel.setAlpha(1.0f);
        } else {
            showGameOverScreen();
        }
    }



    private void skipTurn() {
        game.switchTurn();

        if (game.getCurrentPlayer() == ' ') {
            showGameOverScreen();
        } else {
            updateBoard();
            updatePlayerInfo();
            resetTimer();
        }
    }

    private void showGameOverScreen() {
        String winnerMessage;

        int blackTiles = game.getPlayer1Tiles();
        int whiteTiles = game.getPlayer2Tiles();

        if (blackTiles > whiteTiles) {
            winnerMessage = "🏆 Player 1 (Black) Wins! 🏆";
        } else if (whiteTiles > blackTiles) {
            winnerMessage = "🏆 Player 2 (White) Wins! 🏆";
        } else {
            winnerMessage = "🤝 It's a Tie!";
        }

        gameOverText.setText(winnerMessage);
        boardGrid.setVisibility(View.GONE);
        hintContainer.setVisibility(View.GONE);
        notificationContainer.setVisibility(View.GONE);
        gameOverScreen.setVisibility(View.VISIBLE);
    }

    private void restartGame() {
        recreate();
    }

    private void togglePause() {
        isPaused = !isPaused;

        if (isPaused) {
            boardGrid.setVisibility(View.GONE);
            hintContainer.setVisibility(View.GONE);
            notificationContainer.setVisibility(View.GONE);
            pausedText.setVisibility(View.VISIBLE);
        } else {
            boardGrid.setVisibility(View.VISIBLE);
            hintContainer.setVisibility(View.VISIBLE);
            pausedText.setVisibility(View.GONE);
        }
    }

    private void startGameTimer() {
        // Cancel any existing timer
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPaused) {
                    secondsRemaining--;
                    gameTimer.setText("00:" + String.format("%02d", secondsRemaining));

                    // Make timer text red when less than 10 seconds
                    if (secondsRemaining <= 10) {
                        gameTimer.setTextColor(ContextCompat.getColor(OthelloGameActivity.this, android.R.color.holo_red_light));
                    } else {
                        gameTimer.setTextColor(ContextCompat.getColor(OthelloGameActivity.this, android.R.color.white));
                    }

                    // Timer runs out
                    if (secondsRemaining <= 0 && !timerExpired) {
                        timerExpired = true; // Prevent multiple executions
                        showNotification("Time's up! Turn skipped.");

                        // Use a separate delayed handler for switching turns
                        // This ensures the UI updates properly
                        new Handler().postDelayed(() -> {
                            // Switch player turns
                            game.switchTurn();

                            // Update UI
                            updateBoard();
                            updatePlayerInfo();

                            // Reset timer and timer expired flag
                            resetTimer();
                            timerExpired = false;

                            // Hide notification
                            hideNotification();
                        }, 2000);

                        // Don't reschedule the timer here - let the postDelayed handle it
                        return;
                    }
                }

                // Continue timer if game is still active
                if (game.getCurrentPlayer() != ' ') {
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };

        // Start the timer
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void resetTimer() {
        secondsRemaining = 30;
        gameTimer.setText("00:30");
        gameTimer.setTextColor(ContextCompat.getColor(this, android.R.color.white));

        // Restart the timer
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
            timerHandler.postDelayed(timerRunnable, 1000);
        }
    }

    private void showNotification(String message) {
        notificationText.setText(message);
        notificationContainer.setVisibility(View.VISIBLE);
        notificationContainer.bringToFront();
    }

    private void hideNotification() {
        notificationContainer.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isPaused) {
            togglePause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up timer
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    // Handle system back button press

}