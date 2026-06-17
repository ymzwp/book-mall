<template>
  <div class="page">
    <div class="card">
      <div class="head">
        <span class="logo">🎫</span>
        <h1>卡券商城</h1>
        <p>后台管理系统</p>
      </div>
      <form @submit.prevent="doLogin" class="fm">
        <div class="inp"><span class="ii">👤</span><input v-model="f.username" type="text" placeholder="管理员账号" /></div>
        <div class="inp"><span class="ii">🔒</span><input v-model="f.password" :type="sp ? 'text' : 'password'" placeholder="管理员密码" /><span class="is" @click="sp = !sp">{{ sp ? '🙈' : '👁' }}</span></div>
        <label class="cb"><input type="checkbox" v-model="rm" /><span class="cm"></span>记住账号</label>
        <button class="btn" :disabled="ld">{{ ld ? '登录中...' : '登录后台' }}</button>
      </form>
      <div class="ft"><router-link to="/index">← 返回商城首页</router-link></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '@/utils/http'

const router = useRouter()
const f = ref({ username: 'admin', password: '123456' })
const rm = ref(true)
const ld = ref(false)
const sp = ref(false)

const doLogin = async () => {
  ld.value = true
  try {
    const r = await http.post('/admin/login', f.value)
    if (r.data.code === 200) {
      ElMessage.success('登录成功')
      if (rm.value) {
        localStorage.setItem('admin_username', f.value.username)
        localStorage.setItem('admin_password', f.value.password)
        localStorage.setItem('admin_remember', 'true')
      } else {
        localStorage.removeItem('admin_username'); localStorage.removeItem('admin_password')
        localStorage.setItem('admin_remember', 'false')
      }
      router.push('/admin')
    } else ElMessage.error('账号或密码错误')
  } catch { ElMessage.error('网络错误') }
  finally { ld.value = false }
}

onMounted(() => {
  if (localStorage.getItem('admin_remember') === 'true') {
    f.value.username = localStorage.getItem('admin_username') || 'admin'
    f.value.password = localStorage.getItem('admin_password') || ''
    rm.value = true
  }
})
</script>

<style scoped>
.page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #eef2ff 0%, #f0e6ff 40%, #e0f2fe 100%);
  padding: 20px;
}
.card {
  width: 400px; background: #fff; border-radius: 24px;
  padding: 36px 32px; box-shadow: 0 20px 60px rgba(99,102,241,0.08);
}
.head { text-align: center; margin-bottom: 28px; }
.logo { font-size: 44px; display: block; margin-bottom: 8px; }
.head h1 { font-size: 24px; font-weight: 800; color: var(--text-primary); letter-spacing: 1px; }
.head p { font-size: 13px; color: var(--text-muted); margin-top: 4px; }

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

.cb { display: flex; align-items: center; gap: 8px; font-size: 13px; color: var(--text-muted); cursor: pointer; }
.cb input { display: none; }
.cm {
  width: 18px; height: 18px; border-radius: 5px;
  border: 1px solid var(--border-card); display: flex;
  align-items: center; justify-content: center; transition: all 0.2s;
}
.cb input:checked + .cm { background: var(--grad-primary); border-color: transparent; }
.cb input:checked + .cm::after { content: '✓'; font-size: 11px; color: #fff; font-weight: 700; }

.btn {
  width: 100%; padding: 13px; border: none; border-radius: 12px;
  font-size: 15px; font-weight: 700; cursor: pointer; letter-spacing: 2px;
  background: var(--grad-primary); color: #fff; margin-top: 4px;
  transition: all 0.3s; box-shadow: 0 4px 14px rgba(99,102,241,0.2);
}
.btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(99,102,241,0.35); }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }

.ft { text-align: center; margin-top: 20px; }
.ft a { font-size: 13px; color: var(--text-muted); text-decoration: none; }
.ft a:hover { color: var(--c-primary); }
</style>
