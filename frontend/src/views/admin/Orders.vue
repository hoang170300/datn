<template>
  <div>
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h5 class="fw-bold mb-1">Quản lý đơn hàng</h5>
        <p class="text-muted small mb-0">{{ pagination.totalElements }} đơn hàng</p>
      </div>
    </div>
 
    <!-- Status tabs -->
    <div class="card border-0 shadow-sm mb-4" style="border-radius:12px;">
      <div class="card-body p-2">
        <div class="d-flex gap-1 flex-wrap">
          <button v-for="s in statusFilters" :key="s.value"
            :class="['btn btn-sm rounded-pill px-3', activeStatus===s.value ? 'text-white' : 'btn-outline-secondary']"
            :style="activeStatus===s.value ? `background:${s.color}` : ''"
            @click="filterByStatus(s.value)">
            {{ s.label }}
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
                <th class="ps-4 py-3 small text-muted">Mã đơn</th>
                <th class="py-3 small text-muted">Khách hàng</th>
                <th class="py-3 small text-muted">Thời gian</th>
                <th class="py-3 small text-muted">Tổng tiền</th>
                <th class="py-3 small text-muted">Thanh toán</th>
                <th class="py-3 small text-muted">Trạng thái</th>
                <th class="pe-4 py-3 small text-muted text-center">Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="7" class="text-center py-5">
                  <div class="spinner-border" style="color:#e91e8c;"></div>
                </td>
              </tr>
              <tr v-else-if="!orders.length">
                <td colspan="7" class="text-center py-5 text-muted">
                  <i class="bi bi-inbox fs-1 d-block mb-2"></i>Không có đơn hàng
                </td>
              </tr>
              <tr v-for="order in orders" :key="order.id" v-else>
                <td class="ps-4">
                  <div class="fw-semibold small">#{{ order.orderNumber }}</div>
                  <!-- Danh sách sản phẩm đầy đủ -->
                  <div class="mt-1 d-flex flex-column gap-1">
                    <div v-for="item in (order.items||[])" :key="item.id"
                      class="p-1 rounded-2" style="background:#f8f9fa;font-size:0.75rem;">
                      <!-- Tên + loại -->
                      <div class="d-flex align-items-center gap-1 flex-wrap">
                        <span class="fw-semibold text-dark">{{ item.productName }}</span>
                        <span class="badge py-0 px-1"
                          :class="item.itemType==='RENTAL' ? 'bg-info text-dark' : 'bg-secondary'"
                          style="font-size:0.6rem;">
                          {{ item.itemType==='RENTAL' ? '📅 Thuê' : '🛍️ Mua' }}
                        </span>
                      </div>
                      <!-- Size, màu, số lượng -->
                      <div class="text-muted mt-1 d-flex gap-2 flex-wrap">
                        <span v-if="item.size">
                          <i class="bi bi-rulers me-1"></i>Size: <strong class="text-dark">{{ item.size }}</strong>
                        </span>
                        <span v-if="item.color">
                          <i class="bi bi-palette me-1"></i>Màu: <strong class="text-dark">{{ item.color }}</strong>
                        </span>
                        <span>SL: <strong class="text-dark">{{ item.quantity }}</strong></span>
                        <span>Giá: <strong style="color:#e91e8c;">{{ formatPrice(item.unitPrice) }}</strong></span>
                      </div>
                      <!-- Ngày thuê -->
                      <div v-if="item.itemType==='RENTAL' && item.rentalStartDate"
                        class="text-info mt-1" style="font-size:0.7rem;">
                        <i class="bi bi-calendar-range me-1"></i>
                        {{ item.rentalStartDate }} → {{ item.rentalEndDate }}
                        ({{ item.rentalDays }} ngày)
                      </div>
                    </div>
                  </div>
                </td>
                <td>
                  <div class="small fw-semibold">{{ order.userName||'—' }}</div>
                  <small class="text-muted">{{ order.phone }}</small>
                </td>
                <td class="small text-muted">{{ formatDate(order.createdAt) }}</td>
                <td class="fw-bold small" style="color:#e91e8c;">{{ formatPrice(order.finalPrice) }}</td>
                <td>
                  <span class="badge rounded-pill small"
                    :class="order.paymentStatus==='PAID'
                      ? 'bg-success'
                      : order.paymentStatus==='PENDING' ? 'bg-warning text-dark' : 'bg-danger'">
                    {{ paymentLabel(order.paymentStatus) }}
                  </span>
                  <div class="small text-muted mt-1" style="font-size:0.7rem;">{{ order.paymentMethod }}</div>
                </td>
                <td>
                  <span class="badge rounded-pill small"
                    :style="`background:${statusColor(order.status)}22;color:${statusColor(order.status)};font-weight:600`">
                    {{ statusLabel(order.status) }}
                  </span>
                </td>
                <!-- Nút hành động theo trạng thái -->
                <td class="pe-4 text-center">
                  <div class="d-flex gap-1 justify-content-center flex-wrap">
 
                    <!-- PENDING → Xác nhận thanh toán -->
                    <button v-if="order.status==='PENDING'"
                      class="btn btn-sm rounded-pill text-white"
                      style="background:#17a2b8;font-size:0.75rem;"
                      @click="doAction(order,'CONFIRMED')">
                      <i class="bi bi-check-circle me-1"></i>Xác nhận TT
                    </button>
 
                    <!-- CONFIRMED → Chuẩn bị hàng -->
                    <button v-if="order.status==='CONFIRMED'"
                      class="btn btn-sm rounded-pill text-white"
                      style="background:#6f42c1;font-size:0.75rem;"
                      @click="doAction(order,'PROCESSING')">
                      <i class="bi bi-box-seam me-1"></i>Chuẩn bị hàng
                    </button>
 
                    <!-- PROCESSING → Giao hàng -->
                    <button v-if="order.status==='PROCESSING'"
                      class="btn btn-sm rounded-pill text-white"
                      style="background:#007bff;font-size:0.75rem;"
                      @click="doAction(order,'SHIPPING')">
                      <i class="bi bi-truck me-1"></i>Giao hàng
                    </button>
 
                    <!-- SHIPPING → Hoàn thành (admin mark nếu user chưa confirm) -->
                    <button v-if="order.status==='SHIPPING'"
                      class="btn btn-sm rounded-pill text-white"
                      style="background:#28a745;font-size:0.75rem;"
                      @click="doAction(order,'DELIVERED')">
                      <i class="bi bi-check2-all me-1"></i>Hoàn thành
                    </button>
 
                    <!-- Hủy đơn (chỉ khi PENDING hoặc CONFIRMED) -->
                    <button v-if="['PENDING','CONFIRMED'].includes(order.status)"
                      class="btn btn-sm btn-outline-danger rounded-pill"
                      style="font-size:0.75rem;"
                      @click="doAction(order,'CANCELLED')">
                      <i class="bi bi-x-circle me-1"></i>Hủy
                    </button>
 
                    <!-- Đã giao / Đã hủy — chỉ hiện badge -->
                    <span v-if="['DELIVERED','CANCELLED','RETURNED'].includes(order.status)"
                      class="text-muted small">—</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
 
        <!-- Pagination -->
        <div v-if="pagination.totalPages>1"
          class="d-flex justify-content-between align-items-center px-4 py-3 border-top">
          <small class="text-muted">Trang {{ currentPage+1 }} / {{ pagination.totalPages }}</small>
          <nav>
            <ul class="pagination pagination-sm mb-0">
              <li :class="['page-item',{disabled:currentPage===0}]">
                <button class="page-link" @click="changePage(currentPage-1)">
                  <i class="bi bi-chevron-left"></i>
                </button>
              </li>
              <li v-for="p in Math.min(pagination.totalPages,5)" :key="p"
                :class="['page-item',{active:currentPage===p-1}]">
                <button class="page-link" @click="changePage(p-1)">{{ p }}</button>
              </li>
              <li :class="['page-item',{disabled:pagination.last}]">
                <button class="page-link" @click="changePage(currentPage+1)">
                  <i class="bi bi-chevron-right"></i>
                </button>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>
 
    <!-- Confirm modal -->
    <Teleport to="body">
      <div v-if="confirmModal.show"
        class="position-fixed d-flex align-items-center justify-content-center"
        style="inset:0;background:rgba(0,0,0,0.5);z-index:2000;padding:16px;"
        @click.self="confirmModal.show=false">
        <div class="card border-0 shadow-lg" style="width:100%;max-width:420px;border-radius:20px;">
          <div class="card-body p-4 text-center">
            <div style="font-size:3rem;" class="mb-3">{{ confirmModal.icon }}</div>
            <h5 class="fw-bold mb-2">{{ confirmModal.title }}</h5>
            <p class="text-muted mb-4">{{ confirmModal.desc }}</p>
            <div class="d-flex gap-2 justify-content-center">
              <button class="btn px-4 rounded-pill text-white fw-semibold"
                :style="`background:${confirmModal.color}`"
                @click="executeAction" :disabled="confirmModal.loading">
                <span v-if="confirmModal.loading" class="spinner-border spinner-border-sm me-2"></span>
                Xác nhận
              </button>
              <button class="btn btn-outline-secondary px-4 rounded-pill"
                @click="confirmModal.show=false">Hủy</button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
 
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { orderApi } from '@/api/index'
import { useToast } from 'vue-toastification'
 
