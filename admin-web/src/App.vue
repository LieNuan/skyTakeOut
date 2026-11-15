<template>
  <div id="app">
    <el-container>
      <el-header class="header">
        <span class="logo">苍穹外卖 · 管理端</span>
        <el-tag :type="connected ? 'success' : 'danger'" size="small">
          {{ connected ? 'WebSocket 已连接' : 'WebSocket 未连接' }}
        </el-tag>
      </el-header>

      <el-main>
        <!-- 来单 / 催单 通知组件：收到消息会弹出通知并响铃 -->
        <order-notify @open="connected = true" @close="connected = false" @message="onMessage" />

        <el-alert
          title="语音播报说明：首次使用请先点击页面任意位置，浏览器才允许播放声音。"
          type="info"
          :closable="false"
          show-icon
          class="hint"
        />

        <el-card shadow="never">
          <div slot="header" class="card-header">
            <span>实时消息</span>
            <el-button size="mini" @click="messages = []">清空</el-button>
          </div>
          <el-empty v-if="messages.length === 0" description="还没有收到来单或催单提醒" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="(item, index) in messages"
              :key="index"
              :timestamp="item.time"
              :type="item.tagType"
            >
              <b>{{ item.title }}</b>
              {{ item.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import OrderNotify from '@/components/OrderNotify.vue'

export default {
  name: 'App',
  components: { OrderNotify },
  data() {
    return {
      connected: false,
      messages: []
    }
  },
  methods: {
    /**
     * 收到来单/催单消息后的业务处理
     * 真实项目里在这里刷新订单列表、播放自定义语音、弹出接单弹窗等
     */
    onMessage(msg) {
      const isNewOrder = msg.type === 1
      this.messages.unshift({
        title: isNewOrder ? '新订单：' : '客户催单：',
        content: msg.content || '',
        tagType: isNewOrder ? 'success' : 'warning',
        time: new Date().toLocaleTimeString()
      })
    }
  }
}
</script>

<style>
html,
body {
  margin: 0;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei',
    sans-serif;
}

#app {
  min-height: 100%;
  background: #f5f7fa;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.logo {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.hint {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
