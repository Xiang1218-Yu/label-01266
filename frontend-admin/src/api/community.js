import request from '@/utils/request'

export function getCommunityTree() {
  return request({
    url: '/community/tree',
    method: 'get'
  })
}

export function searchCommunity(keyword) {
  return request({
    url: '/community/search',
    method: 'get',
    params: { keyword }
  })
}
