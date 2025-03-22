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
import android.content.Intent;


public class OthelloGameActivity extends AppCompatActivity {
    private boolean isAIMode = false;
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

    // Added instance variables for player and AI colors
    private char playerColor;
    private char aiColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtils.applyTheme(this);
        setContentView(R.layout.activity_othello_game);

        // Get player color from intent and set AI color
        isAIMode = getIntent().hasExtra("playerColor");
        playerColor = getIntent().getCharExtra("playerColor", 'X');
        aiColor = (playerColor == 'X') ? 'O' : 'X';

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

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.board_background, typedValue, true);
        int boardBackgroundColor = typedValue.data;

        View rootView = findViewById(android.R.id.content);
        rootView.setBackgroundColor(boardBackgroundColor);
        boardGrid.setBackgroundColor(boardBackgroundColor);

        pauseButton.setOnClickListener(v -> togglePause());
        findViewById(R.id.rematchButton).setOnClickListener(v -> restartGame());
        findViewById(R.id.backButton).setOnClickListener(v -> {
            Intent intent = new Intent(OthelloGameActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        backToMenuButton.setOnClickListener(v -> showExitConfirmationDialog());

        hintSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showHints = isChecked;
            updateBoard();
        });

        game = new OthelloGame();
        setupBoard();
        updateBoard();
        updatePlayerInfo();
        startGameTimer();

        updatePanelLabels();
        if (isAIMode && game.getCurrentPlayer() == aiColor) {
            new Handler().postDelayed(this::makeAIMove, 1000);
        }
    }



    private void makeAIMove() {
        if (game.getCurrentPlayer() == aiColor && !isPaused) {
            int[] bestMove = game.getBestAIMove();
            if (bestMove != null) {
                game.makeMove(bestMove[0], bestMove[1]);
                updateBoard();
                updatePlayerInfo();
                resetTimer();
            } else {

                showNotification("AI has no valid moves. Turn skipped.");
                new Handler().postDelayed(() -> {
                    skipTurn();
                    hideNotification();
                }, 2000);
            }
        }
    }

    private void handlePlayerMove(int row, int col) {
        if (!isPaused &&

                (!isAIMode || game.getCurrentPlayer() == playerColor) &&
                game.makeMove(row, col)) {

            updateBoard();
            updatePlayerInfo();
            resetTimer();

            if (isAIMode && game.getCurrentPlayer() != ' ') {
                new Handler().postDelayed(this::makeAIMove, 500);
            }
        }
    }



    private void updatePanelLabels() {
        // Get references to the TextViews in the panels
        TextView player1Label = findViewById(R.id.player1Label);
        TextView player2Label = findViewById(R.id.player2Label);

        if (isAIMode) {
            // In AI mode, set labels based on player color
            if (playerColor == 'X') {
                player1Label.setText("PLAYER");
                player2Label.setText("AI");
            } else {
                player1Label.setText("AI");
                player2Label.setText("PLAYER");
            }
        } else {
            // In 1v1 mode, use default labels
            player1Label.setText("PLAYER 1");
            player2Label.setText("PLAYER 2");
        }
    }


    private void showExitConfirmationDialog() {
        boolean wasPaused = isPaused;
        if (!isPaused) {
            togglePause();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit Game");
        builder.setMessage("Are you sure you want to exit? Your game progress will be lost.");

        builder.setPositiveButton("Exit", (dialog, which) -> {
            // Always go back to MainActivity, not the previous activity
            Intent intent = new Intent(OthelloGameActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            if (!wasPaused) {
                togglePause();
            }
        });

        builder.create().show();
    }
    private void setupBoard() {
        int screenSize = Math.min(getResources().getDisplayMetrics().widthPixels,
                getResources().getDisplayMetrics().heightPixels) - 100;
        screenSize = (int)(screenSize * 0.95);
        int cellSize = screenSize / BOARD_SIZE;

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
                    // Use handlePlayerMove instead of direct game.makeMove
                    handlePlayerMove(finalRow, finalCol);
                });

                boardGrid.addView(cell);
                boardCells[row][col] = cell;
            }
        }
    }

    private void updateBoard() {
        char[][] board = game.getBoard();
        boolean[][] validMoves = game.getValidMoves();

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
                    boardCells[row][col].setColorFilter(blackTileColor);
                } else if (board[row][col] == 'O') {
                    boardCells[row][col].setImageResource(R.drawable.circle_white);
                    boardCells[row][col].setColorFilter(whiteTileColor);
                } else if (showHints && validMoves[row][col] &&
                        (!isAIMode || game.getCurrentPlayer() == playerColor)) {
                    // Show hints for both players in 1v1 mode, or only for player in AI mode
                    boardCells[row][col].setImageResource(R.drawable.circle_hint);
                } else {
                    boardCells[row][col].setImageResource(R.drawable.circle_empty);
                }
                boardCells[row][col].setBackgroundColor(borderColor);
            }
        }
    }

    private void updatePlayerInfo() {
        player1Tiles.setText(getString(R.string.player_tiles, game.getPlayer1Tiles()));
        player2Tiles.setText(getString(R.string.player_tiles, game.getPlayer2Tiles()));

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.panel_background, typedValue, true);
        int panelColor = typedValue.data;

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


            if (isAIMode && game.getCurrentPlayer() == aiColor) {
                new Handler().postDelayed(() -> makeAIMove(), 500);
            }
        }
    }

    private void showGameOverScreen() {
        String winnerMessage;
        int blackTiles = game.getPlayer1Tiles();
        int whiteTiles = game.getPlayer2Tiles();

        if (isAIMode) {
            // AI mode win message
            if (blackTiles > whiteTiles) {
                if (playerColor == 'X') {
                    winnerMessage = "🏆 You Win! 🏆";
                } else {
                    winnerMessage = "🏆 AI Wins! 🏆";
                }
            } else if (whiteTiles > blackTiles) {
                if (playerColor == 'O') {
                    winnerMessage = "🏆 You Win! 🏆";
                } else {
                    winnerMessage = "🏆 AI Wins! 🏆";
                }
            } else {
                winnerMessage = "🤝 It's a Tie!";
            }
        } else {
            // 1v1 mode win message
            if (blackTiles > whiteTiles) {
                winnerMessage = "🏆 Player 1 Wins! 🏆";
            } else if (whiteTiles > blackTiles) {
                winnerMessage = "🏆 Player 2 Wins! 🏆";
            } else {
                winnerMessage = "🤝 It's a Tie!";
            }
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


                    if (secondsRemaining <= 10) {
                        gameTimer.setTextColor(ContextCompat.getColor(OthelloGameActivity.this, android.R.color.holo_red_light));
                    } else {
                        gameTimer.setTextColor(ContextCompat.getColor(OthelloGameActivity.this, android.R.color.white));
                    }


                    if (secondsRemaining <= 0) {
                        showNotification("Time's up! Turn skipped.");


                        new Handler().postDelayed(() -> {
                            // Skip the current turn
                            game.switchTurn();


                            if (game.getCurrentPlayer() == ' ') {
                                showGameOverScreen();
                            } else {

                                updateBoard();
                                updatePlayerInfo();
                                resetTimer();


                                if (isAIMode && game.getCurrentPlayer() == aiColor) {
                                    new Handler().postDelayed(() -> makeAIMove(), 500);
                                }


                                if (!game.hasValidMoves()) {
                                    showNotification("No valid moves available. Turn skipped.");
                                    new Handler().postDelayed(() -> {
                                        skipTurn();
                                        hideNotification();
                                    }, 2000);
                                }
                            }


                            hideNotification();
                        }, 2000);


                        return;
                    }
                }


                if (game.getCurrentPlayer() != ' ') {
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };

        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void resetTimer() {
        secondsRemaining = 30;
        gameTimer.setText("00:30");
        gameTimer.setTextColor(ContextCompat.getColor(this, android.R.color.white));


        if (game.getCurrentPlayer() != ' ' && !game.hasValidMoves()) {
            showNotification("No valid moves available. Turn skipped.");
            new Handler().postDelayed(() -> {
                skipTurn();
                hideNotification();
            }, 2000);
        } else {

            if (timerRunnable != null) {
                timerHandler.removeCallbacks(timerRunnable);
                timerHandler.postDelayed(timerRunnable, 1000);
            }
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

        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
}