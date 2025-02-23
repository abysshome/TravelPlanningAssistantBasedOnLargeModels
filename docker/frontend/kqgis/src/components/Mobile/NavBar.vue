<template>
  <el-dialog
    v-model="dialogVisible"
    title="新建对话"
    width="300"
    :before-close="handleClose"
    :style="{ height: '230px' }"
  >
    <template #footer>
      <div class="dialog-footer">
        <el-input
          v-model="diaName"
          style="height: 45px; width: 240px;"
          maxlength="10"
          placeholder="input name"
          show-word-limit
          type="text"
          class="inp"
        />
        <div class="button-group">
          <el-button @click="dialogVisible = false" size="large" style="width: 80%;">
            取消
          </el-button>
          <el-button type="primary" @click="newDia" size="large" style="width: 80%;">
            确认
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>

  <el-header class="header">
    <div class="center-buttons">
      <el-dropdown trigger="click">
        <el-button size="large" @click="selector = true" class="text-center-button" round>对话历史</el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="openNewDialog">
              <el-icon><Plus /></el-icon>新对话
            </el-dropdown-item>
            <el-dropdown-item
              v-for="dia in props.data"
              :key="dia.id"
              @click="selectDia(dia.id)"
            >
              {{ dia.name }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <el-button size="large" @click="navigate('profile')" class="text-center-button" round>个人主页</el-button>
    </div>
  </el-header>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue';

let dialogVisible = ref(false);
let diaName = ref("");
let diaContent = ref([]);

const props = defineProps({
  currentPage: {
    type: String,
    required: true,
  },
  data: Array
});

const emit = defineEmits(['navigate', 'newDia', 'updateDia', 'updateSelectDiaID']);
let selector = ref(false);

function newDia() {
  dialogVisible.value = false;
  emit('newDia', diaName.value);
  diaName.value = "";
  navigate('navigation');
}

function updateSelectDiaID(id) {
  emit('updateSelectDiaID', id);
}

function selectDia(id) {
  $.get("http://localhost:7778/dialog/records?id=" + id, function(response) {
    updateSelectDiaID(id);
    diaContent.value = response.data;
    diaName.value = response.data.name; // 设置初始文本为对话名
    emit('updateDia', diaContent.value);
    navigate('navigation');
  }).done(function() {
  }).fail(function() {
  });
}

function openNewDialog() {
  diaName.value = "新对话"; // 设置初始文本
  dialogVisible.value = true;
}

function navigate(page) {
  emit('navigate', page);
}
</script>

<style scoped>
.inp {
  margin-right: 30px;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
}

.header {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0 20px;
  background-color: #ffffff;
  color: white;
}

.center-buttons {
  display: flex;
  gap: 20px;
}

.text-center-button {
  text-align: center; /* 水平居中文本 */
  padding: 10px 20px; /* 根据需要设置按钮的内边距 */
}

.black-text-button {
  color: black !important;
}

.button-group {
  display: flex;
  justify-content: space-between;
  width: 90%;
  margin-top: 42px; 
}
</style>
