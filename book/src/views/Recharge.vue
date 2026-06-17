<template>
  <div class="recharge-page">
    <!-- 顶部导航条 -->
    <header class="rc-topbar">
      <button class="rc-back" @click="$router.back()">← 返回</button>
      <h2 class="rc-title">账户充值</h2>
      <div class="rc-topbar-right">
        <span class="rc-top-label">当前余额</span>
        <span class="rc-top-balance">¥ {{ balance }}</span>
      </div>
    </header>

    <!-- 主体：双栏布局 -->
    <div class="rc-body">
      <!-- 左栏：充值表单 -->
      <div class="rc-left">
        <!-- 充值金额 -->
        <section class="rc-card">
          <h3 class="rc-card-title">💰 选择充值金额</h3>
          <div class="rc-amount-grid">
            <button
              v-for="opt in amountOptions"
              :key="opt"
              class="rc-amount-chip"
              :class="{ active: customAmount === opt }"
              @click="pickAmount(opt)"
            >
              <span class="rc-amount-num">¥{{ opt }}</span>
              <span v-if="opt >= 100" class="rc-amount-tag">推荐</span>
            </button>
            <button
              class="rc-amount-chip rc-amount-custom"
              :class="{ active: isCustom }"
              @click="pickAmount(null)"
            >
              <span class="rc-amount-num">自定义</span>
            </button>
          </div>

          <div v-if="isCustom" class="rc-custom-input">
            <span class="rc-custom-prefix">¥</span>
            <input
              v-model="customInput"
              type="number"
              placeholder="请输入充值金额"
              min="1"
              step="0.01"
              class="rc-input"
              @input="onCustomInput"
            />
          </div>
        </section>

        <!-- 支付方式 -->
        <section class="rc-card">
          <h3 class="rc-card-title">💳 选择支付方式</h3>
          <div class="rc-pay-grid">
            <label
              v-for="m in payMethods"
              :key="m.key"
              class="rc-pay-card"
              :class="{ active: payMethod === m.key }"
            >
              <input
                v-model="payMethod"
                type="radio"
                :value="m.key"
                class="rc-pay-radio"
              />
              <span class="rc-pay-icon">{{ m.icon }}</span>
              <div class="rc-pay-info">
                <span class="rc-pay-name">{{ m.name }}</span>
                <span class="rc-pay-desc">{{ m.desc }}</span>
              </div>
              <span v-if="payMethod === m.key" class="rc-pay-check">✓</span>
            </label>
          </div>
        </section>
      </div>

      <!-- 右栏：订单摘要 -->
      <div class="rc-right">
        <div class="rc-summary">
          <h3 class="rc-card-title">📋 充值确认</h3>

          <div class="rc-summary-rows">
            <div class="rc-summary-row">
              <span>充值金额</span>
              <span class="rc-summary-amount">¥{{ finalAmount }}</span>
            </div>
            <div class="rc-summary-row">
              <span>支付方式</span>
              <span>{{ payMethodLabel }}</span>
            </div>
            <div class="rc-summary-row">
              <span>当前余额</span>
              <span>¥{{ balance }}</span>
            </div>
            <div class="rc-summary-divider"></div>
            <div class="rc-summary-row rc-summary-total">
              <span>充值后余额</span>
              <span>¥{{ afterBalance }}</span>
            </div>
          </div>

          <div class="rc-summary-tip">
            💡 充值后余额将实时到账，可用于购买商城中的各类卡券商品。
          </div>

          <button
            class="rc-submit"
            :disabled="!canSubmit || submitting"
            @click="doRecharge"
          >
            {{ submitting ? '提交中...' : `确认充值 ¥${finalAmount}` }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/utils/http'

const router = useRouter()

const balance = ref('0.00')

/* ===== 金额选择 ===== */
const amountOptions = [10, 20, 50, 100, 200, 500]
const customAmount = ref(null)
const customInput = ref('')
const isCustom = computed(() => customAmount.value === null)

const pickAmount = (val) => {
  customAmount.value = val
  if (val !== null) customInput.value = ''
}

const onCustomInput = () => {
  if (Number(customInput.value) < 0) customInput.value = '0'
}

const finalAmount = computed(() => {
  if (customAmount.value !== null) return customAmount.value.toFixed(2)
  const n = Number(customInput.value)
  if (isNaN(n) || n <= 0) return '0.00'
  return n.toFixed(2)
})

const canSubmit = computed(() => Number(finalAmount.value) > 0)

/* ===== 充值后余额 ===== */
const afterBalance = computed(() => {
  const cur = parseFloat(balance.value) || 0
  const add = parseFloat(finalAmount.value) || 0
  return (cur + add).toFixed(2)
})

/* ===== 支付方式 ===== */
const payMethod = ref('alipay')
const payMethods = [
  { key: 'alipay', icon: '💙', name: '支付宝', desc: '推荐使用，支持花呗' },
  { key: 'wechat', icon: '💚', name: '微信支付', desc: '微信扫码支付' },
  { key: 'bank', icon: '🏦', name: '银行卡', desc: '支持主流银行借记卡/信用卡' },
]

const payMethodLabel = computed(() => {
  const m = payMethods.find(p => p.key === payMethod.value)
  return m ? m.name : ''
})

/* ===== 加载余额 ===== */
const loadBalance = async () => {
  try {
    const r = await http.post('/user/getLogin')
    if (r.data.code === 200 && r.data.data) {
      balance.value = (r.data.data.balance || 0).toFixed(2)
    }
  } catch { /* 使用默认值 */ }
}

/* ===== 提交充值 ===== */
const submitting = ref(false)
const doRecharge = async () => {
  if (!canSubmit.value) return ElMessage.warning('请选择或输入充值金额')
  // 敏感操作：密码确认
  let pwd = ''
  try {
    const { value } = await ElMessageBox.prompt('请输入登录密码确认充值', '安全验证', {
      confirmButtonText: '确认', cancelButtonText: '取消',
      inputType: 'password', inputPlaceholder: '请输入密码',
      inputValidator: (v) => v ? true : '密码不能为空'
    })
    pwd = value
  } catch { return }
  submitting.value = true
  try {
    const r = await http.post('/recharge/create', {
      amount: Number(finalAmount.value),
      payMethod: payMethod.value,
      password: pwd
    })
    if (r.data.code === 200) {
      ElMessage.success('充值成功')
      setTimeout(() => router.push('/mine?tab=wallet'), 600)
    } else {
      ElMessage.error(r.data.msg || '充值失败，请稍后重试')
    }
  } catch {
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(loadBalance)
</script>

<style scoped>
/* ===== 整体布局 ===== */
.recharge-page {
  min-height: 100vh;
  background: #f1f5f9;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 24px 60px;
}

/* ===== 顶部栏 ===== */
.rc-topbar {
  width: 100%;
  max-width: 1060px;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px 0 20px;
}
.rc-back {
  background: #fff;
  border: 1px solid #e2e8f0;
  color: #475569;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 10px;
  transition: all 0.2s;
  white-space: nowrap;
}
.rc-back:hover {
  border-color: #6366f1;
  color: #6366f1;
}
.rc-title {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
  flex: 1;
}
.rc-topbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px 20px;
}
.rc-top-label {
  font-size: 12px;
  color: #94a3b8;
}
.rc-top-balance {
  font-size: 18px;
  font-weight: 800;
  color: #6366f1;
}

/* ===== 双栏主体 ===== */
.rc-body {
  width: 100%;
  max-width: 1060px;
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 20px;
  align-items: start;
}

/* ===== 通用卡片 ===== */
.rc-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px 32px;
  margin-bottom: 16px;
  border: 1px solid #e2e8f0;
}
.rc-card-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 20px;
}

