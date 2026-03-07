package com.freshmart.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Minimal JPA utility (RESOURCE_LOCAL).
 * In production you'd usually use a DataSource + JTA, but for PRJ-style projects this is enough.
 */
public final class JPAUtil {

    private static final EntityManagerFactory EMF;

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                    "jdbc:sqlserver://localhost:1433;databaseName=freshmart;encrypt=true;trustServerCertificate=true",
                    "sa", "123456")) {

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
            }
        } catch (Exception e) {
            System.err.println("schema pre-check failed: " + e.getMessage());
        }

        EMF = Persistence.createEntityManagerFactory("freshmartPU");
    }

    private JPAUtil() {}

    public static EntityManagerFactory getEntityManagerFactory() {
        return EMF;
    }

    public static EntityManager createEntityManager() {
        return EMF.createEntityManager();
    }

    public static void shutdown() {
        if (EMF.isOpen()) {
            EMF.close();
        }
    }

    private static void ensureColumnExists(java.sql.Connection conn,
                                           String tableName,
                                           String columnName,
                                           String alterSql) throws java.sql.SQLException {
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
}