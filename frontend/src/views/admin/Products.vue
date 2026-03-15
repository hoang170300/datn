<template>
  <div>
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h5 class="fw-bold mb-1">Quản lý sản phẩm</h5>
        <p class="text-muted small mb-0">{{ pagination.totalElements }} sản phẩm</p>
      </div>
      <router-link to="/admin/products/create" class="btn rounded-pill"
        style="background:#e91e8c;color:#fff;">
        <i class="bi bi-plus-lg me-2"></i>Thêm sản phẩm
      </router-link>
    </div>

    <!-- Filters -->
    <div class="card border-0 shadow-sm mb-4" style="border-radius:12px;">
      <div class="card-body p-3">
        <div class="row g-2 align-items-center">
          <div class="col-md-4">
            <div class="input-group input-group-sm">
              <span class="input-group-text"><i class="bi bi-search"></i></span>
              <input type="text" class="form-control" v-model="search"
                placeholder="Tìm sản phẩm..." @input="debouncedSearch">
            </div>
          </div>
          <div class="col-md-2">
            <select class="form-select form-select-sm" v-model="filterType" @change="fetchData">
              <option value="">Tất cả loại</option>
              <option value="SALE">Bán</option>
              <option value="RENTAL">Thuê</option>
              <option value="BOTH">Cả hai</option>
            </select>
          </div>
          <div class="col-md-2">
            <select class="form-select form-select-sm" v-model="filterCategory" @change="fetchData">
              <option value="">Tất cả DM</option>
              <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
          <div class="col-auto ms-auto">
            <button class="btn btn-sm btn-outline-secondary rounded-pill" @click="resetFilter">
              <i class="bi bi-x-circle me-1"></i>Xóa lọc
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="card border-0 shadow-sm" style="border-radius:16px;">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead style="background:#f8f9fa;">
              <tr>
                <th class="ps-4 py-3 small text-muted text-uppercase">Sản phẩm</th>
                <th class="py-3 small text-muted text-uppercase">Danh mục</th>
                <th class="py-3 small text-muted text-uppercase">Loại</th>
                <th class="py-3 small text-muted text-uppercase">Giá bán</th>
                <th class="py-3 small text-muted text-uppercase">Giá thuê/ngày</th>
                <th class="py-3 small text-muted text-uppercase">Đánh giá</th>
                <th class="py-3 small text-muted text-uppercase">Trạng thái</th>
                <th class="pe-4 py-3 small text-muted text-uppercase text-end">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="8" class="text-center py-5">
                  <div class="spinner-border text-secondary"></div>
                </td>
              </tr>
              <tr v-else-if="!products.length">
                <td colspan="8" class="text-center py-5 text-muted">
                  <i class="bi bi-inbox fs-1 d-block mb-2"></i>
                  Không tìm thấy sản phẩm
                </td>
              </tr>
              <tr v-for="product in products" :key="product.id" v-else>
                <td class="ps-4">
                  <div class="d-flex align-items-center gap-3">
                    <img :src="product.primaryImageUrl || '/img/placeholder.svg'"
                      class="rounded" style="width:48px;height:48px;object-fit:cover;"
                      :alt="product.name">
                    <div>
                      <div class="fw-semibold small">{{ product.name }}</div>
                      <small class="text-muted">{{ product.characterName || product.seriesName || '—' }}</small>
                    </div>
                  </div>
                </td>
                <td><small class="badge text-bg-light text-dark">{{ product.categoryName }}</small></td>
                <td>
                  <span class="badge rounded-pill small"
                    :class="typeClass(product.productType)">
                    {{ typeLabel(product.productType) }}
                  </span>
                </td>
                <td class="small fw-semibold" style="color:#e91e8c;">
                  {{ product.salePrice ? formatPrice(product.salePrice) : '—' }}
                </td>
                <td class="small text-info">
                  {{ product.rentalPricePerDay ? formatPrice(product.rentalPricePerDay) : '—' }}
                </td>
                <td>
                  <div class="d-flex align-items-center gap-1">
                    <i class="bi bi-star-fill text-warning small"></i>
                    <small>{{ (product.avgRating || 0).toFixed(1) }} ({{ product.reviewCount || 0 }})</small>
                  </div>
                </td>
                <td>
                  <div class="form-check form-switch mb-0">
                    <input class="form-check-input" type="checkbox"
                      :checked="product.isActive"
                      @change="toggleStatus(product)">
                  </div>
                </td>
                <td class="pe-4 text-end">
                  <div class="d-flex gap-1 justify-content-end">
                    <router-link :to="`/products/${product.slug}`" target="_blank"
                      class="btn btn-sm btn-outline-secondary rounded"
                      title="Xem trang">
                      <i class="bi bi-eye"></i>
                    </router-link>
                    <router-link :to="`/admin/products/${product.id}/edit`"
                      class="btn btn-sm btn-outline-primary rounded"
                      title="Sửa">
                      <i class="bi bi-pencil"></i>
                    </router-link>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div v-if="pagination.totalPages > 1"
          class="d-flex justify-content-between align-items-center px-4 py-3 border-top">
          <small class="text-muted">
            Trang {{ currentPage + 1 }} / {{ pagination.totalPages }}
            ({{ pagination.totalElements }} sản phẩm)
          </small>
          <nav>
            <ul class="pagination pagination-sm mb-0">
              <li :class="['page-item', { disabled: currentPage === 0 }]">
                <button class="page-link" @click="changePage(currentPage - 1)">
                  <i class="bi bi-chevron-left"></i>
                </button>
              </li>
              <li v-for="p in Math.min(pagination.totalPages, 5)" :key="p"
                :class="['page-item', { active: currentPage === p - 1 }]">
                <button class="page-link" @click="changePage(p - 1)">{{ p }}</button>
              </li>
              <li :class="['page-item', { disabled: pagination.last }]">
                <button class="page-link" @click="changePage(currentPage + 1)">
                  <i class="bi bi-chevron-right"></i>
                </button>
              </li>
            </ul>
          </nav>
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
const categories = ref([])
const loading = ref(false)
const search = ref('')
const filterType = ref('')
const filterCategory = ref('')
const currentPage = ref(0)
const pagination = ref({ totalPages: 0, totalElements: 0, last: true })

