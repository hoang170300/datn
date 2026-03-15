<template>
  <div>
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h5 class="fw-bold mb-1">Quản lý Voucher</h5>
        <p class="text-muted small mb-0">Mã giảm giá & khuyến mãi</p>
      </div>
      <button class="btn rounded-pill" style="background:#e91e8c;color:#fff;"
        @click="openCreateModal">
        <i class="bi bi-plus-lg me-2"></i>Tạo voucher
      </button>
    </div>
 
    <div class="card border-0 shadow-sm" style="border-radius:16px;">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead style="background:#f8f9fa;">
              <tr>
                <th class="ps-4 py-3 small text-muted">Mã voucher</th>
                <th class="py-3 small text-muted">Loại giảm</th>
                <th class="py-3 small text-muted">Giá trị</th>
                <th class="py-3 small text-muted">Đơn tối thiểu</th>
                <th class="py-3 small text-muted">Đã dùng / Giới hạn</th>
                <th class="py-3 small text-muted">Hạn sử dụng</th>
                <th class="py-3 small text-muted">Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="7" class="text-center py-5">
                  <div class="spinner-border text-secondary"></div>
                </td>
              </tr>
              <tr v-for="v in vouchers" :key="v.id" v-else>
                <td class="ps-4">
                  <span class="badge bg-dark px-3 py-2 rounded-pill" style="font-size:0.85rem;letter-spacing:1px;">
                    {{ v.code }}
                  </span>
                </td>
                <td>
                  <span class="badge rounded-pill"
                    :class="v.discountType === 'PERCENTAGE' ? 'bg-info text-dark' : 'bg-warning text-dark'">
                    {{ v.discountType === 'PERCENTAGE' ? '% Phần trăm' : '💰 Cố định' }}
                  </span>
                </td>
                <td class="fw-bold small" style="color:#e91e8c;">
                  {{ v.discountType === 'PERCENTAGE' ? v.discountValue + '%' : formatPrice(v.discountValue) }}
                </td>
                <td class="small">{{ formatPrice(v.minOrderValue) }}</td>
                <td>
                  <div class="d-flex align-items-center gap-2">
                    <div class="progress flex-grow-1" style="height:6px;min-width:60px;">
                      <div class="progress-bar" style="background:#e91e8c;"
                        :style="`width:${v.usageLimit ? Math.min(100, (v.usedCount / v.usageLimit) * 100) : 0}%`">
                      </div>
                    </div>
                    <small class="text-muted text-nowrap">{{ v.usedCount }} / {{ v.usageLimit || '∞' }}</small>
                  </div>
                </td>
                <td class="small text-muted">
                  {{ v.endDate ? new Date(v.endDate).toLocaleDateString('vi-VN') : 'Không giới hạn' }}
                </td>
                <td>
                  <div class="form-check form-switch mb-0">
                    <input class="form-check-input" type="checkbox"
                      :checked="v.isActive" @change="toggleVoucher(v)">
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
 
    <!-- Create Voucher Modal -->
    <div v-if="showModal" class="modal-overlay d-flex align-items-center justify-content-center"
      style="position:fixed;inset:0;background:rgba(0,0,0,0.5);z-index:1050;"
      @click.self="showModal = false">
      <div class="card border-0 shadow-lg" style="border-radius:20px;width:100%;max-width:500px;margin:16px;">
        <div class="card-body p-5">
          <h5 class="fw-bold mb-4">Tạo voucher mới</h5>
          <form @submit.prevent="createVoucher">
            <div class="row g-3">
              <div class="col-12">
                <label class="form-label small fw-semibold">Mã voucher *</label>
                <input type="text" class="form-control text-uppercase" v-model="form.code"
                  placeholder="VD: SUMMER20" required style="letter-spacing:1px;">
              </div>
              <div class="col-12">
                <label class="form-label small fw-semibold">Mô tả</label>
                <input type="text" class="form-control" v-model="form.description" placeholder="Mô tả voucher">
              </div>
              <div class="col-md-6">
                <label class="form-label small fw-semibold">Loại giảm giá</label>
                <select class="form-select" v-model="form.discountType">
                  <option value="PERCENTAGE">Phần trăm (%)</option>
                  <option value="FIXED_AMOUNT">Số tiền cố định</option>
                </select>
              </div>
              <div class="col-md-6">
                <label class="form-label small fw-semibold">
                  Giá trị {{ form.discountType === 'PERCENTAGE' ? '(%)' : '(VNĐ)' }}
                </label>
                <input type="number" class="form-control" v-model="form.discountValue"
                  :max="form.discountType === 'PERCENTAGE' ? 100 : undefined" required>
              </div>
              <div class="col-md-6">
                <label class="form-label small fw-semibold">Đơn hàng tối thiểu (VNĐ)</label>
                <input type="number" class="form-control" v-model="form.minOrderValue" placeholder="0">
              </div>
              <div class="col-md-6">
                <label class="form-label small fw-semibold">Giảm tối đa (VNĐ)</label>
                <input type="number" class="form-control" v-model="form.maxDiscountAmount"
                  placeholder="Để trống = không giới hạn">
              </div>
              <div class="col-md-6">
                <label class="form-label small fw-semibold">Giới hạn lượt dùng</label>
                <input type="number" class="form-control" v-model="form.usageLimit"
                  placeholder="Để trống = không giới hạn">
              </div>
              <div class="col-md-6">
                <label class="form-label small fw-semibold">Ngày hết hạn</label>
                <input type="date" class="form-control" v-model="form.endDate">
              </div>
            </div>
 
            <div class="d-flex gap-2 mt-4">
              <button type="submit" class="btn flex-grow-1"
                style="background:#e91e8c;color:#fff;border-radius:10px;"
                :disabled="savingVoucher">
                <span v-if="savingVoucher" class="spinner-border spinner-border-sm me-2"></span>
                Tạo voucher
              </button>
              <button type="button" class="btn btn-outline-secondary"
                style="border-radius:10px;" @click="showModal = false">
                Hủy
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>
 
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useToast } from 'vue-toastification'
import api from '@/api/index'
 
