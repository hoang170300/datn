<template>
  <div class="product-card card h-100 border-0"
    style="border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);transition:all 0.3s ease;"
    @mouseenter="$event.currentTarget.style.transform='translateY(-6px)'"
    @mouseleave="$event.currentTarget.style.transform='translateY(0)'">
 
    <!-- Image -->
    <div class="position-relative overflow-hidden" style="height:240px;">
      <img :src="product.primaryImageUrl || '/img/placeholder.svg'"
        :alt="product.name"
        class="card-img-top h-100 w-100"
        style="object-fit:cover;transition:transform 0.4s ease;"
        @error="$event.target.src='/img/placeholder.svg'"
        @mouseenter="$event.target.style.transform='scale(1.08)'"
        @mouseleave="$event.target.style.transform='scale(1)'"/>
 
      <!-- Badges -->
      <div class="position-absolute top-0 start-0 p-2 d-flex gap-1 flex-wrap">
        <span v-if="product.isHot" class="badge" style="background:#ff4757;">🔥 Hot</span>
        <span v-if="product.productType==='RENTAL'" class="badge bg-info">Thuê</span>
        <span v-if="product.productType==='BOTH'" class="badge" style="background:#7c4dff;">Mua & Thuê</span>
      </div>
 
      <!-- Favorite -->
      <button v-if="authStore.isLoggedIn"
        class="btn btn-sm position-absolute top-0 end-0 m-2 rounded-circle"
        style="width:34px;height:34px;padding:0;background:rgba(255,255,255,0.9);"
        @click.prevent="toggleFav">
        <i :class="['bi',isFav?'bi-heart-fill text-danger':'bi-heart text-secondary']"></i>
      </button>
 
      <!-- Out of stock badge -->
      <div v-if="isOutOfStock"
        class="position-absolute top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center"
        style="background:rgba(0,0,0,0.45);pointer-events:none;">
        <span class="badge bg-dark px-3 py-2" style="font-size:0.85rem;">Hết hàng</span>
      </div>
 
      <!-- Add to cart button -->
      <div class="position-absolute bottom-0 start-0 end-0 p-2"
        style="background:linear-gradient(transparent,rgba(0,0,0,0.7));">
        <button v-if="!isOutOfStock"
          class="btn btn-sm w-100 text-white fw-semibold"
          style="background:#e91e8c;border:none;border-radius:8px;"
          @click.prevent="openSizeModal">
          <i class="bi bi-cart-plus me-1"></i>Thêm vào giỏ
        </button>
        <router-link v-else :to="`/products/${product.slug}`"
          class="btn btn-sm w-100 btn-light"
          style="border-radius:8px;font-size:0.8rem;">
          Xem chi tiết
        </router-link>
      </div>
    </div>
 
    <!-- Body -->
    <div class="card-body p-3">
      <div class="d-flex justify-content-between align-items-center mb-1">
        <small v-if="product.seriesName" class="text-muted">
          <i class="bi bi-collection me-1"></i>{{ product.seriesName }}
        </small>
        <small v-if="product.characterName" class="text-muted">{{ product.characterName }}</small>
      </div>
      <router-link :to="`/products/${product.slug}`" class="text-decoration-none">
        <h6 class="card-title mb-2 text-dark fw-semibold lh-sm"
          style="display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;">
          {{ product.name }}
        </h6>
      </router-link>
      <div class="d-flex align-items-center gap-1 mb-2">
        <div class="text-warning small">
          <i v-for="n in 5" :key="n"
            :class="['bi',n<=Math.round(product.avgRating||0)?'bi-star-fill':'bi-star']"></i>
        </div>
        <small class="text-muted">({{ product.reviewCount||0 }})</small>
      </div>
      <div>
        <div v-if="product.salePrice" class="fw-bold" style="color:#e91e8c;font-size:1.05rem;">
          {{ formatPrice(product.salePrice) }}
        </div>
        <div v-if="product.rentalPricePerDay" class="mt-1">
          <small class="text-info">
            <i class="bi bi-calendar-event me-1"></i>Thuê: {{ formatPrice(product.rentalPricePerDay) }}/ngày
          </small>
        </div>
      </div>
      <!-- Nút Xem chi tiết luôn hiển thị -->
      <router-link :to="`/products/${product.slug}`"
        class="btn btn-sm btn-outline-secondary w-100 mt-2 rounded-pill"
        style="font-size:0.8rem;">
        <i class="bi bi-eye me-1"></i>Xem chi tiết
      </router-link>
    </div>
  </div>
 
  <!-- ── Size / Add to Cart Modal ─────────────────── -->
  <Teleport to="body">
    <div v-if="showSizeModal"
      class="position-fixed d-flex align-items-center justify-content-center"
      style="inset:0;background:rgba(0,0,0,0.5);z-index:2000;padding:16px;"
      @click.self="showSizeModal=false">
      <div class="card border-0 shadow-lg"
        style="width:100%;max-width:420px;border-radius:20px;max-height:90vh;overflow-y:auto;">
        <div class="card-body p-4">
          <!-- Header -->
          <div class="d-flex align-items-center gap-3 mb-4">
            <img :src="product.primaryImageUrl||'/img/placeholder.svg'"
              class="rounded-3" style="width:64px;height:64px;object-fit:cover;"
              @error="$event.target.src='/img/placeholder.svg'">
            <div>
              <div class="fw-bold">{{ product.name }}</div>
              <div class="text-muted small">{{ product.seriesName }} · {{ product.characterName }}</div>
              <div class="fw-bold mt-1" style="color:#e91e8c;">{{ formatPrice(product.salePrice) }}</div>
            </div>
            <button type="button" class="btn-close ms-auto" @click="showSizeModal=false"></button>
          </div>
 
          <!-- Loading variants -->
          <div v-if="loadingVariants" class="text-center py-3">
            <div class="spinner-border spinner-border-sm" style="color:#e91e8c;"></div>
            <div class="small text-muted mt-1">Đang tải size...</div>
          </div>
 
          <div v-else>
            <!-- Order type (nếu BOTH) -->
            <div v-if="product.productType==='BOTH'" class="mb-3">
              <div class="small fw-semibold mb-2">Hình thức:</div>
              <div class="d-flex gap-2">
                <button :class="['btn btn-sm rounded-pill',orderType==='SALE'?'btn-dark':'btn-outline-secondary']"
                  @click="orderType='SALE'"> Mua</button>
                <button :class="['btn btn-sm rounded-pill',orderType==='RENTAL'?'btn-info text-white':'btn-outline-info']"
                  @click="orderType='RENTAL'"> Thuê</button>
              </div>
            </div>
 
            <!-- Rental dates -->
            <div v-if="orderType==='RENTAL'" class="mb-3">
              <div class="small fw-semibold mb-2">Ngày thuê:</div>
              <div class="row g-2">
                <div class="col-6">
                  <input type="date" class="form-control form-control-sm"
                    v-model="rentalStart" :min="today">
                </div>
                <div class="col-6">
                  <input type="date" class="form-control form-control-sm"
                    v-model="rentalEnd" :min="rentalStart||today">
                </div>
              </div>
              <small v-if="rentalDays>0" class="text-info mt-1 d-block">
                {{ rentalDays }} ngày · {{ formatPrice(product.rentalPricePerDay * rentalDays) }}
              </small>
            </div>
 
            <!-- Size selection -->
            <div class="mb-3">
              <div class="small fw-semibold mb-2">
                Chọn Size: <span v-if="selectedVariant" class="text-primary">{{ selectedVariant.size }}
                  <span v-if="selectedVariant.color"> - {{ selectedVariant.color }}</span>
                </span>
              </div>
              <div v-if="!variants.length" class="text-muted small">
                Không có biến thể — nhấn thêm vào giỏ để tiếp tục
              </div>
              <div class="d-flex gap-2 flex-wrap">
                <button v-for="v in variants" :key="v.id"
                  :class="['btn btn-sm rounded-2',selectedVariant?.id===v.id?'btn-dark':'btn-outline-secondary']"
                  :disabled="(orderType==='SALE'&&v.stockQuantity<=0)||(orderType==='RENTAL'&&v.rentalQuantity<=0)"
                  style="min-width:52px;"
                  @click="selectedVariant=v">
                  {{ v.size }}
                  <span v-if="v.color" class="d-block" style="font-size:0.6rem;">{{ v.color }}</span>
                  <span v-if="orderType==='SALE'&&v.stockQuantity<=0"
                    class="d-block" style="font-size:0.6rem;color:#dc3545;">Hết</span>
                </button>
              </div>
            </div>
 
            <!-- Quantity -->
            <div class="d-flex align-items-center gap-3 mb-4">
              <div class="small fw-semibold">Số lượng:</div>
              <div class="d-flex align-items-center gap-2">
                <button class="btn btn-outline-secondary btn-sm rounded-circle"
                  style="width:30px;height:30px;padding:0;" @click="qty>1&&qty--">
                  <i class="bi bi-dash"></i>
                </button>
                <span class="fw-bold" style="min-width:28px;text-align:center;">{{ qty }}</span>
                <button class="btn btn-outline-secondary btn-sm rounded-circle"
                  style="width:30px;height:30px;padding:0;" @click="qty++">
                  <i class="bi bi-plus"></i>
                </button>
              </div>
            </div>
 
            <!-- Actions -->
            <div class="d-flex gap-2">
              <button class="btn flex-grow-1 py-2 fw-bold"
                style="background:#e91e8c;color:#fff;border-radius:12px;"
                :disabled="(!selectedVariant&&variants.length>0)||adding"
                @click="confirmAddToCart">
                <span v-if="adding" class="spinner-border spinner-border-sm me-2"></span>
                <i v-else class="bi bi-cart-plus me-1"></i>
                Thêm vào giỏ hàng
              </button>
              <router-link :to="`/products/${product.slug}`"
                class="btn btn-outline-secondary px-3 rounded-pill"
                @click="showSizeModal=false">
                Chi tiết
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>
 
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/store/auth'
import { useCartStore } from '@/store/cart'
import { productApi } from '@/api/index'
import { useToast } from 'vue-toastification'
 
