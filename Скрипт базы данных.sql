SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS furniture_sales_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE furniture_sales_db;

DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS commissions;
DROP TABLE IF EXISTS sale_items;
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS expenses;
DROP TABLE IF EXISTS expense_categories;
DROP TABLE IF EXISTS discounts;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    login VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role_id INT,
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_users_roles FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    article VARCHAR(50) UNIQUE,
    stock_quantity INT DEFAULT 0,
    category_id INT,
    purchase_price DECIMAL(10,2) NOT NULL,
    retail_price DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_products_categories FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT chk_products_stock CHECK (stock_quantity >= 0),
    CONSTRAINT chk_products_prices CHECK (purchase_price >= 0 AND retail_price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    customer_type ENUM('individual', 'corporate') DEFAULT 'individual',
    registration_date DATE DEFAULT (CURRENT_DATE)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE discounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    value DECIMAL(10,2) NOT NULL,
    start_date DATE,
    end_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT chk_discounts_value CHECK (value >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE expense_categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    expense_date DATE NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    category_id INT,
    description TEXT,
    user_id INT,
    CONSTRAINT fk_expenses_categories FOREIGN KEY (category_id) REFERENCES expense_categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_expenses_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_expenses_amount CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sale_number VARCHAR(50) UNIQUE NOT NULL,
    sale_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    customer_id INT,
    manager_id INT,
    status VARCHAR(50) DEFAULT 'DRAFT',
    CONSTRAINT fk_sales_customers FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_sales_users FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sale_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    product_id INT,
    quantity INT NOT NULL,
    price_at_sale DECIMAL(10,2) NOT NULL,
    cost_at_sale DECIMAL(10,2) NOT NULL,
    discount_id INT,
    total_amount DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_sale_items_sales FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    CONSTRAINT fk_sale_items_products FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    CONSTRAINT fk_sale_items_discounts FOREIGN KEY (discount_id) REFERENCES discounts(id) ON DELETE RESTRICT,
    CONSTRAINT chk_sale_items_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE commissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT,
    manager_id INT,
    commission_percent DECIMAL(5,2) NOT NULL,
    commission_amount DECIMAL(10,2) NOT NULL,
    period VARCHAR(7) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    CONSTRAINT fk_commissions_sales FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    CONSTRAINT fk_commissions_users FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE audit_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(80),
    entity_id INT,
    details TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO roles(name) VALUES
    ('ADMIN'),
    ('SALES_MANAGER'),
    ('ACCOUNTANT');

INSERT INTO categories(name, description) VALUES
    ('Мягкая мебель', 'Диваны, кресла и пуфы'),
    ('Корпусная мебель', 'Шкафы, стеллажи, комоды'),
    ('Кухни', 'Кухонные гарнитуры и модули'),
    ('Столы и стулья', 'Обеденные и рабочие группы');

INSERT INTO expense_categories(name, description) VALUES
    ('Аренда', 'Постоянные расходы на помещение'),
    ('Коммунальные услуги', 'Электроэнергия, вода, отопление'),
    ('Заработная плата', 'Выплаты сотрудникам'),
    ('Логистика', 'Доставка и сборка мебели'),
    ('Реклама', 'Маркетинговые расходы');

SET FOREIGN_KEY_CHECKS = 1;
