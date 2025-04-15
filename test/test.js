
const globalColumnsMap = {
  chat_message: {
    userId: 'user_id',
    messageId: 'message_id',
    sessionId: 'session_id',
    messageType: 'message_type',
    messageContent: 'message_content',
    contactType: 'contact_type',
    sendUserId: 'send_user_id',
    sendUserNickName: 'send_user_nick_name',
    sendTime: 'send_time',
    status: 'status',
    fileSize: 'file_size',
    fileName: 'file_name',
    filePath: 'file_path',
    fileType: 'file_type'
  },
  chat_session_user: {
    userId: 'user_id',
    contactId: 'contact_id',
    contactType: 'contact_type',
    sessionId: 'session_id',
    status: 'status',
    contactName: 'contact_name',
    lastMessage: 'last_message',
    lastReceiveTime: 'last_receive_time',
    noReadCount: 'no_read_count',
    memberCount: 'member_count',
    topType: 'top_type'
  },
  user_setting: {
    userId: 'user_id',
    email: 'email',
    sysSetting: 'sys_setting',
    contactNoRead: 'contact_no_read',
    serverPort: 'server_port'
  }
}

const insert = (sqlPrefix, tableName, data)=>{
  const columnsMap = globalColumnsMap[tableName]
  const dbColums = [];
  const params = []
  for(let item in data){
    if(data[item]!=undefined && columnsMap[item]!=undefined){
      dbColums.push(columnsMap[item]);
      params.push(data[item]);
    }
  }
  const preper = "?".repeat(dbColums.length).split("").join(",")
  const sql = `${sqlPrefix} ${tableName} (${dbColums.join(",")}) values (${preper})`;
  console.log(sql)
}

const update = (tableName, data, paramData)=>{
  const columnsMap = globalColumnsMap[tableName]
  const dbColums = [];
  const params = []
  const whereColumns = []
  for(let item in data){
    if(data[item]!=undefined && columnsMap[item]!=undefined){
      dbColums.push(`${columnsMap[item]} = ?`);
      params.push(data[item]);
    }
  }

  for(let item in paramData){
    if(paramData[item]){
      params.push(paramData[item])
      whereColumns.push(`${columnsMap[item]} = ?`)
    }
  }
  
  const sql = `update ${tableName} set ${dbColums.join(",")} ${whereColumns.length>0 ? 'where' : ''} ${whereColumns.join(" and ")}`
  console.log(sql)
}

insert('insert', 'chat_message',{
  userId: "124134",
  messageId: "2344"
})

update('chat_message', {
  userId: "123", 
  messageId:"123123",
  messageType: "23"
}, {
  userId: "123",
  messageId:"123123"
})
