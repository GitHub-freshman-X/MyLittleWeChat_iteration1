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
import { updateNoReadCount } from "./ChatSessionUserModel.js"

const saveMessage = (data)=>{
  data.userId = store.getUserId()
  return insertOrReplace("chat_message", data)
}

const saveMessageBatch = (chatMessageList)=>{
  return new Promise(async (resolve, reject) => {
    const chatSessionCountMap = {}
    chatMessageList.forEach(item=>{
      let contactId = item.contactType==1 ? item.contactId : item.sendUserId
      let noReadCount = chatSessionCountMap[contactId]
      if(!noReadCount){
        chatSessionCountMap[contactId] = 1
      }else{
        chatSessionCountMap[contactId] = noReadCount+1
      }
    })

    // 更新未读数
    for(let item in chatSessionCountMap){
      await updateNoReadCount({contactId: item, noReadCount: chatSessionCountMap[item]})
    }

    // 批量插入
    chatMessageList.forEach(async item=>{
      await saveMessage(item)
    })
    resolve()

  })
}

const getPageOffset = (pageNo=1, totalCount)=>{
  const pageSize = 20;
  const pageTotal = totalCount%pageSize==0 ? totalCount/pageSize : Number.parseInt(totalCount/pageSize)+1
  pageNo = pageNo<=1 ? 1 : pageNo
  pageNo = pageNo>=pageTotal ? pageTotal : pageNo
  return{
    pageTotal,
    offset: (pageNo-1)*pageSize,
    limit: pageSize
  }
}

// const selectMessageList = async(query)=>{
//   return new Promise(async (resolve, reject) => {
//     // debugger
//     const { sessionId, pageNo, maxMessageId } = query
//     let sql = "select count(1) from chat_message where session_id = ? "
//     const totalCount = await queryCount(sql, sessionId)

//     // console.log('selectMessageList count: ', totalCount)

//     const {pageTotal, offset, limit} = getPageOffset(pageNo, totalCount)

//     const params = [sessionId]
//     sql = "select * from chat_message where session_id = ? "
//     if(maxMessageId){
//       sql = sql + " and message_id <= ?"
//       params.push(maxMessageId)
//     }
//     params.push(offset)
//     params.push(limit)
//     sql = sql + " order by message_id limit ?, ?"
//     const dataList = await queryAll(sql, params)

//     console.log('selectMessageList: ', sql, params)

//     resolve({
//       dataList,
//       pageTotal,
//       pageNo
//     })

//   })
// }

const selectMessageList = async(query)=>{
  return new Promise(async (resolve, reject) =>{
    const { sessionId, minMessageId } = query;
    const pageSize = 20;

    let sql = "select * from chat_message where session_id = ? ";
    const params = [sessionId];

    // 添加消息ID过滤
    if(minMessageId){
      sql += " and message_id < ? ";
      params.push(minMessageId);
    }

    sql += " order by message_id desc limit ? ";
    params.push(pageSize);

    try {
      const dataList = await queryAll(sql, params);
      const hasMore = dataList.length == pageSize;
      const newMinMessageId = dataList.length > 0
        ? dataList[dataList.length - 1].messageId
        : null;
      
      resolve({
        dataList: dataList.reverse(),
        hasMore,
        minMessageId: newMinMessageId,
      });
    } catch(error){
      reject(error);
    }
  })
}

const updateMessage = (data, paramData)=>{
  return update('chat_message', data, paramData)
}

const selectByMessageId = (messageId)=>{
  let sql = "select * from chat_message where message_id = ?"
  return queryOne(sql, messageId)
}


export {
  saveMessageBatch,
  selectMessageList,
  saveMessage,
  updateMessage, 
  selectByMessageId
}