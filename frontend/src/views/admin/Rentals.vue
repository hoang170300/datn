<template>
    <div>
      <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
          <h5 class="fw-bold mb-1">📅 Quản lý thuê đồ</h5>
          <p class="text-muted small mb-0">Theo dõi tình trạng thuê và nhắc trả đồ</p>
        </div>
      </div>
  
      <!-- Stats -->
      <div class="row g-3 mb-4">
        <div class="col-4">
          <div class="card border-0 shadow-sm text-center p-3" style="border-radius:12px;">
            <div class="fs-2">📦</div>
            <div class="fw-bold fs-4 text-primary">{{ activeRentals.length }}</div>
            <div class="small text-muted">Đang thuê</div>
          </div>
        </div>
        <div class="col-4">
          <div class="card border-0 shadow-sm text-center p-3" style="border-radius:12px;border:2px solid #ffc107!important;">
            <div class="fs-2">⏰</div>
            <div class="fw-bold fs-4 text-warning">{{ dueSoonRentals.length }}</div>
            <div class="small text-muted">Sắp hết hạn (≤2 ngày)</div>
          </div>
        </div>
        <div class="col-4">
          <div class="card border-0 shadow-sm text-center p-3" style="border-radius:12px;border:2px solid #dc3545!important;">
            <div class="fs-2">🚨</div>
            <div class="fw-bold fs-4 text-danger">{{ overdueRentals.length }}</div>
            <div class="small text-muted">Quá hạn</div>
          </div>
        </div>
      </div>
  
      <!-- Tabs -->
      <div class="card border-0 shadow-sm mb-4" style="border-radius:12px;">
        <div class="card-body p-2">
          <div class="d-flex gap-1">
            <button :class="['btn btn-sm rounded-pill px-3', tab==='active'?'text-white':'btn-outline-secondary']"
              :style="tab==='active'?'background:#007bff':''" @click="tab='active'">
              Đang thuê ({{ activeRentals.length }})
            </button>
            <button :class="['btn btn-sm rounded-pill px-3', tab==='overdue'?'text-white':'btn-outline-secondary']"
              :style="tab==='overdue'?'background:#dc3545':''" @click="tab='overdue'">
              Quá hạn ({{ overdueRentals.length }})
            </button>
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
                  <th class="ps-4 py-3 small text-muted">Sản phẩm</th>
                  <th class="py-3 small text-muted">Khách hàng</th>
                  <th class="py-3 small text-muted">Ngày thuê</th>
                  <th class="py-3 small text-muted">Ngày trả</th>
                  <th class="py-3 small text-muted">Còn lại</th>
                  <th class="py-3 small text-muted">Tiền cọc</th>
                  <th class="pe-4 py-3 small text-muted text-center">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="loading">
                  <td colspan="7" class="text-center py-5">
                    <div class="spinner-border" style="color:#e91e8c;"></div>
                  </td>
                </tr>
                <tr v-else-if="!currentList.length">
                  <td colspan="7" class="text-center py-5 text-muted">
                    <i class="bi bi-calendar-check fs-1 d-block mb-2"></i>
                    Không có đơn thuê nào
                  </td>
                </tr>
                <tr v-for="item in currentList" :key="item.itemId" v-else
                  :class="item.daysLeft<0?'table-danger':item.daysLeft<=2?'table-warning':''">
                  <td class="ps-4">
                    <div class="fw-semibold small">{{ item.productName }}</div>
                    <small class="text-muted">Size: {{ item.size }} · x{{ item.quantity }}</small>
                    <small class="text-muted d-block">Đơn: #{{ item.orderNumber }}</small>
                  </td>
                  <td>
                    <div class="small" v-if="item.phone">
                      <i class="bi bi-telephone me-1"></i>{{ item.phone }}
                    </div>
                    <small class="text-muted">{{ item.note?.split('KH:')[1]?.split('|')[0]?.trim() || '—' }}</small>
                  </td>
                  <td class="small">{{ item.rentalStartDate }}</td>
                  <td class="small">{{ item.rentalEndDate }}</td>
                  <td>
                    <span class="badge rounded-pill"
                      :class="item.daysLeft<0?'bg-danger':item.daysLeft<=2?'bg-warning text-dark':'bg-success'">
                      {{ item.daysLeft<0
                          ? `Quá hạn ${Math.abs(item.daysLeft)} ngày`
                          : item.daysLeft===0
                            ? 'Hôm nay trả'
                            : `Còn ${item.daysLeft} ngày` }}
                    </span>
                  </td>
                  <td class="small fw-semibold text-warning">
                    {{ formatPrice(item.depositAmount) }}
                  </td>
                  <td class="pe-4 text-center">
                    <div class="d-flex gap-1 justify-content-center">
                      <button class="btn btn-sm btn-success rounded-pill"
                        style="font-size:0.75rem;"
                        @click="openReturn(item, false)">
                        <i class="bi bi-check-circle me-1"></i>Trả đồ OK
                      </button>
                      <button class="btn btn-sm btn-danger rounded-pill"
                        style="font-size:0.75rem;"
                        @click="openReturn(item, true)">
                        <i class="bi bi-exclamation-triangle me-1"></i>Hư hỏng
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
  
      <!-- Return confirm modal -->
      <Teleport to="body">
        <div v-if="returnModal.show"
          class="position-fixed d-flex align-items-center justify-content-center"
          style="inset:0;background:rgba(0,0,0,0.5);z-index:2000;padding:16px;"
          @click.self="returnModal.show=false">
          <div class="card border-0 shadow-lg" style="width:100%;max-width:420px;border-radius:20px;">
            <div class="card-body p-4 text-center">
              <div style="font-size:3rem;" class="mb-3">{{ returnModal.damaged?'⚠️':'✅' }}</div>
              <h5 class="fw-bold mb-2">
                {{ returnModal.damaged ? 'Xác nhận đồ bị hư hỏng?' : 'Xác nhận trả đồ nguyên vẹn?' }}
              </h5>
              <p class="text-muted mb-3">{{ returnModal.damaged
                ? 'Tiền cọc sẽ KHÔNG được hoàn lại. Đồ hư hỏng cần báo cáo thêm.'
                : 'Tiền cọc sẽ được hoàn lại cho khách. Số lượng thuê sẽ được cập nhật.' }}</p>
  
              <textarea class="form-control mb-3" rows="2"
                v-model="returnModal.note" placeholder="Ghi chú thêm (nếu có)..."></textarea>
  
              <div class="d-flex gap-2 justify-content-center">
                <button class="btn px-4 rounded-pill text-white fw-semibold"
                  :class="returnModal.damaged?'btn-danger':'btn-success'"
                  @click="confirmReturn" :disabled="returnModal.loading">
                  <span v-if="returnModal.loading" class="spinner-border spinner-border-sm me-2"></span>
                  Xác nhận
                </button>
                <button class="btn btn-outline-secondary px-4 rounded-pill"
                  @click="returnModal.show=false">Hủy</button>
              </div>
            </div>
          </div>
        </div>
      </Teleport>
    </div>
  </template>
  
  <script setup>
  import { ref, computed, reactive, onMounted } from 'vue'
  import api from '@/api/index'
  import { useToast } from 'vue-toastification'
  
  const toast = useToast()
  const loading = ref(false)
  const tab = ref('active')
  const activeRentals = ref([])
  const overdueRentals = ref([])
  
  const returnModal = reactive({ show:false, item:null, damaged:false, note:'', loading:false })
  
  const dueSoonRentals = computed(() =>
    activeRentals.value.filter(i => i.daysLeft >= 0 && i.daysLeft <= 2))
  
  const currentList = computed(() =>
    tab.value === 'overdue' ? overdueRentals.value : activeRentals.value)
  
  const formatPrice = (p) => p!=null
    ? new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(p) : '—'
  
  const fetchData = async () => {
    loading.value = true
    try {
      const [a, o] = await Promise.all([
        api.get('/admin/pos/rentals/active'),
        api.get('/admin/pos/rentals/overdue')
      ])
      activeRentals.value = a.data.data || []
      overdueRentals.value = o.data.data || []
    } finally { loading.value = false }
  }
  
  const openReturn = (item, damaged) => {
    Object.assign(returnModal, { show:true, item, damaged, note:'', loading:false })
  }
  
  const confirmReturn = async () => {
    returnModal.loading = true
    try {
      await api.patch(`/admin/pos/orders/${returnModal.item.orderId}/return`, null, {
        params: { damaged: returnModal.damaged, note: returnModal.note }
      })
      toast.success(returnModal.damaged
        ? '⚠️ Đã ghi nhận đồ hư hỏng'
        : '✅ Trả đồ thành công! Đã hoàn tiền cọc')
      returnModal.show = false
      await fetchData()
    } catch (e) {
      toast.error('Lỗi xác nhận')
    } finally { returnModal.loading = false }
  }
  
  onMounted(fetchData)
  </script>
  