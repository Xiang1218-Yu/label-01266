import request from '@/utils/request'

export function getBuildingStats(buildingId) {
  return request({
    url: `/room/stats/building/${buildingId}`,
    method: 'get'
  })
}

export function getCommunityStats(communityId) {
  return request({
    url: `/room/stats/community/${communityId}`,
    method: 'get'
  })
}

export function getRoomCards(buildingId, params) {
  return request({
    url: `/room/cards/${buildingId}`,
    method: 'get',
    params
  })
}

export function getBuildingFloors(buildingId) {
  return request({
    url: `/room/floors/${buildingId}`,
    method: 'get'
  })
}

export function getRoomDetail(roomId) {
  return request({
    url: `/room/detail/${roomId}`,
    method: 'get'
  })
}

export function saveRoomDetail(data) {
  return request({
    url: '/room/save',
    method: 'post',
    data
  })
}

export function importRooms(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/room/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function exportRooms(params) {
  return request({
    url: '/room/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
