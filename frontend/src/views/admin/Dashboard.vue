<template>
  <div>
    <!-- Stats Cards -->
    <div class="row g-4 mb-4">
      <div v-for="stat in stats" :key="stat.label" class="col-6 col-md-3">
        <div class="card border-0 shadow-sm h-100" style="border-radius:16px;">
          <div class="card-body p-4">
            <div class="d-flex align-items-center justify-content-between mb-3">
              <div class="rounded-3 p-2" :style="`background:${stat.color}22`">
                <i :class="['bi',stat.icon]" :style="`color:${stat.color};font-size:1.4rem`"></i>
              </div>
            </div>
            <div v-if="loadingStats" class="placeholder-glow">
              <div class="placeholder w-50 rounded" style="height:28px;background:#e9e9e9;"></div>
            </div>
            <div v-else class="fw-bold" style="font-size:1.5rem;">{{ stat.value }}</div>
            <div class="small text-muted">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Báo cáo hôm nay -->
    <div class="row g-4 mb-4">
      <div class="col-md-3" v-for="r in todayStats" :key="r.label">
        <div class="card border-0 shadow-sm" style="border-radius:14px;">
          <div class="card-body p-3 text-center">
            <div style="font-size:1.8rem;">{{ r.icon }}</div>
            <div class="fw-bold mt-1" :style="`color:${r.color}`">{{ r.value }}</div>
            <div class="small text-muted">{{ r.label }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="row g-4">
      <!-- Biểu đồ doanh thu 7 ngày -->
      <div class="col-lg-8">
        <div class="card border-0 shadow-sm mb-4" style="border-radius:16px;">
          <div class="card-body p-4">
            <div class="d-flex justify-content-between align-items-center mb-4">
              <h6 class="fw-bold mb-0">Doanh thu 7 ngày gần nhất</h6>
              <div class="d-flex gap-2 small">
                <span class="badge" style="background:#e91e8c22;color:#e91e8c;">Online</span>
                <span class="badge" style="background:#28a74522;color:#28a745;">Offline</span>
              </div>
            </div>
            <div style="height:200px;">
              <div v-if="!chartData.length" class="h-100 d-flex align-items-center justify-content-center text-muted">
                <span>Chưa có dữ liệu doanh thu</span>
              </div>
              <div v-else class="h-100 d-flex align-items-end gap-1 px-2 pb-2">
                <div v-for="(d,i) in chartData" :key="i"
                  class="flex-grow-1 d-flex flex-column align-items-center gap-1"
                  style="min-width:0;">
                  <div class="small text-muted" style="font-size:0.6rem;white-space:nowrap;">
                    {{ d.label }}
                  </div>
                  <div class="w-100 d-flex flex-column gap-1" style="height:150px;justify-content:flex-end;">
                    <div v-if="d.offline>0" class="w-100 rounded-top"
                      :style="`height:${Math.max(4,d.offline/maxRevenue*130)}px;background:#28a745;opacity:0.8;`"
                      :title="`Offline: ${formatPrice(d.offline)}`"></div>
                    <div v-if="d.online>0" class="w-100 rounded-top"
                      :style="`height:${Math.max(4,d.online/maxRevenue*130)}px;background:#e91e8c;opacity:0.8;`"
                      :title="`Online: ${formatPrice(d.online)}`"></div>
                    <div v-if="d.online===0&&d.offline===0" class="w-100 rounded"
                      style="height:4px;background:#e9ecef;"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Recent Orders -->
        <div class="card border-0 shadow-sm" style="border-radius:16px;">
          <div class="card-body p-4">
            <div class="d-flex align-items-center justify-content-between mb-4">
              <h6 class="fw-bold mb-0">Đơn hàng gần đây</h6>
              <router-link to="/admin/orders" class="btn btn-sm btn-outline-secondary rounded-pill">
                Xem tất cả
              </router-link>
            </div>
            <div class="table-responsive">
              <table class="table table-hover align-middle">
                <thead class="table-light">
                  <tr>
                    <th class="small">Mã đơn</th>
                    <th class="small">Khách</th>
                    <th class="small">Tổng tiền</th>
                    <th class="small">Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="!recentOrders.length">
                    <td colspan="4" class="text-center py-3 text-muted">Chưa có đơn hàng</td>
                  </tr>
                  <tr v-for="o in recentOrders" :key="o.id" v-else>
                    <td>
                      <router-link :to="`/admin/orders/${o.id}`"
                        class="fw-semibold text-decoration-none small" style="color:#e91e8c;">
                        #{{ o.orderNumber?.slice(-8) }}
                      </router-link>
                    </td>
                    <td class="small">{{ o.userName||o.phone||'—' }}</td>
                    <td class="small fw-semibold" style="color:#e91e8c;">{{ formatPrice(o.finalPrice) }}</td>
                    <td>
                      <span class="badge rounded-pill small"
                        :style="`background:${statusColor(o.status)}22;color:${statusColor(o.status)}`">
                        {{ statusLabel(o.status) }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <!-- RIGHT column -->
      <div class="col-lg-4 d-flex flex-column gap-4">

        <!-- Order status distribution -->
        <div class="card border-0 shadow-sm" style="border-radius:16px;">
          <div class="card-body p-4">
            <h6 class="fw-bold mb-4">Trạng thái đơn hàng</h6>
            <div v-for="s in orderStatusSummary" :key="s.status" class="mb-3">
              <div class="d-flex justify-content-between small mb-1">
                <span>{{ statusLabel(s.status) }}</span>
                <span class="fw-semibold">{{ s.count }}</span>
              </div>
              <div class="progress" style="height:6px;border-radius:4px;">
                <div class="progress-bar" :style="`width:${s.percent}%;background:${statusColor(s.status)}`"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Top sản phẩm -->
        <div class="card border-0 shadow-sm" style="border-radius:16px;">
          <div class="card-body p-4">
            <h6 class="fw-bold mb-4">🏆 Top sản phẩm bán chạy</h6>
            <div v-if="!topProducts.length" class="text-muted small text-center py-2">Chưa có dữ liệu</div>
            <div v-for="(p,i) in topProducts" :key="p.name" class="d-flex align-items-center gap-2 mb-3">
              <span class="fw-bold rounded-circle d-flex align-items-center justify-content-center"
                style="width:26px;height:26px;font-size:0.8rem;flex-shrink:0;"
                :style="i===0?'background:#ffd700;color:#333':i===1?'background:#c0c0c0;color:#333':i===2?'background:#cd7f32;color:#fff':'background:#f0f0f0;color:#666'">
                {{ i+1 }}
              </span>
              <div class="flex-grow-1 small">
                <div class="fw-semibold text-truncate">{{ p.name }}</div>
                <div class="text-muted">{{ p.totalSold }} lượt · {{ formatPrice(p.revenue) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Low stock warning -->
        <div v-if="lowStockItems.length" class="card shadow-sm"
          style="border-radius:16px;border:2px solid #ffc107!important;">
          <div class="card-body p-4">
            <h6 class="fw-bold mb-3 text-warning">
              <i class="bi bi-exclamation-triangle me-2"></i>Cảnh báo tồn kho thấp
            </h6>
            <div v-for="item in lowStockItems.slice(0,5)" :key="item.id"
              class="d-flex justify-content-between align-items-center mb-2">
              <div class="small">
                <div class="fw-semibold">{{ item.productName }}</div>
                <small class="text-muted">Size: {{ item.size }}</small>
              </div>
              <span class="badge" :class="item.stockQuantity===0?'bg-danger':'bg-warning text-dark'">
                {{ item.stockQuantity===0 ? 'Hết hàng' : `Còn ${item.stockQuantity}` }}
              </span>
            </div>
            <router-link to="/admin/products" class="btn btn-sm btn-warning rounded-pill mt-2 w-100">
              Quản lý sản phẩm
            </router-link>
          </div>
        </div>

        <!-- Quick actions -->
        <div class="card border-0 shadow-sm" style="border-radius:16px;">
          <div class="card-body p-4">
            <h6 class="fw-bold mb-3">Thao tác nhanh</h6>
            <div class="d-grid gap-2">
              <router-link to="/admin/pos" class="btn text-start d-flex align-items-center gap-2"
                style="background:#e91e8c;color:#fff;border-radius:10px;">
                <i class="bi bi-shop fs-5"></i>
                <div><div class="fw-semibold small">POS - Bán tại quầy</div></div>
              </router-link>
              <router-link to="/admin/products/create"
                class="btn btn-outline-primary text-start d-flex align-items-center gap-2"
                style="border-radius:10px;">
                <i class="bi bi-plus-circle fs-5"></i>
                <div><div class="fw-semibold small">Thêm sản phẩm mới</div></div>
              </router-link>
              <router-link to="/admin/rentals"
                class="btn btn-outline-warning text-start d-flex align-items-center gap-2"
                style="border-radius:10px;">
                <i class="bi bi-calendar2-check fs-5"></i>
                <div><div class="fw-semibold small">Quản lý thuê đồ</div></div>
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { orderApi, productApi } from '@/api/index'
import api from '@/api/index'

const chartData = ref([])
const maxRevenue = ref(1)
const recentOrders = ref([])
const orderStatusSummary = ref([])
const topProducts = ref([])
const lowStockItems = ref([])
const loadingStats = ref(true)

const stats = ref([
  { label:'Tổng đơn hàng',   value:'—', icon:'bi-bag-check',    color:'#e91e8c' },
  { label:'Doanh thu tháng', value:'—', icon:'bi-cash-stack',   color:'#28a745' },
  { label:'Tổng sản phẩm',  value:'—', icon:'bi-grid',         color:'#6f42c1' },
  { label:'Người dùng',      value:'—', icon:'bi-people',       color:'#17a2b8' }
])

const todayStats = ref([
  { label:'Đơn hôm nay',    value:'0',   color:'#007bff' },
  { label:'Doanh thu hôm nay', value:'—',  color:'#28a745' },
  { label:'Đơn thuê',        value:'0',  color:'#17a2b8' },
  { label:'Chờ xác nhận',    value:'0',   color:'#ffc107' }
])

const formatPrice = (p) => p!=null
  ? new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(p) : '—'
const statusLabel = (s) => ({
  PENDING:'Chờ xác nhận',CONFIRMED:'Đã xác nhận',PROCESSING:'Đang xử lý',
  SHIPPING:'Đang giao',DELIVERED:'Đã giao',CANCELLED:'Đã hủy',RETURNED:'Hoàn trả'
}[s]||s)
const statusColor = (s) => ({
  PENDING:'#ffc107',CONFIRMED:'#17a2b8',PROCESSING:'#6f42c1',
  SHIPPING:'#007bff',DELIVERED:'#28a745',CANCELLED:'#dc3545',RETURNED:'#fd7e14'
}[s]||'#6c757d')

const drawChart = (labels, onlineData, offlineData) => {
  chartData.value = labels.map((label, i) => ({
    label, online: onlineData[i]||0, offline: offlineData[i]||0
  }))
  const allVals = [...onlineData, ...offlineData].filter(v => v > 0)
  maxRevenue.value = allVals.length ? Math.max(...allVals) : 1
}

onMounted(async () => {
  // 1. Recent orders + stats
  try {
    const res = await api.get('/admin/orders?size=100&page=0')
    const allOrders = res.data.data?.content || []
    recentOrders.value = allOrders.slice(0, 5)
    stats.value[0].value = res.data.data?.totalElements || allOrders.length

    // Order status summary
    const map = {}
    allOrders.forEach(o => { map[o.status] = (map[o.status]||0)+1 })
    const total = allOrders.length || 1
    orderStatusSummary.value = Object.entries(map)
      .map(([status,count]) => ({ status, count, percent: Math.round(count/total*100) }))
      .sort((a,b) => b.count-a.count)

    // Doanh thu tháng
    const now = new Date()
    const monthRevenue = allOrders
      .filter(o => o.paymentStatus==='PAID' && new Date(o.createdAt).getMonth()===now.getMonth())
      .reduce((s,o) => s+(o.finalPrice||0), 0)
    stats.value[1].value = formatPrice(monthRevenue)

    // Today stats
    const today = now.toDateString()
    const todayOrders = allOrders.filter(o => new Date(o.createdAt).toDateString()===today)
    todayStats.value[0].value = todayOrders.length
    todayStats.value[1].value = formatPrice(
      todayOrders.filter(o=>o.paymentStatus==='PAID').reduce((s,o)=>s+(o.finalPrice||0),0))
    todayStats.value[2].value = allOrders.filter(o=>o.status==='SHIPPING').length
    todayStats.value[3].value = allOrders.filter(o=>o.status==='PENDING').length

    // Build 7-day chart data
    const labels = []
    const onlineData = []
    const offlineData = []
    for (let i=6; i>=0; i--) {
      const d = new Date(); d.setDate(d.getDate()-i)
      labels.push(`${d.getDate()}/${d.getMonth()+1}`)
      const dayOrders = allOrders.filter(o => new Date(o.createdAt).toDateString()===d.toDateString() && o.paymentStatus==='PAID')
      const onlineRev = dayOrders.filter(o => !o.orderNumber?.startsWith('POS')).reduce((s,o)=>s+(o.finalPrice||0),0)
      const offlineRev = dayOrders.filter(o => o.orderNumber?.startsWith('POS')).reduce((s,o)=>s+(o.finalPrice||0),0)
      onlineData.push(onlineRev)
      offlineData.push(offlineRev)
    }
    drawChart(labels, onlineData, offlineData)

    // Top products from order items
    const productMap = {}
    allOrders.forEach(o => {
      ;(o.items||[]).forEach(item => {
        const key = item.productName
        if (!productMap[key]) productMap[key] = { name:key, totalSold:0, revenue:0 }
        productMap[key].totalSold += item.quantity||1
        productMap[key].revenue += item.totalPrice||0
      })
    })
    topProducts.value = Object.values(productMap)
      .sort((a,b) => b.totalSold-a.totalSold).slice(0,5)

  } catch (e) { console.error(e) }

  // 2. Products count + low stock
  try {
    const res = await api.get('/admin/products?size=100')
    stats.value[2].value = res.data.data?.totalElements || '—'
    const products = res.data.data?.content || []
    const low = []
    for (const p of products.slice(0,20)) {
      try {
        const d = await api.get(`/products/${p.slug}`)
        const variants = d.data.data?.variants || []
        variants.forEach(v => {
          if (v.stockQuantity<=3 || v.rentalQuantity<=1)
            low.push({ id:v.id, productName:p.name, size:v.size, stockQuantity:v.stockQuantity })
        })
      } catch(e) {}
      if (low.length>=8) break
    }
    lowStockItems.value = low.sort((a,b) => a.stockQuantity-b.stockQuantity)
  } catch(e) {}

  // 3. User count
  try {
    const res = await api.get('/admin/users?size=1')
    stats.value[3].value = res.data.data?.totalElements||'—'
  } catch(e) {}

  loadingStats.value = false
})
</script>
