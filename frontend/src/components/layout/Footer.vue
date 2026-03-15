<template>
  <footer style="background: #1a1a2e; color: #aaa; margin-top: 60px;">
    <div class="container py-5">
      <div class="row g-4">
        <div class="col-lg-4">
          <h5 style="color: #e91e8c; font-weight: 900;">⭐ CosPlay Shop</h5>
          <p class="small mt-2">Chuyên cung cấp trang phục cosplay chất lượng cao. Mua & thuê trang phục anime, game, phim.</p>
          <div class="d-flex gap-3 mt-3">
            <a href="#" class="text-white"><i class="bi bi-facebook fs-5"></i></a>
            <a href="#" class="text-white"><i class="bi bi-instagram fs-5"></i></a>
            <a href="#" class="text-white"><i class="bi bi-tiktok fs-5"></i></a>
            <a href="#" class="text-white"><i class="bi bi-youtube fs-5"></i></a>
          </div>
        </div>
        <div class="col-lg-2">
          <h6 class="text-white mb-3">Danh mục</h6>
          <ul class="list-unstyled small">
            <li class="mb-2"><router-link class="text-secondary text-decoration-none" to="/products?categoryId=1">Cosplay Nam</router-link></li>
            <li class="mb-2"><router-link class="text-secondary text-decoration-none" to="/products?categoryId=2">Cosplay Nữ</router-link></li>
            <li class="mb-2"><router-link class="text-secondary text-decoration-none" to="/products?categoryId=3">Phụ kiện</router-link></li>
            <li class="mb-2"><router-link class="text-secondary text-decoration-none" to="/combos">Combo Set</router-link></li>
          </ul>
        </div>
        <div class="col-lg-2">
          <h6 class="text-white mb-3">Hỗ trợ</h6>
          <ul class="list-unstyled small">
            <li class="mb-2"><a class="text-secondary text-decoration-none" href="#">Chính sách đổi trả</a></li>
            <li class="mb-2"><a class="text-secondary text-decoration-none" href="#">Hướng dẫn thuê đồ</a></li>
            <li class="mb-2"><a class="text-secondary text-decoration-none" href="#">Bảng size</a></li>
            <li class="mb-2"><a class="text-secondary text-decoration-none" href="#">Câu hỏi thường gặp</a></li>
          </ul>
        </div>
        <div class="col-lg-4">
          <h6 class="text-white mb-3">Liên hệ</h6>
          <ul class="list-unstyled small">
            <li class="mb-2"><i class="bi bi-geo-alt me-2" style="color:#e91e8c;"></i>123 Đường Cosplay, Q.1, TP.HCM</li>
            <li class="mb-2"><i class="bi bi-telephone me-2" style="color:#e91e8c;"></i>0909 123 456</li>
            <li class="mb-2"><i class="bi bi-envelope me-2" style="color:#e91e8c;"></i>hello@cosplayshop.vn</li>
          </ul>
          <div class="mt-3">
            <span class="badge me-2" style="background:#e91e8c;">COD</span>
            <span class="badge me-2 bg-secondary">Chuyển khoản</span>
            <span class="badge bg-success">Ví điện tử</span>
          </div>
        </div>
      </div>
    </div>
    <div class="text-center py-3 small" style="background: #111; color: #666;">
      © 2024 CosPlay Shop. All rights reserved.
    </div>
  </footer>
</template>
