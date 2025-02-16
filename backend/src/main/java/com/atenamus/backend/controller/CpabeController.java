package com.atenamus.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atenamus.backend.Cpabe;
import com.atenamus.backend.MasterSecretKey;
import com.atenamus.backend.PublicKey;
import com.atenamus.backend.dto.MasterSecretKeyDto;
import com.atenamus.backend.dto.PublicKeyDto;
import com.atenamus.backend.util.FileUtil;
import com.atenamus.backend.util.SerializeUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cpabe")
public class CpabeController {

    private static final String PUB_KEY_FILE = "public_key.dat";
    private static final String MSK_KEY_FILE = "master_secret_key.dat";

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
}
