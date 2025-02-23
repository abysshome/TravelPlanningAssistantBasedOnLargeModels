<script setup>
import "./iconfont.js";
import UserInp from './components/elements/UserInp.vue';
import Dia from "./components/elements/Dia.vue";
import Model from './components/elements/Model.vue';
import SideBar from './components/elements/SideBar.vue';
import MainMobile from './components/Mobile/MainMobile.vue';
import CoverPage from './components/elements/CoverPage.vue';
import MainWeb from './components/elements/MainWeb.vue';
import { ref } from "vue";
import { useWindowSize } from '@vueuse/core';
import Interest4 from "./components/Mobile/Interest4.vue";
import ProfilePage from './components/elements/ProfilePage.vue';
import Interest from './components/elements/Interest.vue';
import 'leaflet/dist/leaflet.css';

const currentComponent = ref(MainWeb); // 默认组件设置为 MainWeb
const username = ref("");
const password = ref("");
const isLoggedIn = ref(false);

const { width } = useWindowSize();
const isMobile = ref(false);
const isRegister = ref(false);
isMobile.value = width.value < 768;

let isLoading = ref(false);
let dias = ref([
  { user: 0, content: "欢迎来到TourGuide!我是您的私人旅程规划小助手，请您简要说明您本次的旅行需求，包括时间、地点和预期途径点个数，例如“我早上八点到北京南站，计划在北京游玩一天，晚上六点左右到达后海，途径3个景点即可。”让我们开始旅行吧!" }
]);
let data = {};

function login(){
  // alert("http://localhost:7778/login?username="+username.value+"&password="+password.value);
  $.get("http://localhost:7778/login?username="+username.value+"&password="+password.value, function(response) {
    if (response.code == "1"){
      isLoggedIn.value = true;
      data = response.data;
    } else {
      alert("用户名或密码错误");
    }
  });
  // alert(username.value+"&password="+password.value);
}

function signup() {
  $.post('http://localhost:7778/register?username='+username.value+'&password='+password.value+"&phone=null", function(response) {
    if (response.code == "1"){
      isRegister.value=true;
      alert("注册成功");
    } else {
      alert("注册失败");
    }
  });
  // alert(username.value+'&password='+password.value+"注册成功");
}


</script>

<template>
  <div class="common-layout">
    <CoverPage />
    <!-- <SideBar /> -->
    
    <!-- <el-col :span="24" :xs="24" :sm="16">
      <component 
        :is="currentComponent" 
        :dias="dias" 
        :isLoading="isLoading" 
      />
    </el-col> -->
  </div>
</template>

<style scoped>
.icon {
  width: 1.5em;
  height: 1.5em;
  vertical-align: -0.15em;
  fill: currentColor;
  overflow: hidden;
  margin-right: 10px;
}
.container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 80vh;
}
.main {
  flex: 50;
  overflow-y: auto;
  min-width: 680px;
}
.foot {
  margin: 20px;
  width: 100%;
}
.header {
  flex: 1;
  margin: 20px;
  width: 100%;
}
.login-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
}
.el-button {
  width: 120px;
}
.el-button.is-text:not(.is-disabled).is-has-bg.in {
  background-color: #3f7edc;
}
.el-button.is-text:not(.is-disabled).is-has-bg.three-way {
  background-color: #ffffff;
  margin: 2px;
  width: 200px;
  border-width: 2px;
  border-color: #f5f7fa;
}
.tx {
  margin: 10px 0;
}
input {
  margin: 10px 0;
  padding: 10px;
  font-size: 16px;
  width: 200px;
}
.login-image {
  width: 150px;
  margin: 10px 0;
}
</style>
