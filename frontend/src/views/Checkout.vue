<template>
  <div class="container py-5">
    <div class="row justify-content-center">
      <div class="col-lg-10">
        <h3 class="fw-bold mb-4">
          <i class="bi bi-bag-check me-2"></i>Thanh toán
        </h3>
 
        <div class="row g-4">
          <!-- LEFT -->
          <div class="col-lg-7 d-flex flex-column gap-4">
 
            <!-- Địa chỉ giao hàng -->
            <div class="card border-0 shadow-sm" style="border-radius:16px;">
              <div class="card-body p-4">
                <h6 class="fw-bold mb-3">
                  <i class="bi bi-geo-alt me-2 text-danger"></i>Địa chỉ giao hàng
                </h6>
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
                <h6 class="fw-bold mb-3">
                  <i class="bi bi-credit-card me-2 text-info"></i>Phương thức thanh toán
                </h6>
 
                <!-- 3 option cards -->
                <div class="d-flex flex-column gap-3">
                  <div v-for="m in paymentMethods" :key="m.value"
                    class="p-3 rounded-3 d-flex align-items-center gap-3"
                    style="border:2px solid;cursor:pointer;transition:all 0.18s;"
                    :style="form.paymentMethod===m.value
                      ? 'border-color:#e91e8c;background:#fff5f8;'
                      : 'border-color:#e9ecef;background:#fff;'"
                    @click="form.paymentMethod=m.value">
                    <span style="font-size:1.7rem;line-height:1;">{{ m.icon }}</span>
                    <div class="flex-grow-1">
                      <div class="fw-semibold small">{{ m.label }}</div>
                      <div class="text-muted" style="font-size:0.76rem;">{{ m.desc }}</div>
                    </div>
                    <!-- Radio circle -->
                    <div class="rounded-circle flex-shrink-0 d-flex align-items-center justify-content-center"
                      style="width:20px;height:20px;border:2px solid;"
                      :style="form.paymentMethod===m.value
                        ? 'border-color:#e91e8c;background:#e91e8c;'
                        : 'border-color:#ced4da;'">
                      <i v-if="form.paymentMethod===m.value"
                        class="bi bi-check text-white" style="font-size:0.65rem;"></i>
                    </div>
                  </div>
                </div>
 
                <!-- Chi tiết theo từng phương thức -->
 
                <!-- COD: giao hàng shipper thu hộ -->
                <div v-if="form.paymentMethod==='COD'"
                  class="mt-3 p-3 rounded-3 small"
                  style="background:#f0fff4;border:1px solid #86efac;">
                  <i class="bi bi-truck me-1 text-success"></i>
                  <strong class="text-success">Shipper ứng COD</strong>
                  khi giao hàng tận nơi. Chuẩn bị đúng số tiền
                  <strong>{{ formatPrice(finalTotal) }}</strong> khi nhận hàng.
                </div>
 
  
 
                <!-- E_WALLET: sandbox -->
                <div v-if="form.paymentMethod==='E_WALLET'"
                  class="mt-3 p-3 rounded-3"
                  style="background:#f0fdf4;border:1px solid #4ade80;">
                  <div class="fw-semibold small mb-2 text-success">
                    <i class="bi bi-shield-check me-1"></i>Cổng thanh toán Sandbox (Test)
                  </div>
                  <div class="p-2 rounded-2 mb-2 small"
                    style="background:#fff;border:1px dashed #86efac;">
                    <div class="fw-semibold mb-1">Thông tin test card:</div>
                    <div>So the: <strong>4111 1111 1111 1111</strong></div>
                    <div>Ngay HH: <strong>12/26</strong>  CVV: <strong>123</strong></div>
                  </div>
                  <div v-if="!sandboxPaid && !sandboxProcessing">
                    <button type="button"
                      class="btn btn-sm w-100 rounded-pill fw-semibold"
                      style="background:#16a34a;color:#fff;"
                      @click="openSandbox">
                      <i class="bi bi-credit-card me-1"></i>Mo cong thanh toan Sandbox
                    </button>
                  </div>
                  <div v-else-if="sandboxProcessing"
                    class="text-center py-2 small fw-semibold text-info">
                    <span class="spinner-border spinner-border-sm me-2"></span>
                    Dang xu ly... Vui long cho
                  </div>
                  <div v-else-if="sandboxPaid && !loading"
                    class="text-center py-2 small fw-bold text-success">
                    <i class="bi bi-check-circle-fill me-1"></i>
                    Da thanh toan! Dang chuyen huong...
                  </div>
                </div>
              </div>
            </div>
 
            <!-- Voucher -->
            <div class="card border-0 shadow-sm" style="border-radius:16px;">
              <div class="card-body p-4">
                <h6 class="fw-bold mb-3">
                  <i class="bi bi-ticket-perforated me-2 text-warning"></i>Ma giam gia
                </h6>
                <div class="input-group">
                  <input type="text" class="form-control text-uppercase"
                    v-model="voucherCode"
                    placeholder="Nhap ma giam gia"
                    @keyup.enter="applyVoucher"
                    style="letter-spacing:1px;">
                  <button class="btn fw-semibold"
                    style="background:#e91e8c;color:#fff;min-width:90px;"
                    @click="applyVoucher"
                    :disabled="applyingVoucher">
                    <span v-if="applyingVoucher" class="spinner-border spinner-border-sm"></span>
                    <span v-else>Ap dung</span>
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
 
            <!-- Ghi chu -->
            <div class="card border-0 shadow-sm" style="border-radius:16px;">
              <div class="card-body p-4">
                <h6 class="fw-bold mb-3">
                  <i class="bi bi-pencil-square me-2"></i>Ghi chu
                </h6>
                <textarea class="form-control" v-model="form.note" rows="3"
                  placeholder="Yeu cau dac biet, ghi chu cho nguoi giao hang..."></textarea>
              </div>
            </div>
          </div>
 
          <!-- RIGHT: Tom tat don hang -->
          <div class="col-lg-5">
            <div class="card border-0 shadow-sm sticky-top" style="top:80px;border-radius:16px;">
              <div class="card-body p-4">
                <h6 class="fw-bold mb-4">
                  Don hang ({{ cartStore.totalItems }} san pham)
                </h6>
 
                <div style="max-height:220px;overflow-y:auto;" class="mb-3">
                  <div v-for="item in cartStore.items" :key="item.id"
                    class="d-flex gap-3 mb-3">
                    <img :src="item.imageUrl || '/img/placeholder.svg'"
                      class="rounded"
                      style="width:52px;height:52px;object-fit:cover;"
                      @error="$event.target.src='/img/placeholder.svg'">
                    <div class="flex-grow-1">
                      <div class="small fw-semibold lh-sm">{{ item.productName }}</div>
                      <small class="text-muted">{{ item.size }} x {{ item.quantity }}</small>
                    </div>
                    <div class="small fw-bold" style="color:#e91e8c;white-space:nowrap;">
                      {{ formatPrice(item.subtotal || item.totalPrice) }}
                    </div>
                  </div>
                </div>
 
                <hr>
                <div class="d-flex justify-content-between small mb-2">
                  <span class="text-muted">Tam tinh</span>
                  <span>{{ formatPrice(cartStore.totalPrice) }}</span>
                </div>
                <div v-if="discount > 0" class="d-flex justify-content-between small mb-2">
                  <span class="text-success">Giam gia (voucher)</span>
                  <span class="text-success fw-semibold">-{{ formatPrice(discount) }}</span>
                </div>
                <div class="d-flex justify-content-between small mb-2">
                  <span class="text-muted">Phi van chuyen</span>
                  <span>{{ formatPrice(shippingFee) }}</span>
                </div>
                <div v-if="totalDeposit > 0" class="d-flex justify-content-between small mb-2">
                  <span class="fw-semibold" style="color:#f59e0b;">
                    <i class="bi bi-shield-lock me-1"></i>Tien dat coc (thue)
                  </span>
                  <span class="fw-semibold" style="color:#f59e0b;">
                    +{{ formatPrice(totalDeposit) }}
                  </span>
                </div>
                <div v-if="totalDeposit > 0"
                  class="p-2 rounded-2 mb-2"
                  style="background:#fff3cd;font-size:0.73rem;color:#856404;">
                  <i class="bi bi-info-circle me-1"></i>
                  Tien coc hoan lai sau khi tra do nguyen ven.
                </div>
                <hr>
                <div class="d-flex justify-content-between fw-bold mb-1">
                  <span>Tong thanh toan</span>
                  <span style="color:#e91e8c;font-size:1.2rem;">
                    {{ formatPrice(finalTotal) }}
                  </span>
                </div>
 
                <!-- Badge trang thai thanh toan theo phuong thuc -->
                <div class="mb-4 mt-2 small text-center">
                  <span v-if="form.paymentMethod==='COD'"
                    class="badge rounded-pill"
                    style="background:#fff3cd;color:#856404;">
                    <i class="bi bi-clock me-1"></i>Thu tien khi nhan hang
                  </span>
                  <span v-else-if="form.paymentMethod==='BANK_TRANSFER'"
                    class="badge rounded-pill"
                    style="background:#dbeafe;color:#1d4ed8;">
                    <i class="bi bi-phone me-1"></i>Shipper thu ho khi nhan hang
                  </span>
                  <span v-else-if="form.paymentMethod==='E_WALLET' && sandboxPaid"
                    class="badge rounded-pill bg-success text-white">
                    <i class="bi bi-check-circle me-1"></i>Da thanh toan qua sandbox
                  </span>
                  <span v-else-if="form.paymentMethod==='E_WALLET'"
                    class="badge rounded-pill"
                    style="background:#fef9c3;color:#854d0e;">
                    <i class="bi bi-exclamation-triangle me-1"></i>Chua thanh toan sandbox
                  </span>
                </div>
 
                <button class="btn w-100 py-3 fw-bold"
                  style="background:linear-gradient(135deg,#e91e8c,#c2185b);color:#fff;border-radius:12px;font-size:1.05rem;"
                  @click="placeOrder"
                  :disabled="loading || !isFormValid || needsSandbox">
                  <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                  <i v-else class="bi bi-bag-check me-2"></i>
                  Dat hang ngay
                </button>
 
                <div v-if="needsSandbox"
                  class="text-center mt-2 small text-warning">
                  <i class="bi bi-exclamation-triangle me-1"></i>
                  Vui long hoan tat sandbox truoc khi dat hang
                </div>
                <div class="text-center mt-3">
                  <small class="text-muted">
                    <i class="bi bi-shield-check me-1 text-success"></i>
                    Giao dich duoc bao mat SSL
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
 
