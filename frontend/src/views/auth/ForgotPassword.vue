<template>
  <div class="min-vh-100 d-flex align-items-center py-5"
    style="background:linear-gradient(135deg,#1a1a2e,#16213e,#0f3460);">
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-md-5">
          <div class="text-center mb-4">
            <router-link to="/" class="text-decoration-none">
              <h2 style="color:#e91e8c;font-weight:900;">OtakuCosPlay</h2>
            </router-link>
          </div>
          <div class="card border-0 shadow-lg" style="border-radius:20px;">
            <div class="card-body p-5">
              <div v-if="!sent">
                <h4 class="fw-bold mb-1">Quên mật khẩu?</h4>
                <p class="text-muted mb-4">Nhập email để nhận link đặt lại mật khẩu</p>

                <div v-if="error" class="alert alert-danger py-2 small">
                  <i class="bi bi-exclamation-circle me-1"></i>{{ error }}
                </div>

                <form @submit.prevent="sendReset">
                  <div class="mb-4">
                    <label class="form-label fw-semibold small">Email đã đăng ký</label>
                    <div class="input-group">
                      <span class="input-group-text"><i class="bi bi-envelope"></i></span>
                      <input type="email" class="form-control" v-model="email"
                        placeholder="email@example.com" required>
                    </div>
                  </div>
                  <button type="submit" class="btn w-100 py-3 fw-bold"
                    style="background:#e91e8c;color:#fff;border-radius:12px;"
                    :disabled="loading">
                    <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                    <i v-else class="bi bi-send me-1"></i>Gửi link đặt lại
                  </button>
                </form>
              </div>

              <div v-else class="text-center">
                <h5 class="fw-bold mt-3 mb-2">Kiểm tra email của bạn!</h5>
                <p class="text-muted">Link đặt lại mật khẩu đã được gửi đến <strong>{{ email }}</strong></p>
                <p class="text-muted small">Kiểm tra cả hộp thư spam nếu không thấy email.</p>
              </div>

              <div class="text-center mt-4">
                <router-link to="/login" style="color:#e91e8c;" class="fw-semibold small">
                  <i class="bi bi-arrow-left me-1"></i>Quay lại đăng nhập
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { authApi } from '@/api/index'

const email = ref('')
const loading = ref(false)
const error = ref('')
const sent = ref(false)

const sendReset = async () => {
  loading.value = true; error.value = ''
  try {
    await authApi.forgotPassword(email.value)
    sent.value = true
  } catch (e) {
    error.value = e.response?.data?.message || 'Có lỗi xảy ra'
  } finally {
    loading.value = false
  }
}
</script>
