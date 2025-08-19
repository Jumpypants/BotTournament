package excludedImplementations;

import interfaces.Connect4Bot;
import java.util.Scanner;

/**
 * A Connect4Bot implementation that gets moves from human input via console.
 * This allows a human player to participate in games against bots.
 */
public class HumanPlayer implements Connect4Bot {

    private final Scanner scanner;
    private final String playerName;

    public HumanPlayer(String playerName) {
        this.scanner = new Scanner(System.in);
        this.playerName = playerName;
    }

    @Override
    public int play(int[][] board, int playerNumber) {
        System.out.println("\n" + playerName + "'s turn (Player " + playerNumber + ")");
        System.out.print("Enter column (0-6): ");

        while (true) {
            try {
                String input = scanner.nextLine().trim();
                int column = Integer.parseInt(input);

                // Validate the move
                if (column < 0 || column >= board[0].length) {
                    System.out.print("Invalid column! Please enter a number between 0 and " +
                                   (board[0].length - 1) + ": ");
                    continue;
                }

                // Check if column is full
                if (board[0][column] != 0) {
                    System.out.print("Column " + column + " is full! Choose another column: ");
                    continue;
                }

                return column;

            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    @Override
    public String getBotName() {
        return playerName;
    }
}
