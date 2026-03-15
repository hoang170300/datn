import { createRouter, createWebHistory } from 'vue-router'

// FIX: KHÔNG import store ở top-level.
// Dùng localStorage trực tiếp trong navigation guard để tránh
// lỗi "injection Symbol(route location) not found" / Pinia chưa init.

const routes = [
  // PUBLIC
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue') },
  { path: '/products', name: 'ProductList', component: () => import('@/views/ProductList.vue') },
  { path: '/products/:slug', name: 'ProductDetail', component: () => import('@/views/ProductDetail.vue') },
  { path: '/categories/:slug', name: 'CategoryProducts', component: () => import('@/views/ProductList.vue') },
  { path: '/series/:slug', name: 'SeriesProducts', component: () => import('@/views/ProductList.vue') },
  { path: '/flash-sale', name: 'FlashSale', component: () => import('@/views/FlashSale.vue') },
  { path: '/combos', name: 'Combos', component: () => import('@/views/Combos.vue') },

  // AUTH
  { path: '/login', name: 'Login', component: () => import('@/views/auth/Login.vue'), meta: { guest: true } },
  { path: '/register', name: 'Register', component: () => import('@/views/auth/Register.vue'), meta: { guest: true } },
  { path: '/forgot-password', name: 'ForgotPassword', component: () => import('@/views/auth/ForgotPassword.vue'), meta: { guest: true } },
  { path: '/reset-password', name: 'ResetPassword', component: () => import('@/views/auth/ResetPassword.vue'), meta: { guest: true } },

  // USER (cần đăng nhập)
  { path: '/cart', name: 'Cart', component: () => import('@/views/Cart.vue'), meta: { requiresAuth: true } },
  { path: '/checkout', name: 'Checkout', component: () => import('@/views/Checkout.vue'), meta: { requiresAuth: true } },
  { path: '/order-success/:orderNumber', name: 'OrderSuccess', component: () => import('@/views/OrderSuccess.vue'), meta: { requiresAuth: true } },
  {
    path: '/profile',
    component: () => import('@/views/user/UserLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/profile/info' },
      { path: 'info', name: 'UserProfile', component: () => import('@/views/user/Profile.vue') },
      { path: 'orders', name: 'UserOrders', component: () => import('@/views/user/Orders.vue') },
      { path: 'orders/:id', name: 'OrderDetail', component: () => import('@/views/user/OrderDetail.vue') },
      { path: 'favorites', name: 'UserFavorites', component: () => import('@/views/user/Favorites.vue') },
      { path: 'notifications', name: 'Notifications', component: () => import('@/views/user/Notifications.vue') },
      { path: 'change-password', name: 'ChangePassword', component: () => import('@/views/user/ChangePassword.vue') }
    ]
  },

  // ADMIN
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/Dashboard.vue') },
      { path: 'products', name: 'AdminProducts', component: () => import('@/views/admin/Products.vue') },
      { path: 'products/create', name: 'AdminProductCreate', component: () => import('@/views/admin/ProductForm.vue') },
      { path: 'products/:id/edit', name: 'AdminProductEdit', component: () => import('@/views/admin/ProductForm.vue') },
      { path: 'categories', name: 'AdminCategories', component: () => import('@/views/admin/Categories.vue') },
      { path: 'series', name: 'AdminSeries', component: () => import('@/views/admin/Series.vue') },
      { path: 'orders', name: 'AdminOrders', component: () => import('@/views/admin/Orders.vue') },
      { path: 'orders/:id', name: 'AdminOrderDetail', component: () => import('@/views/admin/OrderDetail.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/Users.vue') },
      { path: 'vouchers', name: 'AdminVouchers', component: () => import('@/views/admin/Vouchers.vue') },
      { path: 'banners', name: 'AdminBanners', component: () => import('@/views/admin/Banners.vue') },
      { path: 'flash-sales', name: 'AdminFlashSales', component: () => import('@/views/admin/FlashSales.vue') },
      { path: 'pos', name: 'AdminPos', component: () => import('@/views/admin/Pos.vue') },
      { path: 'rentals', name: 'AdminRentals', component: () => import('@/views/admin/Rentals.vue') }
    ]
  },

  // 404
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/NotFound.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0 }
  }
})

// Navigation guard — đọc auth state từ localStorage thay vì Pinia store
// Tránh lỗi "injection not found" do Pinia chưa được mount lúc router khởi tạo
router.beforeEach((to, from, next) => {
  const token   = localStorage.getItem('token')
  const userRaw = localStorage.getItem('user')
  const isLoggedIn = !!token
  const isAdmin = userRaw ? JSON.parse(userRaw)?.role === 'ROLE_ADMIN' : false

  if (to.meta.requiresAuth && !isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.meta.requiresAdmin && !isAdmin) {
    next({ name: 'Home' })
  } else if (to.meta.guest && isLoggedIn) {
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
