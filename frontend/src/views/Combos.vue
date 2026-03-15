<template>
    <div class="container py-5">
      <div class="text-center mb-5">
        <h2 class="fw-bold">👗 Combo Cosplay Full Set</h2>
        <p class="text-muted">Trang phục + Tóc giả + Phụ kiện — Tiết kiệm hơn mua lẻ!</p>
      </div>
   
      <div v-if="loading" class="row g-4">
        <div v-for="n in 4" :key="n" class="col-md-6">
          <div class="card border-0 placeholder-glow" style="height:300px;border-radius:16px;">
            <div class="placeholder w-100 h-100 rounded-3" style="background:#e0e0e0;"></div>
          </div>
        </div>
      </div>
   
      <div v-else-if="!combos.length" class="text-center py-5">
        <div style="font-size:4rem;">📦</div>
        <h5 class="text-muted mt-3">Chưa có combo nào</h5>
        <router-link to="/products" class="btn rounded-pill mt-2" style="background:#e91e8c;color:#fff;">
          Xem sản phẩm lẻ
        </router-link>
      </div>
   
      <div v-else class="row g-4">
        <div v-for="combo in combos" :key="combo.id" class="col-md-6 col-lg-4">
          <div class="card border-0 shadow-sm h-100"
            style="border-radius:16px;overflow:hidden;transition:transform 0.3s;"
            @mouseenter="$event.currentTarget.style.transform='translateY(-6px)'"
            @mouseleave="$event.currentTarget.style.transform='translateY(0)'">
            <div style="height:200px;background:linear-gradient(135deg,#e91e8c22,#7c4dff22);
                        display:flex;align-items:center;justify-content:center;">
              <span style="font-size:4rem;">👗</span>
            </div>
            <div class="card-body p-4">
              <h5 class="fw-bold mb-2">{{ combo.name }}</h5>
              <p class="text-muted small mb-3">{{ combo.description || 'Bộ cosplay đầy đủ' }}</p>
   
              <!-- Items in combo -->
              <div v-if="combo.items?.length" class="mb-3">
                <div class="small text-muted mb-2">Bao gồm:</div>
                <div class="d-flex gap-2 flex-wrap">
                  <span v-for="item in combo.items" :key="item.id"
                    class="badge text-bg-light text-dark small">
                    {{ item.productName || item.product?.name }}
                  </span>
                </div>
              </div>
   
              <div class="d-flex align-items-center justify-content-between">
                <div>
                  <div v-if="combo.comboPrice" class="fw-bold fs-5" style="color:#e91e8c;">
                    {{ formatPrice(combo.comboPrice) }}
                  </div>
                  <div v-if="combo.originalPrice && combo.comboPrice" class="small text-muted text-decoration-line-through">
                    {{ formatPrice(combo.originalPrice) }}
                  </div>
                </div>
                <router-link :to="`/products?comboId=${combo.id}`"
                  class="btn btn-sm rounded-pill px-3" style="background:#e91e8c;color:#fff;">
                  Xem chi tiết
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
   
  <script setup>
  import { ref, onMounted } from 'vue'
  import { productApi } from '@/api/index'
   
  const combos = ref([])
  const loading = ref(false)
   
  const formatPrice = (p) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p)
   
  onMounted(async () => {
    loading.value = true
    try {
      const res = await productApi.getCombos()
      combos.value = res.data.data || []
    } catch (e) {
      combos.value = []
    } finally {
      loading.value = false
    }
  })
  </script>