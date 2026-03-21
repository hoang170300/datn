<template>
    <div class="container py-5">
      <!-- Header -->
      <div class="text-center mb-5">
        <h2 class="fw-black" style="color:#ff4757;"> FLASH SALE</h2>
        <p class="text-muted">Ưu đãi khủng - Số lượng có hạn - Mua ngay kẻo hết!</p>
      </div>
   
      <!-- Active Sale Countdown -->
      <div v-if="activeSale" class="rounded-4 p-4 mb-5 text-white text-center"
        style="background:linear-gradient(135deg,#ff4757,#e91e8c);">
        <h4 class="fw-bold mb-2">{{ activeSale.name }}</h4>
        <p class="mb-3 opacity-75">Kết thúc lúc {{ formatDateTime(activeSale.endTime) }}</p>
        <div class="d-flex justify-content-center gap-3">
          <div v-for="(unit, label) in countdownUnits" :key="label"
            class="rounded-3 p-3" style="background:rgba(255,255,255,0.2);min-width:70px;">
            <div class="fw-black" style="font-size:2rem;">{{ unit }}</div>
            <small class="opacity-75">{{ label }}</small>
          </div>
        </div>
      </div>
   
      <!-- No active sale -->
      <div v-else class="text-center py-5">
        <h5 class="text-muted mt-3">Hiện chưa có flash sale nào đang diễn ra</h5>
        <p class="text-muted">Hãy quay lại sau để không bỏ lỡ ưu đãi!</p>
        <router-link to="/products" class="btn rounded-pill mt-2 px-4"
          style="background:#e91e8c;color:#fff;">
          Xem tất cả sản phẩm
        </router-link>
      </div>
   
      <!-- Products on sale (fallback: show hot products) -->
      <div v-if="products.length">
        <h5 class="fw-bold mb-4"> Sản phẩm trong Flash Sale</h5>
        <div class="row g-3">
          <div v-for="product in products" :key="product.id" class="col-6 col-md-3">
            <ProductCard :product="product" />
          </div>
        </div>
      </div>
    </div>
  </template>
   
  <script setup>
  import { ref, computed, onMounted, onUnmounted } from 'vue'
  import { productApi } from '@/api/index'
  import ProductCard from '@/components/product/ProductCard.vue'
   
  const flashSales = ref([])
  const products = ref([])
  let timer = null
   
  const activeSale = computed(() => {
    const now = new Date()
    return flashSales.value.find(fs =>
      fs.isActive &&
      new Date(fs.startTime) <= now &&
      new Date(fs.endTime) >= now
    ) || null
  })
   
  const countdownUnits = ref({ 'Giờ': '00', 'Phút': '00', 'Giây': '00' })
   
  const updateCountdown = () => {
    if (!activeSale.value) return
    const diff = new Date(activeSale.value.endTime) - new Date()
    if (diff <= 0) { countdownUnits.value = { 'Giờ': '00', 'Phút': '00', 'Giây': '00' }; return }
    const h = Math.floor(diff / 3600000)
    const m = Math.floor((diff % 3600000) / 60000)
    const s = Math.floor((diff % 60000) / 1000)
    countdownUnits.value = {
      'Giờ':   String(h).padStart(2, '0'),
      'Phút':  String(m).padStart(2, '0'),
      'Giây':  String(s).padStart(2, '0')
    }
  }
   
  const formatDateTime = (d) => d
    ? new Date(d).toLocaleString('vi-VN', { day:'2-digit', month:'2-digit', hour:'2-digit', minute:'2-digit' })
    : ''
   
  onMounted(async () => {
    try {
      const res = await productApi.getFlashSales()
      flashSales.value = res.data.data || []
    } catch (e) {}
    // Hiển thị sản phẩm hot thay thế
    try {
      const res = await productApi.getHotProducts(8)
      products.value = res.data.data || []
    } catch (e) {}
    timer = setInterval(updateCountdown, 1000)
  })
   
  onUnmounted(() => clearInterval(timer))
  </script>