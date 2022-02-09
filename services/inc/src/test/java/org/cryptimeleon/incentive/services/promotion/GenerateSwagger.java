package org.cryptimeleon.incentive.services.promotion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * This generates a swagger json file to the api folder.
 * See https://github.com/springfox/springfox/issues/1959 for details.
 */
@SpringBootTest
public class GenerateSwagger {

    @Autowired
    WebApplicationContext context;

    @Test
    public void generateSwagger() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        mockMvc.perform(MockMvcRequestBuilders.get("/v2/api-docs").accept(MediaType.APPLICATION_JSON))
                .andDo((result) -> {
                    var swaggerPath = Paths.get("./../api/promotion.json");
                    Files.createDirectories(swaggerPath.getParent());
                    Files.writeString(swaggerPath, result.getResponse().getContentAsString(), StandardOpenOption.CREATE);
                });
    }
}
