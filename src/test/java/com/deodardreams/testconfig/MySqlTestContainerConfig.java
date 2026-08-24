package com.deodardreams.testconfig;

/**
 * Provides a reusable MySQL Testcontainer for integration/repository tests.
 *
 * The container runs the same MySQL database engine used by the application
 * instead of an in-memory database such as H2, giving tests behavior closer
 * to the real production database.
 */

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

@TestConfiguration // Marks this class as test-only Spring configuration; it is loaded only for test contexts.
public class MySqlTestContainerConfig {

    /**
     * Starts a temporary MySQL 8.4 container before the test context is used.
     *
     * Testcontainers assigns the container's port and connection details
     * dynamically. The container is managed as a Spring bean so test classes
     * can reuse the same database configuration.
     */

    @Bean
    @ServiceConnection // Tells Spring Boot to automatically use this container's connection details for the test application's DataSource.
    MySQLContainer mySQLContainer(){

        MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.4");
        mySQLContainer.start();
        return mySQLContainer;
    }
}
