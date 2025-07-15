package com.ecommerce.project;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = SbEcomApplication.class)
class SbEcomApplicationTests {

    @Test
    void contextLoads() {
    }

}
