<template>
  <div>
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h5 class="fw-bold mb-1">Quản lý Banner</h5>
        <p class="text-muted small mb-0">Ảnh quảng cáo hiển thị trên trang chủ</p>
      </div>
      <button class="btn rounded-pill fw-semibold" style="background:#e91e8c;color:#fff;"
        @click="openModal()">
        <i class="bi bi-plus-lg me-2"></i>Thêm banner
      </button>
    </div>
 
    <!-- Loading -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border" style="color:#e91e8c;"></div>
    </div>
 
    <!-- Banner Grid -->
    <div v-else-if="banners.length" class="row g-4">
      <div v-for="banner in banners" :key="banner.id" class="col-lg-6">
        <div class="card border-0 shadow-sm h-100" style="border-radius:16px;overflow:hidden;">
          <!-- Banner Preview -->
          <div class="position-relative" style="height:180px;background:#1a1a2e;overflow:hidden;">
            <img v-if="banner.imageUrl" :src="banner.imageUrl"
              class="w-100 h-100" style="object-fit:cover;opacity:0.85;"
              @error="$event.target.style.display='none'">
            <div v-else class="w-100 h-100 d-flex align-items-center justify-content-center">
              <i class="bi bi-image text-white opacity-25" style="font-size:3rem;"></i>
            </div>
 
            <!-- Overlay info -->
            <div class="position-absolute bottom-0 start-0 end-0 p-3"
              style="background:linear-gradient(transparent,rgba(0,0,0,0.8));">
              <div class="text-white fw-bold">{{ banner.title || 'Chưa đặt tiêu đề' }}</div>
              <small class="text-white opacity-75">{{ banner.description || '' }}</small>
            </div>
 
            <!-- Status badge -->
            <div class="position-absolute top-0 end-0 m-2">
              <span class="badge rounded-pill"
                :class="banner.isActive ? 'bg-success' : 'bg-secondary'">
                {{ banner.isActive ? '● Hiển thị' : '● Ẩn' }}
              </span>
            </div>
 
            <!-- Order badge -->
            <div class="position-absolute top-0 start-0 m-2">
              <span class="badge bg-dark bg-opacity-75 rounded-pill">#{{ banner.displayOrder }}</span>
            </div>
          </div>
 
          <!-- Card body -->
          <div class="card-body p-3">
            <div class="row g-2 mb-3">
              <div class="col-6">
                <div class="small text-muted">Link URL</div>
                <div class="small fw-semibold text-truncate" style="max-width:200px;">
                  {{ banner.linkUrl || '—' }}
                </div>
              </div>
              <div class="col-6">
                <div class="small text-muted">Thứ tự hiển thị</div>
                <div class="small fw-semibold">{{ banner.displayOrder }}</div>
              </div>
              <div v-if="banner.startDate || banner.endDate" class="col-12">
                <div class="small text-muted">Thời gian</div>
                <div class="small">
                  {{ banner.startDate ? formatDate(banner.startDate) : '∞' }}
                  →
                  {{ banner.endDate ? formatDate(banner.endDate) : '∞' }}
                </div>
              </div>
            </div>
 
            <!-- Actions -->
            <div class="d-flex gap-2">
              <button class="btn btn-sm btn-outline-primary flex-grow-1 rounded-pill"
                @click="openModal(banner)">
                <i class="bi bi-pencil me-1"></i>Chỉnh sửa
              </button>
              <button class="btn btn-sm rounded-pill"
                :class="banner.isActive ? 'btn-outline-warning' : 'btn-outline-success'"
                @click="toggleBanner(banner)">
                <i :class="['bi', banner.isActive ? 'bi-eye-slash' : 'bi-eye']"></i>
              </button>
              <button class="btn btn-sm btn-outline-danger rounded-pill"
                @click="deleteBanner(banner)">
                <i class="bi bi-trash"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
 
    <!-- Empty state -->
    <div v-else class="text-center py-5">
      <i class="bi bi-image fs-1 text-muted d-block mb-3"></i>
      <h6 class="text-muted">Chưa có banner nào</h6>
      <button class="btn rounded-pill mt-2" style="background:#e91e8c;color:#fff;"
        @click="openModal()">
        <i class="bi bi-plus-lg me-2"></i>Tạo banner đầu tiên
      </button>
    </div>
 
    <!-- Modal tạo/sửa Banner -->
    <Teleport to="body">
      <div v-if="showModal"
        class="position-fixed d-flex align-items-center justify-content-center"
        style="inset:0;background:rgba(0,0,0,0.5);z-index:1055;padding:16px;"
        @click.self="closeModal">
        <div class="card border-0 shadow-lg" style="width:100%;max-width:580px;border-radius:20px;max-height:90vh;overflow-y:auto;">
          <div class="card-body p-5">
            <div class="d-flex align-items-center justify-content-between mb-4">
              <h5 class="fw-bold mb-0">{{ editingBanner ? 'Chỉnh sửa banner' : 'Thêm banner mới' }}</h5>
              <button type="button" class="btn-close" @click="closeModal"></button>
            </div>
 
            <!-- Preview realtime -->
            <div v-if="modalForm.imageUrl" class="rounded-3 overflow-hidden mb-4"
              style="height:160px;background:#1a1a2e;">
              <img :src="modalForm.imageUrl" class="w-100 h-100" style="object-fit:cover;"
                @error="$event.target.style.display='none'">
            </div>
 
            <form @submit.prevent="saveBanner">
              <div class="row g-3">
                <div class="col-12">
                  <label class="form-label small fw-semibold">URL hình ảnh <span class="text-danger">*</span></label>
                  <input type="url" class="form-control" v-model="modalForm.imageUrl"
                    placeholder="https://example.com/banner.jpg" required>
                  <small class="text-muted">Kích thước khuyến nghị: 1200×480px</small>
                </div>
 
                <div class="col-12">
                  <label class="form-label small fw-semibold">Tiêu đề</label>
                  <input type="text" class="form-control" v-model="modalForm.title"
                    placeholder="VD: Flash Sale Mùa Hè 2024">
                </div>
 
                <div class="col-12">
                  <label class="form-label small fw-semibold">Mô tả ngắn</label>
                  <input type="text" class="form-control" v-model="modalForm.description"
                    placeholder="VD: Giảm đến 50% toàn bộ sản phẩm">
                </div>
 
                <div class="col-md-8">
                  <label class="form-label small fw-semibold">Link khi click</label>
                  <input type="text" class="form-control" v-model="modalForm.linkUrl"
                    placeholder="/products hoặc /flash-sale">
                </div>
 
                <div class="col-md-4">
                  <label class="form-label small fw-semibold">Thứ tự hiển thị</label>
                  <input type="number" class="form-control" v-model="modalForm.displayOrder"
                    min="0" placeholder="0">
                </div>
 
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">Từ ngày</label>
                  <input type="datetime-local" class="form-control" v-model="modalForm.startDate">
                </div>
 
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">Đến ngày</label>
                  <input type="datetime-local" class="form-control" v-model="modalForm.endDate">
                </div>
 
                <div class="col-12">
                  <div class="form-check form-switch">
                    <input class="form-check-input" type="checkbox" v-model="modalForm.isActive"
                      id="bannerActive">
                    <label class="form-check-label fw-semibold small" for="bannerActive">
                      Hiển thị ngay
                    </label>
                  </div>
                </div>
              </div>
 
              <div class="d-flex gap-2 mt-4">
                <button type="submit" class="btn flex-grow-1 fw-semibold"
                  style="background:#e91e8c;color:#fff;border-radius:12px;"
                  :disabled="savingBanner">
                  <span v-if="savingBanner" class="spinner-border spinner-border-sm me-2"></span>
                  {{ editingBanner ? 'Lưu thay đổi' : 'Tạo banner' }}
                </button>
                <button type="button" class="btn btn-outline-secondary" style="border-radius:12px;"
                  @click="closeModal">
                  Hủy
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
 
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useToast } from 'vue-toastification'
import { productApi } from '@/api/index'
import api from '@/api/index'
 
