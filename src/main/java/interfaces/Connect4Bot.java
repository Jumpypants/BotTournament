package interfaces;

/**
 * Interface for Connect 4 playing bots.
 * All bots participating in the Connect 4 tournament must implement this interface.
 */
public interface Connect4Bot {

    /**
     * Makes a move on the Connect 4 board.
     *
     * @param board The current state of the Connect 4 board.
     *              The board is represented as a 2D array where:
     *              - board[row][col] contains the piece at that position
     *              - 0 = empty space
     *              - 1 = player 1's piece (your piece)
     *              - 2 = player 2's piece (opponent's piece)
     *              - Row 0 is the top of the board, increasing downward
     *              - Column 0 is the leftmost column
     * @param playerNumber Your player number (1 or 2)
     * @return The column number (0-based) where you want to drop your piece.
     *         Must be a valid column (0 <= column < board[0].length)
     *         and the column must not be full.
     * @throws IllegalArgumentException if the returned move is invalid
     */
    int play(int[][] board, int playerNumber);

    /**
     * Gets the name of this bot for display purposes.
     *
     * @return A string identifying this bot (e.g., "MinimaxBot", "RandomBot")
     */
    String getBotName();
}
