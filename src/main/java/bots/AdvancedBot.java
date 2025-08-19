package bots;

/**
 * A more complex bot example with additional methods and logic
 */
public class AdvancedBot {
    private String name;
    private int score;

    public AdvancedBot(String name) {
        this.name = name;
        this.score = 0;
    }

    public void makeMove() {
        // Complex decision making logic
        if (shouldAttack()) {
            attack();
        } else if (shouldDefend()) {
            defend();
        } else {
            explore();
        }
        updateScore();
    }

    private boolean shouldAttack() {
        return Math.random() > 0.7;
    }

    private boolean shouldDefend() {
        return Math.random() > 0.5;
    }

    private void attack() {
        System.out.println(name + " is attacking!");
    }

    private void defend() {
        System.out.println(name + " is defending!");
    }

    private void explore() {
        System.out.println(name + " is exploring!");
    }

    private void updateScore() {
        score += (int)(Math.random() * 10);
    }

    public int getScore() {
        return score;
    }

    public String getName() {
        return name;
    }

    public static void main(String[] args) {
        AdvancedBot bot = new AdvancedBot("Advanced");
        for (int i = 0; i < 5; i++) {
            bot.makeMove();
        }
        System.out.println("Final score: " + bot.getScore());
    }
}
