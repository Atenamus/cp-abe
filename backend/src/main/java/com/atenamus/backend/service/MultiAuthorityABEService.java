package com.atenamus.backend.service;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.atenamus.backend.models.AuthorityPublicKey;
import com.atenamus.backend.models.AuthoritySecretKey;
import com.atenamus.backend.models.GlobalParameters;
import com.atenamus.backend.util.RandomOracle;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;

@Service
public class MultiAuthorityABEService {
    private static final Logger logger = LoggerFactory.getLogger(MultiAuthorityABEService.class);
    private static final String STORAGE_DIR = "keys/";
    private final Path projectRoot;
    private final Path globalParamsPath;

    public MultiAuthorityABEService() {
        // Ensure storage directory exists
        this.projectRoot = Paths.get("").toAbsolutePath();
        this.globalParamsPath = projectRoot.resolve(STORAGE_DIR);
    }

    private PairingParameters loadParametersForSecurityLevel(int securityParameter) {
        if (securityParameter < 80 || securityParameter > 256) {
            throw new IllegalArgumentException("Security parameter must be between 80 and 256 bits");
        }

        try {
            ClassPathResource resource = new ClassPathResource("curves/a_181_603.properties");
            try (InputStream inputStream = resource.getInputStream()) {
                PropertiesParameters params = new PropertiesParameters();
                params.load(inputStream);
                logger.info("Loaded pairing parameters: {}", params.toString());
                return params;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load curve parameters", e);
        }
    }

    public String globalSetup(int securityParameter,
            Set<String> attributeUniverse,
            Set<String> authorityUniverse) {
        // Input validation
        if (attributeUniverse == null || attributeUniverse.isEmpty()) {
            throw new IllegalArgumentException("Attribute universe cannot be null or empty");
        }
        if (authorityUniverse == null || authorityUniverse.isEmpty()) {
            throw new IllegalArgumentException("Authority universe cannot be null or empty");
        }

        try {
            // Initialize pairing parameters based on security parameter
            PairingParameters params = loadParametersForSecurityLevel(securityParameter);

            // Initialize the pairing
            Pairing pairing = PairingFactory.getPairing(params);
            if (pairing == null) {
                throw new RuntimeException("Pairing initialization returned null");
            }
            logger.info("Pairing initialized successfully: {}", pairing.getClass().getSimpleName());

            // Get the prime order p from the pairing
            int p = pairing.getZr().getLengthInBytes() * 8; // Convert bytes to bits
            logger.info("Prime order p: {}", p);

            // Get group G
            Field<Element> G = pairing.getG1();
            logger.info("G1 field order: {}", G.getOrder());

            // Choose generator g with retry mechanism
            Element g = null;
            int attempts = 0;
            final int maxAttempts = 5;
            while (attempts < maxAttempts) {
                try {
                    g = G.newRandomElement();
                    if (!g.isZero()) {
                        g = g.getImmutable();
                        break;
                    }
                    logger.warn("Generated invalid or zero element, retrying ({}/{})", attempts + 1, maxAttempts);
                } catch (ArithmeticException e) {
                    logger.warn("Arithmetic exception during element generation, retrying ({}/{})", attempts + 1,
                            maxAttempts);
                }
                attempts++;
            }
            if (g == null) {
                throw new RuntimeException("Failed to generate valid generator after " + maxAttempts + " attempts");
            }
            logger.info("Generator g generated: {}", g.toString());

            // Initialize random oracles
            RandomOracle randomOracle = new RandomOracle(G);
            Function<String, Element> hFunction = randomOracle::hashToElement;
            Function<String, Element> fFunction = randomOracle::hashToElement;

            // Create GlobalParameters object
            GlobalParameters gp = new GlobalParameters(
                    p, pairing, g, G,
                    Set.copyOf(attributeUniverse),
                    Set.copyOf(authorityUniverse),
                    hFunction, fFunction);

            // Save to file
            String fileId = UUID.randomUUID().toString();
            String filePath = STORAGE_DIR + "gp_" + fileId + ".dat";
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
                // Save serializable components
                oos.writeInt(p);
                oos.writeObject(g.toBytes()); // Save g as bytes
                oos.writeObject(Set.copyOf(attributeUniverse));
                oos.writeObject(Set.copyOf(authorityUniverse));
                oos.writeUTF(params.toString()); // Save pairing parameters as string
                logger.info("Global parameters saved to {}", filePath);
            } catch (IOException e) {
                logger.error("Failed to save global parameters", e);
                throw new RuntimeException("Failed to save global parameters", e);
            }

            return fileId; // Return the file identifier

        } catch (Exception e) {
            logger.error("Global setup failed", e);
            throw new RuntimeException("Global setup failed: " + e.getMessage(), e);
        }
    }

