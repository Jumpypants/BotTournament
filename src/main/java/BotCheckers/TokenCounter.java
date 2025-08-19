package BotCheckers;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TokenCounter {

    public static void main(String[] args) {
        String botsDirectory = "src/main/java/implementations";
        TokenCounter counter = new TokenCounter();
        counter.analyzeBotsDirectory(botsDirectory);
    }

    public void analyzeBotsDirectory(String directoryPath) {
        Path botsPath = Paths.get(directoryPath);

        if (!Files.exists(botsPath) || !Files.isDirectory(botsPath)) {
            System.err.println("Directory not found: " + directoryPath);
            return;
        }

        try (var walk = Files.walk(botsPath)) {
            List<Path> javaFiles = walk
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList();

            if (javaFiles.isEmpty()) {
                System.out.println("No Java files found in " + directoryPath);
                return;
            }

            System.out.println("Token count analysis for files in " + directoryPath + ":");
            System.out.println("=" + "=".repeat(50));

            for (Path javaFile : javaFiles) {
                analyzeFile(javaFile);
            }

        } catch (IOException e) {
            System.err.println("Error reading directory: " + e.getMessage());
        }
    }

    public void analyzeFile(Path filePath) {
        try {
            JavaParser javaParser = new JavaParser();
            ParseResult<CompilationUnit> parseResult = javaParser.parse(filePath);

            if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
                CompilationUnit cu = parseResult.getResult().get();
                int tokenCount = countTokens(cu);

                String fileName = filePath.getFileName().toString();
                System.out.printf("%-30s: %d tokens%n", fileName, tokenCount);

            } else {
                System.err.println("Failed to parse: " + filePath.getFileName());
                parseResult.getProblems().forEach(problem ->
                    System.err.println("  Problem: " + problem.getMessage()));
            }

        } catch (IOException e) {
            System.err.println("Error reading file " + filePath.getFileName() + ": " + e.getMessage());
        }
    }

    /**
     * Analyzes a single file and returns its token count.
     * This method is used by RestrictedConnect4Tournament.
     */
    public int analyzeFileForTokens(Path filePath) {
        try {
            JavaParser javaParser = new JavaParser();
            ParseResult<CompilationUnit> parseResult = javaParser.parse(filePath);

            if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
                CompilationUnit cu = parseResult.getResult().get();
                return countTokens(cu);
            } else {
                System.err.println("Failed to parse: " + filePath.getFileName());
                return Integer.MAX_VALUE; // Return max value to indicate failure
            }

        } catch (IOException e) {
            System.err.println("Error reading file " + filePath.getFileName() + ": " + e.getMessage());
            return Integer.MAX_VALUE; // Return max value to indicate failure
        }
    }

    private int countTokens(Node node) {
        // Skip comment nodes entirely
        if (node instanceof Comment) {
            return 0;
        }

        int count = 1; // Count the current node as a token

        // Recursively count all child nodes
        for (Node child : node.getChildNodes()) {
            count += countTokens(child);
        }

        return count;
    }
}
