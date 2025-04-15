
const globalColumnsMap = {
  chat_message: {
    userId: 'varchar',
    messageId: 'bigint',
    sessionId: 'varchar',
    messageType: 'INTEGER',
    messageContent: 'varchar',
    contactType: 'INTEGER',
    sendUserId: 'varchar',
    sendUserNickName: 'varchar',
    sendTime: 'bigint',
    status: 'INTEGER',
    fileSize: 'bigint',
    fileName: 'varchar',
    filePath: 'varchar',
    fileType: 'INTEGER'
  },
  chat_session_user: {
    userId: 'varchar',
    contactId: 'varchar(11)',
    contactType: 'INTEGER',
    sessionId: 'varchar(11)',
    status: 'INTEGER',
    contactName: 'varchar(20)',
    lastMessage: 'varchar(500)',
    lastReceiveTime: 'bigint',
    noReadCount: 'INTEGER',
    memberCount: 'INTEGER',
    topType: 'INTEGER'
  },
  user_setting: {
    userId: 'varchar',
    email: 'varchar',
    sysSetting: 'varchar',
    contactNoRead: 'INTEGER',
    serverPort: 'INTEGER'
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
  const sql = `${sqlPrefix} ${tableName} (${dbColums.join(",")}) values(${preper})`;
  console.log(sql)
}

insert('insert', 'chat_message', {
  userId: "123", 
  messageId:"123123",
  messageType: "23"
})
