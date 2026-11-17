<template>
  <div class="page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="分类名称">
          <el-input v-model="query.name" placeholder="请输入分类名称" clearable style="width: 180px" @clear="getList" @keyup.enter.native="getList" />
        </el-form-item>
        <el-form-item label="分类类型">
          <el-select v-model="query.type" placeholder="请选择" clearable style="width: 140px" @clear="getList" @change="getList">
            <el-option label="菜品分类" :value="1" />
            <el-option label="套餐分类" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="getList">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div slot="header" class="card-header">
        <span>分类列表（共 {{ total }} 条）</span>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openDialog()">新建分类</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="分类名称" min-width="160" />
        <el-table-column label="分类类型" width="120" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.type === 1 ? 'primary' : 'warning'" size="small">
              {{ row.type === 1 ? '菜品分类' : '套餐分类' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最后操作时间" width="170" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" size="small" @click="openDialog(row)">修改</el-button>
            <el-button type="text" size="small" style="color: #f56c6c" @click="handleDelete(row)">删除</el-button>
            <el-button type="text" size="small" @click="handleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
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

    <!-- 新建/修改弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="480px"
      @closed="resetForm"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分类类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择分类类型" style="width: 100%">
            <el-option label="菜品分类" :value="1" />
            <el-option label="套餐分类" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" controls-position="right" style="width: 100%" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'Category',
  data() {
    return {
      loading: false,
      query: { page: 1, pageSize: 10, name: '', type: '' },
      tableData: [],
      total: 0,
      dialogVisible: false,
      submitLoading: false,
      isEdit: false,
      form: { id: null, type: 1, name: '', sort: 0 },
      rules: {
        type: [{ required: true, message: '请选择分类类型', trigger: 'change' }],
        name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
        sort: [{ required: true, message: '请输入排序', trigger: 'blur' }]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '修改分类' : '新建分类'
    }
  },
  created() {
    this.getList()
  },
  methods: {
    async getList() {
      this.loading = true
      try {
        const data = await request('/admin/category/page', {
          method: 'GET',
          params: {
            page: this.query.page,
            pageSize: this.query.pageSize,
            name: this.query.name || undefined,
            type: this.query.type === '' ? undefined : this.query.type
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
      this.$confirm(`确认删除分类「${row.name}」吗？`, '提示', {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消'
      }).then(async () => {
        try {
          await request('/admin/category', { method: 'DELETE', params: { id: row.id } })
          this.$message.success('删除成功')
          this.getList()
        } catch (e) {
          this.$message.error(e.message)
        }
      }).catch(() => {})
    },
    handleStatus(row) {
      const newStatus = row.status === 1 ? 0 : 1
      const action = newStatus === 1 ? '启用' : '禁用'
      this.$confirm(`确认${action}分类「${row.name}」吗？`, '提示', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(async () => {
        try {
          await request(`/admin/category/status/${newStatus}`, { method: 'POST', params: { id: row.id } })
          this.$message.success(`${action}成功`)
          this.getList()
        } catch (e) {
          this.$message.error(e.message)
        }
      }).catch(() => {})
    },
    openDialog(row) {
      this.isEdit = !!row
      if (row) {
        this.form = { id: row.id, type: row.type, name: row.name, sort: row.sort }
      } else {
        this.resetForm()
      }
      this.dialogVisible = true
    },
    resetForm() {
      this.form = { id: null, type: 1, name: '', sort: 0 }
      this.$refs.form && this.$refs.form.clearValidate()
    },
    submitForm() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return
        this.submitLoading = true
        try {
          if (this.isEdit) {
            await request('/admin/category', { method: 'PUT', data: this.form })
            this.$message.success('修改成功')
          } else {
            await request('/admin/category', { method: 'POST', data: this.form })
            this.$message.success('新增成功')
          }
          this.dialogVisible = false
          this.getList()
        } catch (e) {
          this.$message.error(e.message)
        } finally {
          this.submitLoading = false
        }
      })
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
.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
