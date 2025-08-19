import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class NetworkLibraryChecker {

    // Network-related packages and classes
    private static final Set<String> NETWORK_PACKAGES = Set.of(
        "java.net",
        "java.nio.channels",
        "javax.net",
        "java.rmi",
        "org.apache.http",
        "okhttp3",
        "com.squareup.okhttp",
        "retrofit2",
        "io.netty",
        "org.springframework.web",
        "javax.servlet",
        "java.net.http"
    );

    // External library packages (common ones)
    private static final Set<String> EXTERNAL_LIBRARY_PACKAGES = Set.of(
        "org.apache",
        "com.google",
        "org.springframework",
        "com.fasterxml",
        "org.junit",
        "org.mockito",
        "io.netty",
        "com.squareup",
        "retrofit2",
        "okhttp3",
        "org.slf4j",
        "ch.qos.logback",
        "org.json",
        "com.google.gson",
        "org.yaml",
        "redis.clients",
        "com.mongodb",
        "org.postgresql",
        "com.mysql",
        "org.hibernate"
    );

    // Network-related method calls
    private static final Set<String> NETWORK_METHODS = Set.of(
        "connect", "socket", "bind", "listen", "accept", "send", "receive",
        "openConnection", "openStream", "getInputStream", "getOutputStream",
        "sendRequest", "post", "get", "put", "delete", "patch"
    );

    public static void main(String[] args) {
        String botsDirectory = "src/main/java/implementations";
        NetworkLibraryChecker checker = new NetworkLibraryChecker();
        checker.analyzeBotsDirectory(botsDirectory);
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

            System.out.println("Network and External Library Analysis for bots:");
            System.out.println("=" + "=".repeat(60));

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
                String fileName = filePath.getFileName().toString();

                System.out.println("\n📁 " + fileName);
                System.out.println("-".repeat(40));

                AnalysisResult result = analyzeCompilationUnit(cu);

                if (result.hasNetworkAccess || result.hasExternalLibraries) {
                    if (result.hasNetworkAccess) {
                        System.out.println("🌐 NETWORK ACCESS DETECTED:");
                        result.networkImports.forEach(imp ->
                            System.out.println("  • Import: " + imp));
                        result.networkMethodCalls.forEach(method ->
                            System.out.println("  • Method call: " + method));
                    }

                    if (result.hasExternalLibraries) {
                        System.out.println("📚 EXTERNAL LIBRARIES DETECTED:");
                        result.externalImports.forEach(imp ->
                            System.out.println("  • Import: " + imp));
                    }
                } else {
                    System.out.println("✅ Clean - No network access or external libraries detected");
                }

            } else {
                System.err.println("Failed to parse: " + filePath.getFileName());
                parseResult.getProblems().forEach(problem ->
                    System.err.println("  Problem: " + problem.getMessage()));
            }

        } catch (IOException e) {
            System.err.println("Error reading file " + filePath.getFileName() + ": " + e.getMessage());
        }
    }

    private AnalysisResult analyzeCompilationUnit(CompilationUnit cu) {
        AnalysisResult result = new AnalysisResult();

        // Analyze imports
        for (ImportDeclaration importDecl : cu.getImports()) {
            String importName = importDecl.getNameAsString();

            // Check for network-related imports
            if (isNetworkImport(importName)) {
                result.networkImports.add(importName);
                result.hasNetworkAccess = true;
            }

            // Check for external library imports
            if (isExternalLibraryImport(importName)) {
                result.externalImports.add(importName);
                result.hasExternalLibraries = true;
            }
        }

        // Analyze method calls
        cu.accept(new MethodCallVisitor(), result);

        return result;
    }

    private boolean isNetworkImport(String importName) {
        return NETWORK_PACKAGES.stream().anyMatch(importName::startsWith);
    }

    private boolean isExternalLibraryImport(String importName) {
        // Don't count java.* and javax.* as external libraries at all
        if (importName.startsWith("java.") || importName.startsWith("javax.")) {
            return false;
        }

        return EXTERNAL_LIBRARY_PACKAGES.stream().anyMatch(importName::startsWith);
    }

    private static class MethodCallVisitor extends VoidVisitorAdapter<AnalysisResult> {
        @Override
        public void visit(MethodCallExpr methodCall, AnalysisResult result) {
            super.visit(methodCall, result);

            String methodName = methodCall.getNameAsString();
            if (NETWORK_METHODS.contains(methodName.toLowerCase())) {
                result.networkMethodCalls.add(methodName);
                result.hasNetworkAccess = true;
            }
        }
    }

    private static class AnalysisResult {
        boolean hasNetworkAccess = false;
        boolean hasExternalLibraries = false;
        Set<String> networkImports = new HashSet<>();
        Set<String> externalImports = new HashSet<>();
        Set<String> networkMethodCalls = new HashSet<>();
    }
}
