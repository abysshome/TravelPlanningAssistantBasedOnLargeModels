<template>
  <div v-if="currentPage !== 'main'">
    <div class="content-area">
      <p class="welcome-text">TourGuide</p>
      <hr class="divider" />
      <p class="additional-text">您的私人旅程规划小助手</p>
      <div class="button-container">
        <el-button @click="dialogFormVisible = true" class="login-button">
          <i class="el-icon-user"></i> 登录
        </el-button>
      </div>
    </div>

    <!-- 左下角的背景图 -->
    <div class="left-bottom-image"></div>

    <!-- 登录对话框 -->
    <el-dialog v-model="dialogFormVisible" title="" width="550" class="login-dialog">
      <div class="dialog-title">登录</div>
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
          <el-button @click="login" class="footer-button">登录</el-button>
          <el-button @click="signup" class="regist-button">注册</el-button>
        </div>
      </template>
    </el-dialog>
  </div>

  <div v-if="isLogged && currentPage === 'main'">
    <SideBar :username1="username" :password1="password" />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import SideBar from './SideBar.vue';

const dialogFormVisible = ref(false);
const formLabelWidth = '140px';
const username = ref('');
const password = ref('');
const isLogged = ref(false);
const data = ref({});
const currentPage = ref('');

function login() {
  $.get("http://localhost:7778/login?username=" + username.value + "&password=" + password.value, function(response) {
    if (response.code == "1") {
      dialogFormVisible.value = false;
      isLogged.value = true;
      data.value = response.data;
      currentPage.value = "main";
    } else {
      alert("用户名或密码错误");
    }
  });
}

// 注册函数
function signup() {
  $.post('http://localhost:7778/register?username=' + username.value + '&password=' + password.value + "&phone=null", function(response) {
    if (response.code == "1") {
      alert("注册成功");
    } else {
      alert("注册失败");
    }
  });
}
</script>

<style>
.left-bottom-image {
  position: absolute;
  top: 4vh;
  left: 1vw;
  width: 60vw;
  height: 96vh;
  background: url('/root/kqgis/src/photo/图1.jpg') no-repeat center center;
  background-size: cover;
  z-index: -1; 
}

.content-area {
  position: absolute;
  top: 50%;
  right: 12vw;
  transform: translateY(-50%);
  color: rgb(0, 0, 0);
  width: 300px;
}

.welcome-text {
  font-size: 50px;
}

.divider {
  border: 1px solid rgb(0, 0, 0);
  margin: 10px 0;
}

.additional-text {
  font-size: 27px;
  margin-top: 10px;
}

.button-container {
  margin-top: 40px;
}

.login-button {
  background-color: rgba(128, 128, 128, 0.8);
  color: white;
  border: none;
  padding: 30px 80px;
  font-size: 36px;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  transition: background-color 0.3s;
}

.login-button:hover {
  background-color: rgba(100, 100, 100, 0.9);
  transform: scale(1.05);
  color: #B0B0B0;
}

.login-dialog {
  background-color: #f9f9f9;
  border-radius: 8px;
}

.dialog-title {
  text-align: center;
  font-size: 24px;
  margin-bottom: 20px;
}

.login-form {
  padding: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: center; /* 将按钮居中 */
  gap: 20px; /* 按钮之间的间距 */
  padding: 10px 20px;
}

.footer-button {
  background-color: rgba(128, 128, 128, 0.8); /* 按钮背景颜色 */
  color: white; /* 字体颜色 */
  border: none;
  border-radius: 5px;
  padding: 10px 20px;
  transition: background-color 0.3s, color 0.3s; /* 背景颜色和字体颜色过渡效果 */
}

.footer-button:hover {
  background-color: rgba(100, 100, 100, 0.9); /* 悬停时的背景颜色 */
  color: #B0B0B0; /* 悬停时的字体颜色 */
}

.regist-button {

  border-radius: 5px;
  padding: 10px 20px;
  transition: background-color 0.3s, color 0.3s; /* 背景颜色和字体颜色过渡效果 */
}

/* .regist-button:hover {

} */
</style>
