<!-- OrderSuccess.vue -->
<template>
  <div class="min-vh-100 d-flex align-items-center justify-content-center">
    <div class="text-center px-4">
      <div class="mb-4" style="font-size:6rem;">🎉</div>
      <h2 class="fw-bold mb-2">Đặt hàng thành công!</h2>
      <p class="text-muted mb-1">Mã đơn hàng của bạn:</p>
      <div class="badge fs-5 px-4 py-2 mb-4"
        style="background:linear-gradient(135deg,#e91e8c,#ff6b9d);color:#fff;border-radius:12px;">
        #{{ route.params.orderNumber }}
      </div>
      <p class="text-muted mb-5">
        Chúng tôi sẽ xác nhận đơn hàng và liên hệ với bạn sớm nhất.<br>
        Cảm ơn bạn đã tin tưởng CosPlay Shop! ❤️
      </p>
      <div class="d-flex gap-3 justify-content-center flex-wrap">
        <router-link to="/profile/orders" class="btn px-4 rounded-pill"
          style="background:#e91e8c;color:#fff;">
          <i class="bi bi-bag-check me-1"></i>Xem đơn hàng
        </router-link>
        <router-link to="/" class="btn btn-outline-secondary px-4 rounded-pill">
          <i class="bi bi-house me-1"></i>Về trang chủ
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
</script>
