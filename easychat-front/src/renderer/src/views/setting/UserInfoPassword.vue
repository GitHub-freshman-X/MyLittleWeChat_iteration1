<template>
  <div>
    <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="80px" @submit.prevent>
      <el-form-item label="密码" prop="password">
        <el-input type="password" clearable placeholder="请输入密码" v-model.trim="formData.password"
          show-password></el-input>
      </el-form-item>
      <el-form-item label="确认密码" prop="rePassword">
        <el-input type="password" clearable placeholder="请再次输入密码" v-model.trim="formData.rePassword"
          show-password></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveUserInfo">修改密码</el-button>
        <el-button link @click="cancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()
import { useUserInfoStore } from '@/stores/UserInfoStore'
const userInfoStore = useUserInfoStore()

const formData = ref({});
const formDataRef = ref();

const validateRePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== formData.value.password) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const rules = {
  password: [
    {required: true, message: '请输入密码'},
    {validator: proxy.Verify.password, message: '密码只能是字母、数字和下划线的组合，且长度在8-18位之间'}
  ],
  rePassword: [
    { required: true, message: '请再次输入密码' },
    { validator: validateRePass, message: '两次输入的密码不一致' }
  ]
};


const emit = defineEmits(['editBack'])
const saveUserInfo = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }

    proxy.Confirm({
      message: "修改密码后需重新登陆，是否继续？",
      okfun: async () => {
        let params = {}
        Object.assign(params, formData.value);

        let result = await proxy.Request({
          url: proxy.Api.updatePassword,
          params
        })
        if (!result) {
          return;
        }

        proxy.Message.success("修改成功，请重新登录")
        userInfoStore.setInfo(result.data)
        emit('editBack')
      }
    })

    let params = {};
    Object.assign(params, formData.value);

    proxy.Message.success("修改成功，请重新登录", () => {
      // 重新登录
      window.ipcRenderer.send('reLogin')
    });
  });
}

const cancel = () => {
  emit('editBack')
}


</script>

<style lang="scss" scoped></style>
