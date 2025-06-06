import { defineStore } from 'pinia'
export const useContactStateStore = defineStore('contactStateInfo', {
  state: () => {
    return {
      contactReload: null
    }
  },
  actions: {
    setContactReload(state){
      this.contactReload = state
    }
  }
})