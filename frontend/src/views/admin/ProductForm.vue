<template>
  <div>
    <!-- Header -->
    <div class="d-flex align-items-center gap-3 mb-4">
      <button class="btn btn-outline-secondary rounded-circle"
        style="width:38px;height:38px;padding:0;" @click="$router.back()">
        <i class="bi bi-arrow-left"></i>
      </button>
      <div>
        <h5 class="fw-bold mb-0">{{ isEdit ? 'Chỉnh sửa sản phẩm' : 'Thêm sản phẩm mới' }}</h5>
        <small class="text-muted">{{ isEdit ? `ID: ${route.params.id}` : 'Điền thông tin sản phẩm' }}</small>
      </div>
      <div class="ms-auto d-flex gap-2">
        <button class="btn btn-outline-secondary rounded-pill px-4" @click="$router.back()">Hủy</button>
        <button class="btn rounded-pill px-4 fw-semibold"
          style="background:#e91e8c;color:#fff;"
          :disabled="saving" @click="save">
          <span v-if="saving" class="spinner-border spinner-border-sm me-2"></span>
          <i v-else class="bi bi-check-lg me-2"></i>
          {{ isEdit ? 'Lưu thay đổi' : 'Tạo sản phẩm' }}
        </button>
      </div>
    </div>

    <div class="row g-4">
      <!-- LEFT: Main Info -->
      <div class="col-lg-8 d-flex flex-column gap-4">

        <!-- Thông tin cơ bản -->
        <div class="card border-0 shadow-sm" style="border-radius:16px;">
          <div class="card-body p-4">
            <h6 class="fw-bold mb-4 d-flex align-items-center gap-2">
              <span class="rounded-2 d-flex align-items-center justify-content-center"
                style="width:28px;height:28px;background:#e91e8c22;">
                <i class="bi bi-info-circle" style="color:#e91e8c;"></i>
              </span>
              Thông tin cơ bản
            </h6>

            <div class="row g-3">
              <div class="col-12">
                <label class="form-label small fw-semibold">Tên sản phẩm <span class="text-danger">*</span></label>
                <input type="text" class="form-control" v-model="form.name"
                  placeholder="VD: Bộ Cosplay Naruto Uzumaki - Hokage"
                  @input="autoSlug" required>
              </div>

              <div class="col-md-6">
                <label class="form-label small fw-semibold">Slug (URL) <span class="text-danger">*</span></label>
                <div class="input-group">
                  <span class="input-group-text text-muted small">/products/</span>
                  <input type="text" class="form-control" v-model="form.slug"
                    placeholder="naruto-hokage-set">
                </div>
              </div>

              <div class="col-md-6">
                <label class="form-label small fw-semibold">Tên nhân vật</label>
                <input type="text" class="form-control" v-model="form.characterName"
                  placeholder="VD: Naruto Uzumaki">
              </div>

              <div class="col-12">
                <label class="form-label small fw-semibold">Mô tả sản phẩm</label>
                <textarea class="form-control" v-model="form.description"
                  rows="4" placeholder="Mô tả chi tiết về sản phẩm, chất liệu, kích thước..."></textarea>
              </div>
            </div>
          </div>
        </div>

        <!-- Phân loại -->
        <div class="card border-0 shadow-sm" style="border-radius:16px;">
          <div class="card-body p-4">
            <h6 class="fw-bold mb-4 d-flex align-items-center gap-2">
              <span class="rounded-2 d-flex align-items-center justify-content-center"
                style="width:28px;height:28px;background:#7c4dff22;">
                <i class="bi bi-collection" style="color:#7c4dff;"></i>
              </span>
              Phân loại & Danh mục
            </h6>
            <div class="row g-3">
              <div class="col-md-4">
                <label class="form-label small fw-semibold">Danh mục <span class="text-danger">*</span></label>
                <select class="form-select" v-model="form.categoryId">
                  <option value="">-- Chọn danh mục --</option>
                  <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
                </select>
              </div>
              <div class="col-md-4">
                <label class="form-label small fw-semibold">Series / Bộ truyện</label>
                <select class="form-select" v-model="form.seriesId">
                  <option value="">-- Không thuộc series --</option>
                  <option v-for="s in seriesList" :key="s.id" :value="s.id">{{ s.name }}</option>
                </select>
              </div>
              <div class="col-md-4">
                <label class="form-label small fw-semibold">Loại sản phẩm <span class="text-danger">*</span></label>
                <select class="form-select" v-model="form.productType">
                  <option value="SALE">🛍️ Chỉ bán</option>
                  <option value="RENTAL">📅 Chỉ cho thuê</option>
                  <option value="BOTH">🔄 Vừa bán vừa cho thuê</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        <!-- Giá -->
        <div class="card border-0 shadow-sm" style="border-radius:16px;">
          <div class="card-body p-4">
            <h6 class="fw-bold mb-4 d-flex align-items-center gap-2">
              <span class="rounded-2 d-flex align-items-center justify-content-center"
                style="width:28px;height:28px;background:#28a74522;">
                <i class="bi bi-cash-stack" style="color:#28a745;"></i>
              </span>
              Giá bán
            </h6>
            <div class="row g-3">
              <div class="col-md-4" v-if="form.productType !== 'RENTAL'">
                <label class="form-label small fw-semibold">
                  Giá bán (VNĐ) <span class="text-danger">*</span>
                </label>
                <div class="input-group">
                  <input type="number" class="form-control" v-model="form.salePrice"
                    placeholder="0" min="0">
                  <span class="input-group-text">₫</span>
                </div>
              </div>
              <div class="col-md-4" v-if="form.productType !== 'SALE'">
                <label class="form-label small fw-semibold">
                  Giá thuê/ngày (VNĐ) <span class="text-danger">*</span>
                </label>
                <div class="input-group">
                  <input type="number" class="form-control" v-model="form.rentalPricePerDay"
                    placeholder="0" min="0">
                  <span class="input-group-text">₫/ngày</span>
                </div>
              </div>
              <div class="col-md-4" v-if="form.productType !== 'SALE'">
                <label class="form-label small fw-semibold">Tiền đặt cọc (VNĐ)</label>
                <div class="input-group">
                  <input type="number" class="form-control" v-model="form.depositAmount"
                    placeholder="0" min="0">
                  <span class="input-group-text">₫</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Biến thể (Size) -->
        <div class="card border-0 shadow-sm" style="border-radius:16px;">
          <div class="card-body p-4">
            <div class="d-flex align-items-center justify-content-between mb-4">
              <h6 class="fw-bold mb-0 d-flex align-items-center gap-2">
                <span class="rounded-2 d-flex align-items-center justify-content-center"
                  style="width:28px;height:28px;background:#17a2b822;">
                  <i class="bi bi-rulers" style="color:#17a2b8;"></i>
                </span>
                Biến thể & Tồn kho
              </h6>
              <button type="button" class="btn btn-sm btn-outline-primary rounded-pill"
                @click="addVariant">
                <i class="bi bi-plus-lg me-1"></i>Thêm size
              </button>
            </div>

            <div v-if="!form.variants.length" class="text-center py-3 text-muted">
              <i class="bi bi-rulers fs-2 d-block mb-2"></i>
              Chưa có biến thể. Nhấn "Thêm size" để bắt đầu.
            </div>

            <div v-for="(v, idx) in form.variants" :key="idx"
              class="row g-2 align-items-end mb-3 pb-3"
              :class="{ 'border-bottom': idx < form.variants.length - 1 }">
              <div class="col-6 col-md-2">
                <label class="form-label small fw-semibold">Size *</label>
                <input type="text" class="form-control form-control-sm" v-model="v.size"
                  placeholder="S, M, L, XL...">
              </div>
              <div class="col-6 col-md-2">
                <label class="form-label small fw-semibold">Màu sắc</label>
                <input type="text" class="form-control form-control-sm" v-model="v.color"
                  placeholder="Đỏ, Xanh...">
              </div>
              <div class="col-6 col-md-2">
                <label class="form-label small fw-semibold">SL bán</label>
                <input type="number" class="form-control form-control-sm" v-model="v.stockQuantity"
                  min="0" placeholder="0">
              </div>
              <div class="col-6 col-md-2" v-if="form.productType !== 'SALE'">
                <label class="form-label small fw-semibold">SL thuê</label>
                <input type="number" class="form-control form-control-sm" v-model="v.rentalQuantity"
                  min="0" placeholder="0">
              </div>
              <div class="col-md-3">
                <label class="form-label small fw-semibold">Giá biến thể (bỏ trống = dùng giá chung)</label>
                <input type="number" class="form-control form-control-sm" v-model="v.price"
                  min="0" placeholder="Tuỳ chọn">
              </div>
              <div class="col-auto">
                <button type="button" class="btn btn-sm btn-outline-danger rounded"
                  style="height:31px;" @click="removeVariant(idx)">
                  <i class="bi bi-trash"></i>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Hình ảnh -->
        <div class="card border-0 shadow-sm" style="border-radius:16px;">
          <div class="card-body p-4">
            <h6 class="fw-bold mb-4 d-flex align-items-center gap-2">
              <span class="rounded-2 d-flex align-items-center justify-content-center"
                style="width:28px;height:28px;background:#ffc10722;">
                <i class="bi bi-images" style="color:#ffc107;"></i>
              </span>
              Hình ảnh sản phẩm
            </h6>

            <!-- Image URLs input -->
            <div v-for="(img, idx) in form.images" :key="idx" class="d-flex gap-2 mb-2">
              <input type="url" class="form-control form-control-sm" v-model="form.images[idx]"
                placeholder="https://... (URL hình ảnh)">
              <button type="button" class="btn btn-sm btn-outline-danger rounded"
                @click="form.images.splice(idx, 1)">
                <i class="bi bi-x"></i>
              </button>
            </div>

            <button type="button" class="btn btn-sm btn-outline-secondary rounded-pill mt-2"
              @click="form.images.push('')">
              <i class="bi bi-plus-lg me-1"></i>Thêm URL ảnh
            </button>

            <!-- Preview -->
            <div v-if="form.images.filter(i => i).length" class="d-flex gap-2 flex-wrap mt-3">
              <div v-for="(url, i) in form.images.filter(u => u)" :key="i"
                class="position-relative rounded overflow-hidden"
                style="width:80px;height:80px;">
                <img :src="url" class="w-100 h-100" style="object-fit:cover;"
                  @error="$event.target.src = '/img/placeholder.svg'">
                <div class="position-absolute top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center"
                  style="background:rgba(0,0,0,0);" />
              </div>
            </div>
          </div>
        </div>

      </div>

      <!-- RIGHT: Settings sidebar -->
      <div class="col-lg-4 d-flex flex-column gap-4">

        <!-- Trạng thái -->
        <div class="card border-0 shadow-sm" style="border-radius:16px;">
          <div class="card-body p-4">
            <h6 class="fw-bold mb-3">Trạng thái</h6>
            <div class="d-flex align-items-center justify-content-between p-3 rounded-3"
              :style="form.isActive ? 'background:#d4edda' : 'background:#f8d7da'">
              <div>
                <div class="fw-semibold small" :class="form.isActive ? 'text-success' : 'text-danger'">
                  {{ form.isActive ? '✅ Đang hiển thị' : '❌ Đang ẩn' }}
                </div>
                <small class="text-muted">{{ form.isActive ? 'Khách hàng có thể thấy' : 'Không hiển thị với khách' }}</small>
              </div>
              <div class="form-check form-switch mb-0">
                <input class="form-check-input" type="checkbox" v-model="form.isActive"
                  style="width:40px;height:22px;">
              </div>
            </div>

            <div class="mt-3">
              <div class="form-check">
                <input class="form-check-input" type="checkbox" id="isHot" v-model="form.isHot">
                <label class="form-check-label small fw-semibold" for="isHot">
                  🔥 Đánh dấu là Hot
                </label>
              </div>
              <div class="form-check mt-2">
                <input class="form-check-input" type="checkbox" id="isNew" v-model="form.isNew">
                <label class="form-check-label small fw-semibold" for="isNew">
                  ✨ Đánh dấu là Mới
                </label>
              </div>
            </div>
          </div>
        </div>

        <!-- Preview -->
        <div class="card border-0 shadow-sm" style="border-radius:16px;overflow:hidden;">
          <div class="card-body p-0">
            <div style="height:180px;background:#f0f2ff;overflow:hidden;">
              <img v-if="form.images[0]"
                :src="form.images[0]"
                class="w-100 h-100" style="object-fit:cover;"
                @error="$event.target.src = '/img/placeholder.svg'">
              <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center text-muted">
                <div class="text-center">
                  <i class="bi bi-image fs-1 d-block mb-2"></i>
                  <small>Xem trước ảnh</small>
                </div>
              </div>
            </div>
            <div class="p-3">
              <div class="fw-semibold small lh-sm mb-1">{{ form.name || 'Tên sản phẩm' }}</div>
              <div class="small" style="color:#e91e8c;">
                {{ form.salePrice ? formatPrice(form.salePrice) : '—' }}
              </div>
              <div class="small text-info" v-if="form.rentalPricePerDay">
                Thuê: {{ formatPrice(form.rentalPricePerDay) }}/ngày
              </div>
              <div class="mt-2 d-flex gap-1 flex-wrap">
                <span v-if="form.isHot" class="badge" style="background:#ff4757;font-size:0.65rem;">🔥 Hot</span>
                <span v-if="form.isNew" class="badge bg-success" style="font-size:0.65rem;">✨ Mới</span>
                <span class="badge text-bg-light text-dark" style="font-size:0.65rem;">
                  {{ { SALE: '🛍️ Bán', RENTAL: '📅 Thuê', BOTH: '🔄 Cả hai' }[form.productType] }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Nhanh chóng -->
        <div class="card border-0 shadow-sm" style="border-radius:16px;">
          <div class="card-body p-4">
            <h6 class="fw-bold mb-3">⚡ Size nhanh</h6>
            <div class="d-flex gap-2 flex-wrap">
              <button v-for="preset in sizePresets" :key="preset"
                type="button"
                class="btn btn-sm btn-outline-secondary rounded-pill"
                style="font-size:0.75rem;"
                @click="addPresetVariant(preset)">
                + {{ preset }}
              </button>
            </div>
            <small class="text-muted d-block mt-2">Nhấn để thêm nhanh biến thể size</small>
          </div>
        </div>

        <!-- SEO / Ghi chú nội bộ -->
        <div class="card border-0 shadow-sm" style="border-radius:16px;">
          <div class="card-body p-4">
            <h6 class="fw-bold mb-3">📌 Ghi chú nội bộ</h6>
            <textarea class="form-control form-control-sm" v-model="form.adminNote"
              rows="3" placeholder="Ghi chú nội bộ, không hiển thị với khách hàng..."></textarea>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '@/api/index'
import { useToast } from 'vue-toastification'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const saving = ref(false)
const categories = ref([])
const seriesList = ref([])

const isEdit = computed(() => !!route.params.id)

const sizePresets = ['XS', 'S', 'M', 'L', 'XL', 'XXL', 'Free size']

const defaultForm = () => ({
  name: '',
  slug: '',
  characterName: '',
  description: '',
  categoryId: '',
  seriesId: '',
  productType: 'SALE',
  salePrice: null,
  rentalPricePerDay: null,
  depositAmount: null,
  isActive: true,
  isHot: false,
  isNew: true,
  adminNote: '',
  variants: [],
  images: ['']
})

const form = reactive(defaultForm())

const formatPrice = (p) => p
  ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p)
  : '—'

