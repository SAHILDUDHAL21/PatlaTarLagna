package com.patlatarlagna;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test to verify Spring context loads successfully with H2 test database.
 */
@SpringBootTest
@ActiveProfiles("test")
class PatlatarlagnaApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that Spring context starts without errors
    }
}
