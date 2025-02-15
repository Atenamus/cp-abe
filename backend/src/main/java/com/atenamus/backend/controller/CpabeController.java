package com.atenamus.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atenamus.backend.Cpabe;
import com.atenamus.backend.MasterSecretKey;
import com.atenamus.backend.PublicKey;
import com.atenamus.backend.dto.MasterSecretKeyDto;
import com.atenamus.backend.dto.PublicKeyDto;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cpabe")
public class CpabeController {

    @GetMapping("/setup")
    public Map<String, Object> setup() {
        PublicKey pub = new PublicKey();
        MasterSecretKey msk = new MasterSecretKey();

        Cpabe cpabe = new Cpabe();
        cpabe.setup(pub, msk);

        PublicKeyDto pubDto = new PublicKeyDto();
        pubDto.g = pub.g.toString();
        pubDto.h = pub.h.toString();
        pubDto.f = pub.f.toString();
        pubDto.gp = pub.gp.toString();
        pubDto.g_hat_alpha = pub.g_hat_alpha.toString();

        MasterSecretKeyDto mskDto = new MasterSecretKeyDto();
        mskDto.beta = msk.beta.toString();
        mskDto.g_alpha = msk.g_alpha.toString();

        Map<String, Object> response = new HashMap<>();
        response.put("publicKey", pubDto);
        response.put("masterSecretKey", mskDto);
        response.put("message", "Setup complete! Public and master keys generated.");

        return response;
    }
}
