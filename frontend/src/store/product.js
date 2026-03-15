import { defineStore } from 'pinia'
import { ref } from 'vue'
import { productApi } from '@/api/index'

export const useProductStore = defineStore('product', () => {
  const products       = ref([])
  const currentProduct = ref(null)
  const hotProducts    = ref([])
  const newProducts    = ref([])
  const categories     = ref([])
  const series         = ref([])
  const loading        = ref(false)
  const pagination     = ref({
    page: 0, size: 12, totalElements: 0, totalPages: 0, last: true
  })

  async function fetchProducts(params = {}) {
    loading.value = true
    try {
      const res = await productApi.getProducts(params)
      const data = res.data.data
      products.value  = data.content
      pagination.value = {
        page:          data.page,
        size:          data.size,
        totalElements: data.totalElements,
        totalPages:    data.totalPages,
        last:          data.last
      }
    } finally {
      loading.value = false
    }
  }

  async function fetchProductDetail(slug) {
    loading.value = true
    try {
      const res = await productApi.getProductDetail(slug)
      currentProduct.value = res.data.data
    } finally {
      loading.value = false
    }
  }

  async function fetchHotProducts() {
    const res = await productApi.getHotProducts()
    hotProducts.value = res.data.data || []
  }

  async function fetchNewProducts() {
    const res = await productApi.getNewProducts()
    newProducts.value = res.data.data || []
  }

  async function fetchCategories() {
    const res = await productApi.getCategories()
    categories.value = res.data.data || []
  }

  async function fetchSeries() {
    const res = await productApi.getSeriesList()
    series.value = res.data.data || []
  }

  return {
    products, currentProduct, hotProducts, newProducts,
    categories, series, loading, pagination,
    fetchProducts, fetchProductDetail, fetchHotProducts,
    fetchNewProducts, fetchCategories, fetchSeries
  }
})
