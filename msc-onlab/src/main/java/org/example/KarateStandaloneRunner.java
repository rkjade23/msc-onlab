package org.example;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;

public class KarateStandaloneRunner {

    public static void main(String[] args) {
        Results results = Runner.path("classpath:features") // Path to the feature files
                .outputCucumberJson(true) // Enables cucumber.json output for reporting
                .parallel(1); // Number of threads for parallel execution

        // Print results
        System.out.println("Total Features: " + results.getFeaturesTotal());
        System.out.println("Passed Scenarios: " + (results.getFeaturesPassed()));
        System.out.println("Failed Scenarios: " + results.getFailCount());

        // Exit with status 1 if there are failures
        if (results.getFailCount() > 0) {
            System.exit(1);
        }
    }
}