<template>
  <div>
    <div class="card border-0 shadow-sm" style="border-radius:16px;">
      <div class="card-body p-4">
        <h5 class="fw-bold mb-4"><i class="bi bi-bag-check me-2"></i>Đơn hàng của tôi</h5>
 
        <!-- Status tabs -->
        <div class="d-flex gap-2 mb-4 flex-wrap">
          <button v-for="s in statusTabs" :key="s.value"
            :class="['btn btn-sm rounded-pill', activeTab===s.value ? 'text-white' : 'btn-outline-secondary']"
            :style="activeTab===s.value ? 'background:#e91e8c' : ''"
            @click="filterStatus(s.value)">
            {{ s.label }}
          </button>
        </div>
 
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border" style="color:#e91e8c;"></div>
        </div>
 
        <div v-else-if="!orders.length" class="text-center py-5">
          <div style="font-size:4rem;">📦</div>
          <h6 class="text-muted mt-3">Chưa có đơn hàng nào</h6>
          <router-link to="/products" class="btn rounded-pill mt-2"
            style="background:#e91e8c;color:#fff;">Mua sắm ngay</router-link>
        </div>
 
        <div v-else class="d-flex flex-column gap-3">
          <div v-for="order in orders" :key="order.id"
            class="border rounded-3 p-4"
            style="transition:box-shadow 0.2s;"
            @mouseenter="$event.currentTarget.style.boxShadow='0 4px 20px rgba(233,30,140,0.1)'"
            @mouseleave="$event.currentTarget.style.boxShadow='none'">
 
            <!-- Header -->
            <div class="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-3">
              <div>
                <span class="fw-bold">#{{ order.orderNumber }}</span>
                <small class="text-muted ms-2">{{ formatDate(order.createdAt) }}</small>
              </div>
              <div class="d-flex gap-2 flex-wrap align-items-center">
                <span class="badge rounded-pill"
                  :style="`background:${statusColor(order.status)}22;color:${statusColor(order.status)};font-weight:600`">
                  {{ statusLabel(order.status) }}
                </span>
                <span class="badge"
                  :class="order.paymentStatus==='PAID' ? 'bg-success' : 'bg-warning text-dark'">
                  {{ paymentLabel(order.paymentStatus) }}
                </span>
              </div>
            </div>
 
            <!-- Progress bar -->
            <div class="mb-3">
              <div class="d-flex justify-content-between align-items-center position-relative"
                style="padding:0 12px;">
                <!-- Progress line -->
                <div class="position-absolute" style="top:14px;left:28px;right:28px;height:2px;background:#e9ecef;z-index:0;"></div>
                <div class="position-absolute" style="top:14px;left:28px;height:2px;background:#e91e8c;z-index:1;"
                  :style="`width:${progressWidth(order.status)}%`"></div>
 
                <div v-for="(step,i) in orderSteps" :key="i"
                  class="d-flex flex-column align-items-center position-relative"
                  style="z-index:2;min-width:60px;">
                  <div class="rounded-circle d-flex align-items-center justify-content-center mb-1"
                    style="width:28px;height:28px;font-size:0.75rem;transition:all 0.3s;"
                    :style="stepDone(order.status, step.status)
                      ? 'background:#e91e8c;color:#fff;border:2px solid #e91e8c'
                      : 'background:#fff;color:#aaa;border:2px solid #dee2e6'">
                    <i :class="['bi', step.icon]"></i>
                  </div>
                  <div class="text-center" style="font-size:0.6rem;color:#999;max-width:50px;line-height:1.2;">
                    {{ step.label }}
                  </div>
                </div>
              </div>
            </div>
 
            <!-- Info banner theo trạng thái -->
            <div v-if="statusInfo(order)" class="mb-3 p-2 rounded-3 small d-flex align-items-center gap-2"
              :style="`background:${statusInfo(order).bg};color:${statusInfo(order).color}`">
              <i :class="['bi', statusInfo(order).icon]"></i>
              {{ statusInfo(order).text }}
            </div>
 
            <!-- Items -->
            <div class="d-flex gap-2 mb-3 flex-wrap">
              <div v-for="item in (order.items||[]).slice(0,3)" :key="item.id"
                class="d-flex align-items-center gap-2 p-2 rounded-3" style="background:#f8f9fa;">
                <div>
                  <div class="small fw-semibold">{{ item.productName }}</div>
                  <small class="text-muted">
                    x{{ item.quantity }} ·
                    {{ item.itemType==='RENTAL' ? '📅 Thuê' : '🛍️ Mua' }}
                  </small>
                </div>
              </div>
              <div v-if="(order.items||[]).length>3" class="d-flex align-items-center">
                <small class="text-muted">+{{ order.items.length-3 }} sản phẩm khác</small>
              </div>
            </div>
 
            <!-- Footer -->
            <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
              <div>
                <small class="text-muted">Tổng thanh toán: </small>
                <span class="fw-bold" style="color:#e91e8c;">{{ formatPrice(order.finalPrice) }}</span>
              </div>
              <div class="d-flex gap-2 flex-wrap">
                <router-link :to="`/profile/orders/${order.id}`"
                  class="btn btn-sm btn-outline-secondary rounded-pill">
                  Xem chi tiết
                </router-link>
                <!-- Xác nhận đã nhận hàng -->
                <button v-if="order.status==='SHIPPING'"
                  class="btn btn-sm rounded-pill text-white fw-semibold"
                  style="background:#28a745;"
                  @click="confirmReceived(order)">
                  <i class="bi bi-check-circle me-1"></i>Đã nhận hàng
                </button>
                <!-- Huỷ đơn (chỉ khi PENDING) -->
                <button v-if="order.status==='PENDING'"
                  class="btn btn-sm btn-outline-danger rounded-pill"
                  @click="openCancelModal(order)">
                  <i class="bi bi-x-circle me-1"></i>Huỷ đơn
                </button>
              </div>
            </div>
          </div>
        </div>
 
        <!-- Pagination -->
        <nav v-if="pagination.totalPages>1" class="mt-4 d-flex justify-content-center">
          <ul class="pagination pagination-sm">
            <li :class="['page-item',{disabled:currentPage===0}]">
              <button class="page-link" @click="changePage(currentPage-1)">
                <i class="bi bi-chevron-left"></i>
              </button>
            </li>
            <li v-for="p in pagination.totalPages" :key="p"
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
 
  <!-- Cancel modal -->
  <Teleport to="body">
    <div v-if="cancelModal.show"
      class="position-fixed d-flex align-items-center justify-content-center"
      style="inset:0;background:rgba(0,0,0,0.5);z-index:2000;padding:16px;"
      @click.self="cancelModal.show=false">
      <div class="card border-0 shadow-lg" style="width:100%;max-width:420px;border-radius:20px;">
        <div class="card-body p-4">
          <div class="text-center mb-3">
            <div style="font-size:3rem;">🗑️</div>
            <h5 class="fw-bold mb-1">Huỷ đơn hàng?</h5>
            <p class="text-muted small">#{{ cancelModal.order?.orderNumber }}</p>
          </div>
 
          <!-- Thông báo hoàn tiền -->
          <div v-if="cancelModal.order?.paymentStatus==='PAID'"
            class="p-3 rounded-3 mb-3" style="background:#d4edda;border:1px solid #c3e6cb;">
            <div class="fw-semibold small text-success mb-1">
              <i class="bi bi-arrow-counterclockwise me-1"></i>Hoàn tiền tự động
            </div>
            <div class="small text-success">
              Vì bạn đã thanh toán, số tiền
              <strong>{{ formatPrice(cancelModal.order?.finalPrice) }}</strong>
              sẽ được hoàn lại trong 1-3 ngày làm việc.
            </div>
          </div>
          <div v-else class="p-3 rounded-3 mb-3" style="background:#fff3cd;">
            <div class="small text-warning">
              <i class="bi bi-info-circle me-1"></i>
              Đơn chưa thanh toán — không cần hoàn tiền.
            </div>
          </div>
 
          <div class="mb-3">
            <label class="small fw-semibold mb-1 d-block">Lý do huỷ (không bắt buộc):</label>
            <select class="form-select form-select-sm" v-model="cancelModal.reason">
              <option value="">-- Chọn lý do --</option>
              <option value="Đặt nhầm sản phẩm">Đặt nhầm sản phẩm</option>
              <option value="Muốn thay đổi size">Muốn thay đổi size</option>
              <option value="Tìm được chỗ khác rẻ hơn">Tìm được chỗ khác rẻ hơn</option>
              <option value="Không cần nữa">Không cần nữa</option>
              <option value="other">Lý do khác...</option>
            </select>
            <input v-if="cancelModal.reason==='other'" type="text"
              class="form-control form-control-sm mt-2"
              v-model="cancelModal.customReason" placeholder="Nhập lý do...">
          </div>
 
          <div class="d-flex gap-2">
            <button class="btn flex-grow-1 btn-danger rounded-pill fw-semibold"
              @click="doCancel" :disabled="cancelModal.loading">
              <span v-if="cancelModal.loading" class="spinner-border spinner-border-sm me-2"></span>
              Xác nhận huỷ
            </button>
            <button class="btn btn-outline-secondary px-4 rounded-pill"
              @click="cancelModal.show=false">Giữ lại</button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
 
