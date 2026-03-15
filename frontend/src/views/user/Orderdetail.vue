<template>
    <div class="card border-0 shadow-sm" style="border-radius:16px;">
      <div class="card-body p-4">
        <div class="d-flex align-items-center gap-3 mb-4">
          <button class="btn btn-outline-secondary btn-sm rounded-circle"
            style="width:36px;height:36px;padding:0;" @click="$router.back()">
            <i class="bi bi-arrow-left"></i>
          </button>
          <h5 class="fw-bold mb-0">Chi tiết đơn hàng</h5>
        </div>
   
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border" style="color:#e91e8c;"></div>
        </div>
   
        <div v-else-if="order">
          <!-- Header info -->
          <div class="row g-3 mb-4">
            <div class="col-md-6">
              <div class="p-3 rounded-3" style="background:#f8f9fa;">
                <div class="small text-muted mb-1">Mã đơn hàng</div>
                <div class="fw-bold">#{{ order.orderNumber }}</div>
                <div class="small text-muted mt-2">{{ formatDateTime(order.createdAt) }}</div>
              </div>
            </div>
            <div class="col-md-6">
              <div class="p-3 rounded-3" style="background:#f8f9fa;">
                <div class="small text-muted mb-1">Trạng thái</div>
                <span class="badge rounded-pill"
                  :style="`background:${statusColor(order.status)}22;color:${statusColor(order.status)}`">
                  {{ statusLabel(order.status) }}
                </span>
                <div class="mt-2">
                  <span class="badge"
                    :class="order.paymentStatus === 'PAID' ? 'bg-success' : 'bg-warning text-dark'">
                    {{ paymentLabel(order.paymentStatus) }}
                  </span>
                  <span class="badge bg-secondary ms-1">{{ order.paymentMethod }}</span>
                </div>
              </div>
            </div>
          </div>
   
          <!-- Items -->
          <h6 class="fw-bold mb-3">Sản phẩm đã đặt</h6>
          <div class="d-flex flex-column gap-3 mb-4">
            <div v-for="item in order.items" :key="item.id"
              class="d-flex align-items-center gap-3 p-3 border rounded-3">
              <div class="flex-grow-1">
                <div class="fw-semibold small">{{ item.productName }}</div>
                <div class="small text-muted">
                  Size: {{ item.size }}
                  <span v-if="item.color"> · {{ item.color }}</span>
                  · {{ item.itemType === 'RENTAL' ? '📅 Thuê' : '🛍️ Mua' }}
                </div>
                <div v-if="item.itemType === 'RENTAL' && item.rentalStartDate" class="small text-info">
                  {{ item.rentalStartDate }} → {{ item.rentalEndDate }} ({{ item.rentalDays }} ngày)
                </div>
              </div>
              <div class="text-end">
                <div class="small text-muted">x{{ item.quantity }}</div>
                <div class="fw-bold small" style="color:#e91e8c;">{{ formatPrice(item.totalPrice) }}</div>
              </div>
            </div>
          </div>
   
          <!-- Price summary -->
          <div class="p-3 rounded-3 mb-4" style="background:#f8f9fa;">
            <div class="d-flex justify-content-between small mb-2">
              <span class="text-muted">Tạm tính</span>
              <span>{{ formatPrice(order.subtotalPrice) }}</span>
            </div>
            <div v-if="order.discountAmount > 0" class="d-flex justify-content-between small mb-2">
              <span class="text-success">Giảm giá</span>
              <span class="text-success">-{{ formatPrice(order.discountAmount) }}</span>
            </div>
            <div class="d-flex justify-content-between small mb-2">
              <span class="text-muted">Phí vận chuyển</span>
              <span>{{ formatPrice(order.shippingFee) }}</span>
            </div>
            <hr class="my-2">
            <div class="d-flex justify-content-between fw-bold">
              <span>Tổng thanh toán</span>
              <span style="color:#e91e8c;font-size:1.1rem;">{{ formatPrice(order.finalPrice) }}</span>
            </div>
          </div>
   
          <!-- Shipping address -->
          <h6 class="fw-bold mb-3">Địa chỉ giao hàng</h6>
          <div class="p-3 rounded-3 border">
            <div class="fw-semibold">{{ order.userName }}</div>
            <div class="small text-muted mt-1">
              <i class="bi bi-telephone me-1"></i>{{ order.phone }}
            </div>
            <div class="small text-muted mt-1">
              <i class="bi bi-geo-alt me-1"></i>
              {{ [order.shippingAddressStreet, order.shippingAddressWard,
                  order.shippingAddressDistrict, order.shippingAddressProvince]
                  .filter(Boolean).join(', ') }}
            </div>
          </div>
   
          <!-- Actions -->
          <div v-if="order.status === 'SHIPPING'" class="mt-4">
            <button class="btn rounded-pill px-4" style="background:#28a745;color:#fff;"
              @click="confirmDelivery">
              <i class="bi bi-check-circle me-1"></i>Xác nhận đã nhận hàng
            </button>
          </div>
        </div>
      </div>
    </div>
  </template>
   
  <script setup>
  import { ref, onMounted } from 'vue'
  import { useRoute } from 'vue-router'
  import { orderApi } from '@/api/index'
  import { useToast } from 'vue-toastification'
   
  const route = useRoute()
  const toast = useToast()
  const order = ref(null)
  const loading = ref(false)
   
  const formatPrice = (p) => p != null ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p) : '—'
  const formatDateTime = (d) => d ? new Date(d).toLocaleString('vi-VN') : ''
  const statusLabel = (s) => ({ PENDING:'Chờ xác nhận', CONFIRMED:'Đã xác nhận', PROCESSING:'Đang xử lý', SHIPPING:'Đang giao', DELIVERED:'Đã giao', CANCELLED:'Đã hủy', RETURNED:'Hoàn trả' }[s] || s)
  const statusColor = (s) => ({ PENDING:'#ffc107', CONFIRMED:'#17a2b8', PROCESSING:'#6f42c1', SHIPPING:'#007bff', DELIVERED:'#28a745', CANCELLED:'#dc3545', RETURNED:'#fd7e14' }[s] || '#6c757d')
  const paymentLabel = (s) => ({ PENDING:'Chưa TT', PAID:'Đã TT', FAILED:'Thất bại', REFUNDED:'Hoàn tiền' }[s] || s)
   
  const confirmDelivery = async () => {
    try {
      await orderApi.confirmDelivery(order.value.id)
      order.value.status = 'DELIVERED'
      toast.success('Xác nhận nhận hàng thành công! ✅')
    } catch (e) {
      toast.error('Có lỗi xảy ra')
    }
  }
   
  onMounted(async () => {
    loading.value = true
    try {
      const res = await orderApi.getOrderDetail(route.params.id)
      order.value = res.data.data
    } catch (e) {
      toast.error('Không thể tải thông tin đơn hàng')
    } finally {
      loading.value = false
    }
  })
  </script>