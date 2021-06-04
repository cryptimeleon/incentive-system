package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DsprotectionserviceController {
    @RequestMapping("/")
    public String hello()
    {
        return "Finally it works. Great!";
    }
}
