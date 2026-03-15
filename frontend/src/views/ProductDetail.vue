<template>
  <div class="container py-5" v-if="!loading && product">
    <!-- Breadcrumb -->
    <nav aria-label="breadcrumb" class="mb-4">
      <ol class="breadcrumb small">
        <li class="breadcrumb-item"><router-link to="/">Trang chủ</router-link></li>
        <li class="breadcrumb-item"><router-link to="/products">Sản phẩm</router-link></li>
        <li class="breadcrumb-item active">{{ product.name }}</li>
      </ol>
    </nav>
 
    <div class="row g-5">
      <!-- Images -->
      <div class="col-md-5">
        <div class="position-sticky" style="top:90px;">
          <div class="rounded-3 overflow-hidden mb-3" style="height:420px;background:#f8f9fa;">
            <img :src="selectedImage || product.primaryImageUrl || '/img/placeholder.svg'"
              class="w-100 h-100" style="object-fit:cover;" :alt="product.name">
          </div>
          <!-- Thumbnails -->
          <div class="d-flex gap-2 flex-wrap">
            <img v-for="img in (product.images || [])" :key="img.id"
              :src="img.imageUrl" :alt="img.altText"
              class="rounded cursor-pointer"
              style="width:68px;height:68px;object-fit:cover;border:2px solid transparent;transition:border 0.2s;"
              :style="selectedImage === img.imageUrl ? 'border-color:#e91e8c' : ''"
              @click="selectedImage = img.imageUrl"
              @error="$event.target.src = '/img/placeholder.svg'">
          </div>
        </div>
      </div>
 
      <!-- Info -->
      <div class="col-md-7">
        <!-- Tags -->
        <div class="d-flex gap-2 mb-3 flex-wrap">
          <span v-if="product.isHot" class="badge" style="background:#ff4757;">🔥 Hot</span>
          <span v-if="product.categoryName" class="badge text-bg-light text-dark">{{ product.categoryName }}</span>
          <span v-if="product.seriesName" class="badge text-bg-info">{{ product.seriesName }}</span>
          <span v-if="product.characterName" class="badge" style="background:#7c4dff;color:#fff;">
            {{ product.characterName }}
          </span>
        </div>
 
        <h2 class="fw-bold mb-3">{{ product.name }}</h2>
 
        <!-- Rating -->
        <div class="d-flex align-items-center gap-2 mb-4">
          <div class="text-warning">
            <i v-for="n in 5" :key="n"
              :class="['bi', n <= Math.round(product.avgRating || 0) ? 'bi-star-fill' : 'bi-star']"></i>
          </div>
          <span class="small text-muted">
            {{ (product.avgRating || 0).toFixed(1) }} ({{ product.reviewCount || 0 }} đánh giá)
          </span>
          <span class="text-muted small">· {{ product.viewsCount || 0 }} lượt xem</span>
        </div>
 
        <!-- Price -->
        <div class="p-3 rounded-3 mb-4" style="background:#fff5f8;">
          <div v-if="product.salePrice" class="d-flex align-items-center gap-3 mb-2">
            <i class="bi bi-bag" style="color:#e91e8c;"></i>
            <span class="text-muted small">Giá mua:</span>
            <span style="font-size:1.6rem;font-weight:800;color:#e91e8c;">
              {{ formatPrice(selectedVariant?.price || product.salePrice) }}
            </span>
          </div>
          <div v-if="product.rentalPricePerDay" class="d-flex align-items-center gap-3">
            <i class="bi bi-calendar-event text-info"></i>
            <span class="text-muted small">Giá thuê:</span>
            <span class="fw-bold text-info fs-5">{{ formatPrice(product.rentalPricePerDay) }}/ngày</span>
            <span v-if="product.depositAmount > 0" class="small text-muted">
              (Cọc: {{ formatPrice(product.depositAmount) }})
            </span>
          </div>
        </div>
 
        <!-- Variant Selection -->
        <div v-if="product.variants?.length" class="mb-4">
          <label class="form-label fw-semibold small mb-2">
            Chọn Size: <span v-if="selectedVariant" class="text-primary">{{ selectedVariant.size }}</span>
          </label>
          <div class="d-flex gap-2 flex-wrap">
            <button v-for="v in product.variants" :key="v.id"
              :class="['btn btn-sm position-relative',
                selectedVariant?.id === v.id ? 'btn-dark' :
                (orderType==='SALE'&&v.stockQuantity<=0)||(orderType==='RENTAL'&&v.rentalQuantity<=0)||(v.stockQuantity<=0&&v.rentalQuantity<=0)
                  ? 'btn-outline-secondary opacity-50' : 'btn-outline-secondary']"
              style="min-width:56px;border-radius:8px;"
              :disabled="(orderType==='SALE'&&v.stockQuantity<=0)||(orderType==='RENTAL'&&v.rentalQuantity<=0)||(v.stockQuantity<=0&&v.rentalQuantity<=0)"
              @click="selectedVariant = v">
              {{ v.size }}
              <span v-if="v.color" class="small d-block" style="font-size:0.65rem;">{{ v.color }}</span>
              <span v-if="v.stockQuantity<=0&&v.rentalQuantity<=0"
                class="d-block" style="font-size:0.6rem;color:#dc3545;">Hết</span>
            </button>
          </div>
          <div v-if="selectedVariant" class="mt-2">
            <small class="text-muted">
              Mua: <strong :class="selectedVariant.stockQuantity<=0?'text-danger':selectedVariant.stockQuantity<=3?'text-warning':'text-success'">
                {{ selectedVariant.stockQuantity<=0 ? 'Hết hàng' : `Còn ${selectedVariant.stockQuantity}` }}
              </strong>
              <span v-if="product.productType!=='SALE'"> &nbsp;·&nbsp;
                Thuê: <strong :class="selectedVariant.rentalQuantity<=0?'text-danger':selectedVariant.rentalQuantity<=2?'text-warning':'text-success'">
                  {{ selectedVariant.rentalQuantity<=0 ? 'Hết' : `Còn ${selectedVariant.rentalQuantity}` }}
                </strong>
              </span>
            </small>
            <small v-if="(orderType==='SALE'&&selectedVariant.stockQuantity>0&&selectedVariant.stockQuantity<=3)
                        ||(orderType==='RENTAL'&&selectedVariant.rentalQuantity>0&&selectedVariant.rentalQuantity<=2)"
              class="text-warning d-block mt-1">
              <i class="bi bi-exclamation-triangle me-1"></i>Sắp hết hàng! Đặt ngay kẻo lỡ.
            </small>
          </div>
        </div>
 
        <!-- Order Type -->
        <div class="mb-4" v-if="product.productType === 'BOTH'">
          <label class="form-label fw-semibold small mb-2">Hình thức:</label>
          <div class="d-flex gap-2">
            <button :class="['btn btn-sm rounded-pill', orderType === 'SALE' ? 'btn-dark' : 'btn-outline-secondary']"
              @click="orderType = 'SALE'">🛍️ Mua</button>
            <button :class="['btn btn-sm rounded-pill', orderType === 'RENTAL' ? 'btn-info text-white' : 'btn-outline-info']"
              @click="orderType = 'RENTAL'">📅 Thuê</button>
          </div>
        </div>
 
        <!-- Rental Date -->
        <div v-if="orderType === 'RENTAL'" class="mb-4">
          <label class="form-label fw-semibold small">Chọn ngày thuê:</label>
          <div class="row g-2">
            <div class="col-6">
              <input type="date" class="form-control form-control-sm" v-model="rentalStart"
                :min="today" @change="checkRental">
            </div>
            <div class="col-6">
              <input type="date" class="form-control form-control-sm" v-model="rentalEnd"
                :min="rentalStart" @change="checkRental">
            </div>
          </div>
          <small v-if="rentalDays > 0" class="text-info mt-1 d-block">
            📅 {{ rentalDays }} ngày · Tổng: {{ formatPrice(product.rentalPricePerDay * rentalDays) }}
          </small>
        </div>
 
        <!-- Quantity -->
        <div class="d-flex align-items-center gap-3 mb-4">
          <label class="form-label fw-semibold small mb-0">Số lượng:</label>
          <div class="d-flex align-items-center gap-2">
            <button class="btn btn-outline-secondary btn-sm rounded-circle"
              style="width:34px;height:34px;padding:0;"
              @click="qty > 1 && qty--">
              <i class="bi bi-dash"></i>
            </button>
            <span class="fw-bold px-2" style="min-width:30px;text-align:center;">{{ qty }}</span>
            <button class="btn btn-outline-secondary btn-sm rounded-circle"
              style="width:34px;height:34px;padding:0;"
              @click="qty++">
              <i class="bi bi-plus"></i>
            </button>
          </div>
        </div>
 
        <!-- Action Buttons -->
        <div class="d-flex gap-3 mb-5 flex-wrap">
          <button class="btn flex-grow-1 py-3 fw-bold"
            style="border-radius:12px;"
            :style="isSelectedOutOfStock
              ? 'background:#6c757d;color:#fff;cursor:not-allowed'
              : 'background:#e91e8c;color:#fff'"
            :disabled="!selectedVariant || addingToCart || isSelectedOutOfStock"
            @click="addToCart">
            <span v-if="addingToCart" class="spinner-border spinner-border-sm me-2"></span>
            <span v-else-if="isSelectedOutOfStock"><i class="bi bi-slash-circle me-2"></i>Hết hàng</span>
            <span v-else><i class="bi bi-cart-plus me-2"></i>Thêm vào giỏ hàng</span>
          </button>
          <button class="btn btn-outline-danger rounded-circle"
            style="width:52px;height:52px;padding:0;"
            @click="toggleFav">
            <i :class="['bi', isFav ? 'bi-heart-fill' : 'bi-heart']"></i>
          </button>
        </div>
 
        <!-- Description -->
        <div v-if="product.description">
          <h6 class="fw-bold mb-2">Mô tả sản phẩm</h6>
          <p class="text-muted" style="line-height:1.8;">{{ product.description }}</p>
        </div>
      </div>
    </div>
 
    <!-- Reviews Section -->
    <div class="mt-5 pt-4 border-top">
      <h5 class="fw-bold mb-4">
        ⭐ Đánh giá sản phẩm
        <span class="text-muted fw-normal small ms-2">({{ product.reviewCount || 0 }} đánh giá)</span>
      </h5>
 
      <!-- Add Review Form -->
      <div v-if="authStore.isLoggedIn && !hasReviewed" class="card border-0 bg-light mb-4 p-4 rounded-3">
        <h6 class="fw-semibold mb-3">Viết đánh giá của bạn</h6>
        <div class="mb-3">
          <label class="small fw-semibold">Đánh giá sao:</label>
          <div class="d-flex gap-1 mt-1">
            <i v-for="n in 5" :key="n"
              :class="['bi fs-4 cursor-pointer', n <= reviewForm.rating ? 'bi-star-fill text-warning' : 'bi-star text-muted']"
              @click="reviewForm.rating = n"></i>
          </div>
        </div>
        <div class="mb-3">
          <input type="text" class="form-control form-control-sm mb-2"
            v-model="reviewForm.title" placeholder="Tiêu đề đánh giá">
          <textarea class="form-control form-control-sm" v-model="reviewForm.comment"
            rows="3" placeholder="Chia sẻ trải nghiệm của bạn..."></textarea>
        </div>
        <button class="btn btn-sm px-4 rounded-pill"
          style="background:#e91e8c;color:#fff;"
          :disabled="!reviewForm.rating || submittingReview"
          @click="submitReview">
          <span v-if="submittingReview" class="spinner-border spinner-border-sm me-1"></span>
          Gửi đánh giá
        </button>
      </div>
 
      <!-- Reviews List -->
      <div v-if="reviews.length" class="d-flex flex-column gap-3">
        <div v-for="review in reviews" :key="review.id"
          class="d-flex gap-3 p-4 border rounded-3">
          <div class="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white flex-shrink-0"
            style="width:42px;height:42px;background:#e91e8c;font-size:0.9rem;">
            {{ review.userFullName?.charAt(0)?.toUpperCase() }}
          </div>
          <div class="flex-grow-1">
            <div class="d-flex justify-content-between flex-wrap gap-2 mb-1">
              <div>
                <span class="fw-semibold small me-2">{{ review.userFullName }}</span>
                <span class="text-warning small">
                  <i v-for="n in 5" :key="n"
                    :class="['bi', n <= review.rating ? 'bi-star-fill' : 'bi-star']"></i>
                </span>
              </div>
              <small class="text-muted">{{ formatDate(review.createdAt) }}</small>
            </div>
            <div v-if="review.title" class="fw-semibold small mb-1">{{ review.title }}</div>
            <p class="text-muted small mb-0">{{ review.comment }}</p>
          </div>
        </div>
      </div>
      <div v-else class="text-center py-4 text-muted">
        <i class="bi bi-chat-square-text fs-1 d-block mb-2"></i>
        Chưa có đánh giá nào. Hãy là người đầu tiên!
      </div>
    </div>
  </div>
 
  <!-- Loading state -->
  <div v-else-if="loading" class="container py-5">
    <div class="row g-5">
      <div class="col-md-5">
        <div class="placeholder-glow">
          <div class="placeholder w-100 rounded-3" style="height:420px;background:#e0e0e0;"></div>
        </div>
      </div>
      <div class="col-md-7">
        <div class="placeholder-glow d-flex flex-column gap-3">
          <div class="placeholder w-75 rounded" style="height:32px;background:#e0e0e0;"></div>
          <div class="placeholder w-50 rounded" style="height:24px;background:#e0e0e0;"></div>
          <div class="placeholder w-100 rounded" style="height:80px;background:#e0e0e0;"></div>
        </div>
      </div>
    </div>
  </div>