const router    = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()
const toast     = useToast()
 
const loading         = ref(false)
const applyingVoucher = ref(false)
const voucherCode     = ref('')
const voucherMsg      = ref('')
const discount        = ref(0)
const shippingFee     = ref(35000)
const sandboxPaid        = ref(false)
const sandboxProcessing  = ref(false)
 
// 3 luong thanh toan
const paymentMethods = [
  {
    value: 'COD',
    label: 'Tien mat khi nhan hang',
    desc: 'Shipper thu tien mat tai cua. Khong can chuyen khoan truoc.'
  },
  {
    value: 'E_WALLET',
    label: 'Thanh toan qua cong (Sandbox)',
    desc: 'Test luong thanh toan online. Khong mat tien that.'
  },
]
 
const form = reactive({
  fullName: '', phone: '', province: '', district: '', ward: '',
  street: '', paymentMethod: 'COD', note: ''
})
 
const totalDeposit = computed(() =>
  cartStore.items.reduce((s, i) => s + (parseFloat(i.depositAmount) || 0), 0)
)
 
const finalTotal = computed(() =>
  Math.max(0, (cartStore.totalPrice || 0) - discount.value + shippingFee.value + totalDeposit.value)
)
 
const isFormValid = computed(() =>
  !!(form.fullName && form.phone && form.province && form.district && form.ward && form.street)
)
 
