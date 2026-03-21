<template>
  <div>
    <!-- Hero Banner (Carousel) -->
    <section v-if="banners.length" id="heroBanner" class="carousel slide" data-bs-ride="carousel">
      <div class="carousel-indicators">
        <button v-for="(b, i) in banners" :key="b.id" type="button"
          :data-bs-target="'#heroBanner'" :data-bs-slide-to="i"
          :class="{ active: i === 0 }"></button>
      </div>
      <div class="carousel-inner">
        <div v-for="(banner, i) in banners" :key="banner.id"
          :class="['carousel-item', { active: i === 0 }]">
          <img :src="banner.imageUrl" class="d-block w-100"
            style="height: 480px; object-fit: cover;"
            :alt="banner.title"
            @error="$event.target.src = 'https://images.unsplash.com/photo-1578632292335-df3abbb0d586?w=1200'"
          />
          <div class="carousel-caption text-start" style="bottom: 60px;">
            <h2 class="fw-bold fs-1">{{ banner.title }}</h2>
            <p class="fs-5 opacity-75">{{ banner.description }}</p>
            <router-link :to="banner.linkUrl || '/products'"
              class="btn btn-lg px-5 py-3 rounded-pill"
              style="background:#e91e8c;color:#fff;border:none;font-weight:700;">
              Khám phá ngay <i class="bi bi-arrow-right ms-2"></i>
            </router-link>
          </div>
        </div>
      </div>
      <button class="carousel-control-prev" data-bs-target="#heroBanner" data-bs-slide="prev">
        <span class="carousel-control-prev-icon"></span>
      </button>
      <button class="carousel-control-next" data-bs-target="#heroBanner" data-bs-slide="next">
        <span class="carousel-control-next-icon"></span>
      </button>
    </section>

    <!-- Category Quick Links -->
    <section class="container py-5">
      <div class="row g-3 justify-content-center">
        <div v-for="cat in categories" :key="cat.id" class="col-6 col-md-3">
          <router-link :to="`/products?categoryId=${cat.id}`" class="text-decoration-none">
            <div class="card border-0 text-center p-3 h-100"
              style="border-radius:16px;box-shadow:0 4px 20px rgba(0,0,0,0.08);transition:all 0.3s;"
              @mouseenter="$event.currentTarget.style.transform='translateY(-4px)'"
              @mouseleave="$event.currentTarget.style.transform='translateY(0)'">
              <img :src="cat.imageUrl" :alt="cat.name"
                class="rounded-circle mx-auto mb-3"
                style="width:70px;height:70px;object-fit:cover;"
                @error="$event.target.src = '/img/placeholder.svg'"/>
              <h6 class="fw-bold text-dark mb-0">{{ cat.name }}</h6>
            </div>
          </router-link>
        </div>
      </div>
    </section>

    <!-- Flash Sale Banner -->
    <section v-if="flashSaleActive" class="py-4"
      style="background: linear-gradient(135deg, #ff4757, #e91e8c);">
      <div class="container d-flex align-items-center justify-content-between flex-wrap gap-3">
        <div class="text-white">
          <h4 class="mb-1 fw-bold"> FLASH SALE đang diễn ra!</h4>
          <p class="mb-0 opacity-75">Ưu đãi khủng - Số lượng có hạn!</p>
        </div>
        <router-link to="/flash-sale" class="btn btn-light fw-bold rounded-pill px-4">
          Mua ngay <i class="bi bi-arrow-right"></i>
        </router-link>
      </div>
    </section>

    <!-- Hot Products -->
    <section class="container py-5">
      <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
          <h3 class="fw-bold mb-1"> Sản phẩm nổi bật</h3>
          <p class="text-muted mb-0">Được yêu thích nhất</p>
        </div>
        <router-link to="/products?sortBy=isHot&sortDir=desc" class="btn btn-outline-danger rounded-pill">
          Xem tất cả <i class="bi bi-arrow-right"></i>
        </router-link>
      </div>

      <div v-if="productStore.loading" class="row g-3">
        <div v-for="n in 4" :key="n" class="col-6 col-md-3">
          <div class="card border-0 placeholder-glow" style="border-radius:12px;height:380px;">
            <span class="placeholder w-100 h-100 rounded"></span>
          </div>
        </div>
      </div>

      <div v-else class="row g-3">
        <div v-for="product in productStore.hotProducts" :key="product.id"
          class="col-6 col-md-3">
          <ProductCard :product="product" />
        </div>
      </div>
    </section>

    <!-- Series Section -->
    <section style="background: #f0f2ff;" class="py-5">
      <div class="container">
        <h3 class="fw-bold mb-4 text-center">Khám phá theo Series</h3>
        <div class="row g-3">
          <div v-for="s in seriesList" :key="s.id" class="col-6 col-md-4 col-lg-2">
            <router-link :to="`/products?seriesId=${s.id}`" class="text-decoration-none">
              <div class="card border-0 text-center p-3 h-100"
                style="border-radius:12px;transition:all 0.3s;"
                @mouseenter="$event.currentTarget.style.boxShadow='0 8px 30px rgba(233,30,140,0.2)'"
                @mouseleave="$event.currentTarget.style.boxShadow='none'">
                <div class="mb-2" style="font-size:2rem;">
                  {{ seriesIcon(s.seriesType) }}
                </div>
                <div class="fw-semibold text-dark small">{{ s.name }}</div>
                <div class="mt-1">
                  <span class="badge text-bg-light text-muted" style="font-size:0.65rem;">{{ s.seriesType }}</span>
                </div>
              </div>
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <!-- New Products -->
    <section class="container py-5">
      <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
          <h3 class="fw-bold mb-1">Hàng mới về</h3>
          <p class="text-muted mb-0">Cập nhật liên tục</p>
        </div>
        <router-link to="/products?sortBy=createdAt&sortDir=desc" class="btn btn-outline-secondary rounded-pill">
          Xem thêm <i class="bi bi-arrow-right"></i>
        </router-link>
      </div>
      <div class="row g-3">
        <div v-for="product in productStore.newProducts" :key="product.id"
          class="col-6 col-md-3">
          <ProductCard :product="product" />
        </div>
      </div>
    </section>

    <!-- Combo Section
    <section style="background: linear-gradient(135deg, #1a1a2e, #16213e);" class="py-5">
      <div class="container text-center text-white">
        <h3 class="fw-bold mb-2"> Combo Cosplay Full Set</h3>
        <p class="opacity-75 mb-4">Trang phục + Tóc giả + Phụ kiện - Tiết kiệm hơn mua lẻ!</p>
        <router-link to="/combos" class="btn btn-lg rounded-pill px-5"
          style="background:#e91e8c;color:#fff;border:none;">
          <i class="bi bi-bag-heart me-2"></i>Xem Combo ngay
        </router-link>
      </div>
    </section> -->

    <!-- Features Bar -->
    <section class="container py-5">
      <div class="row g-4 text-center">
        <div v-for="f in features" :key="f.icon" class="col-6 col-md-3">
          <div class="p-3">
            <div style="font-size:2.5rem;">{{ f.icon }}</div>
            <h6 class="fw-bold mt-2">{{ f.title }}</h6>
            <small class="text-muted">{{ f.desc }}</small>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useProductStore } from '@/store/product'