    public String authSetup(String globalParamsFileId, String authorityId) {
        try {
            // Load GlobalParameters from file
            GlobalParameters gp = loadFromFile(globalParamsFileId);
            Pairing pairing = gp.getPairing();
            Element g = gp.getG();
            Field<Element> Zr = pairing.getZr();

            // Step 1: Choose random α_θ, y_θ ∈ Z_p
            Element alpha = Zr.newRandomElement().getImmutable();
            Element y = Zr.newRandomElement().getImmutable();

            // Step 2: Compute PK_θ = {e(g, g)^α_θ, g^y_θ}
            Element e_g_g_alpha = pairing.pairing(g, g).powZn(alpha).getImmutable();
            Element g_y = g.powZn(y).getImmutable();

            AuthorityPublicKey pk = new AuthorityPublicKey(e_g_g_alpha, g_y, authorityId);
            AuthoritySecretKey sk = new AuthoritySecretKey(alpha, y, authorityId);

            // Step 3: Save PK and SK to files
            String pkFileId = UUID.randomUUID().toString();
            String skFileId = UUID.randomUUID().toString();
            String pkFilePath = STORAGE_DIR + "pk_" + pkFileId + ".dat";
            String skFilePath = STORAGE_DIR + "sk_" + skFileId + ".dat";

            // Save public key
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(pkFilePath))) {
                oos.writeObject(e_g_g_alpha.toBytes());
                oos.writeObject(g_y.toBytes());
                oos.writeUTF(authorityId);
                logger.info("Authority public key saved to {}", pkFilePath);
            }

            // Save secret key (in practice, this should be encrypted or secured)
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(skFilePath))) {
                oos.writeObject(alpha.toBytes());
                oos.writeObject(y.toBytes());
                oos.writeUTF(authorityId);
                logger.info("Authority secret key saved to {}", skFilePath);
            }

            // Return a combined identifier (you could split this if needed)
            return pkFileId + "|" + skFileId;
        } catch (Exception e) {
            logger.error("Auth setup failed for authority {}", authorityId, e);
            throw new RuntimeException("Auth setup failed: " + e.getMessage(), e);
        }
    }

    // Utility method to map attribute to authority (T function from paper)
    public String mapAttributeToAuthority(String attribute) {
        // Implementation of T function: extracts authority from
        // "[attribute]@[authority]" format
        if (attribute == null || !attribute.contains("@")) {
            throw new IllegalArgumentException("Invalid attribute format. Expected: [attribute]@[authority]");
        }
        return attribute.split("@")[1];
    }

    // Optional: Method to load GlobalParameters from file (for later use)
    public GlobalParameters loadFromFile(String fileId) {
        String filePath = STORAGE_DIR + "gp_" + fileId + ".dat";
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            int p = ois.readInt();
            byte[] gBytes = (byte[]) ois.readObject();
            @SuppressWarnings("unchecked")
            Set<String> attributeUniverse = (Set<String>) ois.readObject();
            @SuppressWarnings("unchecked")
            Set<String> authorityUniverse = (Set<String>) ois.readObject();
            String paramsString = ois.readUTF();

            // Reconstruct pairing and field
            PropertiesParameters params = new PropertiesParameters();
            params.load(new ByteArrayInputStream(paramsString.getBytes()));
            Pairing pairing = PairingFactory.getPairing(params);
            Field<Element> G = pairing.getG1();
            Element g = G.newElementFromBytes(gBytes).getImmutable();
            RandomOracle randomOracle = new RandomOracle(G);

            return new GlobalParameters(
                    p, pairing, g, G,
                    attributeUniverse, authorityUniverse,
                    randomOracle::hashToElement, randomOracle::hashToElement);
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Failed to load global parameters from {}", filePath, e);
            throw new RuntimeException("Failed to load global parameters", e);
        }
    }
}
