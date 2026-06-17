<template>
  <div class="page">
    <div class="wrap">
      <div class="hd"><h1>📝 确认订单</h1><p>请仔细核对订单信息</p></div>

      <div class="cols">
        <div class="col-l">
          <div class="card">
            <div class="chd">🎁 订单信息</div>
            <div class="grow">
              <div class="gimg"><img :src="goodsImg" :alt="goodsName" /></div>
              <div class="gmeta">
                <h3>{{ goodsName }}</h3>
                <div class="gsub">单价：¥{{ fmtP(goods.price) }} · x{{ qty }}</div>
                <div class="gtotal">小计：<strong>¥{{ fmtP(total) }}</strong></div>
              </div>
            </div>
            <div class="divider"></div>
            <div class="acc">
              <label>充值账号</label>
              <input v-model="acc" type="text" placeholder="请输入QQ号/手机号" />
            </div>
          </div>
          <!-- 优惠券选择 -->
          <div class="card">
            <div class="chd">🎫 优惠活动</div>
            <div v-if="myCoupons.length === 0" class="cph">
              <span>暂无可用优惠券</span>
              <button class="tbtn" @click="$router.push('/mine?tab=coupon')">去领取</button>
            </div>
            <div v-else class="coupon-list">
              <label v-for="mc in myCoupons" :key="mc.user_coupon_id" class="coupon-option" :class="{ on: pickedCoupon === mc }">
                <span class="co-amount">¥{{ fmtP(mc.amount) }}</span>
                <span class="co-info">{{ mc.title }} · 满{{ mc.min_amount || 0 }}减{{ mc.amount }}</span>
                <input v-model="pickedCoupon" type="radio" :value="mc" class="co-radio" />
              </label>
            </div>
            <div class="divider" v-if="pickedCoupon"></div>
            <div class="cph" v-if="pickedCoupon">
              <span>已选：{{ pickedCoupon.title }} 省 ¥{{ fmtP(pickedCoupon.amount) }}</span>
              <button class="tbtn" @click="pickedCoupon = null">取消使用</button>
            </div>
          </div>
        </div>

        <div class="col-r">
          <div class="card sticky">
            <div class="chd">💳 结算</div>
            <div class="pay">
              <div class="pay-t">付款方式</div>
              <el-radio-group v-model="pay" class="pay-g">
                <el-radio label="wechat" class="pay-i"><span>💚 微信支付</span><span class="pay-tag">推荐</span></el-radio>
                <el-radio label="alipay" class="pay-i"><span>💙 支付宝</span></el-radio>
                <el-radio label="balance" class="pay-i"><span>💰 余额支付</span><span class="pay-hint">¥{{ fmtP(balance) }}</span></el-radio>
              </el-radio-group>
            </div>
            <div class="divider"></div>
            <div class="amt">
              <div class="ar"><span>商品总额</span><span>¥{{ fmtP(total) }}</span></div>
              <div class="ar" v-if="discount > 0"><span>优惠券抵扣</span><span style="color:#10b981;">-¥{{ fmtP(discount) }}</span></div>
              <div class="ar"><span>运费</span><span class="free">免运费</span></div>
              <div class="ar ar-total"><span>应付金额</span><span class="tp">¥{{ fmtP(finalTotal) }}</span></div>
            </div>
            <button class="sub" @click="submit" :disabled="subing">{{ subing ? '提交中...' : `提交订单 ¥${fmtP(finalTotal)}` }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http, { resolveImg } from '@/utils/http'
import { fallbackImg } from '@/utils/placeholder'

const route = useRoute()
const router = useRouter()
const gid = route.query.goodsId
const qty = parseInt(route.query.quantity || 1)
const initAcc = route.query.accountNumber || ''

const goods = ref({})
const acc = ref(initAcc)
const pay = ref('wechat')
const balance = ref(0)
const subing = ref(false)
/** 金额格式化为2位小数 */
const fmtP = (v) => (Number(v) || 0).toFixed(2)
/** 用户已领取的优惠券 */
const myCoupons = ref([])
/** 当前选中的优惠券 */
const pickedCoupon = ref(null)

const goodsImg = computed(() => {
  if (!goods.value) return ''
  return resolveImg(goods.value.goodsImg) || fallbackImg(goods.value.id, 90, 90)
})
const goodsName = computed(() => goods.value?.goodsName || goods.value?.info || '商品')

const total = computed(() => (goods.value.price || 0) * qty)
/** 优惠金额 */
const discount = computed(() => {
  if (!pickedCoupon.value) return 0
  // 检查是否满足最低消费
  if (total.value < (pickedCoupon.value.min_amount || 0)) return 0
  return Math.min(pickedCoupon.value.amount, total.value)
})
/** 实付金额 */
const finalTotal = computed(() => Math.max(0, total.value - discount.value))

/** 加载用户信息和优惠券 */
onMounted(async () => {
  if (!gid) { ElMessage.error('商品信息丢失，请返回重新选择'); return }
  try {
    const [gr, ur, cr] = await Promise.all([
      http.get('/goods/get', { params: { id: gid } }),
      http.post('/user/getLogin'),
      http.get('/coupon/my')
    ])
    goods.value = gr.data
    if (ur.data.code === 200 && ur.data.data) {
      balance.value = ur.data.data.balance || 0
    }
    if (cr.data.code === 200) {
      myCoupons.value = cr.data.data || []
    }
  } catch { /* 使用默认数据 */ }
})

/** 提交订单 */
const submit = async () => {
  if (!acc.value.trim()) return ElMessage.warning('请填写充值账号')
  // 余额支付时校验余额是否足够
  if (pay.value === 'balance' && finalTotal.value > balance.value) {
    return ElMessage.warning('余额不足，请充值或更换支付方式')
  }
  subing.value = true
  try {
    const params = {
      goodsId: parseInt(gid),
      accountNumber: acc.value,
      payMethod: pay.value,
      discount: discount.value  // 优惠抵扣
    }
    const r = await http.post('/order/create', params)
    if (r.data.code === 200) {
      // 使用优惠券
      if (pickedCoupon.value) {
        await http.post('/coupon/use', { userCouponId: pickedCoupon.value.user_coupon_id })
      }
      ElMessage.success('下单成功！')
      router.push('/order')
    } else {
      ElMessage.error(r.data.msg || '下单失败')
    }
  } catch { ElMessage.error('网络错误') }
  finally { subing.value = false }
}
</script>

<style scoped>
.page { min-height: 100vh; padding: 24px 24px 60px; }
.wrap { max-width: 900px; margin: 0 auto; }
.hd { margin-bottom: 22px; }
.hd h1 { font-size: 24px; font-weight: 700; color: var(--text-primary); margin-bottom: 2px; }
.hd p { color: var(--text-muted); font-size: 14px; }

.cols { display: flex; gap: 16px; flex-wrap: wrap; }
.col-l { flex: 2; min-width: 320px; }
.col-r { flex: 1; min-width: 280px; }

.card {
  background: #fff; border-radius: 16px;
  border: 1px solid var(--border-card); margin-bottom: 14px;
  overflow: hidden; box-shadow: var(--shadow-sm);
}
.chd {
  padding: 13px 18px; font-size: 14px; font-weight: 600;
  color: var(--text-primary); background: #f8fafc;
  border-bottom: 1px solid var(--border-card);
}

.grow { display: flex; gap: 14px; padding: 16px 18px; }
.gimg { width: 80px; height: 80px; border-radius: 10px; overflow: hidden; background: #f8fafc; flex-shrink: 0; }
.gimg img { width: 100%; height: 100%; object-fit: cover; }
.gmeta h3 { font-size: 15px; font-weight: 600; color: var(--text-primary); margin-bottom: 4px; }
.gsub { font-size: 13px; color: var(--text-muted); margin-bottom: 4px; }
.gtotal strong { color: var(--c-accent); font-size: 18px; }

.divider { height: 1px; background: var(--border-card); margin: 0 18px; }

.acc { padding: 16px 18px; }
.acc label { display: block; font-weight: 500; color: var(--text-primary); margin-bottom: 6px; font-size: 14px; }
.acc input {
  width: 100%; padding: 11px 14px; border: 1px solid var(--border-card);
  border-radius: 10px; font-size: 14px; outline: none;
  background: #f8fafc; color: var(--text-primary); transition: all 0.2s;
}
.acc input::placeholder { color: var(--text-muted); }
.acc input:focus { border-color: var(--c-primary); box-shadow: 0 0 0 3px rgba(99,102,241,0.06); }

.cph { padding: 14px 18px; display: flex; justify-content: space-between; align-items: center; color: var(--text-muted); font-size: 13px; }
.tbtn { background: none; border: none; color: var(--c-primary); cursor: pointer; font-size: 13px; font-weight: 500; }
.tbtn:hover { opacity: 0.7; }

/* 优惠券列表 */
.coupon-list { padding: 8px 10px; display: flex; flex-direction: column; gap: 4px; }
.coupon-option {
  display: flex; align-items: center; gap: 10px; padding: 10px 12px;
  border: 2px solid #e2e8f0; border-radius: 10px; cursor: pointer;
  transition: all 0.2s; background: #fafafa;
}
.coupon-option:hover { border-color: #f59e0b; }
.coupon-option.on { border-color: #f59e0b; background: #fffef5; }
.co-amount { font-size: 18px; font-weight: 800; color: #f59e0b; flex-shrink: 0; width: 50px; }
.co-info { flex: 1; font-size: 12px; color: #64748b; }
.co-radio { accent-color: #f59e0b; }

.sticky { position: sticky; top: 12px; }
.pay { padding: 14px 18px; }
.pay-t { font-weight: 500; color: var(--text-primary); margin-bottom: 10px; font-size: 14px; }
.pay-g { display: flex; flex-direction: column; gap: 8px; }
.pay-i {
  margin: 0 !important; padding: 11px 14px;
  border: 1px solid var(--border-card); border-radius: 10px;
  display: flex; align-items: center; justify-content: space-between; width: 100%;
  transition: all 0.2s; background: #f8fafc;
}
.pay-i:hover { border-color: var(--c-primary); background: #fff; }
.pay-tag { background: rgba(99,102,241,0.08); color: var(--c-primary); font-size: 10px; padding: 2px 7px; border-radius: 6px; font-weight: 600; }
.pay-hint { font-size: 12px; color: var(--text-muted); }

.amt { padding: 10px 18px; }
.ar { display: flex; justify-content: space-between; padding: 7px 0; font-size: 14px; color: var(--text-secondary); }
.ar-total { margin-top: 4px; padding-top: 12px; border-top: 1px solid var(--border-card); font-size: 16px; font-weight: 600; color: var(--text-primary); }
.tp { color: var(--c-accent); font-size: 22px; }
.free { color: var(--c-success); }

.sub {
  margin: 8px 18px 18px; width: calc(100% - 36px); padding: 13px;
  border: none; border-radius: 12px;
  background: var(--grad-primary); color: #fff;
  font-size: 15px; font-weight: 700; cursor: pointer; letter-spacing: 1px;
  transition: all 0.25s; box-shadow: 0 4px 14px rgba(99,102,241,0.2);
}
.sub:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(99,102,241,0.35); }
.sub:disabled { opacity: 0.5; cursor: not-allowed; }

@media (max-width: 768px) {
  .page { padding: 16px 14px 40px; }
  .cols { flex-direction: column; }
  .sticky { position: static; }
}
</style>
