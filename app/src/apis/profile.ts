import _axios from '@/plugins/axios'

export const PROFILE_API = {
  getProfile() {
    return _axios({
      method: 'GET',
      url: '/api/profile',
    })
  },

  setProfile(data: any) {
    return _axios({
      method: 'PUT',
      url: '/api/profile',
      data: data,
    })
  },
}
