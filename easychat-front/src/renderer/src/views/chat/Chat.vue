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
            <template v-if="data.messageType == 1 || data.messageType == 2 || data.messageType == 5">
              <ChatMessage :data="data" :currentChatSession="currentChatSession"></ChatMessage>
            </template>
          </div>
        </div>
        <MessageSend :currentChatSession="currentChatSession" @sendMessage4Local="sendMessage4LocalHandler">
        </MessageSend>
      </div>
    </template>
  </Layout>
</template>

<script setup>
import ChatMessage from './ChatMessage.vue';
import MessageSend from './MessageSend.vue';
import ContextMenu from '@imengyu/vue3-context-menu'
import '@imengyu/vue3-context-menu/lib/vue3-context-menu.css'
import ChatSession from "./ChatSession.vue";
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted } from "vue"
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

// 当前选中的会话
const currentChatSession = ref({})

// 点击会话
const messageList = ref([])
const messageCountInfo = reactive({
  isLoading: false,
  hasMore: true,
  minMessageId: null,  // 用于加载更旧的消息
  maxMessageId: null,  // 用于加载更新的消息
  lastScrollHeight: 0  // 用于保持滚动位置
})
const chatSessionClickHandler = (item) => {
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
    const panel = document.getElementById('message-panel')
    if(!panel) return
    requestAnimationFrame(() => {
      panel.scrollTo({
        top: panel.scrollHeight,
        behavior: behavior
      })
    })
  })
}

onMounted(() => {
  onReceiveMessage()
  onLoadSessionData()
  loadChatSession()
  onLoadChatMessage()
})

onUnmounted(() => {
  window.ipcRenderer.removeAllListeners("receiveMessage")
  window.ipcRenderer.removeAllListeners("loadSessionDataCallback")
  window.ipcRenderer.removeAllListeners("loadChatMessage")
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
