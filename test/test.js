
const { exec } = require('child_process')

const test = async()=>{
  const ffprobePath = "D:/CurriculumDesign/MyLittleWeChat/test1/easychat-front/assets/ffprobe.exe"
  const ffmpegPath = "D:/CurriculumDesign/MyLittleWeChat/test1/easychat-front/assets/ffmpeg.exe"
  const filePath = "C:/Users/hp/Videos/年纪团课/团课.mp4"
  const savePath = "C:/Users/hp/Videos/年纪团课/团课.mp4"

  let command = `${ffprobePath} -v error -select_streams v:0 -show_entries stream=codec_name ${filePath}`
  let result = await execCommand(command)
  result = result.replaceAll("\r\n", "")
  result = result.substring(result.indexOf('=')+1)
  let codec = result.substring(0,result.indexOf('['))

  if('hevc'==codec){
    command = `${ffmpegPath} -y -i "${filePath}" -c:v libx264 -crf 20 "${savePath}"`
    await execCommand(command)
  }

  // 生成缩略图
  const coverPath = savePath + '_cover.png'
  command = `${ffmpegPath} -i "${savePath}" -y -vframes 1 -vf "scale='if(gt(iw,ih),170,-1)':'if(gt(iw,ih),-1,170)'" "${coverPath}"`

  await execCommand(command)
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

test()