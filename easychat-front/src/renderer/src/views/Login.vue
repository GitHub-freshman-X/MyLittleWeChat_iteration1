<template>
  <div class="login-panel">
    <div class="title drag">EasyChat</div>
    <div v-if="showLoading" class="loading-panel">
      <img src="../assets/img/loading.gif" />
    </div>
    <div class="login-form">
      <div class="error-msg">{{ errorMsg }}</div>
      <el-form :model="formData" ref="formDataRef" label-width="0px" @submit.prevent>
        <!--input输入-->
        <el-form-item prop="email">
          <el-input size="large" clearable placeholder="请输入邮箱" maxLength="30" v-model.trim="formData.email"
            @focus="clearVerify">
            <template #prefix>
              <span class="iconfont icon-email"></span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="nickName" v-if="!isLogin">
          <el-input size="large" clearable placeholder="请输入昵称" maxLength="15" v-model.trim="formData.nickName"
            @focus="clearVerify">
            <template #prefix>
              <span class="iconfont icon-user-nick"></span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input size="large" show-password clearable placeholder="请输入密码" v-model.trim="formData.password"
            @focus="clearVerify">
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="rePassword" v-if="!isLogin">
          <el-input size="large" show-password clearable placeholder="请再次确认密码" v-model.trim="formData.rePassword"
            @focus="clearVerify">
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="checkCode">
          <div class="check-code-panel">
            <el-input size="large" clearable placeholder="请输入验证码" v-model.trim="formData.checkCode"
              @focus="clearVerify">
              <template #prefix>
                <span class="iconfont icon-checkcode"></span>
              </template>
            </el-input>
            <img :src="checkCodeUrl" class="check-code" @click="changeCheckCode" />
          </div>
        </el-form-item>

        <el-form-item prop="checkcode">
          <el-button type="primary" class="login-btn" @click="submit">{{
            isLogin ? '登录' : '注册'
          }}</el-button>
        </el-form-item>

        <div class="bottom-link">
          <span class="a-link" @click="changeOpType">{{
            isLogin ? '没有账号？' : '已有账号？'
          }}</span>
        </div>
      </el-form>
    </div>
  </div>
  <WinOp :showSetTop="false" :showMin="false" :showMax="false" :closeType="0"></WinOp>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from 'vue'
const { proxy } = getCurrentInstance()
import { useUserInfoStore } from '@/stores/UserInfoStore'
const userInfoStore = useUserInfoStore()

import { useRouter } from 'vue-router'
const router = useRouter()

import md5 from 'js-md5'

const formData = ref({})
const formDataRef = ref()
// const rules = {
//   email: [{ required: true, message: "请输入邮箱" }],
//   password: [{ required: true, message: "请输入密码" }],
//   checkCode: [{ required: true, message: "请输入验证码" }]
// }

const isLogin = ref(true)
const changeOpType = () => {
  window.ipcRenderer.send('loginOrRegister', !isLogin.value)
  isLogin.value = !isLogin.value
  // 输出isLogin的值
  // console.log(isLogin.value)
  nextTick(() => {
    formDataRef.value.resetFields()
    formData.value = {}
    clearVerify()
    changeCheckCode()
  })
}

// 获取验证码
const checkCodeUrl = ref(null)
const changeCheckCode = async () => {
  let result = await proxy.Request({
    url: proxy.Api.checkCode
  })
  if (!result) {
    return;
  }
  checkCodeUrl.value = result.data.checkCode
  // console.log(result.data.checkCode)
  // localStorage.setItem("checkCode", result.data.checkCode)
  localStorage.setItem("checkCodeKey", result.data.checkCodeKey)
}
changeCheckCode()

const errorMsg = ref('')


const checkValue = (type, value, msg) => {
  if (proxy.Utils.isEmpty(value)) {
    errorMsg.value = msg
    return false
  }
  if (type && !proxy.Verify[type](value)) {
    errorMsg.value = msg
    return false
  }
  return true
}

const clearVerify = () => {
  errorMsg.value = null
}

