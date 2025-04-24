import { app, shell, BrowserWindow, ipcMain } from 'electron'
import { join } from 'path'
import { electronApp, optimizer, is } from '@electron-toolkit/utils'
import icon from '../../resources/icon.png?asset'
import { saveFile2Local, createCover } from './file'
const NODE_ENV = process.env.NODE_ENV
import store from "./store"
import {initWs}from './wsClient'
import { addUserSetting } from './db/UserSettingModel'
import { selectUserSessionList, delChatSession, topChatSession, updateSessionInfo4Message, readAll } from './db/ChatSessionUserModel'
import { saveMessage, selectMessageList, updateMessage } from './db/ChatMessageModel'
import { delWindow, getWindow, saveWindow } from './windowProxy'

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
    e.sender.send("getLocalStoreCallback", store.getData(key));
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

const onLoadChatMessage = ()=>{
  ipcMain.on("loadChatMessage", async (e, data)=>{
    // debugger
    const result = await selectMessageList(data);
    e.sender.send("loadChatMessageCallback", result)
  })
}

const onSetSessionSelect = ()=>{
  ipcMain.on("setSessionSelect",async (e, { contactId, sessionId })=>{

    if(sessionId){
      store.setUserData("currentSessionId", sessionId)
      readAll(contactId);
    }else{
      store.deleteUserData("currentSessionId")
    }     
  })
};


const onAddLocalMessage = ()=>{
  ipcMain.on("addLocalMessage", async (e, data)=>{
    await saveMessage(data);
    //保存文件

    if(data.messageType==5){
      await saveFile2Local(data.messageId, data.filePath, data.fileType);
      const updateInfo = {
        status: 1
      }
      await updateMessage(updateInfo, { messageId: data.messageId })
    }


    //更新session
    data.lastReceiveTime = data.sendTime;
    //更新会话
    updateSessionInfo4Message(store.getData("currentSessionId"), data);
    e.sender.send("addLocalCallback", {status: 1, messageId: data.messageId });
  })
};

const onCreateCover = ()=>{
  ipcMain.on('createCover', async (e, localFilePath)=>{
    const stream = await createCover(localFilePath);
    e.sender.send('createCoverCallback', stream);
  })
}

const onOpenNewWindow = ()=>{
  ipcMain.on('newWindow', async(e, config)=>{
    openWindow(config)
  })
}

const openWindow = ({windowId, title='EasyChat', path, width=960, height=720, data})=>{
  let newWindow = getWindow(windowId);
  if(!newWindow){
    newWindow = new BrowserWindow({
        icon: icon,
        width: width,
        height: height,
        fullscreenable: false,
        fullscreen: false,
        maximizable: false,
        autoHideMenuBar: true,
        titleBarStyle: 'hidden',
        resizable: false,
        frame: true,
        transparent: true,
        hasShadow: false,
        webPreferences: {
          preload: join(__dirname, '../preload/index.js'),
          sandbox: false,
          contextIsolation: false,
        }
      })
      saveWindow(windowId, newWindow)
      newWindow.setMinimumSize(600, 480)
      if(is.dev && process.env['ELECTRON_RENDERER_URL']) {
        newWindow.loadURL(`${process.env['ELECTRON_RENDERER_URL']}/index.html#${path}`)
      }else{
        newWindow.loadFile(join(__dirname, `../renderer/index.html`), {hash:`${path}`})
      }
      // 开发模式下打开开发者工具
      if(NODE_ENV=='development'){
        newWindow.webContents.openDevTools()
      }
      newWindow.on('ready-to-show', () => {
        newWindow.show()
        newWindow.setTitle(title)
      })
      newWindow.once('show', ()=>{
        setTimeout(()=>{
          newWindow.webContents.send('pageInitData', data)
        }, 500)
      })
      newWindow.on('closed', ()=>{
        delWindow(windowId)
      })
  }else{
    newWindow.show()
    newWindow.setSkipTaskbar(false)
    newWindow.webContents.send('pageInitData', data)
  }
}


export {
  onLoginOrRegister,
  onLoginSuccess,
  winTitleOp,
  onSetLocalStore,
  onGetLocalStore,
  onLoadSessionData,
  onDelChatSession,
  onTopChatSession,
  onLoadChatMessage,
  onAddLocalMessage,
  onSetSessionSelect,
  onCreateCover,
  onOpenNewWindow,
}