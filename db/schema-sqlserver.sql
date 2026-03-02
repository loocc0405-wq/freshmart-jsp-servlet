-- ================================
-- FRESHMART DATABASE (SQL SERVER)
-- ================================

IF DB_ID(N'freshmart') IS NULL
BEGIN
    CREATE DATABASE freshmart;
END
GO

USE freshmart;
GO

-- ================= USERS =================
IF OBJECT_ID(N'dbo.users', N'U') IS NOT NULL DROP TABLE dbo.users;
CREATE TABLE dbo.users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password_hash NVARCHAR(100) NOT NULL,
    role NVARCHAR(20) NOT NULL,
    tier NVARCHAR(10) NOT NULL DEFAULT N'FREE',
    expired_date DATE NULL,
    full_name NVARCHAR(100),
    phone NVARCHAR(20),
    address NVARCHAR(255),
    active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 DEFAULT SYSDATETIME()
);

-- ================= SUPPLIERS =================
IF OBJECT_ID(N'dbo.suppliers', N'U') IS NOT NULL DROP TABLE dbo.suppliers;
CREATE TABLE dbo.suppliers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(120) NOT NULL,
    phone NVARCHAR(20),
    address NVARCHAR(255),
    certificate NVARCHAR(255),
    lead_time_days INT DEFAULT 2,
    note NVARCHAR(255)
);

-- ================= PRODUCTS =================
IF OBJECT_ID(N'dbo.products', N'U') IS NOT NULL DROP TABLE dbo.products;
CREATE TABLE dbo.products (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(150) NOT NULL,
    category NVARCHAR(50),
    unit NVARCHAR(20),
    sell_price DECIMAL(18,2) NOT NULL,
    image_url NVARCHAR(500),
    description NVARCHAR(MAX)
);

-- ================= PRODUCT LOTS =================
IF OBJECT_ID(N'dbo.product_lots', N'U') IS NOT NULL DROP TABLE dbo.product_lots;
CREATE TABLE dbo.product_lots (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id BIGINT NOT NULL,
    supplier_id BIGINT,
    import_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    qty_in INT NOT NULL,
    qty_left INT NOT NULL,
    import_price DECIMAL(18,2),
    CONSTRAINT fk_lots_product FOREIGN KEY (product_id) REFERENCES dbo.products(id),
    CONSTRAINT fk_lots_supplier FOREIGN KEY (supplier_id) REFERENCES dbo.suppliers(id)
);

-- ================= ORDERS =================
IF OBJECT_ID(N'dbo.orders', N'U') IS NOT NULL DROP TABLE dbo.orders;
CREATE TABLE dbo.orders (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_code NVARCHAR(30) NOT NULL UNIQUE,
    customer_id BIGINT,
    created_by BIGINT,
    type NVARCHAR(20) NOT NULL,
    status NVARCHAR(20) NOT NULL,
    payment_method NVARCHAR(30),
    total_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    completed_at DATETIME2,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES dbo.users(id)
);

-- ================= ORDER ITEMS =================
IF OBJECT_ID(N'dbo.order_items', N'U') IS NOT NULL DROP TABLE dbo.order_items;
CREATE TABLE dbo.order_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(18,2) NOT NULL,
    line_total DECIMAL(18,2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES dbo.orders(id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES dbo.products(id)
);

-- ================= REVENUE DAILY =================
IF OBJECT_ID(N'dbo.revenue_daily', N'U') IS NOT NULL DROP TABLE dbo.revenue_daily;
CREATE TABLE dbo.revenue_daily (
    revenue_date DATE PRIMARY KEY,
    total_revenue DECIMAL(18,2) NOT NULL DEFAULT 0
);

-- ================= SAMPLE DATA =================

-- USERS (password demo: 123456 đã bcrypt trong project bạn)
INSERT INTO users(username,password_hash,role,tier,active)
VALUES
('admin','$2a$10$dummyhash','ADMIN','PRO',1),
('staff','$2a$10$dummyhash','STAFF','FREE',1),
('seller','$2a$10$dummyhash','SELLER','FREE',1),
('customer','$2a$10$dummyhash','CUSTOMER','FREE',1),
('pro_customer','$2a$10$dummyhash','CUSTOMER','PRO',1);

-- SUPPLIER
INSERT INTO suppliers(name,lead_time_days)
VALUES ('Fresh Farm',2);

-- PRODUCTS
INSERT INTO products(name,category,unit,sell_price)
VALUES 
(N'Rau Cải',N'Rau',N'kg',20000),
(N'Thịt Bò',N'Thịt',N'kg',250000),
(N'Cá Hồi',N'Cá',N'kg',300000);

-- PRODUCT LOTS
INSERT INTO product_lots(product_id,supplier_id,import_date,expiry_date,qty_in,qty_left,import_price)
VALUES
(1,1,GETDATE(),DATEADD(day,5,GETDATE()),100,80,15000),
(2,1,GETDATE(),DATEADD(day,7,GETDATE()),50,40,200000),
(3,1,GETDATE(),DATEADD(day,3,GETDATE()),30,20,250000);

-- SAMPLE COMPLETED ORDERS (để test forecast)
INSERT INTO orders(order_code,type,status,total_amount,created_at)
VALUES
('ORD001','ONLINE','COMPLETED',500000,DATEADD(day,-1,GETDATE())),
('ORD002','ONLINE','COMPLETED',300000,DATEADD(day,-2,GETDATE())),
('ORD003','ONLINE','COMPLETED',700000,DATEADD(day,-3,GETDATE()));

INSERT INTO order_items(order_id,product_id,quantity,unit_price,line_total)
VALUES
(1,1,5,20000,100000),
(2,2,2,250000,500000),
(3,3,1,300000,300000);
