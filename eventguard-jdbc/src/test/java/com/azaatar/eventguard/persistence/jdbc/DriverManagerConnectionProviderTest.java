package com.azaatar.eventguard.persistence.jdbc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DriverManagerConnectionProviderTest {

    @Test
    public void givenValidConfigurationWhenCreatedThenDoesNotThrow() {

        // Arrange
        String jdbcUrl = "jdbc:postgresql://localhost:5432/eventguard_dev";
        String username = "postgres";
        String password = "password";

        // Act and Assert
        assertDoesNotThrow(() -> new DriverManagerConnectionProvider(jdbcUrl, username, password));

    }

    @Test
    public void givenBlankJdbcUrlWhenCreatedThenThrowsIllegalArgumentException() {

        // Arrange
        String jdbcUrl = "    ";
        String username = "postgres";
        String password = "password";

        // Act and Assert
        assertThrows(IllegalArgumentException.class,
                () -> new DriverManagerConnectionProvider(jdbcUrl, username, password));
    }

    @Test
    public void givenBlankUsernameWhenCreatedThenThrowsIllegalArgumentException() {

        // Arrange
        String jdbcUrl = "jdbc:postgresql://localhost:5432/eventguard_dev";
        String username = "     ";
        String password = "password";

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> new DriverManagerConnectionProvider(jdbcUrl, username, password));
    }

    @Test
    public void givenNullPasswordWhenCreatedThenThrowsIllegalArgumentException() {

        // Arrange
        String jdbcUrl = "jdbc:postgresql://localhost:5432/eventguard_dev";
        String username = "postgres";
        String password = null;

        // Act and Assert
        assertThrows(NullPointerException.class, () -> new DriverManagerConnectionProvider(jdbcUrl, username, password));
    }

}

