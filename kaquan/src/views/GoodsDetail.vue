<template>
  <div class="page">
    <div v-if="loading" class="skel-wrap">
      <div class="skel skel-big"></div>
      <div class="skel-info">
        <div class="skel" style="height:24px;width:60%"></div>
        <div class="skel" style="height:36px;width:30%;margin-top:12px"></div>
        <div class="skel" style="height:14px;width:90%;margin-top:20px"></div>
      </div>
    </div>

    <div v-else-if="!goods" class="empty-state">
      <div class="empty-icon">😕</div>
      <p>商品不存在</p>
    </div>

    <div v-else class="detail-wrap">
      <div class="main">
        <!-- 左：大图+描述+购买 -->
        <div class="info">
          <div class="img-box">
            <img :src="goodsImg" :alt="goodsName" />
          </div>

          <h2>{{ goodsName }}</h2>
          <div class="price-row">
            <span class="price">¥{{ goods.price }}</span>
            <span class="stock" v-if="goods.stock">库存 {{ goods.stock }} 件</span>
          </div>
          <div class="badges">
            <span class="b">官方直充</span>
            <span class="b b2">即时到账</span>
          </div>
          <div class="params">
            <div class="pi"><span class="pl">品牌</span><span class="pv">官方直充</span></div>
            <div class="pi"><span class="pl">类型</span><span class="pv">虚拟卡密</span></div>
            <div class="pi"><span class="pl">到账</span><span class="pv">即时到账</span></div>
          </div>

          <div class="qty">
            <span>数量</span>
            <el-input-number v-model="qty" :min="1" :max="goods.stock || 99" size="small" />
          </div>
          <div class="acc" :class="{ err: accErr }">
            <label>充值账号</label>
            <input v-model="acc" type="text" placeholder="请输入QQ号/手机号" @input="accErr = false" />
            <p v-if="accErr" class="err-msg">请填写充值账号</p>
          </div>
          <button class="buy" @click="buyNow">立即购买</button>
        </div>

        <!-- 右：描述 + 详情图片 -->
        <div class="detail-gallery">
          <div class="desc-box">
            <div class="desc-title">📝 商品描述</div>
            <div class="desc-text" v-html="goods.info || '官方直充，即时到账，品质保证'"></div>
          </div>
          <template v-if="detailImgs.length">
            <div class="detail-gallery-title">🖼️ 详情图片</div>
            <img v-for="(url, i) in detailImgs" :key="i" :src="url" class="detail-gallery-img" loading="lazy" />
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http, { resolveImg } from '@/utils/http'
import { fallbackImg } from '@/utils/placeholder'

const route = useRoute()
const router = useRouter()
const gid = route.params.id
const goods = ref(null)
const loading = ref(true)
const qty = ref(1)
const acc = ref('')
const accErr = ref(false)

const goodsImg = computed(() => {
  if (!goods.value) return ''
  return resolveImg(goods.value.goodsImg) || fallbackImg(goods.value.id, 400, 400)
})
const goodsName = computed(() => goods.value?.goodsName || goods.value?.info || '商品')
/** 详情图片列表 */
const detailImgs = computed(() => {
  if (!goods.value?.detailImgs) return []
  return goods.value.detailImgs.split(',').map(s => s.trim()).filter(Boolean)
})