import { productApi } from '@/api/index'
import ProductCard from '@/components/product/ProductCard.vue'

const productStore = useProductStore()
const banners = ref([])
const categories = ref([])
const seriesList = ref([])
const flashSaleActive = ref(false)

const features = [
  {  title: 'Giao hàng nhanh', desc: 'Toàn quốc trong 2-5 ngày' },
  {  title: 'Đổi trả dễ dàng', desc: 'Trong vòng 7 ngày' },
  {  title: 'Chất lượng cao', desc: 'Vải tốt, may đẹp' },
  {  title: 'Thuê & Mua', desc: 'Linh hoạt theo nhu cầu' }
]

const seriesIcon = (type) => {
  const icons = { ANIME: '🎌', GAME: '🎮', MOVIE: '🎬', OTHER: '✨' }
  return icons[type] || '✨'
}

onMounted(async () => {
  try {
    const [bannersRes, categoriesRes, seriesRes] = await Promise.all([
      productApi.getBanners(),
      productApi.getCategories(),
      productApi.getSeriesList()
    ])
    banners.value = bannersRes.data.data || []
    categories.value = categoriesRes.data.data || []
    seriesList.value = seriesRes.data.data || []
  } catch (e) {}

  await Promise.all([
    productStore.fetchHotProducts(),
    productStore.fetchNewProducts()
  ])
})
</script>
