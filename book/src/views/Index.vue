<template>
  <div class="page">
    <!-- 白色头部 -->
    <header class="top-bar">
      <div class="tb-left">
        <span class="tb-logo">🎫</span>
        <div>
          <h1>卡券商城</h1>
          <p>精选数字卡券 · 即买即用</p>
        </div>
      </div>
      <div class="tb-right">
        <div class="tb-cats">
          <span class="tb-cat" @click="$router.push('/cate')">🎫 视频</span>
          <span class="tb-cat" @click="$router.push('/cate')">🎁 游戏</span>
          <span class="tb-cat" @click="$router.push('/cate')">⚡ 生活</span>
          <span class="tb-cat" @click="$router.push('/cate')">💎 礼品</span>
        </div>
        <button class="btn-login" @click="$router.push('/login')">登录</button>
      </div>
    </header>

    <!-- 轮播 + 价格涨跌 -->
    <div class="hero-row">
      <div class="carousel" @mouseenter="pauseCarousel" @mouseleave="startCarousel">
      <div class="car-track">
        <div v-for="(s, i) in slides" :key="i" class="car-slide" :class="{ active: current === i }" @click="$router.push('/cate')">
          <img class="car-img" :src="s.img" :alt="s.title" />
          <div class="car-overlay"></div>
          <div class="car-content">
            <div><div class="car-title">{{ s.title }}</div><div class="car-desc">{{ s.desc }}</div></div>
            <span class="car-cta">{{ s.btn }} →</span>
          </div>
        </div>
      </div>
      <div class="car-dots">
        <span v-for="(_s, i) in slides" :key="i" class="car-dot" :class="{ on: current === i }" @click.stop="current = i"></span>
      </div>
    </div>
    <div class="trend-card">
        <div class="trend-head">
          <h3>📊 价格涨跌</h3>
          <span class="trend-sub">近7天均价变化</span>
        </div>
        <div class="trend-list" v-if="trendItems.length">
          <div class="trend-item" v-for="t in trendItems" :key="t.name">
            <span class="ti-name">{{ t.name }}</span>
            <span class="ti-price">¥{{ t.avg }}</span>
            <span class="ti-change" :class="t.up ? 'up' : 'down'">{{ t.up ? '+' : '' }}{{ t.change }}%</span>
          </div>
        </div>
        <div v-else class="trend-empty">暂无数据</div>
      </div>
    </div>

    <!-- 公告条 -->
    <div class="notice-bar">
      <span class="notice-icon">📢</span>
      <div class="notice-scroll">
        <span class="notice-text" :style="{ animationDuration: sd }">{{ nt }}</span>
      </div>
    </div>

    <!-- 商品区域 -->
    <div class="goods-head">
      <h2>全部商品</h2>
      <span class="goods-count">共 {{ total }} 件</span>
    </div>

    <div v-if="loading" class="grid">
      <div v-for="n in 10" :key="n" class="skel-card">
        <div class="skel" style="padding-top:100%"></div>
        <div class="skel" style="height:12px;margin:8px 8px 4px;width:70%"></div>
        <div class="skel" style="height:16px;margin:0 8px 8px;width:40%"></div>
      </div>
    </div>

    <div v-else-if="list.length===0" class="empty">
      <span>📭</span><p>暂无商品</p>
    </div>

    <div v-else class="grid">
      <div v-for="g in list" :key="g.id" class="card" @click="$router.push('/goodsDetail/'+g.id)">
        <div class="card-img">
          <img :src="getImg(g)" :alt="getName(g)" loading="lazy" />
          <span v-if="g.stock&&g.stock<=10" class="tag">库存紧张</span>
        </div>
        <div class="card-info">
          <h3>{{ getName(g) }}</h3>
          <div class="card-meta">
            <span class="card-price">¥{{ g.price }}</span>
            <span class="card-stock" v-if="g.stock">库存 {{ g.stock }}</span>
          </div>
        </div>
      </div>
    </div>
    <!-- 加载更多 -->
    <div v-if="hasMore" class="load-more">
      <button :disabled="loadingMore" @click="loadMore">{{ loadingMore ? '加载中...' : `加载更多 (${list.length}/${total})` }}</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import http, { resolveImg } from '@/utils/http'
import { fallbackImg } from '@/utils/placeholder'

