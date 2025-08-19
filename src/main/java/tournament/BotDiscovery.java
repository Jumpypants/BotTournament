package tournament;

import interfaces.Connect4Bot;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for discovering Connect4Bot implementations.
 * Provides consistent bot discovery logic across tournament classes.
 */
public class BotDiscovery {

    private static final String IMPLEMENTATIONS_PACKAGE = "implementations";

    /**
     * Discovers all available Connect4Bot implementations using dynamic discovery.
     */
    public static List<Connect4Bot> discoverAllBots() {
        System.out.println("🔍 Discovering bots in '" + IMPLEMENTATIONS_PACKAGE + "' package...");

        List<Connect4Bot> bots = new ArrayList<>();

        // Try dynamic discovery
        try {
            bots.addAll(dynamicDiscovery());
        } catch (Exception e) {
            System.out.println("ℹ️  Dynamic discovery failed: " + e.getMessage());
        }

        // Remove any duplicates based on bot name
        bots = removeDuplicates(bots);

        System.out.println("✅ Bot discovery completed. Found " + bots.size() + " bots:");
        for (Connect4Bot bot : bots) {
            System.out.println("   • " + bot.getBotName());
        }

        return bots;
    }

    /**
     * Attempts to dynamically discover bots using classpath scanning.
     */
    private static List<Connect4Bot> dynamicDiscovery() throws Exception {
        List<Connect4Bot> bots = new ArrayList<>();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = IMPLEMENTATIONS_PACKAGE.replace('.', '/');

        // Try multiple approaches to find the package
        java.util.Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File directory = new File(resource.toURI());

            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles((dir, name) -> name.endsWith(".class"));
                if (files != null) {
                    for (File file : files) {
                        String className = IMPLEMENTATIONS_PACKAGE + '.' + file.getName().replace(".class", "");
                        Connect4Bot bot = loadBotClass(className);
                        if (bot != null) {
                            bots.add(bot);
                            System.out.println("  ✅ Discovered: " + bot.getBotName());
                        }
                    }
                }
            }
        }

        return bots;
    }

    /**
     * Attempts to load a single bot class by name.
     */
    private static Connect4Bot loadBotClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);

            // Verify it's a valid Connect4Bot implementation
            if (!Connect4Bot.class.isAssignableFrom(clazz) ||
                clazz.isInterface() ||
                java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                return null;
            }

            // Try to instantiate it
            return (Connect4Bot) clazz.getDeclaredConstructor().newInstance();

        } catch (ClassNotFoundException e) {
            System.err.println("  ⚠️  Class not found: " + className);
        } catch (InstantiationException | IllegalAccessException e) {
            System.err.println("  ⚠️  Cannot instantiate " + className + ": " + e.getMessage());
        } catch (NoSuchMethodException e) {
            System.err.println("  ⚠️  No default constructor for " + className);
        } catch (Exception e) {
            System.err.println("  ⚠️  Failed to load " + className + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Removes duplicate bots based on their bot names.
     */
    private static List<Connect4Bot> removeDuplicates(List<Connect4Bot> bots) {
        List<Connect4Bot> unique = new ArrayList<>();
        List<String> seenNames = new ArrayList<>();

        for (Connect4Bot bot : bots) {
            if (!seenNames.contains(bot.getBotName())) {
                unique.add(bot);
                seenNames.add(bot.getBotName());
            }
        }

        return unique;
    }
}
