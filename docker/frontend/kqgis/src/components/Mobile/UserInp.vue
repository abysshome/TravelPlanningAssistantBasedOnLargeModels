<template>
    <div class="radius">
        <el-button type="primary" @click="delDia" class="del" color="#ef4545">
            <el-icon class="el-icon--right"><Delete /></el-icon>
        </el-button>
        <div class="textInp">
            <el-input v-model="textarea" style="width: 100%;height: 100%;"
                      input-style="border: 1px solid #f3f5fc; background-color: #f3f5fc;" :rows="1"
                      placeholder="Please input" />
        </div>
        <div class="toolbar">
                <el-icon v-loading.fullscreen.lock="fullscreenLoading" class="svg" @click="showMap" title="生成地图">
                    <Promotion />
                </el-icon>
            <el-icon class="svg" @click="chat" v-if="!isLoading" title="发送">
                <ChatRound />
            </el-icon>
            <el-icon class="svg" v-else title="等待中">
                <Loading />
            </el-icon>
        </div>
    </div>

<el-dialog
    v-model="dialogVisible"
    title=""

    style="height: 60%; width: 80%;"
  >
     <!-- <img :src="picPath" alt="" width="250px"> -->
      <iframe style="height: 500px; width: 100%;" :src="'http://localhost:7778/dialog/html?dialogId='+props.diaID" frameborder="0"></iframe>
</el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessageBox } from 'element-plus'
const fullscreenLoading = ref(false)
const htmlPath = ref("");
const props = defineProps({
    isLoading: Boolean,
    userID: String,
    diaID: String,
})
let textarea = ref("")
const emit = defineEmits(['chat', 'delDia']);
const dialogVisible = ref(false)
// function downloadFromURL(filename, url) {
//   fetch(url)
//     .then(response => response.text())
//     .then(content => {
//       const element = document.createElement('a');
//       element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(content));
//       element.setAttribute('download', filename);

//       element.style.display = 'none';
//       document.body.appendChild(element);

//       element.click();

//       document.body.removeChild(element);
//     });
// }
function chat() {
    emit('chat', textarea.value);
    textarea.value = "";
}
function delDia() {
    emit('delDia');
}
function showMap() {
    fullscreenLoading.value = true;
    $.get("http://localhost:7778/dialog/setSolution?id="+props.userID+"&dialogId="+props.diaID, function(response) {
        console.log(response);
        dialogVisible.value = true;
        htmlPath.value = "http://localhost:7778/dialog/html?dialogId="+props.diaID;
        console.log(htmlPath.value);
    }).done(function() {
        fullscreenLoading.value = false;
    }).fail(function() {
        fullscreenLoading.value = false;
    });
    // downloadFromURL('旅游路线.html',htmlPath.value)
}

</script>

<style>
.del {
    margin-top: 4px;
    margin-right: 4px;
}
.el-input__wrapper {
    border: 1px solid #f3f5fc;
    background-color: #f3f5fc;
}
iframe {
  border: 1px solid black;
  width: 100%; /* takes precedence over the width set with the HTML width attribute */
}
.radius {
    border-radius: 20px;
    border: 1px solid rgb(210, 204, 204);
    min-width: 370px;
    display: flex;
    height: 58px;
    padding: 8px 16px;
}

.textInp {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.toolbar {
    width: 70px;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.svg {
    font-size: 24px;
    cursor: pointer;
    transition: background-color 0.3s, transform 0.3s;
}

.svg:hover {
    background-color: #ccc;
    transform: scale(1.05);
}

</style>