const list = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const page = ref(1)
const total = ref(0)
const pageSize = 12
const hasMore = computed(() => list.value.length < total.value)

const getImg = (g) => resolveImg(g.goodsImg) || fallbackImg(g.id, 300, 300)
const getName = (g) => g.goodsName || g.info || '商品'

const loadGoods = async () => {
  page.value = 1
  loading.value = true
  try {
    const r = await http.get('/goods/list', { params: { page: 1, pageSize } })
    list.value = r.data.list || []
    total.value = r.data.total || 0
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

/** 加载更多 */
const loadMore = async () => {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  try {
    page.value++
    const r = await http.get('/goods/list', { params: { page: page.value, pageSize } })
    list.value.push(...(r.data.list || []))
    total.value = r.data.total || 0
  } catch { ElMessage.error('加载失败') }
  finally { loadingMore.value = false }
}

const current = ref(0)
let timer = null
const slides = [
  { img:'https://picsum.photos/seed/banner1/800/300', title:'限时特惠', desc:'会员卡券低至5折', btn:'立即抢购' },
  { img:'https://picsum.photos/seed/banner2/800/300', title:'新用户专享', desc:'注册即送10元优惠券', btn:'领取福利' },
  { img:'https://picsum.photos/seed/banner3/800/300', title:'热卖推荐', desc:'视频会员月卡19.9元起', btn:'去看看' },
]
// 替换上面的占位图片为你的广告图，例如: img:'/banner1.jpg' 或 img:'@/assets/banner1.png'
const startCarousel = () => { timer = setInterval(() => { current.value = (current.value + 1) % slides.length }, 3000) }
const pauseCarousel = () => clearInterval(timer)

const notes = ['新用户注册即送10元优惠券！','视频会员月卡限时特价，手慢无！','爱奇艺黄金会员买一送一进行中','签到积分可兑换5元无门槛券']
const nt = computed(() => notes.join('    ◆    '))
const sd = computed(() => Math.min(50, Math.max(20, nt.value.length/8)) + 's')

/** 价格涨跌：从后端获取各商品近7天均价变化 */
const trendItems = ref([])

const fetchTrend = async () => {
  try {
    const r = await http.get('/trend/price', { params: { days: 7 } })
    const d = r.data
    if (!d.series || !d.series.length) return
    trendItems.value = d.series.map(s => {
      const data = s.data.filter(v => v != null)
      const avg = data.length ? (data.reduce((a,b) => a+b, 0) / data.length).toFixed(2) : '0.00'
      const first = data[0]; const last = data[data.length-1]
      const change = first && last ? (((last - first) / first) * 100).toFixed(1) : '0.0'
      return { name: s.name, avg, change, up: parseFloat(change) >= 0 }
    })
  } catch { trendItems.value = [] }
}

onMounted(() => {
  startCarousel()
  loadGoods()
  fetchTrend()
})
onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.page { max-width: 1400px; margin: 0 auto; padding: 12px 20px 40px; }

/* ===== 白色头部 ===== */
.top-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px; margin-bottom: 10px;
  background: #fff; border-radius: 14px;
  border: 1px solid #e2e8f0;
}
.tb-left { display: flex; align-items: center; gap: 10px; }
.tb-logo { font-size: 28px; }
.tb-left h1 { font-size: 20px; font-weight: 800; color: #0f172a; margin: 0; line-height: 1.2; }
.tb-left p { font-size: 11px; color: #94a3b8; margin: 0; }
.tb-right { display: flex; align-items: center; gap: 12px; }
.tb-cats { display: flex; gap: 6px; }
.tb-cat {
  font-size: 12px; padding: 5px 10px; border-radius: 8px;
  background: #f1f5f9; color: #475569; cursor: pointer;
  transition: all 0.15s; white-space: nowrap;
}
.tb-cat:hover { background: rgba(99,102,241,0.1); color: #6366f1; }
.btn-login {
  padding: 7px 16px; border-radius: 10px; border: 1px solid #e2e8f0;
  background: #fff; color: #475569; font-size: 12px; font-weight: 600;
  cursor: pointer; transition: all 0.15s; white-space: nowrap;
}
.btn-login:hover { border-color: #6366f1; color: #6366f1; }

/* ===== 轮播 + 价格走势并排 ===== */
.hero-row { display: grid; grid-template-columns: 1.6fr 1fr; gap: 12px; margin-bottom: 10px; }

/* ===== 轮播 + 涨跌并排 ===== */
.hero-row { display: grid; grid-template-columns: 1.6fr 1fr; gap: 12px; margin-bottom: 10px; }

/* ===== 轮播 ===== */
.carousel {
  position: relative; border-radius: 14px; overflow: hidden;
  height: 320px; cursor: pointer;
}
.car-track { position: relative; width: 100%; height: 100%; }
.car-slide {
  position: absolute; inset: 0;
  opacity: 0; transition: opacity 0.5s ease;
  pointer-events: none;
}
.car-slide.active { opacity: 1; pointer-events: auto; }
.car-img {
  position: absolute; inset: 0; width: 100%; height: 100%;
  object-fit: cover; transition: transform 4s ease;
}
.car-slide.active .car-img { transform: scale(1.05); }
.car-overlay {
  position: absolute; inset: 0;
  background: linear-gradient(135deg, rgba(0,0,0,0.5) 0%, rgba(0,0,0,0.15) 60%);
}
.car-content {
  position: relative; z-index: 1; display: flex;
  align-items: center; gap: 20px; padding: 28px 32px;
  height: 100%; color: #fff;
}
.car-title { font-size: 26px; font-weight: 800; margin-bottom: 6px; text-shadow: 0 1px 3px rgba(0,0,0,0.2); }
.car-desc { font-size: 14px; opacity: 0.9; text-shadow: 0 1px 2px rgba(0,0,0,0.15); }
.car-cta {
  margin-left: auto; padding: 8px 20px; border-radius: 20px;
  background: rgba(255,255,255,0.22); font-size: 13px; font-weight: 600;
  backdrop-filter: blur(4px); white-space: nowrap; transition: background 0.2s;
}
.car-slide:hover .car-cta { background: rgba(255,255,255,0.38); }
.car-dots {
  position: absolute; bottom: 12px; left: 50%; transform: translateX(-50%);
  display: flex; gap: 6px; z-index: 2;
}
.car-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: rgba(255,255,255,0.45); cursor: pointer;
  transition: all 0.25s; box-shadow: 0 1px 3px rgba(0,0,0,0.15);
}
.car-dot.on { width: 22px; border-radius: 4px; background: #fff; }

/* ===== 公告条 ===== */
.notice-bar {
  display: flex; align-items: center; background: #fff;
  border-radius: 10px; padding: 0 12px; overflow: hidden;
  border: 1px solid #e2e8f0; height: 34px; margin-bottom: 12px;
}
.notice-icon { flex-shrink:0; padding-right: 12px; border-right: 1px solid #e2e8f0; font-size: 13px; }
.notice-scroll { flex:1; overflow:hidden; white-space:nowrap; margin-left: 12px; }
.notice-text { display:inline-block; white-space:nowrap; animation:scroll linear infinite; font-size:11px; color:#64748b; }
@keyframes scroll { 0%{transform:translateX(40%)} 100%{transform:translateX(-100%)} }

/* ===== 分类筛选（树形折叠面板） ===== */
.cate-section { display: flex; flex-direction: column; gap: 3px; margin-bottom: 14px; }
.cate-group { background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden; }
.cate-l1-row {
  display: flex; align-items: center; gap: 10px; padding: 13px 16px;
  cursor: pointer; transition: background 0.15s; user-select: none;
}
.cate-l1-row:hover { background: #fafbfc; }
.cate-arrow { font-size: 10px; color: #94a3b8; width: 16px; flex-shrink: 0; transition: transform 0.2s; }
.cate-l1-name { font-size: 14px; font-weight: 700; color: #0f172a; }
.cate-l1-count { font-size: 11px; color: #6366f1; background: rgba(99,102,241,0.08); padding: 2px 8px; border-radius: 8px; margin-left: auto; }
.cate-l2-row { display: flex; flex-wrap: wrap; gap: 8px; padding: 8px 16px 14px 42px; border-top: 1px solid #f1f5f9; background: #fafbfc; }
.cate-chip {
  padding: 7px 16px; border-radius: 20px;
  background: #fff; color: #475569; font-size: 13px; font-weight: 500;
  cursor: pointer; white-space: nowrap; border: 1px solid #e2e8f0;
  transition: all 0.2s;
}
.cate-chip:hover { border-color: #6366f1; color: #6366f1; background: #f5f3ff; }
.cate-chip.on { background: #6366f1; color: #fff; border-color: #6366f1; }
.cate-all { font-weight: 600; margin-bottom: 2px; }

/* ===== 价格涨跌 ===== */
.trend-card {
  background: #fff; border-radius: 14px; padding: 14px 18px;
  border: 1px solid #e2e8f0; display: flex; flex-direction: column;
  overflow: hidden;
}
.trend-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.trend-head h3 { font-size: 14px; font-weight: 700; color: #0f172a; margin: 0; }
.trend-sub { font-size: 11px; color: #94a3b8; }
.trend-list { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.trend-item {
  display: flex; align-items: center; gap: 10px;
  padding: 7px 0; border-bottom: 1px solid #f8fafc;
}
.trend-item:last-child { border-bottom: none; }
.ti-name { flex: 1; font-size: 13px; font-weight: 500; color: #334155; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ti-price { font-size: 14px; font-weight: 700; color: #0f172a; }
.ti-change { font-size: 12px; font-weight: 600; min-width: 50px; text-align: right; }
.ti-change.up { color: #ef4444; }
.ti-change.down { color: #10b981; }
.trend-empty { text-align: center; color: #94a3b8; padding: 20px 0; font-size: 13px; }

/* ===== 商品区域 ===== */
.goods-head {
  display: flex; justify-content: space-between; align-items: baseline;
  margin-bottom: 12px;
}
.goods-head h2 { font-size: 17px; font-weight: 700; color: #0f172a; margin: 0; }
.goods-count { font-size: 11px; color: #94a3b8; background: #f1f5f9; padding: 2px 8px; border-radius: 8px; }

/* ===== 商品网格 ===== */
.grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
}
@media (min-width: 1400px) { .grid { grid-template-columns: repeat(6, 1fr); } }
@media (max-width: 1100px) { .grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 750px) { .grid { grid-template-columns: repeat(3, 1fr); gap: 8px; } }
@media (max-width: 480px) { .grid { grid-template-columns: repeat(2, 1fr); gap: 6px; } }

.card {
  background: #fff; border-radius: 12px; overflow: hidden; cursor: pointer;
  border: 1px solid #e2e8f0; transition: all 0.25s;
}
.card:hover { transform: translateY(-3px); box-shadow: 0 8px 24px rgba(0,0,0,0.08); border-color: transparent; }
.card-img { position: relative; width: 100%; padding-top: 100%; background: #f8fafc; overflow: hidden; }
.card-img img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; transition: transform 0.35s; }
.card:hover .card-img img { transform: scale(1.05); }
.tag {
  position:absolute; top:6px; right:6px;
  background:rgba(244,63,94,0.92); color:#fff;
  font-size:9px; padding:2px 6px; border-radius:4px; font-weight:500; z-index:1;
}
.card-info { padding: 8px 10px; }
.card-info h3 {
  font-size: 12px; font-weight: 500; color: #0f172a;
  white-space:nowrap; overflow:hidden; text-overflow:ellipsis; margin-bottom:4px;
}
.card-meta { display:flex; justify-content:space-between; align-items:center; }
.card-price { font-size: 16px; font-weight: 800; color: #f43f5e; line-height: 1; }
.card-stock { font-size: 10px; color: #94a3b8; }

.skel-card { background:#fff; border-radius:12px; overflow:hidden; border:1px solid #e2e8f0; }
.empty { text-align:center; padding:80px 0; color:#94a3b8; }
.empty span { font-size:48px; display:block; margin-bottom:12px; }

/* 加载更多 */
.load-more { text-align: center; margin-top: 20px; }
.load-more button {
  padding: 12px 40px; background: #fff; border: 2px solid #e2e8f0;
  border-radius: 12px; font-size: 14px; font-weight: 600; color: #6366f1;
  cursor: pointer; transition: all 0.2s;
}
.load-more button:hover { border-color: #6366f1; background: #f5f3ff; }
.load-more button:disabled { opacity: 0.5; cursor: not-allowed; }

@media (max-width: 768px) {
  .page { padding: 10px 10px 40px; }
  .top-bar { flex-direction: column; gap: 10px; align-items: stretch; }
  .tb-right { justify-content: space-between; }
  .hero-row { grid-template-columns: 1fr; }
}
</style>