let searchTimer = null
const debouncedSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { currentPage.value = 0; fetchData() }, 400)
}

const formatPrice = (p) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p)

const typeLabel = (t) => ({ SALE: 'Bán', RENTAL: 'Thuê', BOTH: 'Mua & Thuê' })[t] || t
const typeClass = (t) => ({ SALE: 'bg-primary', RENTAL: 'bg-info text-dark', BOTH: 'bg-warning text-dark' })[t] || 'bg-secondary'

const fetchData = async () => {
  loading.value = true
  try {
    const res = await productApi.admin.getProducts({
      keyword: search.value || null,
      productType: filterType.value || null,
      categoryId: filterCategory.value || null,
      page: currentPage.value,
      size: 15
    })
    const data = res.data.data
    products.value = data.content
    pagination.value = { totalPages: data.totalPages, totalElements: data.totalElements, last: data.last }
  } catch (e) {
    toast.error('Lỗi tải danh sách sản phẩm')
  } finally {
    loading.value = false
  }
}

const toggleStatus = async (product) => {
  try {
    await productApi.admin.toggleStatus(product.id)
    product.isActive = !product.isActive
    toast.success(product.isActive ? 'Đã kích hoạt sản phẩm' : 'Đã ẩn sản phẩm')
  } catch (e) {
    toast.error('Có lỗi xảy ra')
  }
}

const changePage = (page) => {
  if (page < 0 || page >= pagination.value.totalPages) return
  currentPage.value = page
  fetchData()
}

const resetFilter = () => {
  search.value = ''
  filterType.value = ''
  filterCategory.value = ''
  currentPage.value = 0
  fetchData()
}

onMounted(async () => {
  const res = await productApi.getCategories()
  categories.value = res.data.data || []
  fetchData()
})
</script>

<style scoped>
.page-item.active .page-link { background: #e91e8c; border-color: #e91e8c; }
.page-link { color: #e91e8c; }
</style>
