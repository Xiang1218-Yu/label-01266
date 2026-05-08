<template>
  <div class="building-page">
    <!-- 左侧小区树 -->
    <div class="left-panel card">
      <div class="panel-header">
        <span class="title">小区列表</span>
      </div>
      
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索小区"
          :prefix-icon="Search"
          clearable
          @input="handleSearch"
        />
      </div>
      
      <div class="tree-container">
        <el-tree
          ref="treeRef"
          :data="communityTree"
          :props="treeProps"
          node-key="nodeKey"
          highlight-current
          :expand-on-click-node="false"
          @node-click="handleNodeClick"
        >
          <template #default="{ node, data }">
            <span class="tree-node">
              <el-icon v-if="data.type === 'community'"><OfficeBuilding /></el-icon>
              <el-icon v-else><House /></el-icon>
              <span>{{ node.label }}</span>
            </span>
          </template>
        </el-tree>
      </div>
      
      <div class="panel-footer">
        <el-button type="primary" :icon="Upload" @click="handleImport">导入</el-button>
        <el-button :icon="Download" @click="handleExport">导出</el-button>
      </div>
    </div>
    
    <!-- 右侧内容区 -->
    <div class="right-panel">
      <!-- 统计信息 -->
      <div class="stats-row" v-if="currentNode">
        <div class="stat-card">
          <div class="stat-label">总户数</div>
          <div class="stat-value">{{ stats.totalCount || 0 }}</div>
        </div>
        <div class="stat-card rented">
          <div class="stat-label">已出租</div>
          <div class="stat-value">{{ stats.rentedCount || 0 }}</div>
        </div>
        <div class="stat-card vacant">
          <div class="stat-label">空置房</div>
          <div class="stat-value">{{ stats.vacantCount || 0 }}</div>
        </div>
        <div class="stat-card rate">
          <div class="stat-label">入住率</div>
          <div class="stat-value">{{ stats.occupancyRate || 0 }}%</div>
        </div>
      </div>
      
      <!-- 筛选条件 -->
      <div class="filter-bar card" v-if="currentNode?.type === 'building'">
        <el-select v-model="filterFloor" placeholder="选择楼层" clearable style="width: 120px">
          <el-option v-for="f in floors" :key="f" :label="`${f}层`" :value="f" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="选择状态" clearable style="width: 120px">
          <el-option label="已出租" :value="1" />
          <el-option label="空置" :value="0" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadRoomCards">查询</el-button>
        <el-button :icon="Refresh" @click="resetFilter">重置</el-button>
      </div>

      <!-- 房屋卡片列表 -->
      <div class="room-grid" v-if="currentNode?.type === 'building'">
        <div v-if="roomCards.length === 0" class="empty-tip">
          <el-empty description="暂无房源数据" />
        </div>
        <template v-else>
          <div v-for="floor in groupedRooms" :key="floor.floor" class="floor-group">
            <div class="floor-label">{{ floor.floor }}层</div>
            <div class="floor-rooms">
              <div
                v-for="room in floor.rooms"
                :key="room.id"
                class="room-card"
                :class="{ rented: room.status === 1, vacant: room.status === 0 }"
                @click="goToDetail(room.id)"
              >
                <div class="room-number">{{ room.roomNumber }}</div>
                <div class="tenant-name">{{ room.tenantName || '空置' }}</div>
                <el-tag :type="room.status === 1 ? 'success' : 'warning'" size="small">
                  {{ room.status === 1 ? '已出租' : '空置' }}
                </el-tag>
              </div>
            </div>
          </div>
        </template>
      </div>
      
      <!-- 未选择提示 -->
      <div class="empty-tip" v-if="!currentNode">
        <el-empty description="请从左侧选择小区或楼栋" />
      </div>
      
      <!-- 选择小区提示 -->
      <div class="community-tip" v-if="currentNode?.type === 'community'">
        <el-result icon="info" title="请选择楼栋" sub-title="点击左侧楼栋查看房源信息" />
      </div>
    </div>
    
    <!-- 导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="导入房源" width="500px">
      <el-upload
        ref="uploadRef"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">只能上传 xlsx/xls 文件</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="submitImport">确定导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Upload, Download, UploadFilled } from '@element-plus/icons-vue'
import { getCommunityTree, searchCommunity } from '@/api/community'
import { getBuildingStats, getCommunityStats, getRoomCards, getBuildingFloors, importRooms, exportRooms } from '@/api/room'

defineOptions({
  name: 'Building'
})

const router = useRouter()

const treeRef = ref(null)
const uploadRef = ref(null)
const communityTree = ref([])
const currentNode = ref(null)
const stats = ref({})
const roomCards = ref([])
const floors = ref([])
const searchKeyword = ref('')
const filterFloor = ref(null)
const filterStatus = ref(null)
const importDialogVisible = ref(false)
const importLoading = ref(false)
const importFile = ref(null)

const treeProps = {
  children: 'children',
  label: 'name'
}

const groupedRooms = computed(() => {
  const groups = {}
  roomCards.value.forEach(room => {
    if (!groups[room.floor]) {
      groups[room.floor] = { floor: room.floor, rooms: [] }
    }
    groups[room.floor].rooms.push(room)
  })
  return Object.values(groups).sort((a, b) => a.floor - b.floor)
})

onMounted(() => {
  loadCommunityTree()
})

