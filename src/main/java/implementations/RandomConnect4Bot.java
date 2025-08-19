package implementations;

import interfaces.Connect4Bot;

/**
 * A simple Connect 4 bot that makes random valid moves.
 * This serves as an example implementation of the Connect4Bot interface.
 */
public class RandomConnect4Bot implements Connect4Bot {

    private final java.util.Random random = new java.util.Random();

    @Override
    public int play(int[][] board, int playerNumber) {
        // Find all valid columns (not full)
        java.util.List<Integer> validMoves = new java.util.ArrayList<>();

        for (int col = 0; col < board[0].length; col++) {
            // A column is valid if the top row is empty
            if (board[0][col] == 0) {
                validMoves.add(col);
            }
        }

        // If no valid moves, throw exception (shouldn't happen in normal game)
        if (validMoves.isEmpty()) {
            throw new IllegalArgumentException("No valid moves available");
        }

        // Return a random valid column
        return validMoves.get(random.nextInt(validMoves.size()));
    }

    @Override
    public String getBotName() {
        return "RandomBot";
    }
}
