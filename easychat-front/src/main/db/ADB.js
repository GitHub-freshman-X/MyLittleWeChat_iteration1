const fs = require("fs");
const sqlite3 = require("sqlite3").verbose();
const os = require("os");
const NODE_ENV = process.env.NODE_ENV;

import { add_tables, add_index, alter_tables } from "./Tables.js";

const userDir = os.homedir();
const dbFolder = userDir + (NODE_ENV === "development" ? "/.easychattest/" : "/.easychat/");
console.log(dbFolder);
if(!fs.existsSync(dbFolder)){
  fs.mkdirSync(dbFolder);
}

const db = new sqlite3.Database(dbFolder + "local.db");

const createTable = ()=> {
  return new Promise(async (resolve, reject)=> {
    // 创建表
    for(const item of add_tables){
      await db.run(item)
    }

    // 创建索引
    for(const item of add_index){
      await db.run(item)
    }

    // 修改表
    for(const item of alter_tables){
      const fieldList = await queryAll(`pragma table_info(${item.tableName})`, [])
      const field = fieldList.some(row=>row.name === item.field)
      if(!field){
        await db.run(item.sql)
      }
    }

    resolve()
  })
}

const queryAll =(sql, params) =>{
  return new Promise((resolve, reject) => {
    const stmt = db.prepare(sql);
    stmt.all(params, function(err, row){
      if(err){
        resolve([])
      }
      row.forEach((item,  index) => {
        row[index] = convertDbObject2BizObj(item)
      })
      resolve(row)
    })
    stmt.finalize()
  })
}

const convertDbObject2BizObj = (data) => {
  if(!data){
    return null;
  }
  const bizData = {}
  for(let item in data){
    bizData[toCamelCase(item)] = data[item]
  }
  return bizData
}

const toCamelCase = (str)=>{
  return str.replace(/_([a-z])/g, function(match, p1){
    return String.fromCharCode(p1.charCodeAt(0) - 32);
  })
}

const init = ()=>{
  db.serialize(async()=>{
    await createTable()
  })
}

init()

export{
  createTable
}
