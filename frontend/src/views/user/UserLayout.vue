<!-- UserLayout.vue -->
<template>
  <div class="container py-5">
    <div class="row g-4">
      <!-- Sidebar -->
      <div class="col-lg-3">
        <div class="card border-0 shadow-sm" style="border-radius:16px;">
          <div class="card-body p-4">
            <!-- Avatar -->
            <div class="text-center mb-4">
              <div class="rounded-circle mx-auto mb-3 d-flex align-items-center justify-content-center fw-bold"
                style="width:80px;height:80px;background:linear-gradient(135deg,#e91e8c,#ff6b9d);color:#fff;font-size:2rem;">
                {{ authStore.fullName?.charAt(0)?.toUpperCase() }}
              </div>
              <h6 class="fw-bold mb-0">{{ authStore.user?.fullName }}</h6>
              <small class="text-muted">{{ authStore.user?.email }}</small>
            </div>

            <!-- Nav -->
            <nav class="nav flex-column gap-1">
              <router-link v-for="item in menuItems" :key="item.to"
                :to="item.to"
                class="nav-link rounded-3 px-3 py-2 d-flex align-items-center gap-2"
                :class="{ 'active-menu': $route.path === item.to }"
                style="color:#555;transition:all 0.2s;"
                active-class="active-menu">
                <i :class="['bi', item.icon]"></i>
                {{ item.label }}
              </router-link>
              <hr>
              <a class="nav-link rounded-3 px-3 py-2 d-flex align-items-center gap-2 text-danger"
                href="#" @click.prevent="logout">
                <i class="bi bi-box-arrow-right"></i>Đăng xuất
              </a>
            </nav>
          </div>
        </div>
      </div>

      <!-- Content -->
      <div class="col-lg-9">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

const menuItems = [
  { to: '/profile/info', icon: 'bi-person-circle', label: 'Thông tin cá nhân' },
  { to: '/profile/orders', icon: 'bi-bag-check', label: 'Đơn hàng của tôi' },
  { to: '/profile/favorites', icon: 'bi-heart', label: 'Yêu thích' },
  { to: '/profile/notifications', icon: 'bi-bell', label: 'Thông báo' },
  { to: '/profile/change-password', icon: 'bi-shield-lock', label: 'Đổi mật khẩu' }
]

const logout = () => {
  authStore.logout()
  router.push('/')
}
</script>

<style scoped>
.active-menu {
  background: linear-gradient(135deg, #fff0f6, #ffe0ef);
  color: #e91e8c !important;
  font-weight: 600;
}
</style>
