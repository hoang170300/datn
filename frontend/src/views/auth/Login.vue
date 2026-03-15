<!-- =========================================
     Login.vue
     ========================================= -->
<template>
  <div class="min-vh-100 d-flex align-items-center py-5"
    style="background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);">
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-md-5">
          <div class="text-center mb-4">
            <router-link to="/" class="text-decoration-none">
              <h2 style="color:#e91e8c;font-weight:900;">⭐ CosPlay Shop</h2>
            </router-link>
          </div>
          <div class="card border-0 shadow-lg" style="border-radius:20px;overflow:hidden;">
            <div class="card-body p-5">
              <h4 class="fw-bold mb-1">Chào mừng trở lại!</h4>
              <p class="text-muted mb-4">Đăng nhập để tiếp tục mua sắm</p>

              <div v-if="error" class="alert alert-danger py-2 small">
                <i class="bi bi-exclamation-circle me-2"></i>{{ error }}
              </div>

              <form @submit.prevent="handleLogin">
                <div class="mb-3">
                  <label class="form-label fw-semibold small">Tên đăng nhập / Email</label>
                  <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-person"></i></span>
                    <input type="text" class="form-control" v-model="form.usernameOrEmail"
                      placeholder="username hoặc email" required>
                  </div>
                </div>

                <div class="mb-3">
                  <div class="d-flex justify-content-between">
                    <label class="form-label fw-semibold small">Mật khẩu</label>
                    <router-link to="/forgot-password" class="small" style="color:#e91e8c;">Quên mật khẩu?</router-link>
                  </div>
                  <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-lock"></i></span>
                    <input :type="showPass ? 'text' : 'password'" class="form-control"
                      v-model="form.password" placeholder="••••••••" required>
                    <button type="button" class="input-group-text" @click="showPass = !showPass">
                      <i :class="['bi', showPass ? 'bi-eye-slash' : 'bi-eye']"></i>
                    </button>
                  </div>
                </div>

                <button type="submit" class="btn w-100 py-3 fw-bold mt-2"
                  style="background:#e91e8c;color:#fff;border-radius:12px;"
                  :disabled="loading">
                  <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                  Đăng nhập
                </button>
              </form>

              <div class="text-center mt-4">
                <span class="text-muted small">Chưa có tài khoản? </span>
                <router-link to="/register" style="color:#e91e8c;" class="fw-semibold small">Đăng ký ngay</router-link>
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
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { useToast } from 'vue-toastification'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const toast = useToast()

const form = reactive({ usernameOrEmail: '', password: '' })
const loading = ref(false)
const error = ref('')
const showPass = ref(false)

const handleLogin = async () => {
  loading.value = true
  error.value = ''
  try {
    await authStore.login(form)
    toast.success('Đăng nhập thành công! 🎉')
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    error.value = e.response?.data?.message || 'Sai tên đăng nhập hoặc mật khẩu'
  } finally {
    loading.value = false
  }
}
</script>

<!-- =========================================
     Register.vue (separate file in real project)
     This is shown here for reference
     ========================================= -->
