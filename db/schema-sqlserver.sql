-- FreshMart reference schema (SQL Server)
-- Run in SSMS / Azure Data Studio

IF DB_ID(N'freshmart') IS NULL
BEGIN
    CREATE DATABASE freshmart;
END
GO

USE freshmart;
GO

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
        gender NVARCHAR(10) NULL,
        dob DATE NULL,
        phone NVARCHAR(20) NULL,
        address NVARCHAR(255) NULL,
        active BIT NOT NULL CONSTRAINT df_users_active DEFAULT (1),
        created_at DATETIME2(0) NOT NULL CONSTRAINT df_users_created_at DEFAULT (SYSDATETIME()),
        CONSTRAINT uq_users_username UNIQUE (username)
    );
END
GO

-- migration: ensure gender exists
IF COL_LENGTH('users', 'gender') IS NULL
BEGIN
    ALTER TABLE users ADD gender NVARCHAR(10) NULL;
END
GO

-- migration: ensure dob exists
IF COL_LENGTH('users', 'dob') IS NULL
BEGIN
    ALTER TABLE users ADD dob DATE NULL;
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
        note NVARCHAR(255) NULL,
        email NVARCHAR(255) NOT NULL
    );
END

-- optional audit columns for supplier timestamps
IF COL_LENGTH('suppliers','created_at') IS NULL
BEGIN
    ALTER TABLE suppliers ADD created_at DATETIME2 NULL CONSTRAINT df_suppliers_created_at DEFAULT (GETDATE());
END
GO

IF COL_LENGTH('suppliers','updated_at') IS NULL
BEGIN
    ALTER TABLE suppliers ADD updated_at DATETIME2 NULL;
END
GO
GO

-- migration: ensure email column exists
IF COL_LENGTH('suppliers', 'email') IS NULL
BEGIN
    ALTER TABLE suppliers ADD email NVARCHAR(255) NULL;
    UPDATE suppliers SET email = N'unknown@example.com' WHERE email IS NULL;
    ALTER TABLE suppliers ALTER COLUMN email NVARCHAR(255) NOT NULL;
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
        description NVARCHAR(MAX) NULL,
        active BIT NOT NULL CONSTRAINT df_products_active DEFAULT (1)
    );

    CREATE INDEX idx_products_category ON dbo.products(category);
END
GO

-- migration: ensure active column exists
IF COL_LENGTH('products', 'active') IS NULL
BEGIN
    ALTER TABLE products ADD active BIT NOT NULL CONSTRAINT df_products_active_upgrade DEFAULT (1);
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

