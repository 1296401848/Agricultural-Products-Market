<template>
  <div class="map-selector">
    <!-- 地址信息显示在地图上方 -->
    <div class="address-info">
      <p class="info-label">当前选择的地址：</p>
      <p class="selected-address">{{ curAddress || '请在地图上点击选择地址' }}</p>
    </div>
    <!-- 地图区域 -->
    <div class="map-container">
      <div class="map" id="map" style="width: 100%; height: 500px"></div>
    </div>
    <div class="action-buttons">
      <el-button @click="$emit('cancel')">取消</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="!curAddress">确认选择</el-button>
    </div>
  </div>
</template>

<script setup>
// 导入高德地图加载器
import AMapLoader from '@amap/amap-jsapi-loader'
import { onMounted, onUnmounted, reactive, ref, shallowRef, watch } from 'vue'
import { getAmapKey } from '@/apis/amap.js'

// 定义组件事件
const emit = defineEmits(['confirm', 'cancel'])

// 保存当前在地图上单击时取得的完整地址
const curAddress = ref(null)
// 详细的地址信息：省、市、区、街道、路、门牌号
const addressDetails = reactive({
  province: '广东省', // 坐标点所在省名称  例如：北京市
  city: '广州市', // 坐标点所在城市名称,请注意：当城市是省直辖县时返回为空，以及城市为北京、上海、天津、重庆四个直辖市时，该字段返回为空；省直辖县列表
  citycode: '', // 城市编码,例如：010
  district: '', // 坐标点所在区,例如：海淀区
  adcode: '', // 行政区编码,例如：110108
  township: '', // 坐标点所在乡镇/街道（此街道为社区街道，不是道路信息）例如：燕园街道
  towncode: '', // 乡镇街道编码,例如：110101001000
  streetNumber: '', // 门牌号
})

// 地图对象: 对象太大，使用浅引用shallowRef（不要监听所有属性的变化）,使用时与ref一样
let AMap = null // 地图类库
let map = null // 地图对象
let geocoder = null // 地理编码对象
let marker = null // 标注点

// 初始化地图
const initMap = async () => {
  try {
    // 从后端获取安全秘钥
    const response = await getAmapKey()
    const securityKey = response.data
    
    // 设置安全秘钥
    window._AMapSecurityConfig = {
      securityJsCode: securityKey,
    }

    AMap = await AMapLoader.load({
      key: 'd6764ca1ccaa85a49362a0519c067a16', // 你在高德地图中申请获取的开发者KEY
      version: '2.0', // 当前使用的版本
      plugins: ['AMap.Geocoder', 'AMap.Marker', 'AMap.Scale', 'AMap.ToolBar'], // 需要加载的插件（类名）,例如标尺：AMap.scale ,缩放工具栏:AMap.ToolBar
    })

    // 创建不可见插件对象待用：地理编码：地址->坐标;坐标->地址
    geocoder = new AMap.Geocoder()
    marker = new AMap.Marker()

    // 创建地图对象: 容器id,选项参数（经纬度坐标[lng,lat]，缩放级别3-20）
    map = new AMap.Map('map', {
      // viewMode: '3D', // 是否为3D地图模式
      // center: [116.397428, 39.90923], // 初始中心点，默认北京,如果不设置，将查询离你最近位置
      zoom: 11, // 初始缩放级别：11-市级，18-建筑物级（小区级）
      resizeEnable: true, // 是否可以缩放
    })

    // 添加可见插件对象
    map.addControl(new AMap.ToolBar())
    map.addControl(new AMap.Scale())

    // 关联单击事件, 单击时，取到经纬度
    map.on('click', clickHandler)
  } catch (error) {
    console.error('初始化地图失败:', error)
    curAddress.value = '初始化地图失败，请稍后重试'
  }
}

const clickHandler = (e) => {
  let point = e.lnglat // 经纬度对象
  // 通过经纬度取得地址
  getAddress(e.lnglat)
}

// 根据经纬度查找地址信息
const getAddress = (point) => {
  geocoder.getAddress(point, (status, result) => {
    // 完成且存在区域编码
    // status (string) 当status为complete时，result为GeocodeResult；
    // 当status为error时，result为错误信息info；当status为no_data时，代表检索返回0结果
    if (status == 'complete' && result.regeocode) {
      // 直接取得格式化的地址 regeo-code
      curAddress.value = result.regeocode.formattedAddress
      const comp = result.regeocode.addressComponent

      console.log(curAddress.value)
      //   console.log(comp)

      for (let props in addressDetails) {
        addressDetails[props] = comp[props] || ''
      }

      console.log(addressDetails)
      // 添加该位置标注

      marker.setPosition(point)
      marker.setTitle(curAddress.value)
      map.add(marker)

      map.setFitView()
    } else {
      curAddress.value = '无法取得当前地址，请配置安全秘钥'
    }
  })
}

const getLocation = (address) => {
  geocoder.getLocation(
    // 关键字：address,例如：广州市
    address,
    // 回调函数
    (status, result) => {
      if (status == 'complete' && result.geocodes.length > 0) {
        // 存在则取第一个
        console.log(result.geocodes) // 输出看结构
        const geo = result.geocodes[0]
        // geo 还包含formattedAddress和addressComponnet属性
        const point = geo.location // 经纬度
        map.setZoomAndCenter(16, point) // 一步飞过去，以新的点为中心

        // 标注
        marker.setPosition(point)
        marker.setTitle(geo.formattedAddress)
        map.add(marker)
      }
    },
    // 可选项：指定城市
     { city: '广州市' }
  )
}

// 确认选择地址
const handleConfirm = () => {
  if (curAddress.value) {
    emit('confirm', {
      fullAddress: curAddress.value,
      ...addressDetails
    })
  }
}

onMounted(() => {
  initMap()
})

onUnmounted(() => {
  if (map) {
    map.destroy()
  }
})


</script>

<style scoped>
.map-selector {
  padding: 1rem;
}

.map-container {
  margin-bottom: 1rem;
}

.address-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  background: #f5f7fa;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.address-info p {
  margin: 0;
}

.info-label {
  font-weight: bold;
  color: #606266;
  min-width: 120px;
}

.selected-address {
  font-weight: bold;
  color: #303133;
  word-break: break-word;
  flex: 1;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

/* ========== 响应式样式 ========== */
@media (max-width: 768px) {
  .map-selector {
    padding: 0.5rem;
  }

  .map {
    height: 350px !important;
  }

  .address-info {
    flex-direction: column;
    gap: 0.5rem;
    padding: 0.75rem;
  }

  .info-label {
    min-width: auto;
  }

  .action-buttons {
    flex-wrap: wrap;
  }
}
</style>