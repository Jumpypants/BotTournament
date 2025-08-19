package tournament;

import BotCheckers.NetworkLibraryChecker;
import BotCheckers.TokenCounter;
import interfaces.Connect4Bot;
import tournament.Connect4Tournament.TournamentResults;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Restricted Connect 4 tournament that enforces security and complexity restrictions.
 * This tournament system:
 * 1. Runs TokenCounter analysis to eliminate overly complex bots
 * 2. Runs NetworkLibraryChecker security checks to eliminate cheating bots
 * 3. Only allows compliant bots to participate in the tournament
 */
public class RestrictedConnect4Tournament {

    private static final int DEFAULT_MAX_TOKENS = 1500; // Maximum allowed tokens per bot
    private static final int DEFAULT_ROUNDS = 6;

    private final int maxTokens;
    private final int numRounds;
    private final List<Connect4Bot> allBots;
    private final List<Connect4Bot> approvedBots;
    private final Map<String, Integer> botTokenCounts;
    private final Set<String> securityViolators;
    private final TokenCounter tokenCounter;
    private final NetworkLibraryChecker networkChecker;

    public RestrictedConnect4Tournament() {
        this(DEFAULT_MAX_TOKENS, DEFAULT_ROUNDS);
    }

    public RestrictedConnect4Tournament(int maxTokens, int numRounds) {
        this.maxTokens = maxTokens;
        this.numRounds = numRounds;
        this.allBots = new ArrayList<>();
        this.approvedBots = new ArrayList<>();
        this.botTokenCounts = new HashMap<>();
        this.securityViolators = new HashSet<>();
        this.tokenCounter = new TokenCounter();
        this.networkChecker = new NetworkLibraryChecker();
    }

    /**
     * Runs the complete restricted tournament with security checks.
     */
    public TournamentResults runRestrictedTournament() {
        System.out.println("🔐 RESTRICTED CONNECT 4 TOURNAMENT");
        System.out.println("=" + "=".repeat(60));
        System.out.printf("Max Tokens Allowed: %d | Tournament Rounds: %d%n", maxTokens, numRounds);
        System.out.println("=" + "=".repeat(60));

        // Step 1: Discover all bots using shared utility
        allBots.addAll(BotDiscovery.discoverAllBots());

        if (allBots.isEmpty()) {
            System.err.println("❌ No bots found for tournament!");
            return null;
        }

        System.out.printf("📋 Found %d bot(s) for evaluation%n", allBots.size());

        // Step 2: Run token complexity analysis
        System.out.println("\n🔍 PHASE 1: TOKEN COMPLEXITY ANALYSIS");
        System.out.println("-".repeat(50));
        runTokenAnalysis();

        // Step 3: Run security and library analysis
        System.out.println("\n🛡️  PHASE 2: SECURITY & LIBRARY ANALYSIS");
        System.out.println("-".repeat(50));
        runSecurityAnalysis();

        // Step 4: Filter approved bots
        filterApprovedBots();

        // Step 5: Run tournament with approved bots
        if (approvedBots.size() >= 2) {
            System.out.println("\n🏆 PHASE 3: TOURNAMENT EXECUTION");
            System.out.println("-".repeat(50));
            return runTournamentWithApprovedBots();
        } else {
            System.err.printf("❌ Insufficient approved bots for tournament. Found: %d (need at least 2)%n",
                            approvedBots.size());
            return null;
        }
    }


