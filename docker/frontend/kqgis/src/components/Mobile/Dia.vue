<template>

    <div>
        <!-- 头像 -->
        <!-- <div class="demo-type">
                <el-avatar :size="35"> user </el-avatar>
        </div> -->
        <!-- 对话框 -->
        <div class="Dia Dia0">
            {{ msg.input }}
        </div>
    </div>
    <div>
        <!-- 头像 -->
        <div class="demo-type">
            <div>
            </div>
        </div>
        <div class="Dia Dia1">{{ msg.output }}</div>
    </div>


</template>
<script setup>
const props = defineProps({
    msg: {}
})
</script>
<style>
.Dia {
    border-radius: 20px;
    /* border: 1px solid gray; */
    display: flex;
    padding: 8px 16px;
    font-size: 18px; /* 调整此处的字体大小 */
    /* box-shadow: 1px 1px 1px 1px; */
}

.Dia0 {
    background: #ffffff;
}

.Dia1 {
    background: #f2f2f2;
}
</style>