// 页面被激活时（从详情页返回）刷新数据
onActivated(() => {
  if (currentNode.value?.type === 'building') {
    // 刷新统计数据和房屋卡片
    getBuildingStats(currentNode.value.id).then(res => {
      stats.value = res.data || {}
    })
    loadRoomCards()
  } else if (currentNode.value?.type === 'community') {
    getCommunityStats(currentNode.value.id).then(res => {
      stats.value = res.data || {}
    })
  }
})

const loadCommunityTree = async () => {
  try {
    const res = await getCommunityTree()
    communityTree.value = res.data || []
  } catch (error) {
    console.error('加载小区树失败', error)
  }
}

const handleSearch = async () => {
  try {
    const res = await searchCommunity(searchKeyword.value)
    communityTree.value = res.data || []
  } catch (error) {
    console.error('搜索失败', error)
  }
}

const handleNodeClick = async (data) => {
  currentNode.value = data
  
  // 切换节点时重置筛选条件
  filterFloor.value = null
  filterStatus.value = null
  
  if (data.type === 'community') {
    const res = await getCommunityStats(data.id)
    stats.value = res.data || {}
    roomCards.value = []
  } else if (data.type === 'building') {
    const [statsRes, floorsRes] = await Promise.all([
      getBuildingStats(data.id),
      getBuildingFloors(data.id)
    ])
    stats.value = statsRes.data || {}
    floors.value = floorsRes.data || []
    loadRoomCards()
  }
}

const loadRoomCards = async () => {
  if (!currentNode.value || currentNode.value.type !== 'building') return
  
  try {
    const res = await getRoomCards(currentNode.value.id, {
      floor: filterFloor.value,
      status: filterStatus.value
    })
    roomCards.value = res.data || []
  } catch (error) {
    console.error('加载房屋卡片失败', error)
  }
}

const resetFilter = () => {
  filterFloor.value = null
  filterStatus.value = null
  loadRoomCards()
}

const goToDetail = (roomId) => {
  router.push(`/room/${roomId}`)
}

const handleImport = () => {
  importDialogVisible.value = true
  importFile.value = null
  // 清空上传组件的文件列表
  uploadRef.value?.clearFiles()
}

const handleFileChange = (file) => {
  importFile.value = file.raw
}

const submitImport = async () => {
  if (!importFile.value) {
    ElMessage.warning('请选择文件')
    return
  }
  
  importLoading.value = true
  try {
    await importRooms(importFile.value)
    ElMessage.success('导入成功')
    importDialogVisible.value = false
    loadCommunityTree()
    if (currentNode.value) {
      handleNodeClick(currentNode.value)
    }
  } catch (error) {
    // handled in interceptor
  } finally {
    importLoading.value = false
  }
}

const handleExport = async () => {
  try {
    const params = {}
    if (currentNode.value) {
      if (currentNode.value.type === 'community') {
        params.communityId = currentNode.value.id
      } else if (currentNode.value.type === 'building') {
        params.buildingId = currentNode.value.id
      }
    }
    
    const res = await exportRooms(params)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '房源数据.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败', error)
  }
}
</script>


<style lang="scss" scoped>
.building-page {
  display: flex;
  gap: 20px;
  padding: 20px;
  min-height: calc(100vh - 60px);
}

.left-panel {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 100px);
  
  .panel-header {
    padding: 16px;
    border-bottom: 1px solid #e8e8e8;
    
    .title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
  }
  
  .search-box {
    padding: 12px 16px;
  }
  
  .tree-container {
    flex: 1;
    overflow-y: auto;
    padding: 0 8px;
    min-height: 0;
  }
  
  .tree-node {
    display: flex;
    align-items: center;
    gap: 6px;
  }
  
  .panel-footer {
    padding: 12px 16px;
    border-top: 1px solid #e8e8e8;
    display: flex;
    gap: 8px;
    flex-shrink: 0;
    
    .el-button {
      flex: 1;
    }
  }
}

.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: calc(100vh - 100px);
  overflow: hidden;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  
  .stat-card {
    background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
    color: #fff;
    padding: 20px;
    border-radius: 8px;
    
    &.rented {
      background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
    }
    
    &.vacant {
      background: linear-gradient(135deg, #faad14 0%, #d48806 100%);
    }
    
    &.rate {
      background: linear-gradient(135deg, #13c2c2 0%, #08979c 100%);
    }
    
    .stat-label {
      font-size: 14px;
      opacity: 0.9;
      margin-bottom: 8px;
    }
    
    .stat-value {
      font-size: 32px;
      font-weight: 600;
    }
  }
}

.filter-bar {
  padding: 16px;
  display: flex;
  gap: 12px;
  align-items: center;
  flex-shrink: 0;
}

.room-grid {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.floor-group {
  margin-bottom: 20px;
  
  .floor-label {
    font-size: 14px;
    font-weight: 600;
    color: #666;
    margin-bottom: 12px;
    padding-left: 8px;
    border-left: 3px solid #1890ff;
  }
  
  .floor-rooms {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 12px;
  }
}

.room-card {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  &.rented {
    border-left: 3px solid #52c41a;
  }
  
  &.vacant {
    border-left: 3px solid #faad14;
  }
  
  .room-number {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin-bottom: 4px;
  }
  
  .tenant-name {
    font-size: 13px;
    color: #666;
    margin-bottom: 8px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.empty-tip, .community-tip {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 8px;
}
</style>
