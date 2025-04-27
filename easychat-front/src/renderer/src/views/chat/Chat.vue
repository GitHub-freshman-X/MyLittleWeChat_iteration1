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
        <template v-for="item in chatSessionList" :key="item.contactId">
          <ChatSession :data="item" @click="chatSessionClickHandler(item)"
            @contextmenu.stop="onContextMenu(item, $event)"
            :currentSession="item.contactId == currentChatSession.contactId">
          </ChatSession>
        </template>
      </div>
    </template>
    <template #right-content>
      <div class="title-panel drag" v-if="Object.keys(currentChatSession).length > 0">
        <div class="title">
          <span>{{ currentChatSession.contactName }}</span>
          <span v-if="currentChatSession.contactType == 1">
            ({{ currentChatSession.memberCount }})
          </span>
        </div>
      </div>
      <div v-if="currentChatSession.contactType == 1" class="iconfont icon-more no-drag" @click="showGroupDetail">
      </div>
      <div class="chat-panel" v-show="Object.keys(currentChatSession).length > 0">
        <div class="message-panel" id="message-panel" @scroll="handleMessageScroll"
          :style="{ overflowY: messageList.length > 0 ? 'auto' : 'hidden' }">
          <div v-if="messageCountInfo.isLoading" class="loading-indicator">加载中...</div>
          <div class="message-item" v-for="(data, index) in messageList" :key="data.messageId"
            :id="'message' + data.messageId">
            <!-- 展示时间 -->
            <template
              v-if="index > 1 && data.sendTime - messageList[index - 1].sendTime > 1000 * 60 * 5 && (data.messageType == 2 || data.messageType == 5)">
              <ChatMessageTime :data="data"></ChatMessageTime>
            </template>
            <!-- 系统消息
             1: 添加好友成功
             3：创建群聊成功
             9：好友加入群组
             11：退出群聊
             12：删除群成员
            -->
            <template v-if="data.messageType == 1 ||
              data.messageType == 3 ||
              data.messageType == 8 ||
              data.messageType == 9 ||
              data.messageType == 11 ||
              data.messageType == 12">
              <ChatMessageSys :data="data"></ChatMessageSys>
            </template>
            <template v-if="data.messageType == 1 || data.messageType == 2 || data.messageType == 5">
              <ChatMessage :data="data" :currentChatSession="currentChatSession"
                @showMediaDetail="showMediaDetailHandler"></ChatMessage>
            </template>
          </div>
        </div>
        <MessageSend :currentChatSession="currentChatSession" @sendMessage4Local="sendMessage4LocalHandler">
        </MessageSend>
      </div>
      <div class="chat-blank" v-show="Object.keys(currentChatSession).length == 0">
        <Blank></Blank>
      </div>
    </template>
  </Layout>
  <ChatGroupDetail ref="chatGroupDetailRef"></ChatGroupDetail>
</template>

<script setup>
import ChatGroupDetail from './ChatGroupDetail.vue';
import ChatMessage from './ChatMessage.vue';
import MessageSend from './MessageSend.vue';
import Blank from '@/components/Blank.vue';
import ContextMenu from '@imengyu/vue3-context-menu'
import ChatMessageTime from './ChatMessageTime.vue'
import ChatMessageSys from './ChatMessageSys.vue';
import '@imengyu/vue3-context-menu/lib/vue3-context-menu.css'
import ChatSession from "./ChatSession.vue";
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted, watch } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()

const searchKey = ref();
const search = () => { }

const chatSessionList = ref([]);

const loadChatSession = () => {
  window.ipcRenderer.send("loadSessionData")
}

// 会话排序
const sortChatSessionList = (dataList) => {
  dataList.sort((a, b) => {
    const topTypeResult = b["topType"] - a["topType"]
    if (topTypeResult == 0) {
      return b["lastReceiveTime"] - a["lastReceiveTime"]
    }
    return topTypeResult
  })
}

