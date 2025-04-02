<template>
  <div>
    <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="400px"
      :showCancel="false" @close="dialogConfig.show = false">
      <el-form :model="formData" :rules="rules" ref="formDataRef" @submit.prevent>
        <!--input输入-->
        <el-form-item label="" prop="">
          <el-input type="textarea" :rows="5" clearable placeholder="输入申请信息，更容易被通过" v-model.trim="formData.applyInfo"
            resize="none" show-word-limit maxlength="100"></el-input>
        </el-form-item>
      </el-form>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed } from 'vue';
const { proxy } = getCurrentInstance();
import { useUserInfoStore } from '@/stores/UserInfoStore';
const userInfoStore = userUserInfoStore()

const formData = ref({});
const formDataRef = ref();
const rules = {
  title: [{ required: true, message: "请输入内容" }],
};

const dialogConfig = reactive({
  show: false,
  title: "提交申请",
  buttons: [
    {
      type: "danger",
      text: "确定",
      click: (e) => {
        submitApply();
      },
    },
  ],
});

const emit = defineEmits('reload')
const submitApply = async() => {
  const { contactId, contactType, applyInfo } = formData.value;
  let result = await proxy.Request({
     url:proxy.Api.applyAdd,
     params:{
      contactId,
      contactType,
      applyInfo
     }
  })
  if(!result){
    return;
  }

  if(result.data==0){
    proxy.Message.success("添加成功")
  }else{
    proxy.Message.success("申请已发送，等待对方回复")
  }
  dialogConfig.value.show = false
  emit('reload')
}

const show = (data) => {
  dialogConfig.value.show = true
  nextTick(()=> {
    formDataRef.value.resetFields()
    formData.value.applyInfo = "我是"+userInfoStore.getInfo().nickName
  })
}

defineExpose({
  show
})

</script>

<style lang="scss" scoped></style>