// Chi can hoan tat sandbox khi chon E_WALLET
const needsSandbox = computed(() =>
  form.paymentMethod === 'E_WALLET' && !sandboxPaid.value
)
 
const formatPrice = (p) => p != null
  ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p)
  : ''
 
const applyVoucher = async () => {
  if (!voucherCode.value.trim()) return
  applyingVoucher.value = true
  try {
    const res = await orderApi.validateVoucher(voucherCode.value, cartStore.totalPrice)
    const d = res.data.data?.discountAmount || 0
    discount.value  = d
    voucherMsg.value = 'Ap dung thanh cong! Giam ' + formatPrice(d)
  } catch (e) {
    discount.value  = 0
    voucherMsg.value = e.response?.data?.message || 'Ma khong hop le'
  } finally {
    applyingVoucher.value = false
  }
}
 
// Sandbox popup - dung string array de tranh parse loi trong .vue
const openSandbox = () => {
  const amtFormatted = new Intl.NumberFormat('vi-VN').format(finalTotal.value || 0)
  const win = window.open('', '_blank', 'width=460,height=500,left=300,top=180')
  if (!win) {
    toast.error('Cho phep popup trong trinh duyet de su dung sandbox!')
    return
  }
  const parts = [
    '<html><head><title>CosPlay Sandbox</title><style>',
    'body{font-family:Arial;background:#f4f4f5;display:flex;align-items:center;justify-content:center;min-height:100vh;margin:0;}',
    '.box{background:#fff;border-radius:16px;padding:32px;width:340px;box-shadow:0 4px 24px rgba(0,0,0,.12);text-align:center;}',
    'h2{color:#e91e8c;margin:0 0 6px;font-size:1.3rem;}',
    '.sub{color:#666;font-size:.8rem;margin-bottom:20px;}',
    '.card-info{background:#f8fafc;border-radius:8px;padding:14px;margin:14px 0;text-align:left;font-size:.82rem;line-height:2;}',
    '.amount{font-size:1.6rem;font-weight:700;color:#e91e8c;margin:12px 0;}',
    '.btn{display:block;width:100%;padding:13px;border:none;border-radius:10px;font-size:1rem;cursor:pointer;margin-top:10px;font-weight:600;}',
    '.ok{background:#e91e8c;color:#fff;}',
    '.cancel{background:#f1f1f1;color:#555;}',
    '</style></head><body><div class="box">',
    '<h2>CosPlay Sandbox</h2>',
    '<div class="sub">Cong thanh toan thu nghiem</div>',
    '<div class="card-info">',
    'So the: <strong>4111 1111 1111 1111</strong><br>',
    'Ngay HH: <strong>12/26</strong>  CVV: <strong>123</strong><br>',
    'Ten: <strong>COSPLAY TEST</strong>',
    '</div>',
    '<div class="amount">' + amtFormatted + 'd</div>',
    '<button class="btn ok" onclick="window.opener.postMessage(\'SANDBOX_OK\',\'*\');window.close()">',
    'Xac nhan thanh toan',
    '</button>',
    '<button class="btn cancel" onclick="window.close()">Huy</button>',
    '</div></body></html>',
  ]
  win.document.write(parts.join(''))
  win.document.close()
  window.addEventListener('message', function handler(e) {
    if (e.data === 'SANDBOX_OK') {
      sandboxPaid.value = true
      window.removeEventListener('message', handler)
      // Tu dong dat hang ngay sau khi sandbox confirm
      // Khong can bam nut - tranh mat tien oan neu refresh/back
      autoPlaceAfterSandbox()
    }
  })
}
 