</template>
 
<script setup>
import { ref, onMounted, reactive } from 'vue'
import { orderApi } from '@/api/index'
import { useToast } from 'vue-toastification'
 
const toast = useToast()
const orders = ref([])
const loading = ref(false)
const activeTab = ref('')
const currentPage = ref(0)
const pagination = ref({ totalPages:0, totalElements:0, last:true })
 
const statusTabs = [
  { label:'Tất cả',       value:'' },
  { label:'Chờ xác nhận', value:'PENDING' },
  { label:'Đang giao',    value:'SHIPPING' },
  { label:'Đã giao',      value:'DELIVERED' },
  { label:'Đã hủy',       value:'CANCELLED' }
]
 
// Bước tiến trình đơn hàng
const orderSteps = [
  { status:'PENDING',   label:'Chờ xác nhận', icon:'bi-clock' },
  { status:'CONFIRMED', label:'Đã xác nhận',  icon:'bi-check-circle' },
  { status:'PROCESSING',label:'Chuẩn bị',     icon:'bi-box-seam' },
  { status:'SHIPPING',  label:'Đang giao',    icon:'bi-truck' },
  { status:'DELIVERED', label:'Đã nhận',      icon:'bi-emoji-smile' }
]
 
const stepOrder = ['PENDING','CONFIRMED','PROCESSING','SHIPPING','DELIVERED']
 
