package implementations;

import interfaces.Connect4Bot;

/**
 * A Connect 4 bot with a defensive mindset.
 * This bot prioritizes blocking the opponent's winning moves
 * before attempting to create its own winning lines.
 *
 * Strategy:
 * 1. Win immediately if possible (still the best move).
 * 2. Block opponent's immediate winning move (primary defensive strategy).
 * 3. Avoid setting up the opponent for a win on the next turn.
 * 4. Play in the center column if it's a safe move.
 * 5. Play a random valid move as a last resort.
 */
public class DefensiveBot implements Connect4Bot {

    private static final int ROWS = 6;
    private static final int COLS = 7;
    private java.util.Random random = new java.util.Random();

    @Override
    public String getBotName() {
        return "DefensiveBot";
    }

    @Override
    public int play(int[][] board, int playerNumber) {
        int opponentNumber = (playerNumber == 1) ? 2 : 1;

        // 1. Check for an immediate winning move
        int winningMove = findWinningMove(board, playerNumber);
        if (winningMove != -1) {
            return winningMove;
        }

        // 2. Block the opponent's winning move
        int blockingMove = findWinningMove(board, opponentNumber);
        if (blockingMove != -1) {
            return blockingMove;
        }

        // 3. Find a move that doesn't give the opponent a win on the next turn
        java.util.List<Integer> safeMoves = new java.util.ArrayList<>();
        for (int col = 0; col < COLS; col++) {
            if (isValidMove(board, col) && !isOpponentWinningNext(board, col, playerNumber)) {
                safeMoves.add(col);
            }
        }

        if (!safeMoves.isEmpty()) {
            // Prefer center columns among safe moves
            int[] preferredColumns = {3, 4, 2, 5, 1, 6, 0};
            for (int col : preferredColumns) {
                if (safeMoves.contains(col)) {
                    return col;
                }
            }
            // If no preferred columns are safe, return the first available safe move
            return safeMoves.get(0);
        }

        // 4. As a last resort, make any valid move (even if it leads to a loss)
        java.util.List<Integer> anyValidMoves = new java.util.ArrayList<>();
        for (int col = 0; col < COLS; col++) {
            if (isValidMove(board, col)) {
                anyValidMoves.add(col);
            }
        }

        return anyValidMoves.isEmpty() ? 0 : anyValidMoves.get(random.nextInt(anyValidMoves.size()));
    }

    private int findWinningMove(int[][] board, int player) {
        for (int col = 0; col < COLS; col++) {
            if (isValidMove(board, col)) {
                int row = getNextEmptyRow(board, col);
                board[row][col] = player; // Temporarily make the move
                if (checkWin(board, row, col, player)) {
                    board[row][col] = 0; // Revert the move
                    return col;
                }
                board[row][col] = 0; // Revert the move
            }
        }
        return -1;
    }

    private boolean isOpponentWinningNext(int[][] board, int col, int player) {
        int opponent = (player == 1) ? 2 : 1;
        int row = getNextEmptyRow(board, col);
        board[row][col] = player; // Make our move

        // Check if the opponent has a winning move right after
        int opponentWinningMove = findWinningMove(board, opponent);

        board[row][col] = 0; // Revert our move
        return opponentWinningMove != -1;
    }

    private boolean isValidMove(int[][] board, int col) {
        return board[0][col] == 0;
    }

    private int getNextEmptyRow(int[][] board, int col) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][col] == 0) {
                return row;
            }
        }
        return -1;
    }

    private boolean checkWin(int[][] board, int r, int c, int player) {
        // Horizontal, Vertical, and Diagonal checks
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        for (int[] dir : directions) {
            int count = 1;
            // Check in one direction
            for (int i = 1; i < 4; i++) {
                int newR = r + dir[0] * i;
                int newC = c + dir[1] * i;
                if (newR >= 0 && newR < ROWS && newC >= 0 && newC < COLS && board[newR][newC] == player) {
                    count++;
                } else {
                    break;
                }
            }
            // Check in the opposite direction
            for (int i = 1; i < 4; i++) {
                int newR = r - dir[0] * i;
                int newC = c - dir[1] * i;
                if (newR >= 0 && newR < ROWS && newC >= 0 && newC < COLS && board[newR][newC] == player) {
                    count++;
                } else {
                    break;
                }
            }
            if (count >= 4) {
                return true;
            }
        }
        return false;
    }
}
