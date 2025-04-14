const fs = require("fs");
const sqlite3 = require("sqlite3").verbose();
const os = require("os");
const NODE_ENV = process.env.NODE_ENV;

const userDir = os.homedir();
const dbFolder = userDir + (NODE_ENV === "development" ? "/.easychattest/" : "/.easychat/");
console.log(dbFolder);
if(!fs.existsSync(dbFolder)){
  fs.mkdirSync(dbFolder);
}


const createTable = ()=> {

}

export{
  createTable
}
