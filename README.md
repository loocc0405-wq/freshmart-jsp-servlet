# FreshMart - Fresh Food Management & Hybrid AI Revenue Forecasting

Tech stack (as requested in the assignment):
- Java + JSP/Servlet (MVC)
- JPA (Hibernate)
- SQL Server (default)
- Filters: Authentication / Authorization / Tier(PRO) gating
- Chart.js (Actual vs Forecast)
- Hybrid AI Forecast Engine: deterministic forecasting + Gemini narrative layer

This starter project implements the **core architecture** and several key business rules:
- Role-based access control: **ADMIN / STAFF / SELLER / CUSTOMER**
- Tier gating: **FREE / PRO** (PRO features are in `/pro/*`)
- Inventory by lot (batch) with FEFO deduction when an order is **COMPLETED**
- Revenue aggregation into `revenue_daily` when an order is **COMPLETED**
- Forecasting utilities: Moving Average & Exponential Smoothing
- **AI Engine dự báo doanh thu** với:
  - lịch sử đơn hàng
  - biến động giá nhập
  - trạng thái tồn kho
  - sự kiện marketing
  - mùa vụ + proxy thời tiết theo tháng
  - khoảng tin cậy, procurement plan và margin warning

## Quick start

1. Create DB and update DB credentials in:
   - `src/main/resources/META-INF/persistence.xml`

   You can create the database/tables using:
   - `db/schema-sqlserver.sql` (SQL Server)

2. (Optional) Configure Gemini key for narrative AI layer:
```bash
export GEMINI_API_KEY=your_api_key_here
```
If no API key is configured, the AI forecast endpoint still works in **hybrid deterministic mode**.

3. Build WAR:
```bash
mvn clean package
```

4. Deploy `target/freshmart.war` to **Tomcat 10.1+** (copy into `TOMCAT_HOME/webapps/`), then start Tomcat.

> ⚠️ Lưu ý: Project đang dùng `jakarta.servlet-api 6.0` + JSTL Jakarta → **bắt buộc Tomcat 10.1+**. Nếu muốn chạy Tomcat 9 (javax.*) thì phải hạ dependency và đổi toàn bộ imports/taglib.

5. Open:
- `http://localhost:8080/freshmart/`

## AI forecast API

- `GET /api/ai/forecast?period=month`
- `POST /api/ai/forecast`

Example body:
```json
{
  "period": "quarter",
  "productId": null
}
```

Response returns a markdown forecast report with:
- quantitative forecast
- confidence interval
- procurement plan
- profit margin warnings
- seasonality insights
- preprocessing/modeling summary

## Default accounts (created on startup)

See `com.freshmart.bootstrap.AppBootstrapListener`.
- admin / admin123
- staff / staff123
- seller / seller123
- customer / customer123

## Folder structure

- `src/main/java` : controllers (servlets), filters, services, repositories, entities
- `src/main/webapp/WEB-INF/jsp` : JSP views
- `src/main/resources/META-INF/persistence.xml` : JPA config (**SQL Server default**)
- `src/main/resources/META-INF/persistence-sqlserver.xml` : SQL Server config (reference)
- `db/schema-sqlserver.sql` : reference schema (SQL Server)

## Optional: Run with MySQL instead of SQL Server

If you want to switch back to MySQL:
1) Change `src/main/resources/META-INF/persistence.xml` to use MySQL driver + URL + dialect.
2) In `pom.xml`, replace the SQL Server driver with MySQL Connector/J.
3) Use `db/schema.sql`.
