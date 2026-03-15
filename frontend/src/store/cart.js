import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { cartApi } from '@/api/index'
 
const emptyCart = () => ({ id: null, items: [], totalItems: 0, totalPrice: 0, totalDeposit: 0, grandTotal: 0 })
 
export const useCartStore = defineStore('cart', () => {
  const cart    = ref(emptyCart())
  const loading = ref(false)
 
  // Đảm bảo null/undefined → 0, parse Number an toàn
  const totalItems   = computed(() => Number(cart.value?.totalItems)   || 0)
  const totalPrice   = computed(() => Number(cart.value?.totalPrice)   || 0)
  const totalDeposit = computed(() => Number(cart.value?.totalDeposit) || 0)
  const grandTotal   = computed(() => {
    // Nếu backend trả grandTotal thì dùng, không thì tự tính
    const g = Number(cart.value?.grandTotal)
    if (g && !isNaN(g)) return g
    return totalPrice.value + totalDeposit.value
  })
  const items = computed(() => cart.value?.items || [])
 
  // Hàm set cart an toàn — đảm bảo không bao giờ set undefined/null
  function setCart(data) {
    if (!data) return
    cart.value = {
      id:           data.id ?? null,
      items:        data.items ?? [],
      totalItems:   Number(data.totalItems)   || 0,
      totalPrice:   Number(data.totalPrice)   || 0,
      totalDeposit: Number(data.totalDeposit) || 0,
      grandTotal:   Number(data.grandTotal)   || (Number(data.totalPrice||0) + Number(data.totalDeposit||0))
    }
  }
 
  async function fetchCart() {
    try {
      loading.value = true
      const res = await cartApi.getCart()
      setCart(res.data.data)
    } catch (e) {
      // Chưa đăng nhập → bỏ qua
    } finally {
      loading.value = false
    }
  }
 
  async function addItem(payload) {
    loading.value = true
    try {
      const res = await cartApi.addItem(payload)
      setCart(res.data.data)
      return res.data
    } finally {
      loading.value = false
    }
  }
 
  async function updateQuantity(itemId, quantity) {
    const res = await cartApi.updateQuantity(itemId, quantity)
    setCart(res.data.data)
  }
 
  async function removeItem(itemId) {
    const res = await cartApi.removeItem(itemId)
    setCart(res.data.data)
  }
 
  async function clearCart() {
    await cartApi.clearCart()
    cart.value = emptyCart()
  }
 
  function reset() {
    cart.value = emptyCart()
  }
 
  return {
    cart, loading, totalItems, totalPrice, totalDeposit, grandTotal, items,
    fetchCart, addItem, updateQuantity, removeItem, clearCart, reset
  }
})