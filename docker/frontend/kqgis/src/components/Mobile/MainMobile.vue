<template>
  <div class="main-mobile">
    <NavigationBar @navigate="navigate" @newDia="newDia" @updateDia="updateDia" @updateSelectDiaID="updateSelectDiaID" :currentPage="currentPage" :data="diaArray" />
    <div class="content" v-if="currentPage === 'navigation'">
      <div class="main">
        <div v-for="dia in diaData" :key="dia.id">
          <Dia :msg="dia" />
        </div>
      </div>
      <div v-if="!selectDiaID">请先新建对话</div>
      <div class="foot">
        <UserInp v-loading.fullscreen.lock="fullscreenLoading" @delDia="delDia" @chat="chat" :isLoading="isLoading" :userID="userID" :diaID="selectDiaID"/>
      </div>      
    </div>
    <ProfilePage 
      v-else-if="currentPage === 'profile'" 
      @logout="handleLogout" 
      @navigate="navigate"   
      :data="data"
    />
    <Interest 
      v-else-if="currentPage === 'interest'" 
      @navigate="navigate" 
      :data="data"
    />
    <Interest2 
      v-else-if="currentPage === 'interest2'" 
      @navigate="navigate" 
      :data="data"
    />
    <Interest3 
      v-else-if="currentPage === 'interest3'" 
      @navigate="navigate" 
      :data="data"
    />
  </div>
  <div v-if="fullscreenLoading" class="loading-overlay">
    <div class="loading-spinner"></div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import NavigationBar from './NavBar.vue';
import ProfilePage from './ProfilePage.vue';
import Interest from './Interest.vue';
import Interest2 from './Interest2.vue';
import Interest3 from './Interest3.vue';
import Interest4 from './Interest4.vue';
import Interest5 from './Interest5.vue';
import Interest6 from './Interest6.vue';
import UserInp from './UserInp.vue';
import Dia from './Dia.vue';

const currentPage = ref('navigation');
const interests = ref([]);
const interests2 = ref([]);
const interests3 = ref([]);
const props = defineProps({
  data: {}
});
const fullscreenLoading = ref(false);
let isLoading = ref(false);
const userID = props.data.id;
let selectDiaID = ref('');
let diaData = ref([]);
let diaArray = ref([]);

function update() {
  $.get("http://localhost:7778/dialog?id=" + userID, function(response) {
    diaArray.value = response.data;
  }).done(function() {
  }).fail(function() {
  });
}
update();

function newDia(diaName) {
  $.post("http://localhost:7778/dialog?userId=" + userID + "&name=" + diaName, function(response) {
    console.log(response);
  }).done(function() {
    update();
  }).fail(function() {
  });
}

function updateDia(data) {
  diaData.value = data;
}

function updateSelectDiaID(id) {
  selectDiaID.value = id;
}

function delDia() {
  var settings = {
    url: "http://localhost:7778/dialog?dialogId=" + selectDiaID.value,
    method: "DELETE",
    timeout: 0,
  };
  $.ajax(settings).done(function(response) {
    console.log(selectDiaID.value);
    update();
    selectDiaID.value = 0;
    updateDia([]);
  });
}

function chat(inp) {
  fullscreenLoading.value = true;
  var settings = {
    url: "http://localhost:7778/chatGPT?input=" + inp + "&dialogId=" + selectDiaID.value + "&userId=" + userID,
    method: "POST",
    timeout: 0,
  };
  $.ajax(settings).done(function(response) {
    $.get("http://localhost:7778/dialog/records?id=" + selectDiaID.value, function(response) {
      updateDia(response.data);
      fullscreenLoading.value = false;
    }).fail(function() {
      fullscreenLoading.value = false;
    });
  }).fail(function() {
    fullscreenLoading.value = false;
  });
}

function handleLogout() {
  currentPage.value = 'navigation';
}

function navigate(page) {
  currentPage.value = page;
}
</script>

<style scoped>
.main-mobile {
  display: flex;
  flex-direction: column;
  height: 100vh;
  position: relative;
}

.content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding-bottom: 60px; /* 给底部预留空间，避免内容被遮挡 */
}

.foot {
  position: fixed;
  bottom: 0;
  width: 100%;
}

/* .loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loading-spinner {
  border: 2px solid rgba(0, 0, 0, 0.1); 
  border-top: 2px solid rgb(0, 140, 255); 
  border-radius: 50%;
  width: 40px; 
  height: 40px; 
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
} */
</style>
