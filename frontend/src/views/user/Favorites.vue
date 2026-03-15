<template>
  <div class="card border-0 shadow-sm" style="border-radius:16px;">
    <div class="card-body p-4">
      <h5 class="fw-bold mb-4"><i class="bi bi-heart me-2 text-danger"></i>Sản phẩm yêu thích</h5>

      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border" style="color:#e91e8c;"></div>
      </div>

      <div v-else-if="!products.length" class="text-center py-5">
        <div style="font-size:4rem;">💔</div>
        <h6 class="text-muted mt-3">Chưa có sản phẩm yêu thích</h6>
        <router-link to="/products" class="btn mt-2 rounded-pill"
          style="background:#e91e8c;color:#fff;">Khám phá ngay</router-link>
      </div>

      <div v-else class="row g-3">
        <div v-for="product in products" :key="product.id" class="col-6 col-md-4">
          <div class="card border-0 shadow-sm h-100" style="border-radius:12px;overflow:hidden;">
            <div class="position-relative">
              <img :src="product.primaryImageUrl || '/img/placeholder.svg'"
                class="card-img-top" style="height:160px;object-fit:cover;"
                :alt="product.name">
              <button class="btn btn-sm position-absolute top-0 end-0 m-2 rounded-circle"
                style="width:32px;height:32px;padding:0;background:rgba(255,255,255,0.9);"
                @click="removeFavorite(product.id)">
                <i class="bi bi-heart-fill text-danger"></i>
              </button>
            </div>
            <div class="card-body p-3">
              <router-link :to="`/products/${product.slug}`"
                class="text-decoration-none text-dark fw-semibold small lh-sm d-block mb-2">
                {{ product.name }}
              </router-link>
              <div class="fw-bold small" style="color:#e91e8c;">
                {{ formatPrice(product.salePrice) }}
              </div>
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
import { useToast } from 'vue-toastification'

const toast = useToast()
const products = ref([])
const loading = ref(false)

const formatPrice = (p) => p ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p) : '—'

const fetchFavorites = async () => {
  loading.value = true
  try {
    const res = await productApi.getFavorites({ page: 0, size: 20 })
    products.value = res.data.data?.content || []
  } finally {
    loading.value = false
  }
}

const removeFavorite = async (productId) => {
  try {
    await productApi.toggleFavorite(productId)
    products.value = products.value.filter(p => p.id !== productId)
    toast.success('Đã bỏ yêu thích')
  } catch (e) {
    toast.error('Có lỗi xảy ra')
  }
}

onMounted(fetchFavorites)
</script>
