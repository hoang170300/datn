-- =========================================
-- COSPLAY SHOP DATABASE - SQL SERVER
-- FULL SCHEMA (Updated & Extended)
-- =========================================

-- Tạo database
CREATE DATABASE DATN;
GO
USE DATN;
GO

-- =========================================
-- ROLES
-- =========================================
CREATE TABLE roles(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- =========================================
-- USERS
-- =========================================
CREATE TABLE users(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name NVARCHAR(255),
    phone VARCHAR(20),
    address_street NVARCHAR(255),
    address_province NVARCHAR(100),
    address_district NVARCHAR(100),
    address_ward NVARCHAR(100),
    avatar_url VARCHAR(500),
    role_id BIGINT NOT NULL,
    is_active BIT DEFAULT 1,
    reset_token VARCHAR(255),
    reset_token_expiry DATETIME,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY(role_id) REFERENCES roles(id)
);

-- =========================================
-- CATEGORIES
-- =========================================
CREATE TABLE categories(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) UNIQUE NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description NVARCHAR(MAX),
    image_url VARCHAR(500),
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- =========================================
-- SERIES
-- =========================================
CREATE TABLE series(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description NVARCHAR(MAX),
    image_url VARCHAR(500),
    series_type VARCHAR(20) CHECK(series_type IN ('ANIME','GAME','MOVIE','OTHER')) DEFAULT 'ANIME',
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- =========================================
-- PRODUCTS
-- =========================================
CREATE TABLE products(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description NVARCHAR(MAX),
    category_id BIGINT NOT NULL,
    series_id BIGINT,
    character_name NVARCHAR(255),              -- Tên nhân vật cosplay
    product_type VARCHAR(20)
        CHECK(product_type IN ('SALE','RENTAL','BOTH')) DEFAULT 'SALE',
    sale_price DECIMAL(15,2),
    rental_price_per_day DECIMAL(15,2),
    deposit_amount DECIMAL(15,2) DEFAULT 0,    -- Tiền cọc khi thuê
    is_hot BIT DEFAULT 0,
    is_active BIT DEFAULT 1,
    views_count INT DEFAULT 0,
    avg_rating DECIMAL(3,2) DEFAULT 0,
    review_count INT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY(category_id) REFERENCES categories(id),
    FOREIGN KEY(series_id) REFERENCES series(id)
);

-- =========================================
-- PRODUCT VARIANTS
-- =========================================
CREATE TABLE product_variants(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id BIGINT NOT NULL,
    size VARCHAR(50) NOT NULL,
    color VARCHAR(100),
    sku VARCHAR(100) UNIQUE,
    price DECIMAL(15,2) NOT NULL,
    stock_quantity INT DEFAULT 0,
    rental_quantity INT DEFAULT 0,             -- Số lượng dành cho thuê
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- =========================================
-- PRODUCT IMAGES
-- =========================================
CREATE TABLE product_images(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    alt_text NVARCHAR(255),
    display_order INT DEFAULT 0,
    is_primary BIT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- =========================================
-- COMBO PRODUCTS (Full Set Cosplay)
-- =========================================
CREATE TABLE combos(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description NVARCHAR(MAX),
    image_url VARCHAR(500),
    combo_price DECIMAL(15,2),                 -- Giá combo (thường thấp hơn tổng)
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

CREATE TABLE combo_items(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    combo_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    FOREIGN KEY(combo_id) REFERENCES combos(id) ON DELETE CASCADE,
    FOREIGN KEY(product_id) REFERENCES products(id)
);

-- =========================================
-- FLASH SALES
-- =========================================
CREATE TABLE flash_sales(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255),
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE()
);

CREATE TABLE flash_sale_items(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    flash_sale_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    discount_percent DECIMAL(5,2),
    sale_price DECIMAL(15,2),
    quantity_limit INT,
    sold_count INT DEFAULT 0,
    FOREIGN KEY(flash_sale_id) REFERENCES flash_sales(id) ON DELETE CASCADE,
    FOREIGN KEY(product_id) REFERENCES products(id)
);

-- =========================================
-- CARTS
-- =========================================
CREATE TABLE carts(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =========================================
-- CART ITEMS
-- =========================================
CREATE TABLE cart_items(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_variant_id BIGINT NOT NULL,
    quantity INT DEFAULT 1,
    order_type VARCHAR(10) CHECK(order_type IN ('SALE','RENTAL')) DEFAULT 'SALE',
    rental_start_date DATE,
    rental_end_date DATE,
    rental_days INT,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY(cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY(product_variant_id) REFERENCES product_variants(id)
);

-- =========================================
-- VOUCHERS
-- =========================================
CREATE TABLE vouchers(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code VARCHAR(50) UNIQUE,
    description NVARCHAR(MAX),
    discount_type VARCHAR(20)
        CHECK(discount_type IN ('PERCENTAGE','FIXED_AMOUNT')),
    discount_value DECIMAL(10,2),
    min_order_value DECIMAL(15,2) DEFAULT 0,
    max_discount_amount DECIMAL(15,2),
    usage_limit INT,
    used_count INT DEFAULT 0,
    start_date DATETIME,
    end_date DATETIME,
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- =========================================
-- SHIPPING FEES
-- =========================================
CREATE TABLE shipping_fees(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    region_name NVARCHAR(100) NOT NULL,
    province_code VARCHAR(20),
    fee DECIMAL(15,2) NOT NULL,
    free_ship_from DECIMAL(15,2),              -- Miễn phí ship từ mức này
    is_active BIT DEFAULT 1
);

-- =========================================
-- ORDERS
-- =========================================
CREATE TABLE orders(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_type VARCHAR(20)
        CHECK(order_type IN ('ONLINE','OFFLINE')) DEFAULT 'ONLINE',
    order_number VARCHAR(50) UNIQUE,
    total_price DECIMAL(15,2),
    discount_amount DECIMAL(15,2) DEFAULT 0,
    shipping_fee DECIMAL(15,2) DEFAULT 0,
    final_price DECIMAL(15,2),
    voucher_id BIGINT,
    voucher_code VARCHAR(50),
    status VARCHAR(20)
        CHECK(status IN ('PENDING','CONFIRMED','PROCESSING','SHIPPING','DELIVERED','CANCELLED','RETURNED'))
        DEFAULT 'PENDING',
    payment_status VARCHAR(20)
        CHECK(payment_status IN ('PENDING','PAID','FAILED','REFUNDED'))
        DEFAULT 'PENDING',
    payment_method VARCHAR(50)
        CHECK(payment_method IN ('COD','BANK_TRANSFER','E_WALLET')),
    shipping_address_street NVARCHAR(255),
    shipping_address_province NVARCHAR(100),
    shipping_address_district NVARCHAR(100),
    shipping_address_ward NVARCHAR(100),
    phone VARCHAR(20),
    note NVARCHAR(MAX),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY(user_id) REFERENCES users(id),
    FOREIGN KEY(voucher_id) REFERENCES vouchers(id)
);

-- =========================================
-- ORDER ITEMS
-- =========================================
CREATE TABLE order_items(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_variant_id BIGINT NOT NULL,
    product_name NVARCHAR(255),
    product_sku VARCHAR(100),
    item_type VARCHAR(10) CHECK(item_type IN ('SALE','RENTAL')) DEFAULT 'SALE',
    quantity INT,
    unit_price DECIMAL(15,2),
    total_price DECIMAL(15,2),
    deposit_amount DECIMAL(15,2) DEFAULT 0,
    rental_start_date DATE,
    rental_end_date DATE,
    rental_days INT,
    rental_status VARCHAR(20)
        CHECK(rental_status IN ('ACTIVE','RETURNED','OVERDUE')) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY(order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY(product_variant_id) REFERENCES product_variants(id)
);

-- =========================================
-- REVIEWS
-- =========================================
CREATE TABLE reviews(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT,                           -- Chỉ review sau khi mua
    rating INT CHECK(rating BETWEEN 1 AND 5) NOT NULL,
    title NVARCHAR(255),
    comment NVARCHAR(MAX),
    is_approved BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    UNIQUE(user_id, product_id, order_id),
    FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY(user_id) REFERENCES users(id),
    FOREIGN KEY(order_id) REFERENCES orders(id)
);

-- =========================================
-- FAVORITES
-- =========================================
CREATE TABLE favorites(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    UNIQUE(user_id, product_id),
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- =========================================
-- NOTIFICATIONS
-- =========================================
CREATE TABLE notifications(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title NVARCHAR(255) NOT NULL,
    message NVARCHAR(MAX),
    type VARCHAR(50)
        CHECK(type IN ('ORDER','PROMOTION','RENTAL','SYSTEM','NEW_PRODUCT')),
    is_read BIT DEFAULT 0,
    link_url VARCHAR(500),
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =========================================
-- BANNERS
-- =========================================
CREATE TABLE banners(
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255),
    description NVARCHAR(MAX),
    image_url VARCHAR(500),
    link_url VARCHAR(500),
    display_order INT DEFAULT 0,
    is_active BIT DEFAULT 1,
    start_date DATETIME,
    end_date DATETIME,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- =========================================
-- INDEXES
-- =========================================
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_series ON products(series_id);
CREATE INDEX idx_products_is_hot ON products(is_hot);
CREATE INDEX idx_products_type ON products(product_type);
CREATE INDEX idx_products_created_at ON products(created_at);
CREATE INDEX idx_product_variants_product ON product_variants(product_id);
CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_favorites_user ON favorites(user_id);
CREATE INDEX idx_reviews_product ON reviews(product_id);
CREATE INDEX idx_notifications_user ON notifications(user_id);
GO

-- =========================================
-- SEED DATA
-- =========================================

-- Roles
INSERT INTO roles (name, description) VALUES
('ROLE_ADMIN', 'Administrator with full access'),
('ROLE_USER', 'Regular user with limited access');

-- Users (password: Admin@123 - BCrypt)
INSERT INTO users (username, email, password, full_name, phone, role_id) VALUES
('admin', 'admin@cosplay.com', '$2a$12$mWeAdTheljp3hxOOfbn0tOWP2lJZF.0C04WkRr8Mo.ve3rZd7FXaW', 'Administrator', '0123456789', 1),
('user1', 'user1@example.com', '$2a$12$mWeAdTheljp3hxOOfbn0tOWP2lJZF.0C04WkRr8Mo.ve3rZd7FXaW', N'Nguyễn Văn A', '0987654321', 2);

-- Categories
INSERT INTO categories (name, slug, description, image_url) VALUES
(N'Cosplay Nam', 'cosplay-nam', N'Trang phục cosplay cho nam', 'https://pin.it/2SqD8eTuq'),
(N'Cosplay Nữ', 'cosplay-nu', N'Trang phục cosplay cho nữ', 'https://pin.it/28nYXNb6H'),
(N'Phụ Kiện', 'phu-kien', N'Phụ kiện cosplay (tóc giả, mũ, giày...)', 'https://pin.it/7e3RRpAm9'),
(N'Vũ Khí Giả', 'vu-khi-gia', N'Vũ khí mô hình cosplay', 'https://pin.it/vTkWVR0w9');

-- Series
INSERT INTO series (name, slug, description, series_type) VALUES
(N'Naruto', 'naruto', N'Series Naruto Shippuden', 'ANIME'),
(N'One Piece', 'one-piece', N'Series Đảo Hải Tặc', 'ANIME'),
(N'Demon Slayer', 'demon-slayer', N'Series Thanh Gươm Diệt Quỷ', 'ANIME'),
(N'Genshin Impact', 'genshin-impact', N'Game Genshin Impact', 'GAME'),
(N'Avengers', 'avengers', N'Marvel Avengers', 'MOVIE');

-- Products
INSERT INTO products (name, slug, description, category_id, series_id, character_name, product_type, sale_price, rental_price_per_day, deposit_amount, is_hot) VALUES
(N'Trang Phục Naruto Uzumaki', 'trang-phuc-naruto-uzumaki', N'Bộ cosplay Naruto Uzumaki chất lượng cao, bao gồm áo vest cam, quần và băng đầu Konoha', 1, 1, N'Naruto Uzumaki', 'BOTH', 450000, 50000, 200000, 1),
(N'Trang Phục Sasuke Uchiha', 'trang-phuc-sasuke-uchiha', N'Bộ cosplay Sasuke phong cách Shippuden, áo xanh navy', 1, 1, N'Sasuke Uchiha', 'BOTH', 480000, 55000, 200000, 1),
(N'Trang Phục Luffy Mũ Rơm', 'trang-phuc-luffy-mu-rom', N'Bộ cosplay Monkey D. Luffy kèm mũ rơm đặc trưng', 1, 2, N'Monkey D. Luffy', 'BOTH', 420000, 45000, 150000, 1),
(N'Trang Phục Nezuko Kamado', 'trang-phuc-nezuko-kamado', N'Bộ kimono hồng phong cách Nezuko Kamado trong Demon Slayer', 2, 3, N'Nezuko Kamado', 'BOTH', 520000, 65000, 250000, 1),
(N'Tóc Giả Tanjiro', 'toc-gia-tanjiro', N'Tóc giả màu đen kiểu Tanjiro Kamado', 3, 3, N'Tanjiro Kamado', 'SALE', 150000, NULL, 0, 0),
(N'Kiếm Thanh Long', 'kiem-thanh-long', N'Kiếm mô hình cosplay bằng nhựa chắc chắn', 4, NULL, NULL, 'BOTH', 280000, 30000, 100000, 0);

-- Product Variants
INSERT INTO product_variants (product_id, size, color, sku, price, stock_quantity, rental_quantity) VALUES
(1, 'S', N'Cam', 'NAR-001-S', 450000, 10, 5),
(1, 'M', N'Cam', 'NAR-001-M', 450000, 15, 8),
(1, 'L', N'Cam', 'NAR-001-L', 450000, 12, 6),
(1, 'XL', N'Cam', 'NAR-001-XL', 470000, 8, 4),
(2, 'S', N'Xanh Navy', 'SAS-001-S', 480000, 8, 4),
(2, 'M', N'Xanh Navy', 'SAS-001-M', 480000, 12, 6),
(2, 'L', N'Xanh Navy', 'SAS-001-L', 480000, 10, 5),
(3, 'M', N'Đỏ', 'LUF-001-M', 420000, 15, 8),
(3, 'L', N'Đỏ', 'LUF-001-L', 420000, 12, 6),
(4, 'S', N'Hồng', 'NEZ-001-S', 520000, 6, 3),
(4, 'M', N'Hồng', 'NEZ-001-M', 520000, 8, 4),
(5, 'FREE', N'Đen', 'WIG-TAN-001', 150000, 20, 0),
(6, 'FREE', N'Bạc', 'WEP-001', 280000, 15, 10);

-- Shipping Fees
INSERT INTO shipping_fees (region_name, fee, free_ship_from) VALUES
(N'Nội thành HN & HCM', 25000, 500000),
(N'Các tỉnh thành khác', 35000, 700000),
(N'Vùng xa / hải đảo', 55000, NULL);

-- Vouchers
INSERT INTO vouchers (code, description, discount_type, discount_value, min_order_value, max_discount_amount, usage_limit, start_date, end_date) VALUES
('WELCOME10', N'Giảm 10% cho đơn hàng đầu tiên', 'PERCENTAGE', 10, 200000, 100000, 100, GETDATE(), DATEADD(MONTH,3,GETDATE())),
('SUMMER50K', N'Giảm 50k cho đơn từ 500k', 'FIXED_AMOUNT', 50000, 500000, NULL, 200, GETDATE(), DATEADD(MONTH,2,GETDATE())),
('FREESHIP', N'Miễn phí vận chuyển cho đơn từ 300k', 'FIXED_AMOUNT', 35000, 300000, NULL, 500, GETDATE(), DATEADD(MONTH,1,GETDATE()));

-- Banners
INSERT INTO banners (title, description, image_url, link_url, display_order, is_active) VALUES
(N'Bộ Sưu Tập Mùa Hè 2024', N'Ưu đãi lên đến 50%', 'https://pin.it/7b7pIfNTP', '/products', 1, 1),
(N'Cosplay Demon Slayer', N'Trang phục hot nhất năm', 'https://pin.it/25Rbkatx0', '/products?series=demon-slayer', 2, 1),
(N'Flash Sale Cuối Tuần', N'Sale up to 70%', 'https://pin.it/6VABHCkbO', '/flash-sale', 3, 1);

-- Combos
INSERT INTO combos (name, slug, description, combo_price) VALUES
(N'Combo Naruto Full Set', 'combo-naruto-full', N'Bao gồm trang phục + tóc giả + băng đầu Naruto', 550000),
(N'Combo Nezuko Full Set', 'combo-nezuko-full', N'Bao gồm kimono + tóc giả + hộp tre Nezuko', 650000);

INSERT INTO combo_items (combo_id, product_id) VALUES
(1, 1), (1, 5),
(2, 4), (2, 5);
GO
