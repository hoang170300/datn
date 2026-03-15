<template>
  <div class="container py-4">
    <div class="row g-4">
      <!-- Sidebar Filter -->
      <div class="col-lg-3">
        <div class="card border-0 sticky-top" style="top:80px;border-radius:12px;box-shadow:0 4px 20px rgba(0,0,0,0.08);">
          <div class="card-body p-4">
            <h6 class="fw-bold mb-3">🔍 Bộ lọc tìm kiếm</h6>

            <!-- Category -->
            <div class="mb-4">
              <label class="form-label fw-semibold small text-muted">DANH MỤC</label>
              <div v-for="cat in categories" :key="cat.id" class="form-check">
                <input class="form-check-input" type="radio" name="category"
                  :value="cat.id" v-model="filters.categoryId"
                  :id="'cat-' + cat.id">
                <label class="form-check-label small" :for="'cat-' + cat.id">{{ cat.name }}</label>
              </div>
              <div class="form-check">
                <input class="form-check-input" type="radio" name="category"
                  :value="null" v-model="filters.categoryId" id="cat-all">
                <label class="form-check-label small text-muted" for="cat-all">Tất cả</label>
              </div>
            </div>

            <!-- Series -->
            <div class="mb-4">
              <label class="form-label fw-semibold small text-muted">SERIES</label>
              <select class="form-select form-select-sm" v-model="filters.seriesId">
                <option :value="null">Tất cả</option>
                <option v-for="s in series" :key="s.id" :value="s.id">{{ s.name }}</option>
              </select>
            </div>

            <!-- Product Type -->
            <div class="mb-4">
              <label class="form-label fw-semibold small text-muted">LOẠI</label>
              <div class="d-flex gap-2 flex-wrap">
                <button v-for="type in productTypes" :key="type.value"
                  :class="['btn btn-sm rounded-pill', filters.productType === type.value ? 'btn-primary text-white' : 'btn-outline-secondary']"
                  style="font-size:0.75rem;"
                  @click="filters.productType = filters.productType === type.value ? null : type.value">
                  {{ type.label }}
                </button>
              </div>
            </div>

            <!-- Price Range -->
            <div class="mb-4">
              <label class="form-label fw-semibold small text-muted">GIÁ BÁN</label>
              <div class="row g-2">
                <div class="col-6">
                  <input type="number" class="form-control form-control-sm"
                    placeholder="Từ" v-model="filters.minPrice">
                </div>
                <div class="col-6">
                  <input type="number" class="form-control form-control-sm"
                    placeholder="Đến" v-model="filters.maxPrice">
                </div>
              </div>
            </div>

            <!-- Actions -->
            <div class="d-grid gap-2">
              <button class="btn btn-sm"
                style="background:#e91e8c;color:#fff;"
                @click="applyFilter">
                <i class="bi bi-funnel me-1"></i>Áp dụng
              </button>
              <button class="btn btn-sm btn-outline-secondary" @click="resetFilter">
                <i class="bi bi-x-circle me-1"></i>Xóa bộ lọc
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Product Grid -->
      <div class="col-lg-9">
        <!-- Header -->
        <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
          <div>
            <h5 class="fw-bold mb-1">
              {{ route.query.keyword ? `Kết quả: "${route.query.keyword}"` : 'Tất cả sản phẩm' }}
            </h5>
            <small class="text-muted">{{ productStore.pagination.totalElements }} sản phẩm</small>
          </div>
          <div class="d-flex align-items-center gap-2">
            <label class="small text-muted">Sắp xếp:</label>
            <select class="form-select form-select-sm" style="width:auto;" v-model="sortOption" @change="fetchProducts">
              <option value="createdAt-desc">Mới nhất</option>
              <option value="salePrice-asc">Giá tăng dần</option>
              <option value="salePrice-desc">Giá giảm dần</option>
              <option value="viewsCount-desc">Phổ biến nhất</option>
              <option value="avgRating-desc">Đánh giá cao nhất</option>
            </select>
          </div>
        </div>

        <!-- Loading skeleton -->
        <div v-if="productStore.loading" class="row g-3">
          <div v-for="n in 8" :key="n" class="col-6 col-md-4">
            <div class="card border-0 placeholder-glow" style="height:380px;border-radius:12px;">
              <div class="placeholder w-100 h-100 rounded" style="background:#e0e0e0;"></div>
            </div>
          </div>
        </div>

        <!-- Products -->
        <div v-else-if="productStore.products.length" class="row g-3">
          <div v-for="product in productStore.products" :key="product.id" class="col-6 col-md-4">
            <ProductCard :product="product" />
          </div>
        </div>

        <!-- Empty state -->
        <div v-else class="text-center py-5">
          <div style="font-size:4rem;">🔍</div>
          <h5 class="text-muted mt-3">Không tìm thấy sản phẩm nào</h5>
          <p class="text-muted">Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm</p>
          <button class="btn btn-outline-secondary rounded-pill" @click="resetFilter">Xóa bộ lọc</button>
        </div>

        <!-- Pagination -->
        <nav v-if="productStore.pagination.totalPages > 1" class="mt-5 d-flex justify-content-center">
          <ul class="pagination">
            <li :class="['page-item', { disabled: currentPage === 0 }]">
              <button class="page-link" @click="changePage(currentPage - 1)">
                <i class="bi bi-chevron-left"></i>
              </button>
            </li>
            <li v-for="p in productStore.pagination.totalPages" :key="p"
              :class="['page-item', { active: currentPage === p - 1 }]">
              <button class="page-link" @click="changePage(p - 1)">{{ p }}</button>
            </li>
            <li :class="['page-item', { disabled: productStore.pagination.last }]">
              <button class="page-link" @click="changePage(currentPage + 1)">
                <i class="bi bi-chevron-right"></i>
              </button>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useProductStore } from '@/store/product'
