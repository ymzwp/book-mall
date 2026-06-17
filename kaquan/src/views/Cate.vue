<template>
  <div class="page">
    <div class="layout">
      <!-- 侧边栏：树形分类 -->
      <aside class="side">
        <div class="side-title">商品分类</div>
        <!-- 树形：一级→二级 -->
        <div v-for="l1 in cates" :key="l1.id" class="side-group">
          <div class="side-item side-l1" @click="toggleSide(l1.id)">
            <span class="si-arrow">{{ sideOpen[l1.id] ? '▼' : '▶' }}</span>
            <span class="si-icon">📁</span><span>{{ l1.cateName }}</span>
          </div>
          <div v-if="sideOpen[l1.id] && l1.children" class="side-children">
            <div v-for="l2 in l1.children" :key="l2.id"
                 class="side-item side-l2" :class="{ on: cid === l2.id }"
                 @click="pick(l2.id)">
              <img v-if="l2.icon" :src="resolveImg(l2.icon)" class="si-thumb" @error="$event.target.style.display='none'" />
              <span v-else class="si-icon">📄</span>
              <span>{{ l2.cateName }}</span>
            </div>
          </div>
        </div>
      </aside>

      <section class="main">
        <div class="main-head">
          <h3>{{ cid ? '当前分类' : '全部商品' }}</h3>
          <span class="count" v-if="!loading">{{ total }} 件</span>
        </div>
        <div v-if="loading" class="grid">
          <div v-for="n in 8" :key="n" class="skel-card">
            <div class="skel" style="padding-top:100%"></div>
            <div class="skel" style="height:14px;margin:10px 10px 6px;width:70%"></div>
            <div class="skel" style="height:18px;margin:0 10px 10px;width:40%"></div>
          </div>
        </div>
        <div v-else-if="list.length===0" class="empty"><span>📭</span><p>暂无商品</p></div>
        <div v-else class="grid">
          <div v-for="g in list" :key="g.id" class="card" @click="$router.push('/goodsDetail/'+g.id)">
            <div class="card-img">
              <img :src="getImg(g)" :alt="getName(g)" loading="lazy" />
              <span v-if="g.stock&&g.stock<=10" class="tag">库存紧张</span>
              <div class="card-hover"><span class="hover-btn">立即抢购</span></div>
            </div>
            <div class="card-info">
              <h3>{{ getName(g) }}</h3>
              <div class="card-row">
                <span class="card-price">¥{{ g.price }}</span>
                <span class="card-stock" v-if="g.stock">{{ g.stock }}件</span>
              </div>
            </div>
          </div>
        </div>
        <div v-if="hasMore" class="load-more">
          <button :disabled="loadingMore" @click="loadMore">{{ loadingMore ? '加载中...' : `加载更多 (${list.length}/${total})` }}</button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http, { resolveImg } from '@/utils/http'
import { fallbackImg } from '@/utils/placeholder'

const cates = ref([])
const list = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const cid = ref('')
const total = ref(0)
const page = ref(1)
const pageSize = 12
const hasMore = computed(() => list.value.length < total.value)
/** 侧边栏展开状态 */
const sideOpen = reactive({})

const getImg = (g) => resolveImg(g.goodsImg) || fallbackImg(g.id, 300, 300)
const getName = (g) => g.goodsName || g.info || '商品'

