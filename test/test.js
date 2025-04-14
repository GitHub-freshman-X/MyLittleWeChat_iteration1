const toCamelCase = (str)=>{
  return str.replace(/_([a-z])/g, function(match, p1){
    return String.fromCharCode(p1.charCodeAt(0) - 32);
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

const obj = convertDbObject2BizObj({
  user_id: "1001",
  user_name: "张三"
})

const obj2 = {
  userId: "1001",
  userName: "张三"
}

console.log(obj) // userId