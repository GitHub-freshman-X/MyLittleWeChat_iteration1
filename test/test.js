const getPageOffset = (pageNo=1, totalCount)=>{
  const pageSize = 2;
  const pageTotal = totalCount % pageSize==0?totalCount/pageSize : Number.parseInt(totalCount/pageSize)+1
  pageNo = pageNo<=1 ? 1 : pageNo
  pageNo = pageNo>=pageTotal ? pageTotal : pageNo
  return{
    pageTotal,
    offset: (pageNo-1)*pageSize,
    limit: pageSize
  }
}

console.log(getPageOffset(2, 3))
