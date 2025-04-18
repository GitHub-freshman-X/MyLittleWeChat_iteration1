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

const globalColumnsMap = {};

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

const initTableColumnsMap = async()=>{
  let sql = `select name from sqlite_master where type='table' and name!='sqlite_sequence'`;
  let tables = await queryAll(sql, []);
  for(let i=0; i<tables.length; ++i){
    sql = `pragma table_info(${tables[i].name})`;
    let columns = await queryAll(sql, []);
    const columnMapItem = {};
    for(let j=0; j<columns.length; ++j){
      columnMapItem[toCamelCase(columns[j].name)] = columns[j].name;
    }
    globalColumnsMap[tables[i].name] = columnMapItem;
  }
  console.log(globalColumnsMap)
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


const queryAll =(sql, params) =>{
  console.log('queryAll: ', sql)
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

const queryCount = (sql, params) =>{
  return new Promise((resolve, reject) => {
    const stmt = db.prepare(sql);
    stmt.get(params, function(err, row){
      if(err){
        resolve(0)
      }
      resolve(Array.from(Object.values(row))[0])
    })
    stmt.finalize()
  })
}

const queryOne = (sql, params) =>{
  return new Promise((resolve, reject) => {
    const stmt = db.prepare(sql);
    stmt.get(params, function(err, row){
      if(err){
        resolve({})
      }
      resolve(convertDbObject2BizObj(row))
      console.log(`执行的sql：${sql}, params:${params}, row:${JSON.stringify(row)}`)
    })
    stmt.finalize()
  })
}

const run = (sql, params)=>{
  return new Promise((resolve, reject) => {
    const stmt = db.prepare(sql);
    stmt.run(params, function(err, row){
      if(err){
        console.error(`执行的sql：${sql}, params:${params}, err:${err}`)
        resolve("操作数据库失败")
      }
      console.log(`执行的sql：${sql}, params:${params}, 执行记录数：${this.changes}, row:${JSON.stringify(row)}`)
      resolve(this.changes)
    })
    stmt.finalize()
  })
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
  return run(sql, params)
}

const insertOrReplace = (tableName, data)=> {
  return insert("insert or replace into", tableName, data)
}

const insertOrIgnore = (tableName, data)=> {
  return insert("insert or ignore into", tableName, data)
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
  return run(sql, params)
}

const init = ()=>{
  db.serialize(async()=>{
    await createTable()
    await initTableColumnsMap()
  })
}

init()

export{
  run,
  queryAll,
  queryOne,
  queryCount,
  insertOrReplace,
  insertOrIgnore,
  update,
  insert
}
