import {   run,
  queryAll,
  queryOne,
  queryCount,
  insertOrReplace,
  insertOrIgnore,
  update,
  insert 
} from "./ADB.js"
import store from "../store"
import { startLocalServer } from "../file.js";

const os = require("os")
const userDir = os.homedir();

const updateContactNoReadCount = ({userId, noReadCount})=>{
  return new Promise(async (resolve, reject) => {
    let sql = null;

    if(noReadCount==0){
      resolve();
      return;
    }

    if(noReadCount){
      sql = "update user_setting set contact_no_read = contact_no_read+? where user_id=?"
    }else{
      // 清空未读数
      noReadCount = 0;
      sql = "update user_setting set contact_no_read = ? where user_id=?"
    }

    await run(sql, [noReadCount, userId])

    resolve()

  })
}
const addUserSetting = async (userId, email)=>{
  let sql = "select max(server_port) server_port from user_setting"
  let {serverPort} = await queryOne(sql, []);
  if(serverPort==null){
    serverPort = 10340;
  }else{
    serverPort = serverPort+1;
  }
  const sysSetting = {
    localFileFolder: userDir + "/.easychat/fileStorage/"
  }
  sql = "select * from user_setting where user_id=?"
  const userInfo = await queryOne(sql, [userId]);

  let resultServerPort = null
  let localFileFolder = sysSetting.localFileFolder + userId
  if(userInfo){
    await update("user_setting", {"email": email}, {"userId": userId})
    resultServerPort = userInfo.serverPort;
    localFileFolder = JSON.parse(userInfo.sysSetting).localFileFolder + userId
  }else{
    await insertOrIgnore("user_setting",{
      userId: userId, 
      email: email,
      sysSetting: JSON.stringify(sysSetting),
      contactNoRead: 0,
      serverPort: serverPort
    })
    resultServerPort = serverPort;
  }

  // 启动本地服务
  startLocalServer(resultServerPort)
  store.setUserData("localServerPort", resultServerPort);
  store.setUserData("localFileFolder", localFileFolder);
  // console.log("setUserData localFileFolder : " , localFileFolder)
}

export {
  updateContactNoReadCount,
  addUserSetting
}
