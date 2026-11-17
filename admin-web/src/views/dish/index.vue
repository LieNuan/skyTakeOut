<template>
  <div class="page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="菜品名称">
          <el-input v-model="query.name" placeholder="请输入菜品名称" clearable style="width: 180px" @clear="getList" @keyup.enter.native="getList" />
        </el-form-item>
        <el-form-item label="菜品分类">
          <el-select v-model="query.categoryId" placeholder="请选择" clearable style="width: 160px" @clear="getList" @change="getList">
            <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="售卖状态">
          <el-select v-model="query.status" placeholder="请选择" clearable style="width: 120px" @clear="getList" @change="getList">
            <el-option label="启售" :value="1" />
            <el-option label="停售" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="getList">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <div slot="header" class="card-header">
        <span>菜品列表（共 {{ total }} 条）</span>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="$message.info('新建菜品功能开发中')">新建菜品</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="菜品名称" min-width="140" />
        <el-table-column label="图片" width="120" align="center">
          <template slot-scope="{ row }">
            <el-image
              v-if="row.image"
              :src="getFoodImage(row.name, row.image)"
              :preview-src-list="[getFoodImage(row.name, row.image)]"
              fit="cover"
              style="width: 80px; height: 50px; border-radius: 4px"
            >
              <div slot="error" class="img-error">加载失败</div>
            </el-image>
            <span v-else class="img-error">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="菜品分类" width="120" />
        <el-table-column label="售价" width="100" align="right">
          <template slot-scope="{ row }">
            <span class="price">￥{{ Number(row.price).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="售卖状态" width="100" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启售' : '停售' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最后操作时间" width="170" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" size="small" @click="$message.info('修改功能开发中')">修改</el-button>
            <el-button type="text" size="small" style="color: #f56c6c" @click="handleDelete(row)">删除</el-button>
            <el-button type="text" size="small" @click="handleStatus(row)">
              {{ row.status === 1 ? '停售' : '启售' }}
            </el-button>
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
  </div>
</template>

<script>
import request, { getFoodImage } from '@/utils/request'

export default {
  name: 'Dish',
  data() {
    return {
      loading: false,
      query: { page: 1, pageSize: 10, name: '', categoryId: '', status: '' },
      tableData: [],
      total: 0,
      categoryList: []
    }
  },
  created() {
    this.getCategoryList()
    this.getList()
  },
  methods: {
    getFoodImage,
    async getList() {
      this.loading = true
      try {
        const data = await request('/admin/dish/page', {
          method: 'GET',
          params: {
            page: this.query.page,
            pageSize: this.query.pageSize,
            name: this.query.name || undefined,
            categoryId: this.query.categoryId || undefined,
            status: this.query.status === '' ? undefined : this.query.status
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
    async getCategoryList() {
      try {
        const data = await request('/admin/category/list', { method: 'GET', params: { type: 1 } })
        this.categoryList = data || []
      } catch (e) {
        // 分类加载失败不影响主流程
      }
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
    handleDelete(row) {
      this.$confirm(`确认删除菜品「${row.name}」吗？`, '提示', {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消'
      }).then(async () => {
        try {
          await request('/admin/dish', { method: 'DELETE', params: { ids: row.id } })
          this.$message.success('删除成功')
          this.getList()
        } catch (e) {
          this.$message.error(e.message)
        }
      }).catch(() => {})
    },
    handleStatus(row) {
      const newStatus = row.status === 1 ? 0 : 1
      const action = newStatus === 1 ? '启售' : '停售'
      this.$confirm(`确认${action}菜品「${row.name}」吗？`, '提示', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(async () => {
        try {
          await request(`/admin/dish/status/${newStatus}`, { method: 'POST', params: { id: row.id } })
          this.$message.success(`${action}成功`)
          this.getList()
        } catch (e) {
          this.$message.error(e.message)
        }
      }).catch(() => {})
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

.search-card {
  margin-bottom: 0;
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

.img-error {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 50px;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 12px;
  border-radius: 4px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
