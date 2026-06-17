<template>
  <div class="page">
    <div class="wrap">
      <!-- 头部 -->
      <div class="head">
        <h1>我的订单</h1>
        <div class="search">
          <span class="sic">🔍</span>
          <input v-model="kw" type="text" placeholder="搜索订单号或商品名" />
          <span v-if="kw" class="scl" @click="kw = ''">✕</span>
        </div>
      </div>

      <!-- 状态筛选 -->
      <div class="tabs">
        <button v-for="t in tabs" :key="t.v" class="tb" :class="{ on: st === t.v }" @click="st = t.v">
          {{ t.l }}
          <span v-if="t.v==='' && list.length" class="tb-num">{{ list.length }}</span>
        </button>
      </div>

      <!-- 加载 -->
      <div v-if="loading" class="skeleton">
        <div v-for="n in 3" :key="n" class="skel-card">
          <div class="skel-row">
            <div class="skel" style="width:40%;height:14px"></div>
            <div class="skel" style="width:60px;height:20px;border-radius:6px"></div>
          </div>
          <div class="skel-row" style="padding:14px 16px">
            <div class="skel" style="width:56px;height:56px;border-radius:10px"></div>
            <div style="flex:1">
              <div class="skel" style="width:70%;height:14px;margin-bottom:6px"></div>
              <div class="skel" style="width:40%;height:12px"></div>
            </div>
          </div>
          <div class="skel-row">
            <div class="skel" style="width:30%;height:12px"></div>
            <div class="skel" style="width:80px;height:28px;border-radius:8px"></div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="filtered.length===0" class="empty">
        <span>📭</span>
        <p>{{ kw ? '未找到匹配的订单' : '暂无订单' }}</p>
        <button v-if="!kw" class="go-shop" @click="$router.push('/cate')">去逛逛</button>
      </div>

      <!-- 订单列表 -->
      <div v-else class="list">
        <div v-for="o in filtered" :key="o.id" class="ocard" :class="'status-'+o.status">
          <div class="oc-top">
            <span class="oc-no">#{{ o.id }}</span>
            <span class="oc-time">{{ fmt(o.createTime) }} · {{ payLabel(o.payMethod) }}</span>
            <span class="oc-st" :class="'st-'+o.status">{{ stxt(o.status) }}</span>
          </div>
          <div class="oc-body" @click="showDetail(o)">
            <div class="oc-thumb">
              <img :src="getImg(o)" :alt="getName(o)" />
            </div>
            <div class="oc-info">
              <div class="oc-name">{{ getName(o) }}</div>
              <div class="oc-acc" v-if="o.accountNumber">账号：{{ o.accountNumber }}</div>
              <div class="oc-price">¥{{ o.orderPrice }}</div>
            </div>
          </div>
          <div class="oc-foot">
            <button class="oc-btn" @click="showDetail(o)">查看详情</button>
            <button v-if="o.status==='completed'" class="oc-btn oc-btn-primary" @click="doRefund(o)">退款</button>
            <button v-if="o.status==='pending'" class="oc-btn oc-btn-primary" @click="$router.push('/confirmOrder?id='+o.id)">去付款</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="showDia" title="订单详情" width="420px" :close-on-click-modal="false" center>
      <template v-if="detail">
        <div class="detail-grid">
          <div class="dg-item"><span class="dg-label">订单编号</span><span class="dg-val">#{{ detail.id }}</span></div>
          <div class="dg-item"><span class="dg-label">商品名称</span><span class="dg-val">{{ getName(detail) }}</span></div>
          <div class="dg-item"><span class="dg-label">充值账号</span><span class="dg-val">{{ detail.accountNumber || '-' }}</span></div>
          <div class="dg-item"><span class="dg-label">支付方式</span><span class="dg-val">{{ payLabel(detail.payMethod) }}</span></div>
          <div class="dg-item"><span class="dg-label">订单金额</span><span class="dg-val" style="color:#f43f5e;font-weight:700;">¥{{ detail.orderPrice }}</span></div>
          <div class="dg-item"><span class="dg-label">下单时间</span><span class="dg-val">{{ fmt(detail.createTime) }}</span></div>
          <div class="dg-item"><span class="dg-label">订单状态</span><span class="dg-val" :style="{ color: detail.status==='completed'?'#10b981':'#f59e0b' }">{{ stxt(detail.status) }}</span></div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http, { resolveImg } from '@/utils/http'
import { fallbackImg } from '@/utils/placeholder'

