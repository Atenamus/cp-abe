package com.atenamus.backend.performance;

import com.atenamus.backend.Cpabe;
import com.atenamus.backend.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
public class CpabePerformanceTest {

    @Autowired
    private Cpabe cpabe;

    private PublicKey pub;
    private MasterSecretKey msk;
    private static final int WARMUP_RUNS = 5;
    private static final int MEASUREMENT_RUNS = 10;

    @BeforeEach
    void init() {
        pub = new PublicKey();
        msk = new MasterSecretKey();
        try {
            cpabe.setup(pub, msk); // Initialize pairing
        } catch (Exception e) {
            System.err.println("Setup initialization failed: " + e.getMessage());
        }
    }

    @Test
    void testSetupPerformance() {
        System.out.println("\n=== Setup Performance Test ===");
        System.out.println(formatRow("Operation", "Avg Time (ms)"));
        System.out.println(formatSeparator());

        // Warmup
        for (int i = 0; i < WARMUP_RUNS; i++) {
            runSetup();
        }

        // Measurement
        double totalTime = 0;
        for (int i = 0; i < MEASUREMENT_RUNS; i++) {
            totalTime += runSetup();
        }
        double avgTime = totalTime / MEASUREMENT_RUNS;

        System.out.println(formatRow("System Setup", String.format("%.2f", avgTime)));
        System.out.println();
    }

    private double runSetup() {
        PublicKey tempPub = new PublicKey();
        MasterSecretKey tempMsk = new MasterSecretKey();
        long start = System.nanoTime();
        try {
            cpabe.setup(tempPub, tempMsk);
        } catch (Exception e) {
            System.err.println("Setup failed: " + e.getMessage());
            return 0.0;
        }
        long end = System.nanoTime();
        return (end - start) / 1_000_000.0;
    }

    @Test
    void testKeygenPerformance() {
        System.out.println("\n=== Keygen Performance Test ===");
        System.out.println(formatRow("Attributes", "Avg Time (ms)"));
        System.out.println(formatSeparator());

        int[] attrCounts = { 1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50 };

        for (int count : attrCounts) {
            String[] attrs = generateAttributes(count);

            // Warmup
            for (int i = 0; i < WARMUP_RUNS; i++) {
                runKeygen(attrs);
            }

            // Measurement
            double totalTime = 0;
            int successfulRuns = 0;
            for (int i = 0; i < MEASUREMENT_RUNS; i++) {
                double time = runKeygen(attrs);
                if (time > 0) {
                    totalTime += time;
                    successfulRuns++;
                }
            }
            double avgTime = successfulRuns > 0 ? totalTime / successfulRuns : 0.0;

            System.out.println(formatRow(String.valueOf(count), String.format("%.2f", avgTime)));
        }
        System.out.println();
    }

    private double runKeygen(String[] attrs) {
        long start = System.nanoTime();
        try {
            cpabe.keygen(pub, msk, attrs);
            long end = System.nanoTime();
            return (end - start) / 1_000_000.0;
        } catch (Exception e) {
            System.err.println("Keygen failed for attributes " + Arrays.toString(attrs) + ": " + e.getMessage());
            return 0.0;
        }
    }

    @Test
    void testEncryptPerformance() {
        System.out.println("\n=== Encrypt Performance Test ===");
        System.out.println(formatRow("Policy", "Avg Time (ms)"));
        System.out.println(formatSeparator());
        String[] policies = {
                "attr1",
                "attr1 and attr2",
                "(attr1 or attr2) and attr3",
                "(attr1 and attr2) or (attr3 and attr4)",
                "(attr1 and attr2 and attr3) or (attr4 and attr5)",
                "((attr1 or attr2) and (attr3 or attr4)) and attr5",
                "(attr1 and attr2) or (attr3 and attr4) or (attr5 and attr6)",
                "((attr1 and attr2) or (attr3 and attr4)) and ((attr5 and attr6) or (attr7 and attr8))",
                "(attr1 or attr2 or attr3) and (attr4 or attr5 or attr6) and (attr7 or attr8 or attr9)",
                "((attr1 and attr2) or (attr3 and attr4)) and ((attr5 or attr6) and (attr7 or attr8)) and (attr9 and attr10)"
        };

        for (String policy : policies) {
            // Warmup
            for (int i = 0; i < WARMUP_RUNS; i++) {
                runEncrypt(policy);
            }

            // Measurement
            double totalTime = 0;
            int successfulRuns = 0;
            for (int i = 0; i < MEASUREMENT_RUNS; i++) {
                double time = runEncrypt(policy);
                if (time > 0) {
                    totalTime += time;
                    successfulRuns++;
                }
            }
            double avgTime = successfulRuns > 0 ? totalTime / successfulRuns : 0.0;

            System.out.println(formatRow(policy, String.format("%.2f", avgTime)));
        }
        System.out.println();
    }

    private double runEncrypt(String policy) {
        long start = System.nanoTime();
        try {
            cpabe.encrypt(pub, policy);
            long end = System.nanoTime();
            return (end - start) / 1_000_000.0;
        } catch (Exception e) {
            System.err.println("Encrypt failed for policy " + policy + ": " + e.getMessage());
            return 0.0;
        }
    }

