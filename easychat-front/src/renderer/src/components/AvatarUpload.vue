<template>
    <div class="avatar-upload">
      <div class="avatar-show">
        <template v-if="modelValue">
          <el-image v-if="preview" :src="localFile" fit="scale-down"></el-image>
          <ShowLocalImage
            :fileId="props.modelValue"
            partType="avatar"
            :width="40"
            v-else
          ></ShowLocalImage>
        </template>
        <template v-else>
          <el-upload
            name="file"
            :show-file-list="false"
            accept=".png,.PNG,.jpg,.JPG,.jpeg,.JPEG,.gif,.GIF,.bmp,.BMP"
            :multiple="false"
            :http-request="uploadImage"
          >
            <span class="iconfont icon-add"></span>
          </el-upload>
        </template>
      </div>
      <div class="select-btn">
      <el-upload
            name="file"
            :show-file-list="false"
            accept=".png,.PNG,.jpg,.JPG,.jpeg,.JPEG,.gif,.GIF,.bmp,.BMP"
            :multiple="false"
            :http-request="uploadImage"
          > 
        <el-button type="primary" size="small">选择</el-button>
      </el-upload>
    </div>
    </div>
  </template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();

const props = defineProps({
  modelValue: {
    type: [String, Object],
    default:null
  }
})

const uploadImage = async (file) => {
    file = file.file
    //window.ipRender.send("");
}     
</script>

<style lang="scss" scoped>
.avatar-upload {
  display: flex;
  justify-content: center;
  align-items: end;
  line-height: normal;
.avatar-show {
  background: #ededed;
  width: 60px;
  height: 60px;
  border-radius: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
.icon-add {
  font-size: 30px;
  color: #b9b9b9;
  width: 60px;
  height: 60px;
  text-align: center;
  line-height: 60px;
}
img {
  width: 100%;
  height: 100%;
}
.top {
  position: absolute;
  color: #00e6ae;
  top: 8px;
}
}
.select-btn {
  vertical-align: bottom;
  margin-left: 5px;
}
}
</style>