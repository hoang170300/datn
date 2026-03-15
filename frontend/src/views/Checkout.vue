<template>
  <div class="container py-5">
    <div class="row justify-content-center">
      <div class="col-lg-10">
        <h3 class="fw-bold mb-4"><i class="bi bi-bag-check me-2"></i>Thanh toán</h3>
 
        <div class="row g-4">
          <!-- LEFT -->
          <div class="col-lg-7 d-flex flex-column gap-4">
 
            <!-- Địa chỉ giao hàng -->
            <div class="card border-0 shadow-sm" style="border-radius:16px;">
              <div class="card-body p-4">
                <h6 class="fw-bold mb-3"><i class="bi bi-geo-alt me-2 text-danger"></i>Địa chỉ giao hàng</h6>
                <div class="row g-3">
                  <div class="col-12">
                    <label class="form-label small fw-semibold">Họ và tên người nhận *</label>
                    <input type="text" class="form-control" v-model="form.fullName" required>
                  </div>
                  <div class="col-12">
                    <label class="form-label small fw-semibold">Số điện thoại *</label>
                    <input type="tel" class="form-control" v-model="form.phone" required>
                  </div>
                  <div class="col-md-6">
                    <label class="form-label small fw-semibold">Tỉnh / Thành phố *</label>
                    <input type="text" class="form-control" v-model="form.province" required>
                  </div>
                  <div class="col-md-6">
                    <label class="form-label small fw-semibold">Quận / Huyện *</label>
                    <input type="text" class="form-control" v-model="form.district" required>
                  </div>
                  <div class="col-md-6">
                    <label class="form-label small fw-semibold">Phường / Xã *</label>
                    <input type="text" class="form-control" v-model="form.ward" required>
                  </div>
                  <div class="col-12">
                    <label class="form-label small fw-semibold">Địa chỉ cụ thể *</label>
                    <input type="text" class="form-control" v-model="form.street"
                      placeholder="Số nhà, tên đường..." required>
                  </div>
                </div>
              </div>
            </div>
 
            <!-- Phương thức thanh toán -->
            <div class="card border-0 shadow-sm" style="border-radius:16px;">
              <div class="card-body p-4">
                <h6 class="fw-bold mb-3"><i class="bi bi-credit-card me-2 text-info"></i>Phương thức thanh toán</h6>
                <div class="d-flex flex-column gap-3">
                  <label v-for="method in paymentMethods" :key="method.value"
                    class="d-flex align-items-center gap-3 p-3 rounded-3 cursor-pointer"
                    style="border:2px solid;transition:all 0.2s;"
                    :style="form.paymentMethod===method.value
                      ? 'border-color:#e91e8c;background:#fff5f8;cursor:pointer'
                      : 'border-color:#dee2e6;cursor:pointer'"
                    @click="form.paymentMethod=method.value">
                    <span style="font-size:1.6rem;">{{ method.icon }}</span>
                    <div class="flex-grow-1">
                      <div class="fw-semibold">{{ method.label }}</div>
                      <small class="text-muted">{{ method.desc }}</small>
                    </div>
                    <div class="rounded-circle border d-flex align-items-center justify-content-center"
                      style="width:20px;height:20px;flex-shrink:0;"
                      :style="form.paymentMethod===method.value
                        ? 'border-color:#e91e8c;background:#e91e8c'
                        : 'border-color:#dee2e6'">
                      <i v-if="form.paymentMethod===method.value"
                        class="bi bi-check text-white" style="font-size:0.7rem;"></i>
                    </div>
                  </label>
                </div>
 
                <!-- Thông tin chuyển khoản -->
                <div v-if="form.paymentMethod==='BANK_TRANSFER'"
                  class="mt-3 p-3 rounded-3" style="background:#f0f7ff;border:1px solid #b3d9ff;">
                  <div class="fw-semibold small mb-2">
                    <i class="bi bi-bank me-1 text-primary"></i>Thông tin tài khoản nhận tiền:
                  </div>
                  <div class="d-flex flex-column gap-1 small">
                    <div><span class="text-muted">Ngân hàng:</span> <strong>Vietcombank</strong></div>
                    <div><span class="text-muted">Số TK:</span> <strong class="text-primary">1234567890</strong>
                      <button class="btn btn-sm btn-outline-secondary ms-2 py-0 px-1"
                        style="font-size:0.65rem;"
                        @click="copyText('1234567890')">
                        <i class="bi bi-copy"></i>
                      </button>
                    </div>
                    <div><span class="text-muted">Chủ TK:</span> <strong>COSPLAY SHOP</strong></div>
                    <div><span class="text-muted">Nội dung CK:</span>
                      <strong class="text-danger">{{ transferContent }}</strong>
                      <button class="btn btn-sm btn-outline-secondary ms-2 py-0 px-1"
                        style="font-size:0.65rem;"
                        @click="copyText(transferContent)">
                        <i class="bi bi-copy"></i>
                      </button>
                    </div>
                  </div>
                  <div class="mt-2 small text-muted">
                    <i class="bi bi-info-circle me-1"></i>
                    Vui lòng chuyển khoản trước, đơn hàng sẽ được xác nhận sau khi nhận được tiền.
                  </div>
                </div>
 
                <!-- Thông tin ví điện tử -->
                <div v-if="form.paymentMethod==='E_WALLET'"
                  class="mt-3 p-3 rounded-3" style="background:#fff5f8;border:1px solid #ffb3d9;">
                  <div class="fw-semibold small mb-2">
                    <i class="bi bi-phone me-1" style="color:#e91e8c;"></i>Quét mã QR để thanh toán:
                  </div>
                  <div class="text-center p-3">
                    <div class="rounded-3 d-inline-flex align-items-center justify-content-center"
                      style="width:120px;height:120px;background:#f0f0f0;border:2px dashed #ccc;">
                      <div class="text-center text-muted small">
                        <i class="bi bi-qr-code fs-2 d-block"></i>
                        QR Code
                      </div>
                    </div>
                  </div>
                  <div class="small text-muted text-center">
                    Hỗ trợ: MoMo, ZaloPay, VNPay
                  </div>
                </div>
              </div>
            </div>
 
            <!-- Voucher -->
            <div class="card border-0 shadow-sm" style="border-radius:16px;">
              <div class="card-body p-4">
                <h6 class="fw-bold mb-3"><i class="bi bi-ticket-perforated me-2 text-warning"></i>Mã giảm giá</h6>
                <div class="input-group">
                  <input type="text" class="form-control text-uppercase" v-model="voucherCode"
                    placeholder="Nhập mã giảm giá" @keyup.enter="applyVoucher"
                    style="letter-spacing:1px;">
                  <button class="btn fw-semibold" style="background:#e91e8c;color:#fff;min-width:90px;"
                    @click="applyVoucher" :disabled="applyingVoucher">
                    <span v-if="applyingVoucher" class="spinner-border spinner-border-sm"></span>
                    <span v-else>Áp dụng</span>
                  </button>
                </div>
                <div v-if="voucherMsg" class="mt-2 small">
                  <span :class="discount>0 ? 'text-success' : 'text-danger'">
                    <i :class="['bi me-1', discount>0 ? 'bi-check-circle' : 'bi-x-circle']"></i>
                    {{ voucherMsg }}
                  </span>
                </div>
              </div>
            </div>
 
            <!-- Ghi chú -->
            <div class="card border-0 shadow-sm" style="border-radius:16px;">
              <div class="card-body p-4">
                <h6 class="fw-bold mb-3"><i class="bi bi-pencil-square me-2"></i>Ghi chú</h6>
                <textarea class="form-control" v-model="form.note" rows="3"
                  placeholder="Yêu cầu đặc biệt, ghi chú cho người giao hàng..."></textarea>
              </div>
            </div>
          </div>
 
          <!-- RIGHT: Tóm tắt đơn hàng -->
          <div class="col-lg-5">
            <div class="card border-0 shadow-sm sticky-top" style="top:80px;border-radius:16px;">
              <div class="card-body p-4">
                <h6 class="fw-bold mb-4">Đơn hàng ({{ cartStore.totalItems }} sản phẩm)</h6>
 
                <div style="max-height:220px;overflow-y:auto;" class="mb-3">
                  <div v-for="item in cartStore.items" :key="item.id" class="d-flex gap-3 mb-3">
                    <img :src="item.imageUrl||'/img/placeholder.svg'"
                      class="rounded" style="width:52px;height:52px;object-fit:cover;"
                      @error="$event.target.src='/img/placeholder.svg'">
                    <div class="flex-grow-1">
                      <div class="small fw-semibold lh-sm">{{ item.productName }}</div>
                      <small class="text-muted">{{ item.size }} × {{ item.quantity }}</small>
                    </div>
                    <div class="small fw-bold" style="color:#e91e8c;white-space:nowrap;">
                      {{ formatPrice(item.subtotal) }}
                    </div>
                  </div>
                </div>
 
                <hr>
                <div class="d-flex justify-content-between small mb-2">
                  <span class="text-muted">Tạm tính</span>
                  <span>{{ formatPrice(cartStore.totalPrice) }}</span>
                </div>
                <div v-if="discount>0" class="d-flex justify-content-between small mb-2">
                  <span class="text-success">Giảm giá</span>
                  <span class="text-success">-{{ formatPrice(discount) }}</span>
                </div>
                <div class="d-flex justify-content-between small mb-2">
                  <span class="text-muted">Phí vận chuyển</span>
                  <span>{{ formatPrice(shippingFee) }}</span>
                </div>
                <div v-if="totalDeposit>0" class="d-flex justify-content-between small mb-2">
                  <span class="text-warning fw-semibold">
                    <i class="bi bi-shield-lock me-1"></i>Tiền đặt cọc (thuê)
                  </span>
                  <span class="text-warning fw-semibold">{{ formatPrice(totalDeposit) }}</span>
                </div>
                <div v-if="totalDeposit>0" class="p-2 rounded-2 mb-2"
                  style="background:#fff3cd;font-size:0.75rem;color:#856404;">
                  <i class="bi bi-info-circle me-1"></i>
                  Tiền cọc sẽ được hoàn lại sau khi trả đồ nguyên vẹn.
                </div>
                <hr>
                <div class="d-flex justify-content-between fw-bold mb-4">
                  <span>Tổng thanh toán</span>
                  <span style="color:#e91e8c;font-size:1.2rem;">{{ formatPrice(finalTotal) }}</span>
                </div>
 
                <button class="btn w-100 py-3 fw-bold"
                  style="background:#e91e8c;color:#fff;border-radius:12px;font-size:1.05rem;"
                  @click="placeOrder" :disabled="loading||!isFormValid">
                  <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                  <i v-else class="bi bi-bag-check me-2"></i>
                  Đặt hàng ngay
                </button>
 
                <div class="text-center mt-3">
                  <small class="text-muted">
                    <i class="bi bi-shield-check me-1 text-success"></i>Giao dịch được bảo mật
                  </small>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
 
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { useCartStore } from '@/store/cart'
import { orderApi, userApi } from '@/api/index'
import { useToast } from 'vue-toastification'
 
