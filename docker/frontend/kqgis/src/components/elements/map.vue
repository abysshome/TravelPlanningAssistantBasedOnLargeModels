<script setup>
import { onMounted } from 'vue';
onMounted(() => {
  // 相当于页面加载完后执行 onload 函数
  function initMap() {
    // 设置 resolutions
    const res = [];
    for (let i = 0; i < 18; i++) {
      res.push(156543.03392828687 / Math.pow(2, i));
    }

    // 初始化地图
    const map = L.map('map', {
      zoomControl: false,
      center: [30.543, 114.397],
      zoom: 11,
      crs: new L.Proj.CRS('EPSG:3857', {
        origin: [-20037508.3427892, 20037508.3427892],
        resolutions: res,
        bounds: L.bounds(
          [-20037508.3427892, -20037508.3427892],
          [20037508.3427892, 20037508.3427892]
        ),
      }),
    });

    // 创建图层组并添加到地图中
    const layergroup = L.layerGroup();
    layergroup.addTo(map);

    // 加载天地图图层
    L.kqmap.mapping.tiandituTileLayer({ layerType: 'vec' }).addTo(map);

    // 添加 WMS 图层
    const jingjinUrl =
      'http://127.0.0.1:8699/KQGis/rest/services/test/MapServer/WMS';
    const jingjin3857Layer = L.kqmap.mapping.singleWMSLayer(jingjinUrl, {
      layers: [2],
      format: 'image/png',
    });

    jingjin3857Layer.addTo(layergroup);
    map.setView([26.62, 106.71], 12); // 设置初始视图
  }

  initMap(); // 调用地图初始化函数
});
</script>

<template>
  <div id = "map"></div> <!-- 地图容器 -->
</template>

<style scoped>
#map {
  position: absolute;
  left: 1000px;
  width: 100%;
  height: 100%;
  margin: 0 auto;
}
</style>
 