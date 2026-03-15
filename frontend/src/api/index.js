import axios from 'axios'
 
const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})
 
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
}, error => Promise.reject(error))
 
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)
 
export default api
 
export const authApi = {
  login:         (data)           => api.post('/auth/login', data),
  register:      (data)           => api.post('/auth/register', data),
  forgotPassword:(email)          => api.post(`/auth/forgot-password?email=${email}`),
  resetPassword: (token, newPass) => api.post(`/auth/reset-password?token=${token}&newPassword=${newPass}`)
}
 
export const productApi = {
  getProducts:            (params)                 => api.get('/products', { params }),
  getProductDetail:       (slug)                   => api.get(`/products/${slug}`),
  getHotProducts:         (limit = 8)              => api.get(`/products/hot?limit=${limit}`),
  getNewProducts:         (limit = 8)              => api.get(`/products/new?limit=${limit}`),
  checkRentalAvailability:(id, startDate, endDate) =>
    api.get(`/products/${id}/rental-availability`, { params: { startDate, endDate } }),
  getCategories:  () => api.get('/categories'),
  getSeriesList:  () => api.get('/series'),
  getBanners:     () => api.get('/banners'),
  getFlashSales:  () => api.get('/flash-sales/active'),
  getCombos:      () => api.get('/combos'),
  getReviews:     (productId, params) => api.get(`/reviews/product/${productId}`, { params }),
  addReview:      (data)              => api.post('/reviews', data),
  toggleFavorite: (productId) => api.post(`/favorites/toggle/${productId}`),
  getFavorites:   (params)    => api.get('/favorites', { params }),
  isFavorite:     (productId) => api.get(`/favorites/check/${productId}`),
  admin: {
    getProducts:    (params)     => api.get('/admin/products', { params }),
    createProduct:  (data)       => api.post('/admin/products', data),
    updateProduct:  (id, data)   => api.put(`/admin/products/${id}`, data),
    toggleStatus:   (id)         => api.patch(`/admin/products/${id}/toggle-status`),
    getCategories:  ()           => api.get('/admin/categories'),
    createCategory: (data)       => api.post('/admin/categories', data),
    updateCategory: (id, data)   => api.put(`/admin/categories/${id}`, data),
    toggleCategory: (id)         => api.patch(`/admin/categories/${id}/toggle`),
    getSeries:      ()           => api.get('/admin/series'),
    createSeries:   (data)       => api.post('/admin/series', data),
    updateSeries:   (id, data)   => api.put(`/admin/series/${id}`, data),
    toggleSeries:   (id)         => api.patch(`/admin/series/${id}/toggle`)
  }
}
 
export const cartApi = {
  getCart:        ()            => api.get('/cart'),
  addItem:        (data)        => api.post('/cart/items', data),
  updateQuantity: (itemId, qty) => api.patch(`/cart/items/${itemId}?quantity=${qty}`),
  removeItem:     (itemId)      => api.delete(`/cart/items/${itemId}`),
  clearCart:      ()            => api.delete('/cart')
}
 
export const orderApi = {
  createOrder:     (data)         => api.post('/orders', data),
  getMyOrders:     (params)       => api.get('/orders', { params }),
  getOrderDetail:  (id)           => api.get(`/orders/${id}`),
  confirmDelivery: (id)           => api.patch(`/orders/${id}/confirm-delivery`),
  cancelOrder:     (id, reason)    => api.patch(`/orders/${id}/cancel`, { reason }),
  validateVoucher: (code, amount) => api.get(`/vouchers/validate?code=${code}&amount=${amount}`),
  admin: {
    getOrders:    (params)     => api.get('/admin/orders', { params }),
    updateStatus: (id, status) => api.patch(`/admin/orders/${id}/status?status=${status}`)
  }
}
 
export const userApi = {
  getProfile:           ()       => api.get('/users/profile'),
  updateProfile:        (data)   => api.put('/users/profile', data),
  changePassword:       (data)   => api.put('/users/change-password', data),
  getNotifications:     (params) => api.get('/notifications', { params }),
  markNotificationRead: (id)     => api.patch(`/notifications/${id}/read`),
  markAllRead:          ()       => api.patch('/notifications/read-all'),
  getUnreadCount:       ()       => api.get('/notifications/unread-count'),
  admin: {
    getUsers:     (params)    => api.get('/admin/users', { params }),
    toggleStatus: (id)        => api.patch(`/admin/users/${id}/toggle-status`),
    changeRole:   (id, role)  => api.patch(`/admin/users/${id}/role?role=${role}`)
  }
}
