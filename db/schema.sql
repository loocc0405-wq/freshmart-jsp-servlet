-- FreshMart reference schema (MySQL 8+)
-- Run in MySQL Workbench / phpMyAdmin / CLI

CREATE DATABASE IF NOT EXISTS freshmart
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE freshmart;

-- USERS
CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  role VARCHAR(20) NOT NULL,
  tier VARCHAR(10) NOT NULL DEFAULT 'FREE',
  expired_date DATE NULL,
  full_name VARCHAR(100) NULL,
  phone VARCHAR(20) NULL,
  address VARCHAR(255) NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_users_username (username)
);

-- SUPPLIERS
CREATE TABLE IF NOT EXISTS suppliers (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL,
  phone VARCHAR(20) NULL,
  address VARCHAR(255) NULL,
  certificate VARCHAR(255) NULL,
  lead_time_days INT NULL DEFAULT 1,
  note VARCHAR(255) NULL,
  PRIMARY KEY (id)
);

-- PRODUCTS
CREATE TABLE IF NOT EXISTS products (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(150) NOT NULL,
  category VARCHAR(50) NULL,
  unit VARCHAR(20) NULL,
  sell_price DECIMAL(18,2) NOT NULL,
  image_url VARCHAR(500) NULL,
  description TEXT NULL,
  PRIMARY KEY (id),
  KEY idx_products_category (category)
);

-- PRODUCT_LOTS
CREATE TABLE IF NOT EXISTS product_lots (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  supplier_id BIGINT NULL,
  import_date DATE NOT NULL,
  expiry_date DATE NOT NULL,
  qty_in INT NOT NULL,
  qty_left INT NOT NULL,
  import_price DECIMAL(18,2) NULL,
  PRIMARY KEY (id),
  KEY idx_lots_product_expiry (product_id, expiry_date),
  CONSTRAINT fk_lots_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_lots_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

-- CARTS
CREATE TABLE IF NOT EXISTS carts (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- CART_ITEMS
CREATE TABLE IF NOT EXISTS cart_items (
  id BIGINT NOT NULL AUTO_INCREMENT,
  cart_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_cart_items_cart_product (cart_id, product_id),
  CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id),
  CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- ORDERS
CREATE TABLE IF NOT EXISTS orders (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_code VARCHAR(30) NOT NULL,
  customer_id BIGINT NULL,
  created_by BIGINT NULL,
  type VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL,
  payment_method VARCHAR(30) NULL,
  total_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_orders_order_code (order_code),
  KEY idx_orders_status (status),
  KEY idx_orders_created_at (created_at),
  CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES users(id),
  CONSTRAINT fk_orders_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

-- ORDER_ITEMS
CREATE TABLE IF NOT EXISTS order_items (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  unit_price DECIMAL(18,2) NOT NULL,
  line_total DECIMAL(18,2) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_order_items_order (order_id),
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- REVENUE_DAILY
CREATE TABLE IF NOT EXISTS revenue_daily (
  revenue_date DATE NOT NULL,
  total_revenue DECIMAL(18,2) NOT NULL DEFAULT 0,
  PRIMARY KEY (revenue_date)
);
