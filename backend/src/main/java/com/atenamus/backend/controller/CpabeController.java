package com.atenamus.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atenamus.backend.Cpabe;
import com.atenamus.backend.dto.MasterSecretKeyDto;
import com.atenamus.backend.dto.PublicKeyDto;
import com.atenamus.backend.models.Cipher;
import com.atenamus.backend.models.CipherKey;
import com.atenamus.backend.models.MasterSecretKey;
import com.atenamus.backend.models.PrivateKey;
import com.atenamus.backend.models.PublicKey;
import com.atenamus.backend.util.AESCoder;
import com.atenamus.backend.util.FileUtil;
import com.atenamus.backend.util.SerializeUtil;
import it.unisa.dia.gas.jpbc.Element;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cpabe")
public class CpabeController {

    private static final String PUB_KEY_FILE = "public_key.dat";
    private static final String MSK_KEY_FILE = "master_secret_key.dat";
    private static final String PRV_KEY_FILE = "private_key.dat";
    private static final String DATA_FILE = "data.txt";
    private static final String ENCRYPTED_DATA_FILE = "data.txt.cpabe";

    @GetMapping("/setup")
    public Map<String, Object> setup() {
        Map<String, Object> response = new HashMap<>();

        try {
            PublicKey pub = new PublicKey();
            MasterSecretKey msk = new MasterSecretKey();

            Cpabe cpabe = new Cpabe();
            cpabe.setup(pub, msk);

            byte[] pubBytes = SerializeUtil.serializePublicKey(pub);
            FileUtil.writeFile(PUB_KEY_FILE, pubBytes);

            byte[] mskBytes = SerializeUtil.serializeMasterSecretKey(msk);
            FileUtil.writeFile(MSK_KEY_FILE, mskBytes);

            PublicKeyDto pubDto = new PublicKeyDto();
            pubDto.g = pub.g.toString();
            pubDto.h = pub.h.toString();
            pubDto.f = pub.f.toString();
            pubDto.gp = pub.gp.toString();
            pubDto.g_hat_alpha = pub.g_hat_alpha.toString();

            MasterSecretKeyDto mskDto = new MasterSecretKeyDto();
            mskDto.beta = msk.beta.toString();
            mskDto.g_alpha = msk.g_alpha.toString();

            response.put("publicKey", pubDto);
            response.put("masterSecretKey", mskDto);
            response.put("message", "Setup complete! Public and master keys generated.");

        } catch (IOException e) {
            // System.err.println("Error during CP-ABE setup: " + e.getMessage());
            response.put("error", "An error occurred during CP-ABE setup. Please try again.");
        }

        return response;
    }

    @PostMapping("/keygen")
    public Map<String, Object> keygen(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            String[] attributes = ((List<String>) request.get("attributes")).toArray(new String[0]);

            byte[] pubBytes = FileUtil.readFile(PUB_KEY_FILE);
            PublicKey pub = SerializeUtil.unserializePublicKey(pubBytes);

            byte[] mskBytes = FileUtil.readFile(MSK_KEY_FILE);
            MasterSecretKey msk = SerializeUtil.unserializeMasterSecretKey(pub, mskBytes);

            Cpabe cpabe = new Cpabe();
            PrivateKey prv = cpabe.keygen(pub, msk, attributes);

            byte[] prvBytes = SerializeUtil.serializePrivateKey(prv);
            FileUtil.writeFile(PRV_KEY_FILE, prvBytes);

            response.put("message", "Private key generated successfully.");
            response.put("attributes", attributes);
        } catch (IOException | NoSuchAlgorithmException e) {
            response.put("error",
                    "An error occurred during private key generation: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/encrypt")
    public Map<String, Object> encrypt(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String policy = (String) request.get("policy");

            // Read data from file
            byte[] plaintext = FileUtil.readFile(DATA_FILE);

            // Read public key from file
            byte[] pubBytes = FileUtil.readFile(PUB_KEY_FILE);
            PublicKey pub = SerializeUtil.unserializePublicKey(pubBytes);

            Cpabe cpabe = new Cpabe();

            CipherKey cipherKey = cpabe.encrypt(pub, policy);
            Cipher cph = cipherKey.cph;
            Element symmetric_key = cipherKey.key;

            if (cph == null) {
                response.put("error", "An error occurred during encryption");
                return response;
            }

            byte[] aesBuf = AESCoder.encrypt(symmetric_key.toBytes(), plaintext);

            response.put("message", "Data encrypted successfully");
            response.put("policy", policy);
            response.put("encryptedData", aesBuf);
            FileUtil.writeFile(ENCRYPTED_DATA_FILE, aesBuf);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "An error occurred during encryption: " + e.getMessage());
        }

        return response;
    }

}
