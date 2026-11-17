<template>
  <div class="page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="员工姓名">
          <el-input v-model="query.name" placeholder="请输入员工姓名" clearable style="width: 180px" @clear="getList" @keyup.enter.native="getList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="getList">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div slot="header" class="card-header">
        <span>员工列表（共 {{ total }} 条）</span>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openDialog()">新增员工</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="性别" width="80" align="center">
          <template slot-scope="{ row }">
            <span>{{ row.sex === '1' ? '男' : row.sex === '0' ? '女' : '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="idNumber" label="身份证号" width="180" />
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

    <!-- 新增/修改弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="480px" @closed="resetForm">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="form.sex">
            <el-radio label="1">男</el-radio>
            <el-radio label="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="身份证号" prop="idNumber">
          <el-input v-model="form.idNumber" placeholder="请输入身份证号" maxlength="18" />
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
  name: 'Employee',
  data() {
    return {
      loading: false,
      query: { page: 1, pageSize: 10, name: '' },
      tableData: [],
      total: 0,
      dialogVisible: false,
      submitLoading: false,
      isEdit: false,
      form: { id: null, username: '', name: '', phone: '', sex: '1', idNumber: '' },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
        ],
        sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
        idNumber: [
          { required: true, message: '请输入身份证号', trigger: 'blur' },
          { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '修改员工' : '新增员工'
    }
  },
  created() {
    this.getList()
  },
  methods: {
    async getList() {
      this.loading = true
      try {
        const data = await request('/admin/employee/page', {
          method: 'GET',
          params: {
            page: this.query.page,
            pageSize: this.query.pageSize,
            name: this.query.name || undefined
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
    handleStatus(row) {
      const newStatus = row.status === 1 ? 0 : 1
      const action = newStatus === 1 ? '启用' : '禁用'
      this.$confirm(`确认${action}员工「${row.name}」吗？`, '提示', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(async () => {
        try {
          await request(`/admin/employee/status/${newStatus}`, { method: 'POST', params: { id: row.id } })
          this.$message.success(`${action}成功`)
          this.getList()
        } catch (e) {
          this.$message.error(e.message)
        }
      }).catch(() => {})
    },
    async openDialog(row) {
      this.isEdit = !!row
      if (row) {
        try {
          const emp = await request(`/admin/employee/${row.id}`, { method: 'GET' })
          this.form = {
            id: emp.id,
            username: emp.username,
            name: emp.name,
            phone: emp.phone,
            sex: emp.sex || '1',
            idNumber: emp.idNumber
          }
        } catch (e) {
          this.$message.error(e.message)
          return
        }
      } else {
        this.resetForm()
      }
      this.dialogVisible = true
    },
    resetForm() {
      this.form = { id: null, username: '', name: '', phone: '', sex: '1', idNumber: '' }
      this.$refs.form && this.$refs.form.clearValidate()
    },
    submitForm() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return
        this.submitLoading = true
        try {
          if (this.isEdit) {
            await request('/admin/employee', { method: 'PUT', data: this.form })
            this.$message.success('修改成功')
          } else {
            await request('/admin/employee', { method: 'POST', data: this.form })
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
