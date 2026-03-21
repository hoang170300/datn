<template>
  <div>
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h5 class="fw-bold mb-1">Quản lý Flash Sale</h5>
        <p class="text-muted small mb-0">Tạo & quản lý các đợt giảm giá nhanh</p>
      </div>
      <button class="btn rounded-pill fw-semibold" style="background:#e91e8c;color:#fff;"
        @click="openModal()">
        <i class="bi bi-plus-lg me-2"></i>Tạo Flash Sale
      </button>
    </div>
 
    <!-- Status bar - Active sale countdown -->
    <div v-if="activeSale"
      class="alert border-0 mb-4 d-flex align-items-center justify-content-between flex-wrap gap-3"
      style="background:linear-gradient(135deg,#ff4757,#e91e8c);color:#fff;border-radius:16px;">
      <div>
        <div class="fw-bold fs-5">⚡ {{ activeSale.name }} đang diễn ra!</div>
        <small class="opacity-75">
          Kết thúc lúc {{ formatDateTime(activeSale.endTime) }}
          <span v-if="countdown" class="ms-2 fw-bold">— còn {{ countdown }}</span>
        </small>
      </div>
      <button class="btn btn-light btn-sm rounded-pill fw-semibold"
        @click="openModal(activeSale)">
        <i class="bi bi-pencil me-1"></i>Chỉnh sửa
      </button>
    </div>
 
    <!-- Flash Sales Table -->
    <div class="card border-0 shadow-sm" style="border-radius:16px;">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead style="background:#f8f9fa;">
              <tr>
                <th class="ps-4 py-3 small text-muted text-uppercase">Flash Sale</th>
                <th class="py-3 small text-muted text-uppercase">Bắt đầu</th>
                <th class="py-3 small text-muted text-uppercase">Kết thúc</th>
                <th class="py-3 small text-muted text-uppercase">Trạng thái</th>
                <th class="pe-4 py-3 small text-muted text-uppercase text-end">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="5" class="text-center py-5">
                  <div class="spinner-border" style="color:#e91e8c;"></div>
                </td>
              </tr>
              <tr v-else-if="!flashSales.length">
                <td colspan="5" class="text-center py-5 text-muted">
                  <i class="bi bi-lightning-charge fs-1 d-block mb-2"></i>
                  Chưa có flash sale nào
                </td>
              </tr>
              <tr v-for="fs in flashSales" :key="fs.id" v-else>
                <td class="ps-4">
                  <div class="fw-semibold">{{ fs.name }}</div>
                  <small class="text-muted">ID: {{ fs.id }}</small>
                </td>
                <td>
                  <div class="small">{{ formatDateTime(fs.startTime) }}</div>
                </td>
                <td>
                  <div class="small">{{ formatDateTime(fs.endTime) }}</div>
                </td>
                <td>
                  <span class="badge rounded-pill"
                    :class="getSaleStatusClass(fs)"
                    :style="getSaleStatusStyle(fs)">
                    {{ getSaleStatusLabel(fs) }}
                  </span>
                </td>
                <td class="pe-4 text-end">
                  <div class="d-flex gap-1 justify-content-end">
                    <button class="btn btn-sm btn-outline-primary rounded-pill"
                      @click="openModal(fs)">
                      <i class="bi bi-pencil me-1"></i>Sửa
                    </button>
                    <button class="btn btn-sm rounded-pill"
                      :class="fs.isActive ? 'btn-outline-warning' : 'btn-outline-success'"
                      @click="toggle(fs)">
                      {{ fs.isActive ? 'Tắt' : 'Bật' }}
                    </button>
                    <button class="btn btn-sm btn-outline-danger rounded-pill"
                      @click="deleteSale(fs)">
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
 
        <!-- Pagination -->
        <div v-if="pagination.totalPages > 1"
          class="d-flex justify-content-between align-items-center px-4 py-3 border-top">
          <small class="text-muted">Trang {{ currentPage + 1 }} / {{ pagination.totalPages }}</small>
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
 
    <!-- Modal tạo/sửa Flash Sale -->
    <Teleport to="body">
      <div v-if="showModal"
        class="position-fixed d-flex align-items-center justify-content-center"
        style="inset:0;background:rgba(0,0,0,0.5);z-index:1055;padding:16px;"
        @click.self="closeModal">
        <div class="card border-0 shadow-lg"
          style="width:100%;max-width:540px;border-radius:20px;max-height:90vh;overflow-y:auto;">
          <div class="card-body p-5">
            <div class="d-flex align-items-center justify-content-between mb-4">
              <div>
                <h5 class="fw-bold mb-0">
                  {{ editingSale ? 'Chỉnh sửa Flash Sale' : 'Tạo Flash Sale mới' }}
                </h5>
                <small class="text-muted">Cài đặt thời gian và trạng thái</small>
              </div>
              <button type="button" class="btn-close" @click="closeModal"></button>
            </div>
 
            <!-- Flash sale visual indicator -->
            <div class="p-3 rounded-3 mb-4 text-center"
              style="background:linear-gradient(135deg,#ff475722,#e91e8c22);border:2px dashed #e91e8c55;">
              <div style="font-size:2.5rem;">⚡</div>
              <div class="fw-bold" style="color:#e91e8c;">FLASH SALE</div>
              <small class="text-muted">Giảm giá nhanh - Số lượng có hạn</small>
            </div>
 
            <form @submit.prevent="saveSale">
              <div class="row g-3">
                <div class="col-12">
                  <label class="form-label small fw-semibold">
                    Tên Flash Sale <span class="text-danger">*</span>
                  </label>
                  <input type="text" class="form-control" v-model="modalForm.name"
                    placeholder="VD: Flash Sale Cuối Tuần - Giảm 30%" required>
                </div>
 
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">
                    Thời gian bắt đầu <span class="text-danger">*</span>
                  </label>
                  <input type="datetime-local" class="form-control"
                    v-model="modalForm.startTime" required>
                </div>
 
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">
                    Thời gian kết thúc <span class="text-danger">*</span>
                  </label>
                  <input type="datetime-local" class="form-control"
                    v-model="modalForm.endTime" required>
                </div>
 
                <!-- Duration preview -->
                <div v-if="modalForm.startTime && modalForm.endTime" class="col-12">
                  <div class="p-2 rounded-3 text-center small"
                    :class="durationValid ? 'bg-success bg-opacity-10 text-success' : 'bg-danger bg-opacity-10 text-danger'">
                    <i :class="['bi me-1', durationValid ? 'bi-clock-history' : 'bi-exclamation-circle']"></i>
                    <span v-if="durationValid">Thời lượng: {{ durationText }}</span>
                    <span v-else>Thời gian kết thúc phải sau thời gian bắt đầu!</span>
                  </div>
                </div>
 
                <div class="col-12">
                  <div class="p-3 rounded-3" style="background:#f8f9fa;">
                    <div class="form-check form-switch mb-0">
                      <input class="form-check-input" type="checkbox" v-model="modalForm.isActive"
                        id="saleActive" style="width:44px;height:24px;">
                      <label class="form-check-label fw-semibold ms-2" for="saleActive">
                        {{ modalForm.isActive ? '✅ Kích hoạt flash sale' : '❌ Tắt flash sale' }}
                      </label>
                    </div>
                    <small class="text-muted d-block mt-1 ms-5">
                      Flash sale chỉ hiển thị khi đang trong khoảng thời gian và được kích hoạt
                    </small>
                  </div>
                </div>
              </div>
 
              <div class="d-flex gap-2 mt-4">
                <button type="submit" class="btn flex-grow-1 py-2 fw-semibold"
                  style="background:#e91e8c;color:#fff;border-radius:12px;"
                  :disabled="savingModal || !durationValid">
                  <span v-if="savingModal" class="spinner-border spinner-border-sm me-2"></span>
                  {{ editingSale ? 'Lưu thay đổi' : 'Tạo Flash Sale' }}
                </button>
                <button type="button" class="btn btn-outline-secondary px-4" style="border-radius:12px;"
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
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useToast } from 'vue-toastification'
import api from '@/api/index'
 
