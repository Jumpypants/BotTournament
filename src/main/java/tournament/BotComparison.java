package tournament;

import interfaces.Connect4Bot;
import implementations.RandomConnect4Bot;
import implementations.SimpleStrategicBot;

/**
 * Test program to demonstrate the difference between RandomBot and SimpleStrategicBot
 * by running multiple games between them.
 */
public class BotComparison {

    public static void main(String[] args) {
        Connect4Bot bot1 = new RandomConnect4Bot();
        Connect4Bot bot2 = new SimpleStrategicBot();

        System.out.println("Bot Comparison:");
        System.out.println("=" + "=".repeat(50));

        // Run a single game to see the gameplay
        System.out.println("\n🎮 Sample Game:");
        Connect4Game sampleGame = new Connect4Game(bot2, bot1);
        Connect4Game.GameResult sampleResult = sampleGame.playGame();
        System.out.println(sampleResult);

        // Run multiple games to see win statistics
        System.out.println("\n📊 Running 1000 games for statistics...");
        runMultipleGames(bot2, bot1, 1000);
    }

    private static void runMultipleGames(Connect4Bot bot1, Connect4Bot bot2, int numGames) {
        int bot1Wins = 0;
        int bot2Wins = 0;
        int draws = 0;
        int totalMoves = 0;

        for (int i = 0; i < numGames; i++) {
            // Alternate who goes first
            Connect4Game game = (i % 2 == 0) ?
                new Connect4Game(bot1, bot2) :
                new Connect4Game(bot2, bot1);

            Connect4Game.GameResult result = game.playGame();
            totalMoves += result.getTotalMoves();

            // Determine winner based on who was player 1 in this game
            if (result.getWinner() == 0) {
                draws++;
            } else if (i % 2 == 0) {
                // bot1 was player 1
                if (result.getWinner() == 1) bot1Wins++;
                else bot2Wins++;
            } else {
                // bot2 was player 1
                if (result.getWinner() == 1) bot2Wins++;
                else bot1Wins++;
            }

            System.out.printf("Game %d: %s%n", i + 1, getGameSummary(result, i % 2 == 0));
        }

        // Print statistics
        System.out.println("\n📈 Final Statistics:");
        System.out.println("-".repeat(30));
        System.out.printf("%-20s: %d wins (%.1f%%)%n",
            bot1.getBotName(), bot1Wins, (bot1Wins * 100.0) / numGames);
        System.out.printf("%-20s: %d wins (%.1f%%)%n",
            bot2.getBotName(), bot2Wins, (bot2Wins * 100.0) / numGames);
        System.out.printf("%-20s: %d (%.1f%%)%n",
            "Draws", draws, (draws * 100.0) / numGames);
        System.out.printf("Average moves per game: %.1f%n", (totalMoves * 1.0) / numGames);
    }

    private static String getGameSummary(Connect4Game.GameResult result, boolean bot1WasPlayer1) {
        if (result.getWinner() == 0) {
            return "Draw after " + result.getTotalMoves() + " moves";
        }

        String winner;
        if (bot1WasPlayer1) {
            winner = (result.getWinner() == 1) ? result.getPlayer1Name() : result.getPlayer2Name();
        } else {
            winner = (result.getWinner() == 1) ? result.getPlayer2Name() : result.getPlayer1Name();
        }

        return winner + " wins in " + result.getTotalMoves() + " moves";
    }
}
