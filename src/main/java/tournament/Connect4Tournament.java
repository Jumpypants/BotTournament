package tournament;

import interfaces.Connect4Bot;
import tournament.Connect4Game.GameResult;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Swiss tournament system for Connect 4 bots.
 * Automatically discovers all bot implementations and runs them in a Swiss-style tournament.
 * In Swiss format, players are paired based on their current standings, and everyone plays
 * the same number of rounds regardless of wins/losses.
 */
public class Connect4Tournament {

    private static final int DEFAULT_ROUNDS = 5;
    private final List<Connect4Bot> bots;
    private final Map<String, TournamentPlayer> players;
    private final List<TournamentRound> rounds;
    private final int numRounds;

    public Connect4Tournament() {
        this(DEFAULT_ROUNDS);
    }

    public Connect4Tournament(int numRounds) {
        this.numRounds = numRounds;
        this.bots = BotDiscovery.discoverAllBots();
        this.players = new HashMap<>();
        this.rounds = new ArrayList<>();

        initializePlayers();
    }

    /**
     * Constructor for pre-approved bots (used by RestrictedConnect4Tournament).
     */
    public Connect4Tournament(List<Connect4Bot> preApprovedBots, int numRounds) {
        this.numRounds = numRounds;
        this.bots = new ArrayList<>(preApprovedBots);
        this.players = new HashMap<>();
        this.rounds = new ArrayList<>();

        System.out.println("🤖 Using pre-approved bots for tournament:");
        for (Connect4Bot bot : bots) {
            System.out.println("   ✅ " + bot.getBotName());
        }

        initializePlayers();
    }


    /**
     * Initialize tournament players from discovered bots.
     */
    private void initializePlayers() {
        for (Connect4Bot bot : bots) {
            players.put(bot.getBotName(), new TournamentPlayer(bot));
        }
    }

    /**
     * Runs the complete Swiss tournament.
     */
    public TournamentResults runTournament() {
        if (bots.size() < 2) {
            throw new IllegalStateException("Need at least 2 bots to run a tournament. Found: " + bots.size());
        }

        System.out.println("🏆 Starting Swiss Tournament");
        System.out.println("=" + "=".repeat(50));
        System.out.printf("Players: %d | Rounds: %d%n", bots.size(), numRounds);
        System.out.println("=" + "=".repeat(50));

        for (int round = 1; round <= numRounds; round++) {
            System.out.printf("%n🎯 ROUND %d%n", round);
            System.out.println("-".repeat(30));

            TournamentRound tournamentRound = runRound(round);
            rounds.add(tournamentRound);

            printRoundResults(tournamentRound);
            printCurrentStandings();
        }

        TournamentResults results = new TournamentResults(players, rounds);
        printFinalResults(results);

        return results;
    }

    /**
     * Runs a single round of the tournament using Swiss pairings.
     */
    private TournamentRound runRound(int roundNumber) {
        List<TournamentPlayer> playerList = new ArrayList<>(players.values());

        // Sort by current score (wins first, then by tiebreakers)
        playerList.sort((a, b) -> {
            // Primary: by wins (descending)
            int winsCompare = Integer.compare(b.wins, a.wins);
            if (winsCompare != 0) return winsCompare;

            // Secondary: by score differential (descending)
            return Double.compare(b.getScoreDifferential(), a.getScoreDifferential());
        });

        // Create Swiss pairings
        List<Pairing> pairings = createSwissPairings(playerList);

        // Play all games in this round
        List<GameResult> roundResults = new ArrayList<>();
        for (Pairing pairing : pairings) {
            GameResult result = playGame(pairing.player1, pairing.player2);
            roundResults.add(result);
            updatePlayerStats(pairing.player1, pairing.player2, result);
        }

        return new TournamentRound(roundNumber, pairings, roundResults);
    }

