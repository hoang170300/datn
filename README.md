# ⭐ CosPlay Shop - Full Stack Web Application

Ứng dụng mua & thuê trang phục cosplay với **Spring Boot** + **Vue 3** + **SQL Server** + **JWT**.

---

## 🗂️ Cây thư mục dự án

```
cosplay-shop/
├── database/
│   └── schema_full.sql              ← SQL hoàn chỉnh (chạy trước)
│
├── backend/                         ← Spring Boot (Port 8080)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/cosplay/
│       │   ├── CosplayApplication.java
│       │   ├── config/
│       │   │   └── SecurityConfig.java
│       │   ├── entity/
│       │   │   └── AllEntities.java     ← Tất cả Entity
│       │   ├── repository/
│       │   │   └── AllRepositories.java ← Tất cả Repository
│       │   ├── dto/
│       │   │   └── AllDTOs.java         ← Request & Response DTOs
│       │   ├── service/
│       │   │   ├── AuthServiceAndController.java
│       │   │   ├── ProductServiceAndController.java
│       │   │   ├── CartAndOrderService.java
│       │   │   └── OtherServices.java   ← User, Review, Favorite...
│       │   ├── security/
│       │   │   ├── JwtTokenProvider.java
│       │   │   ├── JwtAuthenticationFilter.java
│       │   │   └── CustomUserDetailsService.java
│       │   └── exception/
│       │       └── GlobalExceptionHandler.java (trong OtherServices.java)
│       └── resources/
│           └── application.properties
│
└── frontend/                        ← Vue 3 (Port 5173)
    ├── index.html
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── main.js
        ├── App.vue
        ├── router/
        │   └── index.js             ← Tất cả routes + guards
        ├── store/
        │   └── index.js             ← auth, cart, product stores (Pinia)
        ├── api/
        │   └── index.js             ← axios + authApi, productApi, cartApi, orderApi, userApi
        ├── views/
        │   ├── Home.vue             ← Trang chủ (banner, sản phẩm hot, new, series)
        │   ├── ProductList.vue      ← Danh sách + bộ lọc sản phẩm
        │   ├── ProductDetail.vue    ← Chi tiết sản phẩm + đánh giá
        │   ├── Cart.vue             ← Giỏ hàng
        │   ├── Checkout.vue         ← Thanh toán
        │   ├── OrderSuccess.vue     ← Đặt hàng thành công
        │   ├── NotFound.vue         ← 404
        │   ├── auth/
        │   │   ├── Login.vue
        │   │   ├── Register.vue
        │   │   └── ForgotPassword.vue
        │   ├── user/
        │   │   ├── UserLayout.vue   ← Layout sidebar user
        │   │   ├── Profile.vue      ← Thông tin cá nhân
        │   │   ├── Orders.vue       ← Lịch sử đơn hàng
        │   │   ├── Favorites.vue    ← Yêu thích
        │   │   ├── Notifications.vue← Thông báo
        │   │   └── ChangePassword.vue
        │   └── admin/
        │       ├── AdminLayout.vue  ← Layout sidebar admin
        │       ├── Dashboard.vue    ← Thống kê tổng quan
        │       ├── Products.vue     ← Quản lý sản phẩm
        │       ├── Orders.vue       ← Quản lý đơn hàng
        │       ├── Users.vue        ← Quản lý người dùng
        │       └── Vouchers.vue     ← Quản lý voucher
        └── components/
            ├── layout/
            │   ├── Navbar.vue       ← Thanh điều hướng
            │   └── Footer.vue
            └── product/
                └── ProductCard.vue  ← Card sản phẩm tái sử dụng
```

---

## 🚀 Hướng dẫn chạy

### 1. Database (SQL Server)
```sql
-- Mở SQL Server Management Studio hoặc Azure Data Studio
-- Chạy file: database/schema_full.sql
-- Database DATN sẽ được tạo với đầy đủ bảng và dữ liệu mẫu
```

### 2. Backend (Spring Boot)
```bash
cd backend

# Sửa thông tin kết nối DB trong:
# src/main/resources/application.properties
# spring.datasource.password=YourPassword123!

# Chạy
./mvnw spring-boot:run
# hoặc
mvn spring-boot:run

# API chạy tại: http://localhost:8080/api
# Swagger UI: http://localhost:8080/api/swagger-ui.html
```