</template>
 
<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useProductStore } from '@/store/product'
import { useAuthStore } from '@/store/auth'
import { useCartStore } from '@/store/cart'
import { productApi } from '@/api/index'
import { useToast } from 'vue-toastification'
 
const route = useRoute()
const productStore = useProductStore()
const authStore = useAuthStore()
const cartStore = useCartStore()
const toast = useToast()
 
const product = computed(() => productStore.currentProduct)
const loading = computed(() => productStore.loading)
const selectedImage = ref(null)
const selectedVariant = ref(null)
const orderType = ref('SALE')
const qty = ref(1)
const isFav = ref(false)
const addingToCart = ref(false)
const hasReviewed = ref(false)
const submittingReview = ref(false)
const reviews = ref([])
const rentalStart = ref('')
const rentalEnd = ref('')
const today = new Date().toISOString().split('T')[0]
 
// Kiểm tra variant đang chọn có hết hàng không (theo orderType)
const isSelectedOutOfStock = computed(() => {
  if (!selectedVariant.value) return false
  const v = selectedVariant.value
  if (orderType.value === 'RENTAL') return v.rentalQuantity <= 0
  return v.stockQuantity <= 0
})
 
const rentalDays = computed(() => {
  if (!rentalStart.value || !rentalEnd.value) return 0
  const diff = new Date(rentalEnd.value) - new Date(rentalStart.value)
  return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)))
})
 
