<template>
  <Layout>
    <template #left-content>
      <div class="drag-panel drag"></div>
      <div class="top-search">
        <el-input clearable placeholder="搜索" v-model="searchKey" size="small" @keyup="search">
          <template #suffix>
            <span class="iconfont icon-search"></span>
          </template>
        </el-input>
      </div>
      <div class="chat-session-list">
        <template v-for="item in chatSessionList" :key="item.contactName">
          <ChatSession :data="item" @contextmenu.stop="onContextMenu(item, $event)"></ChatSession>
        </template>
      </div>
    </template>
    <template #right-content>
    </template>
  </Layout>
</template>

<script setup>
import ContextMenu from '@imengyu/vue3-context-menu'
import '@imengyu/vue3-context-menu/lib/vue3-context-menu.css'
import ChatSession from "./ChatSession.vue";
import { ref, reactive, getCurrentInstance, nextTick ,onMounted, onUnmounted} from "vue" 
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()

const searchKey = ref();
const search = () =>{}

const chatSessionList = ref([]);

const onReveiveMessage = ()=>{
  window.ipcRenderer.on("receiveMessage", (e, message)=>{
    console.log("收到消息", message)
    
  })
}

const onLoadSessionData = ()=>{
  window.ipcRenderer.on("loadSessionDataCallback", (e, dataList)=>{
    sortChatrSessionList(dataList)
    chatSessionList.value = dataList;
    console.log(dataList)
  })
}

const loadChatSession = ()=>{
  window.ipcRenderer.send("loadSessionData")
}

// 会话排序
const sortChatrSessionList = (dataList)=>{
  dataList.sort((a, b)=>{
    const topTypeResult = b["topType"] - a["topType"]
    if(topTypeResult==0){
      return b["lastMessageTime"] - a["lastMessageTime"]
    }
    return topTypeResult
  })
}

const delChatSessionList = (contactId)=>{
  chatSessionList.value = chatSessionList.value.filter(item=>{
    return item.contactId != contactId
  })
}

onMounted(()=>{
  onReveiveMessage()
  onLoadSessionData()
  loadChatSession()
})

// 当前选中的会话
const currentChatSession = ref({})

onUnmounted(()=>{
  window.ipcRenderer.removeAllListeners("receiveMessage")
  window.ipcRenderer.removeAllListeners("loadSessionDataCallback")
})

const setTop = (data)=>{
  data.topType = data.topType==0 ? 1 : 0
  // 会话排序
  sortChatrSessionList(chatSessionList.value)
  window.ipcRenderer.send("topChatSession", {contactId: data.contactId, topType: data.topType})
}
const delChatSession = (contactId)=>{
  delChatSessionList(contactId)
  currentChatSession.value = {}
  // 设置选中的会话
  window.ipcRenderer.send("delChatSession", contactId)
}
const onContextMenu = (data, e)=>{
  ContextMenu.showContextMenu({
    x: e.x, 
    y: e.y,
    items: [{
      label: data.topType==0 ? "置顶" : "取消置顶",
      onClick: ()=>{
        setTop(data)
      }
    },{
      label: "删除聊天",
      onClick: ()=>{
        proxy.Confirm({
          message: `确定要删除聊天【${data.contactName}】吗？`,
          okfun: ()=>{
            delChatSession(data.contactId)
          }
        })
      }
    }
  ]
  })
}

</script>

<style lang="scss" scoped>
.drag-panel{
  height: 25px;
  background: #f7f7f7;
}
.top-search{
  padding: 0px 10px 9px 10px;
  background: #f7f7f7;
  display: flex;
  align-items: center;
  .iconfont{
    font-size: 12px;
  }
}

.chat-session-list{
  height: calc(100vh - 62px);
  overflow: hidden;
  border-top: 1px solid #dddddd;
  &:hover{
    overflow: auto;
  }
}

.search-list{
  height: calc(100vh - 62px);
  background: #f7f7f7;
  overflow: hidden;
  &:hover{
    overflow: auto;
  }
}

.title-panel{
  display: flex;
  align-items: center;
  .title{
    height: 60px;
    line-height: 60px;
    padding-left: 10px;
    font-size: 18px;
    color: #000000;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.icon-more{
  position: absolute;
  z-index: 1;
  top: 30px;
  right: 3px;
  width: 20px;
  margin-right: 5px;
  cursor: pointer;
}
.chat-panel{
  border-top: 1px solid #dddddd;
  background: #f5f5f5;
  .message-panel{
    paading: 10px 30px 0px 30px;
    height: calc(100vh - 200px - 62px);
    overflow-y: auto;
    .message-item{
      margin-bottom: 15px;
      text-align: center;
    }
  }
}

</style>
