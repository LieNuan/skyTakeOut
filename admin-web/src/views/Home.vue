<template>
  <div class="home">
    <header class="home-header">
      <h1>苍穹外卖 · 管理端</h1>
      <el-tag :type="connected ? 'success' : 'danger'" size="small">
        {{ connected ? '实时提醒已连接' : '实时提醒未连接' }}
      </el-tag>
    </header>

    <main class="home-main">
      <el-card shadow="never">
        <div slot="header" class="card-header">
          <span>来单提醒 / 客户催单</span>
          <el-button type="text" @click="messages = []" :disabled="messages.length === 0">
            清空记录
          </el-button>
        </div>

        <p class="tip">
          请先启动后端服务（localhost:8080）。用户支付成功或催单时，这里会弹窗并语音播报。
        </p>

        <el-empty v-if="messages.length === 0" description="暂无提醒消息" />
        <el-timeline v-else>
          <el-timeline-item
            v-for="(item, index) in messages"
            :key="index"
            :timestamp="item.time"
            :type="item.type === 1 ? 'success' : 'warning'"
          >
            <strong>{{ item.type === 1 ? '新订单' : '客户催单' }}</strong>
            —— {{ item.content }}
            <span v-if="item.orderId" class="order-id">（订单ID：{{ item.orderId }}）</span>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </main>

    <order-notify @open="connected = true" @close="connected = false" @message="onMessage" />
  </div>
</template>

<script>
import OrderNotify from '@/components/OrderNotify.vue'

export default {
  name: 'Home',
  components: { OrderNotify },
  data() {
    return {
      connected: false,
      messages: []
    }
  },
  methods: {
    onMessage(msg) {
      this.messages.unshift({
        type: Number(msg.type),
        orderId: msg.orderId,
        content: msg.content,
        time: new Date().toLocaleTimeString()
      })
      // 只保留最近 20 条，避免列表无限增长
      this.messages = this.messages.slice(0, 20)
    }
  }
}
</script>

<style scoped>
.home {
  min-height: 100%;
  background: #f5f7fa;
}

.home-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.home-header h1 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.home-main {
  max-width: 960px;
  margin: 24px auto;
  padding: 0 24px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tip {
  margin: 0 0 16px;
  color: #909399;
  font-size: 13px;
}

.order-id {
  color: #909399;
  font-size: 12px;
}
</style>
