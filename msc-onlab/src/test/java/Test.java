import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Test {

    @BeforeAll
    static void beforeAll() {

        //modifyConfigFile("src/test/java/karate-config.js", "https://google.com");
    }

    static Process proxyProcess;

    @BeforeAll
    static void setupProxy() {
        try {
            ProcessBuilder pb = new ProcessBuilder("proxy");
            pb.redirectErrorStream(true);
            proxyProcess = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(proxyProcess.getInputStream()));
            new Thread(() -> {
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[Proxy] " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            Thread.sleep(3000);
            System.out.println("Proxy started at: http://localhost:8899");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Karate.Test
    Karate testCat() {
        return Karate.run("cats").relativeTo(getClass());
    }

    @AfterAll
    static void teardownProxy() {
        if (proxyProcess != null) {
            proxyProcess.destroy();
            System.out.println("Proxy shut down.");
        }
    }
    /**
     * Updates the karate-config.js file with a new baseUrl.
     * If the file already returns a valid object with a 'baseUrl' property, it updates that value.
     * If the file returns { null } (or anything similar), it replaces the return object with a valid one.
     *
     * @param configFilePath the file path to the karate-config.js file
     * @param baseUrl     the new base URL to set (e.g. "https://google.com")
     */
    public static void modifyConfigFile(String configFilePath, String baseUrl) {
        String content;
        try {
            // Check if the file exists
            if (Files.exists(Paths.get(configFilePath))) {
                // Read the content of the file
                content = new String(Files.readAllBytes(Paths.get(configFilePath)));

                // Check if the file already contains a baseUrl definition
                if (content.matches("(?s).*baseUrl\\s*:\\s*['\"].*['\"].*")) {
                    // If baseUrl exists, replace it with the new value inside the config object
                    content = content.replaceAll("(?m)(baseUrl\\s*:\\s*['\"])([^'\"]*)(['\"])", "$1" + baseUrl + "$3");
                } else {
                    // If baseUrl doesn't exist, add it inside the config object
                    content = content.replaceFirst("(?m)(var config = \\{)", "$1\n        baseUrl: '" + baseUrl + "'");
                }
            } else {
                // If the file doesn't exist, create it with the baseUrl
                content = "function fn() {\n"
                        + "    var config = {\n"
                        + "        baseUrl: '" + baseUrl + "'\n"
                        + "    };\n"
                        + "    return config;\n"
                        + "}\n";
            }

            // Write the modified or new content to the file
            Files.write(Paths.get(configFilePath), content.getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("karate-config.js file successfully updated at " + configFilePath);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error writing or modifying the config file: " + e.getMessage());
        }
    }
}

