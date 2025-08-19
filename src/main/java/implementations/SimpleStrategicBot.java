package implementations;

import interfaces.Connect4Bot;

/**
 * A simple Connect 4 bot that makes basic strategic moves.
 * This bot is smarter than RandomBot but still uses simple heuristics:
 * 1. Win immediately if possible
 * 2. Block opponent from winning
 * 3. Prefer center columns
 * 4. Avoid giving opponent winning opportunities
 */
public class SimpleStrategicBot implements Connect4Bot {

    private static final int ROWS = 6;
    private static final int COLS = 7;
    private static final int CONNECT_LENGTH = 4;

    @Override
    public int play(int[][] board, int playerNumber) {
        int opponentNumber = (playerNumber == 1) ? 2 : 1;

        // 1. Check if we can win immediately
        for (int col = 0; col < COLS; col++) {
            if (isValidMove(board, col)) {
                if (wouldWin(board, col, playerNumber)) {
                    return col;
                }
            }
        }

        // 2. Check if we need to block opponent from winning
        for (int col = 0; col < COLS; col++) {
            if (isValidMove(board, col)) {
                if (wouldWin(board, col, opponentNumber)) {
                    return col;
                }
            }
        }

        // 3. Prefer center columns (they offer more winning opportunities)
        int[] preferredColumns = {3, 2, 4, 1, 5, 0, 6}; // Center-out preference
        for (int col : preferredColumns) {
            if (isValidMove(board, col)) {
                // 4. Avoid moves that give opponent immediate win opportunity
                if (!givesOpponentWin(board, col, playerNumber, opponentNumber)) {
                    return col;
                }
            }
        }

        // 5. If all moves give opponent a win, just pick the first valid move
        for (int col = 0; col < COLS; col++) {
            if (isValidMove(board, col)) {
                return col;
            }
        }

        // Should never reach here in a normal game
        throw new IllegalArgumentException("No valid moves available");
    }

    /**
     * Checks if dropping a piece in the given column would result in a win.
     */
    private boolean wouldWin(int[][] board, int col, int player) {
        // Find the row where the piece would land
        int row = getDropRow(board, col);
        if (row == -1) return false; // Column is full

        // Temporarily place the piece
        board[row][col] = player;

        // Check if this creates a winning condition
        boolean wins = checkWin(board, row, col, player);

        // Remove the temporary piece
        board[row][col] = 0;

        return wins;
    }

    /**
     * Checks if making a move would give the opponent an immediate winning opportunity.
     */
    private boolean givesOpponentWin(int[][] board, int col, int playerNumber, int opponentNumber) {
        // Find the row where our piece would land
        int row = getDropRow(board, col);
        if (row == -1) return false; // Column is full

        // Temporarily place our piece
        board[row][col] = playerNumber;

        // Check if opponent can win in any column after our move
        boolean opponentCanWin = false;
        for (int checkCol = 0; checkCol < COLS; checkCol++) {
            if (isValidMove(board, checkCol)) {
                if (wouldWin(board, checkCol, opponentNumber)) {
                    opponentCanWin = true;
                    break;
                }
            }
        }

        // Remove our temporary piece
        board[row][col] = 0;

        return opponentCanWin;
    }

    /**
     * Gets the row where a piece would land if dropped in the given column.
     */
    private int getDropRow(int[][] board, int col) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][col] == 0) {
                return row;
            }
        }
        return -1; // Column is full
    }

    /**
     * Checks if a move is valid (column exists and isn't full).
     */
    private boolean isValidMove(int[][] board, int col) {
        return col >= 0 && col < COLS && board[0][col] == 0;
    }

    /**
     * Checks if the last move resulted in a win.
     */
    private boolean checkWin(int[][] board, int row, int col, int player) {
        // Check horizontal
        if (checkDirection(board, row, col, player, 0, 1) || // Right
            checkDirection(board, row, col, player, 0, -1)) { // Left
            return true;
        }

        // Check vertical
        if (checkDirection(board, row, col, player, 1, 0) || // Down
            checkDirection(board, row, col, player, -1, 0)) { // Up
            return true;
        }

        // Check diagonal /
        if (checkDirection(board, row, col, player, 1, 1) || // Down-right
            checkDirection(board, row, col, player, -1, -1)) { // Up-left
            return true;
        }

        // Check diagonal \
        if (checkDirection(board, row, col, player, 1, -1) || // Down-left
            checkDirection(board, row, col, player, -1, 1)) { // Up-right
            return true;
        }

        return false;
    }

    /**
     * Checks for 4 in a row in a specific direction from a given position.
     */
    private boolean checkDirection(int[][] board, int row, int col, int player, int deltaRow, int deltaCol) {
        int count = 1; // Count the piece we just placed

        // Check in positive direction
        int r = row + deltaRow;
        int c = col + deltaCol;
        while (r >= 0 && r < ROWS && c >= 0 && c < COLS && board[r][c] == player) {
            count++;
            r += deltaRow;
            c += deltaCol;
        }

        // Check in negative direction
        r = row - deltaRow;
        c = col - deltaCol;
        while (r >= 0 && r < ROWS && c >= 0 && c < COLS && board[r][c] == player) {
            count++;
            r -= deltaRow;
            c -= deltaCol;
        }

        return count >= CONNECT_LENGTH;
    }

    @Override
    public String getBotName() {
        return "SimpleStrategicBot";
    }
}
