<template>
  <!-- 登陆对话框 -->
  <el-dialog v-model="dialogFormVisible" title="登录" width="550" class="login-dialog">
    <el-form :model="form" class="login-form">
      <el-form-item label="用户名" :label-width="formLabelWidth">
        <el-input v-model="username" autocomplete="off" />
      </el-form-item>
      <el-form-item label="密码" :label-width="formLabelWidth">
        <el-input v-model="password" type="password" autocomplete="off" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="footer-button" @click="login" type="primary"> 登录 </el-button>
        <el-button class="footer-button" @click="signup"> 注册 </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 菜单 -->
  <el-menu 
    :default-active="activeIndex" 
    class="el-menu-demo fixed-menu" 
    mode="vertical" 
    @select="handleSelect"
  >
    <!-- 图片部分 -->
    <div class="menu-image-container">
      <img src="/src/photo/KQGIS.jpg" alt="Menu Image" class="menu-image" />
    </div>

    <el-menu-item index="1" v-if="!isLogged" @click="dialogFormVisible=true" style="font-size: 16px;">
      登陆
    </el-menu-item>
    <el-menu-item index="2" v-if="isLogged" @click="goToProfile" style="font-size: 16px;">
      个人信息
    </el-menu-item>
    <el-menu-item index="3" v-if="isLogged" @click="logout" style="font-size: 16px;">退出登录</el-menu-item>
    <el-menu-item index="4" v-if="isLogged" @click="goToInterests" style="font-size: 16px;">
      爱好
    </el-menu-item>
    <el-menu-item index="6" v-if="isLogged" @click="goToMainWeb" style="font-size: 16px;">对话</el-menu-item>
    <!-- <el-menu-item index="5">删除当前对话</el-menu-item>  -->
  </el-menu>

  <div v-if="isLogged">
    <ProfilePage v-if="currentPage === 'profile'" :data="data" />
    <Interest v-if="currentPage === 'interests'" :data="data" />
    <MainWeb v-if="currentPage === 'mainweb'" :currentComponent="currentComponent" :dias="dias" :isLoading="isLoading" :data="data"/>
    <!-- <MainWeb v-if="currentPage === 'mainweb'" :data="data" /> -->
  </div>
  <!-- <div>
    <MapPage class="map-component"/>
  </div> -->
</template>

<script setup>
import { ref , defineProps , onMounted} from 'vue';
import ProfilePage from './ProfilePage.vue';
import MapPage from './map.vue';
import Interest from './Interest.vue';
import MainWeb from './MainWeb.vue';
const data = ref({});
const dialogFormVisible = ref(false);
const activeIndex = ref('0');
const formLabelWidth = '140px';
// const username = ref('');
// const password = ref('');
const username=ref("");
const password=ref("");
const isLogged = ref(false);
const currentPage = ref('');
// const emit = defineEmits(['updateInterests', 'navigate']);
const props = defineProps({
  username1:String,
  password1:String
});
let dias = ref([
  { user: 0, content: "欢迎来到TourGuide!我是您的私人旅程规划小助手，请您简要说明您本次的旅行需求，包括时间、地点和预期途径点个数，例如“我早上八点到北京南站，计划在北京游玩一天，晚上六点左右到达后海，途径3个景点即可。”让我们开始旅行吧!" }
]);
// 登录函数
onMounted(() => {
  // alert(props.username1);
  // username.value=66;
  // password.value=1;
  // alert(props.username1);
  // alert(props.password1);
  username.value=props.username1;
  password.value=props.password1;
  login();
  // goToProfile();
  goToInterests();
    });
function login() {
  $.get("http://localhost:7778/login?username="+username.value+"&password="+password.value, function(response) {
    if (response.code == "1") {
      dialogFormVisible.value = false;
      isLogged.value = true;
      data.value = response.data;
      // alert("更新成功"+response.data.interestedPlaces+response.data.interestedWays+response.data.travelCompanion);
    } else {
      alert("用户名或密码错误");
    }
  });
}

// 注册函数
function signup() {
  $.post('http://localhost:7778/register?username='+username.value+'&password='+password.value+"&phone=null", function(response) {
    if (response.code == "1") {
      alert("注册成功");
    } else {
      alert("注册失败");
    }
  });
}

function handleSelect(key, keyPath) {
  console.log(key, keyPath);
}

// 退出登录函数
function logout() {
  isLogged.value = false;
  currentPage.value = ''; // 重置当前页面
}

// 跳转到个人信息页面
function goToProfile() {
  currentPage.value = 'profile';
}

// 跳转到爱好页面
function goToInterests() {
  currentPage.value = 'interests';
}

// 跳转到主界面
function goToMainWeb() {
  currentPage.value = 'mainweb';
}
</script>

<style>
.fixed-menu {
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  width: 15vw;
  background-color: #fff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.menu-image-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10px;
}

.menu-image {
  width: 100px;
  height: auto;
}

.el-menu-item {
  margin: 10px 0; /* 调整这里的值来改变间距 */
}

.map-component {
  flex: 1;
  height: 100vh;
  left:1000px;
}

.login-dialog {
  background-color: #f9f9f9; /* 对话框背景颜色 */
  border-radius: 8px; /* 圆角 */
}

.login-form {
  padding: 20px; /* 内边距 */
}

.dialog-footer {
  display: flex;
  justify-content: space-between; /* 按钮左右对齐 */
  padding: 10px 20px; /* 边距 */
}

.footer-button {
  background-color: #4CAF50; /* 按钮背景颜色 */
  color: white; /* 字体颜色 */
  border: none; /* 去掉边框 */
  border-radius: 5px; /* 圆角 */
  padding: 10px 20px; /* 内边距 */
  transition: background-color 0.3s; /* 背景颜色过渡效果 */
}

.footer-button:hover {
  background-color: #45a049; /* 悬停时的背景颜色 */
}
</style>