const reviewForm = ref({ rating: 0, title: '', comment: '' })
 
const formatPrice = (p) => p ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p) : '—'
const formatDate = (d) => d ? new Date(d).toLocaleDateString('vi-VN') : ''
 
const addToCart = async () => {
  if (!authStore.isLoggedIn) { toast.warning('Vui lòng đăng nhập!'); return }
  if (!selectedVariant.value) { toast.warning('Vui lòng chọn size!'); return }
  if (orderType.value === 'RENTAL' && rentalDays.value === 0) {
    toast.warning('Vui lòng chọn ngày thuê!'); return
  }
  addingToCart.value = true
  try {
    await cartStore.addItem({
      productVariantId: selectedVariant.value.id,
      quantity: qty.value,
      orderType: orderType.value,
      rentalStartDate: orderType.value === 'RENTAL' ? rentalStart.value : null,
      rentalEndDate: orderType.value === 'RENTAL' ? rentalEnd.value : null
    })
    toast.success('Đã thêm vào giỏ hàng! 🛒')
  } catch (e) {
    toast.error(e.response?.data?.message || 'Có lỗi xảy ra')
  } finally {
    addingToCart.value = false
  }
}
 
const toggleFav = async () => {
  if (!authStore.isLoggedIn) { toast.warning('Vui lòng đăng nhập!'); return }
  try {
    const res = await productApi.toggleFavorite(product.value.id)
    isFav.value = res.data.data
    toast.success(isFav.value ? 'Đã thêm vào yêu thích ❤️' : 'Đã bỏ yêu thích')
  } catch (e) {}
}
 