/* ===== 左栏 ===== */
.rc-left {
  min-width: 0;
}

/* ===== 金额选择网格 ===== */
.rc-amount-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.rc-amount-chip {
  position: relative;
  padding: 20px 10px 16px;
  border-radius: 14px;
  border: 2px solid #e2e8f0;
  background: #f8fafc;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.rc-amount-chip:hover {
  border-color: #a5b4fc;
  background: #f5f3ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(99,102,241,0.08);
}
.rc-amount-chip.active {
  border-color: #6366f1;
  background: rgba(99,102,241,0.06);
  box-shadow: 0 0 0 3px rgba(99,102,241,0.12);
}
.rc-amount-num {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
}
.rc-amount-chip.active .rc-amount-num {
  color: #6366f1;
}
.rc-amount-tag {
  position: absolute;
  top: -8px;
  right: -6px;
  background: linear-gradient(135deg, #f43f5e, #f97316);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 10px;
  box-shadow: 0 2px 6px rgba(244,63,94,0.3);
}
.rc-amount-custom .rc-amount-num {
  font-size: 15px;
  color: #94a3b8;
}
.rc-amount-custom.active .rc-amount-num {
  color: #6366f1;
}

/* ===== 自定义金额输入 ===== */
.rc-custom-input {
  display: flex;
  align-items: center;
  margin-top: 16px;
  background: #f8fafc;
  border: 2px solid #e2e8f0;
  border-radius: 14px;
  overflow: hidden;
  transition: border-color 0.2s;
}
.rc-custom-input:focus-within {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99,102,241,0.06);
}
.rc-custom-prefix {
  font-size: 22px;
  font-weight: 800;
  color: #6366f1;
  padding: 0 20px;
}
.rc-input {
  flex: 1;
  border: none;
  background: transparent;
  padding: 18px 18px 18px 0;
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  outline: none;
  width: 100%;
}
.rc-input::placeholder {
  color: #cbd5e1;
  font-weight: 400;
  font-size: 16px;
}

