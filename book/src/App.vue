<template>
  <div class="app-shell">
    <main :class="{ 'with-nav': showNav }">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 底部悬浮导航 -->
    <nav v-if="showNav" class="bottom-nav">
      <div class="bn-inner">
        <router-link v-for="item in navs" :key="item.path" :to="item.path" class="bn-item" active-class="bn-active">
          <span class="bn-icon">{{ item.icon }}</span>
          <span class="bn-label">{{ item.name }}</span>
        </router-link>
      </div>
    </nav>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const navs = [
  { name: '首页', path: '/index', icon: '🏠' },
  { name: '分类', path: '/cate', icon: '🏷️' },
  { name: '订单', path: '/order', icon: '📋' },
  { name: '我的', path: '/mine', icon: '👤' }
]

const showNav = computed(() => {
  const hide = ['/admin', '/adminLogin', '/login']
  if (hide.includes(route.path)) return false
  if (route.path.startsWith('/goodsDetail')) return false
  if (route.path.startsWith('/confirmOrder')) return false
  if (route.path === '/recharge') return false
  return true
})
</script>

<style>
* { margin:0; padding:0; box-sizing:border-box; }
body {
  background: var(--bg-page);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.app-shell { min-height: 100vh; display: flex; flex-direction: column; }
main.with-nav { padding-bottom: 90px; }
main:not(.with-nav) { padding-bottom: 0; }

/* 底部悬浮导航 */
.bottom-nav {
  position: fixed; bottom: 16px; left: 50%; transform: translateX(-50%);
  z-index: 100;
}
.bn-inner {
  display: flex; gap: 4px;
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 6px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.08), 0 1px 0 rgba(0,0,0,0.04);
  border: 1px solid rgba(0,0,0,0.06);
}
.bn-item {
  display: flex; flex-direction: column; align-items: center; gap: 2px;
  padding: 8px 20px; border-radius: 16px;
  text-decoration: none; color: var(--text-muted);
  transition: all 0.25s; min-width: 64px; position: relative;
}
.bn-item:hover { color: var(--text-secondary); }
.bn-item.bn-active {
  color: var(--c-primary);
  background: rgba(99,102,241,0.08);
}
.bn-icon { font-size: 20px; line-height: 1; }
.bn-label { font-size: 10px; font-weight: 700; letter-spacing: 0.5px; }
</style>