-- SUBSCRIPTION_PAYMENTS
IF OBJECT_ID(N'dbo.subscription_payments', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.subscription_payments (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        user_id BIGINT NOT NULL,
        payment_code NVARCHAR(30) NOT NULL,
        plan_name NVARCHAR(50) NOT NULL,
        plan_days INT NOT NULL,
        amount DECIMAL(18,2) NOT NULL CONSTRAINT df_sub_pay_amount DEFAULT (0),
        payment_method NVARCHAR(30) NOT NULL,
        payment_status NVARCHAR(20) NOT NULL CONSTRAINT df_sub_pay_status DEFAULT N'SUCCESS',
        start_date DATE NOT NULL,
        end_date DATE NOT NULL,
        note NVARCHAR(255) NULL,
        created_at DATETIME2(0) NOT NULL CONSTRAINT df_sub_pay_created_at DEFAULT (SYSDATETIME()),
        CONSTRAINT uq_sub_pay_code UNIQUE (payment_code),
        CONSTRAINT fk_subscription_payments_user FOREIGN KEY (user_id) REFERENCES dbo.users(id)
    );

    CREATE INDEX idx_subscription_payments_user ON dbo.subscription_payments(user_id);
    CREATE INDEX idx_subscription_payments_created_at ON dbo.subscription_payments(created_at);
END
GO

-- APP_SETTINGS
IF OBJECT_ID(N'dbo.app_settings', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.app_settings (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        setting_key NVARCHAR(100) NOT NULL,
        setting_value NVARCHAR(255) NOT NULL,
        description NVARCHAR(255) NULL,
        updated_at DATETIME2(0) NOT NULL CONSTRAINT df_app_settings_updated_at DEFAULT (SYSDATETIME()),
        CONSTRAINT uq_app_settings_setting_key UNIQUE (setting_key)
    );
END
GO

-- TIER_HISTORY
IF OBJECT_ID(N'dbo.tier_history', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.tier_history (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        user_id BIGINT NOT NULL,
        old_tier NVARCHAR(10) NOT NULL,
        new_tier NVARCHAR(10) NOT NULL,
        old_expired_date DATE NULL,
        new_expired_date DATE NULL,
        change_type NVARCHAR(30) NOT NULL,
        note NVARCHAR(255) NULL,
        created_at DATETIME2(0) NOT NULL CONSTRAINT df_tier_history_created_at DEFAULT (SYSDATETIME()),
        CONSTRAINT fk_tier_history_user FOREIGN KEY (user_id) REFERENCES dbo.users(id)
    );

    CREATE INDEX idx_tier_history_user ON dbo.tier_history(user_id);
    CREATE INDEX idx_tier_history_created_at ON dbo.tier_history(created_at);
END
GO

SELECT 
    'UPDATE products SET image_url = N''' + image_url + ''' WHERE id = ' + CAST(id AS NVARCHAR(20)) + ';' AS sql_update
FROM products
WHERE image_url IS NOT NULL
  AND LTRIM(RTRIM(image_url)) <> '';

UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773240228719_raumuong.webp' WHERE id = 1;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773240627736_thitheo.webp' WHERE id = 2;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773239892904_cathu_result.jsp.jpg' WHERE id = 3;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242089145_raucai_result.jpg' WHERE id = 4;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242101927_caithia_result.jpg' WHERE id = 5;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242111582_bongcaixanh_result.jpg' WHERE id = 6;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242119923_carot_result.jpg' WHERE id = 7;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242133952_khoaitay_result.jpg' WHERE id = 8;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242147910_hanhla_result.jpg' WHERE id = 9;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773240527575_namkimcham.jpg' WHERE id = 10;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242173795_dualeo_result.jpg' WHERE id = 11;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242195810_baroiheo_result.jpg' WHERE id = 12;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242207951_suonnon_result.jpg' WHERE id = 13;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773238480086_thitbo.jpg' WHERE id = 14;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242225135_thitga_result.jpg' WHERE id = 15;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242793427_canhga.jpg' WHERE id = 16;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242249247_thitvit_result.jpg' WHERE id = 17;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773239811518_tomsu_result.jpg' WHERE id = 18;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773239832016_tomthe_result.jpg' WHERE id = 19;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773239845616_cahoi_result.jpg' WHERE id = 20;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773240004018_images_result.jpg' WHERE id = 21;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773239921918_mucong_result.jpg' WHERE id = 22;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242710494_mucnang.jpg' WHERE id = 23;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242347638_cuabien_result.jpg' WHERE id = 24;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242358279_ghexanh_result_result.jpg' WHERE id = 25;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242368273_ngheu_result_result.jpg' WHERE id = 26;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242378739_sodiep_result_result.jpg' WHERE id = 27;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242389305_xucxich_result.jpg' WHERE id = 28;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242630727_dauhu.webp' WHERE id = 33;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242432439_kimchi_result.jpg' WHERE id = 34;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242448226_chuoi_result.jpg' WHERE id = 35;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242458218_tao_result.jpg' WHERE id = 36;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242467814_cam_result.jpg' WHERE id = 37;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242477449_nhoxanh_result.jpg' WHERE id = 38;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242782543_duahau.jpg' WHERE id = 39;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242885544_t_i_xu_ng.jpg' WHERE id = 40;
UPDATE products SET image_url = N'/freshmart/assets/uploads/products/1773242532762_thanhlong_result.jpg' WHERE id = 41;