import { productApi } from '@/api/index'
import ProductCard from '@/components/product/ProductCard.vue'

const route = useRoute()
const router = useRouter()
const productStore = useProductStore()
const categories = ref([])
const series = ref([])
const currentPage = ref(0)
const sortOption = ref('createdAt-desc')

const productTypes = [
  { label: 'Mua', value: 'SALE' },
  { label: 'Thuê', value: 'RENTAL' },
  { label: 'Cả hai', value: 'BOTH' }
]

const filters = reactive({
  keyword: null,
  categoryId: null,
  seriesId: null,
  productType: null,
  minPrice: null,
  maxPrice: null
})

const fetchProducts = async () => {
  const [sortBy, sortDir] = sortOption.value.split('-')
  await productStore.fetchProducts({
    ...filters,
    sortBy, sortDir,
    page: currentPage.value,
    size: 9
  })
}

const applyFilter = () => {
  currentPage.value = 0
  fetchProducts()
}

const resetFilter = () => {
  Object.assign(filters, { keyword: null, categoryId: null, seriesId: null, productType: null, minPrice: null, maxPrice: null })
  currentPage.value = 0
  sortOption.value = 'createdAt-desc'
  fetchProducts()
}

const changePage = (page) => {
  if (page < 0 || page >= productStore.pagination.totalPages) return
  currentPage.value = page
  fetchProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// Sync from URL query
watch(() => route.query, (q) => {
  if (q.keyword) filters.keyword = q.keyword
  if (q.categoryId) filters.categoryId = Number(q.categoryId)
  if (q.seriesId) filters.seriesId = Number(q.seriesId)
  fetchProducts()
}, { immediate: false })

onMounted(async () => {
  const [cRes, sRes] = await Promise.all([productApi.getCategories(), productApi.getSeriesList()])
  categories.value = cRes.data.data || []
  series.value = sRes.data.data || []

  // Apply URL filters
  if (route.query.keyword) filters.keyword = route.query.keyword
  if (route.query.categoryId) filters.categoryId = Number(route.query.categoryId)
  if (route.query.seriesId) filters.seriesId = Number(route.query.seriesId)

  fetchProducts()
})
</script>

<style scoped>
.page-item.active .page-link {
  background: #e91e8c;
  border-color: #e91e8c;
}
.page-link { color: #e91e8c; }
.page-link:hover { color: #c0146e; }
</style>
