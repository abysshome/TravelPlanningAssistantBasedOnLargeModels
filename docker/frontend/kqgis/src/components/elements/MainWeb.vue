<template>
  <el-container class="main-web">
    <div class="fixed-container">
      <div class="header">
        <NavBar @navigate="navigate" @newDia="newDia" @updateDia="updateDia" @updateSelectDiaID="updateSelectDiaID"  :currentPage="currentPage" :data="diaArray" :firstId="firstId" :userID="userID" :delId="delId"/>
      </div>
      <div class="main">
        <div v-for="dia in diaData" :key="dia.id">
          <Dia :msg="dia" />
        </div>
      </div>
      <div class="foot">
        <UserInp v-loading.fullscreen.lock="fullscreenLoading" @delDia="delDia" @chat="chat" @updateMessage="updateLeafletMessage" :isLoading="isLoading" :userID="userID" :diaID="selectDiaID"/>
      </div> 
      <div class="right-image">
        <leaflet-component :message="leafletMessage"/> <!-- 引入 Leaflet 组件 -->
      </div> 
    </div>
  </el-container>
  <div v-if="fullscreenLoading" class="loading-overlay">
    <div class="loading-spinner"></div>
  </div>
</template>



<script setup>
import { ref, defineProps, defineEmits, onMounted } from 'vue';
import Model from './Model.vue';
import Dia from './Dia.vue';
import UserInp from './UserInp.vue';
import NavBar from './NavBar.vue';
import LeafletComponent from './Leaflet.vue'; // 引入 Leaflet 组件

const currentPage = ref('mainweb');
const props = defineProps({
  data: {}
});
const fullscreenLoading = ref(false);
let isLoading = ref(false);
const userID = props.data.id;
let selectDiaID = ref('');
let diaData = ref([]);
let diaArray = ref([]);
const leafletMessage = ref(''); 
const firstId = ref(null);
let delId = ref(0);

function foo() {
  $.get("http://localhost:7778/dialog?id=" + userID, function(response) {
    diaArray.value = response.data;
    const exampleDialog = diaArray.value.find(dia => dia.name === "示例对话");
    if (!exampleDialog) {
      $.post("http://localhost:7778/dialog?userId=" + userID + "&name=" + "示例对话", function(response) {
        const newDialogId = response.data.id;
        update();
        firstId.value=response.data.id;
        // alert(firstId);
        console.log('New dialog created with ID: ' + newDialogId);
      }).done(function() {
        console.log('New dialog created.');
      }).fail(function() {
        console.log('Failed to create new dialog.');
      });
      console.log('不存在 name 为 "示例对话" 的项');
    } else {
      const existingDialogId = exampleDialog.id;
      firstId.value=exampleDialog.id;
      // alert(firstId.value);
      console.log('存在 name 为 "示例对话" 的项，ID: ' + existingDialogId);
    }
  }).done(function() {
    console.log('Dialog list retrieved.');
  }).fail(function() {
    console.log('Failed to get dialog list.');
  });
}
foo();


function update() {
  // alert(props.data.id);
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
  delId.value+=1;
}

function chat(inp) {
  // fullscreenLoading.value = true;
  const newInput = { input: inp };

  // Add the new input object to diaData
  diaData.value.push(newInput);
  var settings = {
  url: "http://localhost:7778/spark/askStream?input=" + inp + "&dialogId=" + selectDiaID.value + "&userId=" + userID,
  method: "POST",
  timeout: 0,
  xhrFields: {
    onprogress: function(e) {
      var response = e.currentTarget.response;
      // 去除 response 中的所有空格、换行符、"event:update" 和 "data:"
      var cleanedResponse = response.replace(/[\s\n]|event:update|data:/g, '');
      diaData.value[diaData.value.length - 1].output = cleanedResponse;
    }
  }
};




var fullResponse = ""; // 用于存储完整的响应数据

$.ajax(settings).done(function() {
  // 在这里处理完整的响应数据
  $.get("http://localhost:7778/dialog/records?id=" + selectDiaID.value, function(response) {
    updateDia(response.data);
    // const lastMessage = response.data[response.data.length - 1].message; 
    // alert(lastMessage.value);
    fullscreenLoading.value = false;
  }).fail(function() {
    fullscreenLoading.value = false;
  });
}).fail(function() {
  fullscreenLoading.value = false;
});
}

function navigate(page) {
  currentPage.value = page;
}

function updateLeafletMessage(newMessage) {
  leafletMessage.value = newMessage;
}
</script>

<style scoped>
.main-web {
  position: fixed;
  left: 16.4vw;
  top: 0;
  bottom: 0;
  width: 40.5vw;
  padding-right: 30px;
  /* border-right: 1px solid #e0e0e0; */
  box-sizing: content-box;
}

.fixed-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.header {
  flex-shrink: 0;
  padding: 10px;
  border-bottom: 1px solid #e0e0e0;
}

.main {
  flex-grow: 1;
  overflow-y: auto;
  padding: 10px;
}

.foot {
  flex-shrink: 0;
  padding: 10px;
  border-top: 1px solid #e0e0e0;
  width: 40vw;
}

.loading-overlay {
  position: fixed;
  top: 25%;
  left: 25%;
  width: 50%;
  height: 50%;
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

.right-image {
  position: fixed;
  top: 0;
  right: 0px;
  width: 41vw;
  /* height: auto; */
  z-index: 1001;
}
</style>