onMounted(async () => {
  try {
    const r = await http.get('/goods/get', { params: { id: gid } })
    goods.value = r.data
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
})

const buyNow = () => {
  if (!acc.value.trim()) { accErr.value = true; return }
  const g = goods.value
  router.push({
    path: '/confirmOrder',
    query: {
      goodsId: g.id || gid,
      quantity: qty.value,
      accountNumber: acc.value
    }
  })
}
</script>

<style scoped>
.page { min-height: 100vh; padding: 24px 24px 40px; }
.detail-wrap { max-width: 900px; margin: 0 auto; }
.main {
  display: flex; flex-wrap: wrap; gap: 0;
  background: #fff; border-radius: 16px; overflow: hidden;
  box-shadow: var(--shadow-sm); border: 1px solid var(--border-card);
}
.info { flex: 1; min-width: 280px; display: flex; flex-direction: column; padding: 28px; }
.detail-gallery { width: 260px; flex-shrink: 0; padding: 28px 24px; border-left: 1px solid #f1f5f9; align-self: flex-start; position: sticky; top: 12px; max-height: calc(100vh - 24px); overflow-y: auto; }
.detail-gallery::-webkit-scrollbar { width: 4px; }
.detail-gallery::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 4px; }
.detail-gallery-title { font-size: 13px; font-weight: 700; color: #0f172a; margin-bottom: 10px; }
.detail-gallery-img { width: 100%; max-height: 300px; object-fit: contain; border-radius: 8px; margin-bottom: 8px; display: block; background: #f8fafc; border: 1px solid #f1f5f9; }
.detail-gallery-img:last-child { margin-bottom: 0; }

.img-box { border-radius: 14px; overflow: hidden; background: #f8fafc; margin-bottom: 16px; max-height: 260px; }
.img-box img { width: 100%; height: 260px; object-fit: contain; display: block; transition: transform 0.4s; }
.img-box:hover img { transform: scale(1.04); }

.badges { display: flex; gap: 6px; margin-bottom: 12px; }
.b { padding: 3px 10px; border-radius: 6px; font-size: 11px; font-weight: 600; background: rgba(99,102,241,0.08); color: var(--c-primary); }
.b2 { background: rgba(14,165,233,0.08); color: var(--c-sky); }
.info h2 { font-size: 22px; font-weight: 700; color: var(--text-primary); margin-bottom: 12px; }
.price-row { display: flex; align-items: baseline; gap: 14px; margin-bottom: 10px; }
.price { font-size: 34px; font-weight: 800; color: var(--c-accent); }
.stock { font-size: 13px; color: var(--text-muted); }
.desc-box { margin-bottom: 16px; padding-bottom: 16px; border-bottom: 1px solid #f1f5f9; }
.desc-title { font-size: 13px; font-weight: 700; color: #0f172a; margin-bottom: 6px; }
.desc-text { color: #475569; font-size: 13px; line-height: 1.8; margin: 0; }
.desc-text :deep(img) { max-width: 100%; border-radius: 8px; margin: 6px 0; display: block; }

.params { background: #f8fafc; border-radius: 12px; padding: 12px 16px; display: flex; flex-direction: column; gap: 6px; margin-bottom: 18px; }
.pi { display: flex; font-size: 13px; }
.pl { color: var(--text-muted); width: 48px; flex-shrink: 0; }
.pv { color: var(--text-secondary); font-weight: 500; }

.qty { display: flex; align-items: center; gap: 14px; margin-bottom: 16px; }
.qty > span { font-size: 14px; font-weight: 500; color: var(--text-primary); }

.acc { margin-bottom: 20px; }
.acc label { display: block; font-size: 14px; font-weight: 500; color: var(--text-primary); margin-bottom: 6px; }
.acc input {
  width: 100%; padding: 11px 14px; border: 1px solid var(--border-card);
  border-radius: 10px; font-size: 14px; outline: none;
  background: #f8fafc; color: var(--text-primary); transition: all 0.2s;
}
.acc input::placeholder { color: var(--text-muted); }
.acc input:focus { border-color: var(--c-primary); box-shadow: 0 0 0 3px rgba(99,102,241,0.06); }
.acc.err input { border-color: var(--c-accent); }
.err-msg { color: var(--c-accent); font-size: 12px; margin-top: 4px; }

.buy {
  width: 100%; padding: 14px; border: none; border-radius: 12px;
  background: var(--grad-primary); color: #fff;
  font-size: 16px; font-weight: 700; cursor: pointer; letter-spacing: 2px;
  transition: all 0.25s; margin-top: auto;
  box-shadow: 0 4px 14px rgba(99,102,241,0.2);
}
.buy:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(99,102,241,0.35); }
.buy:active { transform: translateY(0); }

.skel-wrap { max-width: 900px; margin: 0 auto; display: flex; flex-direction: column; gap: 20px; }
.skel-big { flex: 1; min-width: 280px; min-height: 300px; border-radius: 14px; }
.skel-info { flex: 1; min-width: 280px; padding-top: 10px; }
.skel-info .skel { background: #fff; border-radius: 20px; padding: 28px; }

.empty-state { text-align: center; padding: 100px 0; color: var(--text-muted); }
.empty-icon { font-size: 56px; margin-bottom: 16px; }

@media (max-width: 768px) {
  .page { padding: 14px 12px 40px; }
  .main { flex-direction: column; }
  .info { max-width: none; }
  .detail-gallery { width: 100%; position: static; border-left: none; border-top: 1px solid #f1f5f9; padding: 20px; }
  .info { padding: 18px; }
  .skel-wrap { flex-direction: column; }
  .info h2 { font-size: 18px; }
  .price { font-size: 28px; }
}
</style>
