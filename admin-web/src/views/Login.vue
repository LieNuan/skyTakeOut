<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-brand">
        <div class="brand-icon">
          <svg viewBox="0 0 32 32" width="26" height="26" aria-hidden="true">
            <path d="M4 17h24a12 12 0 0 1-24 0z" fill="currentColor" />
            <path
              d="M9 12c0-2 1.6-2.6 1.6-4.6M16 12c0-2 1.6-2.6 1.6-4.6M23 12c0-2 1.6-2.6 1.6-4.6"
              stroke="currentColor"
              stroke-width="1.8"
              stroke-linecap="round"
              fill="none"
              opacity=".75"
            />
          </svg>
        </div>
        <h1>苍穹外卖</h1>
        <p>商家管理后台</p>
      </div>

      <el-form
        ref="form"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.native.prevent="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            prefix-icon="el-icon-user"
            placeholder="请输入用户名"
            clearable
            @keyup.enter.native="handleLogin"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            prefix-icon="el-icon-lock"
            type="password"
            placeholder="请输入密码"
            show-password
            @keyup.enter.native="handleLogin"
          />
        </el-form-item>

        <el-button
          type="primary"
          :loading="loading"
          class="login-btn"
          @click="handleLogin"
        >
          登 录
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script>
import request, { setToken } from '@/utils/request'

export default {
  name: 'Login',
  data() {
    return {
      form: { username: '', password: '' },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      loading: false
    }
  },
  methods: {
    async handleLogin() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return
        this.loading = true
        try {
          const data = await request('/admin/employee/login', {
            method: 'POST',
            data: this.form
          })
          setToken(data.token)
          // 保存登录员工信息
          localStorage.setItem(
            'sky_admin_user',
            JSON.stringify({ id: data.id, name: data.name, userName: data.userName })
          )
          this.$message.success('登录成功')
          this.$router.push('/dashboard')
        } catch (err) {
          this.$message.error(err.message)
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(140deg, #071d33 0%, #0d3d70 48%, #1a6fe0 100%);
}

.login-card {
  width: 380px;
  padding: 36px 32px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 50px rgba(7, 29, 51, 0.35);
}

.login-brand {
  text-align: center;
  margin-bottom: 28px;
}

.brand-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  color: #fff;
  border-radius: 14px;
  background: linear-gradient(140deg, #3d8bff, #1f6feb);
  margin-bottom: 12px;
}

.login-brand h1 {
  font-size: 22px;
  margin: 0 0 4px;
  color: #1b2733;
}

.login-brand p {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.login-btn {
  width: 100%;
  height: 42px;
  margin-top: 8px;
}
</style>