const toast = useToast()
const flashSales = ref([])
const loading = ref(false)
const showModal = ref(false)
const editingSale = ref(null)
const savingModal = ref(false)
const currentPage = ref(0)
const pagination = ref({ totalPages: 0, last: true })
const countdown = ref('')
let countdownInterval = null
 
const defaultModal = () => ({
  name: '',
  startTime: '',
  endTime: '',
  isActive: true
})
const modalForm = reactive(defaultModal())
 
// Computed
const activeSale = computed(() =>
  flashSales.value.find(fs => {
    const now = new Date()
    return fs.isActive
      && fs.startTime && new Date(fs.startTime) <= now
      && fs.endTime && new Date(fs.endTime) >= now
  })
)
 
const durationValid = computed(() => {
  if (!modalForm.startTime || !modalForm.endTime) return true
  return new Date(modalForm.endTime) > new Date(modalForm.startTime)
})
 
const durationText = computed(() => {
  if (!modalForm.startTime || !modalForm.endTime) return ''
  const ms = new Date(modalForm.endTime) - new Date(modalForm.startTime)
  const h = Math.floor(ms / 3600000)
  const m = Math.floor((ms % 3600000) / 60000)
  if (h >= 24) return `${Math.floor(h / 24)} ngày ${h % 24}h`
  return `${h} giờ ${m} phút`
})
 
// Helpers
const formatDateTime = (d) => d
  ? new Date(d).toLocaleString('vi-VN', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    })
  : '—'
 
