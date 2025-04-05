// 导入 axios 库，用于发起 HTTP 请求
import axios from 'axios';
// 导入 Element Plus 的加载提示组件
import { ElLoading } from 'element-plus';
// 导入自定义的消息提示工具
import Message from '../utils/Message';
// 导入自定义的 API 配置工具
import Api from '../utils/Api';

// 定义表单数据的 Content-Type
const contentTypeForm = 'application/x-www-form-urlencoded;charset=UTF-8';
// 定义 JSON 数据的 Content-Type
const contentTypeJson = 'application/json';
// 定义响应数据类型为 JSON
const responseTypeJson = 'json';

// 定义一个变量用于存储加载提示实例
let loading = null;

// 创建一个 axios 实例，设置请求的基本配置
const instance = axios.create({
  // 允许跨域请求携带凭证
  withCredentials: true,
  // 根据环境变量设置请求的基础 URL
  baseURL: (import.meta.env.PROD ? Api.prodDomain : '') + '/api',
  // 设置请求超时时间为 10 秒
  timeout: 10 * 1000,
});

// 请求前拦截器，在请求发送前进行一些处理
instance.interceptors.request.use(
  // 请求成功时的处理函数
  (config) => {
    // 如果配置中设置了显示加载提示
    if (config.showLoading) {
      // 显示加载提示
      loading = ElLoading.service({
        // 锁定屏幕，防止用户操作
        lock: true,
        // 加载提示的文本
        text: '加载中……',
        // 加载提示的背景颜色
        background: 'rgba(0, 0, 0, 0.7)',
      });
    }
    // 返回配置对象，继续发送请求
    return config;
  },
  // 请求失败时的处理函数
  (error) => {
    // 如果配置中设置了显示加载提示且加载提示实例存在
    if (config.showLoading && loading) {
      // 关闭加载提示
      loading.close();
    }
    // 显示错误消息
    Message.error("请求发送失败");
    // 返回一个被拒绝的 Promise，传递错误信息
    return Promise.reject("请求发送失败");
  }
);

// 请求后拦截器，在响应返回后进行一些处理
instance.interceptors.response.use(
  // 响应成功时的处理函数
  (response) => {
    // 从响应配置中解构出需要的参数
    const { showLoading, errorCallback, showError = true, responseType } = response.config;
    // 如果配置中设置了显示加载提示且加载提示实例存在
    if (showLoading && loading) {
      // 关闭加载提示
      loading.close();
    }

    // 获取响应数据
    const responseData = response.data;
    // 如果响应类型是 arraybuffer 或 blob，直接返回响应数据
    if (responseType === "arraybuffer" || responseType === "blob") {
      return responseData;
    }

    // 如果响应状态码为 200，表示请求成功
    if (responseData.code == 200) {
      // 返回响应数据
      return responseData;
    } 
    // 如果响应状态码为 901，表示登录超时
    else if (responseData.code == 901) {
      // 延迟 2 秒后发送重新登录的消息
      setTimeout(() => {
        window.ipcRenderer.send('reLogin');
      }, 2000);
      // 返回一个被拒绝的 Promise，传递错误信息
      return Promise.reject({ showError: true, msg: "登录超时" });
    } 
    // 其他错误情况
    else {
      // 如果配置中设置了错误回调函数
      if (errorCallback) {
        // 调用错误回调函数，传递响应数据
        errorCallback(responseData);
      }
      // 返回一个被拒绝的 Promise，传递错误信息
      return Promise.reject({ showError: showError, msg: responseData.info });
    }
  },
  // 响应失败时的处理函数
  (error) => {
    // 如果配置中设置了显示加载提示且加载提示实例存在
    if (error.config.showLoading && loading) {
      // 关闭加载提示
      loading.close();
    }
    // 返回一个被拒绝的 Promise，传递错误信息
    return Promise.reject({ showError: true, msg: "网络异常" });
  }
);

/////定义一个通用的请求函数///////////
const request = (config) => {
  // 从配置对象中解构出需要的参数
  const { url, params, dataType, showLoading = true, responseType = responseTypeJson, showError = true } = config;
  // 初始化 Content-Type 为表单数据类型
  let contentType = contentTypeForm;
  // 创建一个 FormData 对象，用于存储请求参数
  let formData = new FormData(); 
  // 遍历请求参数对象
  for (let key in params) {
    // 将参数添加到 FormData 对象中，如果参数值为 undefined，则添加空字符串
    formData.append(key, params[key] == undefined ? "" : params[key]);
  }
  // 如果配置中设置了数据类型为 json
  if (dataType != null && dataType == 'json') {
    // 将 Content-Type 设置为 JSON 数据类型
    contentType = contentTypeJson;
  }
  // 从本地存储中获取 token
  const token = localStorage.getItem('token');
  // 定义请求头
  let headers = {
    // 设置 Content-Type
    'Content-Type': contentType,
    // 表明这是一个 AJAX 请求
    'X-Requested-With': 'XMLHttpRequest',
    // 设置请求头中的 token
    'token': token
  };
  // 发起 POST 请求
  return instance.post(url, formData, {
    // 设置请求头
    headers: headers,
    // 设置是否显示加载提示
    showLoading: showLoading,
    // 设置错误回调函数
    errorCallback: config.errorCallback,
    // 设置是否显示错误消息
    showError: showError,
    // 设置响应数据类型
    responseType: responseType
  }).catch(error => {
    // 如果配置中设置了显示错误消息
    if (error.showError) {
      // 显示错误消息
      Message.error(error.msg);
    }
    // 返回 null
    return null;
  });
};

// 导出通用请求函数
export default request;