// Auto-generate slug from name
const autoSlug = () => {
  if (isEdit.value) return
  form.slug = form.name
    .toLowerCase()
    .normalize('NFD').replace(/[\u0300-\u036f]/g, '')
    .replace(/đ/g, 'd').replace(/Đ/g, 'd')
    .replace(/[^a-z0-9\s-]/g, '')
    .trim()
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
}

const addVariant = () => {
  form.variants.push({ size: '', color: '', stockQuantity: 0, rentalQuantity: 0, price: null })
}

const removeVariant = (idx) => {
  form.variants.splice(idx, 1)
}

const addPresetVariant = (size) => {
  if (form.variants.some(v => v.size === size)) return
  form.variants.push({ size, color: '', stockQuantity: 10, rentalQuantity: 5, price: null })
}

// Validate
const validate = () => {
  if (!form.name.trim()) { toast.error('Vui lòng nhập tên sản phẩm'); return false }
  if (!form.slug.trim()) { toast.error('Vui lòng nhập slug'); return false }
  if (!form.categoryId) { toast.error('Vui lòng chọn danh mục'); return false }
  if (form.productType !== 'RENTAL' && !form.salePrice) { toast.error('Vui lòng nhập giá bán'); return false }
  if (form.productType !== 'SALE' && !form.rentalPricePerDay) { toast.error('Vui lòng nhập giá thuê'); return false }
  if (!form.variants.length) { toast.error('Vui lòng thêm ít nhất 1 biến thể (size)'); return false }
  if (form.variants.some(v => !v.size.trim())) { toast.error('Vui lòng nhập tên size cho tất cả biến thể'); return false }
  return true
}

