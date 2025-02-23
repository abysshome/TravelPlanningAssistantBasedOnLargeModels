<template>
  <div ref="mapContainer" class="map"></div> <!-- Leaflet地图容器 -->
</template>

<script setup>
import { onMounted, ref , defineProps ,watch} from 'vue';
import L from 'leaflet';
import axios from 'axios';
import 'leaflet/dist/leaflet.css';
let map; // 初始化地图为北京南站
const mapContainer = ref(null);
const AMAP_API_KEY = '6a186a3cb0f3af7939876f5d9e3e32c7';
const props = defineProps({
  message: { // 更新props以接收message
    type: String,
    required: true,
  },
});

// 监听 message 的变化
watch(() => props.message, async (newValue, oldValue) => {
  // 清空现有地图的标记和路径
  if (map) {
    map.eachLayer(layer => {
      if (layer instanceof L.Marker || layer instanceof L.Polyline) {
        map.removeLayer(layer);
      }
    });
    map.remove(); // 移除地图
  }

  const points = [];

  // 提取出发地点和结束地点
  const startLocationMatch = props.message.match(/出发地点:\s*(.*)/);
  const endLocationMatch = props.message.match(/结束地点:\s*(.*)/);
  const startCoordsMatch = props.message.match(/出发地点经纬度:\s*\(经度:\s*([\d.-]+),\s*纬度:\s*([\d.-]+)\)/);
  const endCoordsMatch = props.message.match(/结束地点经纬度:\s*\(经度:\s*([\d.-]+),\s*纬度:\s*([\d.-]+)\)/);

  if (startLocationMatch && startCoordsMatch) {
    const wgs84Coords = {
      lat: parseFloat(startCoordsMatch[2]), // 纬度
      lon: parseFloat(startCoordsMatch[1])  // 经度
    };
    const gcj02Coords = wgs84ToGcj02(wgs84Coords.lat, wgs84Coords.lon); // 转换为 GCJ-02

    points.push({
      coords: [gcj02Coords.lat, gcj02Coords.lon], // 使用转换后的坐标
      name: startLocationMatch[1].trim().replace(/,/, '<br>出发时间：') 
    });
  }

  const regex = /名称:\s*(.+?)\s*经纬度:\s*\(([^,]+),\s*([^\)]+)\)\s*时间:\s*\(([^,]+),\s*([^\)]+)\)/g;
  let match;

  while ((match = regex.exec(props.message)) !== null) {
    const name = `${match[1]}<br>到达时间：${match[4]}<br>离开时间：${match[5]}`;
    const wgs84Coords1 = {
      lat: parseFloat(match[3]), // 纬度
      lon: parseFloat(match[2])  // 经度
    };
    const gcj02Coords1 = wgs84ToGcj02(wgs84Coords1.lat, wgs84Coords1.lon);
    const coords = [gcj02Coords1.lat, gcj02Coords1.lon];
    points.push({ coords, name });
  }

  if (endLocationMatch && endCoordsMatch) {
    const wgs84Coords2 = {
      lat: parseFloat(endCoordsMatch[2]), // 纬度
      lon: parseFloat(endCoordsMatch[1])  // 经度
    };
    const gcj02Coords2 = wgs84ToGcj02(wgs84Coords2.lat, wgs84Coords2.lon); // 转换为 GCJ-02

    points.push({
      coords: [gcj02Coords2.lat, gcj02Coords2.lon], // 使用转换后的坐标
      name: endLocationMatch[1].trim().replace(/,/, '<br>结束时间：')
    });
  }

  // 初始化地图
  map = L.map(mapContainer.value).setView([points[0].coords[0], points[0].coords[1]], 13);

  L.tileLayer('http://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}', {
    subdomains: ['1', '2', '3', '4'],
    maxZoom: 19,
  }).addTo(map);

  // 添加标记
  points.forEach(point => {
    L.marker(point.coords)
      .addTo(map)
      .bindPopup(point.name)
      .openPopup();
  });

  // 顺序获取并显示路径
  for (let i = 0; i < points.length - 1; i++) {
    try {
      const route = await fetchRoute(points[i], points[i + 1]);
      if (route) {
        await animatePolylines([route], map); // 确保逐段显示路径
      }
    } catch (error) {
      console.error('Error fetching route:', error);
    }
  }
});

  function wgs84ToGcj02(lat, lon) {
    const pi = 3.1415926535897932384626;
    const a = 6378245.0; // 地球半径
    const ee = 0.00669342162296594323; // 椭球体参数

    if (outOfChina(lat, lon)) {
        return { lat: lat, lon: lon }; // 如果不在中国，返回原坐标
    }

    const dLat = transformLat(lon - 105.0, lat - 35.0);
    const dLon = transformLon(lon - 105.0, lat - 35.0);
    const radLat = lat / 180.0 * pi;
    const magic = Math.sin(radLat);
    const magic2 = 1 - ee * magic * magic;
    const sqrtMagic = Math.sqrt(magic2);
    const dLatAdjusted = (dLat * 180.0) / ((a * (1 - ee)) / (magic2 * sqrtMagic) * pi);
    const dLonAdjusted = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
    
    const gcjLat = lat + dLatAdjusted;
    const gcjLon = lon + dLonAdjusted;

    return { lat: gcjLat, lon: gcjLon };
}

