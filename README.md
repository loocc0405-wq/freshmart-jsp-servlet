# FreshMart - Fresh Food Management & Revenue Forecasting (No AI)

Tech stack (as requested in the assignment):
- Java + JSP/Servlet (MVC)
- JPA (Hibernate)
- SQL Server (default)
- Filters: Authentication / Authorization / Tier(PRO) gating
- Chart.js (Actual vs Forecast)

This starter project implements the **core architecture** and several key business rules:
- Role-based access control: **ADMIN / STAFF / SELLER / CUSTOMER**
- Tier gating: **FREE / PRO** (PRO features are in `/pro/*`)
- Inventory by lot (batch) with FEFO deduction when an order is **COMPLETED**
- Revenue aggregation into `revenue_daily` when an order is **COMPLETED**
- Basic forecasting methods (no AI): Moving Average & Exponential Smoothing

> Note: This repository is a clean, professional baseline. You can extend CRUD screens for suppliers/products/lots, add carts, OMS workflow, fake payment, etc.

## Quick start

1. Create DB and update DB credentials in:
   - `src/main/resources/META-INF/persistence.xml`

   You can create the database/tables using:
   - `db/schema-sqlserver.sql` (SQL Server)

2. Build WAR:
```bash
mvn clean package
```

3. Deploy `target/freshmart.war` to **Tomcat 9** (copy into `TOMCAT_HOME/webapps/`), then start Tomcat.

4. Open:
- `http://localhost:8080/freshmart/`

## Default accounts (created on startup)

See `com.freshmart.bootstrap.AppBootstrapListener`.
- admin / admin123
- staff / staff123
- seller / seller123
- customer / customer123

## Folder structure

- `src/main/java` : controllers (servlets), filters, services, repositories, entities
- `src/main/webapp/WEB-INF/jsp` : JSP views
- `src/main/resources/META-INF/persistence.xml` : JPA config (MySQL)
- `src/main/resources/META-INF/persistence-sqlserver.xml` : SQL Server config (reference)
- `db/schema.sql` : reference schema (MySQL)
- `db/schema-sqlserver.sql` : reference schema (SQL Server)

## Optional: Run with MySQL instead of SQL Server

If you want to switch back to MySQL:
1) Change `src/main/resources/META-INF/persistence.xml` to use MySQL driver + URL + dialect.
2) In `pom.xml`, replace the SQL Server driver with MySQL Connector/J.
3) Use `db/schema.sql`.