const toast = useToast()
const vouchers = ref([])
const loading = ref(false)
const showModal = ref(false)
const savingVoucher = ref(false)
 
const form = reactive({
  code: '', description: '', discountType: 'PERCENTAGE', discountValue: 10,
  minOrderValue: 0, maxDiscountAmount: null, usageLimit: null, endDate: null
})
 
const formatPrice = (p) => p ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p) : '0 ₫'
 
const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/vouchers')
    vouchers.value = res.data.data?.content || res.data.data || []
  } finally { loading.value = false }
}
 
const toggleVoucher = async (v) => {
  try {
    await api.patch(`/admin/vouchers/${v.id}/toggle`)
    v.isActive = !v.isActive
    toast.success(v.isActive ? 'Đã kích hoạt voucher' : 'Đã vô hiệu hóa voucher')
  } catch (e) { toast.error('Có lỗi xảy ra') }
}
 
const openCreateModal = () => {
  Object.assign(form, { code: '', description: '', discountType: 'PERCENTAGE', discountValue: 10, minOrderValue: 0, maxDiscountAmount: null, usageLimit: null, endDate: null })
  showModal.value = true
}
 
const createVoucher = async () => {
  savingVoucher.value = true
  try {
    const payload = {
      ...form,
      code: form.code.toUpperCase(),
      endDate: form.endDate ? form.endDate + 'T23:59:59' : null,
      startDate: new Date().toISOString()
    }
    await api.post('/admin/vouchers', payload)
    toast.success('Tạo voucher thành công!')
    showModal.value = false
    fetchData()
  } catch (e) {
    toast.error(e.response?.data?.message || 'Tạo voucher thất bại')
  } finally { savingVoucher.value = false }
}
 
onMounted(fetchData)
</script>
 