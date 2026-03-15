<template>
  <div class="card border-0 shadow-sm" style="border-radius:16px;">
    <div class="card-body p-4">
      <div class="d-flex align-items-center justify-content-between mb-4">
        <h5 class="fw-bold mb-0"><i class="bi bi-bell me-2"></i>Thông báo</h5>
        <button v-if="notifications.length" class="btn btn-sm btn-outline-secondary rounded-pill"
          @click="markAllRead">
          <i class="bi bi-check-all me-1"></i>Đánh dấu đã đọc
        </button>
      </div>

      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border" style="color:#e91e8c;"></div>
      </div>

      <div v-else-if="!notifications.length" class="text-center py-5">
        <div style="font-size:4rem;">🔔</div>
        <h6 class="text-muted mt-3">Chưa có thông báo nào</h6>
      </div>

      <div v-else class="d-flex flex-column gap-2">
        <div v-for="noti in notifications" :key="noti.id"
          class="d-flex gap-3 p-3 rounded-3 cursor-pointer"
          :style="noti.isRead ? 'background:#fff' : 'background:#fff5f8;'"
          @click="readNotification(noti)">

          <!-- Icon -->
          <div class="rounded-circle d-flex align-items-center justify-content-center flex-shrink-0"
            style="width:42px;height:42px;"
            :style="`background:${typeColor(noti.type)}22`">
            <i :class="['bi', typeIcon(noti.type)]"
              :style="`color:${typeColor(noti.type)}`"></i>
          </div>

          <div class="flex-grow-1">
            <div class="d-flex justify-content-between align-items-start">
              <div class="fw-semibold small" :class="noti.isRead ? 'text-muted' : 'text-dark'">
                {{ noti.title }}
              </div>
              <div class="d-flex align-items-center gap-2">
                <small class="text-muted">{{ formatDate(noti.createdAt) }}</small>
                <span v-if="!noti.isRead" class="rounded-circle"
                  style="width:8px;height:8px;background:#e91e8c;display:inline-block;"></span>
              </div>
            </div>
            <p class="small mb-0" :class="noti.isRead ? 'text-muted' : 'text-secondary'">
              {{ noti.message }}
            </p>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <nav v-if="pagination.totalPages > 1" class="mt-4 d-flex justify-content-center">
        <ul class="pagination pagination-sm">
          <li :class="['page-item', { disabled: currentPage === 0 }]">
            <button class="page-link" @click="changePage(currentPage - 1)">
              <i class="bi bi-chevron-left"></i>
            </button>
          </li>
          <li v-for="p in pagination.totalPages" :key="p"
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
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/api/index'

const router = useRouter()
const notifications = ref([])
const loading = ref(false)
const currentPage = ref(0)
const pagination = ref({ totalPages: 0, last: true })

const typeIcon = (t) => ({ ORDER: 'bi-bag-check', PROMOTION: 'bi-ticket-perforated', RENTAL: 'bi-calendar-event', SYSTEM: 'bi-gear', NEW_PRODUCT: 'bi-stars' }[t] || 'bi-bell')
const typeColor = (t) => ({ ORDER: '#007bff', PROMOTION: '#e91e8c', RENTAL: '#17a2b8', SYSTEM: '#6c757d', NEW_PRODUCT: '#ffc107' }[t] || '#6c757d')
const formatDate = (d) => d ? new Date(d).toLocaleDateString('vi-VN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : ''

const fetchNotifications = async () => {
  loading.value = true
  try {
    const res = await userApi.getNotifications({ page: currentPage.value, size: 15 })
    const data = res.data.data
    notifications.value = data.content
    pagination.value = { totalPages: data.totalPages, last: data.last }
  } finally {
    loading.value = false
  }
}

const readNotification = async (noti) => {
  if (!noti.isRead) {
    noti.isRead = true
    await userApi.markNotificationRead(noti.id)
  }
  if (noti.linkUrl) router.push(noti.linkUrl)
}

const markAllRead = async () => {
  await userApi.markAllRead()
  notifications.value.forEach(n => n.isRead = true)
}

const changePage = (page) => {
  if (page < 0 || page >= pagination.value.totalPages) return
  currentPage.value = page
  fetchNotifications()
}

onMounted(fetchNotifications)
</script>

<style scoped>
.cursor-pointer { cursor: pointer; }
.page-item.active .page-link { background: #e91e8c; border-color: #e91e8c; }
.page-link { color: #e91e8c; }
</style>