const delChatSessionList = (contactId) => {
  chatSessionList.value = chatSessionList.value.filter(item => {
    return item.contactId != contactId
  })
}

const onReceiveMessage = () => {
  window.ipcRenderer.on("receiveMessage", (e, message) => {
    console.log('收到消息', message)

    if (message.messageType == 6) {
      const localMessage = messageList.value.find(item => {
        if (item.messageId == message.messageId) {
          return item
        }
      })
      if (localMessage != null) {
        localMessage.status = 1
      }
      return;
    }

    let curSession = chatSessionList.value.find((item) => {
      return item.sessionId == message.sessionId
    })
    if (curSession == null) {
      chatSessionList.value.push(message.extendData)
    } else {
      Object.assign(curSession, message.extendData)
    }

    sortChatSessionList(chatSessionList.value)

    if (message.sessionId != currentChatSession.value.sessionId) {
      // 展示未读消息
    } else {
      Object.assign(currentChatSession.value, message.extendData)
      messageList.value.push(message)
      gotoBottom('auto')
    }
  })
}

const onLoadSessionData = () => {
  window.ipcRenderer.on("loadSessionDataCallback", (e, dataList) => {
    sortChatSessionList(dataList)
    chatSessionList.value = dataList;
  })
}

// const onLoadChatMessage = () => {
//   window.ipcRenderer.on("loadChatMessageCallback", (e, {dataList, pageNo, pageTotal}) => {
//     if (pageNo == pageTotal) {
//       messageCountInfo.noData = true
//     }
//     dataList.sort((a, b) => {
//       return a.messageId - b.messageId
//     })
//     messageList.value = dataList.concat(messageList.value)
//     messageCountInfo.pageNo = pageNo
//     messageCountInfo.pageTotal = pageTotal
//     if (pageNo == 1) {
//       messageCountInfo.maxMessageId = dataList.length > 0 ? dataList[dataList.length - 1].messageId : null
//       gotoBottom();
//     }
//     console.log(messageList.value)
//   })
// }

const onLoadChatMessage = () => {
  window.ipcRenderer.on("loadChatMessageCallback", (e, { dataList, hasMore }) => {
    messageCountInfo.isLoading = false;

    if (dataList.length === 0) {
      messageCountInfo.hasMore = false;
      return;
    }

    // 按时间排序
    dataList.sort((a, b) => a.messageId - b.messageId);

    // 更新消息ID范围
    const newMinId = dataList[0].messageId;

    nextTick(() => {
      gotoBottom('auto')
    })

    if (!messageCountInfo.minMessageId || newMinId < messageCountInfo.minMessageId) {
      messageCountInfo.minMessageId = newMinId;
    }

    // 合并消息列表
    messageList.value = [...dataList, ...messageList.value]
      .filter((v, i, a) => a.findIndex(t => t.messageId === v.messageId) === i)
      .sort((a, b) => a.messageId - b.messageId);

    messageCountInfo.hasMore = hasMore;
  });
}

const onAddLocalMessage = () => {
  window.ipcRenderer.on('addLocalCallback', (e, { messageId, status }) => {
    const findMessage = messageList.value.find(item => {
      if (item.messageId == messageId) {
        return item
      }
    })
    if (findMessage != null) {
      findMessage.status = status
    }
  })
}

// 当前选中的会话
const currentChatSession = ref({})

