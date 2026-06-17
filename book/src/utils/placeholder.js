/**
 * 根据商品ID生成真实占位图URL（picsum 稳定随机图）
 */
export function fallbackImg(id, width = 300, height = 300) {
  return `https://picsum.photos/seed/goods${id || 0}/${width}/${height}`
}

/**
 * 根据文本生成彩色占位图 data URI
 */
export function placeholderImg(text = '', width = 300, height = 300) {
  const hue = hashStr(text) % 360
  const bg = `hsl(${hue}, 45%, 88%)`
  const fg = `hsl(${hue}, 50%, 40%)`
  const line = text.length > 8 ? text.slice(0, 8) + '…' : text
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}">
    <rect fill="${bg}" width="${width}" height="${height}"/>
    <text fill="${fg}" font-size="${Math.max(14, width / 18)}" text-anchor="middle"
          x="${width / 2}" y="${height / 2 + 6}" font-family="sans-serif">${escapeXml(line)}</text>
  </svg>`
  return `data:image/svg+xml;base64,${btoa(unescape(encodeURIComponent(svg)))}`
}

function hashStr(s) {
  let h = 0
  for (let i = 0; i < s.length; i++) {
    h = ((h << 5) - h + s.charCodeAt(i)) | 0
  }
  return Math.abs(h)
}

function escapeXml(s) {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')
}