const toast = useToast()
const orders = ref([])
const loading = ref(false)
const activeStatus = ref('')
const currentPage = ref(0)
const pagination = ref({ totalPages:0, totalElements:0, last:true })
 
const confirmModal = reactive({
  show: false, loading: false,
  order: null, targetStatus: '',
  icon: '', title: '', desc: '', color: ''
})
 
const statusFilters = [
  { label:'Tất cả',         value:'',            color:'#6c757d' },
  { label:'Chờ xác nhận',   value:'PENDING',     color:'#ffc107' },
  { label:'Đã xác nhận',    value:'CONFIRMED',   color:'#17a2b8' },
  { label:'Đang xử lý',     value:'PROCESSING',  color:'#6f42c1' },
  { label:'Đang giao',      value:'SHIPPING',    color:'#007bff' },
  { label:'Đã giao',        value:'DELIVERED',   color:'#28a745' },
  { label:'Đã hủy',         value:'CANCELLED',   color:'#dc3545' },
  { label:'Hoàn trả',       value:'RETURNED',    color:'#fd7e14' }
]
 
const actionConfig = {
  CONFIRMED:  { icon:'✅', title:'Xác nhận thanh toán?',  desc:'Đơn hàng sẽ chuyển sang "Đã xác nhận". Xác nhận khi đã nhận được tiền từ khách.',  color:'#17a2b8' },
  PROCESSING: { icon:'📦', title:'Chuẩn bị hàng?',       desc:'Đơn hàng sẽ chuyển sang "Đang xử lý". Bắt đầu đóng gói và chuẩn bị giao.',          color:'#6f42c1' },
  SHIPPING:   { icon:'🚚', title:'Xác nhận giao hàng?',  desc:'Đơn hàng sẽ chuyển sang "Đang giao". Khách hàng sẽ nhận được thông báo.',             color:'#007bff' },
  DELIVERED:  { icon:'🎉', title:'Hoàn thành đơn hàng?', desc:'Đánh dấu đơn đã giao thành công. Dùng khi khách chưa tự xác nhận.',                   color:'#28a745' },
  CANCELLED:  { icon:'❌', title:'Hủy đơn hàng?',        desc:'Hành động này không thể hoàn tác. Hàng sẽ được hoàn lại kho.',                         color:'#dc3545' }
}
 
