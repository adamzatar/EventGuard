package com.azaatar.eventguard.persistence.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class DriverManagerConnectionProvider implements ConnectionProvider {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public DriverManagerConnectionProvider(String jdbcUrl, String username, String password) {
        this.jdbcUrl = validateNotBlank(jdbcUrl, "jdbcUrl");
        this.username = validateNotBlank(username, "username");
        this.password = Objects.requireNonNull(password, "password must not be null");
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    private static String validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
        return value.trim();
    }
}