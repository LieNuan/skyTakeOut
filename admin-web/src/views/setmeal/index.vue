<template>
  <div class="page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="套餐名称">
          <el-input v-model="query.name" placeholder="请输入套餐名称" clearable style="width: 180px" @clear="getList" @keyup.enter.native="getList" />
        </el-form-item>
        <el-form-item label="套餐分类">
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

    <el-card shadow="never" class="table-card">
      <div slot="header" class="card-header">
        <span>套餐列表（共 {{ total }} 条）</span>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openDialog()">新建套餐</el-button>
      </div>

      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="套餐名称" min-width="140" />
        <el-table-column label="图片" width="120" align="center">
          <template slot-scope="{ row }">
            <el-image
              v-if="row.image"
              :src="getImageUrl(getFoodImage(row.name, row.image))"
              :preview-src-list="[getImageUrl(getFoodImage(row.name, row.image))]"
              fit="cover"
              style="width: 80px; height: 50px; border-radius: 4px"
            >
              <div slot="error" class="img-error">加载失败</div>
            </el-image>
            <span v-else class="img-error">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="套餐分类" width="120" />
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
            <el-button type="text" size="small" @click="openDialog(row)">修改</el-button>
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

    <!-- 新建/修改套餐弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="680px"
      @closed="resetForm"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="套餐分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择套餐分类" style="width: 100%">
            <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="套餐名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入套餐名称" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="套餐价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="售卖状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启售</el-radio>
            <el-radio :label="0">停售</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="套餐图片" prop="image">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :http-request="handleUpload"
            :before-upload="beforeUpload"
            accept="image/*"
          >
            <img v-if="form.image" :src="getImageUrl(form.image)" class="avatar" />
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
          <div v-if="uploading" style="color: #909399; font-size: 12px; margin-top: 4px">上传中...</div>
        </el-form-item>
        <el-form-item label="套餐描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入套餐描述" maxlength="200" show-word-limit />
        </el-form-item>

        <!-- 套餐包含菜品 -->
        <el-form-item label="包含菜品">
          <div class="dish-selector">
            <el-select
              v-model="selectedDishId"
              placeholder="选择菜品后点击添加"
              filterable
              style="width: 260px"
              @change="addDish"
            >
              <el-option
                v-for="d in dishList"
                :key="d.id"
                :label="`${d.name}  ￥${Number(d.price).toFixed(2)}`"
                :value="d.id"
              />
            </el-select>
          </div>
          <el-table :data="form.setmealDishes" size="small" border style="margin-top: 10px" v-if="form.setmealDishes.length">
            <el-table-column prop="name" label="菜品名称" />
            <el-table-column label="单价" width="120">
              <template slot-scope="{ row }">￥{{ Number(row.price).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="份数" width="160">
              <template slot-scope="{ row }">
                <el-input-number v-model="row.copies" :min="1" :max="99" size="mini" controls-position="right" style="width: 130px" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template slot-scope="{ $index }">
                <el-button type="text" size="small" style="color: #f56c6c" @click="removeDish($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-else style="color: #c0c4cc; font-size: 12px; margin-top: 8px">暂未添加菜品</div>
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
import request, { getFoodImage, uploadFile, getImageUrl } from '@/utils/request'

export default {
  name: 'Setmeal',
  data() {
    return {
      loading: false,
      query: { page: 1, pageSize: 10, name: '', categoryId: '', status: '' },
      tableData: [],
      total: 0,
      categoryList: [],
      dishList: [],
      dialogVisible: false,
      submitLoading: false,
      uploading: false,
      isEdit: false,
      selectedDishId: null,
      form: this.emptyForm(),
      rules: {
        categoryId: [{ required: true, message: '请选择套餐分类', trigger: 'change' }],
        name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
        price: [{ required: true, message: '请输入套餐价格', trigger: 'blur' }],
        image: [{ required: true, message: '请上传套餐图片', trigger: 'change' }]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '修改套餐' : '新建套餐'
    }
  },
  created() {
    this.getCategoryList()
    this.getDishList()
    this.getList()
  },
  methods: {
    getFoodImage,
    getImageUrl,
    emptyForm() {
      return {
        id: null,
        categoryId: null,
        name: '',
        price: 0,
        status: 0,
        description: '',
        image: '',
        setmealDishes: []
      }
    },
    async getList() {
      this.loading = true
      try {
        const data = await request('/admin/setmeal/page', {
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
        const data = await request('/admin/category/list', { method: 'GET', params: { type: 2 } })
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
      this.$confirm(`确认删除套餐「${row.name}」吗？`, '提示', {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消'
      }).then(async () => {
        try {
          await request('/admin/setmeal', { method: 'DELETE', params: { ids: row.id } })
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
      this.$confirm(`确认${action}套餐「${row.name}」吗？`, '提示', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(async () => {
        try {
          await request(`/admin/setmeal/status/${newStatus}`, { method: 'POST', params: { id: row.id } })
          this.$message.success(`${action}成功`)
          this.getList()
        } catch (e) {
          this.$message.error(e.message)
        }
      }).catch(() => {})
    },
    async getDishList() {
      try {
        const data = await request('/admin/dish/page', {
          method: 'GET',
          params: { page: 1, pageSize: 200, status: 1 }
        })
        this.dishList = (data.records || []).map((d) => ({
          id: d.id,
          name: d.name,
          price: d.price
        }))
      } catch (e) {
        // 菜品加载失败不影响主流程
      }
    },
    async openDialog(row) {
      this.isEdit = !!row
      if (row) {
        try {
          const vo = await request(`/admin/setmeal/${row.id}`, { method: 'GET' })
          this.form = {
            id: vo.id,
            categoryId: vo.categoryId,
            name: vo.name,
            price: Number(vo.price),
            status: vo.status,
            description: vo.description || '',
            image: vo.image || '',
            setmealDishes: (vo.setmealDishes || []).map((d) => ({
              dishId: d.dishId,
              name: d.name,
              price: Number(d.price),
              copies: d.copies
            }))
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
      this.form = this.emptyForm()
      this.selectedDishId = null
      this.$refs.form && this.$refs.form.clearValidate()
    },
    beforeUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isImage) {
        this.$message.error('只能上传图片文件')
        return false
      }
      if (!isLt2M) {
        this.$message.error('图片大小不能超过 2MB')
        return false
      }
      return true
    },
    async handleUpload(options) {
      this.uploading = true
      try {
        const fileName = await uploadFile(options.file)
        this.form.image = fileName
        this.$message.success('图片上传成功')
        // 上传成功后清除该字段的校验提示
        this.$nextTick(() => this.$refs.form && this.$refs.form.validateField('image'))
      } catch (e) {
        this.$message.error(e.message)
      } finally {
        this.uploading = false
      }
    },
    addDish(dishId) {
      const dish = this.dishList.find((d) => d.id === dishId)
      if (!dish) return
      const exists = this.form.setmealDishes.find((d) => d.dishId === dishId)
      if (exists) {
        this.$message.warning('该菜品已添加')
        this.selectedDishId = null
        return
      }
      this.form.setmealDishes.push({
        dishId: dish.id,
        name: dish.name,
        price: dish.price,
        copies: 1
      })
      this.selectedDishId = null
    },
    removeDish(index) {
      this.form.setmealDishes.splice(index, 1)
    },
    submitForm() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return
        if (this.form.setmealDishes.length === 0) {
          this.$message.warning('请至少添加一个菜品')
          return
        }
        this.submitLoading = true
        try {
          const payload = { ...this.form }
          if (this.isEdit) {
            await request('/admin/setmeal', { method: 'PUT', data: payload })
            this.$message.success('修改成功')
          } else {
            await request('/admin/setmeal', { method: 'POST', data: payload })
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

.avatar-uploader ::v-deep .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.2s;
}

.avatar-uploader ::v-deep .el-upload:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

.dish-selector {
  display: flex;
  align-items: center;
}
</style>
