<template>
  <ContentPanel>
    <el-form ref="formDataRef" label-width="80px"></el-form>
    <el-form-item label="版本信息">
      <div class="version-info">
        <div>EasyChat {{ config.version }}</div>
        <div>
          <el-button type="primary" @click="checkUpdate">检查更新</el-button>
        </div>
      </div>
    </el-form-item>
  </ContentPanel>
</template>

<script setup>
import config from "../../../../../package.json"
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()

const formData = ref({});
const formDataRef = ref();
const rules = {
  title: [{ required: true, message: "请输入内容" }],
};

const checkUpdate = () =>{
  }

</script>

<style lang="scss" scoped>
</style>
