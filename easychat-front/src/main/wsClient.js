import WebSocket from 'ws';
const NODE_ENV = process.env.NODE_ENV

import store from "./store"

import { saveOrUpdateChatSessionBatch4Init, saveOrUpdate4Message, selectUserSessionByContactId } from './db/ChatSessionUserModel';
import { saveMessageBatch, saveMessage, updateMessage } from './db/ChatMessageModel';
import { updateContactNoReadCount } from './db/UserSettingModel';

let ws = null;
let maxReConnectTimes = null;
let lockReconnect = false;
let wsUrl = null;
let sender = null;
let needReconnect = null;
const initWs = (config, _sender) => {
  wsUrl = `${NODE_ENV !== 'development' ? store.getData("prodWsDomain"):store.getData("devWsDomain")}?token=${config.token}`;
  sender = _sender;

  needReconnect = true;
  maxReConnectTimes = 5;
  createWs();
}

const createWs = () => {
  if(wsUrl==null){
    return;
  }
  ws = new WebSocket(wsUrl);
  ws.onopen = function(){
    ws.send("heart beat");
    console.log("客户端连接成功");
    maxReConnectTimes = 5;
  }
  //从服务器接受到消息的回调函数
  ws.onmessage = async function(e){
    // console.log("收到服务器消息", e.data);
    const message = JSON.parse(e.data);
    const messageType = message.messageType
    switch(messageType){
      case 0: // ws连接成功
      {
        // 保存会话信息
        await saveOrUpdateChatSessionBatch4Init(message.extendData.chatSessionList);

        // 保存消息
        await saveMessageBatch(message.extendData.chatMessageList);

        // 更新联系人申请数量
        await updateContactNoReadCount({userId: store.getUserId(), noReadCount: message.extendData.applyCount});

        sender.send("receiveMessage", {messageType: message.messageType})
        break;
      }
      case 6:{
        updateMessage({status:message.status}, {messageId: message.messageId})
        sender.send('receiveMessage', message)
        break;
      }
      case 2:
      case 5:{
        if(message.sendUserId==store.getUserId() && message.contactType==1){
          break;
        }
        const sessionInfo = {}
        // contactType==1是群聊
        if(message.extendData && typeof message.extendData=='object'){
          Object.assign(sessionInfo, message.extendData)
        }else{
          Object.assign(sessionInfo, message)
          // contactType==0是单聊
          if(message.contactType==0 && messageType!=1){
            sessionInfo.contactName=message.sendUserNickName
          }
          sessionInfo.lastReceiveTime = message.sendTime
        }
        await saveOrUpdate4Message(store.getData("currentSessionId"), sessionInfo)
        // 写入本地消息
        await saveMessage(message)

        const dbSessionInfo = await selectUserSessionByContactId(message.contactId)
        message.extendData = dbSessionInfo
        sender.send("receiveMessage", message)
        break;
      }
    }

  }
  ws.onclose = function(){
      console.log("连接关闭");
      reconnect();
  }
  ws.onerror = function(){
      console.log("连接错误");
      reconnect();
  }
  const reconnect = () => {
    if(!needReconnect){
      console.log("不需要重连了");
      return;
    }
    if(ws != null){
      ws.close();
    }
    if(lockReconnect){
      console.log("正在重连中...");
      return;
    }
    lockReconnect = true;
    if(maxReConnectTimes>0){
      console.log("准备重连，剩余重连次数："+maxReConnectTimes,new Date().getTime());
      maxReConnectTimes--;
      setTimeout(() => {
        createWs();
        lockReconnect = false;
      }, 5000);
    }else{
      console.log("连接超时");
    }
  }

  setInterval(() => {
    if(ws != null && ws.readyState == 1){
      // console.log("心跳包发送成功" );
      ws.send("heart beat");
    }
  },5000);
}


const closeWs = () => {
  needReconnect = false;
  ws.close();
}


export{
  initWs,
  closeWs
}