<!-- src/App.vue -->
<template>
  <div id="app-root" :data-theme="currentTheme">
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const currentTheme = ref('light')

onMounted(() => {
  // 监听全局事件，用于切换主题
  window.addEventListener('theme-changed', (e) => {
    currentTheme.value = e.detail
    localStorage.setItem('spark_theme', e.detail)
  })
  
  const savedTheme = localStorage.getItem('spark_theme')
  if (savedTheme) {
    currentTheme.value = savedTheme
  }
})
</script>

<style>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s ease, transform 0.4s ease;
}
.fade-enter-from {
  opacity: 0;
  transform: translateY(10px) scale(0.98);
}
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.98);
}
</style>