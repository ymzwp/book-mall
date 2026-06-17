<template>
  <div class="mine-page">
    <!-- 主体：侧边栏 + 内容 -->
    <div class="main-layout">
      <!-- 左侧侧边栏 -->
      <aside class="sidebar">
        <div class="sb-user" @click="openEdit">
          <img :src="user.avatar || dfAvt" class="sb-avatar" alt="头像" />
          <div class="sb-user-info">
            <span class="sb-name">{{ user.nickName || '专业版代理' }}</span>
            <span class="sb-level">{{ levelLabel }}</span>
          </div>
        </div>

        <nav class="sb-nav">
          <a
            v-for="item in sideNavs"
            :key="item.key"
            class="sb-nav-item"
            :class="{ active: activeTab === item.key }"
            @click="switchTo(item.key)"
          >
            <span class="sb-nav-icon">{{ item.icon }}</span>
            <span class="sb-nav-label">{{ item.label }}</span>
            <span v-if="item.key==='notify' ? unreadCount > 0 : item.badge" class="sb-nav-badge">{{ item.key==='notify' ? unreadCount : item.badge }}</span>
          </a>
        </nav>

        <div class="sb-footer">
          <button class="sb-logout" @click="doLogout">退出登录</button>
        </div>
      </aside>

      <!-- 右侧内容区（按标签页切换） -->
      <div class="content-area">

        <!-- 个人信息 -->
        <section v-show="activeTab === 'profile'" class="tab-panel">
          <h2 class="panel-title">个人信息</h2>
          <div class="profile-card">
            <div class="pf-top">
              <img :src="user.avatar || dfAvt" class="pf-avatar" @click="openEdit" />
              <div class="pf-info">
                <div class="pf-name">{{ user.nickName || '专业版代理' }} <span class="level-badge">{{ levelLabel }}</span></div>
                <div class="pf-meta">会员编号：{{ user.id || '-' }} | 注册时间：{{ fmt(user.createTime) }} | 邀请码：<span class="invite-copy" @click.stop="copyInviteCode" title="点击复制">{{ inviteData.inviteCode || '暂无' }}</span></div>
              </div>
              <button class="pf-edit-btn" @click="openEdit">编辑资料</button>
            </div>

            <!-- 详细信息 -->
            <div class="pf-detail-grid">
              <div class="pf-detail-item">
                <span class="pf-detail-label">手机号</span>
                <span class="pf-detail-val">{{ user.phone || '未填写' }}</span>
              </div>
              <div class="pf-detail-item">
                <span class="pf-detail-label">联系邮箱</span>
                <span class="pf-detail-val">{{ user.email || '未填写' }}</span>
              </div>
              <div class="pf-detail-item">
                <span class="pf-detail-label">上次登录</span>
                <span class="pf-detail-val">2026-06-06 04:18</span>
              </div>
              <div class="pf-detail-item">
                <span class="pf-detail-label">登录地点</span>
                <span class="pf-detail-val">河南省郑州市</span>
              </div>
              <div class="pf-detail-item">
                <span class="pf-detail-label">推广人数</span>
                <span class="pf-detail-val">{{ inviteData.count || 0 }} 人</span>
              </div>
              <div class="pf-detail-item">
                <span class="pf-detail-label">累计收益</span>
                <span class="pf-detail-val" style="color:#6366f1;font-weight:700;">¥ 128.50</span>
              </div>
            </div>

            <!-- 账户资产 -->
            <div class="pf-assets">
              <div class="pf-asset-item">
                <span class="pf-asset-icon">💰</span>
                <span class="pf-asset-label">账户余额</span>
                <span class="pf-asset-val">¥ {{ balance }}</span>
              </div>
              <div class="pf-asset-item">
                <span class="pf-asset-icon">💎</span>
                <span class="pf-asset-label">云梦豆</span>
                <span class="pf-asset-val">{{ points }}</span>
              </div>
              <div class="pf-asset-item">
                <span class="pf-asset-icon">📊</span>
                <span class="pf-asset-label">佣金余额</span>
                <span class="pf-asset-val">¥ 0.00</span>
              </div>
              <div class="pf-asset-item">
                <span class="pf-asset-icon">🔒</span>
                <span class="pf-asset-label">冻结金额</span>
                <span class="pf-asset-val">¥ 0.00</span>
              </div>
            </div>

          </div>
        </section>

        <!-- 会员权益 -->
        <section v-show="activeTab === 'benefits'" class="tab-panel">
          <h2 class="panel-title">会员权益</h2>

          <!-- 会员信息 -->
          <div class="member-info-bar">
            <div class="mi-left">
              <span class="mi-label">会员等级</span>
              <span class="level-badge">{{ levelLabel }}</span>
            </div>
            <div class="mi-left">
              <span class="mi-label">会员名称</span>
              <span class="mi-value">{{ user.nickName || '专业版代理' }}</span>
            </div>
          </div>

          <!-- 当前权益 -->
          <div class="benefits-card">
            <h3>当前权益 - {{ levelLabel }}</h3>
            <ul class="benefits-list">
              <li v-for="b in currentTier.benefits" :key="b" class="b-active">{{ b }}</li>
            </ul>
            <p class="uc-desc" style="margin-top:12px;">{{ currentTier.desc }}</p>
          </div>

          <!-- 会员升级 -->
          <div class="upgrade-section">
            <h3 class="upgrade-title">会员升级</h3>
            <p class="upgrade-sub">可升级会员列表</p>

            <div class="upgrade-grid">
              <div v-for="tier in memberTiers" :key="tier.name" class="upgrade-card" :class="tier.class">
                <div class="uc-header">
                  <span class="uc-name">{{ tier.name }}</span>
                  <span class="uc-price">{{ tier.price === 0 ? '免费' : '¥' + tier.price + '元' }}</span>
                </div>
                <ul class="uc-benefits">
                  <li v-for="b in tier.benefits" :key="b">{{ b }}</li>
                </ul>
                <p class="uc-desc">{{ tier.desc }}</p>
                <button
                  class="btn-buy"
                  :class="[tier.btnClass, isCurrentLevel(tier.level) ? 'btn-current' : '']"
                  :disabled="isCurrentLevel(tier.level)"
                  @click="buyMember(tier.name, tier.price, tier.level)"
                >
                  {{ isCurrentLevel(tier.level) ? '当前等级' : '立即购买' }}
                </button>
              </div>
            </div>
          </div>
        </section>

        <!-- 我的钱包 -->
        <section v-show="activeTab === 'wallet'" class="tab-panel">
          <h2 class="panel-title">我的钱包</h2>
          <div class="wallet-card">
            <div class="wallet-balance">
              <span class="wallet-label">账户余额</span>
              <span class="wallet-amount">¥ {{ balance }}</span>
            </div>
            <div class="wallet-stats">
              <div class="ws-item"><span class="ws-label">佣金余额</span><span class="ws-val">¥ 0.00</span></div>
              <div class="ws-item"><span class="ws-label">冻结金额</span><span class="ws-val">¥ 0.00</span></div>
              <div class="ws-item"><span class="ws-label">云梦豆</span><span class="ws-val">{{ points }}</span></div>
            </div>
            <div class="wallet-actions">
              <button class="btn-recharge" @click="recharge">充值</button>
              <button class="btn-withdraw" @click="withdraw">提现</button>
              <button class="btn-sign" :disabled="signed" @click="doSign">{{ signed ? '今日已签到' : '🎁 签到领豆' }}</button>
            </div>
            <!-- 交易记录 -->
            <div class="wallet-records">
              <h4 v-if="rechargeRecords.length > 0" class="wallet-records-title">充值记录</h4>
              <div v-if="rechargeRecords.length === 0" class="wallet-empty">暂无交易记录</div>
              <div v-for="r in rechargeRecords" :key="r.id" class="wallet-record-item" @click="showRecordDetail(r)">
                <div class="wri-left">
                  <span class="wri-type">{{ payMethodName(r.payMethod) }}</span>
                  <span class="wri-time">{{ fmtTime(r.createTime) }}</span>
                </div>
                <div class="wri-right">
                  <span class="wri-amount">+¥{{ r.amount }}</span>
                  <span class="wri-status" :class="{ success: r.status === 'success' }">{{ r.status === 'success' ? '已到账' : r.status }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- 我的订单 -->
        <section v-show="activeTab === 'order'" class="tab-panel">
          <h2 class="panel-title">我的订单</h2>
          <div class="order-list">
            <div v-if="orders.length === 0" class="empty-state">暂无订单</div>
            <div v-for="o in orders" :key="o.id" class="order-item" @click="showOrderDetail(o)">
              <div class="oi-left">
                <div class="oi-name">{{ o.goodsName }}</div>
                <div class="oi-time">{{ fmt(o.createTime) }} · {{ payLabel(o.payMethod) }}</div>
              </div>
              <div class="oi-right">
                <span class="oi-status" :class="'os-'+o.status">{{ o.statusText }}</span>
                <span class="oi-price">¥{{ o.orderPrice }}</span>
              </div>
            </div>
          </div>
        </section>

        <!-- 优惠卡券 -->
        <section v-show="activeTab === 'coupon'" class="tab-panel">
          <h2 class="panel-title">优惠卡券</h2>
          <div v-if="coupons.length === 0" class="empty-state">暂无可用优惠券</div>
          <div v-else class="coupon-grid">
            <div v-for="c in coupons" :key="c.id" class="coupon-card">
              <div class="cc-left">
                <span class="cc-amount">¥{{ c.amount }}</span>
                <span class="cc-condition" v-if="c.minAmount > 0">满 ¥{{ c.minAmount }} 可用</span>
                <span class="cc-condition" v-else>无门槛</span>
              </div>
              <div class="cc-right">
                <span class="cc-title">{{ c.title }}</span>
                <span class="cc-stock">剩余 {{ c.stock }} 张</span>
                <button class="cc-btn" :class="{ claimed: claimedIds.has(c.id) }" :disabled="claimedIds.has(c.id)" @click="getCoupon(c)">
                  {{ claimedIds.has(c.id) ? '已领取' : '立即领取' }}
                </button>
              </div>
            </div>
          </div>
        </section>

        <!-- 邀请推广 -->
        <section v-show="activeTab === 'invite'" class="tab-panel invite-panel">
          <!-- 推广信息 -->
          <div class="iv-card iv-info">
            <div class="iv-card-tag">我的推广信息</div>
            <div class="iv-info-grid">
              <div class="iv-info-item">
                <span class="iv-info-num">{{ inviteData.count || 0 }}</span>
                <span class="iv-info-label">推广人数</span>
              </div>
              <div class="iv-info-item">
                <span class="iv-info-num">¥0.00</span>
                <span class="iv-info-label">累计收益</span>
              </div>
              <div class="iv-info-item">
                <span class="iv-info-num">{{ inviteData.inviteCode || '暂无' }}</span>
                <span class="iv-info-label">邀请码</span>
              </div>
            </div>
          </div>

          <!-- 邀请链接 -->
          <div class="iv-card">
            <div class="iv-card-tag">我的邀请链接</div>
            <div class="iv-link-row">
              <span class="iv-link-url">{{ inviteLink }}</span>
              <button class="iv-btn-copy" @click="copyLink">复制</button>
            </div>
          </div>

          <!-- 邀请列表 -->
          <div class="iv-card">
            <div class="iv-card-tag">我的邀请列表</div>
            <div v-if="inviteData.list && inviteData.list.length > 0" class="iv-list">
              <div v-for="item in inviteData.list" :key="item.id" class="iv-list-item">
                <span class="iv-list-reward">{{ item.reward }}</span>
                <span class="iv-list-time">{{ fmtTime(item.createTime) }}</span>
                <span class="iv-list-status" :class="{ claimed: item.status === 1 }">{{ item.status === 1 ? '已领取' : '未领取' }}</span>
              </div>
            </div>
            <div v-else class="iv-list"><div class="iv-list-item" style="justify-content:center;color:#94a3b8;">暂无邀请记录</div></div>
          </div>
        </section>

        <!-- 站内通知 -->
        <section v-show="activeTab === 'notify'" class="tab-panel">
          <h2 class="panel-title">站内通知</h2>
          <div v-if="notifies.length === 0" class="empty-state">暂无通知</div>
          <div v-else class="notify-list">
            <div v-for="n in notifies" :key="n.id" class="notify-item" @click="openNotify(n)">
              <span class="ni-dot" :class="{ read: readIds.has(n.id) }"></span>{{ n.title }}
            </div>
          </div>
        </section>

        <!-- 通知详情弹窗 -->
        <el-dialog v-model="showNotify" :title="notifyDetail?.title" width="440px" :close-on-click-modal="false" center>
          <p v-if="notifyDetail?.content" style="color:#475569;font-size:14px;line-height:1.8;white-space:pre-wrap;">{{ notifyDetail.content }}</p>
          <p v-else style="color:#94a3b8;text-align:center;padding:20px 0;">暂无详细内容</p>
          <template #footer>
            <span style="font-size:12px;color:#94a3b8;">{{ fmtTime(notifyDetail?.createTime) }}</span>
          </template>
        </el-dialog>

        <!-- 系统设置 -->
        <section v-show="activeTab === 'setting'" class="tab-panel">
          <h2 class="panel-title">系统设置</h2>
          <div class="setting-list">
            <div class="setting-item" @click="openEdit"><span>修改个人资料</span><span class="si-arrow">›</span></div>
            <div class="setting-item" @click="devMsg('修改密码')"><span>修改密码</span><span class="si-arrow">›</span></div>
            <div class="setting-item" @click="devMsg('隐私设置')"><span>隐私设置</span><span class="si-arrow">›</span></div>
            <div class="setting-item" @click="devMsg('关于我们')"><span>关于我们</span><span class="si-arrow">›</span></div>
          </div>
        </section>

      </div>
    </div>

    <!-- 充值详情弹窗 -->
    <el-dialog v-model="showDetail" title="充值详情" width="400px" :close-on-click-modal="false" center>
      <template v-if="detail">
        <div class="detail-grid">
          <div class="dg-item"><span class="dg-label">订单编号</span><span class="dg-val">{{ detail.orderNo }}</span></div>
          <div class="dg-item"><span class="dg-label">充值金额</span><span class="dg-val" style="color:#10b981;font-weight:700;">+¥{{ detail.amount }}</span></div>
          <div class="dg-item"><span class="dg-label">支付方式</span><span class="dg-val">{{ payMethodName(detail.payMethod) }}</span></div>
          <div class="dg-item"><span class="dg-label">下单时间</span><span class="dg-val">{{ fmtTime(detail.createTime) }}</span></div>
          <div class="dg-item"><span class="dg-label">充值前余额</span><span class="dg-val">¥{{ detail.balanceBefore || '0.00' }}</span></div>
          <div class="dg-item"><span class="dg-label">充值后余额</span><span class="dg-val" style="color:#6366f1;font-weight:700;">¥{{ detail.balanceAfter || '0.00' }}</span></div>
          <div class="dg-item"><span class="dg-label">状态</span><span class="dg-val" :style="{ color: detail.status==='success'?'#10b981':'#f59e0b' }">{{ detail.status==='success'?'已到账':'待支付' }}</span></div>
        </div>
      </template>
    </el-dialog>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="showOrder" title="订单详情" width="400px" :close-on-click-modal="false" center>
      <template v-if="orderDetail">
        <div class="detail-grid">
          <div class="dg-item"><span class="dg-label">订单编号</span><span class="dg-val">#{{ orderDetail.id }}</span></div>
          <div class="dg-item"><span class="dg-label">商品名称</span><span class="dg-val">{{ orderDetail.goodsName }}</span></div>
          <div class="dg-item"><span class="dg-label">充值账号</span><span class="dg-val">{{ orderDetail.accountNumber || '-' }}</span></div>
          <div class="dg-item"><span class="dg-label">支付方式</span><span class="dg-val">{{ payLabel(orderDetail.payMethod) }}</span></div>
          <div class="dg-item"><span class="dg-label">订单金额</span><span class="dg-val" style="color:#f43f5e;font-weight:700;">¥{{ orderDetail.orderPrice }}</span></div>
          <div class="dg-item"><span class="dg-label">下单时间</span><span class="dg-val">{{ fmt(orderDetail.createTime) }}</span></div>
          <div class="dg-item"><span class="dg-label">订单状态</span><span class="dg-val" :style="{ color: orderDetail.status==='completed'?'#10b981':'#f59e0b' }">{{ orderDetail.statusText }}</span></div>
        </div>
      </template>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="showEdit" title="编辑资料" width="360px" :close-on-click-modal="false" center>
      <el-form label-width="70px">
        <el-form-item label="昵称"><el-input v-model="editName" placeholder="请输入昵称" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="editPhone" placeholder="请输入手机号" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="editEmail" placeholder="请输入邮箱" /></el-form-item>
        <el-form-item label="头像链接"><el-input v-model="editAvatar" placeholder="请输入头像URL" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="doSave">{{ saving ? '保存中...' : '保存' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/utils/http'

const router = useRouter()
const route = useRoute()

const user = ref({})
const editName = ref('')
const editPhone = ref('')
const editEmail = ref('')
const editAvatar = ref('')
const showEdit = ref(false)
const saving = ref(false)
const dfAvt = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const balance = ref('0.00')
const points = ref(0)
const rechargeRecords = ref([])
const showDetail = ref(false)
const detail = ref(null)

const payMethodName = (m) => ({ alipay:'支付宝充值', wechat:'微信充值', bank:'银行卡充值' }[m] || '充值')
const fmtTime = (v) => v ? v.replace('T', ' ').substring(0, 16) : ''
const showRecordDetail = (r) => { detail.value = r; showDetail.value = true }

const levelMap = { normal:'普通会员', bronze:'青铜会员', silver:'白银会员', gold:'黄金会员' }
const levelLabel = computed(() => levelMap[user.value.memberLevel] || '普通会员')
const isCurrentLevel = (level) => (user.value.memberLevel || 'normal') === level
const currentTier = computed(() => {
  const lv = user.value.memberLevel || 'normal'
  return memberTiers.find(t => t.level === lv) || memberTiers[0]
})
const unread = ref(3)
const activeTab = ref('profile')
const allOrders = ref([])

const orders = computed(() => allOrders.value.slice(0, 10))
const fmt = (v) => v ? v.replace('T', ' ').substring(0, 16) : ''

/* ===== 侧边栏导航 ===== */
const sideNavs = reactive([
  { icon: '👤', label: '个人信息', key: 'profile' },
  { icon: '👑', label: '会员权益', key: 'benefits' },
  { icon: '💰', label: '我的钱包', key: 'wallet' },
  { icon: '📋', label: '我的订单', key: 'order' },
  { icon: '🎫', label: '优惠卡券', key: 'coupon' },
  { icon: '📨', label: '邀请推广', key: 'invite' },
  { icon: '🔔', label: '站内通知', key: 'notify' },
  { icon: '⚙️', label: '系统设置', key: 'setting' }
])

const notifies = ref([])
const showNotify = ref(false)
const notifyDetail = ref(null)
const readIds = ref(new Set(JSON.parse(localStorage.getItem('readNotifies') || '[]')))
const unreadCount = computed(() => notifies.value.filter(n => !readIds.value.has(n.id)).length)

const openNotify = (n) => {
  notifyDetail.value = n
  showNotify.value = true
  readIds.value.add(n.id)
  localStorage.setItem('readNotifies', JSON.stringify([...readIds.value]))
}

const signed = ref(false)

const switchTo = (key) => {
  activeTab.value = key
  if (key === 'wallet') { loadRechargeRecords(); checkSign() }
  if (key === 'coupon') loadCoupons()
  if (key === 'invite') loadInvite()
  if (key === 'notify') {
    loadNotifies()
    notifies.value.forEach(n => readIds.value.add(n.id))
    localStorage.setItem('readNotifies', JSON.stringify([...readIds.value]))
  }
}

const checkSign = async () => {
  try {
    const r = await http.post('/user/sign/check')
    signed.value = r.data.data?.signed || false
  } catch { signed.value = true }
}

const doSign = async () => {
  try {
    const r = await http.post('/user/sign')
    if (r.data.code === 200) {
      ElMessage.success(r.data.msg || '签到成功')
      points.value = r.data.data?.points || points.value
      signed.value = true
    } else if (r.data.code === 502) {
      ElMessage.info(r.data.msg || '今日已签到')
      signed.value = true
    } else {
      ElMessage.error(r.data.msg || '签到失败')
    }
  } catch { ElMessage.error('网络错误') }
}

const loadNotifies = async () => {
  try {
    const r = await http.get('/notify/list')
    notifies.value = r.data || []
  } catch { notifies.value = [] }
}

const loadRechargeRecords = async () => {
  try {
    const r = await http.post('/recharge/list')
    if (r.data.code === 200) rechargeRecords.value = r.data.data || []
  } catch { rechargeRecords.value = [] }
}

/* ===== 工具函数 ===== */
const devMsg = (name) => ElMessage.info(`${name}功能开发中`)


/* ===== 数据加载 ===== */
const load = async () => {
  try {
    const r = await http.post('/user/getLogin')
    if (r.data.code === 200 && r.data.data) {
      user.value = r.data.data
      editName.value = user.value.nickName || ''
      editPhone.value = user.value.phone || ''
      editEmail.value = user.value.email || ''
      editAvatar.value = user.value.avatar || ''
      balance.value = (user.value.balance || 0).toFixed(2)
      points.value = user.value.points || 0
    }
  } catch { /* 使用演示数据 */ }
  try {
    const r = await http.post('/order/my')
    let arr = r.data.data || []
    for (let o of arr) {
      if (o.goodsId) {
        try { const g = await http.get('/goods/get', { params: { id: o.goodsId } }); o.goodsName = g.data.goodsName } catch { o.goodsName = '已下架' }
      }
      o.status = o.status || 'completed'
      const m = { unpaid: '未付款', pending: '待处理', progress: '进行中', completed: '已完成', refunded: '已退款' }
      o.statusText = m[o.status] || '已完成'
    }
    allOrders.value = arr
  } catch {}
}

const openEdit = () => {
  editName.value = user.value.nickName || ''
  editPhone.value = user.value.phone || ''
  editEmail.value = user.value.email || ''
  editAvatar.value = user.value.avatar || ''
  showEdit.value = true
}

const doSave = async () => {
  if (!editName.value.trim()) return ElMessage.warning('昵称不能为空')
  if (!user.value.id) return ElMessage.error('用户信息异常，请重新登录')
  saving.value = true
  try {
    const r = await http.post('/user/update', { id: user.value.id, nickName: editName.value.trim(), phone: editPhone.value.trim(), email: editEmail.value.trim(), avatar: editAvatar.value.trim() })
    if (r.data.code === 200) {
      ElMessage.success('已更新')
      showEdit.value = false
      await load()
    } else {
      ElMessage.error(r.data.msg || '保存失败')
    }
  } catch { ElMessage.error('网络错误，请稍后重试') }
  finally { saving.value = false }
}

const memberTiers = [
  { name: '普通会员', level: 'normal', price: 0, class: 'tier-default', btnClass: 'btn-current',
    benefits: ['基础商品浏览', '基础客服支持'],
    desc: '适合初次了解平台的新用户' },
  { name: '青铜会员', level: 'bronze', price: 3, class: 'tier-bronze', btnClass: '',
    benefits: ['开启下级推广提成功能', '售后处理优先级提升', '自定义商品上架状态'],
    desc: '适合长期自用、小规模运营站长' },
  { name: '白银会员', level: 'silver', price: 6, class: 'tier-silver', btnClass: '',
    benefits: ['多级下级提成全开', '优先售后通道', '自定义商城界面模板', '自定义商品利润'],
    desc: '适合想扩大收益的进阶用户' },
  { name: '黄金会员', level: 'gold', price: 9, class: 'tier-gold', btnClass: '',
    benefits: ['全部权益开放', '优先售后通道，问题快速处理', '可开通分站运营权限', '开启余额提现权限'],
    desc: '想认真做项目、提升收益必选等级' }
]

const buyMember = async (name, price, level) => {
  // 弹出密码输入框
  let pwd = ''
  try {
    const { value } = await ElMessageBox.prompt(`确认购买【${name}】？将扣除 ¥${price}，请输入登录密码`, '购买会员', {
      confirmButtonText: '确认购买', cancelButtonText: '取消',
      inputType: 'password', inputPlaceholder: '请输入密码',
      inputValidator: (v) => v ? true : '密码不能为空'
    })
    pwd = value
  } catch { return }
  try {
    const r = await http.post('/user/buyMember', { price: price, level: level, password: pwd })
    if (r.data.code === 200) {
      ElMessage.success(r.data.msg || '购买成功')
      await load()
    } else {
      ElMessage.error(r.data.msg || '购买失败')
    }
  } catch { ElMessage.error('网络错误') }
}
const recharge = () => router.push('/recharge')
const withdraw = () => ElMessage.info('提现功能开发中')
/** 邀请数据 */
const inviteData = ref({ count: 0, inviteCode: '', list: [] })
const inviteLink = computed(() => inviteData.value.inviteCode
  ? `${window.location.origin}/?invite=${inviteData.value.inviteCode}`
  : `${window.location.origin}/`)

/** 加载邀请数据 */
const loadInvite = async () => {
  try {
    const r = await http.get('/user/invite')
    if (r.data.code === 200 && r.data.data) {
      inviteData.value = r.data.data
    }
  } catch { /* 默认空 */ }
}
/** 复制链接 */
const copyLink = () => {
  navigator.clipboard.writeText(inviteLink.value)
    .then(() => ElMessage.success('链接已复制到剪贴板'))
    .catch(() => ElMessage.info('请手动复制链接'))
}
/** 复制邀请码 */
const copyInviteCode = () => {
  const code = inviteData.value.inviteCode
  if (!code) return ElMessage.info('暂无邀请码')
  navigator.clipboard.writeText(code)
    .then(() => ElMessage.success(`邀请码 ${code} 已复制`))
    .catch(() => ElMessage.info('请手动复制'))
}
const showOrder = ref(false)
const orderDetail = ref(null)
const coupons = ref([])
/** 已领取的优惠券ID集合 */
const claimedIds = ref(new Set())

/** 加载可用优惠券 + 已领取记录 */
const loadCoupons = async () => {
  try {
    const [r1, r2] = await Promise.all([
      http.get('/coupon/list'),
      http.get('/coupon/my')
    ])
    coupons.value = r1.data || []
    if (r2.data.code === 200 && r2.data.data) {
      claimedIds.value = new Set(r2.data.data.map(mc => mc.coupon_id || mc.id))
    }
  } catch { coupons.value = [] }
}

/** 领取优惠券 */
const getCoupon = async (c) => {
  try {
    const r = await http.post('/coupon/take', { couponId: c.id })
    if (r.data.code === 200) {
      ElMessage.success('领取成功')
      c.stock--
      claimedIds.value.add(c.id)  // 即时标记已领取
    } else {
      ElMessage.error(r.data.msg || '领取失败')
    }
  } catch { ElMessage.error('网络错误') }
}

/** 支付方式中文映射 */
const payLabel = (m) => ({ alipay:'支付宝', wechat:'微信支付', balance:'余额支付' }[m] || m || '未知')
const showOrderDetail = (o) => { orderDetail.value = o; showOrder.value = true }

const doLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    await http.post('/user/logout')
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch { /* 取消 */ }
}

onMounted(() => {
  load()
  loadNotifies()
  loadInvite()
  if (route.query.tab) activeTab.value = route.query.tab
})
</script>

<style scoped>
/* ===== 整体背景 ===== */
.mine-page {
  height: 100vh;
  overflow: hidden;
  background: var(--bg-page, #f1f5f9);
  display: flex; flex-direction: column; align-items: center;
  padding: 0 20px;
}

/* ===== 主体布局 ===== */
.main-layout {
  width: 100%; max-width: 1400px;
  display: grid; grid-template-columns: 260px 1fr;
  gap: 24px; margin-top: 32px; flex: 1; overflow: hidden;
  position: relative; z-index: 1; min-height: 0;
}

/* ===== 侧边栏 ===== */
.sidebar {
  background: #fff;
  border: 1px solid var(--border-card, #e2e8f0);
  border-radius: 24px; padding: 24px 16px;
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.06));
  display: flex; flex-direction: column;
  height: fit-content;
}
.sb-user {
  display: flex; align-items: center; gap: 14px; padding: 16px;
  background: var(--bg-surface, #f8fafc); border-radius: 18px;
  margin-bottom: 20px; cursor: pointer; transition: background 0.2s;
}
.sb-user:hover { background: #f1f5f9; }
.sb-avatar { width: 48px; height: 48px; border-radius: 50%; object-fit: cover; border: 2px solid #6366f1; flex-shrink: 0; }
.sb-user-info { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.sb-name { font-size: 15px; font-weight: 700; color: var(--text-primary, #0f172a); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.sb-level { font-size: 12px; color: #6366f1; font-weight: 600; }
.sb-nav { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.sb-nav-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 16px; border-radius: 14px;
  cursor: pointer; color: var(--text-secondary, #475569);
  text-decoration: none; font-size: 14px; font-weight: 500;
  transition: all 0.2s; position: relative;
}
.sb-nav-item:hover { background: #f8fafc; color: #0f172a; }
.sb-nav-item.active { background: rgba(99,102,241,0.08); color: #6366f1; }
.sb-nav-item.active::before {
  content: ''; position: absolute; left: 0; top: 50%; transform: translateY(-50%);
  width: 3px; height: 20px; background: #6366f1; border-radius: 3px;
}
.sb-nav-icon { font-size: 18px; line-height: 1; flex-shrink: 0; }
.sb-nav-label { flex: 1; }
.sb-nav-badge {
  min-width: 18px; height: 18px; border-radius: 9px;
  background: #f43f5e; color: #fff;
  font-size: 10px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; line-height: 1;
}
.sb-footer { margin-top: 16px; padding-top: 16px; border-top: 1px solid #f1f5f9; }
.sb-logout {
  width: 100%; padding: 10px; background: transparent;
  color: #94a3b8; border: 1px solid #e2e8f0;
  border-radius: 14px; font-size: 13px; font-weight: 600;
  cursor: pointer; transition: all 0.2s;
}
.sb-logout:hover { background: #fef2f2; border-color: #fecaca; color: #ef4444; }

/* ===== 右侧内容区 ===== */
.content-area { overflow-y: auto; height: 100%; }

.tab-panel {
  background: #fff;
  border: 1px solid var(--border-card, #e2e8f0);
  border-radius: 24px; padding: 32px;
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.06));
}
.panel-title { font-size: 22px; font-weight: 700; color: var(--text-primary, #0f172a); margin-bottom: 24px; }

/* 个人信息 */
.profile-card { display: flex; flex-direction: column; gap: 24px; }
.pf-top { display: flex; align-items: center; gap: 20px; }
.pf-avatar { width: 72px; height: 72px; border-radius: 50%; object-fit: cover; border: 3px solid #6366f1; cursor: pointer; flex-shrink: 0; }
.pf-info { flex: 1; min-width: 0; }
.pf-name { font-size: 24px; font-weight: 700; color: #0f172a; display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.level-badge {
  display: inline-block;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff; font-size: 12px; font-weight: 700;
  padding: 3px 12px; border-radius: 30px; letter-spacing: 1px;
}
.pf-meta { font-size: 13px; color: #94a3b8; }
.invite-copy { color: #6366f1; font-weight: 600; cursor: pointer; border-bottom: 1px dashed #6366f1; padding: 0 2px; transition: all 0.15s; }
.invite-copy:hover { background: rgba(99,102,241,0.08); border-radius: 4px; }

/* 详细信息网格 */
.pf-detail-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 2px; }
.pf-detail-item {
  background: #f8fafc; padding: 16px 20px;
  display: flex; flex-direction: column; gap: 6px;
}
.pf-detail-item:first-child { border-radius: 16px 0 0 0; }
.pf-detail-item:nth-child(3) { border-radius: 0 16px 0 0; }
.pf-detail-item:nth-child(4) { border-radius: 0 0 0 16px; }
.pf-detail-item:nth-child(6) { border-radius: 0 0 16px 0; }
.pf-detail-label { font-size: 12px; color: #94a3b8; }
.pf-detail-val { font-size: 14px; font-weight: 600; color: #0f172a; }

/* 资产卡片 */
.pf-assets { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.pf-asset-item {
  background: #f8fafc; border-radius: 16px; padding: 18px 14px;
  text-align: center; transition: transform 0.2s;
}
.pf-asset-item:hover { transform: translateY(-2px); }
.pf-asset-icon { font-size: 24px; display: block; margin-bottom: 8px; }
.pf-asset-label { display: block; font-size: 12px; color: #94a3b8; margin-bottom: 4px; }
.pf-asset-val { display: block; font-size: 18px; font-weight: 700; color: #0f172a; }

.pf-edit-btn {
  padding: 8px 20px; border-radius: 12px;
  background: #fff; color: #475569;
  border: 1px solid #e2e8f0; font-size: 13px; font-weight: 600;
  cursor: pointer; transition: all 0.2s; white-space: nowrap;
}
.pf-edit-btn:hover { border-color: #6366f1; color: #6366f1; }
.btn-recharge {
  padding: 9px 22px; background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff; border: none; border-radius: 24px;
  font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.25s;
}
.btn-recharge:hover { box-shadow: 0 4px 20px rgba(99,102,241,0.3); transform: translateY(-1px); }
.btn-withdraw {
  padding: 9px 22px; background: transparent; color: #6366f1;
  border: 1px solid #6366f1; border-radius: 24px;
  font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.25s;
}
.btn-withdraw:hover { background: rgba(99,102,241,0.08); }
.btn-sign {
  padding: 9px 22px; background: linear-gradient(135deg, #f59e0b, #f97316);
  color: #fff; border: none; border-radius: 24px;
  font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.25s;
}
.btn-sign:hover:not(:disabled) { box-shadow: 0 4px 20px rgba(245,158,11,0.35); transform: translateY(-1px); }
.btn-sign:disabled { opacity: 0.5; cursor: not-allowed; background: #e2e8f0; color: #94a3b8; }

/* 会员权益 */
.member-info-bar {
  display: flex; gap: 40px; flex-wrap: wrap;
  background: #f8fafc; border-radius: 18px;
  padding: 20px 28px; margin-bottom: 20px;
}
.mi-left { display: flex; align-items: center; gap: 12px; }
.mi-label { color: #94a3b8; font-size: 14px; }
.mi-value { color: #0f172a; font-size: 16px; font-weight: 700; }

.benefits-card { background: #f8fafc; border-radius: 18px; padding: 24px 28px; margin-bottom: 24px; }
.benefits-card h3 { font-size: 15px; font-weight: 600; color: #475569; margin-bottom: 16px; }
.benefits-list { list-style: none; }
.benefits-list li { color: #94a3b8; font-size: 14px; line-height: 2.2; padding-left: 20px; position: relative; }
.benefits-list li::before { content: '·'; position: absolute; left: 4px; color: #cbd5e1; font-weight: 700; }
.benefits-list li.b-active { color: #334155; }
.benefits-list li.b-active::before { content: '✅'; left: 0px; color: #10b981; font-size: 12px; }

/* 升级区域 */
.upgrade-section { margin-top: 8px; }
.upgrade-title { font-size: 18px; font-weight: 700; color: #0f172a; margin-bottom: 4px; }
.upgrade-sub { font-size: 13px; color: #94a3b8; margin-bottom: 16px; }
.upgrade-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.upgrade-card {
  background: #f8fafc;
  border: 2px solid #e2e8f0;
  border-radius: 20px; padding: 24px 24px;
  transition: all 0.3s;
}
.upgrade-card:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(0,0,0,0.06); }
.tier-default { border-color: #e2e8f0; }
.tier-default:hover { border-color: #94a3b8; }
.tier-bronze { border-color: #d6b883; background: linear-gradient(180deg, #fdfaf5 0%, #f8fafc 100%); }
.tier-bronze:hover { border-color: #c9a050; box-shadow: 0 8px 24px rgba(180,140,60,0.12); }
.tier-silver { border-color: #c0c8d4; background: linear-gradient(180deg, #fafbfc 0%, #f8fafc 100%); }
.tier-silver:hover { border-color: #8899aa; box-shadow: 0 8px 24px rgba(100,120,150,0.12); }
.tier-gold { border-color: #e2b84a; background: linear-gradient(180deg, #fffef5 0%, #f8fafc 100%); }
.tier-gold:hover { border-color: #c9a030; box-shadow: 0 8px 24px rgba(200,160,40,0.15); }
.uc-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 14px;
}
.uc-name { font-size: 17px; font-weight: 700; color: #0f172a; }
.tier-bronze .uc-name { color: #8b6914; }
.tier-silver .uc-name { color: #4a5568; }
.tier-gold .uc-name { color: #8b6508; }
.uc-price { font-size: 20px; font-weight: 800; }
.tier-default .uc-price { color: #94a3b8; }
.tier-bronze .uc-price { color: #b8860b; }
.tier-silver .uc-price { color: #5a6c7d; }
.tier-gold .uc-price { color: #c9a030; }
.uc-benefits { list-style: none; margin-bottom: 10px; }
.uc-benefits li { color: #475569; font-size: 13px; line-height: 2; padding-left: 14px; position: relative; }
.uc-benefits li::before { content: '✓'; position: absolute; left: 0; color: #6366f1; font-size: 11px; font-weight: 700; }
.tier-bronze .uc-benefits li::before { color: #b8860b; }
.tier-silver .uc-benefits li::before { color: #5a6c7d; }
.tier-gold .uc-benefits li::before { color: #c9a030; }
.uc-desc { color: #94a3b8; font-size: 12px; margin-bottom: 16px; }
.btn-buy {
  width: 100%; padding: 10px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff; border: none; border-radius: 12px;
  font-size: 14px; font-weight: 700; cursor: pointer;
  transition: all 0.25s;
}
.btn-buy:hover { box-shadow: 0 4px 20px rgba(99,102,241,0.3); transform: translateY(-1px); }
.btn-current {
  background: #e2e8f0; color: #94a3b8; cursor: default;
}
.btn-current:hover { box-shadow: none; transform: none; }
.tier-bronze .btn-buy { background: linear-gradient(135deg, #b8860b, #d4a020); }
.tier-bronze .btn-buy:hover { box-shadow: 0 4px 20px rgba(184,134,11,0.3); }
.tier-silver .btn-buy { background: linear-gradient(135deg, #5a6c7d, #7b8fa0); }
.tier-silver .btn-buy:hover { box-shadow: 0 4px 20px rgba(90,108,125,0.3); }
.tier-gold .btn-buy { background: linear-gradient(135deg, #c9a030, #e2b84a); }
.tier-gold .btn-buy:hover { box-shadow: 0 4px 20px rgba(201,160,48,0.3); }

/* 我的钱包 */
.wallet-card { display: flex; flex-direction: column; gap: 20px; }
.wallet-balance {
  text-align: center; padding: 32px;
  background: #f8fafc; border-radius: 20px;
}
.wallet-label { display: block; font-size: 14px; color: #94a3b8; margin-bottom: 8px; }
.wallet-amount { font-size: 40px; font-weight: 800; color: #6366f1; }
.wallet-stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
.ws-item {
  background: #f8fafc; border-radius: 16px;
  padding: 16px; text-align: center;
}
.ws-label { display: block; font-size: 12px; color: #94a3b8; margin-bottom: 6px; }
.ws-val { font-size: 20px; font-weight: 700; color: #0f172a; }
.wallet-actions { display: flex; gap: 12px; }
.wallet-actions .btn-recharge, .wallet-actions .btn-withdraw { flex: 1; text-align: center; }
.wallet-empty { text-align: center; color: #94a3b8; padding: 40px 0; font-size: 14px; }

.wallet-records { margin-top: 8px; }
.wallet-records-title { font-size: 14px; font-weight: 700; color: #0f172a; margin-bottom: 10px; }
.wallet-record-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 16px; border-radius: 12px;
  background: #f8fafc; margin-bottom: 6px; transition: background 0.2s;
}
.wallet-record-item:hover { background: #f1f5f9; }
.wri-left { display: flex; flex-direction: column; gap: 4px; }
.wri-type { font-size: 14px; font-weight: 600; color: #0f172a; }
.wri-time { font-size: 11px; color: #94a3b8; }
.wri-right { text-align: right; display: flex; flex-direction: column; gap: 4px; }
.wri-amount { font-size: 16px; font-weight: 700; color: #10b981; }
.wri-status {
  font-size: 11px; padding: 2px 8px; border-radius: 6px;
  background: rgba(245,158,11,0.1); color: #d97706; font-weight: 600;
}
.wri-status.success { background: rgba(16,185,129,0.1); color: #10b981; }

/* 充值详情弹窗 */
.detail-grid { display: flex; flex-direction: column; gap: 2px; }
.dg-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 14px; border-radius: 10px; background: #f8fafc;
}
.dg-label { font-size: 13px; color: #94a3b8; }
.dg-val { font-size: 14px; font-weight: 600; color: #0f172a; }

/* 优惠券 */
.coupon-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.coupon-card {
  display: flex; background: linear-gradient(135deg, #fef9e7 0%, #fff 60%);
  border: 1px solid #f0e4c6; border-radius: 14px; overflow: hidden;
  transition: all 0.25s;
}
.coupon-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(200,160,40,0.12); }
.cc-left {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  width: 100px; padding: 16px 10px; background: linear-gradient(135deg, #f59e0b, #d97706);
  color: #fff; flex-shrink: 0;
}
.cc-amount { font-size: 22px; font-weight: 800; }
.cc-condition { font-size: 10px; opacity: 0.85; margin-top: 2px; }
.cc-right { flex: 1; padding: 14px 16px; display: flex; flex-direction: column; gap: 6px; }
.cc-title { font-size: 14px; font-weight: 600; color: #0f172a; }
.cc-stock { font-size: 11px; color: #94a3b8; }
.cc-btn {
  margin-top: auto; padding: 5px 16px; background: linear-gradient(135deg, #f59e0b, #d97706);
  color: #fff; border: none; border-radius: 8px; font-size: 12px; font-weight: 600;
  cursor: pointer; transition: all 0.2s; align-self: flex-start;
}
.cc-btn:hover { box-shadow: 0 4px 12px rgba(245,158,11,0.3); }
.cc-btn.claimed { background: #e2e8f0; color: #94a3b8; cursor: not-allowed; }
.cc-btn.claimed:hover { box-shadow: none; }

/* 订单列表 */
.order-list { display: flex; flex-direction: column; gap: 8px; }
.order-item {
  display: flex; justify-content: space-between; align-items: center;
  background: #f8fafc; border-radius: 14px;
  padding: 16px 20px; cursor: pointer; transition: background 0.2s;
}
.order-item:hover { background: #f1f5f9; }
.oi-name { font-size: 15px; font-weight: 600; color: #0f172a; margin-bottom: 4px; }
.oi-time { font-size: 12px; color: #94a3b8; }
.oi-right { text-align: right; }
.oi-status { display: inline-block; font-size: 11px; padding: 3px 10px; border-radius: 8px; font-weight: 600; margin-bottom: 6px; }
.os-completed { background: rgba(16,185,129,0.1); color: #10b981; }
.os-unpaid { background: rgba(245,158,11,0.1); color: #d97706; }
.os-pending, .os-progress { background: rgba(59,130,246,0.1); color: #3b82f6; }
.os-refunded { background: rgba(148,163,184,0.1); color: #64748b; }
.oi-price { display: block; font-size: 17px; font-weight: 700; color: #6366f1; }

/* 邀请推广 */
.invite-panel {
  padding: 28px 36px;
  display: flex; flex-direction: column;
  gap: 20px;
}

/* 金色卡片 */
.iv-card {
  background: linear-gradient(180deg, #fffef5 0%, #fef9e7 100%);
  border: 1px solid #f0e4c6;
  border-radius: 16px;
  overflow: hidden;
}
.iv-card-tag {
  display: inline-block;
  background: linear-gradient(135deg, #d4a020, #b8860b);
  color: #fff;
  font-size: 12px; font-weight: 700;
  padding: 6px 16px;
  border-radius: 0 0 12px 0;
  letter-spacing: 0.5px;
}

/* 推广信息 */
.iv-info-grid {
  display: grid; grid-template-columns: repeat(3, 1fr);
  padding: 20px 24px 24px;
  gap: 16px;
}
.iv-info-item {
  text-align: center;
  display: flex; flex-direction: column; gap: 6px;
}
.iv-info-num {
  font-size: 22px; font-weight: 800;
  color: #8b6508;
}
.iv-info-label {
  font-size: 12px; color: #a68a3c;
}

/* 邀请链接 */
.iv-link-row {
  display: flex; align-items: center; gap: 12px;
  padding: 16px 24px 20px;
}
.iv-link-url {
  flex: 1; font-size: 13px; color: #8b6508;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.iv-btn-copy {
  padding: 8px 24px;
  background: linear-gradient(135deg, #f43f5e, #f97316);
  color: #fff; border: none; border-radius: 8px;
  font-size: 13px; font-weight: 700; cursor: pointer;
  white-space: nowrap; transition: all 0.25s;
}
.iv-btn-copy:hover { box-shadow: 0 4px 16px rgba(244,63,94,0.3); transform: translateY(-1px); }

/* 推广图片 */
.iv-img-box {
  display: flex; align-items: center; gap: 12px;
  padding: 20px 24px 24px;
}
.iv-img-icon { font-size: 28px; flex-shrink: 0; }
.iv-img-box > span:nth-child(2) { color: #a68a3c; font-size: 14px; flex: 1; }
.iv-btn-download {
  padding: 8px 20px;
  background: linear-gradient(135deg, #b8860b, #d4a020);
  color: #fff; border: none; border-radius: 8px;
  font-size: 13px; font-weight: 600; cursor: pointer;
  transition: all 0.25s; white-space: nowrap;
}
.iv-btn-download:hover { box-shadow: 0 4px 16px rgba(184,134,11,0.3); transform: translateY(-1px); }

/* 邀请列表 */
.iv-list {
  padding: 0 24px 16px;
  display: flex; flex-direction: column;
}
.iv-list-item {
  display: flex; align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #faf0d7;
}
.iv-list-item:last-child { border-bottom: none; }
.iv-list-reward { flex: 1; font-size: 15px; font-weight: 600; color: #0f172a; }
.iv-list-time { flex: 1; font-size: 13px; color: #94a3b8; text-align: center; }
.iv-list-status {
  font-size: 12px; font-weight: 600;
  padding: 5px 14px; border-radius: 20px;
  background: rgba(245,158,11,0.1); color: #d97706;
}
.iv-list-status.claimed {
  background: rgba(16,185,129,0.1); color: #10b981;
}

/* 通知列表 */
.notify-list { display: flex; flex-direction: column; gap: 2px; }
.notify-item {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 18px; border-radius: 14px;
  color: #475569; font-size: 14px;
  background: #f8fafc; transition: background 0.2s;
}
.notify-item:hover { background: #f1f5f9; }
.ni-dot { width: 8px; height: 8px; border-radius: 50%; background: #f43f5e; flex-shrink: 0; }
.ni-dot.read { background: #cbd5e1; }

/* 设置列表 */
.setting-list { display: flex; flex-direction: column; gap: 2px; }
.setting-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 20px; border-radius: 14px;
  color: #475569; font-size: 15px;
  background: #f8fafc; cursor: pointer; transition: background 0.2s;
}
.setting-item:hover { background: #f1f5f9; }
.si-arrow { color: #94a3b8; font-size: 20px; }

/* 空状态 */
.empty-state { text-align: center; color: #94a3b8; padding: 60px 0; font-size: 15px; }

/* ===== 响应式 ===== */
@media (max-width: 1100px) {
  .main-layout { grid-template-columns: 220px 1fr; }
}
@media (max-width: 900px) {
  .main-layout { grid-template-columns: 1fr; }
  .sidebar {
    position: static; flex-direction: row; flex-wrap: wrap;
    gap: 8px; padding: 16px;
  }
  .sb-user { display: none; }
  .sb-nav { flex-direction: row; flex-wrap: wrap; }
  .sb-nav-item { padding: 8px 14px; font-size: 13px; }
  .sb-nav-item.active::before { display: none; }
  .sb-footer { display: none; }
  .pf-detail-grid { grid-template-columns: repeat(2, 1fr); }
  .pf-assets { grid-template-columns: repeat(2, 1fr); }
    .invite-panel { padding: 20px; }
    .iv-info-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 600px) {
  .mine-page { padding: 0 10px; }
  .tab-panel { padding: 20px; }
  .wallet-stats { grid-template-columns: 1fr 1fr 1fr; gap: 8px; }
  .ws-item { padding: 12px 8px; }
  .ws-val { font-size: 16px; }
}
</style>
