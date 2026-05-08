<template>
  <div class="room-detail-page">
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
      <h2>房屋详情 - {{ roomDetail.room?.roomNumber }}</h2>
      <div class="header-actions">
        <template v-if="!isEditing">
          <el-button type="primary" :icon="Edit" @click="startEdit">修改</el-button>
          <el-button type="warning" :icon="Refresh" @click="startChangeTenant" :disabled="formData.status !== 1">更换承租人</el-button>
        </template>
        <template v-else>
          <el-button type="primary" :icon="Check" :loading="saving" @click="handleSave">保存</el-button>
          <el-button :icon="Close" @click="cancelEdit">取消</el-button>
        </template>
      </div>
    </div>
    
    <div class="detail-content">
      <!-- 房屋信息 -->
      <div class="form-section">
        <div class="section-title">
          <el-icon><House /></el-icon>
          房屋信息
        </div>
        <el-form ref="roomFormRef" :model="formData" :rules="roomRules" label-width="100px" :disabled="!isEditing">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="面积(㎡)" prop="area">
                <el-input-number v-model="formData.area" :min="0" :precision="2" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="租金(元/月)" prop="rent">
                <el-input-number v-model="formData.rent" :min="0" :precision="2" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="状态">
                <el-select v-model="formData.status" style="width: 100%" @change="onStatusChange">
                  <el-option label="空置" :value="0" />
                  <el-option label="已出租" :value="1" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="备注">
                <el-input v-model="formData.roomRemark" type="textarea" :rows="2" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
      
      <!-- 承租人信息 -->
      <div class="form-section">
        <div class="section-title">
          <el-icon><User /></el-icon>
          承租人信息
          <el-tag v-if="isEditing && formData.status === 0" type="info" size="small" style="margin-left: 12px">
            空置状态无需填写承租人信息
          </el-tag>
        </div>
        <el-form ref="tenantFormRef" :model="formData" :rules="tenantRules" label-width="100px" :disabled="!isEditing || formData.status === 0">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="姓名" prop="tenantName">
                <el-input v-model="formData.tenantName" maxlength="50" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="formData.idCard" maxlength="18" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="婚姻状况">
                <el-select v-model="formData.maritalStatus" style="width: 100%">
                  <el-option label="未婚" :value="0" />
                  <el-option label="已婚" :value="1" />
                  <el-option label="离异" :value="2" />
                  <el-option label="丧偶" :value="3" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="家庭人口" prop="familySize">
                <el-input-number v-model="formData.familySize" :min="1" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="联系方式" prop="phone">
                <el-input v-model="formData.phone" maxlength="11" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="收入状况">
                <el-select v-model="formData.incomeStatus" style="width: 100%">
                  <el-option label="低收入" :value="0" />
                  <el-option label="中等收入" :value="1" />
                  <el-option label="高收入" :value="2" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="所属社区">
                <el-input v-model="formData.communityBelong" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="入住时间">
                <el-date-picker v-model="formData.checkInTime" type="datetime" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="是否残疾">
                <el-select v-model="formData.isDisabled" style="width: 100%">
                  <el-option label="否" :value="0" />
                  <el-option label="是" :value="1" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="拆迁/轮候">
                <el-select v-model="formData.relocationType" style="width: 100%">
                  <el-option label="无" :value="0" />
                  <el-option label="拆迁" :value="1" />
                  <el-option label="轮候" :value="2" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="出售/置换">
                <el-select v-model="formData.saleExchange" style="width: 100%">
                  <el-option label="无" :value="0" />
                  <el-option label="出售" :value="1" />
                  <el-option label="置换" :value="2" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="备注">
                <el-input v-model="formData.tenantRemark" type="textarea" :rows="2" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <!-- 家庭成员信息 -->
      <div class="form-section">
        <div class="section-title">
          <el-icon><UserFilled /></el-icon>
          家庭成员信息
          <el-button v-if="isEditing && formData.status === 1" type="primary" size="small" :icon="Plus" @click="addMember" class="add-member-btn">
            新增成员
          </el-button>
        </div>
        <el-table :data="formData.familyMembers" border stripe>
          <el-table-column prop="name" label="姓名" width="150">
            <template #default="{ row }">
              <el-input v-if="isEditing && formData.status === 1" v-model="row.name" size="small" />
              <span v-else>{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="idCard" label="身份证号" width="200">
            <template #default="{ row }">
              <el-input v-if="isEditing && formData.status === 1" v-model="row.idCard" size="small" />
              <span v-else>{{ row.idCard }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="relationship" label="与承租人关系">
            <template #default="{ row }">
              <el-input v-if="isEditing && formData.status === 1" v-model="row.relationship" size="small" />
              <span v-else>{{ row.relationship }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" v-if="isEditing && formData.status === 1">
            <template #default="{ $index }">
              <el-button type="danger" size="small" :icon="Delete" @click="removeMember($index)" />
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无家庭成员" :image-size="60" />
          </template>
        </el-table>
      </div>
      
      <!-- 操作记录 -->
      <div class="form-section">
        <div class="section-title">
          <el-icon><Document /></el-icon>
          操作记录
        </div>
        <el-table :data="roomDetail.operationLogs" border stripe max-height="300">
          <el-table-column prop="operatorName" label="操作人" width="100" />
          <el-table-column prop="operationTime" label="操作时间" width="180" />
          <el-table-column prop="operationType" label="操作类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getOpTypeTag(row.operationType)">{{ row.operationType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" show-overflow-tooltip />
          <template #empty>
            <el-empty description="暂无操作记录" :image-size="60" />
          </template>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Edit, Refresh, Check, Close, Plus, Delete } from '@element-plus/icons-vue'
import { getRoomDetail, saveRoomDetail } from '@/api/room'

const route = useRoute()
const router = useRouter()

const roomId = ref(route.params.id)
const roomDetail = ref({})
const isEditing = ref(false)
const isChangeTenant = ref(false)
const saving = ref(false)

const roomFormRef = ref(null)
const tenantFormRef = ref(null)

// 身份证号正则
const idCardPattern = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/
// 手机号正则
const phonePattern = /^1[3-9]\d{9}$/

// 房屋信息校验规则
const roomRules = {
  area: [
    { type: 'number', min: 0, message: '面积不能为负数', trigger: 'blur' }
  ],
  rent: [
    { type: 'number', min: 0, message: '租金不能为负数', trigger: 'blur' }
  ]
}

// 承租人信息校验规则
const tenantRules = {
  tenantName: [
    { max: 50, message: '姓名长度不能超过50', trigger: 'blur' }
  ],
  idCard: [
    { pattern: idCardPattern, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  familySize: [
    { type: 'number', min: 1, message: '家庭人口至少为1', trigger: 'blur' }
  ],
  phone: [
    { pattern: phonePattern, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const formData = reactive({
  roomId: null,
  area: null,
  rent: null,
  status: 0,
  roomRemark: '',
  tenantId: null,
  tenantName: '',
  idCard: '',
  maritalStatus: null,
  familySize: 1,
  phone: '',
  incomeStatus: null,
  communityBelong: '',
  checkInTime: null,
  isDisabled: 0,
  relocationType: 0,
  saleExchange: 0,
  tenantRemark: '',
  familyMembers: [],
  changeTenant: false
})

onMounted(() => {
  loadRoomDetail()
})

const loadRoomDetail = async () => {
  try {
    const res = await getRoomDetail(roomId.value)
    roomDetail.value = res.data || {}
    fillFormData(res.data)
  } catch (error) {
    console.error('加载房屋详情失败', error)
  }
}

const fillFormData = (data) => {
  if (!data) return
  
  const room = data.room || {}
  const tenant = data.tenant || {}
  const members = data.familyMembers || []
  
  formData.roomId = room.id
  formData.area = room.area
  formData.rent = room.rent
  formData.status = room.status
  formData.roomRemark = room.remark
  
  formData.tenantId = tenant.id
  formData.tenantName = tenant.name
  formData.idCard = tenant.idCard
  formData.maritalStatus = tenant.maritalStatus
  formData.familySize = tenant.familySize || 1
  formData.phone = tenant.phone
  formData.incomeStatus = tenant.incomeStatus
  formData.communityBelong = tenant.communityBelong
  formData.checkInTime = tenant.checkInTime
  formData.isDisabled = tenant.isDisabled
  formData.relocationType = tenant.relocationType
  formData.saleExchange = tenant.saleExchange
  formData.tenantRemark = tenant.remark
  
  formData.familyMembers = members.map(m => ({ ...m }))
  formData.changeTenant = false
}

const startEdit = () => {
  isEditing.value = true
  isChangeTenant.value = false
  formData.changeTenant = false
}

const startChangeTenant = async () => {
  try {
    await ElMessageBox.confirm('确定要更换承租人吗？原承租人信息将被清空。', '提示', {
      type: 'warning'
    })
    isEditing.value = true
    isChangeTenant.value = true
    formData.changeTenant = true
    
    // 清空承租人信息
    formData.tenantId = null
    formData.tenantName = ''
    formData.idCard = ''
    formData.maritalStatus = null
    formData.familySize = 1
    formData.phone = ''
    formData.incomeStatus = null
    formData.communityBelong = ''
    formData.checkInTime = null
    formData.isDisabled = 0
    formData.relocationType = 0
    formData.saleExchange = 0
    formData.tenantRemark = ''
    formData.familyMembers = []
  } catch {
    // cancelled
  }
}

const cancelEdit = () => {
  isEditing.value = false
  isChangeTenant.value = false
  fillFormData(roomDetail.value)
}

const onStatusChange = (status) => {
  if (status === 0) {
    // 切换到空置状态，清空承租人信息
    formData.tenantId = null
    formData.tenantName = ''
    formData.idCard = ''
    formData.maritalStatus = null
    formData.familySize = 1
    formData.phone = ''
    formData.incomeStatus = null
    formData.communityBelong = ''
    formData.checkInTime = null
    formData.isDisabled = 0
    formData.relocationType = 0
    formData.saleExchange = 0
    formData.tenantRemark = ''
    formData.familyMembers = []
  }
}

const handleSave = async () => {
  // 表单校验
  try {
    await roomFormRef.value?.validate()
    await tenantFormRef.value?.validate()
  } catch {
    ElMessage.warning('请检查表单填写是否正确')
    return
  }
  
  // 已出租状态下，承租人信息必填校验
  if (formData.status === 1) {
    if (!formData.tenantName || !formData.tenantName.trim()) {
      ElMessage.warning('已出租状态下，承租人姓名不能为空')
      return
    }
    if (!formData.idCard || !formData.idCard.trim()) {
      ElMessage.warning('已出租状态下，承租人身份证号不能为空')
      return
    }
    if (!idCardPattern.test(formData.idCard)) {
      ElMessage.warning('身份证号格式不正确')
      return
    }
    if (!formData.phone || !formData.phone.trim()) {
      ElMessage.warning('已出租状态下，承租人联系方式不能为空')
      return
    }
    if (!phonePattern.test(formData.phone)) {
      ElMessage.warning('手机号格式不正确')
      return
    }
    if (!formData.checkInTime) {
      ElMessage.warning('已出租状态下，入住时间不能为空')
      return
    }
  }
  
  // 校验家庭成员
  for (let i = 0; i < formData.familyMembers.length; i++) {
    const member = formData.familyMembers[i]
    if (!member.name || !member.name.trim()) {
      ElMessage.warning(`第${i + 1}个家庭成员姓名不能为空`)
      return
    }
    if (member.name.length > 50) {
      ElMessage.warning(`第${i + 1}个家庭成员姓名长度不能超过50`)
      return
    }
    if (!member.idCard || !member.idCard.trim()) {
      ElMessage.warning(`第${i + 1}个家庭成员身份证号不能为空`)
      return
    }
    if (!idCardPattern.test(member.idCard)) {
      ElMessage.warning(`第${i + 1}个家庭成员身份证号格式不正确`)
      return
    }
    if (member.relationship && member.relationship.length > 50) {
      ElMessage.warning(`第${i + 1}个家庭成员关系长度不能超过50`)
      return
    }
  }
  
  saving.value = true
  try {
    await saveRoomDetail(formData)
    ElMessage.success('保存成功')
    isEditing.value = false
    isChangeTenant.value = false
    loadRoomDetail()
  } catch (error) {
    // handled in interceptor
  } finally {
    saving.value = false
  }
}

const addMember = () => {
  formData.familyMembers.push({
    name: '',
    idCard: '',
    relationship: ''
  })
}

const removeMember = (index) => {
  formData.familyMembers.splice(index, 1)
}

const goBack = () => {
  router.push('/building')
}

const getOpTypeTag = (type) => {
  const map = {
    '新增': 'success',
    '修改': 'primary',
    '更换承租人': 'warning',
    '删除': 'danger'
  }
  return map[type] || 'info'
}
</script>

<style lang="scss" scoped>
.room-detail-page {
  padding: 20px;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  
  h2 {
    flex: 1;
    margin: 0;
    font-size: 18px;
    color: #333;
  }
  
  .header-actions {
    display: flex;
    gap: 8px;
  }
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e8e8e8;
  
  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid #e8e8e8;
    
    .el-icon {
      color: #1890ff;
    }
    
    .add-member-btn {
      margin-left: auto;
      
      :deep(.el-icon) {
        color: #fff !important;
      }
    }
  }
}
</style>