const statusLabel  = (s) => ({ PENDING:'Chờ xác nhận',CONFIRMED:'Đã xác nhận',PROCESSING:'Đang xử lý',SHIPPING:'Đang giao',DELIVERED:'Đã giao',CANCELLED:'Đã hủy',RETURNED:'Hoàn trả' }[s]||s)
const statusColor  = (s) => ({ PENDING:'#ffc107',CONFIRMED:'#17a2b8',PROCESSING:'#6f42c1',SHIPPING:'#007bff',DELIVERED:'#28a745',CANCELLED:'#dc3545',RETURNED:'#fd7e14' }[s]||'#6c757d')
const paymentLabel = (s) => ({ PENDING:'Chưa TT',PAID:'Đã TT',FAILED:'Thất bại',REFUNDED:'Hoàn tiền' }[s]||s)
const formatPrice  = (p) => p!=null ? new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(p) : '—'
const formatDate   = (d) => d ? new Date(d).toLocaleDateString('vi-VN',{day:'2-digit',month:'2-digit',year:'numeric',hour:'2-digit',minute:'2-digit'}) : '—'
 
const fetchData = async () => {
  loading.value = true
  try {
    const res = await orderApi.admin.getOrders({
      status: activeStatus.value||null, page: currentPage.value, size:15
    })
    const data = res.data.data
    orders.value = data.content
    pagination.value = { totalPages:data.totalPages, totalElements:data.totalElements, last:data.last }
  } catch (e) { toast.error('Lỗi tải đơn hàng') }
  finally { loading.value = false }
}
 
const filterByStatus = (status) => {
  activeStatus.value = status
  currentPage.value = 0
  fetchData()
}
 
const doAction = (order, targetStatus) => {
  const cfg = actionConfig[targetStatus]
  Object.assign(confirmModal, {
    show: true, loading: false,
    order, targetStatus,
    ...cfg
  })
}
 
const executeAction = async () => {
  confirmModal.loading = true
  try {
    await orderApi.admin.updateStatus(confirmModal.order.id, confirmModal.targetStatus)
    confirmModal.order.status = confirmModal.targetStatus
    if (confirmModal.targetStatus === 'DELIVERED') {
      confirmModal.order.paymentStatus = 'PAID'
    }
    toast.success(`✅ ${actionConfig[confirmModal.targetStatus].title.replace('?','')} thành công!`)
    confirmModal.show = false
    fetchData()
  } catch (e) {
    toast.error('Cập nhật thất bại')
  } finally {
    confirmModal.loading = false
  }
}
 
const changePage = (page) => {
  if (page<0||page>=pagination.value.totalPages) return
  currentPage.value = page
  fetchData()
}
 
onMounted(fetchData)
</script>
 
<style scoped>
.page-item.active .page-link { background:#e91e8c;border-color:#e91e8c; }
.page-link { color:#e91e8c; }
</style>