const toast = useToast()
const banners = ref([])
const loading = ref(false)
const showModal = ref(false)
const editingBanner = ref(null)
const savingBanner = ref(false)
 
const defaultModal = () => ({
  imageUrl: '', title: '', description: '', linkUrl: '',
  displayOrder: 0, isActive: true, startDate: '', endDate: ''
})
const modalForm = reactive(defaultModal())
 
const formatDate = (d) => d
  ? new Date(d).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
  : ''
 
const fetchBanners = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/banners')
    banners.value = (res.data.data?.content || res.data.data || [])
      .sort((a, b) => a.displayOrder - b.displayOrder)
  } catch (e) {
    toast.error('Không thể tải danh sách banner')
  } finally {
    loading.value = false
  }
}
 
const openModal = (banner = null) => {
  editingBanner.value = banner
  if (banner) {
    Object.assign(modalForm, {
      imageUrl: banner.imageUrl || '',
      title: banner.title || '',
      description: banner.description || '',
      linkUrl: banner.linkUrl || '',
      displayOrder: banner.displayOrder ?? 0,
      isActive: banner.isActive ?? true,
      startDate: banner.startDate ? banner.startDate.substring(0, 16) : '',
      endDate: banner.endDate ? banner.endDate.substring(0, 16) : ''
    })
  } else {
    Object.assign(modalForm, defaultModal())
  }
  showModal.value = true
}
 
const closeModal = () => {
  showModal.value = false
  editingBanner.value = null
}
 
const saveBanner = async () => {
  if (!modalForm.imageUrl.trim()) { toast.error('Vui lòng nhập URL hình ảnh'); return }
  savingBanner.value = true
  try {
    const payload = {
      ...modalForm,
      startDate: modalForm.startDate ? new Date(modalForm.startDate).toISOString() : null,
      endDate: modalForm.endDate ? new Date(modalForm.endDate).toISOString() : null
    }
    if (editingBanner.value) {
      await api.put(`/admin/banners/${editingBanner.value.id}`, payload)
      toast.success('Đã cập nhật banner')
    } else {
      await api.post('/admin/banners', payload)
      toast.success('Đã tạo banner mới 🖼️')
    }
    closeModal()
    fetchBanners()
  } catch (e) {
    toast.error(e.response?.data?.message || 'Lỗi khi lưu banner')
  } finally {
    savingBanner.value = false
  }
}
 
const toggleBanner = async (banner) => {
  try {
    await api.patch(`/admin/banners/${banner.id}/toggle`)
    banner.isActive = !banner.isActive
    toast.success(banner.isActive ? 'Đã hiển thị banner' : 'Đã ẩn banner')
  } catch (e) {
    toast.error('Có lỗi xảy ra')
  }
}
 
const deleteBanner = async (banner) => {
  if (!confirm(`Xóa banner "${banner.title || 'này'}"?`)) return
  try {
    await api.delete(`/admin/banners/${banner.id}`)
    banners.value = banners.value.filter(b => b.id !== banner.id)
    toast.success('Đã xóa banner')
  } catch (e) {
    toast.error('Không thể xóa banner')
  }
}
 
onMounted(fetchBanners)
</script>