const buildPayload = () => ({
  name: form.name.trim(),
  slug: form.slug.trim(),
  characterName: form.characterName || null,
  description: form.description || null,
  categoryId: Number(form.categoryId),
  seriesId: form.seriesId ? Number(form.seriesId) : null,
  productType: form.productType,
  salePrice: form.productType !== 'RENTAL' ? Number(form.salePrice) : null,
  rentalPricePerDay: form.productType !== 'SALE' ? Number(form.rentalPricePerDay) : null,
  depositAmount: form.depositAmount ? Number(form.depositAmount) : null,
  isActive: form.isActive,
  isHot: form.isHot,
  isNew: form.isNew,
  variants: form.variants.map(v => ({
    size: v.size.trim(),
    color: v.color || null,
    stockQuantity: Number(v.stockQuantity) || 0,
    rentalQuantity: Number(v.rentalQuantity) || 0,
    price: v.price ? Number(v.price) : null,
    isActive: true
  })),
  imageUrls: form.images.filter(i => i.trim())
})

const save = async () => {
  if (!validate()) return
  saving.value = true
  try {
    const payload = buildPayload()
    if (isEdit.value) {
      await productApi.admin.updateProduct(route.params.id, payload)
      toast.success('Cập nhật sản phẩm thành công! ✅')
    } else {
      await productApi.admin.createProduct(payload)
      toast.success('Tạo sản phẩm thành công! 🎉')
    }
    router.push('/admin/products')
  } catch (e) {
    toast.error(e.response?.data?.message || 'Có lỗi xảy ra')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  // Load categories & series
  try {
    const [cRes, sRes] = await Promise.all([
      productApi.getCategories(),
      productApi.getSeriesList()
    ])
    categories.value = cRes.data.data || []
    seriesList.value = sRes.data.data || []
  } catch (e) {}

  // Load product data if edit mode
  if (isEdit.value) {
    try {
      // Search for product by id via admin endpoint
      const res = await productApi.admin.getProducts({ page: 0, size: 1, id: route.params.id })
      // Alternatively fetch by slug – backend dependent; using basic get
      const allRes = await productApi.admin.getProducts({ size: 1000 })
      const found = allRes.data.data?.content?.find(p => p.id == route.params.id)
      if (found) {
        Object.assign(form, {
          name: found.name || '',
          slug: found.slug || '',
          characterName: found.characterName || '',
          description: found.description || '',
          categoryId: found.categoryId || '',
          seriesId: found.seriesId || '',
          productType: found.productType || 'SALE',
          salePrice: found.salePrice || null,
          rentalPricePerDay: found.rentalPricePerDay || null,
          depositAmount: found.depositAmount || null,
          isActive: found.isActive ?? true,
          isHot: found.isHot ?? false,
          isNew: found.isNew ?? false,
          variants: (found.variants || []).map(v => ({
            id: v.id,
            size: v.size || '',
            color: v.color || '',
            stockQuantity: v.stockQuantity || 0,
            rentalQuantity: v.rentalQuantity || 0,
            price: v.price || null
          })),
          images: found.images?.map(i => i.imageUrl) || (found.primaryImageUrl ? [found.primaryImageUrl] : [''])
        })
      }
    } catch (e) {
      toast.error('Không thể tải thông tin sản phẩm')
    }
  }
})
</script>
