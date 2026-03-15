import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/index'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || null)
  const user  = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin    = computed(() => user.value?.role === 'ROLE_ADMIN')
  const fullName   = computed(() => user.value?.fullName || user.value?.username || '')

  async function login(credentials) {
    const res = await authApi.login(credentials)
    const data = res.data.data
    token.value = data.accessToken
    user.value  = {
      id:       data.id,
      username: data.username,
      email:    data.email,
      fullName: data.fullName,
      role:     data.role
    }
    localStorage.setItem('token', data.accessToken)
    localStorage.setItem('user',  JSON.stringify(user.value))

    // Load cart sau khi login (import lười để tránh circular)
    const { useCartStore } = await import('@/store/cart')
    await useCartStore().fetchCart()
  }

  async function register(payload) {
    await authApi.register(payload)
  }

  function logout() {
    token.value = null
    user.value  = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    import('@/store/cart').then(({ useCartStore }) => useCartStore().reset())
  }

  return { token, user, isLoggedIn, isAdmin, fullName, login, register, logout }
})
