
-- Tạo và dùng database
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'DATN')
    CREATE DATABASE DATN;
GO
USE DATN;
GO
 
-- Xóa bảng cũ nếu muốn fresh start (bỏ comment nếu cần)
/*
DROP TABLE IF EXISTS combo_items, flash_sale_items, order_items, cart_items,
    reviews, favorites, notifications, combos, flash_sales, orders,
    vouchers, banners, product_images, product_variants, products,
    carts, shipping_fees, series, categories, users, roles;
GO
*/
 
-- =====================================================
-- ROLES
-- =====================================================
CREATE TABLE roles (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    name        VARCHAR(50)     NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  DATETIME        DEFAULT GETDATE(),
    updated_at  DATETIME        DEFAULT GETDATE()
);
GO
 
-- =====================================================
-- USERS
-- =====================================================
CREATE TABLE users (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    username            VARCHAR(100)    NOT NULL UNIQUE,
    email               VARCHAR(255)    NOT NULL UNIQUE,
    password            VARCHAR(255)    NOT NULL,
    full_name           NVARCHAR(255),
    phone               VARCHAR(20),
    address_street      NVARCHAR(255),
    address_province    NVARCHAR(100),
    address_district    NVARCHAR(100),
    address_ward        NVARCHAR(100),
    avatar_url          VARCHAR(500),
    role_id             BIGINT          NOT NULL,
    is_active           BIT             DEFAULT 1,
    reset_token         VARCHAR(255),
    reset_token_expiry  DATETIME,
    created_at          DATETIME        DEFAULT GETDATE(),
    updated_at          DATETIME        DEFAULT GETDATE(),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
GO
 
-- =====================================================
-- CATEGORIES
-- =====================================================
CREATE TABLE categories (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    name        NVARCHAR(255)   NOT NULL UNIQUE,
    slug        VARCHAR(255)    NOT NULL UNIQUE,
    description NVARCHAR(MAX),
    image_url   VARCHAR(500),
    is_active   BIT             DEFAULT 1,
    created_at  DATETIME        DEFAULT GETDATE(),
    updated_at  DATETIME        DEFAULT GETDATE()
);
GO
 
-- =====================================================
-- SERIES
-- =====================================================
CREATE TABLE series (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    name        NVARCHAR(255)   NOT NULL,
    slug        VARCHAR(255)    NOT NULL UNIQUE,
    description NVARCHAR(MAX),
    image_url   VARCHAR(500),
    series_type VARCHAR(20)     DEFAULT 'ANIME'
        CHECK (series_type IN ('ANIME','GAME','MOVIE','OTHER')),
    is_active   BIT             DEFAULT 1,
    created_at  DATETIME        DEFAULT GETDATE(),
    updated_at  DATETIME        DEFAULT GETDATE()
);
GO
 
-- =====================================================
-- PRODUCTS
-- =====================================================
CREATE TABLE products (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    name                NVARCHAR(255)   NOT NULL,
    slug                VARCHAR(255)    NOT NULL UNIQUE,
    description         NVARCHAR(MAX),
    category_id         BIGINT          NOT NULL,
    series_id           BIGINT,
    character_name      NVARCHAR(255),
    product_type        VARCHAR(20)     DEFAULT 'SALE'
        CHECK (product_type IN ('SALE','RENTAL','BOTH')),
    sale_price          DECIMAL(15,2),
    rental_price_per_day DECIMAL(15,2),
    deposit_amount      DECIMAL(15,2)   DEFAULT 0,
    primary_image_url   VARCHAR(500),           -- ← THÊM MỚI
    is_hot              BIT             DEFAULT 0,
    is_new              BIT             DEFAULT 1,  -- ← THÊM MỚI
    is_active           BIT             DEFAULT 1,
    views_count         INT             DEFAULT 0,
    avg_rating          DECIMAL(3,2)    DEFAULT 0,
    review_count        INT             DEFAULT 0,
    created_at          DATETIME        DEFAULT GETDATE(),
    updated_at          DATETIME        DEFAULT GETDATE(),
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (series_id)   REFERENCES series(id)
);
GO
 
-- =====================================================
-- PRODUCT VARIANTS
-- =====================================================
CREATE TABLE product_variants (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id      BIGINT          NOT NULL,
    size            VARCHAR(50)     NOT NULL,
    color           VARCHAR(100),
    sku             VARCHAR(100)    UNIQUE,
    price           DECIMAL(15,2)   NULL,      -- ← NULL được (dùng sale_price làm fallback)
    stock_quantity  INT             DEFAULT 0,
    rental_quantity INT             DEFAULT 0,
    is_active       BIT             DEFAULT 1,
    created_at      DATETIME        DEFAULT GETDATE(),
    updated_at      DATETIME        DEFAULT GETDATE(),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);
GO
 
-- =====================================================
-- PRODUCT IMAGES
-- =====================================================
CREATE TABLE product_images (
    id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id    BIGINT          NOT NULL,
    image_url     VARCHAR(500)    NOT NULL,
    alt_text      NVARCHAR(255),
    display_order INT             DEFAULT 0,
    is_primary    BIT             DEFAULT 0,
    created_at    DATETIME        DEFAULT GETDATE(),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);
GO
 
-- =====================================================
-- COMBOS
-- =====================================================
CREATE TABLE combos (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    name        NVARCHAR(255)   NOT NULL,
    slug        VARCHAR(255)    NOT NULL UNIQUE,
    description NVARCHAR(MAX),
    image_url   VARCHAR(500),
    combo_price DECIMAL(15,2),
    is_active   BIT             DEFAULT 1,
    created_at  DATETIME        DEFAULT GETDATE(),
    updated_at  DATETIME        DEFAULT GETDATE()
);
GO
 
CREATE TABLE combo_items (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    combo_id    BIGINT NOT NULL,
    product_id  BIGINT NOT NULL,
    FOREIGN KEY (combo_id)   REFERENCES combos(id)   ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);
GO
 
-- =====================================================
-- FLASH SALES
-- =====================================================
CREATE TABLE flash_sales (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    name        NVARCHAR(255),
    start_time  DATETIME        NOT NULL,
    end_time    DATETIME        NOT NULL,
    is_active   BIT             DEFAULT 1,
    created_at  DATETIME        DEFAULT GETDATE()
);
GO
 
CREATE TABLE flash_sale_items (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    flash_sale_id   BIGINT          NOT NULL,
    product_id      BIGINT          NOT NULL,
    discount_percent DECIMAL(5,2),
    sale_price      DECIMAL(15,2),
    quantity        INT             DEFAULT 0,
    sold_quantity   INT             DEFAULT 0,
    is_active       BIT             DEFAULT 1,
    FOREIGN KEY (flash_sale_id) REFERENCES flash_sales(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id)    REFERENCES products(id)
);
GO
 
-- =====================================================
-- CARTS
-- =====================================================
CREATE TABLE carts (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id     BIGINT          NOT NULL UNIQUE,
    created_at  DATETIME        DEFAULT GETDATE(),
    updated_at  DATETIME        DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
GO
 
-- =====================================================
-- CART ITEMS
-- =====================================================
CREATE TABLE cart_items (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    cart_id             BIGINT          NOT NULL,
    product_variant_id  BIGINT          NOT NULL,
    quantity            INT             DEFAULT 1,
    order_type          VARCHAR(10)     DEFAULT 'SALE'
        CHECK (order_type IN ('SALE','RENTAL')),
    rental_start_date   DATE,
    rental_end_date     DATE,
    rental_days         INT,
    created_at          DATETIME        DEFAULT GETDATE(),
    updated_at          DATETIME        DEFAULT GETDATE(),
    FOREIGN KEY (cart_id)            REFERENCES carts(id)            ON DELETE CASCADE,
    FOREIGN KEY (product_variant_id) REFERENCES product_variants(id)
);
GO
 
-- =====================================================
-- VOUCHERS
-- =====================================================
CREATE TABLE vouchers (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    code                VARCHAR(50)     UNIQUE,
    description         NVARCHAR(MAX),
    discount_type       VARCHAR(20)
        CHECK (discount_type IN ('PERCENTAGE','FIXED_AMOUNT')),
    discount_value      DECIMAL(10,2),
    min_order_value     DECIMAL(15,2)   DEFAULT 0,
    max_discount_amount DECIMAL(15,2),
    usage_limit         INT,
    used_count          INT             DEFAULT 0,
    start_date          DATETIME,
    end_date            DATETIME,
    is_active           BIT             DEFAULT 1,
    created_at          DATETIME        DEFAULT GETDATE(),
    updated_at          DATETIME        DEFAULT GETDATE()
);
GO
 
-- =====================================================
-- SHIPPING FEES
-- =====================================================
CREATE TABLE shipping_fees (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    region_name     NVARCHAR(100)   NOT NULL,
    province_code   VARCHAR(20),
    fee             DECIMAL(15,2)   NOT NULL,
    free_ship_from  DECIMAL(15,2),
    is_active       BIT             DEFAULT 1
);
GO
 
-- =====================================================
-- ORDERS
-- =====================================================
CREATE TABLE orders (
    id                          BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_number                VARCHAR(50)     UNIQUE,
    user_id                     BIGINT          NOT NULL,
    status                      VARCHAR(20)     DEFAULT 'PENDING'
        CHECK (status IN ('PENDING','CONFIRMED','PROCESSING','SHIPPING','DELIVERED','CANCELLED','RETURNED')),
    payment_status              VARCHAR(20)     DEFAULT 'PENDING'
        CHECK (payment_status IN ('PENDING','PAID','FAILED','REFUNDED')),
    payment_method              VARCHAR(50)
        CHECK (payment_method IN ('COD','BANK_TRANSFER','E_WALLET')),
    subtotal_price              DECIMAL(15,2),  -- ← THÊM MỚI
    discount_amount             DECIMAL(15,2)   DEFAULT 0,
    shipping_fee                DECIMAL(15,2)   DEFAULT 0,
    final_price                 DECIMAL(15,2),
    voucher_id                  BIGINT,
    shipping_address_street     NVARCHAR(255),
    shipping_address_ward       NVARCHAR(100),
    shipping_address_district   NVARCHAR(100),
    shipping_address_province   NVARCHAR(100),
    phone                       VARCHAR(20),
    note                        NVARCHAR(MAX),
    created_at                  DATETIME        DEFAULT GETDATE(),
    updated_at                  DATETIME        DEFAULT GETDATE(),
    FOREIGN KEY (user_id)    REFERENCES users(id),
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id)
);
GO
 
-- =====================================================
-- ORDER ITEMS
-- =====================================================
CREATE TABLE order_items (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id            BIGINT          NOT NULL,
    product_variant_id  BIGINT          NOT NULL,
    product_name        NVARCHAR(255),
    item_type           VARCHAR(10)     DEFAULT 'SALE'
        CHECK (item_type IN ('SALE','RENTAL')),
    quantity            INT             DEFAULT 1,
    unit_price          DECIMAL(15,2),
    total_price         DECIMAL(15,2),
    deposit_amount      DECIMAL(15,2)   DEFAULT 0,
    rental_start_date   DATE,
    rental_end_date     DATE,
    rental_days         INT,
    rental_status       VARCHAR(20)
        CHECK (rental_status IN ('ACTIVE','RETURNED','OVERDUE')),
    created_at          DATETIME        DEFAULT GETDATE(),
    FOREIGN KEY (order_id)           REFERENCES orders(id)           ON DELETE CASCADE,
    FOREIGN KEY (product_variant_id) REFERENCES product_variants(id)
);
GO
 
-- =====================================================
-- REVIEWS
-- =====================================================
CREATE TABLE reviews (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id  BIGINT          NOT NULL,
    user_id     BIGINT          NOT NULL,
    order_id    BIGINT,
    rating      INT             NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title       NVARCHAR(255),
    comment     NVARCHAR(MAX),
    is_approved BIT             DEFAULT 1,
    created_at  DATETIME        DEFAULT GETDATE(),
    updated_at  DATETIME        DEFAULT GETDATE(),
    UNIQUE (user_id, product_id, order_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)    REFERENCES users(id),
    FOREIGN KEY (order_id)   REFERENCES orders(id)
);
GO
 
-- =====================================================
-- FAVORITES
-- =====================================================
CREATE TABLE favorites (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id     BIGINT  NOT NULL,
    product_id  BIGINT  NOT NULL,
    created_at  DATETIME DEFAULT GETDATE(),
    UNIQUE (user_id, product_id),
    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);
GO
 
-- =====================================================
-- NOTIFICATIONS
-- =====================================================
CREATE TABLE notifications (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id     BIGINT          NOT NULL,
    title       NVARCHAR(255)   NOT NULL,
    message     NVARCHAR(MAX),
    type        VARCHAR(50)
        CHECK (type IN ('ORDER','PROMOTION','RENTAL','SYSTEM','NEW_PRODUCT')),
    is_read     BIT             DEFAULT 0,
    link_url    VARCHAR(500),
    created_at  DATETIME        DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
GO
 
-- =====================================================
-- BANNERS
-- =====================================================
CREATE TABLE banners (
    id            BIGINT IDENTITY(1,1) PRIMARY KEY,
    title         NVARCHAR(255),
    description   NVARCHAR(MAX),
    image_url     VARCHAR(500),
    link_url      VARCHAR(500),
    display_order INT     DEFAULT 0,
    is_active     BIT     DEFAULT 1,
    start_date    DATETIME,
    end_date      DATETIME,
    created_at    DATETIME DEFAULT GETDATE(),
    updated_at    DATETIME DEFAULT GETDATE()
);
GO
 
-- =====================================================
-- INDEXES
-- =====================================================
CREATE INDEX idx_products_category   ON products(category_id);
CREATE INDEX idx_products_series     ON products(series_id);
CREATE INDEX idx_products_is_hot     ON products(is_hot);
CREATE INDEX idx_products_is_new     ON products(is_new);
CREATE INDEX idx_products_type       ON products(product_type);
CREATE INDEX idx_products_created    ON products(created_at);
CREATE INDEX idx_variants_product    ON product_variants(product_id);
CREATE INDEX idx_cart_items_cart     ON cart_items(cart_id);
CREATE INDEX idx_orders_user         ON orders(user_id);
CREATE INDEX idx_orders_status       ON orders(status);
CREATE INDEX idx_order_items_order   ON order_items(order_id);
CREATE INDEX idx_favorites_user      ON favorites(user_id);
CREATE INDEX idx_reviews_product     ON reviews(product_id);
CREATE INDEX idx_notifications_user  ON notifications(user_id);
GO
 
-- =====================================================
-- SEED DATA
-- =====================================================
 
-- Roles
INSERT INTO roles (name, description) VALUES
('ROLE_ADMIN', 'Administrator'),
('ROLE_USER',  'Regular user');
GO
 
-- Users (password: Admin@123)
INSERT INTO users (username, email, password, full_name, phone, role_id) VALUES
('admin', 'admin@cosplay.com',
 '$2a$12$mWeAdTheljp3hxOOfbn0tOWP2lJZF.0C04WkRr8Mo.ve3rZd7FXaW',
 'Administrator', '0123456789', 1),
('user1', 'user1@example.com',
 '$2a$12$mWeAdTheljp3hxOOfbn0tOWP2lJZF.0C04WkRr8Mo.ve3rZd7FXaW',
 N'Nguyễn Văn A', '0987654321', 2);
GO
 
-- Carts cho 2 user
INSERT INTO carts (user_id) VALUES (1), (2);
GO
 
-- Categories
INSERT INTO categories (name, slug, description) VALUES
(N'Cosplay Nam',  'cosplay-nam',  N'Trang phục cosplay cho nam'),
(N'Cosplay Nữ',   'cosplay-nu',   N'Trang phục cosplay cho nữ'),
(N'Phụ Kiện',     'phu-kien',     N'Tóc giả, mũ, giày...'),
(N'Vũ Khí Giả',  'vu-khi-gia',  N'Vũ khí mô hình cosplay');
GO
 
-- Series
INSERT INTO series (name, slug, description, series_type) VALUES
(N'Naruto',        'naruto',        N'Naruto Shippuden',         'ANIME'),
(N'One Piece',     'one-piece',     N'Đảo Hải Tặc',             'ANIME'),
(N'Demon Slayer',  'demon-slayer',  N'Thanh Gươm Diệt Quỷ',     'ANIME'),
(N'Genshin Impact','genshin-impact',N'Game Genshin Impact',      'GAME'),
(N'Avengers',      'avengers',      N'Marvel Avengers',          'MOVIE');
GO
 
-- Products (có primary_image_url và is_new)
INSERT INTO products
    (name, slug, description, category_id, series_id, character_name,
     product_type, sale_price, rental_price_per_day, deposit_amount,
     primary_image_url, is_hot, is_new)
VALUES
(N'Trang Phục Naruto Uzumaki', 'trang-phuc-naruto-uzumaki',
 N'Bộ cosplay Naruto chất lượng cao, áo vest cam, băng đầu Konoha',
 1, 1, N'Naruto Uzumaki', 'BOTH', 450000, 50000, 200000,
 'https://i.pinimg.com/736x/44/49/f9/4449f99cb70ec7b7734471b81f7f3dac.jpg', 1, 1),
 
(N'Trang Phục Sasuke Uchiha', 'trang-phuc-sasuke-uchiha',
 N'Bộ cosplay Sasuke Shippuden, áo xanh navy đặc trưng',
 1, 1, N'Sasuke Uchiha', 'BOTH', 480000, 55000, 200000,
 NULL, 1, 1),
 
(N'Trang Phục Luffy Mũ Rơm', 'trang-phuc-luffy-mu-rom',
 N'Bộ cosplay Monkey D. Luffy kèm mũ rơm',
 1, 2, N'Monkey D. Luffy', 'BOTH', 420000, 45000, 150000,
 NULL, 1, 1),
 
(N'Trang Phục Nezuko Kamado', 'trang-phuc-nezuko-kamado',
 N'Bộ kimono hồng phong cách Nezuko trong Demon Slayer',
 2, 3, N'Nezuko Kamado', 'BOTH', 520000, 65000, 250000,
 NULL, 1, 1),
 
(N'Tóc Giả Tanjiro', 'toc-gia-tanjiro',
 N'Tóc giả màu đen kiểu Tanjiro Kamado',
 3, 3, N'Tanjiro Kamado', 'SALE', 150000, NULL, 0,
 NULL, 0, 1),
 
(N'Kiếm Thanh Long', 'kiem-thanh-long',
 N'Kiếm mô hình cosplay bằng nhựa chắc chắn, an toàn',
 4, NULL, NULL, 'BOTH', 280000, 30000, 100000,
 NULL, 0, 0);
GO
 
-- Product Variants (price nullable)
INSERT INTO product_variants (product_id, size, color, sku, price, stock_quantity, rental_quantity) VALUES
(1,'S',  N'Cam',       'NAR-001-S',   450000, 10, 5),
(1,'M',  N'Cam',       'NAR-001-M',   450000, 15, 8),
(1,'L',  N'Cam',       'NAR-001-L',   450000, 12, 6),
(1,'XL', N'Cam',       'NAR-001-XL',  470000,  8, 4),
(2,'S',  N'Xanh Navy', 'SAS-001-S',   480000,  8, 4),
(2,'M',  N'Xanh Navy', 'SAS-001-M',   480000, 12, 6),
(2,'L',  N'Xanh Navy', 'SAS-001-L',   480000, 10, 5),
(3,'M',  N'Đỏ',        'LUF-001-M',   420000, 15, 8),
(3,'L',  N'Đỏ',        'LUF-001-L',   420000, 12, 6),
(4,'S',  N'Hồng',      'NEZ-001-S',   520000,  6, 3),
(4,'M',  N'Hồng',      'NEZ-001-M',   520000,  8, 4),
(5,'FREE',N'Đen',      'WIG-TAN-001', 150000, 20, 0),
(6,'FREE',N'Bạc',      'WEP-001',     280000, 15, 10);
GO
 
-- Shipping Fees
INSERT INTO shipping_fees (region_name, fee, free_ship_from) VALUES
(N'Nội thành HN & HCM',    25000, 500000),
(N'Các tỉnh thành khác',   35000, 700000),
(N'Vùng xa / hải đảo',     55000, NULL);
GO
 
-- Vouchers
INSERT INTO vouchers (code, description, discount_type, discount_value,
    min_order_value, max_discount_amount, usage_limit, start_date, end_date) VALUES
('WELCOME10', N'Giảm 10% đơn đầu tiên',    'PERCENTAGE',  10, 200000, 100000, 100,
    GETDATE(), DATEADD(MONTH,3,GETDATE())),
('SUMMER50K', N'Giảm 50k cho đơn từ 500k', 'FIXED_AMOUNT', 50000, 500000, NULL, 200,
    GETDATE(), DATEADD(MONTH,2,GETDATE())),
('FREESHIP',  N'Miễn phí ship đơn từ 300k','FIXED_AMOUNT', 35000, 300000, NULL, 500,
    GETDATE(), DATEADD(MONTH,1,GETDATE()));
GO
 
-- Banners
INSERT INTO banners (title, description, image_url, link_url, display_order, is_active) VALUES
(N'Bộ Sưu Tập Mùa Hè 2024',  N'Ưu đãi lên đến 50%',       NULL, '/products',   1, 1),
(N'Cosplay Demon Slayer',      N'Trang phục hot nhất năm',   NULL, '/products',   2, 1),
(N'Flash Sale Cuối Tuần',      N'Sale up to 70%',            NULL, '/flash-sale', 3, 1);
GO
 
-- Combos
INSERT INTO combos (name, slug, description, combo_price) VALUES
(N'Combo Naruto Full Set',  'combo-naruto-full',  N'Trang phục + tóc giả Naruto', 550000),
(N'Combo Nezuko Full Set',  'combo-nezuko-full',  N'Kimono + tóc giả Nezuko',     650000);
GO
 
INSERT INTO combo_items (combo_id, product_id) VALUES (1,1),(1,5),(2,4),(2,5);
GO
 
PRINT N'=== Database DATN created successfully! ===';
GO
 -- Fix 1: Xóa UNIQUE constraint trên sku để cho phép NULL nhiều lần
-- (SQL Server không cho phép nhiều NULL trong UNIQUE constraint)
DECLARE @constraintName NVARCHAR(255)
SELECT @constraintName = name
FROM sys.key_constraints
WHERE parent_object_id = OBJECT_ID('product_variants')
  AND type = 'UQ'
  AND name LIKE '%sku%'
 
IF @constraintName IS NOT NULL
BEGIN
    EXEC('ALTER TABLE product_variants DROP CONSTRAINT ' + @constraintName)
    PRINT 'Dropped SKU unique constraint'
END
 
-- Tạo lại index unique nhưng chỉ cho các row có sku NOT NULL (filtered index)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE object_id = OBJECT_ID('product_variants')
    AND name = 'UIX_product_variants_sku_notnull'
)
BEGIN
    CREATE UNIQUE INDEX UIX_product_variants_sku_notnull
    ON product_variants(sku)
    WHERE sku IS NOT NULL;
    PRINT 'Created filtered unique index on sku (NOT NULL only)'
END
GO
 
PRINT '=== Patch v3 done ===';
GO
SELECT * 
FROM sys.indexes
WHERE object_id = OBJECT_ID('product_variants')
ALTER TABLE product_variants
DROP CONSTRAINT UQ__product___DDDF4BE7A72CB430
UPDATE order_items SET rental_status = 'ACTIVE'
WHERE item_type = 'RENTAL' AND rental_status IS NULL;