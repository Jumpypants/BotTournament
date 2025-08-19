package bots;

/**
 * Connect 4 game engine that runs a match between two Connect4Bot implementations.
 * Handles game logic, board state, win detection, and turn management.
 */
public class Connect4Game {

    private static final int ROWS = 6;
    private static final int COLS = 7;
    private static final int CONNECT_LENGTH = 4;

    private int[][] board;
    private Connect4Bot player1;
    private Connect4Bot player2;
    private int currentPlayer;
    private boolean gameOver;
    private int winner; // 0 = draw, 1 = player1, 2 = player2
    private int moveCount;

    /**
     * Creates a new Connect 4 game with two bot players.
     *
     * @param player1 Bot that will play as player 1 (goes first)
     * @param player2 Bot that will play as player 2 (goes second)
     */
    public Connect4Game(Connect4Bot player1, Connect4Bot player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.board = new int[ROWS][COLS];
        this.currentPlayer = 1; // Player 1 starts
        this.gameOver = false;
        this.winner = 0;
        this.moveCount = 0;

        // Initialize empty board
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                board[row][col] = 0;
            }
        }
    }

    /**
     * Runs the complete game until there's a winner or draw.
     *
     * @return GameResult containing the outcome and game statistics
     */
    public GameResult playGame() {
        System.out.println("Starting Connect 4 game:");
        System.out.println("Player 1: " + player1.getBotName());
        System.out.println("Player 2: " + player2.getBotName());
        System.out.println("=" + "=".repeat(40));

        printBoard();

        while (!gameOver) {
            playTurn();
            printBoard();

            if (moveCount >= ROWS * COLS) {
                gameOver = true;
                winner = 0; // Draw
                break;
            }
        }

        return new GameResult(winner, moveCount, player1.getBotName(), player2.getBotName());
    }

    /**
     * Executes one turn of the game.
     */
    private void playTurn() {
        Connect4Bot currentBot = (currentPlayer == 1) ? player1 : player2;

        try {
            // Get move from current bot
            int column = currentBot.play(copyBoard(), currentPlayer);

            // Validate and make the move
            if (!isValidMove(column)) {
                System.err.println("Invalid move by " + currentBot.getBotName() +
                                 ": column " + column);
                // Forfeit the game
                winner = (currentPlayer == 1) ? 2 : 1;
                gameOver = true;
                return;
            }

            // Drop the piece
            int row = dropPiece(column, currentPlayer);
            moveCount++;

            System.out.println("Player " + currentPlayer + " (" + currentBot.getBotName() +
                             ") plays column " + column);

            // Check for win
            if (checkWin(row, column, currentPlayer)) {
                winner = currentPlayer;
                gameOver = true;
                return;
            }

            // Switch players
            currentPlayer = (currentPlayer == 1) ? 2 : 1;

        } catch (Exception e) {
            System.err.println("Error from " + currentBot.getBotName() + ": " + e.getMessage());
            // Forfeit the game
            winner = (currentPlayer == 1) ? 2 : 1;
            gameOver = true;
        }
    }

    /**
     * Checks if a move is valid (column exists and isn't full).
     */
    private boolean isValidMove(int column) {
        return column >= 0 && column < COLS && board[0][column] == 0;
    }

    /**
     * Drops a piece in the specified column and returns the row it landed in.
     */
    private int dropPiece(int column, int player) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][column] == 0) {
                board[row][column] = player;
                return row;
            }
        }
        throw new IllegalStateException("Column is full"); // Shouldn't happen if validated
    }

    /**
     * Checks if the last move resulted in a win.
     */
    private boolean checkWin(int row, int col, int player) {
        // Check horizontal
        if (checkDirection(row, col, player, 0, 1) || // Right
            checkDirection(row, col, player, 0, -1)) { // Left
            return true;
        }

        // Check vertical
        if (checkDirection(row, col, player, 1, 0) || // Down
            checkDirection(row, col, player, -1, 0)) { // Up
            return true;
        }

        // Check diagonal /
        if (checkDirection(row, col, player, 1, 1) || // Down-right
            checkDirection(row, col, player, -1, -1)) { // Up-left
            return true;
        }

        // Check diagonal \
        if (checkDirection(row, col, player, 1, -1) || // Down-left
            checkDirection(row, col, player, -1, 1)) { // Up-right
            return true;
        }

        return false;
    }

    /**
     * Checks for 4 in a row in a specific direction from a given position.
     */
    private boolean checkDirection(int row, int col, int player, int deltaRow, int deltaCol) {
        int count = 1; // Count the piece we just placed

        // Check in positive direction
        int r = row + deltaRow;
        int c = col + deltaCol;
        while (r >= 0 && r < ROWS && c >= 0 && c < COLS && board[r][c] == player) {
            count++;
            r += deltaRow;
            c += deltaCol;
        }

        // Check in negative direction
        r = row - deltaRow;
        c = col - deltaCol;
        while (r >= 0 && r < ROWS && c >= 0 && c < COLS && board[r][c] == player) {
            count++;
            r -= deltaRow;
            c -= deltaCol;
        }

        return count >= CONNECT_LENGTH;
    }

    /**
     * Creates a copy of the board to pass to bots (prevents cheating).
     */
    private int[][] copyBoard() {
        int[][] copy = new int[ROWS][COLS];
        for (int row = 0; row < ROWS; row++) {
            System.arraycopy(board[row], 0, copy[row], 0, COLS);
        }
        return copy;
    }

    /**
     * Prints the current board state to console.
     */
    private void printBoard() {
        System.out.println();

        // Print column numbers
        System.out.print("  ");
        for (int col = 0; col < COLS; col++) {
            System.out.print(col + " ");
        }
        System.out.println();

        // Print board
        for (int row = 0; row < ROWS; row++) {
            System.out.print("| ");
            for (int col = 0; col < COLS; col++) {
                char piece = switch (board[row][col]) {
                    case 0 -> '.';
                    case 1 -> 'X';
                    case 2 -> 'O';
                    default -> '?';
                };
                System.out.print(piece + " ");
            }
            System.out.println("|");
        }

        // Print bottom border
        System.out.print("+-");
        for (int col = 0; col < COLS; col++) {
            System.out.print("--");
        }
        System.out.println("+");
    }

    /**
     * Main method for testing the game with two RandomConnect4Bot instances.
     */
    public static void main(String[] args) {
        Connect4Bot bot1 = new RandomConnect4Bot();
        Connect4Bot bot2 = new RandomConnect4Bot();

        Connect4Game game = new Connect4Game(bot1, bot2);
        GameResult result = game.playGame();

        System.out.println("\n" + "=".repeat(50));
        System.out.println("GAME OVER!");
        System.out.println(result);
    }

    /**
     * Result of a Connect 4 game.
     */
    public static class GameResult {
        private final int winner; // 0 = draw, 1 = player1, 2 = player2
        private final int totalMoves;
        private final String player1Name;
        private final String player2Name;

        public GameResult(int winner, int totalMoves, String player1Name, String player2Name) {
            this.winner = winner;
            this.totalMoves = totalMoves;
            this.player1Name = player1Name;
            this.player2Name = player2Name;
        }

        public int getWinner() { return winner; }
        public int getTotalMoves() { return totalMoves; }
        public String getPlayer1Name() { return player1Name; }
        public String getPlayer2Name() { return player2Name; }

        @Override
        public String toString() {
            String outcome = switch (winner) {
                case 0 -> "Draw";
                case 1 -> "Winner: " + player1Name + " (Player 1)";
                case 2 -> "Winner: " + player2Name + " (Player 2)";
                default -> "Unknown result";
            };

            return String.format("%s\nTotal moves: %d\nPlayer 1: %s\nPlayer 2: %s",
                               outcome, totalMoves, player1Name, player2Name);
        }
    }
}
