package com.bank.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for Config Server Application.
 * Verifies that the application context loads correctly.
 */
@SpringBootTest
@ActiveProfiles("test")
class ConfigServerApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that Spring context loads successfully
    }
}
