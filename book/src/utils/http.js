import axios from 'axios'

const BASE = 'http://localhost:8088'

const http = axios.create({
  baseURL: BASE,
  withCredentials: true   // 允许携带 cookie/session
})

/**
 * 解析商品图片URL：相对路径补全为后端完整地址
 */
export function resolveImg(src) {
  if (!src) return ''
  if (src.startsWith('http://') || src.startsWith('https://') || src.startsWith('data:')) return src
  return BASE + (src.startsWith('/') ? '' : '/') + src
}

export default http