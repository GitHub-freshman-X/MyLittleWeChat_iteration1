<template>
  <div class="media-window">
    <div class="win-title drag"></div>
    <div class="media-op no-drag">
      <div :class="['iconfont icon-left', currentIndex == 0 ? 'not-allow' : '']" @dblclick.stop title="上一张"
        @click="next(-1)"></div>
      <div :class="['iconfont icon-right', currentIndex >= allFileList.length - 1 ? 'not-allow' : '']" @dblclick.stop
        title="下一张" @click="next(1)"></div>
      <template v-if="fileList[0].fileType == 0">
        <el-divider direction="vertical"></el-divider>
        <div class="iconfont icon-enlarge" @click.stop="changeSize(0.1)" @dblclick.stop title="放大"></div>
        <div class="iconfont icon-narrow" @click="changeSize(-0.1)" @dblclick.stop title="缩小"></div>
        <div :class="['iconfont', isOne2One ? 'icon-resize' : 'icon-source-size']" @dblclick.stop @click="resize"
          :title="isOne2One ? '图片适应窗口大小' : '图片原始大小'"></div>
        <div class="iconfont icon-rotate" @dblclick.stop @click="rotate" title="旋转"></div>
        <el-divider direction="vertical"></el-divider>
      </template>
      <div class="iconfont icon-download" @dblclick.stop @click="saveAs" title="另存为..."></div>
    </div>
    <div class="media-panel">
      <viewer :options="options" @inited="inited" :images="fileList"
        v-if="fileList[0].fileType == 0 && fileList[0].status == 1">
        <img :src="fileList[0].url" />
      </viewer>
      <div ref="player" id="player" v-show="fileList[0].fileType==1 && fileList[0].status==1"
      style="width: 100%; height: 100%"></div>
      <div v-if="fileList[0].fileType==2" class="file-panel">
        <div class="file-item">文件名：{{ fileList[0].fileName }}</div>
        <div class="file-item">文件大小：{{ utilityProcess.size2Str(fileList[0].fileSize) }}</div>
        <div class="file-item download">
          <el-button type="primary" @click="saveAs">下载文件</el-button>
        </div>
      </div>
      <div class="loading" v-if="fileList[0].status!=1">加载中...</div>
    </div>
    <WinOp @closeCallback="closeWin"></WinOp>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted } from "vue"
import DPlayer from 'dplayer'
import 'viewerjs/dist/viewer.css'
import { component as Viewer } from 'v-viewer'

const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()

const options = ref({
  inline: true,
  toolbar: false,
  navbar: false,
  button: false,
  title: false,
  zoomRatio: 0.1,
  zoomOnWheel: false,
})

const viewerMy = ref(null)
const inited = (e)=>{
  viewerMy.value = e
}

const changeSize = (zoomRatio)=>{
  if(!viewerMy.value){
    return
  }
  viewerMy.value.zoom(zoomRatio, true)
}

const rotate = ()=>{
  viewerMy.value.rotate(90, true)
}

const isOne2One = ref(false)
const resize = ()=>{
  isOne2One.value = !isOne2One.value
  if(!isOne2One.value){
    viewerMy.value.zoomTo(viewerMy.value.initialImageData.radio, true)
  }else{
    viewerMy.value.zoomTo(1, true)
  }
}

const onWheel = (e)=>{
  if(fileList.value[0].fileType!=0){
    return
  }
  console.log(e.deltaY)
  if(e.deltay<0){
    changeSize(0.1)
  }else{
    changeSize(-0.1)
  }
}

const currentIndex = ref(0)
const allFileList = ref([])
const fileList = ref([{ fileType: 0, status: 0 }])
onMounted(() => {
  window.addEventListener("wheel", onWheel)
  window.ipcRenderer.on('pageInitData', (e, data) => {
    allFileList.value = data.fileList
  })
})

onUnmounted(()=>{
  window.ipcRenderer.removeAllListeners('pageInitData')
  window.removeEventListener("wheel", onWheel)
})

const closeWin = () => {

}

</script>

<style lang="scss" scoped>
.media-window {
  padding: 0px;
  height: calc(100vh);
  border: 1px solid #dddddd;
  background: #ffffff;
  position: relative;
  overflow: hidden;

  .win-title {
    height: 37px;
  }

  .media-op {
    position: absolute;
    left: 0px;
    top: 0px;
    height: 35px;
    line-height: 35px;
    display: flex;
    align-items: center;

    .iconfont {
      font-size: 18px;
      padding: 0px 10px;

      &:hover {
        background: #f3f3f3;
        cursor: pointer;
      }
    }

    .not-allow {
      cursor: not-allowed;
      color: #dddddd;
      text-decoration: none;

      &:hover {
        color: #dddddd;
        cursor: not-allowed;
        background: none;
      }
    }
  }

  .media-panel {
    height: calc(100vh - 37px);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;

    :deep(.viewer-backdrop) {
      background: #f5f5f5;
    }

    .file-panel {
      .file-item {
        margin-top: 5px;
      }

      .download {
        margin-top: 20px;
        text-align: center;
      }
    }
  }
}
</style>
