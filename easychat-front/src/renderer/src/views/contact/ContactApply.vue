<template>
  <ContentPanel :showTopBorder="true" :infinite-scroll-immediate="false" v-infinite-scroll="loadApply">
    <div>
      <div class="apply-item" v-for="item in applyList" :key="item.applyId">
        <div :class="['contact-type', item.contactType == 0 ? 'user-contact' : '']">
          {{ item.contactType == 0 ? '好友' : '群聊' }}
        </div>
        <Avatar :width="50" :userId="item.applyUserId"></Avatar>
        <div class="contact-info">
          <div class="nick-name">{{ item.contactName }}</div>
          <div class="apply-info">{{ item.applyInfo }}</div>
        </div>
        <div class="op-btn">
          <div v-if="item.status === 0">
            <el-dropdown placement="bottom-end" trigger="click">
              <span class="el-dropdown-link">
                <el-button type="primary" size="small">接受</el-button>
              </span>
              <template #dropdown>
                <el-dropdown-item @click="dealWithApply(item.applyId, item.contactType, 1)">同意</el-dropdown-item>
                <el-dropdown-item @click="dealWithApply(item.applyId, item.contactType, 2)">拒绝</el-dropdown-item>
                <el-dropdown-item @click="dealWithApply(item.applyId, item.contactType, 3)">拉黑</el-dropdown-item>
              </template>
            </el-dropdown>
          </div>
          <div v-else class="result-name">{{ item.statusName }}</div>
        </div>
      </div>
    </div>
    <div v-if="applyList.length == 0" class="no-data">暂无申请</div>
  </ContentPanel>
</template>

<script setup>

import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import { useContactStateStore } from "../../stores/ContactStateStore";
const contactStateStore = useContactStateStore()

import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()

const applyList = ref([])
let pageNo = 0
let pageTotal = 1
const loadApply = async () => {
  pageNo++
  if (pageNo > pageTotal) {
    return;
  }
  let result = await proxy.Request({
    url: proxy.Api.loadApply,
    params: {
    }
  })
  if (!result) {
    return;
  }
  pageTotal = result.data.pageTotal
  if (result.data.pageNo == 1) {
    applyList.value = []
  }
  applyList.value = applyList.value.concat(result.data.list);
  pageNo = result.data.pageNo
}
loadApply()

const dealWithApply = (applyId, contactType, status) => {
  contactStateStore.setContactReload(null);
  proxy.Confirm({
    message: '确定要执行操作吗？',
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.dealWithApply,
        params: {
          applyId,
          status
        }
      })
      if (!result) {
        return;
      }
      pageNo = 0;
      loadApply()
      if (contactType == 0 && status == 1) {
        contactStateStore.setContactReload('USER')
      }
      else if (contactType == 1 && status == 1) {
        contactStateStore.setContactReload('GROUP')
      }
    }
  })
};

//TODO,监听新朋友数量改变




</script>

<style lang="scss" scoped>
.apply-item {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #dddddd;
  padding: 10px 0px;

  .contact-type {
    display: flex;
    justify-content: center;
    align-items: center;
    writing-mode: vertical-rl;
    background: #2cb6fe;
    color: #ffffff;
    border-radius: 5px 0px 0px 5px;
    height: 50px;

    &.user-contact {
      background: #08bf61;
    }
  }

  .contact-info {
    width: 260px;
    margin-left: 10px;

    .nick-name {
      color: #000000;
    }

    .apply-info {
      color: #999999;
      font-size: 12px;
      margin-top: 5px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .op-btn {
    width: 50px;
    text-align: center;
    display: flex;
    align-items: center;
    justify-content: center;

    .result-name {
      color: #999999;
      font-size: 12px;
    }
  }
}
</style>
