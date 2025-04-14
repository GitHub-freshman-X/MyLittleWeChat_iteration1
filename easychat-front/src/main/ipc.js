import { app, shell, BrowserWindow, ipcMain } from 'electron'
import { join } from 'path'
import { electronApp, optimizer, is } from '@electron-toolkit/utils'
import icon from '../../resources/icon.png?asset'
const NODE_ENV = process.env.NODE_ENV
import store from "./store"

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
    callback(config);
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
    console.log("收到渲染进程的获取事件key",key);
    e.sender.send("getLocalStoreCallback", "主进程返回的内容："+store.getData(key));
  })
}

export {
  onLoginOrRegister,
  onLoginSuccess,
  winTitleOp,
  onSetLocalStore,
  onGetLocalStore
}