<template>
  <div class="d-flex flex-column gap-4">
    <!-- Profile Card -->
    <div class="card border-0 shadow-sm" style="border-radius:16px;">
      <div class="card-body p-4">
        <h5 class="fw-bold mb-4"><i class="bi bi-person-circle me-2"></i>Thông tin cá nhân</h5>

        <div v-if="success" class="alert alert-success py-2 small mb-3">
          <i class="bi bi-check-circle me-1"></i>{{ success }}
        </div>
        <div v-if="error" class="alert alert-danger py-2 small mb-3">
          <i class="bi bi-exclamation-circle me-1"></i>{{ error }}
        </div>

        <form @submit.prevent="saveProfile">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label small fw-semibold">Tên đăng nhập</label>
              <input type="text" class="form-control" :value="authStore.user?.username" disabled
                style="background:#f8f9fa;">
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">Email</label>
              <input type="email" class="form-control" :value="authStore.user?.email" disabled
                style="background:#f8f9fa;">
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">Họ và tên</label>
              <input type="text" class="form-control" v-model="form.fullName" placeholder="Nguyễn Văn A">
            </div>
            <div class="col-md-6">
              <label class="form-label small fw-semibold">Số điện thoại</label>
              <input type="tel" class="form-control" v-model="form.phone" placeholder="0909 123 456">
            </div>
            <div class="col-12">
              <label class="form-label small fw-semibold">Địa chỉ chi tiết</label>
              <input type="text" class="form-control" v-model="form.addressStreet"
                placeholder="Số nhà, tên đường...">
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-semibold">Tỉnh / Thành phố</label>
              <input type="text" class="form-control" v-model="form.addressProvince">
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-semibold">Quận / Huyện</label>
              <input type="text" class="form-control" v-model="form.addressDistrict">
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-semibold">Phường / Xã</label>
              <input type="text" class="form-control" v-model="form.addressWard">
            </div>
          </div>

          <div class="d-flex gap-2 mt-4">
            <button type="submit" class="btn px-4"
              style="background:#e91e8c;color:#fff;border-radius:10px;"
              :disabled="loading">
              <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
              <i v-else class="bi bi-save me-1"></i>Lưu thay đổi
            </button>
            <button type="button" class="btn btn-outline-secondary px-4" style="border-radius:10px;"
              @click="loadProfile">
              Hủy
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useAuthStore } from '@/store/auth'
import { userApi } from '@/api/index'

const authStore = useAuthStore()
const loading = ref(false)
const success = ref('')
const error = ref('')

const form = reactive({
  fullName: '', phone: '', addressStreet: '',
  addressProvince: '', addressDistrict: '', addressWard: ''
})

const loadProfile = async () => {
  try {
    const res = await userApi.getProfile()
    const u = res.data.data
    Object.assign(form, {
      fullName: u.fullName || '',
      phone: u.phone || '',
      addressStreet: u.addressStreet || '',
      addressProvince: u.addressProvince || '',
      addressDistrict: u.addressDistrict || '',
      addressWard: u.addressWard || ''
    })
  } catch (e) {}
}

const saveProfile = async () => {
  loading.value = true
  success.value = ''
  error.value = ''
  try {
    await userApi.updateProfile(form)
    success.value = 'Cập nhật thông tin thành công!'
    // Update local auth state
    authStore.user.fullName = form.fullName
    localStorage.setItem('user', JSON.stringify(authStore.user))
  } catch (e) {
    error.value = e.response?.data?.message || 'Cập nhật thất bại'
  } finally {
    loading.value = false
  }
}

onMounted(loadProfile)
</script>