const tabs = [
  { l: '全部', v: '' },
  { l: '待付款', v: 'pending' },
  { l: '进行中', v: 'shipped' },
  { l: '已完成', v: 'completed' },
  { l: '已退款', v: 'refunded' },
  { l: '已取消', v: 'cancelled' }
]
const st = ref('')
const kw = ref('')
const list = ref([])
const loading = ref(true)
/** 订单详情弹窗 */
const showDia = ref(false)
const detail = ref(null)

const getImg = (o) => resolveImg(o.goodsImg) || fallbackImg(o.id, 80, 80)
const getName = (o) => o.goodsName || '商品'
/** 支付方式中文映射 */
const payLabel = (m) => ({ alipay:'支付宝', wechat:'微信支付', balance:'余额支付' }[m] || m || '未知')

const stxt = (s) => ({ pending: '待付款', shipped: '进行中', completed: '已完成', cancelled: '已取消', refunded: '已退款' }[s] || '已完成')
const fmt = (s) => s ? s.replace('T', ' ').substring(0, 16) : ''

const filtered = computed(() => {
  let arr = list.value
  if (st.value) arr = arr.filter(o => o.status === st.value)
  if (kw.value.trim()) {
    const w = kw.value.trim().toLowerCase()
    arr = arr.filter(o => String(o.id).includes(w) || (o.goodsName && o.goodsName.toLowerCase().includes(w)))
  }
  return arr
})

/** 显示订单详情弹窗 */
const showDetail = (o) => { detail.value = o; showDia.value = true }

onMounted(async () => {
  try {
    const r = await http.post('/order/my')
    let orders = r.data.data || []
    for (let o of orders) {
      if (o.goodsId) {
        try { const g = await http.get('/goods/get', { params: { id: o.goodsId } }); o.goodsName = g.data.goodsName || g.data.info || '商品'; o.goodsImg = g.data.goodsImg || '' } catch { o.goodsName = '已下架'; o.goodsImg = '' }
      }
      o.status = o.status || 'completed'
    }
    list.value = orders
  } catch { ElMessage.error('加载失败') }
  finally { loading.value = false }
})

/** 退款（密码确认 + 订单详情） */
const doRefund = async (o) => {
  let pwd = ''
  try {
    const { value } = await ElMessageBox.prompt(
      `【${getName(o)}】· ${payLabel(o.payMethod)}\n金额：¥${o.orderPrice}\n账号：${o.accountNumber || '-'}\n\n退款将退还到账户余额，请输入登录密码确认`,
      '退款确认',
      { confirmButtonText: '确认退款', cancelButtonText: '取消', inputType: 'password', inputPlaceholder: '请输入密码',
        inputValidator: (v) => v ? true : '密码不能为空' }
    )
    pwd = value
  } catch { return }
  try {
    const r = await http.post('/order/refund', { id: o.id, password: pwd })
    if (r.data.code === 200) {
      ElMessage.success(r.data.msg || '退款成功')
      o.status = 'refunded'
    } else {
      ElMessage.error(r.data.msg || '退款失败')
    }
  } catch { ElMessage.error('网络错误') }
}
</script>

<style scoped>
.page { min-height: 100vh; }
.wrap { max-width: 800px; margin: 0 auto; padding: 20px 20px 40px; }