const submitReview = async () => {
  submittingReview.value = true
  try {
    const res = await productApi.addReview({
      productId: product.value.id,
      ...reviewForm.value
    })
    reviews.value.unshift(res.data.data)
    hasReviewed.value = true
    toast.success('Cảm ơn bạn đã đánh giá! ⭐')
    reviewForm.value = { rating: 0, title: '', comment: '' }
  } catch (e) {
    toast.error(e.response?.data?.message || 'Có lỗi xảy ra')
  } finally {
    submittingReview.value = false
  }
}
 
const checkRental = async () => {
  if (!rentalStart.value || !rentalEnd.value || !product.value) return
}
 
watch(() => route.params.slug, async (slug) => {
  if (slug) {
    selectedVariant.value = null
    selectedImage.value = null
    await productStore.fetchProductDetail(slug)
    if (product.value) {
      orderType.value = product.value.productType === 'RENTAL' ? 'RENTAL' : 'SALE'
      if (product.value.variants?.length) selectedVariant.value = product.value.variants[0]
      if (product.value.images?.length) selectedImage.value = product.value.primaryImageUrl
 
      // Load reviews
      try {
        const res = await productApi.getReviews(product.value.id, { page: 0, size: 10 })
        reviews.value = res.data.data?.content || []
      } catch (e) {}
 
      if (authStore.isLoggedIn) {
        try {
          const res = await productApi.isFavorite(product.value.id)
          isFav.value = res.data.data
          hasReviewed.value = reviews.value.some(r => r.userId === authStore.user?.id)
        } catch (e) {}
      }
    }
  }
}, { immediate: true })
</script>
 
<style scoped>
.cursor-pointer { cursor: pointer; }
</style>