const loadCates = async () => { try { const r = await http.get('/cate/list'); cates.value = r.data || [] } catch {} }
const loadGoods = async () => {
  page.value = 1
  loading.value = true
  try {
    const r = await http.get('/goods/list', { params: { ...(cid.value ? { cid: cid.value } : {}), page: 1, pageSize } })
    list.value = r.data.list || []
    total.value = r.data.total || 0
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
}
const loadMore = async () => {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  try {
    page.value++
    const r = await http.get('/goods/list', { params: { ...(cid.value ? { cid: cid.value } : {}), page: page.value, pageSize } })
    list.value.push(...(r.data.list || []))
  } catch { ElMessage.error('加载失败') }
  finally { loadingMore.value = false }
}
const pick = (id) => { cid.value = id; loadGoods() }
const toggleSide = (id) => { sideOpen[id] = !sideOpen[id] }

onMounted(async () => {
  await loadCates()
  // 默认选中第一个有子分类的二级分类
  const firstL2 = cates.value.flatMap(l1 => l1.children || []).find(() => true)
  if (firstL2) pick(firstL2.id)
  else loadGoods()
})
</script>

<style scoped>
.page { max-width: 1400px; margin: 0 auto; padding: 20px 24px 40px; }
.layout { display: flex; gap: 18px; }

.side {
  width: 230px; flex-shrink: 0;
  background: #fff; border-radius: 16px;
  padding: 14px 0; border: 1px solid #e2e8f0;
  align-self: flex-start; position: sticky; top: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.side-title {
  font-size: 12px; font-weight: 700; color: #94a3b8;
  padding: 0 16px 12px; text-transform: uppercase; letter-spacing: 1px;
  border-bottom: 1px solid #e2e8f0; margin-bottom: 4px;
}
.side-item {
  display: flex; align-items: center; gap: 8px;
  padding: 11px 16px; cursor: pointer; font-size: 14px;
  color: #475569; transition: all 0.15s;
}
.si-arrow { font-size: 9px; color: #94a3b8; width: 12px; flex-shrink: 0; }
.si-icon { font-size: 16px; flex-shrink: 0; }
.si-thumb { width: 22px; height: 22px; border-radius: 4px; object-fit: cover; flex-shrink: 0; }
.side-item:hover { background: #f8fafc; color: #0f172a; }
.side-l1 { font-weight: 600; color: #0f172a; border-bottom: 1px solid #f8fafc; }
.side-children { background: #fafbfc; }
.side-l2 { padding-left: 42px; font-size: 13px; border-left: 3px solid transparent; }
.side-l2:hover { background: #f0f0f5; }
.side-l2.on { background: rgba(99,102,241,0.06); color: #6366f1; border-left-color: #6366f1; font-weight: 600; }

.main { flex: 1; min-width: 0; }
.main-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.main-head h3 { font-size: 19px; font-weight: 700; color: #0f172a; }
.count { font-size: 12px; color: #94a3b8; background: #f1f5f9; padding: 3px 10px; border-radius: 10px; }

.grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
@media (min-width: 1100px) { .grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 750px) { .grid { grid-template-columns: repeat(2, 1fr); gap: 10px; } }

.card {
  background: #fff; border-radius: 14px; overflow: hidden; cursor: pointer;
  border: 1px solid #e2e8f0; transition: all 0.3s;
}
.card:hover { transform: translateY(-4px); box-shadow: 0 12px 32px rgba(0,0,0,0.08); border-color: transparent; }
.card-img { position: relative; width: 100%; padding-top: 100%; background: #f8fafc; overflow: hidden; }
.card-img img { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; transition: transform 0.4s; }
.card:hover .card-img img { transform: scale(1.06); }
.tag { position:absolute; top:8px; right:8px; background:rgba(244,63,94,0.9); color:#fff; font-size:10px; padding:3px 8px; border-radius:6px; z-index:1; }
.card-hover { position:absolute; inset:0; background:rgba(0,0,0,0.35); display:flex; align-items:center; justify-content:center; opacity:0; transition:opacity 0.3s; }
.card:hover .card-hover { opacity:1; }
.hover-btn { padding:10px 22px; border-radius:10px; background:#fff; color:#6366f1; font-size:14px; font-weight:700; transform:translateY(8px); transition:transform 0.3s; }
.card:hover .hover-btn { transform:translateY(0); }
.card-info { padding: 12px 14px; }
.card-info h3 { font-size: 13px; font-weight: 600; color: #0f172a; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; margin-bottom:8px; }
.card-row { display:flex; justify-content:space-between; align-items:center; }
.card-price { font-size: 18px; font-weight: 800; color: #f43f5e; }
.card-stock { font-size: 11px; color: #94a3b8; }

.skel-card { background:#fff; border-radius:14px; overflow:hidden; border:1px solid #e2e8f0; }
.empty { text-align:center; padding:80px 0; color:#94a3b8; }
.empty span { font-size:48px; display:block; margin-bottom:12px; }

.load-more { text-align: center; margin-top: 20px; }
.load-more button {
  padding: 12px 40px; background: #fff; border: 2px solid #e2e8f0;
  border-radius: 12px; font-size: 14px; font-weight: 600; color: #6366f1;
  cursor: pointer; transition: all 0.2s;
}
.load-more button:hover { border-color: #6366f1; background: #f5f3ff; }
.load-more button:disabled { opacity: 0.5; cursor: not-allowed; }

@media (max-width: 768px) {
  .page { padding: 12px 10px 40px; }
  .layout { flex-direction: column; }
  .side { width: 100%; position: static; padding: 8px; border-radius: 14px; }
  .side-title { display: none; }
  .side-item { white-space: nowrap; padding: 8px 12px; font-size: 12px; border-radius: 10px; }
  .side-l2 { padding-left: 30px; }
}
</style>