const stepDone = (currentStatus, stepStatus) => {
  const ci = stepOrder.indexOf(currentStatus)
  const si = stepOrder.indexOf(stepStatus)
  return si <= ci
}
 
const progressWidth = (status) => {
  const map = { PENDING:0, CONFIRMED:25, PROCESSING:50, SHIPPING:75, DELIVERED:100 }
  return map[status] ?? 0
}
 
// Banner thông tin theo trạng thái
const statusInfo = (order) => {
  const m = {
    PENDING:    { icon:'bi-clock',         bg:'#fff3cd', color:'#856404', text:'Đơn hàng đang chờ xác nhận thanh toán từ shop. Nếu bạn đã chuyển khoản, vui lòng chờ shop xác nhận.' },
    CONFIRMED:  { icon:'bi-check-circle',  bg:'#d1ecf1', color:'#0c5460', text:'Shop đã xác nhận thanh toán và đang chuẩn bị hàng cho bạn.' },
    PROCESSING: { icon:'bi-box-seam',      bg:'#e2d9f3', color:'#4a0e8f', text:'Shop đang đóng gói hàng. Đơn hàng sẽ sớm được chuyển cho đơn vị vận chuyển.' },
    SHIPPING:   { icon:'bi-truck',         bg:'#cce5ff', color:'#004085', text:'Đơn hàng đang trên đường giao đến bạn. Sau khi nhận được hàng, hãy nhấn "Đã nhận hàng".' },
    DELIVERED:  { icon:'bi-emoji-smile',   bg:'#d4edda', color:'#155724', text:'Đơn hàng đã được giao thành công. Cảm ơn bạn đã mua sắm tại CosPlay Shop! ❤️' },
    CANCELLED:  { icon:'bi-x-circle',      bg:'#f8d7da', color:'#721c24', text:'Đơn hàng đã bị hủy.' }
  }
  return m[order.status] || null
}
 
