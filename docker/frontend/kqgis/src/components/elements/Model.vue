<template>
<el-select
      v-model="sel"
      placeholder="sel"
      size="large"
      style="width: 240px"
    >
      <el-option
        v-for="item in options"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
</el-select>
</template>
<script setup>
import {ref} from "vue";
const options = [{value:""},{value:""}]
const sel = ref('');
</script>
