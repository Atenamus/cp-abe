package com.atenamus.backend.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.atenamus.backend.models.AuthorityPublicKey;
import com.atenamus.backend.models.AuthoritySecretKey;
import com.atenamus.backend.models.Ciphertext;
import com.atenamus.backend.models.GlobalParameters;
import com.atenamus.backend.models.UserSecretKey;
import com.atenamus.backend.util.MultiABEPolicyParser;
import com.atenamus.backend.util.PolicyParser;
import com.atenamus.backend.util.RandomOracle;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;
import jakarta.annotation.PostConstruct;

@Service
public class MultiAuthorityABEService {
    private static final Logger logger = LoggerFactory.getLogger(MultiAuthorityABEService.class);
    private static final String STORAGE_DIR = "keys/";
    private final Map<String, String> authoritySecretKeyMap = new HashMap<>(); // authorityId -> secretKeyFileId
    private final Map<String, String> authorityPublicKeyMap = new HashMap<>(); // Added for PK_θ
    private String globalParamsFileId;
    private final SecureRandom secureRandom = new SecureRandom();

    public MultiAuthorityABEService() {
        try {
            Files.createDirectories(Paths.get(STORAGE_DIR));
        } catch (IOException e) {
            logger.error("Failed to create storage directory", e);
        }
    }