const props = defineProps({ product: { type: Object, required: true } })
 
const authStore = useAuthStore()
const cartStore = useCartStore()
const toast = useToast()
 
const isFav = ref(false)
 
// Sản phẩm hết hàng khi không có variant nào còn stock
// Kiểm tra từ thông tin cơ bản - nếu không có variants thì không biết → để mở
const isOutOfStock = computed(() => {
  if (!variants.value.length) return false // chưa load → không ẩn
  const type = props.product.productType
  if (type === 'RENTAL') return variants.value.every(v => v.rentalQuantity <= 0)
  if (type === 'SALE')   return variants.value.every(v => v.stockQuantity <= 0)
  // BOTH: hết cả mua lẫn thuê
  return variants.value.every(v => v.stockQuantity <= 0 && v.rentalQuantity <= 0)
})
const showSizeModal = ref(false)
const loadingVariants = ref(false)
const variants = ref([])
const selectedVariant = ref(null)
const orderType = ref(props.product.productType === 'RENTAL' ? 'RENTAL' : 'SALE')
const qty = ref(1)
const adding = ref(false)
const rentalStart = ref('')
const rentalEnd = ref('')
const today = new Date().toISOString().split('T')[0]
 
const rentalDays = computed(() => {
  if (!rentalStart.value || !rentalEnd.value) return 0
  // +1 vì tính cả ngày đầu và ngày cuối (inclusive)
  const d = Math.ceil((new Date(rentalEnd.value) - new Date(rentalStart.value)) / 86400000) + 1
  return Math.max(1, d)
})
 
