package com.freshmart.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class JPAUtil {

    private static final String URL =
            "jdbc:sqlserver://localhost:1433;databaseName=freshmart;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASSWORD = "123456";

    private static final EntityManagerFactory EMF;

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                ensureColumnExists(
                        conn,
                        "products",
                        "active",
                        "ALTER TABLE products ADD active bit NOT NULL CONSTRAINT df_products_active DEFAULT (1)"
                );

                ensureColumnExists(
                        conn,
                        "users",
                        "gender",
                        "ALTER TABLE users ADD gender NVARCHAR(10) NULL"
                );

                ensureColumnExists(
                        conn,
                        "users",
                        "dob",
                        "ALTER TABLE users ADD dob DATE NULL"
                );

                ensureTableExists(
                        conn,
                        "app_settings",
                        "CREATE TABLE app_settings (" +
                                "id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, " +
                                "setting_key NVARCHAR(100) NOT NULL, " +
                                "setting_value NVARCHAR(255) NOT NULL, " +
                                "description NVARCHAR(255) NULL, " +
                                "updated_at DATETIME2(0) NOT NULL DEFAULT (SYSDATETIME()), " +
                                "CONSTRAINT uq_app_settings_setting_key UNIQUE (setting_key)" +
                                ")"
                );

                ensureTableExists(
                        conn,
                        "subscription_payments",
                        "CREATE TABLE subscription_payments (" +
                                "id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, " +
                                "user_id BIGINT NOT NULL, " +
                                "payment_code NVARCHAR(30) NOT NULL, " +
                                "plan_name NVARCHAR(50) NOT NULL, " +
                                "plan_days INT NOT NULL, " +
                                "amount DECIMAL(18,2) NOT NULL DEFAULT (0), " +
                                "payment_method NVARCHAR(30) NOT NULL, " +
                                "payment_status NVARCHAR(20) NOT NULL, " +
                                "start_date DATE NOT NULL, " +
                                "end_date DATE NOT NULL, " +
                                "note NVARCHAR(255) NULL, " +
                                "created_at DATETIME2(0) NOT NULL DEFAULT (SYSDATETIME()), " +
                                "CONSTRAINT uq_subscription_payments_code UNIQUE (payment_code), " +
                                "CONSTRAINT fk_subscription_payments_user FOREIGN KEY (user_id) REFERENCES users(id)" +
                                ")"
                );
            }
        } catch (Exception e) {
            System.err.println("schema pre-check failed: " + e.getMessage());
        }

        EMF = Persistence.createEntityManagerFactory("freshmartPU");
    }

    private JPAUtil() {
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return EMF;
    }

    public static EntityManager createEntityManager() {
        return EMF.createEntityManager();
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void shutdown() {
        if (EMF.isOpen()) {
            EMF.close();
        }
    }

    private static void ensureColumnExists(Connection conn,
                                           String tableName,
                                           String columnName,
                                           String alterSql) throws SQLException {
        try (java.sql.PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?")) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (java.sql.Statement st = conn.createStatement()) {
                        st.execute(alterSql);
                    }
                }
            }
        }
    }

    private static void ensureTableExists(Connection conn,
                                          String tableName,
                                          String createSql) throws SQLException {
        try (java.sql.PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?")) {
            ps.setString(1, tableName);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (java.sql.Statement st = conn.createStatement()) {
                        st.execute(createSql);
                    }
                }
            }
        }
    }
}