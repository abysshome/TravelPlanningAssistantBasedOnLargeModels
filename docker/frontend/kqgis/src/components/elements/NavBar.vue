<template>
 <el-dialog
    v-model="dialogVisible"
    title="新建对话"
    width="350"
    :before-close="handleClose"
    class="new-dialog"
  >
    <el-input
      v-model="diaName"
      maxlength="10"
      placeholder="输入对话名称"
      show-word-limit
      class="dialog-input"
    />
    <template #footer>
      <div class="dialog-footer">
        <div class="button-group">
          <el-button @click="dialogVisible = false" size="large" class="cancel-button">
            取消
          </el-button>
          <el-button type="primary" @click="newDia" size="large" class="confirm-button">
            确认
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>

  <el-header class="header">
    <div class="center-buttons">
      <el-dropdown trigger="click">
        <el-button size="large" @click="selector = true" class="text-center-button">
          对话历史
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </el-button>
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
    </div>
  </el-header>
</template>

<script setup>
import { ref, defineProps, defineEmits , watch, onMounted} from 'vue';
import { ArrowDown, Plus } from '@element-plus/icons-vue';

let dialogVisible = ref(false);
let diaName = ref("");
let diaContent = ref([]);

const props = defineProps({
  currentPage: {
    type: String,
    required: true,
  },
  data: Array,
  firstId: { 
    type: String,
    required: true,
  },
  userID: String,
  delId:{ 
    type: String,
    required: true,
  }
});

const emit = defineEmits(['navigate', 'newDia', 'updateDia', 'updateSelectDiaID']);
let selector = ref(false);






function newDia() {
  dialogVisible.value = false;
  emit('newDia', diaName.value);
  diaName.value = "";
  // navigate('mainweb');
  $.get("http://localhost:7778/dialog?id=" + props.userID, function(response) {
}).fail(function() {
  console.log('请求失败');
});

  getNewId();

}


function getNewId() {
  $.get("http://localhost:7778/dialog?id=" + props.userID, function(response) {
  if (response.data && response.data.length > 0) {
    // 提取所有的 id
    const ids = response.data.map(dia => Number(dia.id)); // 或者 parseInt(dia.id, 10)
    // 找到最大的 id
    const maxId = Math.max(...ids);
    // alert(maxId);
    selectDia(maxId);
    console.log('最大的 ID:', maxId);
  } else {
    console.log('没有找到任何对话数据');
  }
}).fail(function() {
  console.log('请求失败');
});

}


function updateSelectDiaID(id) {
  emit('updateSelectDiaID', id);
}

function selectDia(id) {
  $.get("http://localhost:7778/dialog/records?id=" + id, function(response) {
    // alert(id);
    updateSelectDiaID(id);
    diaContent.value = response.data;
    diaName.value = response.data.name; // 设置初始文本为对话名
    emit('updateDia', diaContent.value);
    navigate('mainweb');
  }).done(function() {
  }).fail(function() {
  });
}
watch(() => props.firstId, (newVal) => {
  // alert(props.userID);
  selectDia(props.firstId);
   // 当 firstId 变化时调用 foo 函数
});
watch(() => props.delId, (newVal) => {
  alert("删除成功");
  console.log(props.userID);
  // selectDia(props.firstId);
  $.get("http://localhost:7778/dialog?id=" + props.userID, function(response) {
}).fail(function() {
  console.log('请求失败');
});
  getNewId();
});

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

.new-dialog {
  background-color: #f0f0f0; /* 背景颜色 */
  border-radius: 8px; /* 圆角 */
}

.dialog-input {
  margin: 20px 0; /* 输入框上下间距 */
  width: 100%; /* 宽度自适应 */
  height: 45px; /* 输入框高度 */
}

.dialog-footer {
  display: flex;
  justify-content: center; /* 中心对齐 */
  padding: 10px 20px; /* 边距 */
}

.button-group {
  display: flex;
  gap: 10px; /* 按钮间距 */
}

.cancel-button {
  color: rgb(0, 0, 0); /* 字体颜色 */
  border-radius: 4px; /* 圆角 */
  /* border: none; */
}

.confirm-button {
  color: white; /* 字体颜色 */
  border-radius: 4px; /* 圆角 */
  border: none;
}

.confirm-button:hover {
  background-color: (135, 206, 250, 0.8); /* 悬停效果 */
  color: #ffffff; /* 悬停时的字体颜色 */
}

.cancel-button:hover {
  background-color: (135, 206, 250, 0.8); /* 悬停效果 */
  color: #000000; /* 悬停时的字体颜色 */
}

.header {
  padding: 10px 20px; /* 边距 */
  background-color: #ffffff; /* 头部背景颜色 */
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

.square-button {
  border-radius: 0; /* 移除圆角，改为方形 */
}
</style>
