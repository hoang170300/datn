<template>
  <nav class="navbar navbar-expand-lg sticky-top" style="background: #1a1a2e; box-shadow: 0 2px 20px rgba(0,0,0,0.3);">
    <div class="container">
      <!-- Logo -->
      <router-link class="navbar-brand" to="/">
        <span style="color: #e91e8c; font-weight: 900; font-size: 1.5rem;">OtakuCosPlay</span>
  
      </router-link>

      <!-- Mobile toggle -->
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarMain">
        <i class="bi bi-list text-white fs-4"></i>
      </button>

      <div class="collapse navbar-collapse" id="navbarMain">
        <!-- Search Bar -->
        <form class="d-flex mx-auto" style="width: 360px;" @submit.prevent="handleSearch">
          <div class="input-group">
            <input
              v-model="searchQuery"
              type="text"
              class="form-control"
              placeholder="Tìm trang phục, nhân vật..."
              style="border-radius: 20px 0 0 20px; border: none;"
            />
            <button class="btn" type="submit"
              style="background: #e91e8c; color: white; border-radius: 0 20px 20px 0;">
              <i class="bi bi-search"></i>
            </button>
          </div>
        </form>

        <!-- Nav Links -->
        <ul class="navbar-nav ms-auto align-items-center gap-1">
          <li class="nav-item">
            <router-link class="nav-link text-white" to="/products">
              <i class="bi bi-grid me-1"></i>Sản phẩm
            </router-link>
          </li>
       
          <li class="nav-item">
            <router-link class="nav-link" to="/flash-sale"
              style="color: #ff4757; font-weight: 600;">
              <i class="bi bi-lightning-charge-fill me-1"></i>Flash Sale
            </router-link>
          </li>

          <!-- Cart -->
          <li class="nav-item">
            <router-link class="nav-link text-white position-relative" to="/cart">
              <i class="bi bi-cart3 fs-5"></i>
              <span v-if="cartStore.totalItems > 0"
                class="position-absolute top-0 start-100 translate-middle badge rounded-pill"
                style="background: #e91e8c; font-size: 0.65rem;">
                {{ cartStore.totalItems }}
              </span>
            </router-link>
          </li>

          <!-- User Menu -->
          <li v-if="authStore.isLoggedIn" class="nav-item dropdown">
            <a class="nav-link dropdown-toggle text-white d-flex align-items-center gap-2"
               href="#" data-bs-toggle="dropdown">
              <div class="rounded-circle d-flex align-items-center justify-content-center"
                   style="width:32px;height:32px;background:#e91e8c;font-size:0.85rem;font-weight:700;">
                {{ authStore.fullName?.charAt(0)?.toUpperCase() }}
              </div>
              <span class="d-none d-lg-inline" style="font-size:0.9rem;">{{ authStore.fullName }}</span>
            </a>
            <ul class="dropdown-menu dropdown-menu-end" style="border: none; box-shadow: 0 8px 30px rgba(0,0,0,0.15);">
              <li>
                <router-link class="dropdown-item" to="/profile/info">
                  <i class="bi bi-person me-2 text-primary"></i>Tài khoản
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item" to="/profile/orders">
                  <i class="bi bi-bag-check me-2 text-primary"></i>Đơn hàng
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item" to="/profile/favorites">
                  <i class="bi bi-heart me-2 text-danger"></i>Yêu thích
                </router-link>
              </li>
              <li>
                <router-link class="dropdown-item" to="/profile/notifications">
                  <i class="bi bi-bell me-2 text-warning"></i>Thông báo
                  <span v-if="unreadCount > 0" class="badge text-bg-danger ms-1">{{ unreadCount }}</span>
                </router-link>
              </li>
              <template v-if="authStore.isAdmin">
                <li><hr class="dropdown-divider"></li>
                <li>
                  <router-link class="dropdown-item" to="/admin">
                    <i class="bi bi-shield-lock me-2 text-warning"></i>Quản trị
                  </router-link>
                </li>
              </template>
              <li><hr class="dropdown-divider"></li>
              <li>
                <a class="dropdown-item text-danger" href="#" @click.prevent="logout">
                  <i class="bi bi-box-arrow-right me-2"></i>Đăng xuất
                </a>
              </li>
            </ul>
          </li>

          <!-- Guest buttons -->
          <template v-else>
            <li class="nav-item">
              <router-link class="btn btn-outline-light btn-sm" to="/login">Đăng nhập</router-link>
            </li>
            <li class="nav-item ms-2">
              <router-link class="btn btn-sm" to="/register"
                style="background:#e91e8c;color:white;border:none;">Đăng ký</router-link>
            </li>
          </template>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { useCartStore } from '@/store/cart'
import { userApi } from '@/api/index'
import api from '@/api/index'

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()
const searchQuery = ref('')
const unreadCount = ref(0)

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ name: 'ProductList', query: { keyword: searchQuery.value.trim() } })
    searchQuery.value = ''
  }
}

const logout = () => {
  authStore.logout()
  router.push('/')
}

onMounted(async () => {
  if (authStore.isLoggedIn) {
    cartStore.fetchCart()
    fetchUnread()
    // Poll thông báo mỗi 30 giây
    setInterval(fetchUnread, 30000)
  }
})

const fetchUnread = async () => {
  try {
    const res = await api.get('/notifications/unread-count')
    unreadCount.value = res.data.data || 0
  } catch (e) {}
}
</script>
