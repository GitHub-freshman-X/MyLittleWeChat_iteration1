import moment from 'moment';
const isEmpty = (str)=>{
  if(str==null || str=='' || str==undefined){
    return true;
  }
  return false;
}

const getAreaInfo = (data) => {
  if(isEmpty(data)) {
    return "-"
  }
  return data.replace(","," ")
}

const formatDate = (timestamp) => {
  const timestampTime = moment(timestamp);
  const days = Number.parseInt(moment().format("YYYYMMDD")) - Number.parseInt(timestampTime.format("YYYYMMDD"));
  if(days==0){
    return timestampTime.format("HH:mm")
  }else if(days==1){
    return "昨天"
  }else if(days>=2 && days<7){
    return timestampTime.format("dddd")
  }else {
    return timestampTime.format("YYYY/MM/DD")
  }
}

const size2Str = (limit)=>{
  var size = ""
  if(limit < 0.1*1024){
    size = limit.toFixed(2) + "B"
  }else if(limit < 1024*1024){
    size = (limit/1024).toFixed(2) + "KB"
  }else if(limit < 1024*1024*1024){
    size = (limit/(1024*1024)).toFixed(2) + "MB"
  }else{
    size = (limit/(1024*1024*1024)).toFixed(2) + "GB"
  }
  var sizeStr = size + "";
  var index = sizeStr.indexOf('.')
  var dou = sizeStr.substring(index+1, 2)
  if(dou=='00'){
    return sizeStr.substring(0,index) + sizeStr.substring(index+3, 2)
  }
  return size
}

export default{
  isEmpty,
  getAreaInfo,
  formatDate,
  size2Str,
}