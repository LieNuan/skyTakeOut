<template>
  <div>
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
  </div>
</template>

<script>
import OrderNotify from '@/components/OrderNotify.vue'

export default {
  name: 'Dashboard',
  components: { OrderNotify },
  data() {
    return {
      connected: false,
      messages: []
    }
  },
  methods: {
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

<style scoped>
.hint {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