/* 头部 */
.head { display: flex; justify-content: space-between; align-items: center; gap: 14px; margin-bottom: 16px; flex-wrap: wrap; }
.head h1 { font-size: 22px; font-weight: 800; color: #0f172a; margin: 0; }
.search { position: relative; width: 260px; }
.sic { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); font-size: 14px; color: #94a3b8; z-index: 1; }
.scl {
  position: absolute; right: 12px; top: 50%; transform: translateY(-50%);
  font-size: 12px; color: #94a3b8; cursor: pointer; padding: 2px 6px; border-radius: 50%;
  transition: all 0.15s;
}
.scl:hover { background: #f1f5f9; color: #475569; }
.search input {
  width: 100%; padding: 9px 34px 9px 36px; border: 1px solid #e2e8f0;
  border-radius: 10px; font-size: 13px; outline: none; background: #fff;
  color: #0f172a; transition: all 0.2s;
}
.search input::placeholder { color: #94a3b8; }
.search input:focus { border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99,102,241,0.06); }

/* 标签 */
.tabs { display: flex; gap: 8px; margin-bottom: 20px; }
.tb {
  padding: 7px 16px; border-radius: 20px; border: 1px solid #e2e8f0;
  font-size: 13px; font-weight: 500; cursor: pointer;
  background: #fff; color: #475569; transition: all 0.2s;
  display: flex; align-items: center; gap: 6px;
}
.tb:hover { border-color: #6366f1; color: #6366f1; }
.tb.on { background: #6366f1; color: #fff; border-color: #6366f1; }
.tb-num { font-size: 10px; background: rgba(255,255,255,0.25); padding: 1px 6px; border-radius: 8px; }

/* 订单卡片 */
.list { display: flex; flex-direction: column; gap: 10px; }
.ocard {
  background: #fff; border-radius: 14px; overflow: hidden;
  border: 1px solid #e2e8f0; transition: all 0.25s;
}
.ocard:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.05); }
.oc-top {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 16px; background: #f8fafc; border-bottom: 1px solid #f1f5f9;
}
.oc-no { font-size: 12px; color: #94a3b8; font-family: monospace; }
.oc-time { font-size: 11px; color: #94a3b8; flex: 1; }
.oc-st { font-size: 10px; padding: 3px 10px; border-radius: 10px; font-weight: 600; }
.st-completed { background: rgba(16,185,129,0.08); color: #10b981; }
.st-pending { background: rgba(245,158,11,0.08); color: #f59e0b; }
.st-shipped { background: rgba(59,130,246,0.08); color: #3b82f6; }
.st-cancelled { background: rgba(244,63,94,0.08); color: #f43f5e; }
.st-refunded { background: rgba(148,163,184,0.1); color: #64748b; }

.oc-body { display: flex; gap: 14px; padding: 14px 16px; cursor: pointer; transition: background 0.15s; }
.oc-body:hover { background: #fafafa; }
.oc-thumb { width: 64px; height: 64px; border-radius: 10px; overflow: hidden; background: #f1f5f9; flex-shrink: 0; }
.oc-thumb img { width: 100%; height: 100%; object-fit: cover; }
.oc-info { flex: 1; min-width: 0; display: flex; flex-direction: column; justify-content: center; }
.oc-name { font-size: 14px; font-weight: 600; color: #0f172a; margin-bottom: 4px;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.oc-acc { font-size: 11px; color: #94a3b8; margin-bottom: 4px; }
.oc-price { font-size: 18px; font-weight: 800; color: #f43f5e; }

.oc-foot { display: flex; justify-content: flex-end; gap: 8px; padding: 10px 16px; border-top: 1px solid #f1f5f9; }
.oc-btn {
  padding: 6px 14px; border-radius: 8px; border: 1px solid #e2e8f0;
  background: #fff; font-size: 12px; cursor: pointer;
  color: #475569; transition: all 0.2s;
}
.oc-btn:hover { border-color: #6366f1; color: #6366f1; }
.oc-btn-primary { background: #6366f1; color: #fff; border-color: #6366f1; }
.oc-btn-primary:hover { background: #4f46e5; color: #fff; }

/* 详情弹窗 */
.detail-grid { display: flex; flex-direction: column; gap: 2px; }
.dg-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 14px; border-radius: 10px; background: #f8fafc;
}
.dg-label { font-size: 13px; color: #94a3b8; }
.dg-val { font-size: 14px; font-weight: 600; color: #0f172a; }

/* 骨架 */
.skeleton { display: flex; flex-direction: column; gap: 10px; }
.skel-card { background: #fff; border-radius: 14px; overflow: hidden; border: 1px solid #e2e8f0; }
.skel-row { display: flex; align-items: center; gap: 12px; padding: 10px 16px; border-bottom: 1px solid #f1f5f9; }
.skel-row:last-child { border-bottom: none; }

/* 空状态 */
.empty { text-align: center; padding: 80px 0; color: #94a3b8; }
.empty span { font-size: 48px; display: block; margin-bottom: 12px; }
.empty p { font-size: 14px; margin: 0 0 16px; }
.go-shop {
  padding: 8px 20px; border-radius: 10px; border: none;
  background: #6366f1; color: #fff; font-size: 13px; cursor: pointer;
}

@media (max-width: 600px) {
  .wrap { padding: 14px 12px 40px; }
  .head { flex-direction: column; align-items: stretch; }
  .search { width: 100%; }
  .tabs { gap: 4px; }
  .tb { padding: 6px 12px; font-size: 12px; }
  .oc-foot { justify-content: stretch; }
  .oc-btn { flex: 1; text-align: center; }
}
</style>
