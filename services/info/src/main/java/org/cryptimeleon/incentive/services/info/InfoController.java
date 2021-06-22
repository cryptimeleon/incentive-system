package org.cryptimeleon.incentive.services.info;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@Slf4j
public class InfoController {

    private InfoService infoService;  // Automatically injects an instance of the service

    /*
     * Endpoint for alive testing etc.
     */
    @GetMapping("/")
    public ResponseEntity<String> test() {
        return new ResponseEntity<>("Hello from Info service!", HttpStatus.OK);
    }

    @GetMapping("/public-parameters")
    public ResponseEntity<String> queryPublicParameters() {
        return new ResponseEntity<>(infoService.getSerializedPublicParameters(), HttpStatus.OK);
    }

    @GetMapping("/provider-public-key")
    public ResponseEntity<String> queryProviderPublicKey() {
        return new ResponseEntity<>(infoService.getSerializedProviderPublicKey(), HttpStatus.OK);
    }

    @GetMapping("/provider-secret-key")
    public ResponseEntity<String> queryProviderSecretKey(@RequestHeader(name = "shared-secret") String providerSharedSecret) {
        if (infoService.verifyProviderSharedSecret(providerSharedSecret)) {
            return new ResponseEntity<>(infoService.getSerializedProviderSecretKey(), HttpStatus.OK);
        }
        return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
    }
}