    @PostConstruct
    public void init() {
        // Load existing global parameters
        File dir = new File(STORAGE_DIR);
        File[] gpFiles = dir.listFiles((d, name) -> name.startsWith("gp_") && name.endsWith(".dat"));
        if (gpFiles != null && gpFiles.length > 0) {
            globalParamsFileId = gpFiles[0].getName().replace("gp_", "").replace(".dat", "");
            logger.info("Found existing global parameters: {}", globalParamsFileId);
        } else {
            logger.info("Global parameters not found, initializing with default values...");
            Set<String> defaultAttributeUniverse = Set.of("attr1@auth1", "attr2@auth1", "attr3@auth2");
            Set<String> defaultAuthorityUniverse = Set.of("auth1", "auth2");
            globalParamsFileId = globalSetup(80, defaultAttributeUniverse, defaultAuthorityUniverse);
            logger.info("Global parameters initialized with fileId: {}", globalParamsFileId);
        }

        // Load existing authority keys
        File[] skFiles = dir.listFiles((d, name) -> name.startsWith("sk_") && name.endsWith(".dat"));
        if (skFiles != null) {
            for (File skFile : skFiles) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(skFile))) {
                    ois.readObject(); // alphaBytes
                    ois.readObject(); // yBytes
                    String authorityId = ois.readUTF();
                    String skFileId = skFile.getName().replace("sk_", "").replace(".dat", "");
                    String pkFileId = dir.listFiles((d, name) -> name.startsWith("pk_") && name.endsWith(".dat")
                            && new File(STORAGE_DIR + name).exists())[0].getName().replace("pk_", "")
                            .replace(".dat", "");
                    authoritySecretKeyMap.put(authorityId, skFileId);
                    authorityPublicKeyMap.put(authorityId, pkFileId);
                    logger.info("Loaded existing authority key for {}: pk={}, sk={}", authorityId, pkFileId, skFileId);
                } catch (IOException | ClassNotFoundException e) {
                    logger.warn("Failed to load authority key from {}: {}", skFile.getName(), e.getMessage());
                }
            }
        }
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
            String globalParamsFileId = UUID.randomUUID().toString();
            String filePath = STORAGE_DIR + "gp_" + globalParamsFileId + ".dat";
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

            return globalParamsFileId; // Return the file identifier

        } catch (Exception e) {
            logger.error("Global setup failed", e);
            throw new RuntimeException("Global setup failed: " + e.getMessage(), e);
        }
    }

    public Map<String, String> authSetup(String authorityId) {
        if (globalParamsFileId == null) {
            throw new IllegalStateException("Global parameters not initialized. Run globalSetup first.");
        }

        // Check if authority already exists
        if (authoritySecretKeyMap.containsKey(authorityId)) {
            String skFileId = authoritySecretKeyMap.get(authorityId);
            String pkFileId = authorityPublicKeyMap.get(authorityId);
            logger.info("Authority {} already exists, reusing keys: pk={}, sk={}", authorityId, pkFileId, skFileId);
            return Map.of("publicKeyFileId", pkFileId, "secretKeyFileId", skFileId);
        }

        try {
            // Load GlobalParameters from file
            GlobalParameters gp = loadFromFile(globalParamsFileId);
            Pairing pairing = gp.getPairing();
            Element g = gp.getGen();
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

            authoritySecretKeyMap.put(authorityId, skFileId);
            authorityPublicKeyMap.put(authorityId, pkFileId);
            return Map.of("publicKeyFileId", pkFileId, "secretKeyFileId", skFileId);
        } catch (Exception e) {
            logger.error("Auth setup failed for authority {}", authorityId, e);
            throw new RuntimeException("Auth setup failed: " + e.getMessage(), e);
        }
    }

    public byte[] keyGen(String authorityId, String gid, Set<String> attributes) {
        if (globalParamsFileId == null) {
            throw new IllegalStateException("Global parameters not initialized. Run globalSetup first.");
        }
        String secretKeyFileId = authoritySecretKeyMap.get(authorityId);
        if (secretKeyFileId == null) {
            throw new IllegalStateException("Authority " + authorityId + " not initialized. Run authSetup first.");
        }

        try {
            // Load GlobalParameters and AuthoritySecretKey
            GlobalParameters gp = loadFromFile(globalParamsFileId);
            AuthoritySecretKey sk = loadSecretKey(secretKeyFileId, gp.getPairing());

            Pairing pairing = gp.getPairing();
            Element g = gp.getGen();
            Field<Element> Zr = pairing.getZr();
            Field<Element> G = gp.getG();

            // Verify attributes belong to this authority
            for (String attr : attributes) {
                if (!mapAttributeToAuthority(attr).equals(authorityId)) {
                    throw new IllegalArgumentException(
                            "Attribute " + attr + " does not belong to authority " + authorityId);
                }
            }

            // Step 1: Parse SK_θ
            Element alpha = sk.getAlpha();
            Element y = sk.getY();

            // Step 2: Choose random t ∈ Z_p
            Element t = Zr.newRandomElement().getImmutable();

            // Step 3: Compute user secret key components
            // K_θ = g^α_θ * H(GID)^y_θ * g^t
            Element h_gid = gp.applyHFunction(gid);
            Element K_theta = g.powZn(alpha)
                    .mul(h_gid.powZn(y))
                    .mul(g.powZn(t))
                    .getImmutable();

            // L = g^t
            Element L = g.powZn(t).getImmutable();

            // For each i ∈ S_θ, K_i = F(i)^t
            Map<String, Element> K_i = new HashMap<>();
            for (String attr : attributes) {
                Element f_i = gp.applyFFunction(attr);
                K_i.put(attr, f_i.powZn(t).getImmutable());
            }

            UserSecretKey userKey = new UserSecretKey(K_theta, L, K_i, gid, authorityId);

            // Serialize UserSecretKey to byte array for download
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(K_theta.toBytes());
                oos.writeObject(L.toBytes());
                oos.writeObject(K_i.entrySet().stream()
                        .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue().toBytes()), HashMap::putAll));
                oos.writeUTF(gid);
                oos.writeUTF(authorityId);
            }
            logger.info("User secret key generated for GID {} and authority {}", gid, authorityId);
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("KeyGen failed for GID {} and authority {}", gid, authorityId, e);
            throw new RuntimeException("KeyGen failed: " + e.getMessage(), e);
        }
    }

    public byte[] encrypt(byte[] fileData, String policy) {
        if (globalParamsFileId == null) {
            throw new IllegalStateException("Global parameters not initialized. Run globalSetup first.");
        }
        if (policy == null || policy.trim().isEmpty()) {
            throw new IllegalArgumentException("Policy cannot be null or empty");
        }

        try {
            GlobalParameters gp = loadFromFile(globalParamsFileId);
            Pairing pairing = gp.getPairing();
            Element g = gp.getGen();
            Field<Element> Zr = pairing.getZr();
            Field<Element> GT = pairing.getGT();

            // Step 1: Generate AES key and encrypt file
            byte[] aesKey = new byte[32]; // 256-bit key
            secureRandom.nextBytes(aesKey);
            byte[] iv = new byte[16]; // 128-bit IV
            secureRandom.nextBytes(iv);

            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            aesCipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] aesEncryptedData = aesCipher.doFinal(fileData);

            // Step 2: Parse policy to LSSS
            MultiABEPolicyParser.AccessStructure accessStructure = MultiABEPolicyParser.parsePolicy(policy);
            List<List<Integer>> accessMatrix = accessStructure.matrix;
            List<String> rho = accessStructure.rho;

            // Step 3: Encrypt AES key with CP-ABE
            // Hash AES key to GT to ensure correct field
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hashedKey = sha256.digest(aesKey); // 32 bytes
            Element M = GT.newElementFromHash(hashedKey, 0, hashedKey.length).getImmutable();
            logger.debug("M type: {}", M.getClass().getName()); // Verify GTFiniteElement

            int l = accessMatrix.size();
            int n = accessMatrix.get(0).size();

            // Choose random vector v = (s, v_2, ..., v_n)
            Element[] v = new Element[n];
            v[0] = Zr.newRandomElement().getImmutable(); // s
            for (int j = 1; j < n; j++) {
                v[j] = Zr.newRandomElement().getImmutable();
            }
            Element s = v[0];

            // Compute λ_i = A_i · v
            Element[] lambda = new Element[l];
            for (int i = 0; i < l; i++) {
                List<Integer> A_i = accessMatrix.get(i);
                if (A_i.size() != n) {
                    throw new IllegalArgumentException("Row " + i + " of access matrix must have " + n + " columns");
                }
                lambda[i] = Zr.newZeroElement();
                for (int j = 0; j < n; j++) {
                    Element A_ij = Zr.newElement(A_i.get(j)).getImmutable();
                    lambda[i] = lambda[i].add(A_ij.mul(v[j])).getImmutable();
                }
            }

            // Choose random r_i
            Element[] r = new Element[l];
            for (int i = 0; i < l; i++) {
                r[i] = Zr.newRandomElement().getImmutable();
            }

            // Compute ciphertext components
            Element e_g_g_s = pairing.pairing(g, g).powZn(s).getImmutable(); // GT
            Element C = e_g_g_s.mul(M).getImmutable(); // GT * GT = GT
            Element C_prime = g.powZn(s).getImmutable(); // G1

            List<Ciphertext.CiphertextComponent> components = new ArrayList<>();
            for (int i = 0; i < l; i++) {
                String attribute = rho.get(i);
                String authorityId = mapAttributeToAuthority(attribute);
                String pkFileId = authorityPublicKeyMap.get(authorityId);
                if (pkFileId == null) {
                    throw new IllegalStateException(
                            "Authority " + authorityId + " not initialized. Run authSetup first.");
                }

                AuthorityPublicKey pk = loadPublicKey(pkFileId, pairing);
                Element e_g_g_alpha = pk.getE_g_g_alpha(); // GT
                Element g_y = pk.getG_y(); // G1

                Element C_i = g.powZn(lambda[i]).mul(g_y.powZn(r[i].negate())).getImmutable(); // g^λ_i * (g^y_θ)^(-r_i)
                Element D_i = g.powZn(r[i]).getImmutable(); // g^r_i
                Element E_i = gp.applyFFunction(attribute).powZn(r[i]).getImmutable(); // F(ρ(i))^r_i

                components.add(new Ciphertext.CiphertextComponent(C_i, D_i, E_i, attribute));
            }

            Ciphertext ct = new Ciphertext(aesEncryptedData, iv, C, C_prime, components);

            // Serialize ciphertext
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeInt(aesEncryptedData.length);
                oos.write(aesEncryptedData);
                oos.write(iv); // 16 bytes
                oos.writeObject(C.toBytes());
                oos.writeObject(C_prime.toBytes());
                oos.writeInt(components.size());
                for (Ciphertext.CiphertextComponent comp : components) {
                    oos.writeObject(comp.getC_i().toBytes());
                    oos.writeObject(comp.getD_i().toBytes());
                    oos.writeObject(comp.getE_i().toBytes());
                    oos.writeUTF(comp.getAttribute());
                }
            }
            logger.info("File encrypted successfully");
            return baos.toByteArray();

        } catch (Exception e) {
            logger.error("Encryption failed", e);
            throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
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

    public AuthoritySecretKey loadSecretKey(String skFileId, Pairing pairing) {
        String filePath = STORAGE_DIR + "sk_" + skFileId + ".dat";
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            byte[] alphaBytes = (byte[]) ois.readObject();
            byte[] yBytes = (byte[]) ois.readObject();
            String authorityId = ois.readUTF();

            Field<Element> Zr = pairing.getZr();
            Element alpha = Zr.newElementFromBytes(alphaBytes).getImmutable();
            Element y = Zr.newElementFromBytes(yBytes).getImmutable();

            return new AuthoritySecretKey(alpha, y, authorityId);
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Failed to load secret key from {}", filePath, e);
            throw new RuntimeException("Failed to load secret key", e);
        }
    }

    public AuthorityPublicKey loadPublicKey(String pkFileId, Pairing pairing) {
        String filePath = STORAGE_DIR + "pk_" + pkFileId + ".dat";
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            byte[] e_g_g_alphaBytes = (byte[]) ois.readObject();
            byte[] g_yBytes = (byte[]) ois.readObject();
            String authorityId = ois.readUTF();

            Element e_g_g_alpha = pairing.getGT().newElementFromBytes(e_g_g_alphaBytes).getImmutable();
            Element g_y = pairing.getG1().newElementFromBytes(g_yBytes).getImmutable();

            return new AuthorityPublicKey(e_g_g_alpha, g_y, authorityId);
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Failed to load public key from {}", filePath, e);
            throw new RuntimeException("Failed to load public key", e);
        }
    }

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