const getSaleStatusLabel = (fs) => {
  const now = new Date()
  if (!fs.isActive) return '● Đã tắt'
  if (fs.endTime && new Date(fs.endTime) < now) return '● Đã kết thúc'
  if (fs.startTime && new Date(fs.startTime) > now) return '● Sắp diễn ra'
  return '⚡ Đang diễn ra'
}
 
const getSaleStatusClass = (fs) => ''  // using style instead
const getSaleStatusStyle = (fs) => {
  const now = new Date()
  if (!fs.isActive) return 'background:#6c757d22;color:#6c757d'
  if (fs.endTime && new Date(fs.endTime) < now) return 'background:#dc354522;color:#dc3545'
  if (fs.startTime && new Date(fs.startTime) > now) return 'background:#ffc10722;color:#856404'
  return 'background:#ff475722;color:#ff4757;font-weight:700'
}
 
// Countdown timer for active sale
const updateCountdown = () => {
  if (!activeSale.value?.endTime) { countdown.value = ''; return }
  const diff = new Date(activeSale.value.endTime) - new Date()
  if (diff <= 0) { countdown.value = 'Đã kết thúc'; return }
  const h = Math.floor(diff / 3600000)
  const m = Math.floor((diff % 3600000) / 60000)
  const s = Math.floor((diff % 60000) / 1000)
  countdown.value = `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}
 
// API calls
const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/flash-sales', { params: { page: currentPage.value, size: 10 } })
    const data = res.data.data
    flashSales.value = data?.content || data || []
    pagination.value = { totalPages: data?.totalPages || 1, last: data?.last ?? true }
  } catch (e) {
    toast.error('Không thể tải danh sách flash sale')
  } finally {
    loading.value = false
  }
}
 
const openModal = (sale = null) => {
  editingSale.value = sale
  if (sale) {
    Object.assign(modalForm, {
      name: sale.name || '',
      startTime: sale.startTime ? sale.startTime.substring(0, 16) : '',
      endTime: sale.endTime ? sale.endTime.substring(0, 16) : '',
      isActive: sale.isActive ?? true
    })
  } else {
    // Default: start now, end in 24h
    const now = new Date()
    const end = new Date(now.getTime() + 24 * 3600 * 1000)
    const toLocal = (d) => new Date(d.getTime() - d.getTimezoneOffset() * 60000)
      .toISOString().substring(0, 16)
    Object.assign(modalForm, { ...defaultModal(), startTime: toLocal(now), endTime: toLocal(end) })
  }
  showModal.value = true
}
 
const closeModal = () => {
  showModal.value = false
  editingSale.value = null
}
 
const saveSale = async () => {
  if (!modalForm.name.trim()) { toast.error('Vui lòng nhập tên flash sale'); return }
  if (!durationValid.value) { toast.error('Thời gian không hợp lệ'); return }
  savingModal.value = true
  try {
    const payload = {
      name: modalForm.name.trim(),
      startTime: new Date(modalForm.startTime).toISOString(),
      endTime: new Date(modalForm.endTime).toISOString(),
      isActive: modalForm.isActive
    }
    if (editingSale.value) {
      await api.put(`/admin/flash-sales/${editingSale.value.id}`, payload)
      toast.success('Đã cập nhật flash sale')
    } else {
      await api.post('/admin/flash-sales', payload)
      toast.success('Đã tạo flash sale mới ⚡')
    }
    closeModal()
    fetchData()
  } catch (e) {
    toast.error(e.response?.data?.message || 'Có lỗi xảy ra')
  } finally {
    savingModal.value = false
  }
}
 
const toggle = async (fs) => {
  try {
    await api.patch(`/admin/flash-sales/${fs.id}/toggle`)
    fs.isActive = !fs.isActive
    toast.success(fs.isActive ? 'Đã kích hoạt flash sale ⚡' : 'Đã tắt flash sale')
  } catch (e) {
    toast.error('Có lỗi xảy ra')
  }
}
 
const deleteSale = async (fs) => {
  if (!confirm(`Xóa flash sale "${fs.name}"?`)) return
  try {
    await api.delete(`/admin/flash-sales/${fs.id}`)
    flashSales.value = flashSales.value.filter(f => f.id !== fs.id)
    toast.success('Đã xóa flash sale')
  } catch (e) {
    toast.error('Không thể xóa')
  }
}
 
const changePage = (page) => {
  if (page < 0 || page >= pagination.value.totalPages) return
  currentPage.value = page
  fetchData()
}
 
onMounted(() => {
  fetchData()
  countdownInterval = setInterval(updateCountdown, 1000)
})
 
onUnmounted(() => {
  if (countdownInterval) clearInterval(countdownInterval)
})
</script>
 
<style scoped>
.page-item.active .page-link { background: #e91e8c; border-color: #e91e8c; }
.page-link { color: #e91e8c; }
</style>