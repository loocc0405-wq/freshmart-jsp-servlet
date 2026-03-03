-- FreshMart reference schema (SQL Server)
-- Run in SSMS / Azure Data Studio

-- Create DB if not exists
IF DB_ID(N'freshmart') IS NULL
BEGIN
    CREATE DATABASE freshmart;
END
GO

USE freshmart;
GO

-- Optional: set collation / unicode defaults if you need
-- ALTER DATABASE freshmart COLLATE Vietnamese_100_CI_AS;
-- GO

-- USERS
IF OBJECT_ID(N'dbo.users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        username NVARCHAR(50) NOT NULL,
        password_hash NVARCHAR(100) NOT NULL,
        role NVARCHAR(20) NOT NULL,
        tier NVARCHAR(10) NOT NULL CONSTRAINT df_users_tier DEFAULT N'FREE',
        expired_date DATE NULL,
        full_name NVARCHAR(100) NULL,
        phone NVARCHAR(20) NULL,
        address NVARCHAR(255) NULL,
        active BIT NOT NULL CONSTRAINT df_users_active DEFAULT (1),
        created_at DATETIME2(0) NOT NULL CONSTRAINT df_users_created_at DEFAULT (SYSDATETIME()),
        CONSTRAINT uq_users_username UNIQUE (username)
    );
END
GO

-- SUPPLIERS
IF OBJECT_ID(N'dbo.suppliers', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.suppliers (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        name NVARCHAR(120) NOT NULL,
        phone NVARCHAR(20) NULL,
        address NVARCHAR(255) NULL,
        certificate NVARCHAR(255) NULL,
        lead_time_days INT NULL CONSTRAINT df_suppliers_lead_time DEFAULT (1),
        note NVARCHAR(255) NULL
    );
END
GO

-- ---- SUPPLIERS (schema evolution) ----
-- Add email column if missing (matches Supplier.email NOT NULL in JPA)
IF COL_LENGTH(N'dbo.suppliers', N'email') IS NULL
BEGIN
    ALTER TABLE dbo.suppliers ADD email NVARCHAR(120) NULL;

    -- Backfill existing rows then enforce NOT NULL
    UPDATE dbo.suppliers
    SET email = CONCAT(N'supplier', id, N'@freshmart.local')
    WHERE email IS NULL;

    ALTER TABLE dbo.suppliers ALTER COLUMN email NVARCHAR(120) NOT NULL;
END
GO
-- PRODUCTS
IF OBJECT_ID(N'dbo.products', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.products (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        name NVARCHAR(150) NOT NULL,
        category NVARCHAR(50) NULL,
        unit NVARCHAR(20) NULL,
        sell_price DECIMAL(18,2) NOT NULL,
        image_url NVARCHAR(500) NULL,
        description NVARCHAR(MAX) NULL
    );

    CREATE INDEX idx_products_category ON dbo.products(category);
END
GO

-- PRODUCT_LOTS
IF OBJECT_ID(N'dbo.product_lots', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.product_lots (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        product_id BIGINT NOT NULL,
        supplier_id BIGINT NULL,
        import_date DATE NOT NULL,
        expiry_date DATE NOT NULL,
        qty_in INT NOT NULL,
        qty_left INT NOT NULL,
        import_price DECIMAL(18,2) NULL,
        CONSTRAINT fk_lots_product FOREIGN KEY (product_id) REFERENCES dbo.products(id),
        CONSTRAINT fk_lots_supplier FOREIGN KEY (supplier_id) REFERENCES dbo.suppliers(id)
    );

    CREATE INDEX idx_lots_product_expiry ON dbo.product_lots(product_id, expiry_date);
END
GO

-- CARTS
IF OBJECT_ID(N'dbo.carts', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.carts (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        user_id BIGINT NOT NULL,
        created_at DATETIME2(0) NOT NULL CONSTRAINT df_carts_created_at DEFAULT (SYSDATETIME()),
        CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES dbo.users(id)
    );
END
GO

-- CART_ITEMS
IF OBJECT_ID(N'dbo.cart_items', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.cart_items (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        cart_id BIGINT NOT NULL,
        product_id BIGINT NOT NULL,
        quantity INT NOT NULL,
        CONSTRAINT uq_cart_items_cart_product UNIQUE (cart_id, product_id),
        CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES dbo.carts(id),
        CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES dbo.products(id)
    );
END
GO

-- ORDERS
IF OBJECT_ID(N'dbo.orders', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.orders (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        order_code NVARCHAR(30) NOT NULL,
        customer_id BIGINT NULL,
        created_by BIGINT NULL,
        type NVARCHAR(20) NOT NULL,
        status NVARCHAR(20) NOT NULL,
        payment_method NVARCHAR(30) NULL,
        total_amount DECIMAL(18,2) NOT NULL CONSTRAINT df_orders_total_amount DEFAULT (0),
        created_at DATETIME2(0) NOT NULL CONSTRAINT df_orders_created_at DEFAULT (SYSDATETIME()),
        completed_at DATETIME2(0) NULL,
        CONSTRAINT uq_orders_order_code UNIQUE (order_code),
        CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES dbo.users(id),
        CONSTRAINT fk_orders_created_by FOREIGN KEY (created_by) REFERENCES dbo.users(id)
    );

    CREATE INDEX idx_orders_status ON dbo.orders(status);
    CREATE INDEX idx_orders_created_at ON dbo.orders(created_at);
END
GO

-- ORDER_ITEMS
IF OBJECT_ID(N'dbo.order_items', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.order_items (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        order_id BIGINT NOT NULL,
        product_id BIGINT NOT NULL,
        quantity INT NOT NULL,
        unit_price DECIMAL(18,2) NOT NULL,
        line_total DECIMAL(18,2) NOT NULL,
        CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES dbo.orders(id),
        CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES dbo.products(id)
    );

    CREATE INDEX idx_order_items_order ON dbo.order_items(order_id);
END
GO

-- REVENUE_DAILY
IF OBJECT_ID(N'dbo.revenue_daily', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.revenue_daily (
        revenue_date DATE NOT NULL PRIMARY KEY,
        total_revenue DECIMAL(18,2) NOT NULL CONSTRAINT df_revenue_daily_total DEFAULT (0)
    );
END
GO
