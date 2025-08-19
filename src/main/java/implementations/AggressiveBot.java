package implementations;

import interfaces.Connect4Bot;

/**
 * An improved aggressive Connect 4 bot that balances offensive play with performance.
 * This bot focuses on creating threats while maintaining good defensive awareness.
 *
 * Strategy:
 * 1. Win immediately if possible
 * 2. Block opponent's immediate win
 * 3. Create winning threats (simplified approach)
 * 4. Build strong positions in center columns
 * 5. Avoid giving opponent easy wins
 */
public class AggressiveBot implements Connect4Bot {

    private static final int ROWS = 6;
    private static final int COLS = 7;
    private static final int CONNECT_LENGTH = 4;

    @Override
    public String getBotName() {
        return "AggressiveBot";
    }

    @Override
    public int play(int[][] board, int playerNumber) {
        int opponentNumber = (playerNumber == 1) ? 2 : 1;

        // 1. Win immediately if possible
        int winningMove = findWinningMove(board, playerNumber);
        if (winningMove != -1) {
            return winningMove;
        }

        // 2. Block opponent's immediate winning move
        int blockingMove = findWinningMove(board, opponentNumber);
        if (blockingMove != -1) {
            return blockingMove;
        }

        // 3. Look for aggressive opportunities (simplified)
        int aggressiveMove = findAggressiveMove(board, playerNumber, opponentNumber);
        if (aggressiveMove != -1) {
            return aggressiveMove;
        }

        // 4. Prefer center columns for better positioning
        int[] centerPreference = {3, 2, 4, 1, 5, 0, 6};
        for (int col : centerPreference) {
            if (isValidMove(board, col) && !givesOpponentWin(board, col, opponentNumber)) {
                return col;
            }
        }

        // 5. Fall back to any safe move
        for (int col = 0; col < COLS; col++) {
            if (isValidMove(board, col) && !givesOpponentWin(board, col, opponentNumber)) {
                return col;
            }
        }

        // 6. Last resort - any valid move
        return findAnyValidMove(board);
    }

    /**
     * Find a move that creates an immediate win.
     */
    private int findWinningMove(int[][] board, int player) {
        for (int col = 0; col < COLS; col++) {
            if (isValidMove(board, col)) {
                int[][] tempBoard = copyBoard(board);
                int row = makeMove(tempBoard, col, player);
                if (row != -1 && checkWinFromPosition(tempBoard, row, col, player)) {
                    return col;
                }
            }
        }
        return -1;
    }

    /**
     * Find an aggressive move that creates threats or builds sequences.
     */
    private int findAggressiveMove(int[][] board, int player, int opponent) {
        int bestMove = -1;
        int bestScore = -1;

        for (int col = 0; col < COLS; col++) {
            if (isValidMove(board, col)) {
                // Don't make moves that immediately give opponent a win
                if (givesOpponentWin(board, col, opponent)) {
                    continue;
                }

                int score = evaluateAggressiveMove(board, col, player);
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = col;
                }
            }
        }

        // Only return if we found a reasonably good aggressive move
        return bestScore >= 3 ? bestMove : -1;
    }

    /**
     * Evaluate how aggressive/threatening a move is.
     */
    private int evaluateAggressiveMove(int[][] board, int col, int player) {
        int[][] tempBoard = copyBoard(board);
        int row = makeMove(tempBoard, col, player);
        if (row == -1) return 0;

        int score = 0;

        // Check all four directions for sequence building
        int[][] directions = {{0,1}, {1,0}, {1,1}, {1,-1}}; // horizontal, vertical, diagonal

        for (int[] dir : directions) {
            int sequenceLength = getSequenceLength(tempBoard, row, col, dir[0], dir[1], player);

            // Score based on sequence length (aggressive preference for longer sequences)
            if (sequenceLength >= 3) {
                score += 10; // Very good - close to winning
            } else if (sequenceLength >= 2) {
                score += 3;  // Good - building threat
            }

            // Bonus for sequences that could be extended
            if (canExtendSequence(tempBoard, row, col, dir[0], dir[1], player)) {
                score += 2;
            }
        }

        // Bonus for center column control
        if (col >= 2 && col <= 4) {
            score += 1;
        }

        return score;
    }

    /**
     * Check if a sequence can potentially be extended.
     */
    private boolean canExtendSequence(int[][] board, int row, int col, int deltaRow, int deltaCol, int player) {
        // Check if there are empty spaces that could extend this sequence
        int[] checkPositions = {1, -1}; // Check both directions

        for (int direction : checkPositions) {
            int r = row + (direction * deltaRow);
            int c = col + (direction * deltaCol);

            // Look ahead up to 3 positions
            for (int i = 0; i < 3; i++) {
                if (r >= 0 && r < ROWS && c >= 0 && c < COLS && board[r][c] == 0) {
                    return true; // Found an empty space where we could extend
                }
                r += (direction * deltaRow);
                c += (direction * deltaCol);
            }
        }
        return false;
    }

    /**
     * Get the length of a sequence in a specific direction.
     */
    private int getSequenceLength(int[][] board, int row, int col, int deltaRow, int deltaCol, int player) {
        int count = 1; // Count the current piece

        // Check positive direction
        int r = row + deltaRow;
        int c = col + deltaCol;
        while (r >= 0 && r < ROWS && c >= 0 && c < COLS && board[r][c] == player) {
            count++;
            r += deltaRow;
            c += deltaCol;
        }

        // Check negative direction
        r = row - deltaRow;
        c = col - deltaCol;
        while (r >= 0 && r < ROWS && c >= 0 && c < COLS && board[r][c] == player) {
            count++;
            r -= deltaRow;
            c -= deltaCol;
        }

        return count;
    }

    /**
     * Check if making a move would give the opponent an immediate winning opportunity.
     */
    private boolean givesOpponentWin(int[][] board, int col, int opponent) {
        if (!isValidMove(board, col)) return false;

        int[][] tempBoard = copyBoard(board);
        int row = makeMove(tempBoard, col, 1); // Use dummy player
        if (row == -1) return false;

        // Check if opponent could win by playing on top of our move
        if (row > 0) {
            tempBoard[row - 1][col] = opponent;
            return checkWinFromPosition(tempBoard, row - 1, col, opponent);
        }

        return false;
    }

    /**
     * Check if there's a win from a specific position.
     */
    private boolean checkWinFromPosition(int[][] board, int row, int col, int player) {
        int[][] directions = {{0,1}, {1,0}, {1,1}, {1,-1}};

        for (int[] dir : directions) {
            if (getSequenceLength(board, row, col, dir[0], dir[1], player) >= CONNECT_LENGTH) {
                return true;
            }
        }
        return false;
    }

    /**
     * Make a move on the board and return the row where piece landed.
     */
    private int makeMove(int[][] board, int col, int player) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][col] == 0) {
                board[row][col] = player;
                return row;
            }
        }
        return -1; // Column is full
    }

    /**
     * Check if a move is valid (column not full).
     */
    private boolean isValidMove(int[][] board, int col) {
        return col >= 0 && col < COLS && board[0][col] == 0;
    }

    /**
     * Create a deep copy of the board.
     */
    private int[][] copyBoard(int[][] board) {
        int[][] copy = new int[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, COLS);
        }
        return copy;
    }

    /**
     * Find any valid move as a fallback.
     */
    private int findAnyValidMove(int[][] board) {
        for (int col = 0; col < COLS; col++) {
            if (isValidMove(board, col)) {
                return col;
            }
        }
        return 3; // Default to center if all else fails
    }
}
