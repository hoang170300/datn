<template>
    <div class="pos-container d-flex gap-3" style="height:calc(100vh - 80px);">
  
      <!-- LEFT: Tìm sản phẩm -->
      <div class="flex-grow-1 d-flex flex-column gap-3" style="min-width:0;">
  
        <!-- Search -->
        <div class="card border-0 shadow-sm" style="border-radius:12px;">
          <div class="card-body p-3">
            <div class="d-flex gap-2 align-items-center mb-3">
              <h6 class="fw-bold mb-0">🏪 POS - Bán hàng tại quầy</h6>
              <span class="badge bg-success ms-auto">Offline</span>
            </div>
            <div class="input-group">
              <span class="input-group-text bg-white"><i class="bi bi-search text-muted"></i></span>
              <input type="text" class="form-control border-start-0" v-model="keyword"
                placeholder="Tìm sản phẩm, nhân vật... (để trống = tất cả)" @input="searchDebounce">
            </div>
          </div>
        </div>
  
        <!-- Products grid -->
        <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden" style="border-radius:12px;">
          <div class="card-body p-3 h-100 overflow-y-auto">
            <div v-if="searching" class="text-center py-5">
              <div class="spinner-border spinner-border-sm text-secondary"></div>
            </div>
            <div v-else-if="!products.length" class="text-center py-5 text-muted">
              <i class="bi bi-search fs-1 d-block mb-2"></i>
              Không có sản phẩm nào
            </div>
            <div v-else class="row g-2">
              <div v-for="p in products" :key="p.id" class="col-6 col-lg-4">
                <div class="card h-100 border cursor-pointer"
                  style="border-radius:10px;transition:all 0.2s;"
                  @mouseenter="$event.currentTarget.style.borderColor='#e91e8c'"
                  @mouseleave="$event.currentTarget.style.borderColor=''"
                  @click="selectProduct(p)">
                  <div class="card-body p-2">
                    <img :src="p.primaryImageUrl||'/img/placeholder.svg'"
                      class="rounded w-100 mb-2" style="height:80px;object-fit:cover;"
                      @error="$event.target.src='/img/placeholder.svg'">
                    <div class="small fw-semibold lh-sm mb-1"
                      style="display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;">
                      {{ p.name }}
                    </div>
                    <div v-if="p.salePrice" class="small" style="color:#e91e8c;">
                      {{ formatPrice(p.salePrice) }}
                    </div>
                    <div v-if="p.rentalPricePerDay" class="small text-info">
                      Thuê: {{ formatPrice(p.rentalPricePerDay) }}/ngày
                    </div>
                    <div class="d-flex gap-1 mt-1 flex-wrap">
                      <span v-for="v in (p.variants||[]).slice(0,4)" :key="v.id"
                        class="badge bg-light text-dark border" style="font-size:0.65rem;">
                        {{ v.size }}
                        <span v-if="v.stockQuantity<=0&&v.rentalQuantity<=0" class="text-danger">✗</span>
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
  
      <!-- RIGHT: Giỏ hàng POS -->
      <div class="d-flex flex-column gap-3" style="width:380px;flex-shrink:0;">
  
        <!-- Customer info -->
        <div class="card border-0 shadow-sm" style="border-radius:12px;">
          <div class="card-body p-3">
            <h6 class="fw-bold mb-3"><i class="bi bi-person me-2"></i>Thông tin khách</h6>
            <div class="row g-2">
              <div class="col-7">
                <input type="text" class="form-control form-control-sm" v-model="customer.name"
                  placeholder="Tên khách hàng">
              </div>
              <div class="col-5">
                <input type="tel" class="form-control form-control-sm" v-model="customer.phone"
                  placeholder="Số điện thoại">
              </div>
            </div>
          </div>
        </div>
  
        <!-- Cart items -->
        <div class="card border-0 shadow-sm flex-grow-1 overflow-hidden" style="border-radius:12px;">
          <div class="card-body p-3 d-flex flex-column h-100">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h6 class="fw-bold mb-0"><i class="bi bi-cart3 me-2"></i>Đơn hàng</h6>
              <button v-if="cartItems.length" class="btn btn-sm btn-outline-danger rounded-pill"
                @click="cartItems=[]">Xóa tất cả</button>
            </div>
  
            <div class="flex-grow-1 overflow-y-auto">
              <div v-if="!cartItems.length" class="text-center py-4 text-muted">
                <i class="bi bi-bag fs-2 d-block mb-2"></i>
                Chọn sản phẩm từ danh sách
              </div>
              <div v-for="(item,i) in cartItems" :key="i"
                class="border rounded-3 p-2 mb-2">
                <div class="d-flex gap-2 align-items-start">
                  <div class="flex-grow-1" style="min-width:0;">
                    <div class="small fw-semibold text-truncate">{{ item.productName }}</div>
                    <div class="d-flex gap-1 flex-wrap mt-1">
                      <span class="badge bg-secondary">{{ item.size }}</span>
                      <span v-if="item.color" class="badge bg-light text-dark border">{{ item.color }}</span>
                      <span class="badge" :class="item.orderType==='RENTAL'?'bg-info text-dark':'bg-success'">
                        {{ item.orderType==='RENTAL' ? 'Thuê' : 'Mua' }}
                      </span>
                    </div>
                    <!-- Rental dates -->
                    <div v-if="item.orderType==='RENTAL'" class="mt-1">
                      <div class="d-flex gap-1">
                        <input type="date" class="form-control form-control-sm p-1"
                          style="font-size:0.7rem;" v-model="item.rentalStartDate" :min="today">
                        <span class="align-self-center small text-muted">→</span>
                        <input type="date" class="form-control form-control-sm p-1"
                          style="font-size:0.7rem;" v-model="item.rentalEndDate"
                          :min="item.rentalStartDate||today">
                      </div>
                      <small v-if="rentalDays(item)>0" class="text-info">
                        {{ rentalDays(item) }} ngày · {{ formatPrice(item.unitPrice * rentalDays(item)) }}
                      </small>
                      <small v-if="item.depositAmount>0" class="text-warning d-block">
                        🔒 Cọc: {{ formatPrice(item.depositAmount) }}
                      </small>
                    </div>
                  </div>
                  <div class="d-flex flex-column align-items-end gap-1">
                    <div class="small fw-bold" style="color:#e91e8c;">
                      {{ formatPrice(itemTotal(item)) }}
                    </div>
                    <div class="d-flex align-items-center gap-1">
                      <button class="btn btn-outline-secondary rounded-circle p-0"
                        style="width:22px;height:22px;font-size:0.7rem;"
                        @click="item.quantity>1?item.quantity--:cartItems.splice(i,1)">
                        <i class="bi bi-dash"></i>
                      </button>
                      <span class="small fw-bold" style="min-width:16px;text-align:center;">
                        {{ item.quantity }}
                      </span>
                      <button class="btn btn-outline-secondary rounded-circle p-0"
                        style="width:22px;height:22px;font-size:0.7rem;"
                        @click="item.quantity++">
                        <i class="bi bi-plus"></i>
                      </button>
                      <button class="btn btn-outline-danger rounded-circle p-0 ms-1"
                        style="width:22px;height:22px;font-size:0.7rem;"
                        @click="cartItems.splice(i,1)">
                        <i class="bi bi-trash3"></i>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
  
            <!-- Totals -->
            <div v-if="cartItems.length" class="border-top pt-3 mt-2">
              <div class="d-flex justify-content-between small mb-1">
                <span class="text-muted">Tạm tính</span>
                <span>{{ formatPrice(subtotal) }}</span>
              </div>
              <div v-if="totalDeposit>0" class="d-flex justify-content-between small mb-1">
                <span class="text-warning">🔒 Tiền cọc thuê</span>
                <span class="text-warning">{{ formatPrice(totalDeposit) }}</span>
              </div>
              <div class="d-flex justify-content-between fw-bold mb-3">
                <span>Tổng cộng</span>
                <span style="color:#e91e8c;">{{ formatPrice(grandTotal) }}</span>
              </div>
  
              <!-- Thanh toán -->
              <div class="mb-3">
                <label class="small fw-semibold mb-2 d-block">Phương thức thanh toán:</label>
                <div class="d-flex gap-2">
                  <button :class="['btn btn-sm flex-grow-1 rounded-pill',
                    payMethod==='COD'?'text-white':'btn-outline-secondary']"
                    :style="payMethod==='COD'?'background:#28a745':''"
                    @click="payMethod='COD'">
                    💵 Tiền mặt (COD)
                  </button>
                  <button :class="['btn btn-sm flex-grow-1 rounded-pill',
                    payMethod==='BANK_TRANSFER'?'text-white':'btn-outline-secondary']"
                    :style="payMethod==='BANK_TRANSFER'?'background:#007bff':''"
                    @click="payMethod='BANK_TRANSFER'">
                    🏦 Chuyển khoản
                  </button>
                </div>
              </div>
  
              <!-- Ghi chú -->
              <textarea class="form-control form-control-sm mb-3" rows="2"
                v-model="orderNote" placeholder="Ghi chú đơn hàng..."></textarea>
  
              <button class="btn w-100 py-2 fw-bold text-white"
                style="background:#e91e8c;border-radius:10px;"
                :disabled="placing" @click="placeOrder">
                <span v-if="placing" class="spinner-border spinner-border-sm me-2"></span>
                <i v-else class="bi bi-check-circle me-2"></i>
                Tạo đơn & {{ payMethod==='COD' ? 'Thu tiền' : 'In QR' }}
              </button>
            </div>
          </div>
        </div>
      </div>
  
      <!-- Modal chọn variant -->
      <Teleport to="body">
        <div v-if="selectedProduct"
          class="position-fixed d-flex align-items-center justify-content-center"
          style="inset:0;background:rgba(0,0,0,0.5);z-index:3000;padding:16px;"
          @click.self="selectedProduct=null">
          <div class="card border-0 shadow-lg" style="width:100%;max-width:460px;border-radius:20px;">
            <div class="card-body p-4">
              <div class="d-flex align-items-center gap-3 mb-4">
                <img :src="selectedProduct.primaryImageUrl||'/img/placeholder.svg'"
                  class="rounded-3" style="width:64px;height:64px;object-fit:cover;"
                  @error="$event.target.src='/img/placeholder.svg'">
                <div class="flex-grow-1">
                  <div class="fw-bold">{{ selectedProduct.name }}</div>
                  <div class="small text-muted">Chọn size và hình thức</div>
                </div>
                <button class="btn-close" @click="selectedProduct=null"></button>
              </div>
  
              <!-- Order type -->
              <div v-if="selectedProduct.productType==='BOTH'" class="mb-3">
                <div class="small fw-semibold mb-2">Hình thức:</div>
                <div class="d-flex gap-2">
                  <button :class="['btn btn-sm rounded-pill',pickType==='SALE'?'btn-dark':'btn-outline-secondary']"
                    @click="pickType='SALE'">🛍️ Mua</button>
                  <button :class="['btn btn-sm rounded-pill',pickType==='RENTAL'?'btn-info text-white':'btn-outline-info']"
                    @click="pickType='RENTAL'">📅 Thuê</button>
                </div>
              </div>
  
              <!-- Variants -->
              <div class="mb-3">
                <div class="small fw-semibold mb-2">Chọn Size:</div>
                <div class="d-flex gap-2 flex-wrap">
                  <button v-for="v in (selectedProduct.variants||[])" :key="v.id"
                    :class="['btn btn-sm rounded-2', pickVariant?.id===v.id?'btn-dark':'btn-outline-secondary']"
                    :disabled="pickType==='SALE'?v.stockQuantity<=0:v.rentalQuantity<=0"
                    style="min-width:52px;"
                    @click="pickVariant=v">
                    {{ v.size }}
                    <span v-if="v.color" class="d-block" style="font-size:0.6rem;">{{ v.color }}</span>
                    <span class="d-block" style="font-size:0.6rem;"
                      :class="(pickType==='SALE'?v.stockQuantity:v.rentalQuantity)<=0?'text-danger':'text-success'">
                      Còn {{ pickType==='SALE'?v.stockQuantity:v.rentalQuantity }}
                    </span>
                  </button>
                </div>
              </div>
  
              <!-- Qty -->
              <div class="d-flex align-items-center gap-3 mb-4">
                <span class="small fw-semibold">Số lượng:</span>
                <div class="d-flex align-items-center gap-2">
                  <button class="btn btn-outline-secondary btn-sm rounded-circle"
                    style="width:30px;height:30px;padding:0;" @click="pickQty>1&&pickQty--">
                    <i class="bi bi-dash"></i>
                  </button>
                  <span class="fw-bold" style="min-width:28px;text-align:center;">{{ pickQty }}</span>
                  <button class="btn btn-outline-secondary btn-sm rounded-circle"
                    style="width:30px;height:30px;padding:0;" @click="pickQty++">
                    <i class="bi bi-plus"></i>
                  </button>
                </div>
              </div>
  
              <button class="btn w-100 py-2 fw-bold text-white"
                style="background:#e91e8c;border-radius:12px;"
                :disabled="!pickVariant"
                @click="addToCart">
                <i class="bi bi-cart-plus me-2"></i>Thêm vào đơn
              </button>
            </div>
          </div>
        </div>
      </Teleport>
  
      <!-- Receipt modal -->
      <Teleport to="body">
        <div v-if="receipt"
          class="position-fixed d-flex align-items-center justify-content-center"
          style="inset:0;background:rgba(0,0,0,0.6);z-index:3000;padding:16px;">
          <div class="card border-0 shadow-lg" style="width:100%;max-width:360px;border-radius:16px;">
            <div class="card-body p-4" id="receipt-content">
              <div class="text-center mb-3">
                <div class="fw-bold fs-5">⭐ CosPlay Shop</div>
                <div class="small text-muted">123 Đường Cosplay, Q.1, TP.HCM</div>
                <div class="small text-muted">ĐT: 0909 123 456</div>
                <hr>
                <div class="fw-bold">HÓA ĐƠN BÁN HÀNG</div>
                <div class="small text-muted">#{{ receipt.orderNumber }}</div>
                <div class="small text-muted">{{ formatDateTime(receipt.createdAt) }}</div>
              </div>
  
              <div v-if="receipt.phone||receipt.note" class="mb-2 small">
                <span v-if="receipt.phone"><strong>KH:</strong> {{ receipt.phone }}</span>
                <span v-if="receipt.note" class="d-block text-muted">{{ receipt.note }}</span>
              </div>
              <hr class="my-2">
  
              <table class="w-100 small mb-2">
                <tr v-for="item in receipt.items" :key="item.id" class="border-bottom">
                  <td class="py-1">
                    <div class="fw-semibold">{{ item.productName }}</div>
                    <div class="text-muted">Size: {{ item.size }}
                      {{ item.itemType==='RENTAL'?'· Thuê '+item.rentalDays+' ngày':'' }}
                    </div>
                  </td>
                  <td class="text-end py-1" style="white-space:nowrap;">
                    x{{ item.quantity }}<br>
                    <span style="color:#e91e8c;">{{ formatPrice(item.totalPrice) }}</span>
                  </td>
                </tr>
              </table>
  
              <hr class="my-2">
              <div class="d-flex justify-content-between small mb-1">
                <span>Tạm tính:</span>
                <span>{{ formatPrice(receipt.subtotalPrice) }}</span>
              </div>
              <div v-if="receipt.totalDeposit>0" class="d-flex justify-content-between small mb-1 text-warning">
                <span>🔒 Tiền cọc:</span>
                <span>{{ formatPrice(receipt.totalDeposit) }}</span>
              </div>
              <div class="d-flex justify-content-between fw-bold">
                <span>TỔNG CỘNG:</span>
                <span style="color:#e91e8c;">{{ formatPrice(receipt.finalPrice) }}</span>
              </div>
              <div class="d-flex justify-content-between small text-muted mt-1">
                <span>Thanh toán:</span>
                <span>{{ receipt.paymentMethod==='COD'?'Tiền mặt':'Chuyển khoản' }}</span>
              </div>
  
              <hr class="my-2">
              <div class="text-center small text-muted">
                Cảm ơn quý khách! ❤️<br>
                Hẹn gặp lại tại CosPlay Shop
              </div>
            </div>
            <div class="card-footer border-0 bg-transparent p-3 pt-0">
              <div class="d-flex gap-2">
                <button class="btn flex-grow-1 btn-outline-secondary rounded-pill"
                  @click="printReceipt">
                  <i class="bi bi-printer me-1"></i>In hóa đơn
                </button>
                <button class="btn flex-grow-1 text-white rounded-pill"
                  style="background:#e91e8c;"
                  @click="receipt=null;resetCart()">
                  <i class="bi bi-check me-1"></i>Xong
                </button>
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
  const keyword = ref('')
  const products = ref([])
  const searching = ref(false)
  const cartItems = ref([])
  const customer = reactive({ name: '', phone: '' })
  const payMethod = ref('COD')
  const orderNote = ref('')
  const placing = ref(false)
  const receipt = ref(null)
  const selectedProduct = ref(null)
  const pickVariant = ref(null)
  const pickType = ref('SALE')
  const pickQty = ref(1)
  const today = new Date().toISOString().split('T')[0]
  
  // Load tất cả sản phẩm khi vào trang
  onMounted(() => doSearch())
  
  let searchTimer = null
  const searchDebounce = () => {
    clearTimeout(searchTimer)
    searchTimer = setTimeout(doSearch, 400)
  }
  
  const doSearch = async () => {
    searching.value = true
    try {
      // Truyền keyword rỗng = lấy tất cả sản phẩm
      const kw = keyword.value.trim() || ' '
      const res = await api.get(`/admin/pos/search-products?keyword=${encodeURIComponent(kw)}`)
      products.value = res.data.data || []
    } finally { searching.value = false }
  }
  
  const selectProduct = (p) => {
    selectedProduct.value = p
    pickVariant.value = null
    pickQty.value = 1
    pickType.value = p.productType === 'RENTAL' ? 'RENTAL'
      : p.productType === 'SALE' ? 'SALE' : 'SALE'
  }
  
  const addToCart = () => {
    if (!pickVariant.value) return
    const p = selectedProduct.value
    const v = pickVariant.value
  
    cartItems.value.push({
      variantId:     v.id,
      productName:   p.name,
      size:          v.size,
      color:         v.color,
      orderType:     pickType.value,
      quantity:      pickQty.value,
      unitPrice:     pickType.value === 'RENTAL'
                       ? p.rentalPricePerDay || 0
                       : (v.price || p.salePrice || 0),
      depositAmount: pickType.value === 'RENTAL'
                       ? (p.depositAmount || 0) * pickQty.value
                       : 0,
      rentalStartDate: pickType.value === 'RENTAL' ? today : null,
      rentalEndDate:   pickType.value === 'RENTAL' ? today : null
    })
    selectedProduct.value = null
    toast.success(`Đã thêm ${p.name}`)
  }
  
  const rentalDays = (item) => {
    if (!item.rentalStartDate || !item.rentalEndDate) return 0
    const d = Math.ceil((new Date(item.rentalEndDate) - new Date(item.rentalStartDate)) / 86400000) + 1
    return Math.max(1, d)
  }
  
  const itemTotal = (item) => {
    if (item.orderType === 'RENTAL') {
      return item.unitPrice * rentalDays(item) * item.quantity + (item.depositAmount || 0)
    }
    return item.unitPrice * item.quantity
  }
  
  const subtotal  = computed(() => cartItems.value.reduce((s,i) => {
    if (i.orderType === 'RENTAL')
      return s + i.unitPrice * rentalDays(i) * i.quantity
    return s + i.unitPrice * i.quantity
  }, 0))
  
  const totalDeposit = computed(() =>
    cartItems.value.filter(i => i.orderType==='RENTAL')
      .reduce((s,i) => s + (i.depositAmount||0), 0))
  
  const grandTotal = computed(() => subtotal.value + totalDeposit.value)
  
  const formatPrice = (p) => p != null
    ? new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(p)
    : '—'
  
  const formatDateTime = (d) => d
    ? new Date(d).toLocaleString('vi-VN')
    : new Date().toLocaleString('vi-VN')
  
  const placeOrder = async () => {
    if (!cartItems.value.length) { toast.warning('Giỏ hàng trống'); return }
    placing.value = true
    try {
      const payload = {
        customerName:  customer.name,
        customerPhone: customer.phone,
        paymentMethod: payMethod.value,
        note: orderNote.value,
        items: cartItems.value.map(i => ({
          variantId:       i.variantId,
          quantity:        i.quantity,
          orderType:       i.orderType,
          rentalStartDate: i.rentalStartDate,
          rentalEndDate:   i.rentalEndDate
        }))
      }
      const res = await api.post('/admin/pos/orders', payload)
      receipt.value = res.data.data
      toast.success('✅ Đặt hàng thành công!')
    } catch (e) {
      toast.error(e.response?.data?.message || 'Lỗi tạo đơn')
    } finally { placing.value = false }
  }
  
  const printReceipt = () => {
    const content = document.getElementById('receipt-content').innerHTML
    const win = window.open('', '_blank', 'width=400,height=600')
    win.document.write(`
      <html><head><title>Hóa đơn</title>
      <style>body{font-family:Arial,sans-serif;font-size:12px;padding:16px;}
      table{width:100%;} hr{border:1px dashed #ccc;} .text-muted{color:#666;}
      .fw-bold{font-weight:bold;} .text-end{text-align:right;} .text-center{text-align:center;}
      </style></head><body>${content}</body></html>`)
    win.document.close()
    win.print()
  }
  
  const resetCart = () => {
    cartItems.value = []
    customer.name = ''
    customer.phone = ''
    orderNote.value = ''
    keyword.value = ''
    products.value = []
  }
  </script>
  
  <style scoped>
  .overflow-y-auto { overflow-y: auto; }
  .cursor-pointer { cursor: pointer; }
  </style>
  