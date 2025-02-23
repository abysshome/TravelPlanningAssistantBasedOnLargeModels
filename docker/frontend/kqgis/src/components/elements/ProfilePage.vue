<template>
  <div class="profile-page">
    <div class="content">
      <div class="info">
        <img :src="avatarUrl" alt="用户头像" class="avatar-image" />
        <el-form>
          <el-form-item label="昵称">
            <span>{{ userData.username }}</span>
          </el-form-item>
          <el-form-item label="性别">
            <el-select v-model="gender" placeholder="请选择性别" :disabled="!isEditing">
              <el-option label="男" value="男"></el-option>
              <el-option label="女" value="女"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="年龄">
            <el-input v-model="age" placeholder="请输入年龄" :disabled="!isEditing"></el-input>
          </el-form-item>
          <el-form-item label="家乡">
            <el-select v-model="hometown" placeholder="请选择家乡" :disabled="!isEditing">
              <el-option v-for="province in provinces" :key="province" :label="province" :value="province"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <div class="button-container">
          <el-button v-if="!isEditing" @click="startEditing" type="primary">编辑</el-button>
          <el-button v-if="isEditing" @click="save" type="success">保存</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineEmits, defineProps, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
// const emit = defineEmits(['navigate']);
// const userData = ref({
//   username: '示例用户'
// });
const props = defineProps({
  data: Object // 接收从父组件传递过来的 data
});
const userData = props.data; // 使用传递过来的 data 属性
const avatarUrl = ref('src/photo/KQGIS.jpg');
const gender = ref('');
const age = ref('');
const hometown = ref('');
const isEditing = ref(false);
let userdata = {};

const provinces = ref([
  "北京", "天津", "上海", "重庆", "河北", "山西", "辽宁", "吉林", "黑龙江", "江苏", 
  "浙江", "安徽", "福建", "江西", "山东", "河南", "湖北", "湖南", "广东", "海南", 
  "四川", "贵州", "云南", "陕西", "甘肃", "青海", "台湾", "内蒙古", "广西", 
  "西藏", "宁夏", "新疆", "香港", "澳门"
]);

function fetchPreferences() {
  $.get("http://localhost:7778/preferences?id=" + userData.id, function(response) {
    if (response.code === 1) {
      const data = response.data;
      gender.value = data.gender;
      age.value = data.age;
      hometown.value = data.hometown;
      console.log("偏好数据获取成功:", data);
    } else {
      ElMessage.error("获取偏好数据失败: " + response.msg);
      console.error("获取偏好数据失败:", response);
    }
  }).fail(function(jqXHR, textStatus, errorThrown) {
    ElMessage.error("请求失败: " + textStatus);
    console.error("请求失败:", textStatus, errorThrown);
  });
}

function savePreferences() {
  if (!userData.travelCompanion) {
    userData.travelCompanion = 1;
  }
  if (!userData.interestedPlaces) {
    userData.interestedPlaces = 1;
  }
  if (!userData.interestedWays) {
    userData.interestedWays = 1;
  }
  if (!gender.value) {
    gender.value = '请选择性别';
  }
  if (!age.value) {
    age.value = '请选择年龄';
  }
  if (!hometown.value) {
    hometown.value = '请选择家乡';
  }
  $.post("http://localhost:7778/preferences?id=" + userData.id + '&username=' + userData.username + '&password=' + userData.password + '&gender=' + gender.value + '&age=' + age.value + '&hometown=' + hometown.value + '&interestedPlaces=' + userData.interestedPlaces + '&interestedWays=' + userData.interestedWays + '&travelCompanion=' + userData.travelCompanion, function(response) {
    console.log("服务器响应:", response);
    if (response.code == "1") {
      // alert("更新成功" + response.data.imgUrl);
    } else {
      alert("更新失败" + response.data.age);
    }
  }).done(function() {
  }).fail(function() {
  });
}
function startEditing() {
  isEditing.value = true;
  fetchPreferences();
}

function save() {
  isEditing.value = false;
  savePreferences();
}

// function navigate(page) {
//   emit('navigate', page);
// }

onMounted(() => {
  fetchPreferences();
});
</script>

<style scoped>
.profile-page {
  position: fixed;
  left: 30.4vw;
  top: 0;
  bottom: 20vh;
  width: 40.5vw;
  padding-right: 30px;
  /* border-right: 1px solid #e0e0e0; */
  box-sizing: content-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transform: scale(1.15); /* 放大整体界面 */
  transform-origin: top; 
}

.content {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-container {
  display: flex;
  justify-content: center; 
  margin-bottom: 20px; 
}

.info {
  width: 100%;
  max-width: 400px;
  margin-bottom: 20px;
  
}

.avatar-image {
  left: 60vw;
  width: 150px; 
  height: 150px;
  border-radius: 50%;
  margin-bottom: 20px;
  object-fit: cover;
}

.el-form-item {
  margin-bottom: 20px;
}

.button-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
