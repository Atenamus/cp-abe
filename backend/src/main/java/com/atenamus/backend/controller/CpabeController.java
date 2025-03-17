package com.atenamus.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.atenamus.backend.models.ElementBoolean;
import com.atenamus.backend.models.MasterSecretKey;
import com.atenamus.backend.models.PrivateKey;
import com.atenamus.backend.models.PublicKey;
import com.atenamus.backend.util.AESCoder;
import com.atenamus.backend.util.FileUtil;
import com.atenamus.backend.util.SerializeUtil;
import it.unisa.dia.gas.jpbc.Element;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
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

    @Autowired
    private Cpabe cpabe;

    @GetMapping("/setup")
    public Map<String, Object> setup() {
        Map<String, Object> response = new HashMap<>();

        try {
            PublicKey pub = new PublicKey();
            MasterSecretKey msk = new MasterSecretKey();

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

            byte[] plaintext = FileUtil.readFile(DATA_FILE);
            System.out.println("Plaintext length: " + plaintext.length);

            byte[] pubBytes = FileUtil.readFile(PUB_KEY_FILE);
            PublicKey pub = SerializeUtil.unserializePublicKey(pubBytes);

            System.out.println("After preprocessing: " + policy);

            CipherKey cipherKey = cpabe.encrypt(pub, policy);
            Cipher cph = cipherKey.cph;
            Element symmetric_key = cipherKey.key;

            System.out.println("Generated symmetric key: " + symmetric_key.toString());
            byte[] symmetricKeyBytes = symmetric_key.toBytes();
            System.out.println("Symmetric key bytes length: " + symmetricKeyBytes.length);
            System.out.println("Symmetric key bytes hash: " + Arrays.hashCode(symmetricKeyBytes));

            if (cph == null) {
                response.put("error", "An error occurred during encryption");
                return response;
            }

            // Store the symmetric key bytes - this is what we'll retrieve during decryption
            byte[] storedKeyBytes = symmetricKeyBytes;

            // Serialize the CP-ABE ciphertext
            byte[] cphBuf = SerializeUtil.serializeCipher(cph);
            System.out.println("CP-ABE ciphertext length: " + cphBuf.length);

            // Encrypt the plaintext data using the symmetric key
            byte[] encryptedData = AESCoder.encrypt(symmetricKeyBytes, plaintext);
            System.out.println("AES encrypted data length: " + encryptedData.length);

            // Store the CP-ABE ciphertext, symmetric key bytes, and AES-encrypted data
            FileUtil.writeFullCpabeFile(ENCRYPTED_DATA_FILE, cphBuf, storedKeyBytes, encryptedData);

            response.put("message", "Data encrypted successfully");
            response.put("policy", policy);
            response.put("encryptedDataLength", encryptedData.length);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "An error occurred during encryption: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/decrypt")
    public Map<String, Object> decrypt() {
        Map<String, Object> response = new HashMap<>();
        try {
            byte[] plaintext;

            byte[] pubBytes = FileUtil.readFile(PUB_KEY_FILE);
            PublicKey pub = SerializeUtil.unserializePublicKey(pubBytes);

            byte[][] encryptedData = FileUtil.readFullCpabeFile(ENCRYPTED_DATA_FILE);
            byte[] encryptedDataBuf = encryptedData[0]; // AES encrypted data
            byte[] storedKeyBytes = encryptedData[1]; // Stored symmetric key bytes
            byte[] cphBuf = encryptedData[2]; // CP-ABE ciphertext

            System.out.println("AES encrypted data length: " + encryptedDataBuf.length);
            System.out.println("Stored key bytes length: " + storedKeyBytes.length);
            System.out.println("Stored key bytes hash: " + Arrays.hashCode(storedKeyBytes));
            System.out.println("CP-ABE ciphertext length: " + cphBuf.length);

            Cipher cipher = SerializeUtil.unserializeCipher(pub, cphBuf);

            byte[] prvBytes = FileUtil.readFile(PRV_KEY_FILE);
            PrivateKey prv = SerializeUtil.unserializePrivateKey(pub, prvBytes);

            // Verify policy satisfaction with CP-ABE (but we'll use the stored key for
            // decryption)
            ElementBoolean result = cpabe.decrypt(pub, prv, cipher);
            System.out.println("CP-ABE decryption result: " + result.satisfy);

            if (result.satisfy) {
                // Use the originally stored symmetric key bytes for decryption
                try {
                    plaintext = AESCoder.decrypt(storedKeyBytes, encryptedDataBuf);
                    response.put("message", "Data decrypted successfully");
                    response.put("decryptedData", new String(plaintext));
                } catch (Exception e) {
                    System.err.println("AES decryption failed: " + e.getMessage());
                    e.printStackTrace();
                    response.put("error", "AES decryption failed: " + e.getMessage());
                }
            } else {
                response.put("error", "Policy not satisfied during decryption");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "An error occurred during decryption: " + e.getMessage());
        }

        return response;
    }

}
