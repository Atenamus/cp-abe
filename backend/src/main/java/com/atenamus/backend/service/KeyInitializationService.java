package com.atenamus.backend.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.atenamus.backend.Cpabe;
import com.atenamus.backend.models.MasterSecretKey;
import com.atenamus.backend.models.PublicKey;
import com.atenamus.backend.util.FileUtil;
import com.atenamus.backend.util.SerializeUtil;

@Service
public class KeyInitializationService {
    private static final String KEYS_DIR = "keys";
    private static final String PUB_KEY_FILE = "public_key.dat";
    private static final String MSK_KEY_FILE = "master_secret_key.dat";

    private final Path keysDirectoryPath;
    private final Path publicKeyPath;
    private final Path masterKeyPath;

    @Autowired
    private Cpabe cpabe;

    private PublicKey pub;
    private MasterSecretKey msk;

    public KeyInitializationService() {
        // Get the absolute path to the project root directory
        Path projectRoot = Paths.get("").toAbsolutePath();
        this.keysDirectoryPath = projectRoot.resolve(KEYS_DIR);
        this.publicKeyPath = keysDirectoryPath.resolve(PUB_KEY_FILE);
        this.masterKeyPath = keysDirectoryPath.resolve(MSK_KEY_FILE);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        try {
            // Create keys directory if it doesn't exist
            Files.createDirectories(keysDirectoryPath);

            // Check if keys exist, if not generate them
            boolean pubKeyExists = Files.exists(publicKeyPath);
            boolean mskExists = Files.exists(masterKeyPath);

            if (!pubKeyExists || !mskExists) {
                initializeKeys();
            } else {
                // Load existing keys
                byte[] pubBytes = Files.readAllBytes(publicKeyPath);
                pub = SerializeUtil.unserializePublicKey(pubBytes);

                byte[] mskBytes = Files.readAllBytes(masterKeyPath);
                msk = SerializeUtil.unserializeMasterSecretKey(pub, mskBytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializeKeys() throws Exception {
        System.out.println("Initializing CP-ABE keys...");
        pub = new PublicKey();
        msk = new MasterSecretKey();
        cpabe.setup(pub, msk);

        byte[] pubBytes = SerializeUtil.serializePublicKey(pub);
        byte[] mskBytes = SerializeUtil.serializeMasterSecretKey(msk);

        Files.write(publicKeyPath, pubBytes);
        Files.write(masterKeyPath, mskBytes);
        System.out.println("CP-ABE keys initialized successfully in: " + keysDirectoryPath);
    }

    public Path getPublicKeyPath() {
        return publicKeyPath;
    }

    public Path getMasterKeyPath() {
        return masterKeyPath;
    }

    public Path getKeysDirectoryPath() {
        return keysDirectoryPath;
    }
}