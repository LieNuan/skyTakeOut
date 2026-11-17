// 统一请求封装
// 后端统一返回体：{ code: 1成功, msg, data }
// 后端 JwtTokenAdminInterceptor 从请求头 token 中读取令牌

const TOKEN_KEY = 'sky_admin_token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

// 失效的 OSS 桶（黑马课程官方桶已过期，返回 403）
const DEAD_OSS = 'sky-itcast.oss-cn-beijing.aliyuncs.com'

/**
 * 获取菜品/套餐展示图：若原图是已失效的 OSS 地址，则用 AI 生成的美食图兜底
 * @param {string} name 菜品/套餐名称
 * @param {string} image 数据库里的图片地址
 */
export function getFoodImage(name, image) {
  if (image && !image.includes(DEAD_OSS)) return image
  const prompt = encodeURIComponent(`${name} 美食摄影 高清 诱人`)
  return `https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=${prompt}&image_size=square`
}

/**
 * 发起请求
 * @param {string} url 接口地址（以 / 开头，走 vite 代理）
 * @param {object} options { method, data, params }
 */
export default async function request(url, options = {}) {
  const headers = { 'Content-Type': 'application/json' }
  const token = getToken()
  if (token) headers.token = token

  // 拼接 query 参数
  let fullUrl = url
  if (options.params) {
    const search = new URLSearchParams()
    Object.keys(options.params).forEach((key) => {
      const val = options.params[key]
      if (val !== undefined && val !== null && val !== '') {
        search.append(key, val)
      }
    })
    const qs = search.toString()
    if (qs) fullUrl += (url.includes('?') ? '&' : '?') + qs
  }

  let response
  try {
    response = await fetch(fullUrl, {
      method: options.method || 'GET',
      headers,
      body: options.data ? JSON.stringify(options.data) : undefined
    })
  } catch (e) {
    throw new Error('网络异常，请检查后端是否启动')
  }

  // 令牌失效
  if (response.status === 401) {
    removeToken()
    window.location.reload()
    throw new Error('登录已过期，请重新登录')
  }

  const body = await response.json().catch(() => {
    throw new Error('服务异常，请稍后重试')
  })

  if (body.code !== 1) {
    throw new Error(body.msg || '操作失败')
  }
  return body.data
}

/**
 * 文件上传（multipart/form-data，由浏览器自动设置 Content-Type）
 * @param {File} file 上传的文件
 * @returns {string} 文件名（拼接 /Photos/ 即可访问）
 */
export async function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)

  const headers = {}
  const token = getToken()
  if (token) headers.token = token

  let response
  try {
    response = await fetch('/admin/common/upload', {
      method: 'POST',
      headers,
      body: formData
    })
  } catch (e) {
    throw new Error('网络异常，请检查后端是否启动')
  }

  if (response.status === 401) {
    removeToken()
    window.location.reload()
    throw new Error('登录已过期，请重新登录')
  }

  const body = await response.json().catch(() => {
    throw new Error('服务异常，请稍后重试')
  })

  if (body.code !== 1) {
    throw new Error(body.msg || '上传失败')
  }
  return body.data
}

/**
 * 上传后拼接可访问的图片地址
 * @param {string} fileName 上传接口返回的文件名
 */
export function getImageUrl(fileName) {
  if (!fileName) return ''
  if (fileName.startsWith('http')) return fileName
  return `/Photos/${fileName}`
}