    /**
     * Creates Swiss-style pairings for a round.
     */
    private List<Pairing> createSwissPairings(List<TournamentPlayer> sortedPlayers) {
        List<Pairing> pairings = new ArrayList<>();
        Set<TournamentPlayer> paired = new HashSet<>();

        for (int i = 0; i < sortedPlayers.size(); i++) {
            TournamentPlayer player1 = sortedPlayers.get(i);
            if (paired.contains(player1)) continue;

            // Find the best available opponent
            TournamentPlayer player2 = null;
            for (int j = i + 1; j < sortedPlayers.size(); j++) {
                TournamentPlayer candidate = sortedPlayers.get(j);
                if (!paired.contains(candidate) && !player1.hasPlayedAgainst(candidate)) {
                    player2 = candidate;
                    break;
                }
            }

            // If no unplayed opponent found, pair with next available
            if (player2 == null) {
                for (int j = i + 1; j < sortedPlayers.size(); j++) {
                    TournamentPlayer candidate = sortedPlayers.get(j);
                    if (!paired.contains(candidate)) {
                        player2 = candidate;
                        break;
                    }
                }
            }

            if (player2 != null) {
                pairings.add(new Pairing(player1, player2));
                paired.add(player1);
                paired.add(player2);
            }
        }

        // Handle odd number of players (bye)
        if (paired.size() < sortedPlayers.size()) {
            TournamentPlayer byePlayer = sortedPlayers.stream()
                .filter(p -> !paired.contains(p))
                .findFirst()
                .orElse(null);

            if (byePlayer != null) {
                // Award bye (counts as a win)
                byePlayer.wins++;
                byePlayer.byes++;
                System.out.println("🔄 " + byePlayer.bot.getBotName() + " receives a bye");
            }
        }

        return pairings;
    }

    /**
     * Plays a game between two players.
     */
    private GameResult playGame(TournamentPlayer player1, TournamentPlayer player2) {
        Connect4Game game = new Connect4Game(player1.bot, player2.bot);
        return game.playGame();
    }

    /**
     * Updates player statistics after a game.
     */
    private void updatePlayerStats(TournamentPlayer player1, TournamentPlayer player2, GameResult result) {
        player1.gamesPlayed++;
        player2.gamesPlayed++;
        player1.addOpponent(player2);
        player2.addOpponent(player1);

        if (result.getWinner() == 1) {
            // Player 1 wins
            player1.wins++;
            player2.losses++;
        } else if (result.getWinner() == 2) {
            // Player 2 wins
            player2.wins++;
            player1.losses++;
        } else {
            // Draw
            player1.draws++;
            player2.draws++;
        }

        // Track move counts for tiebreaking
        player1.totalMoves += result.getTotalMoves();
        player2.totalMoves += result.getTotalMoves();
    }

    /**
     * Prints results for a single round.
     */
    private void printRoundResults(TournamentRound round) {
        for (int i = 0; i < round.pairings.size(); i++) {
            Pairing pairing = round.pairings.get(i);
            GameResult result = round.results.get(i);

            String outcome;
            if (result.getWinner() == 0) {
                outcome = "Draw";
            } else if (result.getWinner() == 1) {
                outcome = pairing.player1.bot.getBotName() + " wins";
            } else {
                outcome = pairing.player2.bot.getBotName() + " wins";
            }

            System.out.printf("%-20s vs %-20s -> %s (%d moves)%n",
                pairing.player1.bot.getBotName(),
                pairing.player2.bot.getBotName(),
                outcome,
                result.getTotalMoves());
        }
    }

    /**
     * Prints current tournament standings.
     */
    private void printCurrentStandings() {
        System.out.println("\n📊 Current Standings:");
        List<TournamentPlayer> standings = players.values().stream()
            .sorted((a, b) -> {
                int winsCompare = Integer.compare(b.wins, a.wins);
                if (winsCompare != 0) return winsCompare;
                return Double.compare(b.getScoreDifferential(), a.getScoreDifferential());
            })
            .collect(Collectors.toList());

        System.out.printf("%-4s %-20s %4s %4s %4s %4s %7s%n",
            "Pos", "Bot", "W", "L", "D", "Pts", "Diff");
        System.out.println("-".repeat(55));

        for (int i = 0; i < standings.size(); i++) {
            TournamentPlayer player = standings.get(i);
            System.out.printf("%-4d %-20s %4d %4d %4d %4d %7.1f%n",
                i + 1,
                player.bot.getBotName(),
                player.wins,
                player.losses,
                player.draws,
                player.getPoints(),
                player.getScoreDifferential());
        }
    }

