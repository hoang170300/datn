<template>
    <div>
      <div class="d-flex align-items-center gap-3 mb-4">
        <button class="btn btn-outline-secondary btn-sm rounded-circle"
          style="width:36px;height:36px;padding:0;" @click="$router.back()">
          <i class="bi bi-arrow-left"></i>
        </button>
        <h5 class="fw-bold mb-0">Chi tiết đơn hàng — Admin</h5>
      </div>
   
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border" style="color:#e91e8c;"></div>
      </div>
   
      <div v-else-if="order" class="row g-4">
        <div class="col-lg-8">
          <!-- Order info -->
          <div class="card border-0 shadow-sm mb-4" style="border-radius:16px;">
            <div class="card-body p-4">
              <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-4">
                <div>
                  <h6 class="fw-bold mb-1">#{{ order.orderNumber }}</h6>
                  <small class="text-muted">{{ formatDateTime(order.createdAt) }}</small>
                </div>
                <div class="d-flex gap-2 align-items-center">
                  <select class="form-select form-select-sm" style="width:auto;"
                    :value="order.status" @change="updateStatus($event.target.value)">
                    <option v-for="s in statusOptions" :key="s.value" :value="s.value">{{ s.label }}</option>
                  </select>
                </div>
              </div>
   
              <!-- Items -->
              <div v-for="item in order.items" :key="item.id"
                class="d-flex align-items-center gap-3 py-3 border-bottom">
                <div class="flex-grow-1">
                  <div class="fw-semibold small">{{ item.productName }}</div>
                  <div class="small text-muted">
                    Size: {{ item.size }}<span v-if="item.color"> · {{ item.color }}</span>
                    · x{{ item.quantity }}
                    <span class="badge ms-2"
                      :class="item.itemType === 'RENTAL' ? 'bg-info text-dark' : 'bg-primary'">
                      {{ item.itemType === 'RENTAL' ? ' Thuê' : ' Mua' }}
                    </span>
                  </div>
                  <div v-if="item.rentalStartDate" class="small text-info">
                    {{ item.rentalStartDate }} → {{ item.rentalEndDate }}
                  </div>
                </div>
                <div class="fw-bold small" style="color:#e91e8c;">{{ formatPrice(item.totalPrice) }}</div>
              </div>
   
              <!-- Price totals -->
              <div class="mt-3">
                <div class="d-flex justify-content-between small text-muted mb-1">
                  <span>Tạm tính</span><span>{{ formatPrice(order.subtotalPrice) }}</span>
                </div>
                <div v-if="order.discountAmount > 0" class="d-flex justify-content-between small mb-1">
                  <span class="text-success">Giảm giá</span>
                  <span class="text-success">-{{ formatPrice(order.discountAmount) }}</span>
                </div>
                <div class="d-flex justify-content-between small text-muted mb-1">
                  <span>Phí vận chuyển</span><span>{{ formatPrice(order.shippingFee) }}</span>
                </div>
                <div class="d-flex justify-content-between fw-bold mt-2 pt-2 border-top">
                  <span>Tổng</span>
                  <span style="color:#e91e8c;">{{ formatPrice(order.finalPrice) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
   
        <div class="col-lg-4">
          <!-- Customer info -->
          <div class="card border-0 shadow-sm mb-4" style="border-radius:16px;">
            <div class="card-body p-4">
              <h6 class="fw-bold mb-3">Thông tin khách hàng</h6>
              <div class="fw-semibold">{{ order.userName }}</div>
              <div class="small text-muted mt-1"><i class="bi bi-telephone me-1"></i>{{ order.phone }}</div>
              <hr>
              <div class="small text-muted mb-1">Địa chỉ giao hàng:</div>
              <div class="small">
                {{ [order.shippingAddressStreet, order.shippingAddressWard,
                    order.shippingAddressDistrict, order.shippingAddressProvince]
                    .filter(Boolean).join(', ') }}
              </div>
              <div v-if="order.note" class="mt-3">
                <div class="small text-muted">Ghi chú:</div>
                <div class="small fst-italic">{{ order.note }}</div>
              </div>
            </div>
          </div>
   
          <!-- Payment info -->
          <div class="card border-0 shadow-sm" style="border-radius:16px;">
            <div class="card-body p-4">
              <h6 class="fw-bold mb-3">Thanh toán</h6>
              <div class="d-flex justify-content-between small mb-2">
                <span class="text-muted">Phương thức</span>
                <span class="fw-semibold">{{ order.paymentMethod }}</span>
              </div>
              <div class="d-flex justify-content-between small">
                <span class="text-muted">Trạng thái</span>
                <span class="badge" :class="order.paymentStatus === 'PAID' ? 'bg-success' : 'bg-warning text-dark'">
                  {{ paymentLabel(order.paymentStatus) }}
                </span>
              </div>
            </div>
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
   
  const statusOptions = [
    { value: 'PENDING', label: 'Chờ xác nhận' },
    { value: 'CONFIRMED', label: 'Đã xác nhận' },
    { value: 'PROCESSING', label: 'Đang xử lý' },
    { value: 'SHIPPING', label: 'Đang giao' },
    { value: 'DELIVERED', label: 'Đã giao' },
    { value: 'CANCELLED', label: 'Đã hủy' },
    { value: 'RETURNED', label: 'Hoàn trả' }
  ]
   
  const formatPrice = (p) => p != null ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p) : '—'
  const formatDateTime = (d) => d ? new Date(d).toLocaleString('vi-VN') : ''
  const paymentLabel = (s) => ({ PENDING:'Chưa TT', PAID:'Đã TT', FAILED:'Thất bại', REFUNDED:'Hoàn tiền' }[s] || s)
   
  const updateStatus = async (status) => {
    try {
      await orderApi.admin.updateStatus(order.value.id, status)
      order.value.status = status
      toast.success('Đã cập nhật trạng thái')
    } catch (e) { toast.error('Cập nhật thất bại') }
  }
   
  onMounted(async () => {
    loading.value = true
    try {
      const res = await orderApi.getOrderDetail(route.params.id)
      order.value = res.data.data
    } finally { loading.value = false }
  })
  </script>