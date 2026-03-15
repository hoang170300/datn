<!-- ChangePassword.vue -->
<template>
  <div class="card border-0 shadow-sm" style="border-radius:16px;">
    <div class="card-body p-4">
      <h5 class="fw-bold mb-4"><i class="bi bi-shield-lock me-2"></i>Đổi mật khẩu</h5>

      <div v-if="success" class="alert alert-success py-2 small"><i class="bi bi-check-circle me-1"></i>{{ success }}</div>
      <div v-if="error" class="alert alert-danger py-2 small"><i class="bi bi-exclamation-circle me-1"></i>{{ error }}</div>

      <form @submit.prevent="changePassword" style="max-width:420px;">
        <div class="mb-3">
          <label class="form-label small fw-semibold">Mật khẩu hiện tại</label>
          <div class="input-group">
            <input :type="show.old ? 'text' : 'password'" class="form-control" v-model="form.oldPassword" required>
            <button type="button" class="input-group-text" @click="show.old = !show.old">
              <i :class="['bi', show.old ? 'bi-eye-slash' : 'bi-eye']"></i>
            </button>
          </div>
        </div>
        <div class="mb-3">
          <label class="form-label small fw-semibold">Mật khẩu mới</label>
          <div class="input-group">
            <input :type="show.new1 ? 'text' : 'password'" class="form-control" v-model="form.newPassword"
              required minlength="6" placeholder="Ít nhất 6 ký tự">
            <button type="button" class="input-group-text" @click="show.new1 = !show.new1">
              <i :class="['bi', show.new1 ? 'bi-eye-slash' : 'bi-eye']"></i>
            </button>
          </div>
        </div>
        <div class="mb-4">
          <label class="form-label small fw-semibold">Xác nhận mật khẩu mới</label>
          <input type="password" class="form-control" v-model="confirmPassword"
            required placeholder="Nhập lại mật khẩu mới">
          <small v-if="confirmPassword && form.newPassword !== confirmPassword" class="text-danger">
            Mật khẩu không khớp
          </small>
        </div>
        <button type="submit" class="btn px-4"
          style="background:#e91e8c;color:#fff;border-radius:10px;"
          :disabled="loading || (confirmPassword && form.newPassword !== confirmPassword)">
          <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
          <i v-else class="bi bi-lock-fill me-1"></i>Đổi mật khẩu
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { userApi } from '@/api/index'

const form = reactive({ oldPassword: '', newPassword: '' })
const confirmPassword = ref('')
const loading = ref(false)
const success = ref('')
const error = ref('')
const show = reactive({ old: false, new1: false })

const changePassword = async () => {
  if (form.newPassword !== confirmPassword.value) return
  loading.value = true; success.value = ''; error.value = ''
  try {
    await userApi.changePassword(form)
    success.value = 'Đổi mật khẩu thành công!'
    form.oldPassword = ''; form.newPassword = ''; confirmPassword.value = ''
  } catch (e) {
    error.value = e.response?.data?.message || 'Đổi mật khẩu thất bại'
  } finally { loading.value = false }
}
</script>
