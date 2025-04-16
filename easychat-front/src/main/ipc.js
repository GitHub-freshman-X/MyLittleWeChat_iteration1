import { app, shell, BrowserWindow, ipcMain } from 'electron'
import { join } from 'path'
import { electronApp, optimizer, is } from '@electron-toolkit/utils'
import icon from '../../resources/icon.png?asset'
const NODE_ENV = process.env.NODE_ENV
import store from "./store"
import {initWs}from './wsClient'
import { addUserSetting } from './db/UserSettingModel'
import { selectUserSessionList, delChatSession, topChatSession } from './db/ChatSessionUserModel'
const onLoginOrRegister = (callback) => {
  ipcMain.on("loginOrRegister", (e, isLogin) => {
    callback(isLogin)
  })
}

const onLoginSuccess = (callback) => {
  // 发送openChat事件，config是自定义的参数，是一个对象，封装了所有数据
  ipcMain.on("openChat", (e, config) => {
    store.initUserId(config.userId);
    store.setUserData("token", config.token);
    addUserSetting(config.userId, config.email);
    callback(config);
    initWs(config,e.sender);
    });
}

const winTitleOp = (callback)=> {
  ipcMain.on("winTitleOp", (e, data) => {
    callback(e, data);
  })
}

const onSetLocalStore=()=>{
  ipcMain.on("setLocalStore", (e, {key, value}) => {
    store.setData(key, value);
  
  })
}

const onGetLocalStore=()=>{
  ipcMain.on("getLocalStore", (e, key) => {
    //console.log("收到渲染进程的获取事件key",key);
    e.sender.send("getLocalStoreCallback", "主进程返回的内容："+store.getData(key));
  })
}

const onLoadSessionData = ()=>{
  ipcMain.on("loadSessionData", async(e)=>{
    const result = await selectUserSessionList()
    e.sender.send("loadSessionDataCallback", result)
  })

}

const onDelChatSession = ()=>{
  ipcMain.on("delChatSession", (e, contactId)=>{
    delChatSession(contactId);
  })
}

const onTopChatSession = ()=>{
  ipcMain.on("topChatSession", (e, {contactId, topType})=>{
    topChatSession(contactId, topType);
  })
}

export {
  onLoginOrRegister,
  onLoginSuccess,
  winTitleOp,
  onSetLocalStore,
  onGetLocalStore,
  onLoadSessionData,
  onDelChatSession,
  onTopChatSession
}