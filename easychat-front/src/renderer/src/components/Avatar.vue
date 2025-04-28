<template>
  <div>
    <AvatarBase :userId="userId" :width="width" :borderRadius="borderRadius" :showDetail="false"
      v-if="userId == 'Urobot'">
    </AvatarBase>
    <el-popover v-else :width="280" placement="right-start" :show-arrow="false" trigger="click" transition="none"
      :hide-after="0" @show="getContactInfo" ref="popoverRef">
      <template #reference>
        <!-- 在父组件中userId传的是contactId，所以userId既是userId也可能是groupId -->
        <AvatarBase :userId="userId" :width="width" :borderRadius="borderRadius" :showDetail="false">
        </AvatarBase>
      </template>
      <template #default>
        <div class="popover-user-panel">
          <UserBaseInfo :userInfo="userInfo"></UserBaseInfo>
          <div class="op-btn" v-if="userId !== userInfoStore.getInfo().userId">
            <template v-if="userId && userId.startsWith('U')">
              <el-button v-if="userInfo.contactStatus == 1" type="primary" @click="sendMessage">发送消息</el-button>
              <el-button v-else type="primary" @click="addContact">加为好友</el-button>
            </template>
            <template v-else>
              <el-button type="primary" @click="sendMessage">发送消息</el-button>
            </template>
          </div>
        </div>
      </template>
    </el-popover>
    <SearchAdd ref="searchAddRef"></SearchAdd>
  </div>
</template>

<script setup>
import SearchAdd from '@/views/contact/SearchAdd.vue';
import { ref, reactive, getCurrentInstance, nextTick, computed } from 'vue';
const { proxy } = getCurrentInstance();
import AvatarBase from './AvatarBase.vue';

import { useRoute, useRouter } from "vue-router"
const route = useRoute()
const router = useRouter()

import UserBaseInfo from './UserBaseInfo.vue';
import { useUserInfoStore } from '@/stores/UserInfoStore';
const userInfoStore = useUserInfoStore();

const props = defineProps({
  userId: {
    type: String
  },
  width: {
    type: Number,
    default: 40
  },
  borderRadius: {
    type: Number,
    default: 0
  }
})

const userInfo = ref({})
const getContactInfo = async () => {
  userInfo.value.userId = props.userId
  if (userInfoStore.getInfo().userId == props.userId) {
    userInfo.value = userInfoStore.getInfo()
  } else {
    let result = {}
    if (props.userId && props.userId.startsWith('U')) {
      result = await proxy.Request({
        url: proxy.Api.getContactInfo,
        params: {
          contactId: props.userId
        },
        showLoading: false,
      })
    } else if (props.userId && props.userId.startsWith('G')) {
      result = await proxy.Request({
        url: proxy.Api.getGroupInfo,
        params: {
          groupId: props.userId
        },
        showLoading: false,
      })
    }
    if (!result) {
      return;
    }
    userInfo.value = Object.assign({}, result.data)
  }
}

const popoverRef = ref()
const emit = defineEmits(['closeDrawer'])
const sendMessage = () => {
  popoverRef.value.hide()
  emit('closeDrawer')
  router.push({
    path: '/chat',
    query: {
      chatId: props.userId,
      timestamp: new Date().getTime()
    }
  })
}

const searchAddRef = ref()
const addContact = () => {
  popoverRef.value.hide()
  searchAddRef.value.show({
    contactId: props.userId,
    contactType: 'USER',
  })
}

</script>

<style lang="scss" scoped>
.op-btn {
  text-align: center;
  border-top: 1px solid #eaeaea;
  padding-top: 10px;
}
</style>