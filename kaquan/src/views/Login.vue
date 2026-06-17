<template>
  <div class="page">
    <div class="card">
      <div class="card-left">
        <div class="cl-inner">
          <div class="cl-logo">🎫</div>
          <h1>卡券商城</h1>
          <p>精选好券 · 即刻拥有</p>
          <div class="cl-features">
            <div class="clf">✓ 海量卡券任你选</div>
            <div class="clf">✓ 秒速到账即买即用</div>
            <div class="clf">✓ 新用户专享优惠</div>
          </div>
        </div>
      </div>
      <div class="card-right">
        <h2>{{ tab==='login'?'欢迎回来':'创建账号' }}</h2>
        <p class="subtitle">{{ tab==='login'?'登录您的账号':'注册即享专属福利' }}</p>

        <div class="tabs">
          <button :class="{ on:tab==='login' }" @click="tab='login'">登录</button>
          <button :class="{ on:tab==='reg' }" @click="tab='reg'">注册</button>
        </div>

        <form v-if="tab==='login'" class="fm" @submit.prevent="doLogin">
          <div class="inp"><span class="ii">👤</span><input v-model="lf.username" type="text" placeholder="请输入账号" /></div>
          <div class="inp"><span class="ii">🔒</span><input v-model="lf.password" :type="sp1?'text':'password'" placeholder="请输入密码" /><span class="is" @click="sp1=!sp1">{{ sp1?'🙈':'👁' }}</span></div>
          <button class="sub" type="submit">登 录</button>
        </form>

        <form v-if="tab==='reg'" class="fm" @submit.prevent="doReg">
          <div class="inp"><span class="ii">👤</span><input v-model="rf.username" type="text" placeholder="请输入账号" /></div>
          <div class="inp"><span class="ii">🔒</span><input v-model="rf.password" :type="sp2?'text':'password'" placeholder="请输入密码" /><span class="is" @click="sp2=!sp2">{{ sp2?'🙈':'👁' }}</span></div>
          <div class="inp"><span class="ii">🎫</span><input v-model="rf.inviteCode" type="text" placeholder="邀请码（选填）" /></div>
          <button class="sub sreg" type="submit">注 册</button>
        </form>

        <div class="back"><router-link to="/index">← 返回首页</router-link></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '@/utils/http'

const router = useRouter()
const route = useRoute()
/** 从URL读取邀请码 */
const inviteCode = route.query.invite || ''
const tab = ref('login')
const sp1 = ref(false); const sp2 = ref(false)
const lf = ref({ username:'user1', password:'123456' })
const rf = ref({ username:'', password:'', inviteCode: route.query.invite || '' })

const doLogin = async () => {
  if (!lf.value.username || !lf.value.password) return ElMessage.warning('请填写账号和密码')
  const r = await http.post('/user/login', lf.value)
  if (r.data.code===200) { ElMessage.success('登录成功'); router.push('/index') }
  else ElMessage.error('账号或密码错误')
}
const doReg = async () => {
  if (!rf.value.username || !rf.value.password) return ElMessage.warning('请填写账号和密码')
  const r = await http.post('/user/reg', { username: rf.value.username, password: rf.value.password, inviteCode: inviteCode })
  if (r.data.code===200) { ElMessage.success('注册成功，请登录'); tab.value='login' }
  else ElMessage.error('账号已存在')
}
</script>

<style scoped>
.page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #eef2ff 0%, #f0e6ff 40%, #e0f2fe 100%);
  padding: 20px;
}
.card {
  display: flex; width: 880px; min-height: 500px;
  background: #fff; border-radius: 24px;
  box-shadow: 0 20px 60px rgba(99,102,241,0.1);
  overflow: hidden;
}
.card-left {
  width: 42%; background: var(--grad-primary);
  display: flex; align-items: center; justify-content: center;
  padding: 40px 28px;
}
.cl-inner { text-align: center; color: #fff; }
.cl-logo { font-size: 56px; margin-bottom: 8px; }
.cl-inner h1 { font-size: 28px; font-weight: 800; letter-spacing: 2px; }
.cl-inner > p { font-size: 14px; opacity: 0.8; margin: 8px 0 32px; }
.cl-features { display: flex; flex-direction: column; gap: 12px; text-align: left; }
.clf { display: flex; align-items: center; gap: 8px; font-size: 14px; opacity: 0.9; }

.card-right { flex: 1; padding: 40px 36px; display: flex; flex-direction: column; justify-content: center; }
.card-right h2 { font-size: 24px; font-weight: 700; color: var(--text-primary); }
.subtitle { font-size: 14px; color: var(--text-muted); margin: 6px 0 20px; }

.tabs { display: flex; background: #f1f5f9; border-radius: 12px; padding: 3px; margin-bottom: 24px; }
.tabs button {
  flex: 1; padding: 9px; border-radius: 10px; border: none;
  font-size: 14px; font-weight: 600; cursor: pointer;
  background: transparent; color: var(--text-muted); transition: all 0.3s;
}
.tabs button.on { background: #fff; color: var(--c-primary); box-shadow: var(--shadow-sm); }

.fm { display: flex; flex-direction: column; gap: 14px; }
.inp {
  display: flex; align-items: center; background: #f8fafc;
  border-radius: 12px; border: 1px solid var(--border-card); transition: all 0.3s;
}
.inp:focus-within { border-color: var(--c-primary); background: #fff; box-shadow: 0 0 0 3px rgba(99,102,241,0.06); }
.ii { width: 44px; text-align: center; font-size: 16px; flex-shrink: 0; }
.inp input { flex: 1; padding: 13px 0; border: none; background: transparent; color: var(--text-primary); font-size: 14px; outline: none; }
.inp input::placeholder { color: var(--text-muted); }
.is { width: 44px; text-align: center; cursor: pointer; font-size: 14px; color: var(--text-muted); }
.is:hover { color: var(--text-secondary); }

.sub {
  width: 100%; padding: 13px; border: none; border-radius: 12px;
  font-size: 15px; font-weight: 700; cursor: pointer; letter-spacing: 3px;
  background: var(--grad-primary); color: #fff; margin-top: 4px;
  transition: all 0.3s; box-shadow: 0 4px 14px rgba(99,102,241,0.2);
}
.sub:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(99,102,241,0.35); }
.sreg { background: var(--grad-accent); box-shadow: 0 4px 14px rgba(244,63,94,0.2); }
.sreg:hover { box-shadow: 0 6px 20px rgba(244,63,94,0.35); }

.back { margin-top: 22px; text-align: center; }
.back a { font-size: 13px; color: var(--text-muted); text-decoration: none; }
.back a:hover { color: var(--c-primary); }

@media (max-width: 768px) {
  .card { flex-direction: column; width: calc(100vw - 32px); min-height: auto; }
  .card-left { width: 100%; padding: 24px 20px; }
  .cl-features { flex-direction: row; flex-wrap: wrap; justify-content: center; gap: 8px; }
  .clf { font-size: 12px; }
  .card-right { padding: 24px 20px; }
}
</style>
