package org.example.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class Database {
    protected static HikariDataSource dataSource;

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    private static DataSource getDataSource() {
        if (null == dataSource) {
            System.out.println("No DataSource is available. We will create a new one.");
            createDataSource();
        }
        return dataSource;
    }

    private static void createDataSource() {
        HikariConfig hikariConfig = getHikariConfig();
        System.out.println("Configuration is ready.");
        System.out.println("Creating the HikariDataSource and assigning it as the global");
        dataSource = new HikariDataSource(hikariConfig);
    }

    private static HikariConfig getHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/sgbd");
        config.setUsername("postgres");
        config.setPassword("valea");
        config.setMaximumPoolSize(50);
        config.setAutoCommit(false);
        config.setConnectionTimeout(0);
        config.setIdleTimeout(30000);
        return config;
    }

}
