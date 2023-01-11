package org.cryptimeleon.incentive.services.incentive;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class StoreController{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StoreController.class);
    private final StoreService storeService;
    @Value("${incentive-service.provider-secret}")
    private String providerSecret;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    /**
     * HTTP endpoint for alive testing etc.
     */
    @GetMapping("/")
    public ResponseEntity<String> test() {
        return new ResponseEntity<>("Hello from store!", HttpStatus.OK);
    }

    @GetMapping("/register")
    public String register(String userPublicKey, String userIdentification) {
        // TODO verify data
        return ""; // TODO sign user data, return serialized signature
    }
}
