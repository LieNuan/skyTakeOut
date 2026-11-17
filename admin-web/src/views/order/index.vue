<template>
  <div class="page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="订单号">
          <el-input v-model="query.number" placeholder="请输入订单号" clearable style="width: 160px" @keyup.enter.native="getList" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="query.phone" placeholder="请输入手机号" clearable style="width: 150px" @keyup.enter.native="getList" />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px" @change="getList">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            value-format="yyyy-MM-dd HH:mm:ss"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 240px"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="getList">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div slot="header" class="card-header">
        <span>订单列表（共 {{ total }} 条）</span>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="number" label="订单号" width="170" />
        <el-table-column prop="orderDishes" label="订单菜品" min-width="200" show-overflow-tooltip />
        <el-table-column prop="orderTime" label="下单时间" width="160" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="address" label="收货地址" min-width="180" show-overflow-tooltip />
        <el-table-column label="金额" width="100" align="right">
          <template slot-scope="{ row }">
            <span class="price">￥{{ Number(row.amount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" size="small" @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.status === 2" type="text" size="small" @click="handleAction('confirm', row)">接单</el-button>
            <el-button v-if="row.status === 2" type="text" size="small" style="color: #e6a23c" @click="handleAction('rejection', row)">拒单</el-button>
            <el-button v-if="row.status === 3" type="text" size="small" @click="handleAction('delivery', row)">派送</el-button>
            <el-button v-if="row.status === 4" type="text" size="small" style="color: #67c23a" @click="handleAction('complete', row)">完成</el-button>
            <el-button v-if="row.status === 1 || row.status === 2 || row.status === 3" type="text" size="small" style="color: #f56c6c" @click="handleAction('cancel', row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        :current-page="query.page"
        :page-size="query.pageSize"
        :page-sizes="[10, 20, 30, 40]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </el-card>

    <!-- 订单详情弹窗 -->
    <el-dialog title="订单详情" :visible.sync="detailVisible" width="600px">
      <div v-if="detail" class="order-detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="订单号">{{ detail.number }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="statusTagType(detail.status)" size="small">{{ statusText(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ detail.orderTime }}</el-descriptions-item>
          <el-descriptions-item label="结账时间">{{ detail.checkoutTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ detail.consignee }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detail.phone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ detail.address }}</el-descriptions-item>
          <el-descriptions-item label="订单金额" :span="2">
            <span class="price">￥{{ Number(detail.amount).toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.cancelReason" label="取消原因" :span="2">{{ detail.cancelReason }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.rejectionReason" label="拒单原因" :span="2">{{ detail.rejectionReason }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="detail-title">订单明细</h4>
        <el-table :data="detail.orderDetailList" size="small" border>
          <el-table-column prop="name" label="菜品" />
          <el-table-column label="单价" width="100" align="right">
            <template slot-scope="{ row }">￥{{ Number(row.amount).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="number" label="数量" width="80" align="center" />
          <el-table-column label="小计" width="110" align="right">
            <template slot-scope="{ row }">￥{{ (Number(row.amount) * row.number).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </div>
      <div slot="footer">
        <el-button @click="detailVisible = false">关闭</el-button>
      </div>
    </el-dialog>

    <!-- 拒单/取消原因输入弹窗 -->
    <el-dialog :title="reasonTitle" :visible.sync="reasonVisible" width="420px">
      <el-input v-model="reasonText" type="textarea" :rows="4" placeholder="请输入原因" maxlength="50" show-word-limit />
      <div slot="footer">
        <el-button @click="reasonVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReason">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

const STATUS_MAP = {
  1: '待付款',
  2: '待接单',
  3: '已接单',
  4: '派送中',
  5: '已完成',
  6: '已取消'
}

export default {
  name: 'Order',
  data() {
    return {
      loading: false,
      query: { page: 1, pageSize: 10, number: '', phone: '', status: '', beginTime: '', endTime: '' },
      dateRange: [],
      tableData: [],
      total: 0,
      statusOptions: Object.keys(STATUS_MAP).map((k) => ({ value: Number(k), label: STATUS_MAP[k] })),
      detailVisible: false,
      detail: null,
      reasonVisible: false,
      reasonText: '',
      reasonTitle: '',
      pendingAction: null,
      pendingRow: null
    }
  },
  created() {
    this.getList()
  },
  methods: {
    statusText(s) {
      return STATUS_MAP[s] || '未知'
    },
    statusTagType(s) {
      return { 1: 'info', 2: 'warning', 3: 'primary', 4: 'warning', 5: 'success', 6: 'danger' }[s] || 'info'
    },
    async getList() {
      this.loading = true
      try {
        const data = await request('/admin/order/page', {
          method: 'GET',
          params: {
            page: this.query.page,
            pageSize: this.query.pageSize,
            number: this.query.number || undefined,
            phone: this.query.phone || undefined,
            status: this.query.status === '' ? undefined : this.query.status,
            beginTime: this.query.beginTime || undefined,
            endTime: this.query.endTime || undefined
          }
        })
        this.tableData = data.records || []
        this.total = data.total || 0
      } catch (e) {
        this.$message.error(e.message)
      } finally {
        this.loading = false
      }
    },
    handleDateChange(val) {
      if (val && val.length === 2) {
        this.query.beginTime = val[0]
        this.query.endTime = val[1]
      } else {
        this.query.beginTime = ''
        this.query.endTime = ''
      }
      this.getList()
    },
    handleSizeChange(size) {
      this.query.pageSize = size
      this.query.page = 1
      this.getList()
    },
    handlePageChange(page) {
      this.query.page = page
      this.getList()
    },
    async showDetail(row) {
      try {
        this.detail = await request(`/admin/order/details/${row.id}`, { method: 'GET' })
        this.detailVisible = true
      } catch (e) {
        this.$message.error(e.message)
      }
    },
    handleAction(action, row) {
      if (action === 'rejection' || action === 'cancel') {
        this.pendingAction = action
        this.pendingRow = row
        this.reasonTitle = action === 'rejection' ? '拒单原因' : '取消原因'
        this.reasonText = ''
        this.reasonVisible = true
      } else {
        const labels = { confirm: '接单', delivery: '派送', complete: '完成' }
        this.$confirm(`确认${labels[action]}该订单吗？`, '提示', {
          type: 'warning',
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        }).then(() => this.doAction(action, row)).catch(() => {})
      }
    },
    confirmReason() {
      if (!this.reasonText.trim()) {
        this.$message.warning('请输入原因')
        return
      }
      this.doAction(this.pendingAction, this.pendingRow, this.reasonText.trim())
      this.reasonVisible = false
    },
    async doAction(action, row, reason) {
      const urls = {
        confirm: { url: '/admin/order/confirm', method: 'PUT', data: { id: row.id } },
        rejection: { url: '/admin/order/rejection', method: 'PUT', data: { id: row.id, rejectionReason: reason } },
        cancel: { url: '/admin/order/cancel', method: 'PUT', data: { id: row.id, cancelReason: reason } },
        delivery: { url: `/admin/order/delivery/${row.id}`, method: 'PUT' },
        complete: { url: `/admin/order/complete/${row.id}`, method: 'PUT' }
      }
      const cfg = urls[action]
      try {
        await request(cfg.url, { method: cfg.method, data: cfg.data })
        this.$message.success('操作成功')
        this.getList()
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
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.price {
  color: #f56c6c;
  font-weight: 600;
}
.pagination {
  margin-top: 20px;
  text-align: right;
}
.detail-title {
  margin: 16px 0 10px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
</style>
