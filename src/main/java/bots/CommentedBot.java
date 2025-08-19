package bots;

/**
 * This is a heavily commented bot to test comment filtering
 * It has lots of documentation and inline comments
 * @author Test
 * @version 1.0
 */
public class CommentedBot {

    // This field stores the bot's name
    private String name;

    /* Multi-line comment
       explaining the constructor
       with detailed information */
    public CommentedBot(String name) {
        this.name = name; // Set the name
    }

    /**
     * Makes a move in the game
     * This method contains the main logic
     * @return true if successful
     */
    public boolean makeMove() {
        // Check if we should move
        if (name != null) {
            // Print status
            System.out.println("Moving: " + name);
            return true; // Success
        }
        return false; // Failed
    }

    // Simple getter method
    public String getName() {
        return name; // Return the name
    }
}