    @Test
    void testDecryptPerformance() {
        System.out.println("\n=== Decrypt Performance Test ===");
        System.out.println(formatRow("Policy", "Attributes", "Satisfied", "Avg Time (ms)"));
        System.out.println(formatSeparator());
        String[][] testCases = {
                // Simple cases
                { "attr1", "attr1", "true" },
                { "attr1 and attr2", "attr1,attr2", "true" },

                // Basic AND/OR combinations
                { "(attr1 or attr2) and attr3", "attr1,attr3", "true" },
                { "(attr1 or attr2) and attr3", "attr2,attr3", "true" },

                // More complex combinations
                { "(attr1 and attr2) or (attr3 and attr4)", "attr1,attr2", "true" },
                { "(attr1 and attr2) or (attr3 and attr4)", "attr3,attr4", "true" },

                // Complex nested structures
                { "((attr1 or attr2) and (attr3 or attr4)) and attr5", "attr1,attr3,attr5", "true" },
                { "((attr1 or attr2) and (attr3 or attr4)) and attr5", "attr2,attr4,attr5", "true" },

                // Multiple clauses
                { "(attr1 and attr2) or (attr3 and attr4) or (attr5 and attr6)", "attr1,attr2", "true" },
                { "(attr1 and attr2) or (attr3 and attr4) or (attr5 and attr6)", "attr3,attr4", "true" },
                { "(attr1 and attr2) or (attr3 and attr4) or (attr5 and attr6)", "attr5,attr6", "true" },

                // Large complex policies
                { "((attr1 and attr2) or (attr3 and attr4)) and ((attr5 and attr6) or (attr7 and attr8))",
                        "attr1,attr2,attr5,attr6", "true" },
                { "((attr1 and attr2) or (attr3 and attr4)) and ((attr5 and attr6) or (attr7 and attr8))",
                        "attr3,attr4,attr7,attr8", "true" },

                // Three-clause policies
                { "(attr1 or attr2 or attr3) and (attr4 or attr5 or attr6) and (attr7 or attr8 or attr9)",
                        "attr1,attr4,attr7", "true" },
                { "(attr1 or attr2 or attr3) and (attr4 or attr5 or attr6) and (attr7 or attr8 or attr9)",
                        "attr2,attr5,attr8", "true" },

                // Most complex case
                { "((attr1 and attr2) or (attr3 and attr4)) and ((attr5 or attr6) and (attr7 or attr8)) and (attr9 and attr10)",
                        "attr1,attr2,attr5,attr7,attr9,attr10", "true" },
                { "((attr1 and attr2) or (attr3 and attr4)) and ((attr5 or attr6) and (attr7 or attr8)) and (attr9 and attr10)",
                        "attr3,attr4,attr6,attr8,attr9,attr10", "true" },
        };

        for (String[] test : testCases) {
            String policy = test[0];
            String[] attrList = test[1].split(",");
            boolean expectedSatisfy = Boolean.parseBoolean(test[2]);

            // Generate keys and ciphertext
            PrivateKey prv = null;
            Cipher cipher = null;
            try {
                prv = cpabe.keygen(pub, msk, attrList);
                CipherKey cipherKey = cpabe.encrypt(pub, policy);
                cipher = cipherKey.cph;

                // Log details for debugging
                // System.out.println("Testing policy: " + policy + ", attributes: " +
                // Arrays.toString(attrList));

                if (expectedSatisfy) {
                    // Verify decryption works
                    try {
                        ElementBoolean result = cpabe.decrypt(pub, prv, cipher);
                        // System.out.println("Decrypt result: " + (result != null ? result.toString() :
                        // "null"));
                    } catch (Exception e) {
                        System.out.println(formatRow(policy, Arrays.toString(attrList), "false (expected true)",
                                "Failed: " + e.getMessage()));
                        continue;
                    }

                    // Warmup
                    for (int i = 0; i < WARMUP_RUNS; i++) {
                        runDecrypt(prv, cipher);
                    }

                    // Measurement
                    double totalTime = 0;
                    int successfulRuns = 0;
                    for (int i = 0; i < MEASUREMENT_RUNS; i++) {
                        double time = runDecrypt(prv, cipher);
                        if (time > 0) {
                            totalTime += time;
                            successfulRuns++;
                        }
                    }
                    double avgTime = successfulRuns > 0 ? totalTime / successfulRuns : 0.0;

                    System.out.println(
                            formatRow(policy, Arrays.toString(attrList), "true", String.format("%.2f", avgTime)));
                } else {
                    // Expect failure
                    try {
                        cpabe.decrypt(pub, prv, cipher);
                        System.out.println(formatRow(policy, Arrays.toString(attrList), "true (expected false)",
                                "Unexpected success"));
                    } catch (Exception e) {
                        System.out.println(formatRow(policy, Arrays.toString(attrList), "false", "N/A"));
                    }
                }
            } catch (Exception e) {
                System.out.println(formatRow(policy, Arrays.toString(attrList), String.valueOf(expectedSatisfy),
                        "Error: " + e.getMessage()));
            }
        }
        System.out.println();
    }

    private double runDecrypt(PrivateKey prv, Cipher cipher) {
        long start = System.nanoTime();
        try {
            cpabe.decrypt(pub, prv, cipher);
            long end = System.nanoTime();
            return (end - start) / 1_000_000.0;
        } catch (Exception e) {
            System.err.println("Decrypt failed: " + e.getMessage());
            return 0.0;
        }
    }

    private String[] generateAttributes(int count) {
        String[] attrs = new String[count];
        for (int i = 0; i < count; i++) {
            attrs[i] = "attr" + (i + 1);
        }
        return attrs;
    }

    private String formatRow(String... columns) {
        StringBuilder sb = new StringBuilder("|");
        for (String col : columns) {
            sb.append(String.format(" %-30s |", col));
        }
        return sb.toString();
    }

    private String formatSeparator() {
        return formatRow("", "", "", "").replace(" ", "-").replace("|", "+");
    }
}