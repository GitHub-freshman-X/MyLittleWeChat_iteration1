<template>
    <div class="main">
      <div class="left-sider">
        <div></div>
        <div class="menu-list">
          <template v-for="item in menuList">
            <div 
              :class="['tab-item iconfont', item.icon,item.path=currentMenu.path ? 'active' : '']" 
              v-if="item.position === 'top'"
              @click="changeMenu(item)"
              >
              <template v-if="item.name === 'chat'"></template>
            </div>
          </template>
        </div>
        <div class="menu-list menu-bottom">
          <template v-for="item in menuList">
            <div 
              :class="['tab-item iconfont', item.icon,item.path=currentMenu.path ? 'active' : '']" 
              v-if="item.position === 'bottom'"
              @click="changeMenu(item)"
            ></div>
          </template>
        </div>
      </div>
      <div class="right-container">
        <router-view v-slot="{ Component }">
          <keep-alive include="chat">
            <component :is="Component" ref="componentRef"></component>
          </keep-alive>
        </router-view>
      </div>
    </div>
  </template>
  
  <script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
const { proxy } = getCurrentInstance()

const menuList = ref([
  {
    name: 'chat',
    icon: 'icon-chat',
    path: '/chat',
    countKey: 'chatCount',
    position: 'top'
  },
  {
    name: 'contact',
    icon: 'icon-user',
    path: '/contact',
    countKey: 'contactApplyCount',
    position: 'top'
  },
  {
    name: 'mysetting',
    icon: 'icon-more2',
    path: '/setting',
    position: 'bottom'
  }
])

const currentMenu = ref(menuList.value[0])
const changeMenu = (item) => {
  currentMenu.value = item
  router.push(item.path)
}
</script>
  
  <style lang="scss" scoped>
  .main {
    background: #ddd;
    display: flex;
    border-radius: 0px 3px 3px 0px;
    overflow: hidden;
    .left-sider {
      width: 55px;
      background: #2e2e2e;
      text-align: center;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding-top: 35px;
      border: 1px solid #2e2e2e;
      border-right: none;
      padding-bottom: 10px;
    //联系人框架删除这一条   height: calc(100vh);
      .menu-list {
        width: 100%;
        flex: 1;
        .tab-item {
          color: #d3d3d3;
          font-size: 20px;
          height: 40px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-top: 10px;
          cursor: pointer;
          font-size: 22px;
          position: relative;
        }
        .active {
          /* 此处代码未完整，应该在登录7里面写的 */
        }
      }
    }
  }
  </style>