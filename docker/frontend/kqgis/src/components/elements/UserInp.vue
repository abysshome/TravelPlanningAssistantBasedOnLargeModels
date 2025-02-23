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
                <el-icon  class="svg" @click="showMap" title="生成地图">
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
    <div v-if="fullscreenLoading" class="loading-mask">
        <div class="loading-content">
            <el-icon class="loading-icon"><Loading /></el-icon>
            <p>地图加载中</p>
        </div>
    </div>
</template>

<script setup>
import { ElMessage } from 'element-plus';
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

function chat() {
    // alert(textarea.value);
    emit('chat', textarea.value);
    textarea.value = "";
}
function delDia() {
    emit('delDia');
}


function showMap() {
    fullscreenLoading.value = true;
    // alert(props.userID + " " + props.diaID);
    
    $.get("http://localhost:7778/dialog/setSolution?id=" + props.userID + "&dialogId=" + props.diaID, function(response) {
        console.log(response);
        console.log(response.code);
        // alert(props.userID + " " + response.code);
        // 检查响应状态
        if (response.code === 1) {
            const itinerary = response.data;
            const message = `
                出发地点: ${itinerary['出发地点']},${itinerary['出发时间']}
                结束地点: ${itinerary['结束地点']},${itinerary['结束时间']}
                出发地点经纬度: (经度: ${itinerary['出发地点经纬度'].longitude}, 纬度: ${itinerary['出发地点经纬度'].latitude})
                结束地点经纬度: (经度: ${itinerary['结束地点经纬度'].longitude}, 纬度: ${itinerary['结束地点经纬度'].latitude})
                途径景点: ${itinerary['途径景点'].map(spot => `
                    名称: ${spot['名称']}
                    经纬度: (${spot['longitude']}, ${spot['latitude']})
                    时间: (${spot['到达时间']}, ${spot['离开时间']})
                `).join('')}
            `;
            emit('updateMessage', message);
            // 显示成功信息
            ElMessage({
                message: message,
                type: 'success',
                duration: 10 // 10秒后自动关闭
            });
        } else {
            // 显示错误信息
            ElMessage({
                message: response.msg,
                type: 'error',
            });
        }
        
        console.log(htmlPath.value);
    }).done(function() {
        fullscreenLoading.value = false;
    }).fail(function() {
        ElMessage({
            message: '请求失败，请重试。',
            type: 'error',
        });
        fullscreenLoading.value = false;
    });
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

.loading-mask {
    position: fixed;
    top: 25%;
    left: 25%;
    width: 50%;
    height: 50%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
    border-radius: 10px;
}

.loading-content {
    text-align: center;
    color: white;
}

.loading-icon {
    font-size: 48px; /* Increase the size of the loading icon */
    margin-bottom: 10px;
}

</style>