/* ===== 支付方式 ===== */
.rc-pay-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.rc-pay-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 12px 16px;
  border-radius: 14px;
  border: 2px solid #e2e8f0;
  background: #f8fafc;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
  position: relative;
}
.rc-pay-card:hover {
  border-color: #a5b4fc;
  background: #f5f3ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(99,102,241,0.08);
}
.rc-pay-card.active {
  border-color: #6366f1;
  background: rgba(99,102,241,0.04);
  box-shadow: 0 0 0 3px rgba(99,102,241,0.12);
}
.rc-pay-radio {
  position: absolute;
  top: 10px;
  left: 12px;
  accent-color: #6366f1;
  width: 16px;
  height: 16px;
  cursor: pointer;
}
.rc-pay-icon {
  font-size: 32px;
  line-height: 1;
}
.rc-pay-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.rc-pay-name {
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
}
.rc-pay-desc {
  font-size: 11px;
  color: #94a3b8;
}
.rc-pay-check {
  position: absolute;
  top: 8px;
  right: 10px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #6366f1;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ===== 右栏 - 订单摘要 ===== */
.rc-summary {
  background: #fff;
  border-radius: 16px;
  padding: 28px 28px 24px;
  border: 1px solid #e2e8f0;
  position: sticky;
  top: 20px;
}
.rc-summary .rc-card-title {
  margin-bottom: 24px;
}
.rc-summary-rows {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.rc-summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #475569;
}
.rc-summary-amount {
  font-weight: 700;
  color: #0f172a;
  font-size: 16px;
}
.rc-summary-divider {
  height: 1px;
  background: #e2e8f0;
  margin: 4px 0;
}
.rc-summary-total {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}
.rc-summary-total span:last-child {
  font-size: 20px;
  color: #6366f1;
}
.rc-summary-tip {
  margin: 18px 0 20px;
  padding: 14px 16px;
  background: #f8fafc;
  border-radius: 10px;
  font-size: 12px;
  color: #64748b;
  line-height: 1.7;
}
.rc-submit {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.25s;
}
.rc-submit:hover:not(:disabled) {
  box-shadow: 0 6px 28px rgba(99,102,241,0.35);
  transform: translateY(-1px);
}
.rc-submit:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

/* ===== 响应式：小屏回退单栏 ===== */
@media (max-width: 860px) {
  .recharge-page {
    padding: 0 12px 40px;
  }
  .rc-topbar {
    flex-wrap: wrap;
    gap: 12px;
  }
  .rc-topbar-right {
    width: 100%;
    justify-content: center;
  }
  .rc-body {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  .rc-amount-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .rc-pay-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }
  .rc-pay-card {
    padding: 14px 8px 12px;
  }
  .rc-pay-icon {
    font-size: 24px;
  }
  .rc-summary {
    position: static;
  }
}
</style>
