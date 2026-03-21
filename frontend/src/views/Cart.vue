<template>
  <div class="container py-5">
    <h3 class="fw-bold mb-4"><i class="bi bi-cart3 me-2"></i>Giỏ hàng của bạn</h3>

    <!-- Empty Cart -->
    <div v-if="!cartStore.items.length" class="text-center py-5">
      <div style="font-size:5rem;">🛒</div>
      <h5 class="text-muted mt-3">Giỏ hàng trống</h5>
      <p class="text-muted">Hãy thêm sản phẩm yêu thích vào giỏ hàng</p>
      <router-link to="/products" class="btn btn-lg rounded-pill px-5"
        style="background:#e91e8c;color:#fff;">
        Khám phá sản phẩm <i class="bi bi-arrow-right ms-1"></i>
      </router-link>
    </div>

    <!-- Cart Content -->
    <div v-else class="row g-4">
      <div class="col-lg-8">
        <div class="card border-0 shadow-sm" style="border-radius:16px;">
          <div class="card-body p-0">
            <div v-for="(item, index) in cartStore.items" :key="item.id"
              :class="['p-4', { 'border-bottom': index < cartStore.items.length - 1 }]">
              <div class="row align-items-center g-3">
                <!-- Image -->
                <div class="col-auto">
                  <img :src="item.imageUrl || '/img/placeholder.svg'"
                    class="rounded" style="width:80px;height:80px;object-fit:cover;"
                    :alt="item.productName">
                </div>
                <!-- Info -->
                <div class="col">
                  <router-link :to="`/products/${item.productSlug}`"
                    class="fw-semibold text-dark text-decoration-none">
                    {{ item.productName }}
                  </router-link>
                  <div class="small text-muted mt-1">
                    <span class="me-3">Size: <strong>{{ item.size }}</strong></span>
                    <span v-if="item.color" class="me-3">Màu: <strong>{{ item.color }}</strong></span>
                    <span class="badge"
                      :style="item.orderType === 'RENTAL' ? 'background:#17a2b8' : 'background:#e91e8c'">
                      {{ item.orderType === 'RENTAL' ? ' Thuê' : '🛍️ Mua' }}
                    </span>
                  </div>
                  <div v-if="item.orderType === 'RENTAL'" class="small text-info mt-1">
                    <i class="bi bi-calendar-range me-1"></i>
                    {{ item.rentalStartDate }} → {{ item.rentalEndDate }} ({{ item.rentalDays }} ngày)
                  </div>
                </div>
                <!-- Price & Qty -->
                <div class="col-auto text-end">
                  <div class="fw-bold mb-2" style="color:#e91e8c;">
                    {{ formatPrice(item.subtotal) }}
                  </div>
                  <div class="d-flex align-items-center gap-2 justify-content-end">
                    <button class="btn btn-outline-secondary btn-sm rounded-circle"
                      style="width:30px;height:30px;padding:0;"
                      @click="updateQty(item.id, item.quantity - 1)">
                      <i class="bi bi-dash"></i>
                    </button>
                    <span class="fw-semibold" style="min-width:24px;text-align:center;">{{ item.quantity }}</span>
                    <button class="btn btn-outline-secondary btn-sm rounded-circle"
                      style="width:30px;height:30px;padding:0;"
                      @click="updateQty(item.id, item.quantity + 1)">
                      <i class="bi bi-plus"></i>
                    </button>
                    <button class="btn btn-outline-danger btn-sm rounded-circle ms-2"
                      style="width:30px;height:30px;padding:0;"
                      @click="removeItem(item.id)">
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <button class="btn btn-outline-danger btn-sm mt-3" @click="clearCart">
          <i class="bi bi-trash me-1"></i>Xóa toàn bộ giỏ hàng
        </button>
      </div>

      <!-- Summary -->
      <div class="col-lg-4">
        <div class="card border-0 shadow-sm sticky-top" style="top:80px;border-radius:16px;">
          <div class="card-body p-4">
            <h6 class="fw-bold mb-4">Tóm tắt đơn hàng</h6>

            <div class="d-flex justify-content-between mb-2">
              <span class="text-muted">Tạm tính ({{ cartStore.totalItems }} sản phẩm)</span>
              <span>{{ formatPrice(cartStore.totalPrice) }}</span>
            </div>
            <div class="d-flex justify-content-between mb-2">
              <span class="text-muted">Phí vận chuyển</span>
              <span class="text-success">Tính ở bước sau</span>
            </div>
            <hr>
            <div class="d-flex justify-content-between fw-bold mb-4">
              <span>Tổng cộng</span>
              <span style="color:#e91e8c;font-size:1.2rem;">{{ formatPrice(cartStore.totalPrice) }}</span>
            </div>

            <router-link to="/checkout" class="btn w-100 py-3 fw-bold"
              style="background:#e91e8c;color:#fff;border-radius:12px;">
              Tiến hành thanh toán <i class="bi bi-arrow-right ms-1"></i>
            </router-link>
            <router-link to="/products" class="btn btn-outline-secondary w-100 mt-2">
              Tiếp tục mua sắm
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useCartStore } from '@/store/cart'
import { useToast } from 'vue-toastification'

const cartStore = useCartStore()
const toast = useToast()

const formatPrice = (p) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p)

const updateQty = async (itemId, quantity) => {
  try {
    await cartStore.updateQuantity(itemId, quantity)
  } catch (e) {
    toast.error('Có lỗi xảy ra')
  }
}

const removeItem = async (itemId) => {
  try {
    await cartStore.removeItem(itemId)
    toast.success('Đã xóa sản phẩm')
  } catch (e) {
    toast.error('Có lỗi xảy ra')
  }
}

const clearCart = async () => {
  if (confirm('Xóa toàn bộ giỏ hàng?')) {
    await cartStore.clearCart()
    toast.info('Đã xóa giỏ hàng')
  }
}

onMounted(() => cartStore.fetchCart())
</script>
