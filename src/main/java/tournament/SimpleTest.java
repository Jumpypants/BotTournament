package tournament;

/**
 * Minimal test to check basic Java execution and bot loading.
 */
public class SimpleTest {
    public static void main(String[] args) {
        System.out.println("Starting simple test...");

        try {
            // Test basic bot instantiation without using BotDiscovery
            System.out.println("Testing direct bot instantiation...");

            interfaces.Connect4Bot randomBot = new implementations.RandomConnect4Bot();
            System.out.println("RandomBot created: " + randomBot.getBotName());

            interfaces.Connect4Bot simpleBot = new implementations.SimpleStrategicBot();
            System.out.println("SimpleBot created: " + simpleBot.getBotName());

            interfaces.Connect4Bot defensiveBot = new implementations.DefensiveBot();
            System.out.println("DefensiveBot created: " + defensiveBot.getBotName());

            interfaces.Connect4Bot aggressiveBot = new implementations.AggressiveBot();
            System.out.println("AggressiveBot created: " + aggressiveBot.getBotName());

            System.out.println("All bots loaded successfully!");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
