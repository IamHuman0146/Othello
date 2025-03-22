package com.example.othellodarren;

public class OthelloGame {
    private static final int BOARD_SIZE = 8;
    private char[][] board;
    private char player1 = 'X', player2 = 'O';
    private char currentPlayer;
    private int player1Tiles, player2Tiles;

    public OthelloGame() {
        board = new char[BOARD_SIZE][BOARD_SIZE];
        resetBoard();
        currentPlayer = player1;
        countTiles();
    }

    //greedy
    public int[] getBestAIMove() {
        int bestRow = -1, bestCol = -1;
        int maxFlips = 0;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (isValidMove(row, col)) {
                    int flips = calculateGained(row, col, currentPlayer, (currentPlayer == player1) ? player2 : player1);
                    if (flips > maxFlips) {
                        maxFlips = flips;
                        bestRow = row;
                        bestCol = col;
                    }
                }
            }
        }

        return new int[] {bestRow, bestCol};
    }
    public void resetBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                board[row][col] = ' ';
            }
        }
        board[3][3] = player1;
        board[4][4] = player1;
        board[3][4] = player2;
        board[4][3] = player2;
    }

    public char[][] getBoard() {
        return board;
    }

    public char getCurrentPlayer() {
        return currentPlayer;
    }

    public int getPlayer1Tiles() {
        return player1Tiles;
    }

    public int getPlayer2Tiles() {
        return player2Tiles;
    }

    public boolean isValidMove(int row, int col) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            return false;
        }

        if (board[row][col] != ' ') return false;

        char opponent = (currentPlayer == player1) ? player2 : player1;
        return calculateGained(row, col, currentPlayer, opponent) > 0;
    }

    public boolean[][] getValidMoves() {
        boolean[][] validMoves = new boolean[BOARD_SIZE][BOARD_SIZE];
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                validMoves[row][col] = isValidMove(row, col);
            }
        }
        return validMoves;
    }

    public boolean hasValidMoves() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (isValidMove(row, col)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean makeMove(int row, int col) {
        if (!isValidMove(row, col)) return false;

        char opponent = (currentPlayer == player1) ? player2 : player1;
        flipPieces(row, col, currentPlayer, opponent);
        board[row][col] = currentPlayer;

        switchTurn();
        return true;
    }

    public void switchTurn() {

        currentPlayer = (currentPlayer == player1) ? player2 : player1;


        if (!hasValidMoves()) {
            currentPlayer = (currentPlayer == player1) ? player2 : player1;


            if (!hasValidMoves()) {
                currentPlayer = ' ';
            }
        }

        countTiles();
    }



    private int calculateGained(int row, int col, char turn, char oppose) {
        int[] directions = {-1, 0, 1};
        int totalFlipped = 0;

        for (int dr : directions) {
            for (int dc : directions) {
                if (dr == 0 && dc == 0) continue;
                int r = row + dr, c = col + dc, count = 0;

                while (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && board[r][c] == oppose) {
                    count++;
                    r += dr;
                    c += dc;
                }

                if (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && board[r][c] == turn) {
                    totalFlipped += count;
                }
            }
        }
        return totalFlipped;
    }

    private void flipPieces(int row, int col, char turn, char oppose) {
        int[] directions = {-1, 0, 1};

        for (int dr : directions) {
            for (int dc : directions) {
                if (dr == 0 && dc == 0) continue;
                int r = row + dr, c = col + dc, count = 0;

                while (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && board[r][c] == oppose) {
                    count++;
                    r += dr;
                    c += dc;
                }

                if (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && board[r][c] == turn) {
                    r = row + dr;
                    c = col + dc;
                    for (int i = 0; i < count; i++) {
                        board[r][c] = turn;
                        r += dr;
                        c += dc;
                    }
                }
            }
        }
    }

    private void countTiles() {
        player1Tiles = 0;
        player2Tiles = 0;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] == player1) {
                    player1Tiles++;
                } else if (board[row][col] == player2) {
                    player2Tiles++;
                }
            }
        }
    }
}
