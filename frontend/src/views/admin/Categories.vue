<template>
    <div>
      <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
          <h5 class="fw-bold mb-1">Quản lý Danh mục</h5>
          <p class="text-muted small mb-0">{{ categories.length }} danh mục</p>
        </div>
        <button class="btn rounded-pill" style="background:#e91e8c;color:#fff;" @click="openModal()">
          <i class="bi bi-plus-lg me-2"></i>Thêm danh mục
        </button>
      </div>
   
      <div class="card border-0 shadow-sm" style="border-radius:16px;">
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead style="background:#f8f9fa;">
                <tr>
                  <th class="ps-4 py-3 small text-muted">Danh mục</th>
                  <th class="py-3 small text-muted">Slug</th>
                  <th class="py-3 small text-muted">Trạng thái</th>
                  <th class="pe-4 py-3 small text-muted text-end">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="loading"><td colspan="4" class="text-center py-5"><div class="spinner-border text-secondary"></div></td></tr>
                <tr v-else-if="!categories.length"><td colspan="4" class="text-center py-5 text-muted">Chưa có danh mục nào</td></tr>
                <tr v-for="cat in categories" :key="cat.id" v-else>
                  <td class="ps-4">
                    <div class="d-flex align-items-center gap-3">
                      <img v-if="cat.imageUrl" :src="cat.imageUrl"
                        class="rounded-circle" style="width:40px;height:40px;object-fit:cover;"
                        @error="$event.target.style.display='none'">
                      <div v-else class="rounded-circle d-flex align-items-center justify-content-center"
                        style="width:40px;height:40px;background:#e91e8c22;color:#e91e8c;font-weight:700;">
                        {{ cat.name?.charAt(0) }}
                      </div>
                      <div>
                        <div class="fw-semibold small">{{ cat.name }}</div>
                        <small class="text-muted">{{ cat.description || '—' }}</small>
                      </div>
                    </div>
                  </td>
                  <td><code class="small">{{ cat.slug }}</code></td>
                  <td>
                    <div class="form-check form-switch mb-0">
                      <input class="form-check-input" type="checkbox"
                        :checked="cat.isActive" @change="toggle(cat)">
                    </div>
                  </td>
                  <td class="pe-4 text-end">
                    <button class="btn btn-sm btn-outline-primary rounded-pill"
                      @click="openModal(cat)">
                      <i class="bi bi-pencil me-1"></i>Sửa
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
   
      <!-- Modal -->
      <Teleport to="body">
        <div v-if="showModal" class="position-fixed d-flex align-items-center justify-content-center"
          style="inset:0;background:rgba(0,0,0,0.5);z-index:1055;padding:16px;"
          @click.self="showModal=false">
          <div class="card border-0 shadow-lg" style="width:100%;max-width:480px;border-radius:20px;">
            <div class="card-body p-5">
              <div class="d-flex justify-content-between mb-4">
                <h5 class="fw-bold mb-0">{{ editing ? 'Sửa danh mục' : 'Thêm danh mục' }}</h5>
                <button type="button" class="btn-close" @click="showModal=false"></button>
              </div>
              <form @submit.prevent="save">
                <div class="mb-3">
                  <label class="form-label small fw-semibold">Tên danh mục *</label>
                  <input class="form-control" v-model="form.name" required @input="autoSlug">
                </div>
                <div class="mb-3">
                  <label class="form-label small fw-semibold">Slug *</label>
                  <input class="form-control" v-model="form.slug" required>
                </div>
                <div class="mb-3">
                  <label class="form-label small fw-semibold">Mô tả</label>
                  <textarea class="form-control" v-model="form.description" rows="2"></textarea>
                </div>
                <div class="mb-4">
                  <label class="form-label small fw-semibold">URL ảnh đại diện</label>
                  <input class="form-control" v-model="form.imageUrl" placeholder="https://...">
                </div>
                <div class="d-flex gap-2">
                  <button type="submit" class="btn flex-grow-1" style="background:#e91e8c;color:#fff;border-radius:10px;" :disabled="saving">
                    <span v-if="saving" class="spinner-border spinner-border-sm me-2"></span>
                    {{ editing ? 'Lưu' : 'Tạo' }}
                  </button>
                  <button type="button" class="btn btn-outline-secondary" style="border-radius:10px;" @click="showModal=false">Hủy</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </Teleport>
    </div>
  </template>
   
  <script setup>
  import { ref, reactive, onMounted } from 'vue'
  import { productApi } from '@/api/index'
  import { useToast } from 'vue-toastification'
   
  const toast = useToast()
  const categories = ref([])
  const loading = ref(false)
  const showModal = ref(false)
  const editing = ref(null)
  const saving = ref(false)
  const form = reactive({ name: '', slug: '', description: '', imageUrl: '' })
   
  const autoSlug = () => {
    if (editing.value) return
    form.slug = form.name.toLowerCase()
      .normalize('NFD').replace(/[\u0300-\u036f]/g,'')
      .replace(/đ/g,'d').replace(/[^a-z0-9\s-]/g,'')
      .trim().replace(/\s+/g,'-')
  }
   
  const fetchData = async () => {
    loading.value = true
    try {
      const res = await productApi.admin.getCategories()
      categories.value = res.data.data || []
    } finally { loading.value = false }
  }
   
  const openModal = (cat = null) => {
    editing.value = cat
    Object.assign(form, cat
      ? { name: cat.name, slug: cat.slug, description: cat.description || '', imageUrl: cat.imageUrl || '' }
      : { name: '', slug: '', description: '', imageUrl: '' })
    showModal.value = true
  }
   
  const save = async () => {
    saving.value = true
    try {
      if (editing.value) {
        await productApi.admin.updateCategory(editing.value.id, form)
        toast.success('Đã cập nhật danh mục')
      } else {
        await productApi.admin.createCategory(form)
        toast.success('Đã tạo danh mục mới')
      }
      showModal.value = false
      fetchData()
    } catch (e) {
      toast.error(e.response?.data?.message || 'Có lỗi xảy ra')
    } finally { saving.value = false }
  }
   
  const toggle = async (cat) => {
    try {
      await productApi.admin.toggleCategory(cat.id)
      cat.isActive = !cat.isActive
      toast.success(cat.isActive ? 'Đã kích hoạt' : 'Đã ẩn danh mục')
    } catch (e) { toast.error('Có lỗi xảy ra') }
  }
   
  onMounted(fetchData)
  </script>