<template>
  <div class="interest-container">
    <h2>感兴趣的地点</h2>
    <div class="interest-options">
      <el-button
        v-for="option in locationOptions"
        :key="option.name"
        :class="{'selected': selectedLocations.includes(option.name)}"
        @click="toggleLocation(option.name)"
        class="interest-button"
      >
        <img :src="option.image" :alt="option.name" class="interest-image"/>
        {{ option.name }}
      </el-button>
    </div>

    <h2>感兴趣的旅行方式</h2>
    <div class="interest-options">
      <el-button
        v-for="option in travelOptions"
        :key="option.name"
        :class="{'selected': selectedTravels.includes(option.name)}"
        @click="toggleTravel(option.name)"
        class="interest-button"
      >
        <img :src="option.image" :alt="option.name" class="interest-image"/>
        {{ option.name }}
      </el-button>
    </div>

    <h2>旅行伙伴</h2>
    <div class="interest-options">
      <el-button
        v-for="option in partnerOptions"
        :key="option.name"
        :class="{'selected': selectedPartners.includes(option.name)}"
        @click="togglePartner(option.name)"
        class="interest-button"
      >
        <img :src="option.image" :alt="option.name" class="interest-image"/>
        {{ option.name }}
      </el-button>
    </div>
    <el-footer class="footer">
      <el-button @click="saveAndReturn" type="primary">保存</el-button>
    </el-footer>
  </div>
</template>

<script setup>
import { ref, defineEmits, defineProps } from 'vue';
const props = defineProps({
  // getSelectedInterests: Function,
  data: Object,
});

const locationOptions = ref([
  { name: '自然风光', image: 'src/photo/interset1/nature.jpg' },
  { name: '人文历史', image: 'src/photo/interset1/history.jpg' },
  { name: '艺术展览', image: 'src/photo/interset1/art.jpg' },
  { name: '美食', image: 'src/photo/interset1/food.jpg' },
  { name: '电影', image: 'src/photo/interset1/film.jpg' },
  { name: '户外运动', image: 'src/photo/interset1/sport.jpg' },
  { name: '室内休闲', image: 'src/photo/interset1/rest.jpg' },
  { name: '网红打卡点', image: 'src/photo/interset1/net.jpg' }
]);

const travelOptions = ref([
  { name: '特种兵式旅行', image: 'src/photo/interest2/vel.jpg' },
  { name: 'citywalk', image: 'src/photo/interest2/walk.jpg' },
  { name: '常规旅行方式', image: 'src/photo/interest2/regular.jpg' }
]);

const partnerOptions = ref([
  { name: '独自', image: 'src/photo/interest3/individual.jpg' },
  { name: '家人', image: 'src/photo/interest3/family.jpg' },
  { name: '朋友', image: 'src/photo/interest3/friend.jpg' },
  { name: '恋人', image: 'src/photo/interest3/lover.jpg' }
]);

let selectedLocations = ref([]);
let selectedTravels = ref([]);
let selectedPartners = ref([]);

function toggleLocation(name) {
  const index = selectedLocations.value.indexOf(name);
  if (index === -1) {
    selectedLocations.value.push(name);
  } else {
    selectedLocations.value.splice(index, 1);
  }
}

function toggleTravel(name) {
  const index = selectedTravels.value.indexOf(name);
  if (index === -1) {
    selectedTravels.value.push(name);
  } else {
    selectedTravels.value.splice(index, 1);
  }
}

function togglePartner(name) {
  const index = selectedPartners.value.indexOf(name);
  if (index === -1) {
    selectedPartners.value.push(name);
  } else {
    selectedPartners.value.splice(index, 1);
  }
}

// function isInterest1Selected(interestName) {
//   return selectedLocations.value.includes(interestName);
// }


