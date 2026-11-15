<template>
  <span class="order-notify">
    <!-- 语音播报：来单提醒 -->
    <audio ref="newOrderAudio" :src="newOrderAudioSrc" preload="auto"></audio>
    <!-- 语音播报：客户催单 -->
    <audio ref="reminderAudio" :src="reminderAudioSrc" preload="auto"></audio>
  </span>
</template>

<script>
import SkyWebSocket from '@/utils/websocket'
import newOrderAudioSrc from '@/assets/audio/new-order.mp3'
import reminderAudioSrc from '@/assets/audio/reminder.mp3'

/**
 * 来单提醒 / 客户催单 消息接收组件
 *
 * 用法：
 *   <order-notify @open="..." @close="..." @message="onOrderMessage" />
 *
 * 事件：
 *   open    WebSocket 连接建立
 *   close   WebSocket 连接断开
 *   message 收到业务消息 { type: 1|2, orderId, content }
 */
export default {
  name: 'OrderNotify',
  data() {
    return {
      newOrderAudioSrc,
      reminderAudioSrc,
      audioCtx: null
    }
  },
  created() {
    this.socket = new SkyWebSocket()
    this.socket
      .on('open', () => this.$emit('open'))
      .on('close', () => this.$emit('close'))
      .on('error', (e) => console.error('[ws] 连接错误', e))
      .on('message', this.handleMessage)
    this.socket.connect()
  },
  mounted() {
    // 浏览器要求先有用户交互才允许播放声音，首次点击时预解锁两段语音
    document.addEventListener('click', this.unlockAudio, { once: true })
  },
  beforeDestroy() {
    document.removeEventListener('click', this.unlockAudio)
    this.socket.close()
    if (this.audioCtx) {
      this.audioCtx.close()
      this.audioCtx = null
    }
  },
  methods: {
    handleMessage(msg) {
      const type = Number(msg.type)
      if (type === 1) {
        this.showNotify('新订单提醒', 'success', msg, 'newOrderAudio')
      } else if (type === 2) {
        this.showNotify('客户催单', 'warning', msg, 'reminderAudio')
      } else {
        console.warn('[ws] 未知消息类型', msg)
      }
      this.$emit('message', { ...msg, type })
    },

    showNotify(title, notifyType, msg, audioRef) {
      this.$notify({
        title: title,
        message: msg.content + (msg.orderId ? '（订单ID：' + msg.orderId + '）' : ''),
        type: notifyType,
        duration: 8000,
        position: 'top-right'
      })
      this.playVoice(audioRef)
    },

    /**
     * 播放语音播报；被浏览器自动播放策略拦截时退化为合成提示音
     */
    playVoice(refName) {
      const audio = this.$refs[refName]
      if (!audio) {
        return
      }
      audio.currentTime = 0
      const promise = audio.play()
      if (promise && promise.catch) {
        promise
          .then(() => console.log('[voice] 播放', refName))
          .catch((err) => {
            console.warn('[voice] 语音播放被拦截，请先点击页面一次', err)
            this.beep()
          })
      }
    },

    /**
     * 首次点击页面时静音播放再暂停，完成音频解锁
     */
    unlockAudio() {
      ;['newOrderAudio', 'reminderAudio'].forEach((name) => {
        const audio = this.$refs[name]
        if (!audio) {
          return
        }
        audio.muted = true
        const promise = audio.play()
        if (!promise || !promise.then) {
          audio.muted = false
          return
        }
        promise
          .then(() => {
            audio.pause()
            audio.currentTime = 0
            audio.muted = false
          })
          .catch(() => {
            audio.muted = false
          })
      })
    },

    /**
     * 语音播放失败时的兜底提示音
     */
    beep() {
      const AudioCtx = window.AudioContext || window.webkitAudioContext
      if (!AudioCtx) {
        return
      }
      if (!this.audioCtx) {
        this.audioCtx = new AudioCtx()
      }
      const ctx = this.audioCtx
      if (ctx.state === 'suspended') {
        ctx.resume()
      }
      ;[880, 660].forEach((frequency, index) => {
        const start = ctx.currentTime + index * 0.35
        const osc = ctx.createOscillator()
        const gain = ctx.createGain()
        osc.type = 'sine'
        osc.frequency.value = frequency
        gain.gain.setValueAtTime(0.001, start)
        gain.gain.exponentialRampToValueAtTime(0.3, start + 0.02)
        gain.gain.exponentialRampToValueAtTime(0.001, start + 0.28)
        osc.connect(gain)
        gain.connect(ctx.destination)
        osc.start(start)
        osc.stop(start + 0.3)
      })
    }
  }
}
</script>
