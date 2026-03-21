<template>
  <div>
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <h5 class="fw-bold mb-1">Quản lý người dùng</h5>
        <p class="text-muted small mb-0">{{ pagination.totalElements }} người dùng</p>
      </div>
    </div>

    <!-- Search -->
    <div class="card border-0 shadow-sm mb-4" style="border-radius:12px;">
      <div class="card-body p-3">
        <div class="input-group input-group-sm" style="max-width:320px;">
          <span class="input-group-text"><i class="bi bi-search"></i></span>
          <input type="text" class="form-control" v-model="search"
            placeholder="Tìm theo tên, email..." @input="debouncedSearch">
        </div>
      </div>
    </div>

    <div class="card border-0 shadow-sm" style="border-radius:16px;">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead style="background:#f8f9fa;">
              <tr>
                <th class="ps-4 py-3 small text-muted">Người dùng</th>
                <th class="py-3 small text-muted">Liên hệ</th>
                <th class="py-3 small text-muted">Vai trò</th>
                <th class="py-3 small text-muted">Ngày đăng ký</th>
                <th class="py-3 small text-muted">Trạng thái</th>
                <th class="pe-4 py-3 small text-muted text-end">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="6" class="text-center py-5">
                  <div class="spinner-border text-secondary"></div>
                </td>
              </tr>
              <tr v-else-if="!users.length">
                <td colspan="6" class="text-center py-5 text-muted">Không tìm thấy người dùng</td>
              </tr>
              <tr v-for="user in users" :key="user.id" v-else>
                <td class="ps-4">
                  <div class="d-flex align-items-center gap-3">
                    <div class="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white"
                      style="width:40px;height:40px;background:linear-gradient(135deg,#e91e8c,#ff6b9d);flex-shrink:0;">
                      {{ user.fullName?.charAt(0)?.toUpperCase() || user.username?.charAt(0)?.toUpperCase() }}
                    </div>
                    <div>
                      <div class="fw-semibold small">{{ user.fullName || user.username }}</div>
                      <small class="text-muted">@{{ user.username }}</small>
                    </div>
                  </div>
                </td>
                <td>
                  <div class="small">{{ user.email }}</div>
                  <small class="text-muted">{{ user.phone || '—' }}</small>
                </td>
                <td>
                  <span class="badge rounded-pill"
                    :class="user.role === 'ROLE_ADMIN' ? 'bg-warning text-dark' : 'bg-light text-dark'">
                    {{ user.role === 'ROLE_ADMIN' ? ' Admin' : ' User' }}
                  </span>
                </td>
                <td class="small text-muted">{{ formatDate(user.createdAt) }}</td>
                <td>
                  <span class="badge rounded-pill"
                    :class="user.isActive ? 'bg-success' : 'bg-danger'">
                    {{ user.isActive ? 'Hoạt động' : 'Đã khóa' }}
                  </span>
                </td>
                <td class="pe-4 text-end">
                  <button class="btn btn-sm rounded-pill"
                    :class="user.isActive ? 'btn-outline-danger' : 'btn-outline-success'"
                    :disabled="user.role === 'ROLE_ADMIN'"
                    @click="toggleStatus(user)">
                    {{ user.isActive ? 'Khóa' : 'Mở khóa' }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="pagination.totalPages > 1"
          class="d-flex justify-content-between align-items-center px-4 py-3 border-top">
          <small class="text-muted">Trang {{ currentPage + 1 }} / {{ pagination.totalPages }}</small>
          <nav>
            <ul class="pagination pagination-sm mb-0">
              <li :class="['page-item', { disabled: currentPage === 0 }]">
                <button class="page-link" @click="changePage(currentPage - 1)">
                  <i class="bi bi-chevron-left"></i>
                </button>
              </li>
              <li v-for="p in Math.min(pagination.totalPages, 5)" :key="p"
                :class="['page-item', { active: currentPage === p - 1 }]">
                <button class="page-link" @click="changePage(p - 1)">{{ p }}</button>
              </li>
              <li :class="['page-item', { disabled: pagination.last }]">
                <button class="page-link" @click="changePage(currentPage + 1)">
                  <i class="bi bi-chevron-right"></i>
                </button>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/api/index'
import { useToast } from 'vue-toastification'

const toast = useToast()
const users = ref([])
const loading = ref(false)
const search = ref('')
const currentPage = ref(0)
const pagination = ref({ totalPages: 0, totalElements: 0, last: true })

const formatDate = (d) => d ? new Date(d).toLocaleDateString('vi-VN') : '—'

let searchTimer = null
const debouncedSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { currentPage.value = 0; fetchData() }, 400)
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await userApi.admin.getUsers({ keyword: search.value || null, page: currentPage.value, size: 15 })
    const data = res.data.data
    users.value = data.content
    pagination.value = { totalPages: data.totalPages, totalElements: data.totalElements, last: data.last }
  } finally {
    loading.value = false
  }
}

const toggleStatus = async (user) => {
  try {
    await userApi.admin.toggleStatus(user.id)
    user.isActive = !user.isActive
    toast.success(user.isActive ? 'Đã mở khóa tài khoản' : 'Đã khóa tài khoản')
  } catch (e) {
    toast.error('Có lỗi xảy ra')
  }
}

const changePage = (page) => {
  if (page < 0 || page >= pagination.value.totalPages) return
  currentPage.value = page
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.page-item.active .page-link { background: #e91e8c; border-color: #e91e8c; }
.page-link { color: #e91e8c; }
</style>
