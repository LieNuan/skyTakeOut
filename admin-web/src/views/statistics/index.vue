<template>
  <div class="page">
    <!-- 日期选择 -->
    <el-card shadow="never" class="date-card">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        value-format="yyyy-MM-dd"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        style="width: 280px"
        @change="loadAll"
      />
      <el-button type="primary" style="margin-left: 12px" @click="loadAll">查询</el-button>
    </el-card>

    <div class="row">
      <!-- 营业额统计 -->
      <el-card shadow="never" class="chart-card">
        <div slot="header"><span>营业额统计</span></div>
        <div class="summary">
          <div class="sum-item">
            <div class="sum-label">总营业额</div>
            <div class="sum-value">￥{{ totalTurnover }}</div>
          </div>
        </div>
        <div class="bar-chart">
          <div v-for="(item, i) in turnoverData" :key="i" class="bar-row">
            <div class="bar-label" :title="item.date">{{ item.date }}</div>
            <div class="bar-track">
              <div class="bar-fill turnover" :style="{ width: item.width + '%' }">
                <span class="bar-val">￥{{ item.value }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 订单统计 -->
      <el-card shadow="never" class="chart-card">
        <div slot="header"><span>订单统计</span></div>
        <div class="summary">
          <div class="sum-item">
            <div class="sum-label">订单总数</div>
            <div class="sum-value">{{ orderReport.totalOrderCount || 0 }}</div>
          </div>
          <div class="sum-item">
            <div class="sum-label">有效订单</div>
            <div class="sum-value">{{ orderReport.validOrderCount || 0 }}</div>
          </div>
          <div class="sum-item">
            <div class="sum-label">完成率</div>
            <div class="sum-value">{{ orderCompletionRate }}%</div>
          </div>
        </div>
        <div class="bar-chart">
          <div v-for="(item, i) in orderData" :key="i" class="bar-row">
            <div class="bar-label" :title="item.date">{{ item.date }}</div>
            <div class="bar-track">
              <div class="bar-fill order" :style="{ width: item.width + '%' }">
                <span class="bar-val">{{ item.value }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <div class="row">
      <!-- 用户统计 -->
      <el-card shadow="never" class="chart-card">
        <div slot="header"><span>用户统计</span></div>
        <div class="bar-chart">
          <div v-for="(item, i) in userData" :key="i" class="bar-row">
            <div class="bar-label" :title="item.date">{{ item.date }}</div>
            <div class="bar-track">
              <div class="bar-fill new" :style="{ width: item.newWidth + '%' }">
                <span class="bar-val">新{{ item.newValue }}</span>
              </div>
            </div>
            <div class="bar-track" style="margin-left: 4px">
              <div class="bar-fill total" :style="{ width: item.totalWidth + '%' }">
                <span class="bar-val">总{{ item.totalValue }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 销量TOP10 -->
      <el-card shadow="never" class="chart-card">
        <div slot="header"><span>销量TOP10</span></div>
        <el-table :data="top10Data" size="small" style="width: 100%">
          <el-table-column label="排名" width="70" align="center">
            <template slot-scope="{ $index }">
              <el-tag :type="$index < 3 ? 'danger' : 'info'" size="mini">{{ $index + 1 }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="菜品名称" />
          <el-table-column prop="number" label="销量" width="100" align="center" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'

function parseList(str) {
  if (!str) return []
  return str.split(',').map((s) => s.trim())
}

function fmt(n) {
  return Number(n || 0).toFixed(2)
}

export default {
  name: 'Statistics',
  data() {
    return {
      dateRange: this.defaultRange(),
      turnoverReport: { dateList: '', turnoverList: '' },
      orderReport: {},
      userReport: { dateList: '', totalUserList: '', newUserList: '' },
      top10: { nameList: '', numberList: '' }
    }
  },
  computed: {
    totalTurnover() {
      const list = parseList(this.turnoverReport.turnoverList)
      return list.reduce((s, v) => s + Number(v || 0), 0).toFixed(2)
    },
    orderCompletionRate() {
      const r = this.orderReport.orderCompletionRate
      return r ? (r * 100).toFixed(1) : '0.0'
    },
    turnoverData() {
      const dates = parseList(this.turnoverReport.dateList)
      const values = parseList(this.turnoverReport.turnoverList)
      const max = Math.max(...values.map((v) => Number(v || 0)), 1)
      return dates.map((d, i) => ({
        date: d.slice(5),
        value: fmt(values[i]),
        width: (Number(values[i] || 0) / max) * 100
      }))
    },
    orderData() {
      const dates = parseList(this.orderReport.dateList || '')
      const values = parseList(this.orderReport.validOrderCountList || '')
      const max = Math.max(...values.map((v) => Number(v || 0)), 1)
      return dates.map((d, i) => ({
        date: d.slice(5),
        value: values[i] || 0,
        width: (Number(values[i] || 0) / max) * 100
      }))
    },
    userData() {
      const dates = parseList(this.userReport.dateList)
      const totals = parseList(this.userReport.totalUserList)
      const news = parseList(this.userReport.newUserList)
      const max = Math.max(...totals.map((v) => Number(v || 0)), 1)
      return dates.map((d, i) => ({
        date: d.slice(5),
        totalValue: totals[i] || 0,
        newValue: news[i] || 0,
        totalWidth: (Number(totals[i] || 0) / max) * 100,
        newWidth: (Number(news[i] || 0) / max) * 100
      }))
    },
    top10Data() {
      const names = parseList(this.top10.nameList)
      const numbers = parseList(this.top10.numberList)
      return names.map((n, i) => ({ name: n, number: numbers[i] || 0 }))
    }
  },
  created() {
    this.loadAll()
  },
  methods: {
    defaultRange() {
      const end = new Date()
      const start = new Date()
      start.setDate(end.getDate() - 6)
      const fmt = (d) => d.toISOString().slice(0, 10)
      return [fmt(start), fmt(end)]
    },
    async loadAll() {
      if (!this.dateRange || this.dateRange.length !== 2) return
      const [begin, end] = this.dateRange
      try {
        const [t, o, u, top] = await Promise.all([
          request('/admin/report/turnoverStatistics', { method: 'GET', params: { begin, end } }),
          request('/admin/report/orderStatistics', { method: 'GET', params: { begin, end } }),
          request('/admin/report/userStatistics', { method: 'GET', params: { begin, end } }),
          request('/admin/report/top10', { method: 'GET', params: { begin, end } })
        ])
        this.turnoverReport = t
        this.orderReport = o
        this.userReport = u
        this.top10 = top
      } catch (e) {
        this.$message.error(e.message)
      }
    }
  }
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.date-card {
  display: flex;
  align-items: center;
}
.row {
  display: flex;
  gap: 16px;
}
.chart-card {
  flex: 1;
  min-width: 0;
}
.summary {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}
.sum-item {
  text-align: center;
}
.sum-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.sum-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}
.bar-chart {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 320px;
  overflow-y: auto;
}
.bar-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.bar-label {
  width: 60px;
  flex-shrink: 0;
  font-size: 12px;
  color: #606266;
  text-align: right;
}
.bar-track {
  flex: 1;
  height: 22px;
  background: #f5f7fa;
  border-radius: 4px;
  position: relative;
  min-width: 0;
}
.bar-fill {
  height: 100%;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 6px;
  min-width: 40px;
  transition: width 0.3s;
}
.bar-fill.turnover {
  background: linear-gradient(90deg, #67c23a, #85ce61);
}
.bar-fill.order {
  background: linear-gradient(90deg, #409eff, #66b1ff);
}
.bar-fill.new {
  background: linear-gradient(90deg, #e6a23c, #ebb563);
}
.bar-fill.total {
  background: linear-gradient(90deg, #909399, #a6a9ad);
}
.bar-val {
  font-size: 11px;
  color: #fff;
  white-space: nowrap;
}
</style>
