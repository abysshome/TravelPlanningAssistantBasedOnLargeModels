<template>
    <div v-if="!isGoto" class="interest-page">
      <div class="interest-title" style="font-size: 30px; text-align: center;">
        <h3>感兴趣的旅行方式</h3>
      </div>
      <div class="interests">
        <div v-for="interest in interestOptions" :key="interest.name" class="interest-item">
          <el-image
            :src="interest.image"
            :class="{ selected: isInterestSelected(interest.name) }"
            @click="toggleInterest(interest.name)"
          />
          <el-button
            @click="toggleInterest(interest.name)"
            :type="isInterestSelected(interest.name) ? 'primary' : 'default'"
          >
            {{ interest.name }}
          </el-button>
        </div>
      </div>
      <el-footer class="footer">
        <el-button @click="saveAndReturn" type="primary">下一步</el-button>
        <el-button @click="saveAndSkip" type="primary">跳过</el-button>
      </el-footer>
    </div>
    <div v-else>
      <Interest6 v-if="!isGoto1" @navigate="navigate" :data="data"/>
      <MainMobile v-else :data="data"/>
    </div>
  </template>
  
  <script setup>
  import { ref, defineEmits, defineProps } from 'vue';
  import Interest6 from './Interest6.vue';
  import MainMobile from './MainMobile.vue';

  const emit = defineEmits(['updateInterests2', 'navigate']);
  const props = defineProps({
    // getSelectedInterests2: Function,
    data:Object,
  });
  const isGoto = ref(false);
  const isGoto1 = ref(false);
  const interestOptions = [
    { name: '特种兵式旅行', image: 'src/photo/interest2/vel.jpg' },
    { name: 'citywalk', image: 'src/photo/interest2/walk.jpg' },
    { name: '常规旅行方式', image: 'src/photo/interest2/regular.jpg' }
  ];
  
  const selectedInterests = ref([]);
  
  function toggleInterest(interestName) {
    const index = selectedInterests.value.indexOf(interestName);
    if (index > -1) {
      selectedInterests.value.splice(index, 1);
    } else {
      selectedInterests.value.push(interestName);
    }
  }
  
  function isInterestSelected(interestName) {
    return selectedInterests.value.includes(interestName);
  }
  
  // 从后端读取已选择的兴趣点
  function foo() {
    $.get("http://localhost:7778/preferences?id=" + props.data.id, function(response) {
      if (response.code === 1) {
        const data = response.data;
        // 将 data.interestedWays 按逗号分割成数组
        const interestsArray = data.interestedWays.split(',');
  
        // 清空 selectedInterests
        selectedInterests.value = [];
  
        // 遍历数组，依次调用 toggleInterest
        interestsArray.forEach(interestName => {
          toggleInterest(interestName.trim());
        });
  
        // alert("更新成功" + data.interestedWays);
        console.log("已选择的兴趣点获取成功:", selectedInterests.value);
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
        // alert("更新成功"+data.interestedWays);
        if (!data.gender) {
          data.gender = '请选择性别';
        }
        if (!data.age) {
          data.age= '请选择年龄';
        }
        if (!data.hometown) {
          data.hometown = '请选择家乡';
        }
        if (!data.travelCompanion) {
          data.travelCompanion = 1;
        }
        if (!data.interestedPlaces) {
          data.interestedPlaces = 1;
        }
        if (!data.interestedWays) {
          data.interestedWays = 1;
        }
        callback(data.gender, data.age, data.hometown,data.travelCompanion,data.interestedPlaces);  // 调用回调函数传递数据
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
    fetchPreferences(function(genderValue, ageValue, hometownValue,travelCompanionValue,interestedPlacesValue) {
      // 使用 fetchPreferences() 中获取的数据
      const selectedInterestsString = selectedInterests.value.join(',');
      $.post("http://localhost:7778/preferences?id=" + props.data.id + '&username=' + props.data.username + '&password=' + props.data.password + '&gender=' + genderValue + '&age=' + ageValue + '&hometown=' + hometownValue + '&interestedPlaces=' + interestedPlacesValue+ '&interestedWays=' + selectedInterestsString + '&travelCompanion=' + travelCompanionValue, function(response) {
        if (response.code === 1) {
          // emit('updateInterests', selectedInterests.value);
          isGoto.value=true;
          emit('navigate', 'interest6');
          // alert("更新成功"+response.data.interestedWays);
          // alert("更新成功");
        } else {
          alert("更新失败：" + response.msg);
        }
      }).fail(function() {
        alert("请求失败");
      });
    });
  }

  function saveAndSkip() {
    fetchPreferences(function(genderValue, ageValue, hometownValue,travelCompanionValue,interestedPlacesValue) {
      // 使用 fetchPreferences() 中获取的数据
      const selectedInterestsString = selectedInterests.value.join(',');
      $.post("http://localhost:7778/preferences?id=" + props.data.id + '&username=' + props.data.username + '&password=' + props.data.password + '&gender=' + genderValue + '&age=' + ageValue + '&hometown=' + hometownValue + '&interestedPlaces=' + interestedPlacesValue + '&interestedWays=' + selectedInterestsString + '&travelCompanion=' + travelCompanionValue, function(response) {
        if (response.code === 1) {
          // emit('updateInterests', selectedInterests.value);
          isGoto1.value=true;
          isGoto.value=true;
          emit('navigate', 'interest5');
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
  .interest-page {
    display: flex;
    flex-direction: column;
    height: 100vh;
  }
  
  .interest-title {
    text-align: center;
  }
  
  .interests {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    padding: 20px;
  }
  
  .interest-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin: 10px;
  }
  
  .interest-item .el-image {
    width: 100px;
    height: 100px;
    cursor: pointer;
    margin-bottom: 10px;
  }
  
  .interest-item .el-image.selected {
    border: 2px solid #409EFF;
  }
  
  .footer {
    display: flex;
    justify-content: center;
    padding: 20px;
  }
  </style>
  