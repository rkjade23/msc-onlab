package org.example;
//
//import com.intuit.karate.Results;
//import com.intuit.karate.Runner;
//import com.intuit.karate.junit5.Karate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;


public class Main {

    static Process proxyProcess;

    public static void main(String[] args) {
        // Step 1: Modify the karate-config.js file with a new baseUrl
        String workingDir = System.getProperty("user.dir");
        String configPath = Paths.get(workingDir, "src", "test", "java", "karate-config.js").toString();
        modifyConfigFile(configPath, "https://google.com");

        // Step 2: Start the proxy server
        try {
            startProxy();

            // Wait a few seconds to ensure the proxy is fully up and running
            TimeUnit.SECONDS.sleep(3);
            System.out.println("Proxy started at: http://localhost:8899");

            // Set the system properties for Karate config directly
            System.setProperty("karate.config", "proxy=http://localhost:8899\nbaseUrl=https://www.google.com");
//            Karate result = Karate.run("catss").relativeTo(Main.class);
//            result.outputHtmlReport(true);
//
            try {
                // Run Karate tests programmatically
//                Results results = Runner.path("classpath:features") // Path to the features folder
//                        .outputCucumberJson(true) // Generate Cucumber-compatible JSON for reporting
//                        .parallel(3);

//                System.out.println("Test Report generated at: " + results.getReportDir());
//                System.exit(results.getFailCount() > 0 ? 1 : 0); // Exit with error code if tests failed

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            // Step 4: Tear down the proxy
            if (proxyProcess != null) {
                proxyProcess.destroy();
                System.out.println("Proxy shut down.");
            }
        }
    }

    private static void startProxy() throws IOException {
        // Start the proxy process using a simple ProcessBuilder
        ProcessBuilder pb = new ProcessBuilder("proxy");
        pb.redirectErrorStream(true);
        proxyProcess = pb.start();

        // Log proxy output to the console
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
    }

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
