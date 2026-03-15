<template>
  <div id="app">
    <Navbar v-if="!isAdminRoute" />
    <router-view />
    <Footer v-if="!isAdminRoute" />
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import Navbar from '@/components/layout/Navbar.vue'
import Footer from '@/components/layout/Footer.vue'

// FIX: Không dùng useRoute() / useRouter() ở App.vue root
// vì router chưa được provide khi component này render lần đầu.
// Đọc path từ window.location và lắng nghe popstate thay thế.

const isAdminRoute = ref(window.location.pathname.startsWith('/admin'))

const updateRoute = () => {
  isAdminRoute.value = window.location.pathname.startsWith('/admin')
}

onMounted(() => {
  window.addEventListener('popstate', updateRoute)

  // Lắng nghe Vue Router navigation dùng MutationObserver trên URL
  // Cách đơn giản: override history.pushState
  const origPush = history.pushState.bind(history)
  history.pushState = function (...args) {
    origPush(...args)
    updateRoute()
  }
  const origReplace = history.replaceState.bind(history)
  history.replaceState = function (...args) {
    origReplace(...args)
    updateRoute()
  }
})
</script>

<style>
:root {
  --primary: #e91e8c;
  --primary-dark: #c0146e;
  --secondary: #6c757d;
  --dark: #1a1a2e;
  --card-shadow: 0 4px 20px rgba(0,0,0,0.08);
}
body {
  font-family: 'Segoe UI', sans-serif;
  background: #f8f9fa;
  color: #333;
}
.btn-primary { background: var(--primary); border-color: var(--primary); }
.btn-primary:hover { background: var(--primary-dark); border-color: var(--primary-dark); }
.text-primary { color: var(--primary) !important; }
.badge-hot { background: #ff4757; }
::-webkit-scrollbar { width: 6px; }
::-webkit-scrollbar-thumb { background: #ccc; border-radius: 4px; }
</style>