const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()
const toast = useToast()
 
const loading = ref(false)
const applyingVoucher = ref(false)
const voucherCode = ref('')
const voucherMsg = ref('')
const discount = ref(0)
const shippingFee = ref(35000)
 
const paymentMethods = [
  { value: 'COD',           icon: '💵', label: 'Thanh toán khi nhận hàng (COD)', desc: 'Trả tiền mặt khi nhận' },
  { value: 'BANK_TRANSFER', icon: '🏦', label: 'Chuyển khoản ngân hàng',         desc: 'Xem thông tin TK bên dưới' },
  { value: 'E_WALLET',      icon: '📱', label: 'Ví điện tử (MoMo / ZaloPay)',    desc: 'Quét QR để thanh toán' }
]
 
const form = reactive({
  fullName: '', phone: '', province: '', district: '', ward: '',
  street: '', paymentMethod: 'COD', note: ''
})
 
// Nội dung chuyển khoản tự động
const transferContent = computed(() =>
  `COSPLAY ${authStore.user?.username?.toUpperCase() || 'ORDER'} ${Date.now().toString().slice(-6)}`
)
 
// Tính tổng tiền đặt cọc từ các item thuê trong giỏ
const totalDeposit = computed(() => {
  return cartStore.items
    .filter(i => i.orderType === 'RENTAL')
    .reduce((sum, i) => {
      // depositAmount có thể không có trong cart item, tạm để 0 nếu không có
      return sum + (i.depositAmount || 0) * i.quantity
    }, 0)
})
 
