const fs = require('fs')
const fse = require('fs-extra')
const NODE_ENV = process.env.NODE_ENV
const path = require('path')
const { app, ipcMain, shell } = require('electron')
const { exec } = require('child_process')
const FormData = require('form-data')
const axios = require('axios')
import store from "./store"
import { selectByMessageId } from './db/ChatMessageModel'
const { dialog } = require('electron')
const moment = require('moment')
moment.locale('zh-cn', {})

const express = require('express')
const expressServer = express()

const cover_image_suffix = '_cover.png'
const image_suffix = '.png'

const ffprobePath = '/assets/ffprobe.exe'
const ffmpePath = '/assets/ffmpeg.exe'

const getDomain = ()=>{
  return NODE_ENV!=='development' ? store.getData('prodDomain') : store.getData('devDomain')
}

const mkdirs = (dir) =>{
  if(!fs.existsSync(dir)){
    const parentDir = path.dirname(dir)
    if(parentDir!=dir){
      mkdirs(parentDir)
    }
    fs.mkdirSync(dir)
  }
}

const getResouecesPath = ()=>{
  let resourcesPath = app.getAppPath()
  if(NODE_ENV!=='development'){
    resourcesPath = path.dirname(app.getPath('exe')+'/resoueces')
  }
  return resourcesPath
}

const getFFprobePath = ()=>{
  return path.join(getResouecesPath(), ffprobePath)
}

const getFFmegPath = ()=>{
  return path.join(getResouecesPath(), ffmpePath)
}

const execCommand = (command)=>{
  return new Promise((resolve, reject)=>{
    exec(command, (error, stdout, stderr) => {
      console.log('ffmpeg命令：' + command)
      if(error){
        console.error('执行命令失败：', error)
      }
      resolve(stdout)
    })
  })
}

const saveFile2Local = (messageId, filePath, fileType)=>{
  return new Promise(async (resolve, reject)=>{
    let savePath = await getLocalFilePath('chat', false, messageId);
    console.log('saveFile2Local savePath : ', savePath);
    
    fs.copyFileSync(filePath, savePath);

    let coverPath = null;

    if (fileType != 2) {
      // 获取视频编码格式
      let command = `${getFFprobePath()} -v error -select_streams v:0 -show_entries stream=codec_name "${filePath}"`;
      let result = await execCommand(command);
      result = result.replaceAll('\r\n', '')
      result = result.substring(result.indexOf('=') + 1)
      let codec = result.substring(0, result.indexOf('['))

      // 如果是 hevc，转码成 h264
      if (codec === 'hevc') {
        command = `${getFFmegPath()} -y -i "${filePath}" -c:v libx264 -crf 20 "${savePath}"`;
        await execCommand(command);
      }

      // 截取封面图
      coverPath = savePath + cover_image_suffix;
      command = `${getFFmegPath()} -i "${savePath}" -y -vframes 1 -vf "scale='if(gt(iw,ih),170,-1)':'if(gt(iw,ih),-1,170)'" "${coverPath}"`;
      await execCommand(command);
    }

    uploadFile(messageId, savePath, coverPath);
    resolve();
  })
}

const uploadFile = (messageId, savePath, coverPath)=>{
  const formData = new FormData()
  formData.append("messageId", messageId)
  formData.append("file", fs.createReadStream(savePath))
  if(coverPath){
    formData.append("cover", fs.createReadStream(coverPath))
  }
  const url = `${getDomain()}/api/chat/uploadFile`
  const token = store.getUserData('token')
  const config = { headers: {
    'Content-Type': 'multipart/form-data',
    'token': token,
  }}

  axios.post(url, formData, config).then(()=>{

  }).catch((error)=>{
    console.log('上传文件失败：', error)
  })

}

const getLocalFilePath = async(partType, showCover, fileId)=>{
  return new Promise(async (resolve, reject)=>{
    let localFolder = store.getUserData('localFileFolder')

    // console.log('getLocalFilePath: ', partType, fileId)

    let localPath = null
    if(partType=='avatar'){
      localFolder = localFolder + '/avatar/'
      if(!fs.existsSync(localFolder)){
        mkdirs(localFolder)
      }
      localPath = localFolder + fileId + image_suffix
    }else if(partType=='chat'){
      let messageInfo = await selectByMessageId(fileId)
      const month = moment(Number.parseInt(messageInfo.sendTime)).format('YYYYMM')
      localFolder = localFolder + '/' + month
      if(!fs.existsSync(localFolder)){
        mkdirs(localFolder)
      }
      let fileSuffix = messageInfo.fileName
      fileSuffix = fileSuffix.substring(fileSuffix.lastIndexOf('.'))
      localPath = localFolder + '/' + fileId + fileSuffix
    }
    // console.log('getLocalFilePath: ', localPath)'
    if(showCover){
      localPath = localPath + cover_image_suffix
    }
    resolve(localPath)
  })

}

let server = null
const startLocalServer = (serverPort)=>{
  server = expressServer.listen(serverPort, ()=>{
    console.log('本地文件服务器已启动，端口：', serverPort)
  })
}