// 从后端读取已选择的兴趣点
function foo() {
  $.get("http://localhost:7778/preferences?id=" + props.data.id, function(response) {
    if (response.code === 1) {
      const data = response.data;
      // 将 data.interestedPlaces 按逗号分割成数组
      const interest1Array = data.interestedPlaces.split(',');
      const interest2Array = data.interestedWays.split(',');
      const interest3Array = data.travelCompanion.split(',');
      // 清空 selectedInterests
      // selectedInterests.value = [];
      selectedLocations = [];
      selectedTravels = [];
      selectedPartners = [];

      // 遍历数组，依次调用 toggleInterest
      interest1Array.forEach(interestName => {
        if (interestName.trim() !== '') toggleLocation(interestName.trim());
      });
      interest2Array.forEach(interestName => {
        if (interestName.trim() !== '') toggleTravel(interestName.trim());
      });
      interest3Array.forEach(interestName => {
        if (interestName.trim() !== '') togglePartner(interestName.trim());
      });

      // alert("更新成功" + data.interestedPlaces);
      // console.log("已选择的兴趣点获取成功:", selectedInterests.value);
    } else {
      ElMessage.error("获取已选择兴趣点失败: " + response.msg);
      console.error("获取已选择兴趣点失败:", response);
    }
  }).fail(function(jqXHR, textStatus, errorThrown) {
    ElMessage.error("请求失败: " + textStatus);
    console.error("请求失败:", textStatus, errorThrown);
  });
}
foo();
// 修改 fetchPreferences() 函数
function fetchPreferences(callback) {
  $.get("http://localhost:7778/preferences?id=" + props.data.id, function(response) {
    if (response.code === 1) {
      const data = response.data;
      // alert("更新成功"+data.interestedPlaces);
      const genderValue=ref("请选择性别");
      const ageValue=ref("0");
      const hometownValue=ref("请选择家乡");
      // alert(genderValue.value);
      if(data.age){
        ageValue.value=data.age;
      }
      if(data.gender){
        genderValue.value=data.gender;
      }
      if(data.hometown){
        hometownValue.value=data.hometown;
      }
      // alert(ageValue.value);
      // alert(data.age);
      callback(genderValue.value, ageValue.value, hometownValue.value);  // 调用回调函数传递数据
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

// 在 saveAndReturn() 中使用回调函数传递的数据
function saveAndReturn() {
  fetchPreferences(function(genderValue, ageValue, hometownValue) {
    // 使用 fetchPreferences() 中获取的数据
    const selectedInterest1String = selectedLocations.value.join(',');
    const selectedInterest2String = selectedTravels.value.join(',');
    const selectedInterest3String = selectedPartners.value.join(',');
    $.post("http://localhost:7778/preferences?id=" + props.data.id + '&username=' + props.data.username + '&password=' + props.data.password + '&gender=' + genderValue + '&age=' + ageValue + '&hometown=' + hometownValue + '&interestedPlaces=' + selectedInterest1String + '&interestedWays=' + selectedInterest2String + '&travelCompanion=' + selectedInterest3String, function(response) {
      if (response.code === 1) {
        // emit('updateInterests', selectedInterests.value);
        // emit('navigate', 'profile');
        alert("更新成功"+response.data.interestedPlaces+response.data.interestedWays+response.data.travelCompanion);
        // alert("更新成功"+response.data.interestedPlaces);
        // alert("更新成功");
      } else {
        alert("更新失败：" + response.msg);
      }
    }).fail(function() {
      alert("请求失败");
    });
  });
}
</script>

<style scoped>
.interest-container {
  position: fixed;
  top: 10.5vh;
  left: 26.4vw;
  width: 55.5vw; 
  padding-right: 30px; /* 与 .main-web 的 padding-right 相同 */
  /* border-right: 1px solid #e0e0e0; 保留右侧边框 */
  box-sizing: content-box; /* 使 padding 不影响内容宽度 */
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.interest-options {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.interest-button {
  width: 170px; /* 增大按钮大小 */
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.interest-image {
  width: 60px; /* 增大图片大小 */
  height: 60px;
  margin-right: 10px;
}

.selected {
  background-color: #409eff;
  color: #fff;
}
</style>