const finalTotal = computed(() =>
  Math.max(0, cartStore.totalPrice - discount.value + shippingFee.value + totalDeposit.value)
)
 
const isFormValid = computed(() =>
  form.fullName && form.phone && form.province && form.district && form.ward && form.street
)
 
const formatPrice = (p) =>
  p != null ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p) : '—'
 
const copyText = (text) => {
  navigator.clipboard.writeText(text)
  toast.success('Đã copy!')
}
 
const applyVoucher = async () => {
  if (!voucherCode.value.trim()) return
  applyingVoucher.value = true
  try {
    const res = await orderApi.validateVoucher(voucherCode.value, cartStore.totalPrice)
    const d = res.data.data.discountAmount
    discount.value = d
    voucherMsg.value = `Áp dụng thành công! Giảm ${formatPrice(d)}`
  } catch (e) {
    discount.value = 0
    voucherMsg.value = e.response?.data?.message || 'Mã không hợp lệ'
  } finally {
    applyingVoucher.value = false
  }
}
 
const placeOrder = async () => {
  if (!isFormValid.value) { toast.warning('Vui lòng điền đầy đủ thông tin giao hàng'); return }
  loading.value = true
  try {
    const res = await orderApi.createOrder({
      paymentMethod:            form.paymentMethod,
      voucherCode:              voucherCode.value || null,
      shippingAddressStreet:    form.street,
      shippingAddressWard:      form.ward,
      shippingAddressDistrict:  form.district,
      shippingAddressProvince:  form.province,
      phone:                    form.phone,
      note:                     form.note
    })
    toast.success('Đặt hàng thành công! 🎉')
    router.push(`/order-success/${res.data.data.orderNumber}`)
  } catch (e) {
    toast.error(e.response?.data?.message || 'Đặt hàng thất bại')
  } finally {
    loading.value = false
  }
}
 
onMounted(async () => {
  await cartStore.fetchCart()
 
  // FIX: Tự động điền từ thông tin cá nhân
  try {
    const res = await userApi.getProfile()
    const u = res.data.data
    form.fullName = u.fullName || authStore.user?.fullName || ''
    form.phone    = u.phone || ''
    form.street   = u.addressStreet || ''
    form.province = u.addressProvince || ''
    form.district = u.addressDistrict || ''
    form.ward     = u.addressWard || ''
  } catch (e) {
    form.fullName = authStore.user?.fullName || ''
  }
})
</script>
 
<style scoped>
.cursor-pointer { cursor: pointer; }
</style>