    /**
     * Runs TokenCounter analysis on all bot files and records results.
     */
    private void runTokenAnalysis() {
        String implementationsDir = "src/main/java/implementations";

        try (var walk = Files.walk(Paths.get(implementationsDir))) {
            List<Path> javaFiles = walk
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList();

            System.out.println("Running token analysis on bot files...");

            for (Path javaFile : javaFiles) {
                String fileName = javaFile.getFileName().toString();
                String botName = fileName.replace(".java", "");

                try {
                    // Use TokenCounter to analyze the file
                    int tokenCount = analyzeFileWithTokenCounter(javaFile);
                    botTokenCounts.put(botName, tokenCount);

                    boolean compliant = tokenCount <= maxTokens;
                    String status = compliant ? "✅ PASS" : "❌ FAIL";

                    System.out.printf("%-25s: %4d tokens %s%n", botName, tokenCount, status);

                    if (!compliant) {
                        System.out.printf("   └─ Exceeds limit by %d tokens%n", tokenCount - maxTokens);
                    }

                } catch (Exception e) {
                    System.err.printf("❌ %-25s: Analysis failed - %s%n", botName, e.getMessage());
                    // Assume violation if analysis fails
                    botTokenCounts.put(botName, Integer.MAX_VALUE);
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Failed to analyze tokens: " + e.getMessage());
        }
    }

    /**
     * Uses TokenCounter to analyze a single file.
     */
    private int analyzeFileWithTokenCounter(Path filePath) {
        // Call TokenCounter's analysis method directly on the file
        return tokenCounter.analyzeFileForTokens(filePath);
    }

    /**
     * Runs NetworkLibraryChecker analysis on all bot files.
     */
    private void runSecurityAnalysis() {
        String implementationsDir = "src/main/java/implementations";

        try (var walk = Files.walk(Paths.get(implementationsDir))) {
            List<Path> javaFiles = walk
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList();

            System.out.println("Running security analysis on bot files...");

            for (Path javaFile : javaFiles) {
                String fileName = javaFile.getFileName().toString();
                String botName = fileName.replace(".java", "");

                try {
                    // Use NetworkLibraryChecker to analyze the file
                    boolean hasViolations = analyzeFileWithNetworkChecker(javaFile);

                    if (hasViolations) {
                        securityViolators.add(botName);
                        System.out.printf("❌ %-25s: SECURITY VIOLATION DETECTED%n", botName);
                    } else {
                        System.out.printf("✅ %-25s: Security check passed%n", botName);
                    }

                } catch (Exception e) {
                    System.err.printf("❌ %-25s: Security analysis failed - %s%n", botName, e.getMessage());
                    // Assume violation if analysis fails
                    securityViolators.add(botName);
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Failed to run security analysis: " + e.getMessage());
        }
    }

    /**
     * Uses NetworkLibraryChecker to analyze a single file for security violations.
     */
    private boolean analyzeFileWithNetworkChecker(Path filePath) {
        // Call NetworkLibraryChecker's analysis method directly on the file
        return networkChecker.analyzeFileForViolations(filePath);
    }

    /**
     * Filters bots based on token limits and security analysis results.
     */
    private void filterApprovedBots() {
        System.out.println("\n📊 FILTERING RESULTS:");
        System.out.println("-".repeat(40));

        int tokenViolations = 0;
        int securityViolationsCount = 0;

        for (Connect4Bot bot : allBots) {
            String botSimpleName = bot.getClass().getSimpleName();

            boolean tokenCompliant = true;
            boolean securityCompliant = true;

            // Check token compliance
            Integer tokens = botTokenCounts.get(botSimpleName);
            if (tokens != null && tokens > maxTokens) {
                tokenCompliant = false;
                tokenViolations++;
            }

            // Check security compliance
            if (securityViolators.contains(botSimpleName)) {
                securityCompliant = false;
                securityViolationsCount++;
            }

            if (tokenCompliant && securityCompliant) {
                approvedBots.add(bot);
                System.out.printf("✅ %-25s: APPROVED%n", bot.getBotName());
            } else {
                List<String> violations = new ArrayList<>();
                if (!tokenCompliant) violations.add("Token limit exceeded");
                if (!securityCompliant) violations.add("Security violation");

                System.out.printf("❌ %-25s: REJECTED (%s)%n",
                                bot.getBotName(), String.join(", ", violations));
            }
        }

        System.out.println("-".repeat(40));
        System.out.printf("📈 SUMMARY: %d approved, %d token violations, %d security violations%n",
                         approvedBots.size(), tokenViolations, securityViolationsCount);
    }

    /**
     * Runs the Connect4Tournament with only the approved bots.
     */
    private TournamentResults runTournamentWithApprovedBots() {
        System.out.printf("🎮 Starting tournament with %d approved bots%n", approvedBots.size());
        Connect4Tournament tournament = new Connect4Tournament(approvedBots, numRounds);
        return tournament.runTournament();
    }

    /**
     * Main method to run a restricted tournament with default settings.
     */
    public static void main(String[] args) {
        int maxTokens = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_MAX_TOKENS;
        int rounds = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_ROUNDS;

        System.out.println("🚀 Starting Restricted Connect4 Tournament");
        System.out.printf("   Max tokens: %d | Rounds: %d%n", maxTokens, rounds);

        RestrictedConnect4Tournament restrictedTournament =
            new RestrictedConnect4Tournament(maxTokens, rounds);

        TournamentResults results = restrictedTournament.runRestrictedTournament();

        if (results != null) {
            System.out.println("\n🎉 Restricted tournament completed successfully!");
        } else {
            System.out.println("\n💥 Restricted tournament could not be completed.");
        }
    }
}