const closeLocalServer = ()=>{
  if(server){
    server.close()
  }
}

const   FILE_TYPE_CONTENT_TYPE = {
  "0": "image/",
  "1": "video/",
  "2": "application/octet-stream"
}

expressServer.get('/file', async(req, res)=>{
  // console.log('expressServer get /file', req.query)
  let {partType,fileType,fileId,showCover,forceGet} = req.query
  if(!partType || !fileId){
    res.send('请求参数错误')
    return
  }
  showCover = showCover==undefined ? false : Boolean(showCover)
  const localPath = await getLocalFilePath(partType, showCover, fileId)
  if(!fs.existsSync(localPath) || forceGet=='true'){
    if(forceGet=='true' && partType=='avatar'){
      await downloadFile(fileId,true,localPath+cover_image_suffix,partType) // 获取头像缩略图
    }
    await downloadFile(fileId,showCover,localPath,partType) // 获取头型原图
  }
  // console.log('expressServer', fileId, localPath)
  const fileSuffix = localPath.substring(localPath.lastIndexOf('.')+1)
  let contentType = FILE_TYPE_CONTENT_TYPE[fileType] + fileSuffix
  res.setHeader('Access-Control-Allow-Origin', '*')
  res.setHeader('Content-Type', contentType)
  if(showCover || fileType!='1'){
    fs.createReadStream(localPath).pipe(res)
    return
  }
  let stat = fs.statSync(localPath)
  let fileSize = stat.size
  let range = req.headers.range
  if(range){
    let parts = range.replace(/bytes=/, "").split("-")
    let start = parseInt(parts[0], 10)
    let end = parts[1] ? parseInt(parts[1], 10) : start + 999999
    end = end>fileSize-1 ? fileSize-1 : end
    let chunksize = (end-start)+1
    let stream = fs.createReadStream(localPath, {start, end})
    let head = {
      'Content-Range': `bytes ${start}-${end}/${fileSize}`,
      'Accept-Ranges': 'bytes',
      'Content-Length': chunksize,
      'Content-Type': 'video/mp4',
    }
    res.writeHead(206, head)
    stream.pipe(res)
  }else{
    let head = {
      'Content-Length': fileSize,
      'Content-Type': contentType,
    }
    res.writeHead(200, head)
    fs.createReadStream(localPath).pipe(res)
  }
  return
})

const downloadFile = (fileId,showCover,savePath,partType)=>{
  showCover = showCover + ""
  let url = `${getDomain()}/api/chat/downloadFile`
  const token = store.getUserData('token')
  return new Promise(async (resolve, reject)=>{
    const config = {
      responseType: 'stream',
      headers: {
        'Content-Type': 'multipart/form-data',
        'token': token
      }
    }
    let response = await axios.post(url,{
      fileId,
      showCover,
    },config)
    // console.log(response.headers)
    const folder = savePath.substring(0,savePath.lastIndexOf('/'))
    mkdirs(folder)
    const stream = fs.createWriteStream(savePath)
    // console.log('downloadFile: ', response.headers['content-type'])
    if(response.headers['content-type']!='application/json'){
      response.data.pipe(stream)
    }else{
      let resourcesPath = getResouecesPath()
      if(partType=='avatar'){
        fs.createReadStream(resourcesPath + '/assets/user.jpg').pipe(stream)
      }else{
        fs.createReadStream(resourcesPath + '/assets/404.png').pipe(stream)
      }
    }
    stream.on('finish',()=>{
      stream.close()
      resolve()
    })
  })
}

const createCover = (filePath)=>{
  return new Promise(async (resolve, reject)=>{
    let ffmpegPath = getFFmegPath()
    let avatarPath = await getLocalFilePath('avatar', false, store.getUserId() + '_temp')
    let command = `${ffmpegPath} -i "${filePath}" "${avatarPath}" -y` // 图片压缩
    await execCommand(command)

    let coverPath = await getLocalFilePath('avatar', false, store.getUserId()+'_temp_cover')
    command = `${getFFmegPath()} -i "${avatarPath}" -y -vframes 1 -vf "scale='if(gt(iw,ih),170,-1)':'if(gt(iw,ih),-1,170)'" "${coverPath}"`
    await execCommand(command)
    resolve({
      avatarStream: fs.readFileSync(avatarPath),
      coverStream: fs.readFileSync(coverPath),
    })
  })
}

const saveAs = async ({partType, fileId})=>{
  let fileName=""
  if(partType=='avatar'){
    fileName = fileId + image_suffix
  }else if(partType=='chat'){
    let messageInfo = await selectByMessageId(fileId)
    fileName = messageInfo.fileName
  }
  const localPath = await getLocalFilePath(partType, false, fileId)
  const options = {
    title: '保存文件',
    defaultPath: fileName,
  }
  let result = await dialog.showSaveDialog(options)
  if(result.canceled || result.filePath==""){
    return
  }
  const filePath = result.filePath
  fs.copyFileSync(localPath, filePath)
}

export {
  saveFile2Local,
  startLocalServer,
  closeLocalServer,
  createCover,
  saveAs,
}
