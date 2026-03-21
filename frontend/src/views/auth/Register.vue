<template>
  <div class="min-vh-100 d-flex align-items-center py-5"
    style="background: linear-gradient(135deg, #1a1a2e, #16213e, #0f3460);">
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-md-6">
          <div class="text-center mb-4">
            <router-link to="/" class="text-decoration-none">
              <h2 style="color:#e91e8c;font-weight:900;">OtakuCosPlay</h2>
            </router-link>
          </div>
          <div class="card border-0 shadow-lg" style="border-radius:20px;">
            <div class="card-body p-5">
              <h4 class="fw-bold mb-1">Tạo tài khoản mới</h4>
              <p class="text-muted mb-4">Đăng ký để trải nghiệm cosplay đỉnh cao</p>

              <div v-if="error" class="alert alert-danger py-2 small">
                <i class="bi bi-exclamation-circle me-2"></i>{{ error }}
              </div>
              <div v-if="success" class="alert alert-success py-2 small">
                <i class="bi bi-check-circle me-2"></i>{{ success }}
              </div>

              <form @submit.prevent="handleRegister">
                <div class="row g-3">
                  <div class="col-12">
                    <label class="form-label fw-semibold small">Tên đăng nhập *</label>
                    <div class="input-group">
                      <span class="input-group-text"><i class="bi bi-at"></i></span>
                      <input type="text" class="form-control" v-model="form.username"
                        placeholder="username (3-50 ký tự)" required minlength="3">
                    </div>
                  </div>

                  <div class="col-12">
                    <label class="form-label fw-semibold small">Email *</label>
                    <div class="input-group">
                      <span class="input-group-text"><i class="bi bi-envelope"></i></span>
                      <input type="email" class="form-control" v-model="form.email"
                        placeholder="email@example.com" required>
                    </div>
                  </div>

                  <div class="col-12">
                    <label class="form-label fw-semibold small">Họ và tên</label>
                    <div class="input-group">
                      <span class="input-group-text"><i class="bi bi-person"></i></span>
                      <input type="text" class="form-control" v-model="form.fullName"
                        placeholder="Nguyễn Văn A">
                    </div>
                  </div>

                  <div class="col-12">
                    <label class="form-label fw-semibold small">Số điện thoại</label>
                    <div class="input-group">
                      <span class="input-group-text"><i class="bi bi-phone"></i></span>
                      <input type="tel" class="form-control" v-model="form.phone"
                        placeholder="0909 123 456" pattern="^[0-9]{10,11}$">
                    </div>
                  </div>

                  <div class="col-12">
                    <label class="form-label fw-semibold small">Mật khẩu *</label>
                    <div class="input-group">
                      <span class="input-group-text"><i class="bi bi-lock"></i></span>
                      <input :type="showPass ? 'text' : 'password'" class="form-control"
                        v-model="form.password" placeholder="Ít nhất 6 ký tự" required minlength="6">
                      <button type="button" class="input-group-text" @click="showPass = !showPass">
                        <i :class="['bi', showPass ? 'bi-eye-slash' : 'bi-eye']"></i>
                      </button>
                    </div>
                  </div>

                  <div class="col-12">
                    <label class="form-label fw-semibold small">Xác nhận mật khẩu *</label>
                    <div class="input-group">
                      <span class="input-group-text"><i class="bi bi-lock-fill"></i></span>
                      <input type="password" class="form-control" v-model="confirmPassword"
                        placeholder="Nhập lại mật khẩu" required>
                    </div>
                    <small v-if="confirmPassword && form.password !== confirmPassword" class="text-danger">
                      Mật khẩu không khớp
                    </small>
                  </div>
                </div>

                <button type="submit" class="btn w-100 py-3 fw-bold mt-4"
                  style="background:#e91e8c;color:#fff;border-radius:12px;"
                  :disabled="loading || (confirmPassword && form.password !== confirmPassword)">
                  <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                  Đăng ký ngay
                </button>
              </form>

              <div class="text-center mt-4">
                <span class="text-muted small">Đã có tài khoản? </span>
                <router-link to="/login" style="color:#e91e8c;" class="fw-semibold small">Đăng nhập</router-link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()
const form = reactive({ username: '', email: '', password: '', fullName: '', phone: '' })
const confirmPassword = ref('')
const loading = ref(false)
const error = ref('')
const success = ref('')
const showPass = ref(false)

const handleRegister = async () => {
  if (form.password !== confirmPassword.value) return
  loading.value = true
  error.value = ''
  try {
    await authStore.register(form)
    success.value = 'Đăng ký thành công! Đang chuyển đến trang đăng nhập...'
    setTimeout(() => router.push('/login'), 2000)
  } catch (e) {
    error.value = e.response?.data?.message || 'Đăng ký thất bại'
  } finally {
    loading.value = false
  }
}
</script>