    /**
     * Prints final tournament results.
     */
    private void printFinalResults(TournamentResults results) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🏆 FINAL TOURNAMENT RESULTS");
        System.out.println("=".repeat(60));

        List<TournamentPlayer> finalStandings = results.getFinalStandings();

        System.out.printf("%-4s %-20s %4s %4s %4s %4s %7s %6s%n",
            "Pos", "Bot", "W", "L", "D", "Pts", "Diff", "AvgM");
        System.out.println("-".repeat(65));

        for (int i = 0; i < finalStandings.size(); i++) {
            TournamentPlayer player = finalStandings.get(i);
            double avgMoves = player.gamesPlayed > 0 ?
                (double) player.totalMoves / player.gamesPlayed : 0.0;

            String medal = "";
            if (i == 0) medal = "🥇";
            else if (i == 1) medal = "🥈";
            else if (i == 2) medal = "🥉";

            System.out.printf("%-4d %-20s %4d %4d %4d %4d %7.1f %6.1f %s%n",
                i + 1,
                player.bot.getBotName(),
                player.wins,
                player.losses,
                player.draws,
                player.getPoints(),
                player.getScoreDifferential(),
                avgMoves,
                medal);
        }

        if (!finalStandings.isEmpty()) {
            System.out.println("\n🏆 Tournament Champion: " + finalStandings.get(0).bot.getBotName());
        }
    }

    // Inner classes for tournament structure

    public static class TournamentPlayer {
        final Connect4Bot bot;
        int wins = 0;
        int losses = 0;
        int draws = 0;
        int byes = 0;
        int gamesPlayed = 0;
        int totalMoves = 0;
        private final Set<String> opponents = new HashSet<>();

        public TournamentPlayer(Connect4Bot bot) {
            this.bot = bot;
        }

        public boolean hasPlayedAgainst(TournamentPlayer other) {
            return opponents.contains(other.bot.getBotName());
        }

        public void addOpponent(TournamentPlayer opponent) {
            opponents.add(opponent.bot.getBotName());
        }

        public int getPoints() {
            return wins * 3 + draws; // 3 points for win, 1 for draw
        }

        public double getScoreDifferential() {
            return gamesPlayed > 0 ? (double) wins / gamesPlayed : 0.0;
        }
    }

    public static class Pairing {
        final TournamentPlayer player1;
        final TournamentPlayer player2;

        public Pairing(TournamentPlayer player1, TournamentPlayer player2) {
            this.player1 = player1;
            this.player2 = player2;
        }
    }

    public static class TournamentRound {
        final int roundNumber;
        final List<Pairing> pairings;
        final List<GameResult> results;

        public TournamentRound(int roundNumber, List<Pairing> pairings, List<GameResult> results) {
            this.roundNumber = roundNumber;
            this.pairings = pairings;
            this.results = results;
        }
    }

    public static class TournamentResults {
        private final Map<String, TournamentPlayer> players;
        private final List<TournamentRound> rounds;

        public TournamentResults(Map<String, TournamentPlayer> players, List<TournamentRound> rounds) {
            this.players = players;
            this.rounds = rounds;
        }

        public List<TournamentPlayer> getFinalStandings() {
            return players.values().stream()
                .sorted((a, b) -> {
                    int pointsCompare = Integer.compare(b.getPoints(), a.getPoints());
                    if (pointsCompare != 0) return pointsCompare;
                    return Double.compare(b.getScoreDifferential(), a.getScoreDifferential());
                })
                .collect(Collectors.toList());
        }

        public List<TournamentRound> getRounds() {
            return rounds;
        }

        public TournamentPlayer getWinner() {
            List<TournamentPlayer> standings = getFinalStandings();
            return standings.isEmpty() ? null : standings.get(0);
        }
    }

    /**
     * Main method to run a tournament with default settings.
     */
    public static void main(String[] args) {
        Connect4Tournament tournament = new Connect4Tournament(6); // 6 rounds
        tournament.runTournament();
    }
}