// 点击会话
// let distanceBottom = 0
const messageList = ref([])
const messageCountInfo = reactive({
  isLoading: false,
  hasMore: true,
  minMessageId: null,  // 用于加载更旧的消息
  maxMessageId: null,  // 用于加载更新的消息
  lastScrollHeight: 0  // 用于保持滚动位置
})
const chatSessionClickHandler = (item) => {
  // distanceBottom = 0
  currentChatSession.value = Object.assign({}, item);
  messageList.value = []

  // messageCountInfo.pageNo = 0
  // messageCountInfo.tatalPage = 1
  // messageCountInfo.maxMessageId = null
  // messageCountInfo.noData = false

  // 充值分页信息
  Object.assign(messageCountInfo, {
    isLoading: false,
    hashMore: true,
    minMessageId: null,
    scrollLock: false,
    noData: false,
  })

  loadChatMessage('initial')
  // 设置选中的会话session
  setSessionSelect({ contactId: item.contactId, sessionId: item.sessionId })

}
const setSessionSelect = ({ contactId, sessionId }) => {
  window.ipcRenderer.send("setSessionSelect", {
    contactId, sessionId
  });
};

// const loadChatMessage = () => {
//   if (messageCountInfo.noData) {
//     return;
//   }
//   // messageCountInfo.pageNo++
//   window.ipcRenderer.send("loadChatMessage", {
//     sessionId: currentChatSession.value.sessionId,
//     // pageNo: messageCountInfo.pageNo,
//     maxMessageId: messageCountInfo.maxMessageId
//   })
// }

const sendMessage4LocalHandler = (messageObj) => {
  messageList.value.push(messageObj)
  const chatSession = chatSessionList.value.find((item) => {
    return item.sessionId == messageObj.sessionId
  })
  if (chatSession) {
    chatSession.lastMessage = messageObj.lastMessage
    chatSession.lastReceiveTime = messageObj.sendTime
  }
  sortChatSessionList(chatSessionList.value)
  gotoBottom('auto')
}

const loadChatMessage = async (loadType = 'initial') => {
  if (messageCountInfo.isLoading || (loadType == 'history' && !messageCountInfo.hasMore)) {
    return
  }

  messageCountInfo.isLoading = true

  const panel = document.getElementById('message-panel');
  if (panel && loadType === 'history') {
    messageCountInfo.lastScrollHeight = panel.scrollHeight;
  }

  window.ipcRenderer.send("loadChatMessage", {
    sessionId: currentChatSession.value.sessionId,
    loadType,
    minMessageId: messageCountInfo.minMessageId,
    maxMessageId: messageCountInfo.maxMessageId
  });
}

const handleMessageScroll = (event) => {
  if (messageCountInfo.isLoading) {
    return
  }

  const panel = event.target

  if (messageCountInfo.hasMore) {
    loadChatMessage('history');

    nextTick(() => {
      if (messageCountInfo.lastScrollHeight > 0) {
        panel.scrollTop = panel.scrollHeight - messageCountInfo.lastScrollHeight;
        messageCountInfo.lastScrollHeight = 0;
      }
    });
  }
}


//滚动到底部
// const gotoBottom = () => {
//   nextTick(() => {
//     const items = document.querySelectorAll(".message-item")
//     if (items.length > 0) {
//       setTimeout(() => {
//         items[items.length - 1].scrollIntoView()
//       }, 100)
//     }
//   })
// }

const gotoBottom = (behavior = 'smooth') => {
  nextTick(() => {
    // if(distanceBottom > 200){
    //   return
    // }
    const panel = document.getElementById('message-panel')
    if (!panel) return
    requestAnimationFrame(() => {
      panel.scrollTo({
        top: panel.scrollHeight,
        behavior: behavior
      })
    })
  })
}

const onReloadChatSession = ()=>{
  window.ipcRenderer.on('reloadChatSessionCallback', (e, { contactId, chatSessionList })=>{
    sortChatSessionList(chatSessionList)
    chatSessionList.value = chatSessionList
    sendMessage(contactId)
  })
}

onMounted(() => {
  onReceiveMessage()
  onLoadSessionData()
  loadChatSession()
  onLoadChatMessage()
  onAddLocalMessage()
  onReloadChatSession()

})

