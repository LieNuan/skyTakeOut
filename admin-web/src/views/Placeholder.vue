<template>
  <el-card shadow="never" class="placeholder">
    <el-empty :description="`${moduleName} · 开发中`">
      <p class="tip">该模块的前端页面将随后端接口一起完善，敬请期待。</p>
      <el-button type="primary" plain @click="$router.push('/dashboard')">返回工作台</el-button>
    </el-empty>
  </el-card>
</template>

<script>
export default {
  name: 'Placeholder',
  computed: {
    moduleName() {
      return this.$route.meta.module || this.$route.meta.title || '该模块'
    }
  }
}
</script>

<style scoped>
.placeholder {
  min-height: 420px;
}

.tip {
  color: #909399;
  font-size: 13px;
  margin: 8px 0 16px;
}
</style>
