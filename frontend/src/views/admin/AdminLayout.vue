<template>
  <div class="d-flex" style="min-height:100vh;background:#f0f2f5;">

    <!-- Sidebar -->
    <div class="d-flex flex-column"
      style="width:260px;min-height:100vh;position:fixed;left:0;top:0;z-index:100;
             background:linear-gradient(180deg,#1a1a2e 0%,#16213e 60%,#0f3460 100%);
             box-shadow:4px 0 24px rgba(0,0,0,0.35);">

      <!-- Logo → link trang chủ -->
      <router-link to="/" class="text-decoration-none">
        <div class="px-4 py-4 text-center"
          style="border-bottom:1px solid rgba(255,255,255,0.08);
                 transition:background 0.2s;cursor:pointer;"
          @mouseenter="$event.currentTarget.style.background='rgba(233,30,140,0.08)'"
          @mouseleave="$event.currentTarget.style.background='transparent'">
          <div style="font-size:1.4rem;font-weight:900;
                      background:linear-gradient(135deg,#e91e8c,#ff6bcb);
                      -webkit-background-clip:text;-webkit-text-fill-color:transparent;">
            ⭐ CosPlay
          </div>
          <div class="mt-1" style="color:#7c8db0;font-size:0.72rem;letter-spacing:2px;text-transform:uppercase;">
            Admin Panel
          </div>
        </div>
      </router-link>

      <!-- Nav -->
      <nav class="flex-grow-1 px-3 py-3" style="overflow-y:auto;">
        <div v-for="group in menuGroups" :key="group.label" class="mb-4">
          <small class="px-2 mb-2 d-block"
            style="color:#4a5568;font-size:0.63rem;letter-spacing:2px;
                   text-transform:uppercase;font-weight:700;">
            {{ group.label }}
          </small>
          <router-link v-for="item in group.items" :key="item.to"
            :to="item.to"
            class="d-flex align-items-center gap-3 px-3 py-2 rounded-3 mb-1 text-decoration-none nav-item-link"
            active-class="nav-item-active">
            <div class="nav-icon-wrap">
              <i :class="['bi', item.icon]"></i>
            </div>
            <span>{{ item.label }}</span>
          </router-link>
        </div>
      </nav>

      <!-- User + logout -->
      <div class="px-3 pb-4" style="border-top:1px solid rgba(255,255,255,0.07);">
        <div class="d-flex align-items-center gap-3 px-3 py-3 rounded-3 mt-3"
          style="background:rgba(255,255,255,0.05);">
          <div class="rounded-circle d-flex align-items-center justify-content-center fw-bold flex-shrink-0"
            style="width:34px;height:34px;background:linear-gradient(135deg,#e91e8c,#ff6bcb);
                   color:#fff;font-size:0.85rem;">
            {{ authStore.fullName?.charAt(0)?.toUpperCase() || 'A' }}
          </div>
          <div style="min-width:0;">
            <div class="fw-semibold text-truncate" style="color:#e2e8f0;font-size:0.82rem;">
              {{ authStore.fullName || 'Admin' }}
            </div>
            <div style="color:#4a5568;font-size:0.7rem;">Administrator</div>
          </div>
        </div>
        <button class="btn w-100 mt-2 py-2 rounded-3 d-flex align-items-center justify-content-center gap-2"
          style="background:rgba(239,68,68,0.1);color:#fc8181;border:1px solid rgba(239,68,68,0.2);
                 font-size:0.82rem;transition:all 0.2s;"
          @mouseenter="$event.currentTarget.style.background='rgba(239,68,68,0.2)'"
          @mouseleave="$event.currentTarget.style.background='rgba(239,68,68,0.1)'"
          @click="authStore.logout();$router.push('/login')">
          <i class="bi bi-box-arrow-left"></i> Đăng xuất
        </button>
      </div>
    </div>

    <!-- Main -->
    <div class="flex-grow-1" style="margin-left:260px;">
      <!-- Topbar -->
      <div class="d-flex align-items-center justify-content-between px-4 py-3 bg-white sticky-top"
        style="box-shadow:0 1px 12px rgba(0,0,0,0.07);z-index:99;">
        <div>
          <h6 class="mb-0 fw-bold">{{ currentPageTitle }}</h6>
          <small class="text-muted">{{ breadcrumb }}</small>
        </div>
        <div class="d-flex align-items-center gap-3">
          <router-link to="/" class="btn btn-sm btn-outline-secondary rounded-pill"
            style="font-size:0.78rem;">
            <i class="bi bi-house me-1"></i>Trang chủ
          </router-link>
        </div>
      </div>

      <!-- Content -->
      <div class="p-4">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const route = useRoute()
const authStore = useAuthStore()

const menuGroups = [
  {
    label: 'Tổng quan',
    items: [
      { to: '/admin/dashboard', icon: 'bi-speedometer2', label: 'Dashboard' }
    ]
  },
  {
    label: 'Sản phẩm',
    items: [
      { to: '/admin/products',   icon: 'bi-bag',            label: 'Sản phẩm' },
      { to: '/admin/categories', icon: 'bi-collection',     label: 'Danh mục' },
      { to: '/admin/series',     icon: 'bi-film',           label: 'Series' }
    ]
  },
  {
    label: 'Bán hàng',
    items: [
      { to: '/admin/orders',     icon: 'bi-bag-check',          label: 'Đơn hàng' },
      { to: '/admin/pos',        icon: 'bi-shop',               label: 'POS Quầy' },
      { to: '/admin/rentals',    icon: 'bi-calendar2-check',    label: 'Thuê đồ' },
      { to: '/admin/vouchers',   icon: 'bi-ticket-perforated',  label: 'Voucher' },
      { to: '/admin/flash-sales',icon: 'bi-lightning-charge',   label: 'Flash Sale' }
    ]
  },
  {
    label: 'Marketing',
    items: [
      { to: '/admin/banners', icon: 'bi-image', label: 'Banners' }
    ]
  },
  {
    label: 'Hệ thống',
    items: [
      { to: '/admin/users', icon: 'bi-people', label: 'Người dùng' }
    ]
  }
]

const allItems = menuGroups.flatMap(g => g.items)
const currentItem = computed(() => allItems.find(i => route.path.startsWith(i.to)))
const currentPageTitle = computed(() => currentItem.value?.label || 'Admin')
const breadcrumb = computed(() => {
  const g = menuGroups.find(g => g.items.some(i => route.path.startsWith(i.to)))
  return g ? `${g.label} / ${currentItem.value?.label}` : ''
})
</script>

<style scoped>
.nav-item-link {
  color: #8892a4;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.18s;
}
.nav-item-link:hover {
  color: #e2e8f0;
  background: rgba(255,255,255,0.06) !important;
}
.nav-icon-wrap {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 7px;
  font-size: 0.95rem;
  background: rgba(255,255,255,0.05);
  transition: all 0.18s;
  flex-shrink: 0;
}
.nav-item-link:hover .nav-icon-wrap {
  background: rgba(233,30,140,0.18);
  color: #e91e8c;
}
.nav-item-active {
  color: #fff !important;
  background: rgba(233,30,140,0.18) !important;
  font-weight: 600;
}
.nav-item-active .nav-icon-wrap {
  background: #e91e8c !important;
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(233,30,140,0.4);
}
</style>