const autoPlaceAfterSandbox = async () => {
  if (!isFormValid.value) {
    // Form chua dien du → giu sandboxPaid, user dien form roi bam nut
    sandboxPaid.value = true
    toast.warning('Da thanh toan sandbox thanh cong! Vui long dien day du dia chi roi bam Dat hang.')
    // Scroll len form dia chi
    document.querySelector('.form-control')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    return
  }
  // Form hop le → dat hang ngay, hien loading
  sandboxProcessing.value = true
  toast.info('Dang xu ly don hang...')
  await placeOrder()
  sandboxProcessing.value = false
}
 
const placeOrder = async () => {
  if (!isFormValid.value) {
    toast.warning('Vui long dien day du thong tin giao hang')
    return
  }
  if (needsSandbox.value) {
    toast.warning('Vui long hoan tat thanh toan sandbox truoc!')
    return
  }
  loading.value = true
  try {
    const res = await orderApi.createOrder({
      paymentMethod:           form.paymentMethod,
      voucherCode:             voucherCode.value || null,
      shippingAddressStreet:   form.street,
      shippingAddressWard:     form.ward,
      shippingAddressDistrict: form.district,
      shippingAddressProvince: form.province,
      phone:                   form.phone,
      note:                    form.note || null,
    })
    toast.success('Dat hang thanh cong!')
    router.push('/order-success/' + res.data.data.orderNumber)
  } catch (e) {
    toast.error(e.response?.data?.message || 'Dat hang that bai, vui long thu lai')
  } finally {
    loading.value = false
  }
}
 
onMounted(async () => {
  await cartStore.fetchCart()
  try {
    const res = await userApi.getProfile()
    const u   = res.data.data
    form.fullName = u.fullName || authStore.user?.fullName || ''
    form.phone    = u.phone    || ''
    form.street   = u.addressStreet   || ''
    form.province = u.addressProvince || ''
    form.district = u.addressDistrict || ''
    form.ward     = u.addressWard     || ''
  } catch {
    form.fullName = authStore.user?.fullName || ''
  }
})
</script>
 
<style scoped>
.cursor-pointer { cursor: pointer; }
</style>