onUnmounted(() => {
  window.ipcRenderer.removeAllListeners("receiveMessage")
  window.ipcRenderer.removeAllListeners("loadSessionDataCallback")
  window.ipcRenderer.removeAllListeners("loadChatMessage")
  window.ipcRenderer.removeAllListeners("onAddLocalMessage")
  window.ipcRenderer.removeAllListeners("reloadChatSessionCallback")
})

const setTop = (data) => {
  data.topType = data.topType == 0 ? 1 : 0
  // 会话排序
  sortChatSessionList(chatSessionList.value)
  window.ipcRenderer.send("topChatSession", { contactId: data.contactId, topType: data.topType })
}
const delChatSession = (contactId) => {
  delChatSessionList(contactId)
  currentChatSession.value = {}
  // 设置选中的会话
  window.ipcRenderer.send("delChatSession", contactId)
}
const onContextMenu = (data, e) => {
  ContextMenu.showContextMenu({
    x: e.x,
    y: e.y,
    items: [{
      label: data.topType == 0 ? "置顶" : "取消置顶",
      onClick: () => {
        setTop(data)
      }
    }, {
      label: "删除聊天",
      onClick: () => {
        proxy.Confirm({
          message: `确定要删除聊天【${data.contactName}】吗？`,
          okfun: () => {
            delChatSession(data.contactId)
          }
        })
      }
    }
    ]
  })
}

const showMediaDetailHandler = (messageId) => {
  let showFileList = messageList.value.filter(item => {
    return item.messageType == 5
  })
  showFileList = showFileList.map(item => {
    return {
      partType: 'chat',
      fileId: item.messageId,
      fileType: item.fileType,
      fileName: item.fileName,
      fileSize: item.fileSize,
      forceGet: false,
    }
  })
  window.ipcRenderer.send('newWindow', {
    windowId: 'media',
    title: '图片查看',
    path: '/showMedia',
    data: {
      currentFileId: messageId,
      fileList: showFileList
    }
  })
}

const chatGroupDetailRef = ref()
const showGroupDetail = () => {
  chatGroupDetailRef.value.show(currentChatSession.value.contactId)
}

const sendMessage = (contactId)=>{
  let curSession = chatSessionList.value.find(item=>{
    return item.contactId == contactId
  })
  if(!curSession){
    window.ipcRenderer.send('reloadChatSession', {contactId})
    return;
  }else{
    chatSessionClickHandler(curSession)
  }
}
watch(() => route.query.timestamp,
  (newVal, oldVal) => {
    if(newVal && route.query.chatId){
      sendMessage(route.query.chatId)
    }
  },
  { immediate: true, deep: true }
)

</script>

<style lang="scss" scoped>
.drag-panel {
  height: 25px;
  background: #f7f7f7;
}

.top-search {
  padding: 0px 10px 9px 10px;
  background: #f7f7f7;
  display: flex;
  align-items: center;

  .iconfont {
    font-size: 12px;
  }
}

.chat-session-list {
  height: calc(100vh - 62px);
  overflow: hidden;
  border-top: 1px solid #dddddd;

  &:hover {
    overflow: auto;
  }
}

.search-list {
  height: calc(100vh - 62px);
  background: #f7f7f7;
  overflow: hidden;

  &:hover {
    overflow: auto;
  }
}

.title-panel {
  display: flex;
  align-items: center;

  .title {
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

.icon-more {
  position: absolute;
  z-index: 1;
  top: 30px;
  right: 3px;
  width: 20px;
  margin-right: 5px;
  cursor: pointer;
}

.chat-panel {
  border-top: 1px solid #dddddd;
  background: #f5f5f5;

  .message-panel {
    paading: 10px 30px 0px 30px;
    height: calc(100vh - 200px - 62px);
    overflow-y: auto;
    scroll-behavior: smooth;

    .message-item {
      margin-bottom: 15px;
      text-align: center;
    }
  }

  .loading-indicator {
    text-align: center;
    padding: 10px;
    color: #666;
  }
}
</style>
