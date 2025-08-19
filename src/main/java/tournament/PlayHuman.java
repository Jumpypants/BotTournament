package tournament;

import excludedImplementations.HumanPlayer;
import interfaces.Connect4Bot;
import java.util.*;

/**
 * Simple application that allows a human player to play Connect 4 against discovered bots.
 */
public class PlayHuman {

    private final Scanner scanner;

    public PlayHuman() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Main game loop - discovers bots and lets user play against them
     */
    public void start() {
        System.out.println("Welcome to Connect 4!");

        // Discover available bots
        List<Connect4Bot> availableBots = BotDiscovery.discoverAllBots();

        if (availableBots.isEmpty()) {
            System.out.println("No bots found! Cannot start game.");
            return;
        }

        while (true) {
            System.out.println("\nAvailable opponents:");
            for (int i = 0; i < availableBots.size(); i++) {
                System.out.println((i + 1) + ". " + availableBots.get(i).getBotName());
            }

            System.out.print("Choose your opponent: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());

                if (choice < 1 || choice > availableBots.size()) {
                    System.out.println("Invalid choice!");
                    continue;
                }

                Connect4Bot selectedBot = availableBots.get(choice - 1);
                playGame(selectedBot);

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }
        }
    }

    /**
     * Play a single game against the selected bot
     */
    private void playGame(Connect4Bot bot) {
        System.out.println("Do you want to go first? (y/n): ");
        boolean humanFirst = scanner.nextLine().trim().toLowerCase().startsWith("y");

        HumanPlayer human = new HumanPlayer("Human");
        Connect4Game game;

        if (humanFirst) {
            game = new Connect4Game(human, bot);
        } else {
            game = new Connect4Game(bot, human);
        }

        Connect4Game.GameResult result = game.playGame();
        System.out.println("\nGame Over! " + result);
    }

    /**
     * Main method to start the application
     */
    public static void main(String[] args) {
        PlayHuman game = new PlayHuman();
        game.start();
    }
}