const showLoading = ref(false)

const submit = async () => {
  clearVerify()

  if (!checkValue('checkEmail', formData.value.email, '请输入正确的邮箱')) {
    return
  }
  if (!isLogin.value && !checkValue(null, formData.value.nickName, '请输入昵称')) {
    return
  }
  if (
    !checkValue('checkPassword', formData.value.password, '密码只能是数字、字母、特殊字符8~18位')
  ) {
    return
  }
  if (!isLogin.value && formData.value.password != formData.value.rePassword) {
    errorMsg.value = '两次密码不一致'
    return
  }
  if (!checkValue(null, formData.value.checkCode, '请输入验证码')) {
    return
  }
  if (isLogin.value == true) {
    showLoading.value = true
  }
  let result = await proxy.Request({
    url: isLogin.value ? proxy.Api.login : proxy.Api.register,
    showLoading: isLogin.value ? false : true,
    showError: false,
    params: {
      email: formData.value.email,
      password: isLogin.value ? md5(formData.value.password) : formData.value.password,
      checkCode: formData.value.checkCode,
      nickName: formData.value.nickName,
      checkCodeKey: localStorage.getItem('checkCodeKey')
    },
    errorCallback: (response) => {
      showLoading.value = false
      changeCheckCode()
      errorMsg.value = response.info
    }
  })
  if (!result) {
    return;
  }
  if (isLogin.value) {
    userInfoStore.setInfo(result.data)
    localStorage.setItem('token', result.data.token)

    router.push('/main')

    const screenWidth = window.screen.width
    const screenHeight = window.screen.height
    window.ipcRenderer.send('openChat', {
      email: formData.value.email,
      token: result.data.token,
      userId: result.data.userId,
      nickName: result.data.nickName,
      admin: result.data.admin,
      screenWidth: screenWidth,
      screenHeight: screenHeight
    })
    window.ipcRenderer.send('setLocalStore',{key:"devWsDomain",value:proxy.Api.devWsDomain})

    window.ipcRenderer.send('getLocalStore','devWsDomain');
  } else {
    proxy.Message.success("注册成功")
    changeOpType()
  }
}
const init=()=>{ 
    window.ipcRenderer.send("setLocalStore",{key:'prodDomain',value:proxy.Api.prodDomain})
    window.ipcRenderer.send("setLocalStore",{key:'devDomain',value:proxy.Api.devDomain})
    window.ipcRenderer.send("setLocalStore",{key:'prodWsDomain',value:proxy.Api.prodWsDomain})
    window.ipcRenderer.send("setLocalStore",{key:'devWsDomain',value:proxy.Api.devWsDomain})
}

onMounted(()=>{
  init()
})

</script>

<style lang="scss" scoped>
.email-select {
  width: 250px;
}

.loading-panel {
  height: calc(100vh - 32px);
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;

  img {
    width: 300px;
  }
}

.login-panel {
  background: #fff;
  border-radius: 3px;
  border: 1px solid #ddd;

  .title {
    height: 30px;
    padding: 5px 0px 0px 10px;
  }

  .login-form {
    padding: 0px 15px 29px 15px;

    :deep(.el-input__wrapper) {
      box-shadow: none;
      border-radius: none;
    }

    .el-form-item {
      border-bottem: 1px solid #ddd;
    }

    .email-panel {
      align-items: center;
      width: 100%;
      display: flex;

      .input {
        flex: 1;
      }

      .icon-down {
        margin-left: 3px;
        width: 16px;
        cursor: pointer;
        border: none;
      }
    }

    .error-msg {
      line-height: 30px;
      height: 30px;
      color: #fb7373;
    }

    .check-code-panel {
      display: flex;

      .check-code {
        cursor: pointer;
        width: 120px;
        margin-left: 5px;
      }
    }

    .login-btn {
      margin-top: 20px;
      width: 100%;
      background: #07c160;
      height: 36px;
      font-size: 16px;
    }

    .bottom-link {
      text-align: right;
    }
  }
}
</style>
