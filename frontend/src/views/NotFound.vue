<!-- NotFound.vue -->
<template>
  <div class="min-vh-100 d-flex align-items-center justify-content-center text-center px-4">
    <div>
      <div style="font-size:8rem;line-height:1;">🧟</div>
      <h1 class="fw-black mt-3" style="font-size:5rem;color:#e91e8c;">404</h1>
      <h4 class="fw-bold text-dark mb-2">Trang không tìm thấy</h4>
      <p class="text-muted mb-5">Trang bạn tìm kiếm đã bị cosplay thành trang khác rồi!</p>
      <div class="d-flex gap-3 justify-content-center">
        <button class="btn btn-outline-secondary px-4 rounded-pill" @click="$router.back()">
          <i class="bi bi-arrow-left me-1"></i>Quay lại
        </button>
        <router-link to="/" class="btn px-4 rounded-pill"
          style="background:#e91e8c;color:#fff;">
          <i class="bi bi-house me-1"></i>Trang chủ
        </router-link>
      </div>
    </div>
  </div>
</template>
