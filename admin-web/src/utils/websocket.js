/**
 * 管理端 WebSocket 客户端（可独立复用，不依赖 Vue）
 *
 * 后端端点：/ws/{sid}
 * 消息体：{ type: 1|2, orderId: number, content: string }
 *   type = 1 来单提醒（支付成功）
 *   type = 2 客户催单
 *
 * 用法：
 *   const socket = new SkyWebSocket()
 *   socket.on('open', () => {})
 *   socket.on('message', (msg) => console.log(msg.type, msg.orderId, msg.content))
 *   socket.connect()
 *   // 组件销毁时
 *   socket.close()
 */
export default class SkyWebSocket {
  /**
   * @param {object} [options]
   * @param {string} [options.clientId] 客户端标识，默认随机生成
   * @param {string} [options.url] 完整 ws 地址，默认按当前页面 host + 后端 8080 端口拼接
   * @param {number} [options.heartbeatInterval] 心跳间隔（毫秒），0 表示不发送
   * @param {number} [options.reconnectBaseDelay] 首次重连等待时间（毫秒）
   * @param {number} [options.maxReconnectDelay] 重连等待上限（毫秒）
   */
  constructor(options = {}) {
    const {
      clientId,
      url,
      heartbeatInterval = 30000,
      reconnectBaseDelay = 2000,
      maxReconnectDelay = 30000
    } = options

    this.clientId = clientId || 'admin_' + Math.random().toString(36).slice(2, 10)
    this.url = url || SkyWebSocket.buildUrl(this.clientId)
    this.heartbeatInterval = heartbeatInterval
    this.reconnectBaseDelay = reconnectBaseDelay
    this.maxReconnectDelay = maxReconnectDelay

    this.socket = null
    this.connected = false
    this.manualClosed = false
    this.reconnectAttempts = 0
    this.heartbeatTimer = null
    this.reconnectTimer = null
    this.handlers = {}
  }

  /**
   * 默认地址：ws://{当前 hostname}:8080/ws/{clientId}
   * WebSocket 不受同源策略限制，所以无需走 vite 代理。
   */
  static buildUrl(clientId) {
    const scheme = window.location.protocol === 'https:' ? 'wss' : 'ws'
    return scheme + '://' + window.location.hostname + ':8080/ws/' + clientId
  }

  on(event, handler) {
    if (!this.handlers[event]) {
      this.handlers[event] = []
    }
    this.handlers[event].push(handler)
    return this
  }

  off(event, handler) {
    const list = this.handlers[event]
    if (!list) {
      return this
    }
    this.handlers[event] = handler ? list.filter((fn) => fn !== handler) : []
    return this
  }

  emit(event, payload) {
    const list = this.handlers[event] || []
    list.forEach((fn) => {
      try {
        fn(payload)
      } catch (e) {
        console.error('[ws] ' + event + ' 回调执行异常', e)
      }
    })
  }

  connect() {
    this.manualClosed = false
    if (
      this.socket &&
      (this.socket.readyState === WebSocket.OPEN || this.socket.readyState === WebSocket.CONNECTING)
    ) {
      return this
    }

    this.socket = new WebSocket(this.url)

    this.socket.onopen = () => {
      this.connected = true
      this.reconnectAttempts = 0
      console.log('[ws] 已连接', this.url)
      this.startHeartbeat()
      this.emit('open')
    }

    this.socket.onmessage = (event) => {
      let message = event.data
      try {
        message = JSON.parse(event.data)
      } catch (e) {
        // 非 JSON 消息（如测试文本）原样透传
      }
      this.emit('message', message)
    }

    this.socket.onclose = (event) => {
      this.connected = false
      this.stopHeartbeat()
      this.emit('close', event)
      if (!this.manualClosed) {
        this.scheduleReconnect()
      }
    }

    this.socket.onerror = (event) => {
      this.emit('error', event)
    }

    return this
  }

  /**
   * 指数退避重连：2s、4s、8s……最多 30s
   */
  scheduleReconnect() {
    if (this.reconnectTimer) {
      return
    }
    const delay = Math.min(
      this.reconnectBaseDelay * Math.pow(2, this.reconnectAttempts),
      this.maxReconnectDelay
    )
    this.reconnectAttempts += 1
    console.warn('[ws] ' + delay + 'ms 后重连（第 ' + this.reconnectAttempts + ' 次）')
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null
      this.connect()
    }, delay)
  }

  startHeartbeat() {
    this.stopHeartbeat()
    if (!this.heartbeatInterval) {
      return
    }
    this.heartbeatTimer = setInterval(() => {
      if (this.connected && this.socket) {
        this.socket.send('ping')
      }
    }, this.heartbeatInterval)
  }

  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  close() {
    this.manualClosed = true
    this.stopHeartbeat()
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    if (this.socket) {
      this.socket.close()
      this.socket = null
    }
    this.connected = false
  }
}

export function createOrderSocket(options) {
  return new SkyWebSocket(options)
}
