package org.cryptimeleon.incentive.services.store;

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

/*
 * This test case generates the swagger api using springfox and stores it to the api folder.
 * https://github.com/springfox/springfox/issues/1959
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
                    var swaggerPath = Paths.get("./../api/basket.json");
                    Files.createDirectories(swaggerPath.getParent());
                    Files.writeString(swaggerPath, result.getResponse().getContentAsString(), StandardOpenOption.CREATE);
                });
    }
}
