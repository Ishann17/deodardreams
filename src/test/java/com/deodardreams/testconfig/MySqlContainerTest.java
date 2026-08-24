package com.deodardreams.testconfig;

/**
 * Verifies that Spring Boot can start a test context using
 * the MySQL Test container as its database connection.
 */

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.testcontainers.mysql.MySQLContainer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest //It tells Spring boot to - Starts the full Spring Boot application context for this integration test.
@Import(MySqlTestContainerConfig.class) // Adds our MySQL Test container configuration to the Spring test context.
public class MySqlContainerTest {

    /*
     * ApplicationContext is Spring's central container that creates and manages
     * application components such as repositories, services and configurations.
     *
     * We inject it here only to verify that the complete Spring test context
     * was successfully created with the Test container database.
     */

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldStartSpringContextWithMySqlContainer() {
        /*
         * If Spring cannot initialize the DataSource, JPA, repositories or any
         * other required application component, the test fails before reaching
         * this assertion.
         *
         * A non-null context therefore confirms that the Spring test environment
         * started successfully with the Test container configuration.
         */
        assertNotNull(applicationContext);
    }
}
