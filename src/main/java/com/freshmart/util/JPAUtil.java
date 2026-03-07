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
        // perform minimal schema adjustment before Hibernate validates
        try {
            // load driver (redundant with hibernate but safe)
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                    "jdbc:sqlserver://localhost:1433;databaseName=freshmart;encrypt=true;trustServerCertificate=true",
                    "sa", "123456")) {
                java.sql.ResultSet rs = conn.prepareStatement(
                        "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                                "WHERE TABLE_NAME='products' AND COLUMN_NAME='active'")
                        .executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    conn.createStatement().execute(
                            "ALTER TABLE products ADD active bit NOT NULL CONSTRAINT df_products_active DEFAULT (1)");
                }
            }
        } catch (Exception e) {
            // log but continue; if this fails and schema is missing, validation will catch it
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
}
