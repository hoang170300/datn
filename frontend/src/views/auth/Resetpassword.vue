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
                <div v-if="!done">
                  <h4 class="fw-bold mb-1">Đặt lại mật khẩu</h4>
                  <p class="text-muted mb-4">Nhập mật khẩu mới cho tài khoản của bạn</p>
   
                  <div v-if="error" class="alert alert-danger py-2 small">
                    <i class="bi bi-exclamation-circle me-1"></i>{{ error }}
                  </div>
   
                  <form @submit.prevent="handleReset">
                    <div class="mb-3">
                      <label class="form-label small fw-semibold">Mật khẩu mới</label>
                      <div class="input-group">
                        <input :type="show ? 'text' : 'password'" class="form-control"
                          v-model="password" placeholder="Ít nhất 6 ký tự" required minlength="6">
                        <button type="button" class="input-group-text" @click="show = !show">
                          <i :class="['bi', show ? 'bi-eye-slash' : 'bi-eye']"></i>
                        </button>
                      </div>
                    </div>
                    <div class="mb-4">
                      <label class="form-label small fw-semibold">Xác nhận mật khẩu</label>
                      <input type="password" class="form-control" v-model="confirm"
                        placeholder="Nhập lại mật khẩu" required>
                      <small v-if="confirm && password !== confirm" class="text-danger">
                        Mật khẩu không khớp
                      </small>
                    </div>
                    <button type="submit" class="btn w-100 py-3 fw-bold"
                      style="background:#e91e8c;color:#fff;border-radius:12px;"
                      :disabled="loading || (confirm && password !== confirm)">
                      <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                      Đặt lại mật khẩu
                    </button>
                  </form>
                </div>
   
                <div v-else class="text-center">
                  <div style="font-size:4rem;">✅</div>
                  <h5 class="fw-bold mt-3 mb-2">Thành công!</h5>
                  <p class="text-muted">Mật khẩu đã được đặt lại. Vui lòng đăng nhập lại.</p>
                  <router-link to="/login" class="btn rounded-pill px-4 mt-2"
                    style="background:#e91e8c;color:#fff;">
                    Đăng nhập ngay
                  </router-link>
                </div>
   
                <div class="text-center mt-4">
                  <router-link to="/login" style="color:#e91e8c;" class="small">
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
  import { useRoute } from 'vue-router'
  import { authApi } from '@/api/index'
   
  const route = useRoute()
  const password = ref('')
  const confirm = ref('')
  const loading = ref(false)
  const error = ref('')
  const done = ref(false)
  const show = ref(false)
   
  const handleReset = async () => {
    if (password.value !== confirm.value) return
    loading.value = true
    error.value = ''
    try {
      const token = route.query.token
      if (!token) { error.value = 'Token không hợp lệ'; return }
      await authApi.resetPassword(token, password.value)
      done.value = true
    } catch (e) {
      error.value = e.response?.data?.message || 'Token không hợp lệ hoặc đã hết hạn'
    } finally {
      loading.value = false
    }
  }
  </script>