function outOfChina(lat, lon) {
    return lon < 72.004 || lon > 137.8347 || lat < 0.8293 || lat > 55.8271;
}

function transformLat(x, y) {
    const pi = 3.1415926535897932384626;
    const a = 6378245.0; // 地球半径
    const ee = 0.00669342162296594323; // 椭球体参数
    let ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
    ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
    ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
    ret += (160.0 * Math.sin(y / 12.0 * pi) + 320.0 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
    return ret;
}

function transformLon(x, y) {
    const pi = 3.1415926535897932384626;
    let ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
    ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
    ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
    ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
    return ret;
}

// onMounted(async () => {
//           const ipLocationUrl = `https://restapi.amap.com/v5/ip/location?key=${AMAP_API_KEY}&type=6`;
          
//           try {
//             // Fetch IP location
//             const ipResponse = await fetch(ipLocationUrl);
            
//             // Alert JSON formatted response

//             // alert(ipResponse); // Alert the response object (this will show [object Response])
//             const ipData = await ipResponse.json();
//             alert(JSON.stringify(ipData, null, 2));
//             if (!ipResponse.ok) throw new Error('Network response was not ok');
//             // const ipData = await ipResponse.json();
//             // alert(JSON.stringify(ipData, null, 2));

//             if (ipData.status !== '1') {
//               throw new Error(`IP location fetch failed: ${ipData.info}`);
//             }

//             const { location, province, city } = ipData;
//             const [longitude, latitude] = location.split(',');

//             console.log(`IP location: ${province} ${city} - [Longitude: ${longitude}, Latitude: ${latitude}]`);

//             map = L.map(mapContainer.value).setView([parseFloat(latitude), parseFloat(longitude)], 13);

//             L.tileLayer('http://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}', {
//               subdomains: ['1', '2', '3', '4'],
//               maxZoom: 19,
//             }).addTo(map);
//             alert(1);
//           } catch (error) {
            
//             console.error('Error fetching IP location:', error);
//           }
//         });

onMounted(() => {




      // if (navigator.geolocation) {
      //   navigator.geolocation.getCurrentPosition(
      //     (position) => {
      //       const latitude = position.coords.latitude;
      //       const longitude = position.coords.longitude;
      //       // 39.727784, 116.075278
      //       // 初始化地图为当前经纬度
      //       map = L.map(mapContainer.value).setView([parseFloat(latitude), parseFloat(longitude)], 13);

      //       L.tileLayer('http://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}', {
      //         subdomains: ['1', '2', '3', '4'],
      //         maxZoom: 19,
      //       }).addTo(map);
      //     },
      //     (error) => {
      //       console.error("Error getting location: ", error);
      //       // 如果获取位置失败，可以使用默认的经纬度
      //       const defaultLat = 39.866066; // 北京南站的经纬度
      //       const defaultLng = 116.377235;
      //       map = L.map(mapContainer.value).setView([defaultLat, defaultLng], 13);
      //     }
      //   );
      // } else {
      //   console.error("Geolocation is not supported by this browser.");
      //   // 如果不支持地理位置，使用默认的经纬度
      //   const defaultLat = 39.866066; 
      //   const defaultLng = 116.377235;
      //   map = L.map(mapContainer.value).setView([defaultLat, defaultLng], 13);
      //   L.tileLayer('http://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}', {
      //   subdomains: ['1', '2', '3', '4'],
      //   maxZoom: 19,
      // }).addTo(map);
      // }

      map = L.map(mapContainer.value).setView([parseFloat(39.74), parseFloat(116.18)], 13);

      L.tileLayer('http://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}', {
        subdomains: ['1', '2', '3', '4'],
        maxZoom: 19,
      }).addTo(map);
    });




// 获取路径的函数（使用高德API）


async function fetchRoute(start, end) {
  const url = `https://restapi.amap.com/v3/direction/driving?origin=${start.coords[1]},${start.coords[0]}&destination=${end.coords[1]},${end.coords[0]}&key=${AMAP_API_KEY}`;
  try {
    const response = await fetch(url);
    if (!response.ok) throw new Error('Network response was not ok');
    const data = await response.json();
    const route = data.route.paths[0].steps.map(step => {
      const coords = step.polyline.split(';').map(coord => coord.split(',').map(Number));
      return coords;
    }).flat();
    return route.map(coord_1 => [coord_1[1], coord_1[0]]);
  } catch (error) {
    console.error('Error fetching route:', error);
    return null;
  }
}

// 动态显示路径的函数
async function animatePolylines(routes, map) {
  for (const route of routes) {
    for (let j = 0; j < route.length - 1; j++) {
      const segment = [route[j], route[j + 1]];
      L.polyline(segment, { color: 'blue' }).addTo(map);
      await new Promise(resolve => setTimeout(resolve, 10)); // 延迟1秒
    }
  }
}
</script>

<style scoped>
.map {
  width: 41vw;
  height: 105vh; /* 根据需要调整高度 */
}
</style>
