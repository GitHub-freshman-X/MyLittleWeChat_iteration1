const fs = require('fs')
const fse = require('fs-extra')
const NODE_ENV = process.env.NODE_ENV
const path = require('path')
const { app, ipcMain, shell } = require('electron')
const { exec } = require('child_process')
const FormData = require('form-data')
const axios = require('axios')
import store from "./store"
const moment = require('moment')
moment.locale('zh-cn', {})

const saveFile2Local = (messageId, filePath, fileType)=>{
  return new Promise(async (resolve, reject)=>{
    resolve()
  })
}

export {
  saveFile2Local
}