const statusLabel  = (s) => ({ PENDING:'Chờ xác nhận',CONFIRMED:'Đã xác nhận',PROCESSING:'Đang chuẩn bị',SHIPPING:'Đang giao',DELIVERED:'Đã giao',CANCELLED:'Đã hủy',RETURNED:'Hoàn trả' }[s]||s)
const statusColor  = (s) => ({ PENDING:'#ffc107',CONFIRMED:'#17a2b8',PROCESSING:'#6f42c1',SHIPPING:'#007bff',DELIVERED:'#28a745',CANCELLED:'#dc3545',RETURNED:'#fd7e14' }[s]||'#6c757d')
const paymentLabel = (s) => ({ PENDING:'Chưa thanh toán',PAID:'Đã thanh toán',FAILED:'Thất bại',REFUNDED:'Hoàn tiền' }[s]||s)
const formatPrice  = (p) => p!=null ? new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(p) : '—'
const formatDate   = (d) => d ? new Date(d).toLocaleString('vi-VN',{day:'2-digit',month:'2-digit',year:'numeric',hour:'2-digit',minute:'2-digit'}) : '—'
 
const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await orderApi.getMyOrders({ page:currentPage.value, size:5 })
    const data = res.data.data
    orders.value = data.content
    pagination.value = { totalPages:data.totalPages, totalElements:data.totalElements, last:data.last }
  } finally { loading.value = false }
}
 
const filterStatus = (status) => {
  activeTab.value = status
  currentPage.value = 0
  fetchOrders()
}
 
const changePage = (page) => {
  if (page<0||page>=pagination.value.totalPages) return
  currentPage.value = page
  fetchOrders()
}
 
const confirmReceived = async (order) => {
  try {
    await orderApi.confirmDelivery(order.id)
    order.status = 'DELIVERED'
    order.paymentStatus = 'PAID'
    toast.success('Xác nhận nhận hàng thành công! ✅')
  } catch (e) {
    toast.error('Có lỗi xảy ra')
  }
}
 
const cancelModal = reactive({
  show: false, loading: false,
  order: null, reason: '', customReason: ''
})
 
const openCancelModal = (order) => {
  Object.assign(cancelModal, { show:true, order, reason:'', customReason:'', loading:false })
}
 
const doCancel = async () => {
  cancelModal.loading = true
  try {
    const reason = cancelModal.reason === 'other'
      ? cancelModal.customReason
      : cancelModal.reason
    await orderApi.cancelOrder(cancelModal.order.id, reason)
    cancelModal.order.status = 'CANCELLED'
    if (cancelModal.order.paymentStatus === 'PAID') {
      cancelModal.order.paymentStatus = 'REFUNDED'
      toast.success('✅ Đã huỷ đơn! Tiền sẽ được hoàn lại trong 1-3 ngày làm việc.')
    } else {
      toast.success('✅ Đã huỷ đơn hàng thành công!')
    }
    cancelModal.show = false
    await fetchOrders()
  } catch(e) {
    toast.error(e.response?.data?.message || 'Không thể huỷ đơn này')
  } finally { cancelModal.loading = false }
}
 
onMounted(fetchOrders)
</script>
 
<style scoped>
.page-item.active .page-link { background:#e91e8c;border-color:#e91e8c; }
.page-link { color:#e91e8c; }
</style>