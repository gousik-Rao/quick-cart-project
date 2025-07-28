package com.ecommerce.project;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = QuickCartApplication.class)
class QuickCartApplicationTests {

    @Test
    void contextLoads() {
    }

}
