<template>
  <div>
    <el-form :model="formData" :rules="rules" ref="formDataRef" label0width="80px" @submit.prevent>
      <el-form-item label="头像" prop="avatarFile">
        <AvatarUpload v-model="formData.avatarFile" @coverFile="saveCover"></AvatarUpload>
      </el-form-item>
      <el-form-item lable="昵称" prop="nickName">
        <el-input maxlength="150" clearable placeholder="请输入昵称" v-model.trim="formData.nickName"></el-input>
      </el-form-item>
      <el-form-item label="性别" prop="sex">
        <el-radio-group v-model="formData.sex">
          <el-radio :label="1">男</el-radio>
          <el-radio :label="0">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="朋友权限" prop="joinType">
        <el-switch v-model="formData.joinType" :active-value="1" :inactive-value="0"></el-switch>
        <div class="info">加我为好友时需要验证</div>
      </el-form-item>
      <el-form-item label="地区" prop="area">
        <AreaSelect v-model="formData.area"></AreaSelect>
      </el-form-item>
      <el-from-item label="个性签名" prop="personalSignature">
        <el-input clearable placeholder="请输入个性签名" v-model.trim="formData.personalSignature" type="textarea" rows="5"
          maxlength="30" :show-word-limit="true" resize="none"></el-input>
      </el-from-item>
      <el-form-item>
        <el-button type="primary" @click="saveUserInfo">保存个人信息</el-button>
        <el-button link @click="cancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import AreaSelect from "../../components/AreaSelect.vue";
import { ref, reactive, getCurrentInstance, nextTick, computed } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()

import { useUserInfoStore } from '@/stores/UserInfoStore'
const userInfoStore = useUserInfoStore()

import { useAvatarInfoStore } from "../../stores/AvatarUploadStore";
const avatarInfoStore = useAvatarInfoStore()

const props = defineProps({
  data: {
    type: Object
  }
})

const formData = computed(() => {
  const userInfo = props.data
  userInfo.avatarFile = userInfo.userId
  userInfo.area = {
    areaCode: userInfo.areaCode ? userInfo.areaCode.split(',') : [],
    areaName: userInfo.areaName ? userInfo.areaName.split(',') : []
  }
  return userInfo
})

const formDataRef = ref();
const rules = {
  avatarFile: [{ required: true, message: "请选择头像" }],
  nickName: [{ required: true, message: "请输入昵称" }]
};

const saveCover = ({ avatarFile, coverFile }) => {
  formData.value.avatarFile = avatarFile
  formData.value.avatarCover = coverFile
}

const emit = defineEmits(['editBack'])
const saveUserInfo = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    let params = {};
    Object.assign(params, formData.value);

    params.areaName = ""
    params.areaCode = ""
    if (params.area) {
      params.areaName = params.area.areaName.join(',')
      params.areaCode = params.area.areaCode.join(',')
      delete params.area
    }

    // 强制刷新头像
    avatarInfoStore.setForceReload(userInfoStore.getInfo().userId, false)

    let result = await proxy.Request({
      url: proxy.Api.saveUserInfo,
      params,
    });
    if (!result) {
      return;
    }

    proxy.Message.success("保存成功");
    userInfoStore.setInfo(result.data);
    avatarInfoStore.setForceReload(userInfoStore.getInfo().userId, true) // 值发生变化
    emit('editBack')
  });
}

const cancel = () => {
  emit('editBack')
}

</script>

<style lang="scss" scoped>
.info {
  margin-left: 5px;
  color: #949494;
  font-size: 12px;
}
</style>
