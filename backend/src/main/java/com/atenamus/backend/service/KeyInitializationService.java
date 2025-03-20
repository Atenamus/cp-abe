package com.atenamus.backend.service;

import java.io.File;
import java.nio.file.Files;
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

    private static final String PUB_KEY_FILE = "public_key.dat";
    private static final String MSK_KEY_FILE = "master_secret_key.dat";

    @Autowired
    private Cpabe cpabe;

    /**
     * Checks if the public and master secret keys exist and generates them if
     * either is missing. This method is called when the application is ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeKeys() {
        try {
            boolean pubKeyExists = Files.exists(Paths.get(PUB_KEY_FILE));
            boolean mskExists = Files.exists(Paths.get(MSK_KEY_FILE));

            if (!pubKeyExists || !mskExists) {
                System.out.println("CP-ABE key files missing. Generating new keys...");
                System.out.println("Public key exists: " + pubKeyExists);
                System.out.println("Master secret key exists: " + mskExists);

                // Generate new keys
                PublicKey pub = new PublicKey();
                MasterSecretKey msk = new MasterSecretKey();

                // Use the setup method to generate a new key pair
                cpabe.setup(pub, msk);

                // Serialize and save the keys
                byte[] pubBytes = SerializeUtil.serializePublicKey(pub);
                FileUtil.writeFile(PUB_KEY_FILE, pubBytes);

                byte[] mskBytes = SerializeUtil.serializeMasterSecretKey(msk);
                FileUtil.writeFile(MSK_KEY_FILE, mskBytes);

                System.out.println("Successfully generated new CP-ABE keys.");
            } else {
                System.out.println("CP-ABE keys already exist. Skipping key generation.");
            }
        } catch (Exception e) {
            System.err.println("Error initializing CP-ABE keys: " + e.getMessage());
            // e.printStackTrace();
        }
    }
}