### 3. Frontend (Vue 3)
```bash
cd frontend
npm install
npm run dev

# Chạy tại: http://localhost:5173
```

---

## 🔑 Tài khoản mặc định

| Tài khoản | Mật khẩu      | Quyền |
|-----------|---------------|-------|
| admin     | Admin@123     | ADMIN |
| user1     | Admin@123     | USER  |

---

## 📋 API Endpoints chính

### Auth
```
POST /api/auth/login          → Đăng nhập
POST /api/auth/register       → Đăng ký
POST /api/auth/forgot-password → Quên mật khẩu
POST /api/auth/reset-password → Đặt lại mật khẩu
```

### Products (Public)
```
GET  /api/products            → Danh sách + lọc + tìm kiếm
GET  /api/products/hot        → Sản phẩm nổi bật
GET  /api/products/new        → Sản phẩm mới
GET  /api/products/{slug}     → Chi tiết sản phẩm
GET  /api/products/{id}/rental-availability?startDate=&endDate= → Kiểm tra tồn kho thuê
GET  /api/categories          → Danh mục
GET  /api/series              → Series
GET  /api/banners             → Banners
```

### Cart (Cần đăng nhập)
```
GET    /api/cart              → Xem giỏ hàng
POST   /api/cart/items        → Thêm vào giỏ
PATCH  /api/cart/items/{id}   → Cập nhật số lượng
DELETE /api/cart/items/{id}   → Xóa item
DELETE /api/cart              → Xóa toàn bộ
```

### Orders (Cần đăng nhập)
```
POST  /api/orders                     → Đặt hàng
GET   /api/orders                     → Lịch sử đơn hàng
GET   /api/orders/{id}                → Chi tiết đơn
PATCH /api/orders/{id}/confirm-delivery → Xác nhận nhận hàng
```

### Admin (Cần role ADMIN)
```
GET    /api/admin/products            → DS sản phẩm
POST   /api/admin/products            → Thêm sản phẩm
PUT    /api/admin/products/{id}       → Sửa sản phẩm
PATCH  /api/admin/products/{id}/toggle-status → Bật/tắt
GET    /api/admin/orders              → DS đơn hàng
PATCH  /api/admin/orders/{id}/status  → Cập nhật trạng thái
GET    /api/admin/users               → DS người dùng
PATCH  /api/admin/users/{id}/toggle-status → Khóa/mở tài khoản
```

---

## 🛠️ Tech Stack

| Layer    | Technology |
|----------|------------|
| Backend  | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Database | Microsoft SQL Server |
| Auth     | JWT (jjwt 0.12.3), BCrypt |
| Frontend | Vue 3, Pinia, Vue Router 4, Axios |
| UI       | Bootstrap 5.3, Bootstrap Icons |
| Build    | Vite 5, Maven |

---

## ⚠️ Lưu ý quan trọng

### Tách Entity thành file riêng
Trong project thực tế, **tách mỗi Entity/Service/Controller ra file riêng**:
```
entity/
  Role.java
  User.java
  Product.java
  ...
service/
  AuthService.java
  ProductService.java
  ...
controller/
  AuthController.java
  ProductController.java
  ...
```
Các file `AllEntities.java`, `AllRepositories.java`... trong repo này **chỉ để tham khảo gộp cho tiện** — cần tách ra trước khi compile.

### Tách API file frontend
```
api/
  axios.js       ← Base instance
  auth.js        ← authApi
  product.js     ← productApi
  cart.js        ← cartApi
  order.js       ← orderApi
  user.js        ← userApi
```

### Cần implement thêm
- [ ] Upload ảnh sản phẩm (Multipart + lưu file/S3)
- [ ] Gửi email reset password (Spring Mail)
- [ ] Trang admin tạo/sửa sản phẩm (ProductForm.vue)
- [ ] Trang admin quản lý Banner, FlashSale
- [ ] Voucher Controller phía backend
- [ ] User Controller phía backend (profile, notifications)
- [ ] Notification Controller + Service
- [ ] Review Controller + Service
- [ ] Category + Series admin controllers
- [ ] Combo + FlashSale controllers