const formatPrice = (p) => p != null
  ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p)
  : '—'
 
// Mở modal và fetch variants nếu chưa có
const openSizeModal = async () => {
  if (!authStore.isLoggedIn) {
    toast.warning('Vui lòng đăng nhập để thêm vào giỏ hàng')
    return
  }
  showSizeModal.value = true
  qty.value = 1
  selectedVariant.value = null
  orderType.value = props.product.productType === 'RENTAL' ? 'RENTAL' : 'SALE'
 
  // Nếu chưa có variants, fetch từ product detail
  if (!variants.value.length) {
    loadingVariants.value = true
    try {
      const res = await productApi.getProductDetail(props.product.slug)
      variants.value = res.data.data?.variants || []
      if (variants.value.length === 1) selectedVariant.value = variants.value[0]
    } catch (e) {
      variants.value = []
    } finally {
      loadingVariants.value = false
    }
  } else {
    if (variants.value.length === 1) selectedVariant.value = variants.value[0]
  }
}
 
const confirmAddToCart = async () => {
  const variantToUse = selectedVariant.value || (variants.value.length === 0 ? null : null)
  if (!variantToUse && variants.value.length > 0) {
    toast.warning('Vui lòng chọn size')
    return
  }
  if (orderType.value === 'RENTAL' && rentalDays.value === 0) {
    toast.warning('Vui lòng chọn ngày thuê')
    return
  }
 
  adding.value = true
  try {
    await cartStore.addItem({
      productVariantId: variantToUse?.id,
      quantity: qty.value,
      orderType: orderType.value,
      rentalStartDate: orderType.value === 'RENTAL' ? rentalStart.value : null,
      rentalEndDate: orderType.value === 'RENTAL' ? rentalEnd.value : null
    })
    toast.success('Đã thêm vào giỏ hàng! 🛒')
    showSizeModal.value = false
  } catch (e) {
    toast.error(e.response?.data?.message || 'Có lỗi xảy ra')
  } finally {
    adding.value = false
  }
}
 
const toggleFav = async () => {
  try {
    const res = await productApi.toggleFavorite(props.product.id)
    isFav.value = res.data.data
    toast.success(isFav.value ? 'Đã thêm vào yêu thích ❤️' : 'Đã bỏ yêu thích')
  } catch (e) {}
}
 
onMounted(async () => {
  if (authStore.isLoggedIn) {
    try {
      const res = await productApi.isFavorite(props.product.id)
      isFav.value = res.data.data
    } catch (e) {}
  }
})
</script>